use serde::{Deserialize, Serialize};
use std::fs;
use std::path::PathBuf;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum ConfigError {
    #[error("Failed to read config: {0}")]
    ReadError(#[from] std::io::Error),
    #[error("Failed to parse config: {0}")]
    ParseError(#[from] serde_json::Error),
    #[error("Config directory not found")]
    NoDirError,
}

/// Application configuration
#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct AppConfig {
    /// Path to WoW installation directory
    pub wow_path: Option<String>,
    /// WoW account name (folder name in WTF/Account/)
    pub account_name: Option<String>,
    /// Backend API URL
    #[serde(default = "default_api_url")]
    pub api_url: String,
    /// API authentication key
    pub api_key: Option<String>,
    /// Guild ID for syncing
    pub guild_id: Option<String>,
    /// Enable automatic sync on file change
    #[serde(default = "default_true")]
    pub auto_sync: bool,
    /// Show desktop notifications
    #[serde(default = "default_true")]
    pub notifications_enabled: bool,
    /// Start minimized to tray
    #[serde(default)]
    pub start_minimized: bool,
    /// Start with Windows
    #[serde(default)]
    pub start_with_windows: bool,
}

fn default_api_url() -> String {
    "https://api.edgerush.gg".to_string()
}

fn default_true() -> bool {
    true
}

impl AppConfig {
    /// Get the config file path
    fn config_path() -> Result<PathBuf, ConfigError> {
        let dirs = directories::ProjectDirs::from("gg", "edgerush", "lootman")
            .ok_or(ConfigError::NoDirError)?;
        let config_dir = dirs.config_dir();
        fs::create_dir_all(config_dir)?;
        Ok(config_dir.join("config.json"))
    }

    /// Load configuration from disk
    pub fn load() -> Result<Self, ConfigError> {
        let path = Self::config_path()?;
        if path.exists() {
            let content = fs::read_to_string(&path)?;
            let config: AppConfig = serde_json::from_str(&content)?;
            Ok(config)
        } else {
            Ok(AppConfig::default())
        }
    }

    /// Save configuration to disk
    pub fn save(&self) -> Result<(), ConfigError> {
        let path = Self::config_path()?;
        let content = serde_json::to_string_pretty(self)?;
        fs::write(&path, content)?;
        Ok(())
    }

    /// Check if the configuration is complete enough to start syncing
    pub fn is_configured(&self) -> bool {
        self.wow_path.is_some() && self.account_name.is_some()
    }

    /// Get the SavedVariables path for our addon
    pub fn saved_variables_path(&self) -> Option<PathBuf> {
        let wow_path = self.wow_path.as_ref()?;
        let account = self.account_name.as_ref()?;
        Some(PathBuf::from(wow_path)
            .join("WTF")
            .join("Account")
            .join(account)
            .join("SavedVariables")
            .join("EdgeRushLootMan.lua"))
    }

    /// Get the character-specific SavedVariables path
    pub fn char_saved_variables_path(&self, realm: &str, character: &str) -> Option<PathBuf> {
        let wow_path = self.wow_path.as_ref()?;
        let account = self.account_name.as_ref()?;
        Some(PathBuf::from(wow_path)
            .join("WTF")
            .join("Account")
            .join(account)
            .join(realm)
            .join(character)
            .join("SavedVariables")
            .join("EdgeRushLootMan.lua"))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_default_config() {
        let config = AppConfig::default();
        assert_eq!(config.api_url, "https://api.edgerush.gg");
        assert!(config.auto_sync);
        assert!(config.notifications_enabled);
        assert!(!config.is_configured());
    }

    #[test]
    fn test_is_configured() {
        let mut config = AppConfig::default();
        assert!(!config.is_configured());

        config.wow_path = Some("/path/to/wow".to_string());
        assert!(!config.is_configured());

        config.account_name = Some("AccountName".to_string());
        assert!(config.is_configured());
    }
}
