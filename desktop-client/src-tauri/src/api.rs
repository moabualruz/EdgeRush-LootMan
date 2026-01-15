use reqwest::header::{HeaderMap, HeaderValue, AUTHORIZATION, CONTENT_TYPE};
use serde::{Deserialize, Serialize};
use std::time::Duration;
use thiserror::Error;

use crate::parser::{AddonData, FlpsData, GearItem};

#[derive(Error, Debug)]
pub enum ApiError {
    #[error("HTTP request failed: {0}")]
    RequestError(#[from] reqwest::Error),
    #[error("Authentication failed: {0}")]
    AuthError(String),
    #[error("API error: {status} - {message}")]
    ApiError { status: u16, message: String },
    #[error("Invalid configuration: {0}")]
    ConfigError(String),
    #[error("Serialization error: {0}")]
    SerializationError(#[from] serde_json::Error),
}

/// API client for EdgeRush LootMan backend
pub struct ApiClient {
    client: reqwest::Client,
    base_url: String,
    api_key: Option<String>,
    guild_id: Option<String>,
}

impl ApiClient {
    /// Create a new API client
    pub fn new(base_url: String, api_key: Option<String>, guild_id: Option<String>) -> Self {
        let client = reqwest::Client::builder()
            .timeout(Duration::from_secs(30))
            .build()
            .expect("Failed to create HTTP client");

        Self {
            client,
            base_url,
            api_key,
            guild_id,
        }
    }

    /// Build headers for authenticated requests
    fn build_headers(&self) -> Result<HeaderMap, ApiError> {
        let mut headers = HeaderMap::new();
        headers.insert(CONTENT_TYPE, HeaderValue::from_static("application/json"));

        if let Some(ref api_key) = self.api_key {
            headers.insert(
                AUTHORIZATION,
                HeaderValue::from_str(&format!("Bearer {}", api_key))
                    .map_err(|_| ApiError::AuthError("Invalid API key format".to_string()))?,
            );
        }

        Ok(headers)
    }

    /// Get the guild ID or return an error
    fn require_guild_id(&self) -> Result<&str, ApiError> {
        self.guild_id
            .as_deref()
            .ok_or_else(|| ApiError::ConfigError("Guild ID not configured".to_string()))
    }

    /// Check if the client is properly configured
    pub fn is_configured(&self) -> bool {
        self.api_key.is_some() && self.guild_id.is_some()
    }

    /// Test the API connection
    pub async fn test_connection(&self) -> Result<bool, ApiError> {
        let url = format!("{}/health", self.base_url);
        let response = self.client.get(&url).send().await?;
        Ok(response.status().is_success())
    }

    /// Sync character gear data to the server
    pub async fn sync_gear(&self, character_name: &str, realm: &str, gear: &[GearItem]) -> Result<SyncResponse, ApiError> {
        let guild_id = self.require_guild_id()?;
        let url = format!(
            "{}/api/guilds/{}/characters/{}-{}/gear",
            self.base_url, guild_id, character_name, realm
        );

        let payload = GearSyncRequest {
            items: gear.iter().map(|g| GearItemRequest {
                slot_id: g.slot_id,
                item_id: g.item_id,
                item_level: g.item_level,
                quality: g.quality,
                enchant_id: g.enchant_id,
                gem_ids: g.gem_ids.clone(),
                bonus_ids: g.bonus_ids.clone(),
            }).collect(),
        };

        let response = self
            .client
            .post(&url)
            .headers(self.build_headers()?)
            .json(&payload)
            .send()
            .await?;

        if response.status().is_success() {
            Ok(response.json().await?)
        } else {
            let status = response.status().as_u16();
            let message = response.text().await.unwrap_or_default();
            Err(ApiError::ApiError { status, message })
        }
    }

    /// Sync full addon data to the server
    pub async fn sync_addon_data(&self, data: &AddonData) -> Result<SyncResponse, ApiError> {
        let guild_id = self.require_guild_id()?;
        let url = format!("{}/api/guilds/{}/sync/addon", self.base_url, guild_id);

        let payload = AddonSyncRequest {
            characters: data.characters.iter().map(|c| CharacterSyncData {
                name: c.name.clone(),
                realm: c.realm.clone(),
                class: c.class.clone(),
                spec: c.spec.clone(),
                level: c.level,
                item_level: c.item_level,
            }).collect(),
            gear: data.gear.iter().map(|g| GearItemRequest {
                slot_id: g.slot_id,
                item_id: g.item_id,
                item_level: g.item_level,
                quality: g.quality,
                enchant_id: g.enchant_id,
                gem_ids: g.gem_ids.clone(),
                bonus_ids: g.bonus_ids.clone(),
            }).collect(),
            version: data.version.clone(),
            timestamp: chrono::Utc::now().to_rfc3339(),
        };

        let response = self
            .client
            .post(&url)
            .headers(self.build_headers()?)
            .json(&payload)
            .send()
            .await?;

        if response.status().is_success() {
            Ok(response.json().await?)
        } else {
            let status = response.status().as_u16();
            let message = response.text().await.unwrap_or_default();
            Err(ApiError::ApiError { status, message })
        }
    }

    /// Fetch FLPS data for a character
    pub async fn get_flps(&self, character_name: &str, realm: &str) -> Result<FlpsResponse, ApiError> {
        let guild_id = self.require_guild_id()?;
        let url = format!(
            "{}/api/guilds/{}/characters/{}-{}/flps",
            self.base_url, guild_id, character_name, realm
        );

        let response = self
            .client
            .get(&url)
            .headers(self.build_headers()?)
            .send()
            .await?;

        if response.status().is_success() {
            Ok(response.json().await?)
        } else {
            let status = response.status().as_u16();
            let message = response.text().await.unwrap_or_default();
            Err(ApiError::ApiError { status, message })
        }
    }

    /// Fetch FLPS leaderboard for the guild
    pub async fn get_flps_leaderboard(&self) -> Result<FlpsLeaderboardResponse, ApiError> {
        let guild_id = self.require_guild_id()?;
        let url = format!("{}/api/guilds/{}/flps/leaderboard", self.base_url, guild_id);

        let response = self
            .client
            .get(&url)
            .headers(self.build_headers()?)
            .send()
            .await?;

        if response.status().is_success() {
            Ok(response.json().await?)
        } else {
            let status = response.status().as_u16();
            let message = response.text().await.unwrap_or_default();
            Err(ApiError::ApiError { status, message })
        }
    }

    /// Update configuration
    pub fn update_config(&mut self, api_key: Option<String>, guild_id: Option<String>) {
        self.api_key = api_key;
        self.guild_id = guild_id;
    }
}

// Request/Response types

#[derive(Debug, Serialize)]
struct GearSyncRequest {
    items: Vec<GearItemRequest>,
}

#[derive(Debug, Serialize)]
struct GearItemRequest {
    slot_id: u32,
    item_id: i64,
    item_level: u32,
    quality: u32,
    enchant_id: Option<i64>,
    gem_ids: Vec<i64>,
    bonus_ids: Vec<i64>,
}

#[derive(Debug, Serialize)]
struct AddonSyncRequest {
    characters: Vec<CharacterSyncData>,
    gear: Vec<GearItemRequest>,
    version: Option<String>,
    timestamp: String,
}

#[derive(Debug, Serialize)]
struct CharacterSyncData {
    name: String,
    realm: String,
    class: String,
    spec: String,
    level: u32,
    item_level: f64,
}

#[derive(Debug, Deserialize)]
pub struct SyncResponse {
    pub success: bool,
    pub message: Option<String>,
    pub synced_at: Option<String>,
}

#[derive(Debug, Deserialize)]
pub struct FlpsResponse {
    pub character_name: String,
    pub realm: String,
    pub score: f64,
    pub rms: f64,
    pub ipi: f64,
    pub rdf: f64,
    pub rank: u32,
    pub total_raiders: u32,
    pub breakdown: Option<FlpsBreakdown>,
    pub last_updated: String,
}

#[derive(Debug, Deserialize)]
pub struct FlpsBreakdown {
    pub attendance_rate: f64,
    pub performance_score: f64,
    pub loot_count: u32,
    pub days_since_loot: u32,
}

#[derive(Debug, Deserialize)]
pub struct FlpsLeaderboardResponse {
    pub guild_id: String,
    pub guild_name: String,
    pub entries: Vec<FlpsLeaderboardEntry>,
    pub generated_at: String,
}

#[derive(Debug, Deserialize)]
pub struct FlpsLeaderboardEntry {
    pub rank: u32,
    pub character_name: String,
    pub realm: String,
    pub class: String,
    pub spec: String,
    pub score: f64,
    pub rms: f64,
    pub ipi: f64,
    pub rdf: f64,
}

impl From<FlpsResponse> for FlpsData {
    fn from(response: FlpsResponse) -> Self {
        FlpsData {
            score: response.score,
            rms: response.rms,
            ipi: response.ipi,
            rdf: response.rdf,
            rank: response.rank,
            last_updated: Some(response.last_updated),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_api_client_is_configured() {
        let client = ApiClient::new(
            "https://api.example.com".to_string(),
            Some("test-key".to_string()),
            Some("guild-123".to_string()),
        );
        assert!(client.is_configured());

        let unconfigured = ApiClient::new(
            "https://api.example.com".to_string(),
            None,
            None,
        );
        assert!(!unconfigured.is_configured());
    }

    #[test]
    fn test_flps_response_conversion() {
        let response = FlpsResponse {
            character_name: "TestChar".to_string(),
            realm: "TestRealm".to_string(),
            score: 0.85,
            rms: 0.4,
            ipi: 0.3,
            rdf: 0.15,
            rank: 5,
            total_raiders: 25,
            breakdown: None,
            last_updated: "2024-01-15T10:00:00Z".to_string(),
        };

        let flps: FlpsData = response.into();
        assert_eq!(flps.score, 0.85);
        assert_eq!(flps.rank, 5);
    }
}
