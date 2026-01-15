-- EdgeRush LootMan Core
-- Main addon initialization and event handling

local ADDON_NAME, ELM = ...

-- Create the main addon object using Ace3
ELM.Addon = LibStub("AceAddon-3.0"):NewAddon(ADDON_NAME, "AceConsole-3.0", "AceEvent-3.0", "AceComm-3.0", "AceSerializer-3.0")

local Addon = ELM.Addon

-- Addon initialization
function Addon:OnInitialize()
    -- Initialize database
    self.db = LibStub("AceDB-3.0"):New("EdgeRushLootManDB", ELM.DB_DEFAULTS, true)

    -- Set up profile callbacks
    self.db.RegisterCallback(self, "OnProfileChanged", "RefreshConfig")
    self.db.RegisterCallback(self, "OnProfileCopied", "RefreshConfig")
    self.db.RegisterCallback(self, "OnProfileReset", "RefreshConfig")

    -- Register slash commands
    for _, cmd in ipairs(ELM.SLASH_COMMANDS) do
        self:RegisterChatCommand(cmd:sub(2), "SlashCommand")
    end

    -- Register comm prefix
    self:RegisterComm(ELM.COMM_PREFIX)

    -- Initialize modules
    self:InitializeModules()

    ELM.Utils:Print("Loaded v" .. ELM.VERSION)
end

-- Addon enable
function Addon:OnEnable()
    -- Register events
    for _, event in ipairs(ELM.EVENTS) do
        self:RegisterEvent(event)
    end

    -- Initial gear export
    if self.db.profile.sync.autoExport then
        C_Timer.After(2, function()
            self:ExportGear()
        end)
    end
end

-- Addon disable
function Addon:OnDisable()
    -- Unregister events
    for _, event in ipairs(ELM.EVENTS) do
        self:UnregisterEvent(event)
    end
end

-- Initialize modules
function Addon:InitializeModules()
    -- Initialize Session Manager
    if ELM.SessionManager then
        ELM.SessionManager:Initialize()
    end

    -- Initialize Loot History
    if ELM.LootHistory then
        ELM.LootHistory:Initialize()
    end

    -- Initialize RCLC Compatibility
    if ELM.RCLCCompat then
        ELM.RCLCCompat:Initialize()
    end

    -- Initialize Minimap Button
    if ELM.MinimapButton then
        ELM.MinimapButton:Initialize()
    end

    -- Initialize Options
    if ELM.Options then
        ELM.Options:Initialize()
    end

    -- Hook tooltip for FLPS display
    if ELM.Display then
        ELM.Display:HookTooltip()
    end
end

-- Refresh config (called on profile change)
function Addon:RefreshConfig()
    -- Refresh minimap button
    if ELM.MinimapButton then
        ELM.MinimapButton:Refresh()
    end

    -- Refresh display
    if ELM.Display then
        ELM.Display:Refresh()
    end
end

-- Slash command handler
function Addon:SlashCommand(input)
    local args = {}
    for word in input:gmatch("%S+") do
        table.insert(args, word:lower())
    end

    local cmd = args[1] or "help"

    if cmd == "help" or cmd == "?" then
        self:ShowHelp()
    elseif cmd == "config" or cmd == "options" then
        self:OpenOptions()
    elseif cmd == "sync" or cmd == "export" then
        self:ExportGear()
        ELM.Utils:Print("Gear exported to SavedVariables")
    elseif cmd == "flps" then
        self:ShowFLPS()
    elseif cmd == "leaderboard" or cmd == "lb" then
        self:ShowLeaderboard()
    elseif cmd == "loot" then
        self:ShowLootCouncil()
    elseif cmd == "ml" or cmd == "masterloot" then
        self:ShowMasterLoot()
    elseif cmd == "history" then
        self:ShowHistory()
    elseif cmd == "wishlist" or cmd == "wl" then
        self:ShowWishlist()
    elseif cmd == "minimap" then
        self:ToggleMinimap()
    elseif cmd == "debug" then
        ELM.DEBUG = not ELM.DEBUG
        ELM.Utils:Print("Debug mode: " .. (ELM.DEBUG and "ON" or "OFF"))
    else
        ELM.Utils:Print("Unknown command: " .. cmd)
        self:ShowHelp()
    end
