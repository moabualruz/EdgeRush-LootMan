-- EdgeRush LootMan Options
-- Configuration panel using AceConfig

local ADDON_NAME, ELM = ...

ELM.Options = {}

local Options = ELM.Options

-- Build options table
function Options:GetOptionsTable()
    return {
        name = "EdgeRush LootMan",
        handler = ELM.Addon,
        type = "group",
        args = {
            general = {
                name = "General",
                type = "group",
                order = 1,
                args = {
                    headerDisplay = {
                        name = "Display",
                        type = "header",
                        order = 1,
                    },
                    showTooltip = {
                        name = "Show FLPS in Tooltips",
                        desc = "Display FLPS scores when hovering over raid members",
                        type = "toggle",
                        order = 2,
                        get = function() return ELM.Addon.db.profile.display.showTooltip end,
                        set = function(_, val) ELM.Addon.db.profile.display.showTooltip = val end,
                    },
                    showLeaderboard = {
                        name = "Show Leaderboard Button",
                        desc = "Show the leaderboard quick access button",
                        type = "toggle",
                        order = 3,
                        get = function() return ELM.Addon.db.profile.display.showLeaderboard end,
                        set = function(_, val) ELM.Addon.db.profile.display.showLeaderboard = val end,
                    },
                    showUpgradeValue = {
                        name = "Show Upgrade Values",
                        desc = "Display upgrade values in loot council",
                        type = "toggle",
                        order = 4,
                        get = function() return ELM.Addon.db.profile.display.showUpgradeValue end,
                        set = function(_, val) ELM.Addon.db.profile.display.showUpgradeValue = val end,
                    },
                    headerMinimap = {
                        name = "Minimap",
                        type = "header",
                        order = 10,
                    },
                    minimapHide = {
                        name = "Hide Minimap Button",
                        desc = "Hide the minimap button",
                        type = "toggle",
                        order = 11,
                        get = function() return ELM.Addon.db.profile.minimap.hide end,
                        set = function(_, val)
                            ELM.Addon.db.profile.minimap.hide = val
                            if ELM.MinimapButton then
                                ELM.MinimapButton:Refresh()
                            end
                        end,
                    },
                    minimapLock = {
                        name = "Lock Minimap Button",
                        desc = "Prevent moving the minimap button",
                        type = "toggle",
                        order = 12,
                        get = function() return ELM.Addon.db.profile.minimap.lock end,
                        set = function(_, val) ELM.Addon.db.profile.minimap.lock = val end,
                    },
                },
            },
            lootCouncil = {
                name = "Loot Council",
                type = "group",
                order = 2,
                args = {
                    headerCouncil = {
                        name = "Loot Council Settings",
                        type = "header",
                        order = 1,
                    },
                    enabled = {
                        name = "Enable Loot Council",
                        desc = "Enable the built-in loot council features",
                        type = "toggle",
                        order = 2,
                        get = function() return ELM.Addon.db.profile.lootCouncil.enabled end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.enabled = val end,
                    },
                    autoOpen = {
                        name = "Auto-Open on Loot",
                        desc = "Automatically open loot council when boss loot drops",
                        type = "toggle",
                        order = 3,
                        get = function() return ELM.Addon.db.profile.lootCouncil.autoOpen end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.autoOpen = val end,
                    },
                    sortByFLPS = {
                        name = "Sort by FLPS",
                        desc = "Sort candidates by FLPS score (otherwise sort by upgrade value)",
                        type = "toggle",
                        order = 4,
                        get = function() return ELM.Addon.db.profile.lootCouncil.sortByFLPS end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.sortByFLPS = val end,
                    },
                    responseTimeout = {
                        name = "Response Timeout",
                        desc = "Time in seconds to wait for player responses (0 = no timeout)",
                        type = "range",
                        order = 5,
                        min = 0,
                        max = 180,
                        step = 5,
                        get = function() return ELM.Addon.db.profile.lootCouncil.responseTimeout or 60 end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.responseTimeout = val end,
                    },
                    announceAwards = {
                        name = "Announce Awards in Chat",
                        desc = "Announce loot awards in raid chat",
                        type = "toggle",
                        order = 6,
                        get = function() return ELM.Addon.db.profile.lootCouncil.announceAwards end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.announceAwards = val end,
                    },
                    headerRCLC = {
                        name = "RCLootCouncil Integration",
                        type = "header",
                        order = 10,
                    },
                    descRCLC = {
                        name = function()
                            if ELM.RCLCCompat and ELM.RCLCCompat:IsRCLCLoaded() then
                                return "|cff00ff00RCLootCouncil detected.|r Choose whether to use EdgeRush native loot council or integrate with RCLootCouncil."
                            else
                                return "|cffff9900RCLootCouncil not detected.|r EdgeRush native loot council will be used."
                            end
                        end,
                        type = "description",
                        order = 11,
                    },
                    preferEdgeRush = {
                        name = "Use EdgeRush Native Loot Council",
                        desc = "Use EdgeRush's built-in loot council instead of RCLootCouncil. When disabled, FLPS data will be injected into RCLootCouncil.",
                        type = "toggle",
                        order = 12,
                        get = function() return ELM.Addon.db.profile.lootCouncil.preferEdgeRush end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.preferEdgeRush = val end,
                        disabled = function()
                            return not (ELM.RCLCCompat and ELM.RCLCCompat:IsRCLCLoaded())
                        end,
                    },
                    rclcImport = {
                        name = "Import RCLC History",
                        desc = "Import loot history from RCLootCouncil into EdgeRush",
                        type = "execute",
                        order = 13,
                        func = function()
                            if ELM.RCLCCompat then
                                ELM.RCLCCompat:ImportRCLCHistory()
                            end
                        end,
                        disabled = function()
                            return not (ELM.RCLCCompat and ELM.RCLCCompat:IsRCLCLoaded())
                        end,
                    },
                    headerFlpsWeight = {
                        name = "FLPS Weighting",
                        type = "header",
                        order = 20,
                    },
                    descFlps = {
                        name = "Adjust how much each FLPS component affects loot priority. These settings sync with your guild's web configuration.",
                        type = "description",
                        order = 21,
                    },
                    rmsWeight = {
                        name = "RMS Weight (Raider Merit)",
                        desc = "Weight given to Raider Merit Score (attendance, performance)",
                        type = "range",
                        order = 22,
                        min = 0,
                        max = 1,
                        step = 0.05,
                        isPercent = true,
                        get = function() return ELM.Addon.db.profile.lootCouncil.rmsWeight or 0.4 end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.rmsWeight = val end,
                    },
                    ipiWeight = {
                        name = "IPI Weight (Item Priority)",
                        desc = "Weight given to Item Priority Index (item need, wishlist)",
                        type = "range",
                        order = 23,
                        min = 0,
                        max = 1,
                        step = 0.05,
                        isPercent = true,
                        get = function() return ELM.Addon.db.profile.lootCouncil.ipiWeight or 0.4 end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.ipiWeight = val end,
                    },
                    rdfWeight = {
                        name = "RDF Weight (Recency Decay)",
                        desc = "Weight given to Recency Decay Factor (time since last loot)",
                        type = "range",
                        order = 24,
                        min = 0,
                        max = 1,
                        step = 0.05,
                        isPercent = true,
                        get = function() return ELM.Addon.db.profile.lootCouncil.rdfWeight or 0.2 end,
                        set = function(_, val) ELM.Addon.db.profile.lootCouncil.rdfWeight = val end,
                    },
                },
            },
            sync = {
                name = "Sync",
                type = "group",
                order = 3,
                args = {
                    headerSync = {
                        name = "Data Sync Settings",
                        type = "header",
                        order = 1,
                    },
                    descSync = {
                        name = "Gear data is automatically synced via the EdgeRush desktop client.\nInstall the desktop client and configure it to enable automatic syncing.",
                        type = "description",
                        order = 2,
                    },
                    autoExport = {
                        name = "Auto Export Gear",
                        desc = "Automatically export gear data when equipment changes",
                        type = "toggle",
                        order = 3,
                        get = function() return ELM.Addon.db.profile.sync.autoExport end,
                        set = function(_, val) ELM.Addon.db.profile.sync.autoExport = val end,
                    },
                    exportOnLogout = {
                        name = "Export on Logout",
                        desc = "Export gear data when logging out",
                        type = "toggle",
                        order = 4,
                        get = function() return ELM.Addon.db.profile.sync.exportOnLogout end,
                        set = function(_, val) ELM.Addon.db.profile.sync.exportOnLogout = val end,
                    },
                    manualExport = {
                        name = "Export Now",
                        desc = "Manually export gear data to SavedVariables",
                        type = "execute",
                        order = 5,
                        func = function()
                            if ELM.GearExport then
                                ELM.GearExport:Export()
                                ELM.Utils:Print("Gear exported to SavedVariables")
                            end
                        end,
                    },
                },
            },
            about = {
                name = "About",
                type = "group",
                order = 99,
                args = {
                    version = {
                        name = "Version: " .. ELM.VERSION,
                        type = "description",
                        order = 1,
                    },
                    author = {
                        name = "Author: EdgeRush Team",
                        type = "description",
                        order = 2,
                    },
                    website = {
                        name = "Website: https://edgerush.gg",
                        type = "description",
                        order = 3,
                    },
                    spacer = {
                        name = " ",
                        type = "description",
                        order = 4,
                    },
                    description = {
                        name = "EdgeRush LootMan is a guild management addon that provides FLPS-based loot distribution. It integrates with the EdgeRush web platform for complete guild management.",
                        type = "description",
                        order = 5,
                    },
                },
            },
        },
    }
end

-- Register options with AceConfig
function Options:Register()
    local AceConfig = LibStub("AceConfig-3.0")
    local AceConfigDialog = LibStub("AceConfigDialog-3.0")

    AceConfig:RegisterOptionsTable("EdgeRushLootMan", self:GetOptionsTable())
    self.optionsFrame = AceConfigDialog:AddToBlizOptions("EdgeRushLootMan", "EdgeRush LootMan")
end

-- Open options
function Options:Open()
    local AceConfigDialog = LibStub("AceConfigDialog-3.0")
    AceConfigDialog:Open("EdgeRushLootMan")
end

-- Initialize options on addon load
function Options:Initialize()
    self:Register()
end
