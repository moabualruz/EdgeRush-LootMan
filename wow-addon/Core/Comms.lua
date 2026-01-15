-- EdgeRush LootMan Communications
-- Addon communication utilities

local ADDON_NAME, ELM = ...

ELM.Comms = {}

local Comms = ELM.Comms

-- Send FLPS data to a player
function Comms:SendFLPS(target, flpsData)
    if not ELM.Addon then return end

    local message = ELM.Addon:Serialize("FLPS_DATA", flpsData)
    ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", target)
end

-- Broadcast FLPS to raid
function Comms:BroadcastFLPS(flpsData)
    if not ELM.Addon or not IsInRaid() then return end

    local message = ELM.Addon:Serialize("FLPS_DATA", flpsData)
    ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

-- Send loot vote
function Comms:SendVote(itemLink, candidate, vote)
    if not ELM.Addon or not IsInRaid() then return end

    local data = {
        item = itemLink,
        candidate = candidate,
        vote = vote,
        voter = ELM.Utils:GetCharacterID(),
    }

    local message = ELM.Addon:Serialize("LOOT_VOTE", data)
    -- Send to raid officers only
    for i = 1, GetNumGroupMembers() do
        local name, rank = GetRaidRosterInfo(i)
        if rank >= 1 then -- Assistant or Leader
            ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", name)
        end
    end
end

-- Announce loot award
function Comms:AnnounceLootAward(itemLink, winner, reason)
    if not ELM.Addon then return end

    local data = {
        item = itemLink,
        winner = winner,
        reason = reason,
        announcer = ELM.Utils:GetCharacterID(),
        timestamp = time(),
    }

    -- Broadcast to raid
    if IsInRaid() then
        local message = ELM.Addon:Serialize("LOOT_AWARD", data)
        ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")

        -- Also announce in raid chat
        SendChatMessage(
            string.format("[EdgeRush] %s awarded to %s (%s)",
                itemLink, winner, reason or "FLPS"),
            "RAID"
        )
    end
end

-- Request leaderboard sync
function Comms:RequestLeaderboardSync()
    if not ELM.Addon or not IsInRaid() then return end

    local message = ELM.Addon:Serialize("LEADERBOARD_REQUEST", {})
    ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

-- Send leaderboard data
function Comms:SendLeaderboard(target, leaderboardData)
    if not ELM.Addon then return end

    local message = ELM.Addon:Serialize("LEADERBOARD_DATA", leaderboardData)
    if target then
        ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", target)
    elseif IsInRaid() then
        ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
    end
end

-- Send wishlist request
function Comms:RequestWishlist(target)
    if not ELM.Addon then return end

    local message = ELM.Addon:Serialize("WISHLIST_REQUEST", {})
    ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", target)
end

-- Send wishlist data
function Comms:SendWishlist(target, wishlistData)
    if not ELM.Addon then return end

    local message = ELM.Addon:Serialize("WISHLIST_DATA", wishlistData)
    ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", target)
end

-- ============================================================
-- Session Manager Communication Methods
-- ============================================================

-- Broadcast a message to the raid
function Comms:BroadcastMessage(msgType, data)
    if not ELM.Addon or not IsInRaid() then return end

    local message = ELM.Addon:Serialize(msgType, data)
    ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "RAID")
end

-- Send message to the master looter
function Comms:SendToML(msgType, data)
    if not ELM.Addon then return end
    if not ELM.SessionManager or not ELM.SessionManager.currentSession then return end

    local mlName = ELM.SessionManager.currentSession.masterLooter
    if not mlName then
        ELM.Utils:Debug("No master looter to send to")
        return
    end

    local message = ELM.Addon:Serialize(msgType, data)
    ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", mlName)
end

-- Broadcast to council members only
function Comms:BroadcastToCouncil(msgType, data)
    if not ELM.Addon or not IsInRaid() then return end

    local message = ELM.Addon:Serialize(msgType, data)

    -- Get council members from Session Manager or fall back to raid officers
    local councilMembers = {}

    if ELM.SessionManager and ELM.SessionManager.council and #ELM.SessionManager.council > 0 then
        councilMembers = ELM.SessionManager.council
    else
        -- Fall back to raid officers (assistants and leader)
        for i = 1, GetNumGroupMembers() do
            local name, rank = GetRaidRosterInfo(i)
            if rank >= 1 then -- Assistant or Leader
                table.insert(councilMembers, name)
            end
        end
    end

    -- Send to each council member
    for _, memberName in ipairs(councilMembers) do
        if memberName ~= ELM.Utils:GetCharacterID() then
            ELM.Addon:SendCommMessage(ELM.COMM_PREFIX, message, "WHISPER", memberName)
        end
    end
end

-- Send item response to ML
function Comms:SendItemResponse(itemIndex, responseId, note, currentGear)
    if not ELM.Addon then return end
    if not ELM.SessionManager or not ELM.SessionManager.currentSession then return end

    local session = ELM.SessionManager.currentSession
    local data = {
        sessionId = session.id,
        itemIndex = itemIndex,
        responseId = responseId,
        note = note,
        currentGear = currentGear,
    }

    -- If we are ML, handle locally
    if ELM.SessionManager:IsMasterLooter() then
        ELM.Addon:HandleResponse(ELM.Utils:GetCharacterID(), data)
    else
        self:SendToML("RESPONSE", data)
    end
end

-- Send vote for a candidate
function Comms:SendCandidateVote(itemIndex, candidate, vote)
    if not ELM.Addon then return end
    if not ELM.SessionManager or not ELM.SessionManager.currentSession then return end

    local session = ELM.SessionManager.currentSession
    local data = {
        sessionId = session.id,
        itemIndex = itemIndex,
        candidate = candidate,
        vote = vote,
    }

    -- If we are ML, handle locally
    if ELM.SessionManager:IsMasterLooter() then
        ELM.Addon:HandleVote(ELM.Utils:GetCharacterID(), data)
    else
        self:SendToML("VOTE", data)
    end

    -- Also broadcast to other council members for their UI
    self:BroadcastToCouncil("VOTE", data)
end

-- Get master looter name
function Comms:GetMasterLooterName()
    if ELM.SessionManager and ELM.SessionManager.currentSession then
        return ELM.SessionManager.currentSession.masterLooter
    end

    -- Fall back to game API
    local lootMethod, masterLooterPartyID = GetLootMethod()
    if lootMethod == "master" then
        if masterLooterPartyID == 0 then
            return UnitName("player")
        end
        return GetMasterLootCandidate(1)
    end

    -- For personal/group loot, return raid leader
    for i = 1, GetNumGroupMembers() do
        local name, rank = GetRaidRosterInfo(i)
        if rank == 2 then -- Raid leader
            return name
        end
    end

    return nil
end

-- Check if a player is a council member
function Comms:IsCouncilMember(playerName)
    if not playerName then return false end

    -- ML is always on council
    if self:GetMasterLooterName() == playerName then
        return true
    end

    -- Check custom council list
    if ELM.SessionManager and ELM.SessionManager.council then
        for _, member in ipairs(ELM.SessionManager.council) do
            if member == playerName then
                return true
            end
        end
    end

    -- Fall back to raid officer check
    for i = 1, GetNumGroupMembers() do
        local name, rank = GetRaidRosterInfo(i)
        if name == playerName and rank >= 1 then
            return true
        end
    end

    return false
end
