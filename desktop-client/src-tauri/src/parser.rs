use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::fs;
use std::path::Path;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum ParserError {
    #[error("Failed to read file: {0}")]
    ReadError(#[from] std::io::Error),
    #[error("Failed to parse Lua: {0}")]
    ParseError(String),
    #[error("Missing required field: {0}")]
    MissingField(String),
    #[error("Invalid data format: {0}")]
    InvalidFormat(String),
}

/// Parser for WoW SavedVariables Lua files
pub struct SavedVariablesParser;

impl SavedVariablesParser {
    /// Parse the EdgeRushLootMan SavedVariables file
    pub fn parse(path: &Path) -> Result<AddonData, ParserError> {
        let content = fs::read_to_string(path)?;
        Self::parse_content(&content)
    }

    /// Parse SavedVariables content string
    pub fn parse_content(content: &str) -> Result<AddonData, ParserError> {
        // Simple Lua table parser - handles the basic SavedVariables format
        let mut data = AddonData::default();

        // Extract EdgeRushLootManDB table
        if let Some(db_start) = content.find("EdgeRushLootManDB = {") {
            let db_content = Self::extract_table(&content[db_start..])?;
            data = Self::parse_addon_db(&db_content)?;
        }

        Ok(data)
    }

    /// Extract a Lua table from content
    fn extract_table(content: &str) -> Result<String, ParserError> {
        let start = content.find('{').ok_or_else(|| {
            ParserError::ParseError("No opening brace found".to_string())
        })?;

        let mut depth = 0;
        let mut end = start;

        for (i, c) in content[start..].char_indices() {
            match c {
                '{' => depth += 1,
                '}' => {
                    depth -= 1;
                    if depth == 0 {
                        end = start + i + 1;
                        break;
                    }
                }
                _ => {}
            }
        }

        if depth != 0 {
            return Err(ParserError::ParseError("Unbalanced braces".to_string()));
        }

        Ok(content[start..end].to_string())
    }

    /// Parse the addon database table
    fn parse_addon_db(content: &str) -> Result<AddonData, ParserError> {
        let mut data = AddonData::default();

        // Parse character data
        if let Some(chars_start) = content.find("[\"characters\"]") {
            if let Ok(chars_table) = Self::extract_table(&content[chars_start..]) {
                data.characters = Self::parse_characters(&chars_table)?;
            }
        }

        // Parse gear data
        if let Some(gear_start) = content.find("[\"gear\"]") {
            if let Ok(gear_table) = Self::extract_table(&content[gear_start..]) {
                data.gear = Self::parse_gear(&gear_table)?;
            }
        }

        // Parse FLPS data (received from server)
        if let Some(flps_start) = content.find("[\"flps\"]") {
            if let Ok(flps_table) = Self::extract_table(&content[flps_start..]) {
                data.flps = Self::parse_flps(&flps_table)?;
            }
        }

        // Parse sync timestamp
        if let Some(sync_match) = Self::extract_string_value(content, "lastSync") {
            data.last_sync = Some(sync_match);
        }

        // Parse version
        if let Some(version) = Self::extract_string_value(content, "version") {
            data.version = Some(version);
        }

        Ok(data)
    }

    /// Parse character data
    fn parse_characters(content: &str) -> Result<Vec<CharacterData>, ParserError> {
        let mut characters = Vec::new();

        // Find character entries in format: ["CharName-Realm"] = { ... }
        let re_pattern = r#"\["([^"]+)"\]\s*=\s*\{"#;

        let mut pos = 0;
        while let Some(key_start) = content[pos..].find("[\"") {
            let abs_start = pos + key_start;

            // Extract key
            let key_end = content[abs_start + 2..].find("\"]")
                .map(|i| abs_start + 2 + i)
                .unwrap_or(content.len());

            let key = &content[abs_start + 2..key_end];

            // Check if this looks like a character key (Name-Realm format)
            if key.contains('-') && !key.starts_with("item") {
                if let Ok(table) = Self::extract_table(&content[key_end..]) {
                    if let Ok(char_data) = Self::parse_single_character(key, &table) {
                        characters.push(char_data);
                    }
                }
            }

            pos = key_end + 1;
            if pos >= content.len() {
                break;
            }
        }

        Ok(characters)
    }

    /// Parse a single character entry
    fn parse_single_character(key: &str, content: &str) -> Result<CharacterData, ParserError> {
        let parts: Vec<&str> = key.splitn(2, '-').collect();
        let (name, realm) = if parts.len() == 2 {
            (parts[0].to_string(), parts[1].to_string())
        } else {
            (key.to_string(), "Unknown".to_string())
        };

        Ok(CharacterData {
            name,
            realm,
            class: Self::extract_string_value(content, "class").unwrap_or_default(),
            spec: Self::extract_string_value(content, "spec").unwrap_or_default(),
            level: Self::extract_number_value(content, "level").unwrap_or(0.0) as u32,
            item_level: Self::extract_number_value(content, "itemLevel").unwrap_or(0.0),
        })
    }

    /// Parse gear data
    fn parse_gear(content: &str) -> Result<Vec<GearItem>, ParserError> {
        let mut items = Vec::new();

        // Parse gear items in format: [slotId] = { itemId = ..., itemLevel = ..., ... }
        for slot_id in 1..=19 {
            let slot_key = format!("[{}]", slot_id);
            if let Some(slot_start) = content.find(&slot_key) {
                if let Ok(item_table) = Self::extract_table(&content[slot_start..]) {
                    if let Ok(item) = Self::parse_gear_item(slot_id, &item_table) {
                        items.push(item);
                    }
                }
            }
        }

        Ok(items)
    }

    /// Parse a single gear item
    fn parse_gear_item(slot_id: u32, content: &str) -> Result<GearItem, ParserError> {
        Ok(GearItem {
            slot_id,
            slot_name: slot_id_to_name(slot_id),
            item_id: Self::extract_number_value(content, "itemId")
                .map(|v| v as i64)
                .unwrap_or(0),
            item_level: Self::extract_number_value(content, "itemLevel")
                .map(|v| v as u32)
                .unwrap_or(0),
            quality: Self::extract_number_value(content, "quality")
                .map(|v| v as u32)
                .unwrap_or(0),
            enchant_id: Self::extract_number_value(content, "enchantId")
                .map(|v| v as i64),
            gem_ids: Self::extract_array_values(content, "gems")
                .unwrap_or_default()
                .into_iter()
                .map(|v| v as i64)
                .collect(),
            bonus_ids: Self::extract_array_values(content, "bonusIds")
                .unwrap_or_default()
                .into_iter()
                .map(|v| v as i64)
                .collect(),
        })
    }

    /// Parse FLPS data
    fn parse_flps(content: &str) -> Result<Option<FlpsData>, ParserError> {
        let score = match Self::extract_number_value(content, "score") {
            Some(s) => s,
            None => return Ok(None),
        };

        Ok(Some(FlpsData {
            score,
            rms: Self::extract_number_value(content, "rms").unwrap_or(0.0),
            ipi: Self::extract_number_value(content, "ipi").unwrap_or(0.0),
            rdf: Self::extract_number_value(content, "rdf").unwrap_or(0.0),
            rank: Self::extract_number_value(content, "rank")
                .map(|v| v as u32)
                .unwrap_or(0),
            last_updated: Self::extract_string_value(content, "lastUpdated"),
        }))
    }

    /// Extract a string value from Lua content
    fn extract_string_value(content: &str, key: &str) -> Option<String> {
        let patterns = [
            format!("[\"{}\"] = \"", key),
            format!("{} = \"", key),
        ];

        for pattern in patterns {
            if let Some(start) = content.find(&pattern) {
                let value_start = start + pattern.len();
                if let Some(end) = content[value_start..].find('"') {
                    return Some(content[value_start..value_start + end].to_string());
                }
            }
        }

        None
    }

    /// Extract a number value from Lua content
    fn extract_number_value(content: &str, key: &str) -> Option<f64> {
        let patterns = [
            format!("[\"{}\"] = ", key),
            format!("{} = ", key),
        ];

        for pattern in patterns {
            if let Some(start) = content.find(&pattern) {
                let value_start = start + pattern.len();
                let value_end = content[value_start..]
                    .find(|c: char| !c.is_numeric() && c != '.' && c != '-')
                    .unwrap_or(content.len() - value_start);

                if let Ok(num) = content[value_start..value_start + value_end].parse::<f64>() {
                    return Some(num);
                }
            }
        }

        None
    }

    /// Extract array values from Lua content
    fn extract_array_values(content: &str, key: &str) -> Option<Vec<f64>> {
        let patterns = [
            format!("[\"{}\"] = {{", key),
            format!("{} = {{", key),
        ];

        for pattern in patterns {
            if let Some(start) = content.find(&pattern) {
                let array_start = start + pattern.len() - 1;
                if let Ok(array_content) = Self::extract_table(&content[array_start..]) {
                    let values: Vec<f64> = array_content
                        .trim_matches(|c| c == '{' || c == '}')
                        .split(',')
                        .filter_map(|s| s.trim().parse::<f64>().ok())
                        .collect();
                    return Some(values);
                }
            }
        }

        None
    }
}

/// Convert slot ID to slot name
fn slot_id_to_name(slot_id: u32) -> String {
    match slot_id {
        1 => "Head",
        2 => "Neck",
        3 => "Shoulder",
        4 => "Shirt",
        5 => "Chest",
        6 => "Waist",
        7 => "Legs",
        8 => "Feet",
        9 => "Wrist",
        10 => "Hands",
        11 => "Finger1",
        12 => "Finger2",
        13 => "Trinket1",
        14 => "Trinket2",
        15 => "Back",
        16 => "MainHand",
        17 => "OffHand",
        18 => "Ranged",
        19 => "Tabard",
        _ => "Unknown",
    }
    .to_string()
}

/// Addon data structure
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct AddonData {
    pub characters: Vec<CharacterData>,
    pub gear: Vec<GearItem>,
    pub flps: Option<FlpsData>,
    pub last_sync: Option<String>,
    pub version: Option<String>,
}

/// Character data
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CharacterData {
    pub name: String,
    pub realm: String,
    pub class: String,
    pub spec: String,
    pub level: u32,
    pub item_level: f64,
}

/// Gear item data
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GearItem {
    pub slot_id: u32,
    pub slot_name: String,
    pub item_id: i64,
    pub item_level: u32,
    pub quality: u32,
    pub enchant_id: Option<i64>,
    pub gem_ids: Vec<i64>,
    pub bonus_ids: Vec<i64>,
}

/// FLPS score data
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FlpsData {
    pub score: f64,
    pub rms: f64,
    pub ipi: f64,
    pub rdf: f64,
    pub rank: u32,
    pub last_updated: Option<String>,
}

/// Generate Lua content for FLPS data to write back to SavedVariables
pub fn generate_flps_lua(flps: &FlpsData) -> String {
    format!(
        r#"["flps"] = {{
    ["score"] = {},
    ["rms"] = {},
    ["ipi"] = {},
    ["rdf"] = {},
    ["rank"] = {},
    ["lastUpdated"] = "{}",
}},"#,
        flps.score,
        flps.rms,
        flps.ipi,
        flps.rdf,
        flps.rank,
        flps.last_updated.as_deref().unwrap_or("")
    )
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_slot_id_to_name_all_slots() {
        assert_eq!(slot_id_to_name(1), "Head");
        assert_eq!(slot_id_to_name(2), "Neck");
        assert_eq!(slot_id_to_name(3), "Shoulder");
        assert_eq!(slot_id_to_name(5), "Chest");
        assert_eq!(slot_id_to_name(6), "Waist");
        assert_eq!(slot_id_to_name(7), "Legs");
        assert_eq!(slot_id_to_name(8), "Feet");
        assert_eq!(slot_id_to_name(9), "Wrist");
        assert_eq!(slot_id_to_name(10), "Hands");
        assert_eq!(slot_id_to_name(11), "Finger1");
        assert_eq!(slot_id_to_name(12), "Finger2");
        assert_eq!(slot_id_to_name(13), "Trinket1");
        assert_eq!(slot_id_to_name(14), "Trinket2");
        assert_eq!(slot_id_to_name(15), "Back");
        assert_eq!(slot_id_to_name(16), "MainHand");
        assert_eq!(slot_id_to_name(17), "OffHand");
        assert_eq!(slot_id_to_name(99), "Unknown");
    }

    #[test]
    fn test_extract_string_value() {
        let content = r#"["version"] = "1.0.0","#;
        assert_eq!(
            SavedVariablesParser::extract_string_value(content, "version"),
            Some("1.0.0".to_string())
        );
    }

    #[test]
    fn test_extract_string_value_alternative_format() {
        let content = r#"version = "2.0.0","#;
        assert_eq!(
            SavedVariablesParser::extract_string_value(content, "version"),
            Some("2.0.0".to_string())
        );
    }

    #[test]
    fn test_extract_string_value_not_found() {
        let content = r#"["other"] = "value","#;
        assert_eq!(
            SavedVariablesParser::extract_string_value(content, "notfound"),
            None
        );
    }

    #[test]
    fn test_extract_number_value() {
        let content = r#"["score"] = 0.85,"#;
        assert_eq!(
            SavedVariablesParser::extract_number_value(content, "score"),
            Some(0.85)
        );
    }

    #[test]
    fn test_extract_number_value_integer() {
        let content = r#"["rank"] = 42,"#;
        assert_eq!(
            SavedVariablesParser::extract_number_value(content, "rank"),
            Some(42.0)
        );
    }

    #[test]
    fn test_extract_number_value_not_found() {
        let content = r#"["other"] = 123,"#;
        assert_eq!(
            SavedVariablesParser::extract_number_value(content, "notfound"),
            None
        );
    }

    #[test]
    fn test_extract_table_simple() {
        let content = r#"data = { a = 1, b = 2 }"#;
        let result = SavedVariablesParser::extract_table(content);
        assert!(result.is_ok());
        assert_eq!(result.unwrap(), "{ a = 1, b = 2 }");
    }

    #[test]
    fn test_extract_table_nested() {
        let content = r#"data = { inner = { value = 1 } }"#;
        let result = SavedVariablesParser::extract_table(content);
        assert!(result.is_ok());
        assert_eq!(result.unwrap(), "{ inner = { value = 1 } }");
    }

    #[test]
    fn test_extract_table_unbalanced() {
        let content = r#"data = { a = 1, b = 2"#;
        let result = SavedVariablesParser::extract_table(content);
        assert!(result.is_err());
    }

    #[test]
    fn test_parse_content_empty() {
        let content = "";
        let result = SavedVariablesParser::parse_content(content);
        assert!(result.is_ok());
        let data = result.unwrap();
        assert!(data.characters.is_empty());
        assert!(data.gear.is_empty());
    }

    #[test]
    fn test_parse_content_with_version() {
        let content = r#"
EdgeRushLootManDB = {
    ["version"] = "1.0.0",
    ["lastSync"] = "2024-01-15T10:00:00Z",
}
"#;
        let result = SavedVariablesParser::parse_content(content);
        assert!(result.is_ok());
        let data = result.unwrap();
        assert_eq!(data.version, Some("1.0.0".to_string()));
        assert_eq!(data.last_sync, Some("2024-01-15T10:00:00Z".to_string()));
    }

    #[test]
    fn test_parse_single_character() {
        let key = "TestChar-TestRealm";
        let content = r#"{
            ["class"] = "WARRIOR",
            ["spec"] = "Arms",
            ["level"] = 80,
            ["itemLevel"] = 500.5,
        }"#;

        let result = SavedVariablesParser::parse_single_character(key, content);
        assert!(result.is_ok());
        let char = result.unwrap();
        assert_eq!(char.name, "TestChar");
        assert_eq!(char.realm, "TestRealm");
        assert_eq!(char.class, "WARRIOR");
        assert_eq!(char.spec, "Arms");
        assert_eq!(char.level, 80);
        assert_eq!(char.item_level, 500.5);
    }

    #[test]
    fn test_parse_single_character_no_realm() {
        let key = "CharacterOnly";
        let content = r#"{ ["class"] = "MAGE" }"#;

        let result = SavedVariablesParser::parse_single_character(key, content);
        assert!(result.is_ok());
        let char = result.unwrap();
        assert_eq!(char.name, "CharacterOnly");
        assert_eq!(char.realm, "Unknown");
    }

    #[test]
    fn test_parse_gear_item() {
        let content = r#"{
            ["itemId"] = 12345,
            ["itemLevel"] = 500,
            ["quality"] = 4,
            ["enchantId"] = 6789,
        }"#;

        let result = SavedVariablesParser::parse_gear_item(1, content);
        assert!(result.is_ok());
        let item = result.unwrap();
        assert_eq!(item.slot_id, 1);
        assert_eq!(item.slot_name, "Head");
        assert_eq!(item.item_id, 12345);
        assert_eq!(item.item_level, 500);
        assert_eq!(item.quality, 4);
        assert_eq!(item.enchant_id, Some(6789));
    }

    #[test]
    fn test_parse_flps() {
        let content = r#"{
            ["score"] = 0.85,
            ["rms"] = 0.4,
            ["ipi"] = 0.3,
            ["rdf"] = 0.15,
            ["rank"] = 5,
            ["lastUpdated"] = "2024-01-15",
        }"#;

        let result = SavedVariablesParser::parse_flps(content);
        assert!(result.is_ok());
        let flps = result.unwrap().unwrap();
        assert_eq!(flps.score, 0.85);
        assert_eq!(flps.rms, 0.4);
        assert_eq!(flps.ipi, 0.3);
        assert_eq!(flps.rdf, 0.15);
        assert_eq!(flps.rank, 5);
    }

    #[test]
    fn test_generate_flps_lua() {
        let flps = FlpsData {
            score: 0.85,
            rms: 0.4,
            ipi: 0.3,
            rdf: 0.15,
            rank: 5,
            last_updated: Some("2024-01-15T10:00:00Z".to_string()),
        };
        let lua = generate_flps_lua(&flps);
        assert!(lua.contains("0.85"));
        assert!(lua.contains("0.4"));
        assert!(lua.contains("0.3"));
        assert!(lua.contains("0.15"));
        assert!(lua.contains("5"));
        assert!(lua.contains("2024-01-15"));
        assert!(lua.contains("[\"flps\"]"));
        assert!(lua.contains("[\"score\"]"));
    }

    #[test]
    fn test_generate_flps_lua_no_last_updated() {
        let flps = FlpsData {
            score: 0.5,
            rms: 0.2,
            ipi: 0.2,
            rdf: 0.1,
            rank: 10,
            last_updated: None,
        };
        let lua = generate_flps_lua(&flps);
        assert!(lua.contains("[\"lastUpdated\"] = \"\""));
    }

    #[test]
    fn test_addon_data_default() {
        let data = AddonData::default();
        assert!(data.characters.is_empty());
        assert!(data.gear.is_empty());
        assert!(data.flps.is_none());
        assert!(data.last_sync.is_none());
        assert!(data.version.is_none());
    }

    #[test]
    fn test_character_data_clone() {
        let char = CharacterData {
            name: "Test".to_string(),
            realm: "Realm".to_string(),
            class: "WARRIOR".to_string(),
            spec: "Arms".to_string(),
            level: 80,
            item_level: 500.0,
        };
        let cloned = char.clone();
        assert_eq!(cloned.name, char.name);
        assert_eq!(cloned.realm, char.realm);
    }

    #[test]
    fn test_gear_item_clone() {
        let item = GearItem {
            slot_id: 1,
            slot_name: "Head".to_string(),
            item_id: 12345,
            item_level: 500,
            quality: 4,
            enchant_id: Some(6789),
            gem_ids: vec![100, 200],
            bonus_ids: vec![10, 20, 30],
        };
        let cloned = item.clone();
        assert_eq!(cloned.slot_id, item.slot_id);
        assert_eq!(cloned.gem_ids.len(), 2);
        assert_eq!(cloned.bonus_ids.len(), 3);
    }

    #[test]
    fn test_extract_array_values() {
        let content = r#"["gems"] = { 100, 200, 300 }"#;
        let result = SavedVariablesParser::extract_array_values(content, "gems");
        assert!(result.is_some());
        let values = result.unwrap();
        assert_eq!(values.len(), 3);
        assert_eq!(values[0], 100.0);
        assert_eq!(values[1], 200.0);
        assert_eq!(values[2], 300.0);
    }

    #[test]
    fn test_extract_array_values_empty() {
        let content = r#"["gems"] = { }"#;
        let result = SavedVariablesParser::extract_array_values(content, "gems");
        assert!(result.is_some());
        let values = result.unwrap();
        assert!(values.is_empty());
    }

    #[test]
    fn test_extract_array_values_not_found() {
        let content = r#"["other"] = { 1, 2, 3 }"#;
        let result = SavedVariablesParser::extract_array_values(content, "gems");
        assert!(result.is_none());
    }
}