end

-- Show help
function Addon:ShowHelp()
    ELM.Utils:Print("Commands:")
    print("  /elm help - Show this help")
    print("  /elm config - Open configuration")
    print("  /elm sync - Export gear data")
    print("  /elm flps - Show your FLPS score")
    print("  /elm leaderboard - Show guild FLPS leaderboard")
    print("  /elm loot - Open loot council frame")
    print("  /elm ml - Open master loot frame (ML only)")
    print("  /elm history - Show loot history")
    print("  /elm wishlist - Show your wishlist")
    print("  /elm minimap - Toggle minimap button")
end

-- Open options panel
function Addon:OpenOptions()
    if ELM.Options then
        ELM.Options:Open()
    else
        Settings.OpenToCategory(ADDON_NAME)
    end
end

-- Export gear to SavedVariables
function Addon:ExportGear()
    if ELM.GearExport then
        ELM.GearExport:Export()
    end
end

-- Show FLPS score
function Addon:ShowFLPS()
    local flps = self.db.char.flps
    ELM.Utils:Print("Your FLPS Score:")
    print("  Total: " .. ELM.Utils:ColorByFLPS(ELM.Utils:FormatFLPS(flps.score), flps.score))
    print("  RMS (Raider Merit): " .. string.format("%.1f%%", flps.rms * 100))
    print("  IPI (Item Priority): " .. string.format("%.1f%%", flps.ipi * 100))
    print("  RDF (Recency Decay): " .. string.format("%.1f%%", flps.rdf * 100))
    print("  Rank: #" .. flps.rank)
    if flps.lastUpdated then
        print("  Last Updated: " .. ELM.Utils:FormatTimestamp(flps.lastUpdated))
    end
end

-- Show leaderboard
function Addon:ShowLeaderboard()
    if ELM.Leaderboard then
        ELM.Leaderboard:Show()
    else
        ELM.Utils:Print("Leaderboard not available")
    end
end

-- Show loot council
function Addon:ShowLootCouncil()
    if ELM.LootFrame then
        ELM.LootFrame:Show()
    else
        ELM.Utils:Print("Loot council frame not available")
    end
end

-- Show master loot frame
function Addon:ShowMasterLoot()
    if not ELM.SessionManager then
        ELM.Utils:Print("Session manager not available")
        return
    end

    if not ELM.SessionManager:IsMasterLooter() then
        ELM.Utils:Print("You must be the master looter or raid leader to use this")
        return
    end

    if ELM.MasterLootFrame then
        ELM.MasterLootFrame:Show()
    else
        ELM.Utils:Print("Master loot frame not available")
    end
end

-- Show loot history
function Addon:ShowHistory()
    if ELM.LootHistory then
        ELM.LootHistory:Toggle()
    else
        ELM.Utils:Print("Loot history not available")
    end
end

-- Show wishlist
function Addon:ShowWishlist()
    if ELM.Wishlist then
        ELM.Wishlist:Toggle()
    else
        ELM.Utils:Print("Wishlist not available")
    end
end

-- Toggle minimap button
function Addon:ToggleMinimap()
    self.db.profile.minimap.hide = not self.db.profile.minimap.hide
    if ELM.MinimapButton then
        ELM.MinimapButton:Refresh()
    end
    ELM.Utils:Print("Minimap button: " .. (self.db.profile.minimap.hide and "Hidden" or "Shown"))
end

-- Event handlers

function Addon:PLAYER_LOGIN()
    -- Update character info
    self.db.char.characterID = ELM.Utils:GetCharacterID()
    self.db.char.class = ELM.Utils:GetCharacterClass()
    self.db.char.level = ELM.Utils:GetCharacterLevel()
end

function Addon:PLAYER_LOGOUT()
    -- Export gear on logout if enabled
    if self.db.profile.sync.exportOnLogout then
        self:ExportGear()
    end
end

