-- Constants Module Tests
-- Tests for Core/Constants.lua

local TestEnv = require("spec.mocks.init")

describe("Constants", function()
    local ELM
    local MockWowApi, MockAceLibs

    before_each(function()
        MockWowApi, MockAceLibs = TestEnv:Setup()
        ELM = TestEnv:LoadModule("Core/Constants.lua")
    end)

    describe("VERSION", function()
        it("should have a version string", function()
            assert.is_string(ELM.VERSION)
            assert.is_not_nil(ELM.VERSION:match("%d+%.%d+%.%d+"))
        end)
    end)

    describe("COMM_PREFIX", function()
        it("should have a communication prefix", function()
            assert.is_string(ELM.COMM_PREFIX)
            assert.equals("EdgeRushLM", ELM.COMM_PREFIX)
        end)
    end)

    describe("SLOTS", function()
        it("should define equipment slots", function()
            assert.is_table(ELM.SLOTS)
        end)

        it("should have head slot at position 1", function()
            assert.is_not_nil(ELM.SLOTS[1])
            assert.equals("Head", ELM.SLOTS[1].name)
            assert.equals("HEADSLOT", ELM.SLOTS[1].invSlot)
        end)

        it("should have main hand slot at position 16", function()
            assert.is_not_nil(ELM.SLOTS[16])
            assert.equals("MainHand", ELM.SLOTS[16].name)
        end)

        it("should have off hand slot at position 17", function()
            assert.is_not_nil(ELM.SLOTS[17])
            assert.equals("OffHand", ELM.SLOTS[17].name)
        end)

        it("should define all 16 equipment slots", function()
            local count = 0
            for _ in pairs(ELM.SLOTS) do
                count = count + 1
            end
            assert.equals(16, count)
        end)
    end)

    describe("ARMOR_TYPES", function()
        it("should define armor types", function()
            assert.is_table(ELM.ARMOR_TYPES)
        end)

        it("should list cloth classes", function()
            assert.is_table(ELM.ARMOR_TYPES.CLOTH)
            assert.is_true(#ELM.ARMOR_TYPES.CLOTH > 0)
        end)

        it("should include MAGE in cloth users", function()
            local found = false
            for _, class in ipairs(ELM.ARMOR_TYPES.CLOTH) do
                if class == "MAGE" then
                    found = true
                    break
                end
            end
            assert.is_true(found)
        end)

        it("should include WARRIOR in plate users", function()
            local found = false
            for _, class in ipairs(ELM.ARMOR_TYPES.PLATE) do
                if class == "WARRIOR" then
                    found = true
                    break
                end
            end
            assert.is_true(found)
        end)
    end)

    describe("QUALITY_COLORS", function()
        it("should define quality colors", function()
            assert.is_table(ELM.QUALITY_COLORS)
        end)

        it("should have epic quality color (4)", function()
            assert.is_table(ELM.QUALITY_COLORS[4])
            assert.is_number(ELM.QUALITY_COLORS[4].r)
            assert.is_number(ELM.QUALITY_COLORS[4].g)
            assert.is_number(ELM.QUALITY_COLORS[4].b)
        end)

        it("should have legendary quality color (5)", function()
            assert.is_table(ELM.QUALITY_COLORS[5])
        end)
    end)

    describe("FLPS_COLORS", function()
        it("should define FLPS score colors", function()
            assert.is_table(ELM.FLPS_COLORS)
            assert.is_table(ELM.FLPS_COLORS.HIGH)
            assert.is_table(ELM.FLPS_COLORS.MEDIUM)
            assert.is_table(ELM.FLPS_COLORS.LOW)
        end)

        it("should have RGB values for HIGH", function()
            assert.is_number(ELM.FLPS_COLORS.HIGH.r)
            assert.is_number(ELM.FLPS_COLORS.HIGH.g)
            assert.is_number(ELM.FLPS_COLORS.HIGH.b)
        end)
    end)

    describe("FLPS_THRESHOLDS", function()
        it("should define FLPS thresholds", function()
            assert.is_table(ELM.FLPS_THRESHOLDS)
        end)

        it("should have HIGH threshold", function()
            assert.is_number(ELM.FLPS_THRESHOLDS.HIGH)
            assert.equals(0.7, ELM.FLPS_THRESHOLDS.HIGH)
        end)

        it("should have MEDIUM threshold", function()
            assert.is_number(ELM.FLPS_THRESHOLDS.MEDIUM)
            assert.equals(0.4, ELM.FLPS_THRESHOLDS.MEDIUM)
        end)

        it("should have HIGH > MEDIUM threshold", function()
            assert.is_true(ELM.FLPS_THRESHOLDS.HIGH > ELM.FLPS_THRESHOLDS.MEDIUM)
        end)
    end)

    describe("DB_DEFAULTS", function()
        it("should define database defaults", function()
            assert.is_table(ELM.DB_DEFAULTS)
        end)

        it("should have global defaults", function()
            assert.is_table(ELM.DB_DEFAULTS.global)
        end)

        it("should have profile defaults", function()
            assert.is_table(ELM.DB_DEFAULTS.profile)
        end)

        it("should have char defaults", function()
            assert.is_table(ELM.DB_DEFAULTS.char)
        end)

        it("should define loot council settings", function()
            assert.is_table(ELM.DB_DEFAULTS.profile.lootCouncil)
            assert.is_true(ELM.DB_DEFAULTS.profile.lootCouncil.enabled)
        end)

        it("should have default FLPS weights summing to 1.0", function()
            local lc = ELM.DB_DEFAULTS.profile.lootCouncil
            local sum = lc.rmsWeight + lc.ipiWeight + lc.rdfWeight
            assert.equals(1.0, sum)
        end)

        it("should define character FLPS data structure", function()
            assert.is_table(ELM.DB_DEFAULTS.char.flps)
            assert.equals(0, ELM.DB_DEFAULTS.char.flps.score)
            assert.equals(0, ELM.DB_DEFAULTS.char.flps.rms)
            assert.equals(0, ELM.DB_DEFAULTS.char.flps.ipi)
            assert.equals(0, ELM.DB_DEFAULTS.char.flps.rdf)
        end)
    end)

    describe("SLASH_COMMANDS", function()
        it("should define slash commands", function()
            assert.is_table(ELM.SLASH_COMMANDS)
            assert.is_true(#ELM.SLASH_COMMANDS > 0)
        end)

        it("should include /elm command", function()
            local found = false
            for _, cmd in ipairs(ELM.SLASH_COMMANDS) do
                if cmd == "/elm" then
                    found = true
                    break
                end
            end
            assert.is_true(found)
        end)
    end)

    describe("EVENTS", function()
        it("should define events to register", function()
            assert.is_table(ELM.EVENTS)
            assert.is_true(#ELM.EVENTS > 0)
        end)

        it("should include PLAYER_LOGIN event", function()
            local found = false
            for _, event in ipairs(ELM.EVENTS) do
                if event == "PLAYER_LOGIN" then
                    found = true
                    break
                end
            end
            assert.is_true(found)
        end)

        it("should include equipment change event", function()
            local found = false
            for _, event in ipairs(ELM.EVENTS) do
                if event == "PLAYER_EQUIPMENT_CHANGED" then
                    found = true
                    break
                end
            end
            assert.is_true(found)
        end)
    end)
end)
