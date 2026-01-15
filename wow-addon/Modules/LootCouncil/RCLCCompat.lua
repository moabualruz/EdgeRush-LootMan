-- EdgeRush LootMan RCLootCouncil Compatibility
-- Integration with RCLootCouncil addon

local ADDON_NAME, ELM = ...

ELM.RCLCCompat = {}

local RCLCCompat = ELM.RCLCCompat

-- Check if RCLootCouncil is loaded
function RCLCCompat:IsRCLCLoaded()
    return RCLootCouncil ~= nil
end

-- Initialize RCLC compatibility
function RCLCCompat:Initialize()
    if not self:IsRCLCLoaded() then
        ELM.Utils:Debug("RCLootCouncil not detected")
        return
    end

    ELM.Utils:Debug("RCLootCouncil detected, initializing compatibility")

    -- Hook into RCLC events
    self:HookRCLCEvents()

    -- Add our custom column
    self:AddFLPSColumn()
end

-- Hook RCLC events
function RCLCCompat:HookRCLCEvents()
    if not self:IsRCLCLoaded() then return end

    -- Hook the session frame
    local ML = RCLootCouncil:GetActiveModule("masterlooter")
    if ML then
        -- Hook award function to track awards
        hooksecurefunc(ML, "Award", function(_, session, winner, response, reason, ...)
            self:OnRCLCAward(session, winner, response, reason)
        end)
    end

    -- Hook voting frame update
    local VF = RCLootCouncil:GetActiveModule("votingframe")
    if VF and VF.Update then
        hooksecurefunc(VF, "Update", function()
            self:OnVotingFrameUpdate()
        end)
    end
end

-- Add FLPS column to RCLC voting frame
function RCLCCompat:AddFLPSColumn()
    if not self:IsRCLCLoaded() then return end

    local VF = RCLootCouncil:GetActiveModule("votingframe")
    if not VF then return end

    -- Check if column already exists
    if self.columnAdded then return end

    -- Add column to voting frame
    -- Note: Actual implementation depends on RCLC version
    -- This is a simplified example

    local columnDef = {
        name = "FLPS",
        DoCellUpdate = function(rowFrame, frame, data, cols, row, realrow, column, fShow, table)
            local name = data[realrow].name
            local flpsData = ELM.Display:GetPlayerFLPS(name)

            if flpsData then
                local score = flpsData.score or 0
                frame.text:SetText(ELM.Utils:ColorByFLPS(
                    ELM.Utils:FormatFLPS(score),
                    score
                ))
            else
                frame.text:SetText("N/A")
            end
        end,
        colName = "flps",
        sortnext = 1,
        width = 60,
        defaultsort = "dsc",
    }

    -- Try to add column if RCLC provides the method
    if VF.AddColumn then
        VF:AddColumn(columnDef)
    end

    self.columnAdded = true
end

-- Called when RCLC awards an item
function RCLCCompat:OnRCLCAward(session, winner, response, reason)
    if not session then return end

    -- Get item info from session
    local lootTable = RCLootCouncil:GetLootTable()
    if not lootTable or not lootTable[session] then return end

    local itemLink = lootTable[session].link

    ELM.Utils:Debug("RCLC Award: " .. tostring(itemLink) .. " to " .. tostring(winner))

    -- Record the award in our system
    self:RecordAward(itemLink, winner, response, reason)
end

-- Record an award
function RCLCCompat:RecordAward(itemLink, winner, response, reason)
    -- Store award in our database
    local awards = ELM.Addon.db.global.lootHistory or {}

    table.insert(awards, {
        item = itemLink,
        winner = winner,
        response = response,
        reason = reason,
        timestamp = time(),
        source = "RCLC",
    })

    ELM.Addon.db.global.lootHistory = awards

    -- Also broadcast via our comms for tracking
    ELM.Comms:AnnounceLootAward(itemLink, winner, "RCLC: " .. (response or ""))
end

-- Called when RCLC voting frame updates
function RCLCCompat:OnVotingFrameUpdate()
    -- Could refresh our FLPS column data here if needed
end

