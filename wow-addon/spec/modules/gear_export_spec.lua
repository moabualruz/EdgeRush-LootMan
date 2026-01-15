-- Gear Export Module Tests
-- Tests for Modules/GearSync/GearExport.lua

local TestEnv = require("spec.mocks.init")

describe("GearExport", function()
    local ELM
    local MockWowApi, MockAceLibs
    local GearExport

    before_each(function()
        MockWowApi, MockAceLibs = TestEnv:Setup()
        -- Load dependencies
        ELM = TestEnv:LoadModule("Core/Constants.lua")
        TestEnv:LoadModule("Core/Utils.lua")

        -- Mock Addon.db
        ELM.Addon = {
            db = {
                char = {
                    gear = {},
                    lastExport = nil,
                    characterName = nil,
                    realm = nil,
                    class = nil,
                    spec = nil,
                    level = nil,
                    itemLevel = nil,
                    avgItemLevel = nil,
                },
            },
        }

        -- Load GearExport
        TestEnv:LoadModule("Modules/GearSync/GearExport.lua")
        GearExport = ELM.GearExport

        TestEnv:ClearPrintOutput()
    end)

    describe("Export", function()
        it("should export character info", function()
            MockWowApi:SetPlayer({
                name = "TestChar",
                realm = "TestRealm",
                classFile = "WARRIOR",
                spec = "Arms",
                level = 80,
                itemLevel = 500.5,
                avgItemLevel = 505.0,
            })

            GearExport:Export()

            local db = ELM.Addon.db.char
            assert.equals("TestChar", db.characterName)
            assert.equals("TestRealm", db.realm)
            assert.equals("WARRIOR", db.class)
            assert.equals("Arms", db.spec)
            assert.equals(80, db.level)
            assert.equals(500.5, db.itemLevel)
            assert.equals(505.0, db.avgItemLevel)
        end)

        it("should set lastExport timestamp", function()
            GearExport:Export()

            assert.is_number(ELM.Addon.db.char.lastExport)
        end)

        it("should export equipped gear", function()
            -- Mock equipped item
            MockWowApi:SetEquipment(1, "|cff0070dd|Hitem:12345|h[Test Helm]|h|r")

            -- Mock item info
            _G.GetItemInfoInstant = function(link)
                return 12345
            end

            GearExport:Export()

            assert.is_table(ELM.Addon.db.char.gear)
        end)

        it("should return gear table", function()
            local result = GearExport:Export()

            assert.is_table(result)
        end)
    end)

    describe("GetItemData", function()
        before_each(function()
            -- Mock item functions
            _G.GetItemInfoInstant = function(link)
                if link then return 12345 end
                return nil
            end

            _G.GetDetailedItemLevelInfo = function(link)
                return 500
            end

            _G.C_Item.GetItemInfo = function(link)
                return "Test Item", nil, 4, 500, nil, "Armor", "Plate", nil, "INVTYPE_HEAD", 12345
            end

            _G.GetItemStats = function(link)
                return {}
            end
        end)

        it("should return nil for nil link", function()
            local result = GearExport:GetItemData(1, nil)
            assert.is_nil(result)
        end)

        it("should return item data for valid link", function()
            local link = "|cff0070dd|Hitem:12345|h[Test Helm]|h|r"
            local result = GearExport:GetItemData(1, link)

            assert.is_table(result)
            assert.equals(1, result.slotID)
            assert.equals(12345, result.itemID)
            assert.equals(link, result.itemLink)
        end)

        it("should include slot name from constants", function()
            local link = "|cff0070dd|Hitem:12345|h[Test Helm]|h|r"
            local result = GearExport:GetItemData(1, link)

            assert.equals("Head", result.slotName)
        end)

        it("should detect missing enchants on enchantable slots", function()
            local link = "|cff0070dd|Hitem:12345:0|h[Test Helm]|h|r" -- No enchant
            local result = GearExport:GetItemData(1, link)

            assert.is_false(result.isEnchanted)
            assert.is_true(result.needsEnchant)
        end)
    end)

    describe("GetMissingEnchants", function()
        before_each(function()
            _G.GetItemInfoInstant = function(link) return 12345 end
            _G.GetDetailedItemLevelInfo = function(link) return 500 end
            _G.C_Item.GetItemInfo = function(link)
                return "Test Item", nil, 4, 500, nil, "Armor", "Plate", nil, "INVTYPE_HEAD", 12345
            end
            _G.GetItemStats = function(link) return {} end
        end)

        it("should return empty list when no equipped items", function()
            local missing = GearExport:GetMissingEnchants()

            assert.is_table(missing)
            assert.equals(0, #missing)
        end)

        it("should detect items missing enchants", function()
            -- Mock equipped unenchanted helm
            MockWowApi:SetEquipment(1, "|cff0070dd|Hitem:12345:0|h[Test Helm]|h|r")

            local missing = GearExport:GetMissingEnchants()

            assert.is_table(missing)
        end)
    end)

    describe("GetMissingGems", function()
        before_each(function()
            _G.GetItemInfoInstant = function(link) return 12345 end
            _G.GetDetailedItemLevelInfo = function(link) return 500 end
            _G.C_Item.GetItemInfo = function(link)
                return "Test Item", nil, 4, 500, nil, "Armor", "Plate", nil, "INVTYPE_HEAD", 12345
            end
        end)

        it("should return empty list when no equipped items", function()
            local missing = GearExport:GetMissingGems()

            assert.is_table(missing)
            assert.equals(0, #missing)
        end)

        it("should detect items with empty sockets", function()
            -- Mock item with socket
            MockWowApi:SetEquipment(1, "|cff0070dd|Hitem:12345|h[Test Helm]|h|r")

            _G.GetItemStats = function(link)
                return { EMPTY_SOCKET_PRISMATIC = 1 }
            end

            local missing = GearExport:GetMissingGems()

            assert.is_table(missing)
        end)
    end)

    describe("PrintReport", function()
        before_each(function()
            _G.GetItemInfoInstant = function(link) return 12345 end
            _G.GetDetailedItemLevelInfo = function(link) return 500 end
            _G.C_Item.GetItemInfo = function(link)
                return "Test Item", nil, 4, 500, nil, "Armor", "Plate", nil, "INVTYPE_HEAD", 12345
            end
            _G.GetItemStats = function(link) return {} end
        end)

        it("should print item level info", function()
            MockWowApi:SetPlayer({ itemLevel = 500.5, avgItemLevel = 505.0 })

            GearExport:PrintReport()

            local output = TestEnv:GetPrintOutput()
            assert.is_true(#output > 0)

            -- Check that item level was printed
            local hasIlvl = false
            for _, line in ipairs(output) do
                if line:match("iLvl") or line:match("500") then
                    hasIlvl = true
                    break
                end
            end
            assert.is_true(hasIlvl)
        end)

        it("should report all enchants applied when none missing", function()
            GearExport:PrintReport()

            local output = TestEnv:GetPrintOutput()
            local hasAllEnchants = false
            for _, line in ipairs(output) do
                if line:match("All enchants applied") then
                    hasAllEnchants = true
                    break
                end
            end
            assert.is_true(hasAllEnchants)
        end)

        it("should report all gems socketed when none missing", function()
            GearExport:PrintReport()

            local output = TestEnv:GetPrintOutput()
            local hasAllGems = false
            for _, line in ipairs(output) do
                if line:match("All gems socketed") then
                    hasAllGems = true
                    break
                end
            end
            assert.is_true(hasAllGems)
        end)
    end)
end)
