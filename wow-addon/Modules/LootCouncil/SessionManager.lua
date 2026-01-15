-- EdgeRush LootMan Session Manager
-- Manages loot council sessions for multiple items

local ADDON_NAME, ELM = ...

ELM.SessionManager = {}

local SessionManager = ELM.SessionManager

-- Session states
SessionManager.STATE = {
    IDLE = "IDLE",
    ACTIVE = "ACTIVE",
    VOTING = "VOTING",
    AWARDING = "AWARDING",
    COMPLETED = "COMPLETED",
}

-- Response types (RCLC compatible)
SessionManager.RESPONSES = {
    { id = "BIS",        text = "Best in Slot",    color = { r = 0.0, g = 1.0, b = 0.0 }, order = 1 },
    { id = "UPGRADE",    text = "Major Upgrade",   color = { r = 0.0, g = 0.8, b = 0.0 }, order = 2 },
    { id = "MINOR",      text = "Minor Upgrade",   color = { r = 0.5, g = 0.8, b = 0.0 }, order = 3 },
    { id = "SIDEGRADE",  text = "Sidegrade",       color = { r = 1.0, g = 1.0, b = 0.0 }, order = 4 },
    { id = "OFFSPEC",    text = "Offspec",         color = { r = 1.0, g = 0.5, b = 0.0 }, order = 5 },
    { id = "TRANSMOG",   text = "Transmog",        color = { r = 0.8, g = 0.5, b = 0.8 }, order = 6 },
    { id = "PASS",       text = "Pass",            color = { r = 0.5, g = 0.5, b = 0.5 }, order = 7 },
    { id = "AUTOPASS",   text = "Auto Pass",       color = { r = 0.3, g = 0.3, b = 0.3 }, order = 8 },
}

-- Initialize
function SessionManager:Initialize()
    self.sessions = {}
    self.currentSession = nil
    self.state = self.STATE.IDLE
    self.masterLooter = nil
    self.council = {}

    -- Register for loot events
    self:RegisterEvents()
end

-- Register loot-related events
function SessionManager:RegisterEvents()
    -- These would be registered through the main addon
    -- ENCOUNTER_LOOT_RECEIVED, LOOT_OPENED, LOOT_CLOSED, etc.
end

-- Check if player is master looter or has loot council rights
function SessionManager:IsMasterLooter()
    if not IsInRaid() then return false end

    local lootMethod, masterLooterPartyID = GetLootMethod()

    if lootMethod == "master" then
        if masterLooterPartyID == 0 then
            return true -- Player is ML
        end
        local mlName = GetMasterLootCandidate(1)
        return mlName == UnitName("player")
    end

    -- For personal/group loot, check if player is raid leader or assistant
    return UnitIsGroupLeader("player") or UnitIsGroupAssistant("player")
end

-- Check if player is on the loot council
function SessionManager:IsOnCouncil()
    if self:IsMasterLooter() then return true end

    local playerName = ELM.Utils:GetCharacterID()
    for _, member in ipairs(self.council) do
        if member == playerName then
            return true
        end
    end

    return UnitIsGroupAssistant("player")
end

-- Check if a specific player is a council member
function SessionManager:IsCouncilMember(playerName)
    if not playerName then return false end

    -- ML is always on council
    if self:IsMasterLooter() and playerName == ELM.Utils:GetCharacterID() then
        return true
    end

    -- Check custom council list
    for _, member in ipairs(self.council or {}) do
        if member == playerName then
            return true
        end
    end

    -- Fall back to raid officer check
    for i = 1, GetNumGroupMembers() do
        local name, rank = GetRaidRosterInfo(i)
        if name == playerName and rank >= 1 then -- Assistant or Leader
            return true
        end
    end

    return false
end

