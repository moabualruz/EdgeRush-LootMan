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

            Ok(())
        })
        .invoke_handler(tauri::generate_handler![
            get_config,
            save_config,
            trigger_sync,
            get_sync_status,
            detect_wow_paths,
            get_wow_accounts,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
