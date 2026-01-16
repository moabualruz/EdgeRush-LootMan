// Prevents additional console window on Windows in release
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod api;
mod config;
mod parser;
mod sync;
mod tray;
mod watcher;

use std::path::PathBuf;
use std::sync::Arc;
use tauri::Manager;
use tauri::Emitter;
use tokio::sync::Mutex;

use crate::config::AppConfig;
use crate::sync::{SyncService, SyncStatus as ServiceSyncStatus};
use crate::watcher::{WowInstallation, detect_wow_installations, get_accounts};

/// Application state shared across the app
pub struct AppState {
    pub config: Arc<Mutex<AppConfig>>,
    pub sync_service: Arc<Mutex<Option<SyncService>>>,
}

impl Default for AppState {
    fn default() -> Self {
        Self {
            config: Arc::new(Mutex::new(AppConfig::load().unwrap_or_default())),
            sync_service: Arc::new(Mutex::new(None)),
        }
    }
}

/// Get the current configuration
#[tauri::command]
async fn get_config(state: tauri::State<'_, AppState>) -> Result<AppConfig, String> {
    let config = state.config.lock().await;
    Ok(config.clone())
}

/// Save configuration
#[tauri::command]
async fn save_config(
    state: tauri::State<'_, AppState>,
    config: AppConfig,
) -> Result<(), String> {
    config.save().map_err(|e| e.to_string())?;

    // Update state
    let mut current = state.config.lock().await;
    *current = config.clone();

    // Update sync service if running
    let mut sync_service = state.sync_service.lock().await;
    if let Some(ref mut service) = *sync_service {
        service.update_config(config).map_err(|e| e.to_string())?;
    }

    Ok(())
}

/// Trigger a manual sync
#[tauri::command]
async fn trigger_sync(state: tauri::State<'_, AppState>) -> Result<sync::SyncResult, String> {
    let mut sync_service = state.sync_service.lock().await;
    if let Some(ref mut service) = *sync_service {
        service.sync_now().await.map_err(|e| e.to_string())
    } else {
        // Try to create sync service if not initialized
        let config = state.config.lock().await;
        if config.is_configured() {
            let mut service = SyncService::new(config.clone());
            let result = service.sync_now().await.map_err(|e| e.to_string())?;
            *sync_service = Some(service);
            Ok(result)
        } else {
            Err("Sync service not initialized. Please configure WoW path first.".to_string())
        }
    }
}

/// Get sync status
#[tauri::command]
async fn get_sync_status(state: tauri::State<'_, AppState>) -> Result<ServiceSyncStatus, String> {
    let sync_service = state.sync_service.lock().await;
    if let Some(ref service) = *sync_service {
        Ok(service.get_status())
    } else {
        let config = state.config.lock().await;
        Ok(ServiceSyncStatus {
            is_running: false,
            is_syncing: false,
            is_configured: config.is_configured(),
            auto_sync_enabled: config.auto_sync,
            last_sync: None,
            watched_path: config.saved_variables_path().map(|p| p.display().to_string()),
        })
    }
}

/// WoW installation info for frontend
#[derive(serde::Serialize)]
pub struct WowInstallationInfo {
    path: String,
    flavor: String,
}

/// Detect WoW installation paths
#[tauri::command]
fn detect_wow_paths() -> Vec<WowInstallationInfo> {
    detect_wow_installations()
        .into_iter()
        .map(|i| WowInstallationInfo {
            path: i.path.display().to_string(),
            flavor: i.flavor.to_string(),
        })
        .collect()
}

/// Get accounts for a WoW installation
#[tauri::command]
fn get_wow_accounts(wow_path: String) -> Vec<String> {
    get_accounts(&PathBuf::from(wow_path))
}

mod simc;

/// Generate SimC input for a character
#[tauri::command]
async fn generate_simc_input(
    state: tauri::State<'_, AppState>,
    character_name: String,
    realm: String,
) -> Result<String, String> {
    let config = state.config.lock().await;
    
    // Check if configured
    if !config.is_configured() {
        return Err("App not configured".to_string());
    }

    let saved_vars_path = config.saved_variables_path()
        .ok_or_else(|| "Cannot determine SavedVariables path".to_string())?;

    if !saved_vars_path.exists() {
        return Err("SavedVariables file not found".to_string());
    }

    // Parse DB
    let addon_data = parser::SavedVariablesParser::parse(&saved_vars_path)
        .map_err(|e| format!("Failed to parse SavedVariables: {}", e))?;

    // Find character
    let character = addon_data.characters.iter()
        .find(|c| c.name.eq_ignore_ascii_case(&character_name) && c.realm.eq_ignore_ascii_case(&realm))
        .ok_or_else(|| format!("Character {} not found in SavedVariables", character_name))?;

    // Generate SimC
    Ok(simc::generate_simc(character))
}

/// Get available characters from SavedVariables
#[tauri::command]
async fn get_characters(state: tauri::State<'_, AppState>) -> Result<Vec<String>, String> {
    let config = state.config.lock().await;
    
    if !config.is_configured() {
        return Ok(Vec::new());
    }

    let saved_vars_path = config.saved_variables_path()
        .ok_or_else(|| "Cannot determine SavedVariables path".to_string())?;

    if !saved_vars_path.exists() {
        return Ok(Vec::new());
    }

    let addon_data = parser::SavedVariablesParser::parse(&saved_vars_path)
        .map_err(|e| format!("Failed to parse SavedVariables: {}", e))?;

    Ok(addon_data.characters.into_iter().map(|c| format!("{} - {}", c.name, c.realm)).collect())
}

fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_notification::init())
        .manage(AppState::default())
        .setup(|app| {
            // Set up the system tray
            let _tray = tray::create_tray(app.handle())?;

            // Start the sync service if configured
            let state = app.state::<AppState>();
            let config = tauri::async_runtime::block_on(async {
                state.config.lock().await.clone()
            });

            if config.is_configured() {
                let mut service = SyncService::new(config);
                if let Err(e) = service.start() {
                    eprintln!("Failed to start sync service: {}", e);
                } else {
                    let mut sync_service = tauri::async_runtime::block_on(async {
                        state.sync_service.lock().await
                    });
                    *sync_service = Some(service);
                }
            }

            // Spawn background sync task
            let service_state = state.sync_service.clone();
            let app_handle = app.handle().clone();
            tauri::async_runtime::spawn(async move {
                loop {
                    tokio::time::sleep(std::time::Duration::from_secs(1)).await;
                    
                    let mut guard = service_state.lock().await;
                    if let Some(ref mut service) = *guard {
                        match service.check_and_sync().await {
                            Ok(Some(result)) => {
                                println!("Auto-sync completed: {:?}", result);
                                let _ = app_handle.emit("sync-complete", result);
                            }
                            Ok(None) => {} // No action needed
                            Err(e) => eprintln!("Auto-sync error: {}", e),
                        }
                    }
                }
            });

            Ok(())
        })
        .invoke_handler(tauri::generate_handler![
            get_config,
            save_config,
            trigger_sync,
            get_sync_status,
            detect_wow_paths,
            get_wow_accounts,
            generate_simc_input,
            get_characters,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