function Addon:PLAYER_EQUIPMENT_CHANGED(event, slotID)
    -- Re-export gear when equipment changes
    if self.db.profile.sync.autoExport then
        -- Debounce rapid changes
        if not self.exportTimer then
            self.exportTimer = C_Timer.NewTimer(2, function()
                self:ExportGear()
                self.exportTimer = nil
            end)
        end
    end
end

function Addon:PLAYER_ENTERING_WORLD()
    -- Ensure we have latest gear data
    C_Timer.After(1, function()
        if self.db.profile.sync.autoExport then
            self:ExportGear()
        end
    end)
end

function Addon:ENCOUNTER_LOOT_RECEIVED(event, encounterID, itemID, itemLink, quantity, playerName, className)
    -- Log loot received
    ELM.Utils:Debug("Loot received: " .. tostring(itemLink) .. " by " .. tostring(playerName))

    -- Could trigger loot council or logging here
end

function Addon:ENCOUNTER_END(event, encounterID, encounterName, difficultyID, groupSize, success)
    if success == 1 then
        ELM.Utils:Debug("Encounter defeated: " .. encounterName)
        -- Could trigger loot council auto-open here
    end
end

function Addon:GROUP_ROSTER_UPDATE()
    -- Refresh raid roster information
    if IsInRaid() then
        self.raidRoster = ELM.Utils:GetRaidRoster()
    end
end

-- Communication handlers
function Addon:OnCommReceived(prefix, message, distribution, sender)
    if prefix ~= ELM.COMM_PREFIX then return end

    local success, msgType, data = self:Deserialize(message)
    if not success then
        ELM.Utils:Debug("Failed to deserialize message from " .. sender)
        return
    end

    -- FLPS messages
    if msgType == "FLPS_REQUEST" then
        self:HandleFLPSRequest(sender)
    elseif msgType == "FLPS_RESPONSE" then
        self:HandleFLPSResponse(sender, data)

    -- Legacy loot messages
    elseif msgType == "LOOT_VOTE" then
        self:HandleLootVote(sender, data)
    elseif msgType == "LOOT_AWARD" then
        self:HandleLootAward(sender, data)

    -- Session Manager messages
    elseif msgType == "SESSION_START" then
        self:HandleSessionStart(sender, data)
    elseif msgType == "SESSION_END" then
        self:HandleSessionEnd(sender, data)
    elseif msgType == "SESSION_CANCEL" then
        self:HandleSessionCancel(sender, data)
    elseif msgType == "RESPONSE_REQUEST" then
        self:HandleResponseRequest(sender, data)
    elseif msgType == "RESPONSE" then
        self:HandleResponse(sender, data)
    elseif msgType == "VOTE" then
        self:HandleVote(sender, data)
    elseif msgType == "AWARD" then
        self:HandleAward(sender, data)
    end
end

function Addon:HandleFLPSRequest(sender)
    -- Send our FLPS data to requester
    local data = self.db.char.flps
    local message = self:Serialize("FLPS_RESPONSE", data)
    self:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", sender)
end

function Addon:HandleFLPSResponse(sender, data)
    -- Store received FLPS data
    if ELM.Leaderboard then
        ELM.Leaderboard:UpdatePlayer(sender, data)
    end
end

function Addon:HandleLootVote(sender, data)
    if ELM.VotingFrame then
        ELM.VotingFrame:ProcessVote(sender, data)
    end
end

function Addon:HandleLootAward(sender, data)
    if ELM.LootFrame then
        ELM.LootFrame:ProcessAward(sender, data)
    end
end

-- Request FLPS from raid members
function Addon:RequestRaidFLPS()
    if not IsInRaid() then return end

    local message = self:Serialize("FLPS_REQUEST", {})
    self:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

-- Session Manager Message Handlers

function Addon:HandleSessionStart(sender, data)
    -- Received session start from master looter
    if not ELM.SessionManager then return end

    ELM.Utils:Debug("Session started by " .. sender)

    -- Store session info
    ELM.SessionManager.currentSession = {
        id = data.sessionId,
        masterLooter = sender,
        items = data.items or {},
        startTime = time(),
        candidates = {},
        responses = {},
        votes = {},
    }

    -- Show notification
    ELM.Utils:Print("Loot council session started by " .. sender)

    -- Play sound
    PlaySound(SOUNDKIT.READY_CHECK)