-- Get loot history
function RCLCCompat:GetLootHistory(limit)
    local awards = ELM.Addon.db.global.lootHistory or {}
    limit = limit or 50

    -- Sort by timestamp descending
    table.sort(awards, function(a, b)
        return (a.timestamp or 0) > (b.timestamp or 0)
    end)

    -- Return limited results
    local result = {}
    for i = 1, math.min(#awards, limit) do
        table.insert(result, awards[i])
    end

    return result
end

-- Print loot history to chat
function RCLCCompat:PrintLootHistory(limit)
    local history = self:GetLootHistory(limit or 10)

    if #history == 0 then
        ELM.Utils:Print("No loot history recorded")
        return
    end

    ELM.Utils:Print("Recent Loot History:")
    for i, award in ipairs(history) do
        local dateStr = date("%m/%d %H:%M", award.timestamp)
        print(string.format("  %s: %s -> %s (%s)",
            dateStr,
            award.item or "Unknown",
            award.winner or "Unknown",
            award.response or "N/A"
        ))
    end
end

-- Export awards to format suitable for web sync
function RCLCCompat:ExportAwards()
    local awards = ELM.Addon.db.global.lootHistory or {}

    local export = {}
    for _, award in ipairs(awards) do
        local itemInfo = award.item and ELM.Utils:GetItemInfo(award.item)

        table.insert(export, {
            itemId = itemInfo and itemInfo.id or 0,
            itemName = itemInfo and itemInfo.name or "Unknown",
            winner = award.winner,
            response = award.response,
            reason = award.reason,
            timestamp = award.timestamp,
        })
    end

    return export
end

-- ============================================================
-- Enhanced RCLC Integration
-- ============================================================

-- Hook into RCLC session events for bidirectional sync
function RCLCCompat:HookSessionEvents()
    if not self:IsRCLCLoaded() then return end

    -- Hook session start
    local ML = RCLootCouncil:GetActiveModule("masterlooter")
    if ML and ML.NewML then
        hooksecurefunc(ML, "NewML", function(_, ...)
            self:OnRCLCSessionStart()
        end)
    end

    -- Hook session end
    if ML and ML.EndSession then
        hooksecurefunc(ML, "EndSession", function(_, ...)
            self:OnRCLCSessionEnd()
        end)
    end
end

function RCLCCompat:OnRCLCSessionStart()
    ELM.Utils:Debug("RCLC session started")

    -- Request FLPS data from raid members for display
    if ELM.Addon then
        ELM.Addon:RequestRaidFLPS()
    end
end

function RCLCCompat:OnRCLCSessionEnd()
    ELM.Utils:Debug("RCLC session ended")
end

-- Inject FLPS data into RCLC candidate tooltips
function RCLCCompat:InjectFLPSTooltip()
    if not self:IsRCLCLoaded() then return end

    local VF = RCLootCouncil:GetActiveModule("votingframe")
    if not VF or not VF.GetFrame then return end

    local frame = VF:GetFrame()
    if not frame or not frame.ScrollTable then return end

    -- Hook the scroll table's cell tooltip
    local scrollTable = frame.ScrollTable
    if scrollTable and not self.tooltipHooked then
        local oldOnEnter = scrollTable.OnEnter
        scrollTable.OnEnter = function(rowFrame, cellFrame, data, cols, row, realrow, column, scrollingTable, ...)
            if oldOnEnter then
                oldOnEnter(rowFrame, cellFrame, data, cols, row, realrow, column, scrollingTable, ...)
            end

            -- Add FLPS data to tooltip if showing for a player
            if GameTooltip:IsShown() and data and data[realrow] and data[realrow].name then
                local name = data[realrow].name
                local flpsData = ELM.Display and ELM.Display:GetPlayerFLPS(name)

                if flpsData then
                    GameTooltip:AddLine(" ")
                    GameTooltip:AddLine("|cff00ccffEdgeRush FLPS Data:|r")
                    GameTooltip:AddDoubleLine("FLPS Score:",
                        ELM.Utils:ColorByFLPS(ELM.Utils:FormatFLPS(flpsData.score), flpsData.score))
                    GameTooltip:AddDoubleLine("Rank:", "#" .. (flpsData.rank or "N/A"))
                    if flpsData.rms then
                        GameTooltip:AddDoubleLine("RMS (Merit):", string.format("%.1f%%", flpsData.rms * 100))
                    end
                    if flpsData.ipi then
                        GameTooltip:AddDoubleLine("IPI (Priority):", string.format("%.1f%%", flpsData.ipi * 100))
                    end
                    if flpsData.rdf then
                        GameTooltip:AddDoubleLine("RDF (Recency):", string.format("%.1f%%", flpsData.rdf * 100))
                    end
                    GameTooltip:Show()
                end
            end
        end
        self.tooltipHooked = true
    end
end

-- Sort RCLC candidates by FLPS
function RCLCCompat:SortByFLPS()
    if not self:IsRCLCLoaded() then return end

    local VF = RCLootCouncil:GetActiveModule("votingframe")
    if not VF then return end

    -- If RCLC provides a custom sort method
    if VF.Sort then
        VF:Sort("flps", "dsc")
    end
end

-- Get RCLC loot table for current session
function RCLCCompat:GetCurrentLootTable()
    if not self:IsRCLCLoaded() then return nil end

    return RCLootCouncil:GetLootTable()
end

-- Get RCLC candidates for a session
function RCLCCompat:GetCandidates(session)
    if not self:IsRCLCLoaded() then return {} end

    local lootTable = RCLootCouncil:GetLootTable()
    if not lootTable or not lootTable[session] then return {} end

    return lootTable[session].candidates or {}
end

-- Check if we should use EdgeRush or RCLC
function RCLCCompat:ShouldUseEdgeRush()
    -- If RCLC is not loaded, use EdgeRush
    if not self:IsRCLCLoaded() then return true end

    -- Check user preference
    if ELM.Addon and ELM.Addon.db and ELM.Addon.db.profile then
        local preference = ELM.Addon.db.profile.lootCouncil.preferEdgeRush
        if preference ~= nil then
            return preference
        end
    end

    -- Default: use EdgeRush if ML, use RCLC integration otherwise
    if ELM.SessionManager and ELM.SessionManager:IsMasterLooter() then
        return true
    end

    return false
end

-- Sync EdgeRush session with RCLC
function RCLCCompat:SyncWithRCLC(sessionData)
    if not self:IsRCLCLoaded() then return end

    -- This would sync our session data with RCLC
    -- Implementation depends on what RCLC allows
    ELM.Utils:Debug("Syncing session with RCLC (if available)")
end

-- Import RCLC history into EdgeRush
function RCLCCompat:ImportRCLCHistory()
    if not self:IsRCLCLoaded() then return 0 end

    -- Get RCLC history
    local history = RCLootCouncil.db and RCLootCouncil.db.profile and RCLootCouncil.db.profile.lootDB
    if not history then
        ELM.Utils:Print("No RCLC history found")
        return 0
    end

    local imported = 0
    local existingTimestamps = {}

    -- Build index of existing awards
    local ourHistory = ELM.Addon.db.global.lootHistory or {}
    for _, award in ipairs(ourHistory) do
        existingTimestamps[award.timestamp .. (award.item or "")] = true
    end

    -- Import each RCLC award
    for _, entry in ipairs(history) do
        local key = (entry.time or 0) .. (entry.lootWon or "")
        if not existingTimestamps[key] then
            table.insert(ourHistory, {
                item = entry.lootWon,
                winner = entry.winner,
                response = entry.response,
                reason = entry.responseID,
                timestamp = entry.time or time(),
                source = "RCLC_IMPORT",
                instance = entry.instance,
                boss = entry.boss,
            })
            imported = imported + 1
        end
    end

    ELM.Addon.db.global.lootHistory = ourHistory

    ELM.Utils:Print("Imported " .. imported .. " awards from RCLC history")
    return imported
end

-- Export EdgeRush history to RCLC format (for backup/transfer)
function RCLCCompat:ExportToRCLCFormat()
    local ourHistory = ELM.Addon.db.global.lootHistory or {}
    local export = {}

    for _, award in ipairs(ourHistory) do
        table.insert(export, {
            lootWon = award.item,
            winner = award.winner,
            response = award.response,
            responseID = award.reason,
            time = award.timestamp,
            instance = award.instance,
            boss = award.boss,
        })
    end

    return export
end

-- Register RCLC integration command
function RCLCCompat:RegisterCommands()
    -- Add slash command for RCLC import
    if ELM.Addon then
        ELM.Addon:RegisterChatCommand("elmrclc", function(input)
            local args = {}
            for word in input:gmatch("%S+") do
                table.insert(args, word:lower())
            end

            local cmd = args[1] or "help"

            if cmd == "import" then
                self:ImportRCLCHistory()
            elseif cmd == "status" then
                self:PrintRCLCStatus()
            elseif cmd == "sync" then
                if ELM.SessionManager and ELM.SessionManager.currentSession then
                    self:SyncWithRCLC(ELM.SessionManager.currentSession)
                else
                    ELM.Utils:Print("No active session to sync")
                end
            else
                ELM.Utils:Print("RCLC Commands:")
                print("  /elmrclc import - Import RCLC loot history")
                print("  /elmrclc status - Show RCLC integration status")
                print("  /elmrclc sync - Sync current session with RCLC")
            end
        end)
    end
end

-- Print RCLC integration status
function RCLCCompat:PrintRCLCStatus()
    ELM.Utils:Print("RCLC Integration Status:")

    if self:IsRCLCLoaded() then
        print("  RCLootCouncil: |cff00ff00Detected|r")
        print("  FLPS Column: " .. (self.columnAdded and "|cff00ff00Added|r" or "|cffff0000Not Added|r"))
        print("  Tooltip Hook: " .. (self.tooltipHooked and "|cff00ff00Active|r" or "|cffff0000Inactive|r"))

        local rclcVersion = RCLootCouncil.version or "Unknown"
        print("  RCLC Version: " .. rclcVersion)
    else
        print("  RCLootCouncil: |cffff0000Not Detected|r")
        print("  Using: EdgeRush Native Loot Council")
    end

    local useEdgeRush = self:ShouldUseEdgeRush()
    print("  Active System: " .. (useEdgeRush and "|cff00ccffEdgeRush|r" or "|cffffff00RCLC + FLPS|r"))
end
