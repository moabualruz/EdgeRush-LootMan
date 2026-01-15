use notify::{Config, Event, EventKind, RecommendedWatcher, RecursiveMode, Watcher};
use std::path::{Path, PathBuf};
use std::sync::mpsc::{channel, Receiver};
use std::time::Duration;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum WatcherError {
    #[error("Failed to create watcher: {0}")]
    CreateError(#[from] notify::Error),
    #[error("Path does not exist: {0}")]
    PathNotFound(PathBuf),
    #[error("WoW installation not found")]
    WowNotFound,
}

/// File watcher for WoW SavedVariables
pub struct SavedVariablesWatcher {
    watcher: RecommendedWatcher,
    receiver: Receiver<Result<Event, notify::Error>>,
    watched_path: PathBuf,
}

impl SavedVariablesWatcher {
    /// Create a new watcher for the given SavedVariables path
    pub fn new(saved_variables_path: PathBuf) -> Result<Self, WatcherError> {
        if !saved_variables_path.parent().map(|p| p.exists()).unwrap_or(false) {
            return Err(WatcherError::PathNotFound(saved_variables_path));
        }

        let (tx, rx) = channel();

        let watcher = RecommendedWatcher::new(
            move |res| {
                let _ = tx.send(res);
            },
            Config::default()
                .with_poll_interval(Duration::from_secs(2))
                .with_compare_contents(false),
        )?;

        Ok(Self {
            watcher,
            receiver: rx,
            watched_path: saved_variables_path,
        })
    }

    /// Start watching the SavedVariables directory
    pub fn start(&mut self) -> Result<(), WatcherError> {
        let watch_dir = self.watched_path.parent().ok_or_else(|| {
            WatcherError::PathNotFound(self.watched_path.clone())
        })?;

        // Create directory if it doesn't exist
        if !watch_dir.exists() {
            std::fs::create_dir_all(watch_dir).map_err(|_| {
                WatcherError::PathNotFound(watch_dir.to_path_buf())
            })?;
        }

        self.watcher.watch(watch_dir, RecursiveMode::NonRecursive)?;
        Ok(())
    }

    /// Stop watching
    pub fn stop(&mut self) -> Result<(), WatcherError> {
        let watch_dir = self.watched_path.parent().ok_or_else(|| {
            WatcherError::PathNotFound(self.watched_path.clone())
        })?;

        self.watcher.unwatch(watch_dir)?;
        Ok(())
    }

    /// Check for file changes (non-blocking)
    pub fn check_changes(&self) -> Option<FileChange> {
        match self.receiver.try_recv() {
            Ok(Ok(event)) => {
                // Only process modify/create events for our target file
                match event.kind {
                    EventKind::Modify(_) | EventKind::Create(_) => {
                        if event.paths.iter().any(|p| p == &self.watched_path) {
                            Some(FileChange::Modified(self.watched_path.clone()))
                        } else {
                            None
                        }
                    }
                    _ => None,
                }
            }
            _ => None,
        }
    }

    /// Get the watched path
    pub fn watched_path(&self) -> &Path {
        &self.watched_path
    }
}

/// Represents a file change event
#[derive(Debug, Clone)]
pub enum FileChange {
    Modified(PathBuf),
}

/// Detect WoW installation paths on the system
pub fn detect_wow_installations() -> Vec<WowInstallation> {
    let mut installations = Vec::new();

    #[cfg(target_os = "windows")]
    {
        // Common Windows installation paths
        let common_paths = vec![
            "C:\\Program Files (x86)\\World of Warcraft",
            "C:\\Program Files\\World of Warcraft",
            "D:\\World of Warcraft",
            "E:\\World of Warcraft",
            "D:\\Games\\World of Warcraft",
            "E:\\Games\\World of Warcraft",
            "C:\\Games\\World of Warcraft",
        ];

        for path in common_paths {
            let path = PathBuf::from(path);
            if let Some(install) = check_wow_installation(&path) {
                installations.push(install);
            }
        }
    }

    #[cfg(target_os = "macos")]
    {
        let common_paths = vec![
            "/Applications/World of Warcraft",
            dirs::home_dir()
                .map(|h| h.join("Applications/World of Warcraft"))
                .unwrap_or_default(),
        ];

        for path in common_paths.into_iter().filter(|p| !p.as_os_str().is_empty()) {
            if let Some(install) = check_wow_installation(&path) {
                installations.push(install);
            }
        }
    }

    installations
}

/// Check if a path contains a valid WoW installation
fn check_wow_installation(base_path: &Path) -> Option<WowInstallation> {
    if !base_path.exists() {
        return None;
    }

    // Check for _retail_ folder (modern WoW)
    let retail_path = base_path.join("_retail_");
    if retail_path.exists() && retail_path.join("WTF").exists() {
        return Some(WowInstallation {
            path: retail_path,
            flavor: WowFlavor::Retail,
        });
    }

    // Check for _classic_ folder
    let classic_path = base_path.join("_classic_");
    if classic_path.exists() && classic_path.join("WTF").exists() {
        return Some(WowInstallation {
            path: classic_path,
            flavor: WowFlavor::Classic,
        });
    }

    // Check for _classic_era_ folder
    let classic_era_path = base_path.join("_classic_era_");
    if classic_era_path.exists() && classic_era_path.join("WTF").exists() {
        return Some(WowInstallation {
            path: classic_era_path,
            flavor: WowFlavor::ClassicEra,
        });
    }

    // Check if base path itself is a WoW installation
    if base_path.join("WTF").exists() {
        return Some(WowInstallation {
            path: base_path.to_path_buf(),
            flavor: WowFlavor::Unknown,
        });
    }

    None
}

/// Get account names from a WoW installation
pub fn get_accounts(wow_path: &Path) -> Vec<String> {
    let account_path = wow_path.join("WTF").join("Account");

    if !account_path.exists() {
        return Vec::new();
    }

    std::fs::read_dir(&account_path)
        .map(|entries| {
            entries
                .filter_map(|entry| entry.ok())
                .filter(|entry| entry.path().is_dir())
                .filter_map(|entry| {
                    entry.file_name().to_str().map(String::from)
                })
                .filter(|name| name != "SavedVariables") // Filter out SavedVariables folder
                .collect()
        })
        .unwrap_or_default()
}

/// Represents a WoW installation
#[derive(Debug, Clone)]
pub struct WowInstallation {
    pub path: PathBuf,
    pub flavor: WowFlavor,
}

/// WoW game flavor
#[derive(Debug, Clone, PartialEq)]
pub enum WowFlavor {
    Retail,
    Classic,
    ClassicEra,
    Unknown,
}

impl std::fmt::Display for WowFlavor {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            WowFlavor::Retail => write!(f, "Retail"),
            WowFlavor::Classic => write!(f, "Classic"),
            WowFlavor::ClassicEra => write!(f, "Classic Era"),
            WowFlavor::Unknown => write!(f, "Unknown"),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_wow_flavor_display() {
        assert_eq!(WowFlavor::Retail.to_string(), "Retail");
        assert_eq!(WowFlavor::Classic.to_string(), "Classic");
        assert_eq!(WowFlavor::ClassicEra.to_string(), "Classic Era");
    }
}
