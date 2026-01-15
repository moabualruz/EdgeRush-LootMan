-- EdgeRush LootMan Constants
-- Contains all constants and configuration values

local ADDON_NAME, ELM = ...

-- Version
ELM.VERSION = "1.0.0"

-- Addon prefix for comms
ELM.COMM_PREFIX = "EdgeRushLM"

-- Equipment slot IDs and names
ELM.SLOTS = {
    [1]  = { id = 1,  name = "Head",      invSlot = "HEADSLOT" },
    [2]  = { id = 2,  name = "Neck",      invSlot = "NECKSLOT" },
    [3]  = { id = 3,  name = "Shoulder",  invSlot = "SHOULDERSLOT" },
    [5]  = { id = 5,  name = "Chest",     invSlot = "CHESTSLOT" },
    [6]  = { id = 6,  name = "Waist",     invSlot = "WAISTSLOT" },
    [7]  = { id = 7,  name = "Legs",      invSlot = "LEGSSLOT" },
    [8]  = { id = 8,  name = "Feet",      invSlot = "FEETSLOT" },
    [9]  = { id = 9,  name = "Wrist",     invSlot = "WRISTSLOT" },
    [10] = { id = 10, name = "Hands",     invSlot = "HANDSSLOT" },
    [11] = { id = 11, name = "Finger1",   invSlot = "FINGER0SLOT" },
    [12] = { id = 12, name = "Finger2",   invSlot = "FINGER1SLOT" },
    [13] = { id = 13, name = "Trinket1",  invSlot = "TRINKET0SLOT" },
    [14] = { id = 14, name = "Trinket2",  invSlot = "TRINKET1SLOT" },
    [15] = { id = 15, name = "Back",      invSlot = "BACKSLOT" },
    [16] = { id = 16, name = "MainHand",  invSlot = "MAINHANDSLOT" },
    [17] = { id = 17, name = "OffHand",   invSlot = "SECONDARYHANDSLOT" },
}

-- Armor types by class
ELM.ARMOR_TYPES = {
    CLOTH  = { "MAGE", "PRIEST", "WARLOCK" },
    LEATHER = { "DEMONHUNTER", "DRUID", "MONK", "ROGUE" },
    MAIL   = { "EVOKER", "HUNTER", "SHAMAN" },
    PLATE  = { "DEATHKNIGHT", "PALADIN", "WARRIOR" },
}

-- Item quality colors
ELM.QUALITY_COLORS = {
    [0] = { r = 0.62, g = 0.62, b = 0.62 }, -- Poor (gray)
    [1] = { r = 1.00, g = 1.00, b = 1.00 }, -- Common (white)
    [2] = { r = 0.12, g = 1.00, b = 0.00 }, -- Uncommon (green)
    [3] = { r = 0.00, g = 0.44, b = 0.87 }, -- Rare (blue)
    [4] = { r = 0.64, g = 0.21, b = 0.93 }, -- Epic (purple)
    [5] = { r = 1.00, g = 0.50, b = 0.00 }, -- Legendary (orange)
    [6] = { r = 0.90, g = 0.80, b = 0.50 }, -- Artifact (artifact)
    [7] = { r = 0.00, g = 0.80, b = 1.00 }, -- Heirloom (heirloom)
}

-- FLPS Colors
ELM.FLPS_COLORS = {
    HIGH    = { r = 0.00, g = 0.80, b = 0.00 }, -- Green (high priority)
    MEDIUM  = { r = 1.00, g = 0.80, b = 0.00 }, -- Yellow (medium)
    LOW     = { r = 1.00, g = 0.30, b = 0.30 }, -- Red (low priority)
}

-- FLPS thresholds for color coding
ELM.FLPS_THRESHOLDS = {
    HIGH = 0.7,
    MEDIUM = 0.4,
}

-- Default database structure
ELM.DB_DEFAULTS = {
    global = {
        version = ELM.VERSION,
        lastSync = nil,
        flpsLeaderboard = {},
        lootHistory = {},
        lootSessions = {},
    },
    profile = {
        minimap = {
            hide = false,
            minimapPos = 220,
            lock = false,
        },
        display = {
            showTooltip = true,
            showLeaderboard = true,
            showUpgradeValue = true,
        },
        lootCouncil = {
            enabled = true,
            autoOpen = true,
            sortByFLPS = true,
            responseTimeout = 60,
            announceAwards = true,
            preferEdgeRush = true,
            -- FLPS component weights (sync with web platform)
            rmsWeight = 0.4,
            ipiWeight = 0.4,
            rdfWeight = 0.2,
            -- Council configuration
            councilMembers = {},
            minQualityForCouncil = 4, -- Epic
        },
        sync = {
            autoExport = true,
            exportOnLogout = true,
        },
    },
    char = {
        characterID = nil,
        class = nil,
        level = nil,
        gear = {},
        bags = {},
        flps = {
            score = 0,
            rms = 0,
            ipi = 0,
            rdf = 0,
            rank = 0,
            lastUpdated = nil,
        },
        wishlist = {},
        lastExport = nil,
    },
}

-- Slash commands
ELM.SLASH_COMMANDS = {
    "/elm",
    "/edgerush",
    "/lootman",
}

-- Events to register
ELM.EVENTS = {
    "PLAYER_LOGIN",
    "PLAYER_LOGOUT",
    "PLAYER_EQUIPMENT_CHANGED",
    "ENCOUNTER_LOOT_RECEIVED",
    "CHAT_MSG_LOOT",
    "ENCOUNTER_END",
    "GROUP_ROSTER_UPDATE",
    "PLAYER_ENTERING_WORLD",
}
