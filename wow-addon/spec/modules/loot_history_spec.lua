-- Loot History Module Tests
-- Tests for Modules/LootCouncil/LootHistory.lua

local TestEnv = require("spec.mocks.init")

describe("LootHistory", function()
    local ELM
    local MockWowApi, MockAceLibs
    local LootHistory

    before_each(function()
        MockWowApi, MockAceLibs = TestEnv:Setup()
        -- Load dependencies
        ELM = TestEnv:LoadModule("Core/Constants.lua")
        TestEnv:LoadModule("Core/Utils.lua")

        -- Mock frame creation functions
        _G.CreateFrame = function(frameType, name, parent, template)
            return {
                SetSize = function() end,
                SetPoint = function() end,
                SetMovable = function() end,
                EnableMouse = function() end,
                RegisterForDrag = function() end,
                SetScript = function() end,
                SetBackdrop = function() end,
                SetBackdropColor = function() end,
                SetFrameStrata = function() end,
                CreateFontString = function() return { SetPoint = function() end, SetText = function() end, SetTextColor = function() end } end,
                CreateTexture = function() return { SetAllPoints = function() end, SetColorTexture = function() end } end,
                Hide = function() end,
                Show = function() end,
                IsShown = function() return false end,
            }
        end

        _G.GetInstanceInfo = function()
            return "Test Instance"
        end

        _G.UIDropDownMenu_SetWidth = function() end
        _G.UIDropDownMenu_SetText = function() end

        -- Mock Addon.db
        ELM.Addon = {
            db = {
                global = {
                    lootHistory = {},
                    lootSessions = {},
                },
            },
        }

        -- Load LootHistory
        TestEnv:LoadModule("Modules/LootCouncil/LootHistory.lua")
        LootHistory = ELM.LootHistory

        TestEnv:ClearPrintOutput()
    end)

    describe("Initialize", function()
        it("should initialize with empty history", function()
            LootHistory:Initialize()

            assert.is_table(LootHistory.history)
            assert.is_table(LootHistory.sessions)
        end)

        it("should load existing history from database", function()
            ELM.Addon.db.global.lootHistory = {
                { id = "1", winner = "Player1", itemName = "Test Item" },
            }

            LootHistory:Initialize()

            assert.equals(1, #LootHistory.history)
        end)
    end)

    describe("RecordAward", function()
        before_each(function()
            LootHistory:Initialize()
        end)

        it("should record award with all fields", function()
            local item = {
                id = 12345,
                link = "|cff0070dd|Hitem:12345|h[Test Sword]|h|r",
                name = "Test Sword",
                itemLevel = 500,
                quality = 4,
            }

            LootHistory:RecordAward(item, "Winner", "BIS", "EdgeRush")

            assert.equals(1, #LootHistory.history)
            assert.equals("Winner", LootHistory.history[1].winner)
            assert.equals("BIS", LootHistory.history[1].reason)
            assert.equals("EdgeRush", LootHistory.history[1].source)
            assert.equals(12345, LootHistory.history[1].itemId)
        end)

        it("should insert new awards at beginning", function()
            local item1 = { id = 1, name = "Item 1" }
            local item2 = { id = 2, name = "Item 2" }

            LootHistory:RecordAward(item1, "Player1", "Reason1")
            LootHistory:RecordAward(item2, "Player2", "Reason2")

            -- Most recent should be first
            assert.equals("Player2", LootHistory.history[1].winner)
            assert.equals("Player1", LootHistory.history[2].winner)
        end)

        it("should use default source when not provided", function()
            local item = { id = 1, name = "Test" }

            LootHistory:RecordAward(item, "Player", "Reason")

            assert.equals("EdgeRush", LootHistory.history[1].source)
        end)

        it("should include timestamp", function()
            local item = { id = 1, name = "Test" }

            LootHistory:RecordAward(item, "Player", "Reason")

            assert.is_number(LootHistory.history[1].timestamp)
        end)
    end)

    describe("SaveSession", function()
        before_each(function()
            LootHistory:Initialize()
        end)

        it("should save session data", function()
            local session = {
                id = "test-session-123",
                startTime = 1000,
                endTime = 2000,
                items = {
                    { link = "item1", name = "Test Item 1", awarded = true, awardedTo = "Player1" },
                },
                awards = {
                    { winner = "Player1", item = "item1" },
                },
            }

            LootHistory:SaveSession(session)

            assert.equals(1, #LootHistory.sessions)
            assert.equals("test-session-123", LootHistory.sessions[1].id)
            assert.equals(1, #LootHistory.sessions[1].awards)
        end)

        it("should insert new sessions at beginning", function()
            local session1 = { id = "session1", startTime = 1000, items = {}, awards = {} }
            local session2 = { id = "session2", startTime = 2000, items = {}, awards = {} }

            LootHistory:SaveSession(session1)
            LootHistory:SaveSession(session2)

            -- Most recent should be first
            assert.equals("session2", LootHistory.sessions[1].id)
        end)
    end)

    describe("GetLastBossName", function()
        before_each(function()
            LootHistory:Initialize()
        end)

        it("should return Unknown when no boss set", function()
            assert.equals("Unknown", LootHistory:GetLastBossName())
        end)

        it("should return set boss name", function()
            LootHistory:SetLastBossName("Sylvanas Windrunner")
            assert.equals("Sylvanas Windrunner", LootHistory:GetLastBossName())
        end)
    end)

    describe("GetHistory", function()
        before_each(function()
            LootHistory:Initialize()
            -- Add test data
            LootHistory.history = {
                { winner = "Player1", itemQuality = 4, timestamp = 1000, instance = "Instance1" },
                { winner = "Player2", itemQuality = 3, timestamp = 2000, instance = "Instance1" },
                { winner = "Player1", itemQuality = 5, timestamp = 3000, instance = "Instance2" },
            }
        end)

        it("should return all history with no filters", function()
            local results = LootHistory:GetHistory()
            assert.equals(3, #results)
        end)

        it("should filter by winner", function()
            local results = LootHistory:GetHistory({ winner = "Player1" })
            assert.equals(2, #results)
        end)

        it("should filter by minimum quality", function()
            local results = LootHistory:GetHistory({ minQuality = 4 })
            assert.equals(2, #results)
        end)

        it("should filter by instance", function()
            local results = LootHistory:GetHistory({ instance = "Instance1" })
            assert.equals(2, #results)
        end)

        it("should filter by date range", function()
            local results = LootHistory:GetHistory({ startDate = 1500, endDate = 2500 })
            assert.equals(1, #results)
            assert.equals("Player2", results[1].winner)
        end)

        it("should combine multiple filters", function()
            local results = LootHistory:GetHistory({
                winner = "Player1",
                minQuality = 4,
            })
            assert.equals(2, #results)
        end)
    end)

    describe("GetPlayerLoot", function()
        before_each(function()
            LootHistory:Initialize()
            LootHistory.history = {
                { winner = "Player1", itemName = "Item1" },
                { winner = "Player2", itemName = "Item2" },
                { winner = "Player1", itemName = "Item3" },
                { winner = "Player1", itemName = "Item4" },
            }
        end)

        it("should return loot for specific player", function()
            local results = LootHistory:GetPlayerLoot("Player1")
            assert.equals(3, #results)
        end)

        it("should return empty for player with no loot", function()
            local results = LootHistory:GetPlayerLoot("UnknownPlayer")
            assert.equals(0, #results)
        end)

        it("should respect limit parameter", function()
            local results = LootHistory:GetPlayerLoot("Player1", 2)
            assert.equals(2, #results)
        end)
    end)

    describe("GetRecent", function()
        before_each(function()
            LootHistory:Initialize()
            LootHistory.history = {}
            for i = 1, 30 do
                table.insert(LootHistory.history, { itemName = "Item" .. i })
            end
        end)

        it("should return default 20 items", function()
            local results = LootHistory:GetRecent()
            assert.equals(20, #results)
        end)

        it("should respect custom limit", function()
            local results = LootHistory:GetRecent(10)
            assert.equals(10, #results)
        end)

        it("should return all if fewer than limit", function()
            LootHistory.history = {
                { itemName = "Item1" },
                { itemName = "Item2" },
            }
            local results = LootHistory:GetRecent(10)
            assert.equals(2, #results)
        end)
    end)

    describe("Toggle", function()
        before_each(function()
            LootHistory:Initialize()
            LootHistory.frame = nil
        end)

        it("should show when hidden", function()
            local shown = false
            _G.CreateFrame = function()
                return {
                    SetSize = function() end,
                    SetPoint = function() end,
                    SetMovable = function() end,
                    EnableMouse = function() end,
                    RegisterForDrag = function() end,
                    SetScript = function() end,
                    SetBackdrop = function() end,
                    SetBackdropColor = function() end,
                    SetFrameStrata = function() end,
                    CreateFontString = function() return { SetPoint = function() end, SetText = function() end, SetTextColor = function() end, SetWidth = function() end, SetJustifyH = function() end } end,
                    CreateTexture = function() return { SetAllPoints = function() end, SetColorTexture = function() end, SetSize = function() end, SetPoint = function() end, SetTexture = function() end, Hide = function() end, Show = function() end } end,
                    Hide = function() shown = false end,
                    Show = function() shown = true end,
                    IsShown = function() return shown end,
                    SetWidth = function() end,
                    SetHeight = function() end,
                    SetEnabled = function() end,
                }
            end

            LootHistory:Toggle()
            assert.is_true(shown)
        end)
    end)

    describe("Pagination", function()
        before_each(function()
            LootHistory:Initialize()
            LootHistory.currentPage = 1
            LootHistory.currentFilters = {}

            -- Add 50 items
            LootHistory.history = {}
            for i = 1, 50 do
                table.insert(LootHistory.history, { itemName = "Item" .. i })
            end
        end)

        describe("NextPage", function()
            it("should increment page number", function()
                -- Mock UpdateDisplay
                LootHistory.UpdateDisplay = function() end

                LootHistory:NextPage()
                assert.equals(2, LootHistory.currentPage)
            end)

            it("should not exceed total pages", function()
                LootHistory.UpdateDisplay = function() end
                LootHistory.currentPage = 3 -- Last page for 50 items with PAGE_SIZE=20

                LootHistory:NextPage()
                assert.equals(3, LootHistory.currentPage)
            end)
        end)

        describe("PrevPage", function()
            it("should decrement page number", function()
                LootHistory.UpdateDisplay = function() end
                LootHistory.currentPage = 2

                LootHistory:PrevPage()
                assert.equals(1, LootHistory.currentPage)
            end)

            it("should not go below page 1", function()
                LootHistory.UpdateDisplay = function() end
                LootHistory.currentPage = 1

                LootHistory:PrevPage()
                assert.equals(1, LootHistory.currentPage)
            end)
        end)
    end)
end)