-- Start a new loot session
function SessionManager:StartSession(items)
    if self.state ~= self.STATE.IDLE then
        ELM.Utils:Print("A session is already in progress")
        return false
    end

    if not self:IsMasterLooter() then
        ELM.Utils:Print("You must be the master looter or raid leader to start a session")
        return false
    end

    -- Create session
    self.currentSession = {
        id = time() .. "-" .. math.random(1000, 9999),
        startTime = time(),
        items = {},
        candidates = {},
        responses = {},
        votes = {},
        awards = {},
        state = self.STATE.ACTIVE,
    }

    -- Add items to session
    for i, itemLink in ipairs(items) do
        local itemInfo = ELM.Utils:GetItemInfo(itemLink)
        if itemInfo then
            self.currentSession.items[i] = {
                index = i,
                link = itemLink,
                id = itemInfo.id,
                name = itemInfo.name,
                icon = itemInfo.icon,
                quality = itemInfo.quality,
                itemLevel = itemInfo.itemLevel,
                equipLoc = itemInfo.equipLoc,
                awarded = false,
                awardedTo = nil,
            }
        end
    end

    -- Build candidate list
    self:BuildCandidateList()

    self.state = self.STATE.ACTIVE

    -- Broadcast session start
    self:BroadcastSessionStart()

    -- Show loot frame
    if ELM.MasterLootFrame then
        ELM.MasterLootFrame:Show()
        ELM.MasterLootFrame:SetSession(self.currentSession)
    end

    ELM.Utils:Print("Loot session started with " .. #self.currentSession.items .. " item(s)")

    return true
end

-- Build list of eligible candidates
function SessionManager:BuildCandidateList()
    if not self.currentSession then return end

    local candidates = {}
    local roster = ELM.Utils:GetRaidRoster()

    for _, member in ipairs(roster) do
        if member.online then
            local flpsData = ELM.Display and ELM.Display:GetPlayerFLPS(member.name) or nil

            table.insert(candidates, {
                name = member.name,
                class = member.class,
                role = member.role,
                flps = flpsData and flpsData.score or 0,
                flpsRank = flpsData and flpsData.rank or 999,
                response = nil,
                vote = nil,
                note = nil,
                gear = {}, -- Would be populated from inspection or comms
            })
        end
    end

    -- Sort by FLPS
    table.sort(candidates, function(a, b)
        return a.flps > b.flps
    end)

    self.currentSession.candidates = candidates
end

-- Request responses from raid
function SessionManager:RequestResponses(itemIndex)
    if not self.currentSession then return end

    local item = self.currentSession.items[itemIndex]
    if not item then return end

    self.state = self.STATE.VOTING

    -- Broadcast request
    local data = {
        sessionId = self.currentSession.id,
        itemIndex = itemIndex,
        itemLink = item.link,
        itemLevel = item.itemLevel,
    }

    ELM.Comms:BroadcastMessage("RESPONSE_REQUEST", data)

    -- Announce to raid
    SendChatMessage(
        "[EdgeRush] Now considering: " .. item.link .. " - Please submit your response!",
        "RAID_WARNING"
    )
end

-- Handle incoming response
function SessionManager:HandleResponse(sender, data)
    if not self.currentSession then return end
    if self.currentSession.id ~= data.sessionId then return end

    local itemIndex = data.itemIndex
    if not self.currentSession.responses[itemIndex] then
        self.currentSession.responses[itemIndex] = {}
    end

    self.currentSession.responses[itemIndex][sender] = {
        response = data.response,
        note = data.note,
        currentGear = data.currentGear,
        timestamp = time(),
    }

    -- Update UI
    if ELM.MasterLootFrame and ELM.MasterLootFrame:IsShown() then
        ELM.MasterLootFrame:UpdateResponses(itemIndex)
    end

    ELM.Utils:Debug("Response from " .. sender .. ": " .. data.response)
end

-- Submit own response (for non-ML players)
function SessionManager:SubmitResponse(itemIndex, response, note)
    if not self.currentSession then return end

    local data = {
        sessionId = self.currentSession.id,
        itemIndex = itemIndex,
        response = response,
        note = note,
        currentGear = self:GetCurrentGearForSlot(self.currentSession.items[itemIndex].equipLoc),
    }

    -- If we're ML, handle locally
    if self:IsMasterLooter() then
        self:HandleResponse(ELM.Utils:GetCharacterID(), data)
    else
        -- Send to ML
        ELM.Comms:SendToML("RESPONSE", data)
    end
end

-- Get current gear for comparison
function SessionManager:GetCurrentGearForSlot(equipLoc)
    local slotID = self:EquipLocToSlot(equipLoc)
    if not slotID then return nil end

    local itemLink = GetInventoryItemLink("player", slotID)
    if itemLink then
        return {
            link = itemLink,
            itemLevel = ELM.Utils:GetItemLevelFromLink(itemLink),
        }
    end
    return nil
end

-- Convert equipment location to slot ID
function SessionManager:EquipLocToSlot(equipLoc)
    local mapping = {
        INVTYPE_HEAD = 1,
        INVTYPE_NECK = 2,
        INVTYPE_SHOULDER = 3,
        INVTYPE_CHEST = 5,
        INVTYPE_ROBE = 5,
        INVTYPE_WAIST = 6,
        INVTYPE_LEGS = 7,
        INVTYPE_FEET = 8,
        INVTYPE_WRIST = 9,
        INVTYPE_HAND = 10,
        INVTYPE_FINGER = 11,
        INVTYPE_TRINKET = 13,
        INVTYPE_CLOAK = 15,
        INVTYPE_WEAPON = 16,
        INVTYPE_SHIELD = 17,
        INVTYPE_2HWEAPON = 16,
        INVTYPE_WEAPONMAINHAND = 16,
        INVTYPE_WEAPONOFFHAND = 17,
        INVTYPE_HOLDABLE = 17,
    }
    return mapping[equipLoc]
end

-- Cast vote (for council members)
function SessionManager:CastVote(itemIndex, candidate, vote)
    if not self.currentSession then return end
    if not self:IsOnCouncil() then return end

    local voter = ELM.Utils:GetCharacterID()

    if not self.currentSession.votes[itemIndex] then
        self.currentSession.votes[itemIndex] = {}
    end
    if not self.currentSession.votes[itemIndex][candidate] then
        self.currentSession.votes[itemIndex][candidate] = {}
    end

    self.currentSession.votes[itemIndex][candidate][voter] = {
        vote = vote,
        timestamp = time(),
    }

    -- Broadcast vote to other council members
    local data = {
        sessionId = self.currentSession.id,
        itemIndex = itemIndex,
        candidate = candidate,
        vote = vote,
    }
    ELM.Comms:BroadcastToCouncil("VOTE", data)

    -- Update UI
    if ELM.MasterLootFrame then
        ELM.MasterLootFrame:UpdateVotes(itemIndex)
    end
end

-- Award item to candidate
function SessionManager:AwardItem(itemIndex, winner, reason)
    if not self.currentSession then return end
    if not self:IsMasterLooter() then return end

    local item = self.currentSession.items[itemIndex]
    if not item or item.awarded then return end

    -- Mark as awarded
    item.awarded = true
    item.awardedTo = winner
    item.awardReason = reason
    item.awardTime = time()

    -- Record in session awards
    table.insert(self.currentSession.awards, {
        itemIndex = itemIndex,
        item = item.link,
        winner = winner,
        reason = reason,
        timestamp = time(),
    })

    -- Broadcast award
    local data = {
        sessionId = self.currentSession.id,
        itemIndex = itemIndex,
        itemLink = item.link,
        winner = winner,
        reason = reason,
    }
    ELM.Comms:BroadcastMessage("AWARD", data)

    -- Announce to raid
    SendChatMessage(
        string.format("[EdgeRush] %s awarded to %s (%s)",
            item.link, winner, reason or "Council Decision"),
        "RAID"
    )

    -- Record in history
    if ELM.LootHistory then
        ELM.LootHistory:RecordAward(item, winner, reason)
    end

    -- Update UI
    if ELM.MasterLootFrame then
        ELM.MasterLootFrame:UpdateItem(itemIndex)
    end

    -- Check if session is complete
    self:CheckSessionComplete()
end

-- Check if all items have been awarded
function SessionManager:CheckSessionComplete()
    if not self.currentSession then return end

    local allAwarded = true
    for _, item in ipairs(self.currentSession.items) do
        if not item.awarded then
            allAwarded = false
            break
        end
    end

    if allAwarded then
        self:EndSession()
    end
end

-- End current session
function SessionManager:EndSession()
    if not self.currentSession then return end

    self.currentSession.endTime = time()
    self.currentSession.state = self.STATE.COMPLETED

    -- Store in history
    table.insert(self.sessions, self.currentSession)

    -- Save to database
    if ELM.LootHistory then
        ELM.LootHistory:SaveSession(self.currentSession)
    end

    -- Broadcast session end
    local data = {
        sessionId = self.currentSession.id,
    }
    ELM.Comms:BroadcastMessage("SESSION_END", data)

    ELM.Utils:Print("Loot session ended. " .. #self.currentSession.awards .. " item(s) awarded.")

    self.currentSession = nil
    self.state = self.STATE.IDLE

    -- Close UI
    if ELM.MasterLootFrame then
        ELM.MasterLootFrame:Hide()
    end
end

-- Cancel current session
function SessionManager:CancelSession()
    if not self.currentSession then return end
    if not self:IsMasterLooter() then return end

    -- Broadcast cancel
    local data = {
        sessionId = self.currentSession.id,
    }
    ELM.Comms:BroadcastMessage("SESSION_CANCEL", data)

    ELM.Utils:Print("Loot session cancelled")

    self.currentSession = nil
    self.state = self.STATE.IDLE

    if ELM.MasterLootFrame then
        ELM.MasterLootFrame:Hide()
    end
end

-- Broadcast session start
function SessionManager:BroadcastSessionStart()
    if not self.currentSession then return end

    local itemLinks = {}
    for _, item in ipairs(self.currentSession.items) do
        table.insert(itemLinks, item.link)
    end

    local data = {
        sessionId = self.currentSession.id,
        items = itemLinks,
        mlName = ELM.Utils:GetCharacterID(),
    }

    ELM.Comms:BroadcastMessage("SESSION_START", data)
end

-- Get response color
function SessionManager:GetResponseColor(responseId)
    for _, response in ipairs(self.RESPONSES) do
        if response.id == responseId then
            return response.color
        end
    end
    return { r = 1, g = 1, b = 1 }
end

-- Get response text
function SessionManager:GetResponseText(responseId)
    for _, response in ipairs(self.RESPONSES) do
        if response.id == responseId then
            return response.text
        end
    end
    return responseId
end

-- Auto-pass check (item not usable by class/spec)
function SessionManager:ShouldAutoPass(itemLink, playerClass)
    local itemInfo = ELM.Utils:GetItemInfo(itemLink)
    if not itemInfo then return false end

    -- Check armor type
    local _, _, _, _, _, itemType, itemSubType = C_Item.GetItemInfo(itemLink)

    if itemType == "Armor" then
        local armorTypes = {
            CLOTH = { "MAGE", "PRIEST", "WARLOCK" },
            LEATHER = { "DEMONHUNTER", "DRUID", "MONK", "ROGUE" },
            MAIL = { "EVOKER", "HUNTER", "SHAMAN" },
            PLATE = { "DEATHKNIGHT", "PALADIN", "WARRIOR" },
        }

        -- Check if class can use this armor type
        local subType = itemSubType:upper()
        if subType == "CLOTH" then return false end -- Everyone can use cloth

        local classArmor = nil
        for armor, classes in pairs(armorTypes) do
            for _, class in ipairs(classes) do
                if class == playerClass then
                    classArmor = armor
                    break
                end
            end
            if classArmor then break end
        end

        -- If item armor is higher tier than class can use, auto-pass
        local armorOrder = { CLOTH = 1, LEATHER = 2, MAIL = 3, PLATE = 4 }
        local itemArmorLevel = armorOrder[subType] or 0
        local classArmorLevel = armorOrder[classArmor] or 4

        if itemArmorLevel > classArmorLevel then
            return true
        end
    end

    return false
end