end

function Addon:HandleSessionEnd(sender, data)
    -- Received session end from master looter
    if not ELM.SessionManager then return end

    -- Verify sender is the ML
    if ELM.SessionManager.currentSession and
       ELM.SessionManager.currentSession.masterLooter ~= sender then
        return
    end

    ELM.Utils:Debug("Session ended by " .. sender)

    -- Save session to history
    if ELM.LootHistory and ELM.SessionManager.currentSession then
        ELM.LootHistory:SaveSession(ELM.SessionManager.currentSession)
    end

    -- Clear session
    ELM.SessionManager.currentSession = nil

    -- Hide any open frames
    if ELM.ResponseFrame then
        ELM.ResponseFrame:Hide()
    end

    ELM.Utils:Print("Loot council session ended")
end

function Addon:HandleSessionCancel(sender, data)
    -- Received session cancel from master looter
    if not ELM.SessionManager then return end

    -- Verify sender is the ML
    if ELM.SessionManager.currentSession and
       ELM.SessionManager.currentSession.masterLooter ~= sender then
        return
    end

    ELM.Utils:Debug("Session cancelled by " .. sender)

    -- Clear session without saving
    ELM.SessionManager.currentSession = nil

    -- Hide any open frames
    if ELM.ResponseFrame then
        ELM.ResponseFrame:Hide()
    end

    ELM.Utils:Print("Loot council session cancelled")
end

function Addon:HandleResponseRequest(sender, data)
    -- Master looter is requesting our response for an item
    if not ELM.SessionManager then return end
    if not ELM.ResponseFrame then return end

    -- Verify we have an active session from this ML
    if not ELM.SessionManager.currentSession or
       ELM.SessionManager.currentSession.masterLooter ~= sender then
        ELM.Utils:Debug("Ignoring response request - no active session from " .. sender)
        return
    end

    local itemIndex = data.itemIndex
    local itemLink = data.itemLink
    local timeout = data.timeout or 60

    ELM.Utils:Debug("Response requested for item " .. (itemLink or "unknown"))

    -- Show response frame for this item
    ELM.ResponseFrame:ShowForItem(
        ELM.SessionManager.currentSession.id,
        itemIndex,
        itemLink,
        timeout
    )
end

function Addon:HandleResponse(sender, data)
    -- Received a response from a raider (ML only)
    if not ELM.SessionManager then return end

    -- Only process if we're the ML
    if not ELM.SessionManager:IsMasterLooter() then return end

    local session = ELM.SessionManager.currentSession
    if not session then return end

    local itemIndex = data.itemIndex
    local responseId = data.responseId
    local note = data.note

    ELM.Utils:Debug("Response from " .. sender .. ": " .. (responseId or "nil"))

    -- Store response
    if not session.responses[itemIndex] then
        session.responses[itemIndex] = {}
    end

    session.responses[itemIndex][sender] = {
        responseId = responseId,
        note = note,
        timestamp = time(),
    }

    -- Update UI if visible
    if ELM.MasterLootFrame and ELM.MasterLootFrame:IsShown() then
        ELM.MasterLootFrame:UpdateCandidates()
    end
end

function Addon:HandleVote(sender, data)
    -- Received a vote from a council member (ML only)
    if not ELM.SessionManager then return end

    -- Only process if we're the ML
    if not ELM.SessionManager:IsMasterLooter() then return end

    local session = ELM.SessionManager.currentSession
    if not session then return end

    -- Verify sender is a council member
    if not ELM.SessionManager:IsCouncilMember(sender) then
        ELM.Utils:Debug("Ignoring vote from non-council member: " .. sender)
        return
    end

    local itemIndex = data.itemIndex
    local candidate = data.candidate
    local voteValue = data.vote

    ELM.Utils:Debug("Vote from " .. sender .. " for " .. candidate .. ": " .. tostring(voteValue))

    -- Store vote
    if not session.votes[itemIndex] then
        session.votes[itemIndex] = {}
    end
    if not session.votes[itemIndex][candidate] then
        session.votes[itemIndex][candidate] = {}
    end

    session.votes[itemIndex][candidate][sender] = {
        vote = voteValue,
        timestamp = time(),
    }

    -- Update UI if visible
    if ELM.MasterLootFrame and ELM.MasterLootFrame:IsShown() then
        ELM.MasterLootFrame:UpdateCandidates()
    end
