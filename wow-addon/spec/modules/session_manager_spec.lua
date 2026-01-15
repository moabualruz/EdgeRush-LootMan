-- Session Manager Module Tests
-- Tests for Modules/LootCouncil/SessionManager.lua

local TestEnv = require("spec.mocks.init")

describe("SessionManager", function()
    local ELM
    local MockWowApi, MockAceLibs
    local SessionManager

    before_each(function()
        MockWowApi, MockAceLibs = TestEnv:Setup()
        -- Load dependencies
        ELM = TestEnv:LoadModule("Core/Constants.lua")
        TestEnv:LoadModule("Core/Utils.lua")

        -- Mock Comms module
        ELM.Comms = {
            BroadcastMessage = function(self, msgType, data) end,
            SendToML = function(self, msgType, data) end,
            BroadcastToCouncil = function(self, msgType, data) end,
        }

        -- Load SessionManager
        TestEnv:LoadModule("Modules/LootCouncil/SessionManager.lua")
        SessionManager = ELM.SessionManager

        TestEnv:ClearPrintOutput()
    end)

    describe("STATE constants", function()
        it("should define all session states", function()
            assert.is_table(SessionManager.STATE)
            assert.equals("IDLE", SessionManager.STATE.IDLE)
            assert.equals("ACTIVE", SessionManager.STATE.ACTIVE)
            assert.equals("VOTING", SessionManager.STATE.VOTING)
            assert.equals("AWARDING", SessionManager.STATE.AWARDING)
            assert.equals("COMPLETED", SessionManager.STATE.COMPLETED)
        end)
    end)

    describe("RESPONSES constants", function()
        it("should define response types", function()
            assert.is_table(SessionManager.RESPONSES)
            assert.is_true(#SessionManager.RESPONSES > 0)
        end)

        it("should include BIS response", function()
            local found = false
            for _, response in ipairs(SessionManager.RESPONSES) do
                if response.id == "BIS" then
                    found = true
                    assert.equals("Best in Slot", response.text)
                    break
                end
            end
            assert.is_true(found)
        end)

        it("should include PASS response", function()
            local found = false
            for _, response in ipairs(SessionManager.RESPONSES) do
                if response.id == "PASS" then
                    found = true
                    break
                end
            end
            assert.is_true(found)
        end)

        it("should have colors for all responses", function()
            for _, response in ipairs(SessionManager.RESPONSES) do
                assert.is_table(response.color)
                assert.is_number(response.color.r)
                assert.is_number(response.color.g)
                assert.is_number(response.color.b)
            end
        end)

        it("should have ordered responses", function()
            for _, response in ipairs(SessionManager.RESPONSES) do
                assert.is_number(response.order)
            end
        end)
    end)

    describe("Initialize", function()
        it("should initialize session manager state", function()
            SessionManager:Initialize()

            assert.is_table(SessionManager.sessions)
            assert.is_nil(SessionManager.currentSession)
            assert.equals(SessionManager.STATE.IDLE, SessionManager.state)
            assert.is_table(SessionManager.council)
        end)
    end)

    describe("IsMasterLooter", function()
        it("should return false when not in raid", function()
            MockWowApi:SetPlayer({ isInRaid = false })
            assert.is_false(SessionManager:IsMasterLooter())
        end)

        it("should return true when player is raid leader", function()
            MockWowApi:SetPlayer({
                isInRaid = true,
                isGroupLeader = true,
            })
            assert.is_true(SessionManager:IsMasterLooter())
        end)

        it("should return true when player is assistant", function()
            MockWowApi:SetPlayer({
                isInRaid = true,
                isGroupAssistant = true,
            })
            assert.is_true(SessionManager:IsMasterLooter())
        end)
    end)

    describe("IsOnCouncil", function()
        before_each(function()
            SessionManager:Initialize()
        end)

        it("should return true for master looter", function()
            MockWowApi:SetPlayer({ isInRaid = true, isGroupLeader = true })
            assert.is_true(SessionManager:IsOnCouncil())
        end)

        it("should return true for raid assistant", function()
            MockWowApi:SetPlayer({ isInRaid = true, isGroupAssistant = true })
            assert.is_true(SessionManager:IsOnCouncil())
        end)

        it("should return true for council members", function()
            MockWowApi:SetPlayer({
                isInRaid = true,
                isGroupLeader = false,
                isGroupAssistant = false,
                name = "CouncilMember",
                realm = "TestRealm",
            })
            SessionManager.council = { "CouncilMember-TestRealm" }
            assert.is_true(SessionManager:IsOnCouncil())
        end)
    end)

    describe("IsCouncilMember", function()
        before_each(function()
            SessionManager:Initialize()
            MockWowApi:SetPlayer({ isInRaid = true })
            MockWowApi:SetRaidRoster({
                { name = "Player1", rank = 2 }, -- Leader
                { name = "Player2", rank = 1 }, -- Assistant
                { name = "Player3", rank = 0 }, -- Member
            })
        end)

        it("should return false for nil player", function()
            assert.is_false(SessionManager:IsCouncilMember(nil))
        end)

        it("should return true for players in council list", function()
            SessionManager.council = { "SpecialMember" }
            assert.is_true(SessionManager:IsCouncilMember("SpecialMember"))
        end)

        it("should return true for raid assistants", function()
            assert.is_true(SessionManager:IsCouncilMember("Player2"))
        end)

        it("should return false for regular raid members", function()
            assert.is_false(SessionManager:IsCouncilMember("Player3"))
        end)
    end)

    describe("StartSession", function()
        before_each(function()
            SessionManager:Initialize()
            MockWowApi:SetPlayer({
                isInRaid = true,
                isGroupLeader = true,
                name = "RaidLeader",
                realm = "TestRealm",
            })
            MockWowApi:SetRaidRoster({
                { name = "RaidLeader", rank = 2, online = true, class = "WARRIOR", fileName = "WARRIOR", role = "TANK", combatRole = "TANK" },
            })
        end)

        it("should fail when session already in progress", function()
            SessionManager.state = SessionManager.STATE.ACTIVE
            local result = SessionManager:StartSession({})
            assert.is_false(result)
        end)

        it("should fail when not master looter", function()
            MockWowApi:SetPlayer({ isInRaid = true, isGroupLeader = false, isGroupAssistant = false })
            local result = SessionManager:StartSession({})
            assert.is_false(result)
        end)

        it("should create session with items", function()
            -- Mock item info for testing
            _G.C_Item.GetItemInfo = function(link)
                return "Test Sword", nil, 4, 500, nil, "Weapon", "Sword", nil, "INVTYPE_WEAPON", 12345
            end

            local items = { "|cff0070dd|Hitem:12345|h[Test Sword]|h|r" }
            local result = SessionManager:StartSession(items)

            assert.is_true(result)
            assert.is_not_nil(SessionManager.currentSession)
            assert.is_not_nil(SessionManager.currentSession.id)
            assert.equals(SessionManager.STATE.ACTIVE, SessionManager.state)
        end)

        it("should build candidate list from raid roster", function()
            MockWowApi:SetRaidRoster({
                { name = "Player1", rank = 2, online = true, class = "WARRIOR", fileName = "WARRIOR", combatRole = "TANK" },
                { name = "Player2", rank = 0, online = true, class = "PRIEST", fileName = "PRIEST", combatRole = "HEALER" },
                { name = "Player3", rank = 0, online = false, class = "MAGE", fileName = "MAGE", combatRole = "DAMAGER" },
            })

            local items = { "|cff0070dd|Hitem:12345|h[Test Sword]|h|r" }
            SessionManager:StartSession(items)

            -- Should only include online players
            assert.equals(2, #SessionManager.currentSession.candidates)
        end)
    end)

    describe("HandleResponse", function()
        before_each(function()
            SessionManager:Initialize()
            SessionManager.currentSession = {
                id = "test-session-123",
                items = {},
                responses = {},
            }
        end)

        it("should ignore response for wrong session", function()
            SessionManager:HandleResponse("Player1", {
                sessionId = "wrong-session",
                itemIndex = 1,
                response = "BIS",
            })

            assert.is_nil(SessionManager.currentSession.responses[1])
        end)

        it("should store valid response", function()
            SessionManager:HandleResponse("Player1", {
                sessionId = "test-session-123",
                itemIndex = 1,
                response = "BIS",
                note = "Need for main spec",
            })

            assert.is_not_nil(SessionManager.currentSession.responses[1])
            assert.is_not_nil(SessionManager.currentSession.responses[1]["Player1"])
            assert.equals("BIS", SessionManager.currentSession.responses[1]["Player1"].response)
            assert.equals("Need for main spec", SessionManager.currentSession.responses[1]["Player1"].note)
        end)
    end)

    describe("CastVote", function()
        before_each(function()
            SessionManager:Initialize()
            SessionManager.currentSession = {
                id = "test-session",
                votes = {},
            }
            MockWowApi:SetPlayer({
                name = "Voter",
                realm = "TestRealm",
                isInRaid = true,
                isGroupAssistant = true,
            })
        end)

        it("should not cast vote when not on council", function()
            MockWowApi:SetPlayer({ isInRaid = true, isGroupAssistant = false, isGroupLeader = false })
            SessionManager.council = {}

            SessionManager:CastVote(1, "Candidate1", 1)

            assert.is_nil(SessionManager.currentSession.votes[1])
        end)

        it("should store vote from council member", function()
            SessionManager:CastVote(1, "Candidate1", 1)

            assert.is_not_nil(SessionManager.currentSession.votes[1])
            assert.is_not_nil(SessionManager.currentSession.votes[1]["Candidate1"])
            assert.is_not_nil(SessionManager.currentSession.votes[1]["Candidate1"]["Voter-TestRealm"])
        end)
    end)

    describe("AwardItem", function()
        before_each(function()
            SessionManager:Initialize()
            SessionManager.currentSession = {
                id = "test-session",
                items = {
                    [1] = {
                        link = "|cff0070dd|Hitem:12345|h[Test Sword]|h|r",
                        awarded = false,
                    },
                },
                awards = {},
            }
            MockWowApi:SetPlayer({ isInRaid = true, isGroupLeader = true })
        end)

        it("should not award when not master looter", function()
            MockWowApi:SetPlayer({ isInRaid = true, isGroupLeader = false, isGroupAssistant = false })

            SessionManager:AwardItem(1, "Winner", "BIS")

            assert.is_false(SessionManager.currentSession.items[1].awarded)
        end)

        it("should mark item as awarded", function()
            SessionManager:AwardItem(1, "Winner", "BIS")

            assert.is_true(SessionManager.currentSession.items[1].awarded)
            assert.equals("Winner", SessionManager.currentSession.items[1].awardedTo)
            assert.equals("BIS", SessionManager.currentSession.items[1].awardReason)
        end)

        it("should record award in session", function()
            SessionManager:AwardItem(1, "Winner", "BIS")

            assert.equals(1, #SessionManager.currentSession.awards)
            assert.equals("Winner", SessionManager.currentSession.awards[1].winner)
        end)

        it("should not award already awarded item", function()
            SessionManager.currentSession.items[1].awarded = true

            SessionManager:AwardItem(1, "NewWinner", "BIS")

            -- Should still be nil awardedTo since we returned early
            assert.is_nil(SessionManager.currentSession.items[1].awardedTo)
        end)
    end)

    describe("EndSession", function()
        before_each(function()
            SessionManager:Initialize()
            SessionManager.currentSession = {
                id = "test-session",
                items = {},
                awards = {},
            }
            SessionManager.state = SessionManager.STATE.ACTIVE
        end)

        it("should end session and reset state", function()
            SessionManager:EndSession()

            assert.is_nil(SessionManager.currentSession)
            assert.equals(SessionManager.STATE.IDLE, SessionManager.state)
        end)

        it("should store session in history", function()
            SessionManager:EndSession()

            assert.equals(1, #SessionManager.sessions)
        end)
    end)

    describe("CancelSession", function()
        before_each(function()
            SessionManager:Initialize()
            SessionManager.currentSession = { id = "test-session" }
            SessionManager.state = SessionManager.STATE.ACTIVE
            MockWowApi:SetPlayer({ isInRaid = true, isGroupLeader = true })
        end)

        it("should not cancel when not master looter", function()
            MockWowApi:SetPlayer({ isInRaid = true, isGroupLeader = false, isGroupAssistant = false })

            SessionManager:CancelSession()

            -- Session should still exist
            assert.is_not_nil(SessionManager.currentSession)
        end)

        it("should cancel session and reset state", function()
            SessionManager:CancelSession()

            assert.is_nil(SessionManager.currentSession)
            assert.equals(SessionManager.STATE.IDLE, SessionManager.state)
        end)

        it("should not store cancelled session in history", function()
            SessionManager:CancelSession()

            assert.equals(0, #SessionManager.sessions)
        end)
    end)

    describe("GetResponseColor", function()
        it("should return color for known response", function()
            local color = SessionManager:GetResponseColor("BIS")
            assert.is_table(color)
            assert.is_number(color.r)
        end)

        it("should return white for unknown response", function()
            local color = SessionManager:GetResponseColor("UNKNOWN")
            assert.equals(1, color.r)
            assert.equals(1, color.g)
            assert.equals(1, color.b)
        end)
    end)

    describe("GetResponseText", function()
        it("should return text for known response", function()
            local text = SessionManager:GetResponseText("BIS")
            assert.equals("Best in Slot", text)
        end)

        it("should return id for unknown response", function()
            local text = SessionManager:GetResponseText("CUSTOM")
            assert.equals("CUSTOM", text)
        end)
    end)

    describe("ShouldAutoPass", function()
        it("should not auto-pass for nil link", function()
            local result = SessionManager:ShouldAutoPass(nil, "WARRIOR")
            assert.is_false(result)
        end)
    end)

    describe("EquipLocToSlot", function()
        it("should map head slot correctly", function()
            local slot = SessionManager:EquipLocToSlot("INVTYPE_HEAD")
            assert.equals(1, slot)
        end)

        it("should map weapon slot correctly", function()
            local slot = SessionManager:EquipLocToSlot("INVTYPE_WEAPON")
            assert.equals(16, slot)
        end)

        it("should map off-hand slot correctly", function()
            local slot = SessionManager:EquipLocToSlot("INVTYPE_WEAPONOFFHAND")
            assert.equals(17, slot)
        end)

        it("should return nil for unknown slot", function()
            local slot = SessionManager:EquipLocToSlot("UNKNOWN")
            assert.is_nil(slot)
        end)
    end)
end)
