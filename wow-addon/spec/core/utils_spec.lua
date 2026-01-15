-- Utils Module Tests
-- Tests for Core/Utils.lua

local TestEnv = require("spec.mocks.init")

describe("Utils", function()
    local ELM
    local MockWowApi, MockAceLibs

    before_each(function()
        MockWowApi, MockAceLibs = TestEnv:Setup()
        -- Load Constants first (Utils depends on it)
        ELM = TestEnv:LoadModule("Core/Constants.lua")
        -- Then load Utils
        TestEnv:LoadModule("Core/Utils.lua")
        TestEnv:ClearPrintOutput()
    end)

    describe("GetCharacterID", function()
        it("should return Name-Realm format", function()
            MockWowApi:SetPlayer({ name = "TestPlayer", realm = "TestRealm" })
            local charId = ELM.Utils:GetCharacterID()
            assert.equals("TestPlayer-TestRealm", charId)
        end)

        it("should handle special characters in realm name", function()
            MockWowApi:SetPlayer({ name = "Player", realm = "Area 52" })
            local charId = ELM.Utils:GetCharacterID()
            assert.equals("Player-Area 52", charId)
        end)
    end)

    describe("GetCharacterName", function()
        it("should return player name", function()
            MockWowApi:SetPlayer({ name = "MyCharacter" })
            local name = ELM.Utils:GetCharacterName()
            assert.equals("MyCharacter", name)
        end)
    end)

    describe("GetRealmName", function()
        it("should return realm name", function()
            MockWowApi:SetPlayer({ realm = "Illidan" })
            local realm = ELM.Utils:GetRealmName()
            assert.equals("Illidan", realm)
        end)
    end)

    describe("GetCharacterClass", function()
        it("should return class file name", function()
            MockWowApi:SetPlayer({ classFile = "MAGE" })
            local class = ELM.Utils:GetCharacterClass()
            assert.equals("MAGE", class)
        end)
    end)

    describe("GetCharacterSpec", function()
        it("should return spec name", function()
            MockWowApi:SetPlayer({ specId = 1, spec = "Arms" })
            local spec = ELM.Utils:GetCharacterSpec()
            assert.equals("Arms", spec)
        end)

        it("should return Unknown when no spec selected", function()
            MockWowApi:SetPlayer({ specId = nil })
            local spec = ELM.Utils:GetCharacterSpec()
            assert.equals("Unknown", spec)
        end)
    end)

    describe("GetCharacterLevel", function()
        it("should return player level", function()
            MockWowApi:SetPlayer({ level = 80 })
            local level = ELM.Utils:GetCharacterLevel()
            assert.equals(80, level)
        end)
    end)

    describe("GetItemLevel", function()
        it("should return equipped and overall item level", function()
            MockWowApi:SetPlayer({ itemLevel = 500.5, avgItemLevel = 505.0 })
            local equipped, overall = ELM.Utils:GetItemLevel()
            assert.equals(500.5, equipped)
            assert.equals(505.0, overall)
        end)
    end)

    describe("IsInRaid", function()
        it("should return true when in raid", function()
            MockWowApi:SetPlayer({ isInRaid = true })
            assert.is_true(ELM.Utils:IsInRaid())
        end)

        it("should return false when not in raid", function()
            MockWowApi:SetPlayer({ isInRaid = false })
            assert.is_false(ELM.Utils:IsInRaid())
        end)
    end)

    describe("IsRaidLeader", function()
        it("should return true when group leader", function()
            MockWowApi:SetPlayer({ isGroupLeader = true })
            assert.is_true(ELM.Utils:IsRaidLeader())
        end)

        it("should return true when group assistant", function()
            MockWowApi:SetPlayer({ isGroupAssistant = true })
            assert.is_true(ELM.Utils:IsRaidLeader())
        end)

        it("should return false when neither leader nor assistant", function()
            MockWowApi:SetPlayer({ isGroupLeader = false, isGroupAssistant = false })
            assert.is_false(ELM.Utils:IsRaidLeader())
        end)
    end)

    describe("GetRaidRoster", function()
        it("should return empty table when not in raid", function()
            MockWowApi:SetPlayer({ isInRaid = false })
            local roster = ELM.Utils:GetRaidRoster()
            assert.is_table(roster)
            assert.equals(0, #roster)
        end)

        it("should return raid members when in raid", function()
            MockWowApi:SetPlayer({ isInRaid = true })
            MockWowApi:SetRaidRoster({
                { name = "Player1", rank = 2, subgroup = 1, class = "WARRIOR", fileName = "WARRIOR", online = true, role = "TANK", combatRole = "TANK" },
                { name = "Player2", rank = 0, subgroup = 1, class = "PRIEST", fileName = "PRIEST", online = true, role = "HEALER", combatRole = "HEALER" },
            })

            local roster = ELM.Utils:GetRaidRoster()
            assert.equals(2, #roster)
            assert.equals("Player1", roster[1].name)
            assert.equals("WARRIOR", roster[1].class)
            assert.equals("TANK", roster[1].role)
        end)
    end)

    describe("FormatFLPS", function()
        it("should format score as percentage", function()
            local formatted = ELM.Utils:FormatFLPS(0.85)
            assert.equals("85.0%", formatted)
        end)

        it("should handle zero score", function()
            local formatted = ELM.Utils:FormatFLPS(0)
            assert.equals("0.0%", formatted)
        end)

        it("should handle full score", function()
            local formatted = ELM.Utils:FormatFLPS(1.0)
            assert.equals("100.0%", formatted)
        end)
    end)

    describe("FormatTimestamp", function()
        it("should return 'Never' for nil timestamp", function()
            local formatted = ELM.Utils:FormatTimestamp(nil)
            assert.equals("Never", formatted)
        end)

        it("should format timestamp as date string", function()
            local timestamp = os.time({ year = 2025, month = 1, day = 15, hour = 12, min = 30, sec = 45 })
            local formatted = ELM.Utils:FormatTimestamp(timestamp)
            assert.is_string(formatted)
            assert.is_truthy(formatted:match("%d%d%d%d%-%d%d%-%d%d"))
        end)
    end)

    describe("Print", function()
        it("should print with addon prefix", function()
            ELM.Utils:Print("Test message")
            local output = TestEnv:GetPrintOutput()
            assert.equals(1, #output)
            assert.is_truthy(output[1]:match("%[EdgeRush%]"))
            assert.is_truthy(output[1]:match("Test message"))
        end)
    end)

    describe("Debug", function()
        it("should not print when debug mode is off", function()
            ELM.DEBUG = false
            ELM.Utils:Debug("Debug message")
            local output = TestEnv:GetPrintOutput()
            assert.equals(0, #output)
        end)

        it("should print when debug mode is on", function()
            ELM.DEBUG = true
            ELM.Utils:Debug("Debug message")
            local output = TestEnv:GetPrintOutput()
            assert.equals(1, #output)
            assert.is_truthy(output[1]:match("Debug"))
        end)
    end)

    describe("DeepCopy", function()
        it("should copy simple values", function()
            assert.equals(5, ELM.Utils:DeepCopy(5))
            assert.equals("test", ELM.Utils:DeepCopy("test"))
            assert.is_true(ELM.Utils:DeepCopy(true))
        end)

        it("should deep copy tables", function()
            local original = { a = 1, b = { c = 2 } }
            local copy = ELM.Utils:DeepCopy(original)

            assert.equals(1, copy.a)
            assert.equals(2, copy.b.c)

            -- Modify original should not affect copy
            original.b.c = 999
            assert.equals(2, copy.b.c)
        end)

        it("should handle nested tables", function()
            local original = { level1 = { level2 = { level3 = { value = "deep" } } } }
            local copy = ELM.Utils:DeepCopy(original)

            assert.equals("deep", copy.level1.level2.level3.value)
        end)
    end)

    describe("IsTableEmpty", function()
        it("should return true for empty table", function()
            assert.is_true(ELM.Utils:IsTableEmpty({}))
        end)

        it("should return false for non-empty array", function()
            assert.is_false(ELM.Utils:IsTableEmpty({ 1, 2, 3 }))
        end)

        it("should return false for non-empty hash table", function()
            assert.is_false(ELM.Utils:IsTableEmpty({ key = "value" }))
        end)
    end)

    describe("TableCount", function()
        it("should return 0 for empty table", function()
            assert.equals(0, ELM.Utils:TableCount({}))
        end)

        it("should count array elements", function()
            assert.equals(3, ELM.Utils:TableCount({ 1, 2, 3 }))
        end)

        it("should count hash table entries", function()
            assert.equals(2, ELM.Utils:TableCount({ a = 1, b = 2 }))
        end)

        it("should count mixed table entries", function()
            assert.equals(4, ELM.Utils:TableCount({ 1, 2, key1 = "a", key2 = "b" }))
        end)
    end)

    describe("ColorByFLPS", function()
        it("should color high scores green", function()
            local result = ELM.Utils:ColorByFLPS("High", 0.9)
            assert.is_string(result)
            assert.is_truthy(result:match("|cff"))
        end)

        it("should color medium scores yellow", function()
            local result = ELM.Utils:ColorByFLPS("Medium", 0.5)
            assert.is_string(result)
        end)

        it("should color low scores red", function()
            local result = ELM.Utils:ColorByFLPS("Low", 0.2)
            assert.is_string(result)
        end)
    end)

    describe("GetBonusIDs", function()
        it("should return empty table for nil link", function()
            local bonusIDs = ELM.Utils:GetBonusIDs(nil)
            assert.is_table(bonusIDs)
            assert.equals(0, #bonusIDs)
        end)

        it("should parse bonus IDs from item link", function()
            -- Mock item link with bonus IDs
            local itemLink = "|cff0070dd|Hitem:12345:0:0:0:0:0:0:0:80:0:0:0:0:2:1234:5678|h[Test Item]|h|r"
            local bonusIDs = ELM.Utils:GetBonusIDs(itemLink)
            assert.is_table(bonusIDs)
        end)
    end)

    describe("GetGemIDs", function()
        it("should return empty table for nil link", function()
            local gemIDs = ELM.Utils:GetGemIDs(nil)
            assert.is_table(gemIDs)
            assert.equals(0, #gemIDs)
        end)
    end)

    describe("GetEnchantID", function()
        it("should return nil for nil link", function()
            local enchantID = ELM.Utils:GetEnchantID(nil)
            assert.is_nil(enchantID)
        end)
    end)
end)