end

function Addon:HandleAward(sender, data)
    -- Received award announcement from ML
    if not ELM.SessionManager then return end

    -- Verify sender is the ML
    if ELM.SessionManager.currentSession and
       ELM.SessionManager.currentSession.masterLooter ~= sender then
        return
    end

    local itemLink = data.itemLink
    local winner = data.winner
    local reason = data.reason

    ELM.Utils:Debug("Award: " .. (itemLink or "unknown") .. " to " .. (winner or "unknown"))

    -- Record in history
    if ELM.LootHistory then
        ELM.LootHistory:RecordAward({
            link = itemLink,
            name = data.itemName,
            id = data.itemId,
            itemLevel = data.itemLevel,
            quality = data.quality,
        }, winner, reason, "EdgeRush")
    end

    -- Show notification
    local winnerClass = data.winnerClass
    local coloredWinner = winner
    if winnerClass then
        local classColor = RAID_CLASS_COLORS[winnerClass]
        if classColor then
            coloredWinner = string.format("|c%s%s|r", classColor.colorStr, winner)
        end
    end

    ELM.Utils:Print(string.format("%s awarded to %s (%s)", itemLink or "Item", coloredWinner, reason or ""))

    -- Hide response frame if shown for this item
    if ELM.ResponseFrame and ELM.ResponseFrame:IsShown() then
        ELM.ResponseFrame:Hide()
    end
end

-- Broadcast helpers for Session Manager

function Addon:BroadcastSessionStart(sessionData)
    local message = self:Serialize("SESSION_START", sessionData)
    self:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

function Addon:BroadcastSessionEnd(sessionId)
    local message = self:Serialize("SESSION_END", { sessionId = sessionId })
    self:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

function Addon:BroadcastSessionCancel(sessionId)
    local message = self:Serialize("SESSION_CANCEL", { sessionId = sessionId })
    self:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

function Addon:BroadcastResponseRequest(itemIndex, itemLink, timeout)
    local message = self:Serialize("RESPONSE_REQUEST", {
        itemIndex = itemIndex,
        itemLink = itemLink,
        timeout = timeout,
    })
    self:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

function Addon:SendResponse(itemIndex, responseId, note)
    -- Send to ML
    local session = ELM.SessionManager and ELM.SessionManager.currentSession
    if not session then return end

    local message = self:Serialize("RESPONSE", {
        itemIndex = itemIndex,
        responseId = responseId,
        note = note,
    })
    self:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", session.masterLooter)
end

function Addon:SendVote(itemIndex, candidate, voteValue)
    -- Send to ML
    local session = ELM.SessionManager and ELM.SessionManager.currentSession
    if not session or not ELM.SessionManager:IsMasterLooter() then
        -- Send to ML if we're not the ML
        if session then
            local message = self:Serialize("VOTE", {
                itemIndex = itemIndex,
                candidate = candidate,
                vote = voteValue,
            })
            self:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", session.masterLooter)
        end
    else
        -- We are ML, process locally
        self:HandleVote(UnitName("player"), {
            itemIndex = itemIndex,
            candidate = candidate,
            vote = voteValue,
        })
    end
end

function Addon:BroadcastAward(itemData, winner, reason)
    local message = self:Serialize("AWARD", {
        itemLink = itemData.link,
        itemName = itemData.name,
        itemId = itemData.id,
        itemLevel = itemData.itemLevel,
        quality = itemData.quality,
        winner = winner,
        winnerClass = select(2, UnitClass(winner)),
        reason = reason,
    })
    self:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end
