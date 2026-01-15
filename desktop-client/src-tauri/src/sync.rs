use std::fs;
use std::path::PathBuf;
use std::sync::Arc;
use std::time::{Duration, Instant};
use thiserror::Error;
use tokio::sync::Mutex;

use crate::api::{ApiClient, ApiError, FlpsResponse};
use crate::config::AppConfig;
use crate::parser::{AddonData, FlpsData, ParserError, SavedVariablesParser, generate_flps_lua};
use crate::watcher::{FileChange, SavedVariablesWatcher, WatcherError};

#[derive(Error, Debug)]
pub enum SyncError {
    #[error("Watcher error: {0}")]
    WatcherError(#[from] WatcherError),
    #[error("Parser error: {0}")]
    ParserError(#[from] ParserError),
    #[error("API error: {0}")]
    ApiError(#[from] ApiError),
    #[error("IO error: {0}")]
    IoError(#[from] std::io::Error),
    #[error("Not configured: {0}")]
    NotConfigured(String),
    #[error("Sync in progress")]
    SyncInProgress,
}

/// Sync service that coordinates file watching, parsing, and API communication
pub struct SyncService {
    config: AppConfig,
    api_client: ApiClient,
    watcher: Option<SavedVariablesWatcher>,
    last_sync: Option<Instant>,
    sync_in_progress: Arc<Mutex<bool>>,
    debounce_duration: Duration,
}

impl SyncService {
    /// Create a new sync service
    pub fn new(config: AppConfig) -> Self {
        let api_client = ApiClient::new(
            config.api_url.clone(),
            config.api_key.clone(),
            config.guild_id.clone(),
        );

        Self {
            config,
            api_client,
            watcher: None,
            last_sync: None,
            sync_in_progress: Arc::new(Mutex::new(false)),
            debounce_duration: Duration::from_secs(5),
        }
    }

    /// Initialize and start the sync service
    pub fn start(&mut self) -> Result<(), SyncError> {
        if !self.config.is_configured() {
            return Err(SyncError::NotConfigured(
                "WoW path and account name required".to_string(),
            ));
        }

        let saved_vars_path = self.config.saved_variables_path()
            .ok_or_else(|| SyncError::NotConfigured("Cannot determine SavedVariables path".to_string()))?;

        let mut watcher = SavedVariablesWatcher::new(saved_vars_path)?;
        watcher.start()?;
        self.watcher = Some(watcher);

        Ok(())
    }

    /// Stop the sync service
    pub fn stop(&mut self) -> Result<(), SyncError> {
        if let Some(ref mut watcher) = self.watcher {
            watcher.stop()?;
        }
        self.watcher = None;
        Ok(())
    }

    /// Check for file changes and sync if needed
    pub async fn check_and_sync(&mut self) -> Result<Option<SyncResult>, SyncError> {
        // Check if auto-sync is enabled
        if !self.config.auto_sync {
            return Ok(None);
        }

        // Check for file changes
        let change = match &self.watcher {
            Some(watcher) => watcher.check_changes(),
            None => return Ok(None),
        };

        if let Some(FileChange::Modified(path)) = change {
            // Debounce rapid changes
            if let Some(last) = self.last_sync {
                if last.elapsed() < self.debounce_duration {
                    return Ok(None);
                }
            }

            // Perform sync
            return self.sync_now().await.map(Some);
        }

        Ok(None)
    }

    /// Perform a sync immediately
    pub async fn sync_now(&mut self) -> Result<SyncResult, SyncError> {
        // Check if sync is already in progress
        let mut in_progress = self.sync_in_progress.lock().await;
        if *in_progress {
            return Err(SyncError::SyncInProgress);
        }
        *in_progress = true;
        drop(in_progress);

        // Ensure cleanup happens
        let result = self.do_sync().await;

        let mut in_progress = self.sync_in_progress.lock().await;
        *in_progress = false;

        result
    }

    /// Internal sync implementation
    async fn do_sync(&mut self) -> Result<SyncResult, SyncError> {
        let saved_vars_path = self.config.saved_variables_path()
            .ok_or_else(|| SyncError::NotConfigured("Cannot determine SavedVariables path".to_string()))?;

        // Parse SavedVariables
        let addon_data = if saved_vars_path.exists() {
            SavedVariablesParser::parse(&saved_vars_path)?
        } else {
            AddonData::default()
        };

        let mut result = SyncResult {
            success: true,
            characters_synced: 0,
            gear_items_synced: 0,
            flps_updated: false,
            message: None,
        };

        // Sync data to server
        if !addon_data.characters.is_empty() || !addon_data.gear.is_empty() {
            match self.api_client.sync_addon_data(&addon_data).await {
                Ok(response) => {
                    result.characters_synced = addon_data.characters.len();
                    result.gear_items_synced = addon_data.gear.len();
                    if let Some(msg) = response.message {
                        result.message = Some(msg);
                    }
                }
                Err(e) => {
                    result.success = false;
                    result.message = Some(format!("Upload failed: {}", e));
                }
            }
        }

        // Fetch FLPS data and write back to SavedVariables
        if let Some(char) = addon_data.characters.first() {
            match self.api_client.get_flps(&char.name, &char.realm).await {
                Ok(flps_response) => {
                    let flps_data: FlpsData = flps_response.into();
                    if let Err(e) = self.write_flps_to_saved_variables(&saved_vars_path, &flps_data) {
                        // Log error but don't fail the sync
                        eprintln!("Failed to write FLPS data: {}", e);
                    } else {
                        result.flps_updated = true;
                    }
                }
                Err(e) => {
                    // Log error but don't fail the sync
                    eprintln!("Failed to fetch FLPS data: {}", e);
                }
            }
        }

        self.last_sync = Some(Instant::now());
        Ok(result)
    }

    /// Write FLPS data back to SavedVariables file
    fn write_flps_to_saved_variables(&self, path: &PathBuf, flps: &FlpsData) -> Result<(), SyncError> {
        // Read existing content
        let content = if path.exists() {
            fs::read_to_string(path)?
        } else {
            // Create initial SavedVariables structure
            r#"
EdgeRushLootManDB = {
}
"#.to_string()
        };

        // Generate new FLPS section
        let flps_lua = generate_flps_lua(flps);

        // Update or insert FLPS section
        let updated_content = if content.contains("[\"flps\"]") {
            // Replace existing FLPS section
            let start = content.find("[\"flps\"]").unwrap();
            let remaining = &content[start..];
            let end = remaining.find("},")
                .map(|i| start + i + 2)
                .unwrap_or(content.len());

            format!("{}{}{}", &content[..start], flps_lua, &content[end..])
        } else {
            // Insert new FLPS section before closing brace
            if let Some(insert_pos) = content.rfind('}') {
                format!(
                    "{}    {}\n{}",
                    &content[..insert_pos],
                    flps_lua,
                    &content[insert_pos..]
                )
            } else {
                content
            }
        };

        fs::write(path, updated_content)?;
        Ok(())
    }

    /// Update configuration and restart if needed
    pub fn update_config(&mut self, config: AppConfig) -> Result<(), SyncError> {
        let was_running = self.watcher.is_some();

        if was_running {
            self.stop()?;
        }

        self.config = config.clone();
        self.api_client.update_config(config.api_key.clone(), config.guild_id.clone());

        if was_running && config.is_configured() {
            self.start()?;
        }

        Ok(())
    }

    /// Get the current sync status
    pub async fn get_status(&self) -> SyncStatus {
        let in_progress = *self.sync_in_progress.lock().await;

        SyncStatus {
            is_running: self.watcher.is_some(),
            is_syncing: in_progress,
            is_configured: self.config.is_configured(),
            auto_sync_enabled: self.config.auto_sync,
            last_sync: self.last_sync.map(|t| t.elapsed().as_secs()),
            watched_path: self.config.saved_variables_path().map(|p| p.display().to_string()),
        }
    }
}

/// Result of a sync operation
#[derive(Debug, Clone, serde::Serialize)]
pub struct SyncResult {
    pub success: bool,
    pub characters_synced: usize,
    pub gear_items_synced: usize,
    pub flps_updated: bool,
    pub message: Option<String>,
}

/// Current sync status
#[derive(Debug, Clone, serde::Serialize)]
pub struct SyncStatus {
    pub is_running: bool,
    pub is_syncing: bool,
    pub is_configured: bool,
    pub auto_sync_enabled: bool,
    pub last_sync: Option<u64>,
    pub watched_path: Option<String>,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_sync_service_creation() {
        let config = AppConfig::default();
        let service = SyncService::new(config);
        assert!(service.watcher.is_none());
    }

    #[tokio::test]
    async fn test_sync_status() {
        let config = AppConfig::default();
        let service = SyncService::new(config);
        let status = service.get_status().await;

        assert!(!status.is_running);
        assert!(!status.is_syncing);
        assert!(!status.is_configured);
    }
}
