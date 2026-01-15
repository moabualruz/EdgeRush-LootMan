-- EdgeRush LootMan Loot History
-- Tracks and displays loot award history

local ADDON_NAME, ELM = ...

ELM.LootHistory = {}

local LootHistory = ELM.LootHistory

local ROW_HEIGHT = 25
local MAX_ROWS = 50
local PAGE_SIZE = 20

-- Initialize history
function LootHistory:Initialize()
    self.history = ELM.Addon.db.global.lootHistory or {}
    self.sessions = ELM.Addon.db.global.lootSessions or {}
end

-- Record an award
function LootHistory:RecordAward(item, winner, reason, source)
    local entry = {
        id = time() .. "-" .. math.random(1000, 9999),
        timestamp = time(),
        itemId = item.id,
        itemLink = item.link,
        itemName = item.name,
        itemLevel = item.itemLevel,
        itemQuality = item.quality,
        winner = winner,
        reason = reason,
        source = source or "EdgeRush",
        instance = GetInstanceInfo(),
        boss = self:GetLastBossName(),
    }

    table.insert(self.history, 1, entry) -- Insert at beginning

    -- Limit history size
    while #self.history > 1000 do
        table.remove(self.history)
    end

    -- Save to database
    ELM.Addon.db.global.lootHistory = self.history

    ELM.Utils:Debug("Recorded award: " .. (item.link or "Unknown") .. " to " .. winner)
end

-- Save a complete session
function LootHistory:SaveSession(session)
    local sessionData = {
        id = session.id,
        startTime = session.startTime,
        endTime = session.endTime or time(),
        items = {},
        awards = session.awards,
        instance = GetInstanceInfo(),
    }

    for _, item in ipairs(session.items) do
        table.insert(sessionData.items, {
            link = item.link,
            name = item.name,
            awarded = item.awarded,
            awardedTo = item.awardedTo,
        })
    end

    table.insert(self.sessions, 1, sessionData)

    -- Limit sessions stored
    while #self.sessions > 100 do
        table.remove(self.sessions)
    end

    ELM.Addon.db.global.lootSessions = self.sessions
end

-- Get last boss name (from encounter end)
function LootHistory:GetLastBossName()
    return self.lastBossName or "Unknown"
end

-- Set last boss name
function LootHistory:SetLastBossName(name)
    self.lastBossName = name
end

-- Get history with filters
function LootHistory:GetHistory(filters)
    filters = filters or {}

    local results = {}

    for _, entry in ipairs(self.history) do
        local include = true

        -- Filter by winner
        if filters.winner and entry.winner ~= filters.winner then
            include = false
        end

        -- Filter by item quality
        if filters.minQuality and (entry.itemQuality or 0) < filters.minQuality then
            include = false
        end

        -- Filter by date range
        if filters.startDate and entry.timestamp < filters.startDate then
            include = false
        end
        if filters.endDate and entry.timestamp > filters.endDate then
            include = false
        end

        -- Filter by instance
        if filters.instance and entry.instance ~= filters.instance then
            include = false
        end

        if include then
            table.insert(results, entry)
        end
    end

    return results
end

-- Get loot for a player
function LootHistory:GetPlayerLoot(playerName, limit)
    limit = limit or 50

    local results = {}

    for _, entry in ipairs(self.history) do
        if entry.winner == playerName then
            table.insert(results, entry)
            if #results >= limit then
                break
            end
        end
    end

    return results
end

-- Get recent loot
function LootHistory:GetRecent(limit)
    limit = limit or 20

    local results = {}

    for i = 1, math.min(limit, #self.history) do
        table.insert(results, self.history[i])
    end

    return results
end

-- Create the history frame
function LootHistory:CreateFrame()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushLootHistory", UIParent, "BackdropTemplate")
    frame:SetSize(550, 450)
    frame:SetPoint("CENTER")
    frame:SetMovable(true)
    frame:EnableMouse(true)
    frame:RegisterForDrag("LeftButton")
    frame:SetScript("OnDragStart", frame.StartMoving)
    frame:SetScript("OnDragStop", frame.StopMovingOrSizing)
    frame:SetFrameStrata("HIGH")

    frame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background-Dark",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 32,
        insets = { left = 8, right = 8, top = 8, bottom = 8 }
    })
    frame:SetBackdropColor(0.08, 0.08, 0.12, 0.98)

    -- Title
    local title = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("TOP", frame, "TOP", 0, -15)
    title:SetText("Loot History")
    title:SetTextColor(1, 0.8, 0)

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)

    -- Filter controls
    local filterFrame = CreateFrame("Frame", nil, frame)
    filterFrame:SetSize(510, 30)
    filterFrame:SetPoint("TOP", title, "BOTTOM", 0, -10)

    -- Player filter dropdown
    local playerDropdown = CreateFrame("Frame", "ELMHistoryPlayerDropdown", filterFrame, "UIDropDownMenuTemplate")
    playerDropdown:SetPoint("LEFT", filterFrame, "LEFT", -10, 0)
    UIDropDownMenu_SetWidth(playerDropdown, 120)
    UIDropDownMenu_SetText(playerDropdown, "All Players")
    frame.playerDropdown = playerDropdown

    -- Quality filter dropdown
    local qualityDropdown = CreateFrame("Frame", "ELMHistoryQualityDropdown", filterFrame, "UIDropDownMenuTemplate")
    qualityDropdown:SetPoint("LEFT", playerDropdown, "RIGHT", 0, 0)
    UIDropDownMenu_SetWidth(qualityDropdown, 100)
    UIDropDownMenu_SetText(qualityDropdown, "All Quality")
    frame.qualityDropdown = qualityDropdown

    -- Export button
    local exportBtn = CreateFrame("Button", nil, filterFrame, "UIPanelButtonTemplate")
    exportBtn:SetSize(80, 22)
    exportBtn:SetPoint("RIGHT", filterFrame, "RIGHT", 0, 0)
    exportBtn:SetText("Export")
    exportBtn:SetScript("OnClick", function()
        self:ShowExportFrame()
    end)

    -- Header
    local header = CreateFrame("Frame", nil, frame)
    header:SetSize(510, 20)
    header:SetPoint("TOP", filterFrame, "BOTTOM", 0, -5)

    local headers = {
        { text = "Date", width = 80, offset = 5 },
        { text = "Item", width = 200, offset = 90 },
        { text = "Winner", width = 100, offset = 295 },
        { text = "Reason", width = 100, offset = 400 },
    }

    for _, h in ipairs(headers) do
        local text = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
        text:SetPoint("LEFT", header, "LEFT", h.offset, 0)
        text:SetText(h.text)
        text:SetWidth(h.width)
    end

    -- Scroll frame
    local scrollFrame = CreateFrame("ScrollFrame", "ELMHistoryScroll", frame, "UIPanelScrollFrameTemplate")
    scrollFrame:SetPoint("TOPLEFT", header, "BOTTOMLEFT", 0, -5)
    scrollFrame:SetPoint("BOTTOMRIGHT", frame, "BOTTOMRIGHT", -30, 45)

    local scrollChild = CreateFrame("Frame")
    scrollFrame:SetScrollChild(scrollChild)
    scrollChild:SetWidth(490)
    scrollChild:SetHeight(1)
    self.scrollChild = scrollChild

    -- Create rows
    self.rows = {}
    for i = 1, MAX_ROWS do
        local row = self:CreateHistoryRow(scrollChild, i)
        row:SetPoint("TOPLEFT", scrollChild, "TOPLEFT", 0, -((i - 1) * ROW_HEIGHT))
        self.rows[i] = row
    end

    -- Page controls
    local pageFrame = CreateFrame("Frame", nil, frame)
    pageFrame:SetSize(510, 25)
    pageFrame:SetPoint("BOTTOM", frame, "BOTTOM", 0, 15)

    local prevBtn = CreateFrame("Button", nil, pageFrame, "UIPanelButtonTemplate")
    prevBtn:SetSize(60, 22)
    prevBtn:SetPoint("LEFT", pageFrame, "LEFT", 5, 0)
    prevBtn:SetText("< Prev")
    prevBtn:SetScript("OnClick", function()
        self:PrevPage()
    end)
    frame.prevBtn = prevBtn

    local pageText = pageFrame:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    pageText:SetPoint("CENTER", pageFrame, "CENTER", 0, 0)
    pageText:SetText("Page 1")
    frame.pageText = pageText

    local nextBtn = CreateFrame("Button", nil, pageFrame, "UIPanelButtonTemplate")
    nextBtn:SetSize(60, 22)
    nextBtn:SetPoint("RIGHT", pageFrame, "RIGHT", -5, 0)
    nextBtn:SetText("Next >")
    nextBtn:SetScript("OnClick", function()
        self:NextPage()
    end)
    frame.nextBtn = nextBtn

    frame:Hide()
    self.frame = frame
    self.currentPage = 1
    self.currentFilters = {}

    return frame
end

-- Create history row
function LootHistory:CreateHistoryRow(parent, index)
    local row = CreateFrame("Frame", nil, parent)
    row:SetSize(490, ROW_HEIGHT - 2)
    row.index = index

    if index % 2 == 0 then
        local bg = row:CreateTexture(nil, "BACKGROUND")
        bg:SetAllPoints()
        bg:SetColorTexture(1, 1, 1, 0.02)
    end

    -- Hover
    row:EnableMouse(true)
    local highlight = row:CreateTexture(nil, "BACKGROUND")
    highlight:SetAllPoints()
    highlight:SetColorTexture(1, 1, 1, 0.05)
    highlight:Hide()
    row.highlight = highlight

    row:SetScript("OnEnter", function(r)
        r.highlight:Show()
        if r.itemLink then
            GameTooltip:SetOwner(r, "ANCHOR_RIGHT")
            GameTooltip:SetHyperlink(r.itemLink)
            GameTooltip:Show()
        end
    end)
    row:SetScript("OnLeave", function(r)
        r.highlight:Hide()
        GameTooltip:Hide()
    end)

    -- Date
    local dateText = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    dateText:SetPoint("LEFT", row, "LEFT", 5, 0)
    dateText:SetWidth(80)
    dateText:SetJustifyH("LEFT")
    row.dateText = dateText

    -- Item icon
    local icon = row:CreateTexture(nil, "ARTWORK")
    icon:SetSize(20, 20)
    icon:SetPoint("LEFT", row, "LEFT", 90, 0)
    row.icon = icon

    -- Item name
    local itemName = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    itemName:SetPoint("LEFT", icon, "RIGHT", 5, 0)
    itemName:SetWidth(170)
    itemName:SetJustifyH("LEFT")
    row.itemName = itemName

    -- Winner
    local winner = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    winner:SetPoint("LEFT", row, "LEFT", 295, 0)
    winner:SetWidth(100)
    winner:SetJustifyH("LEFT")
    row.winner = winner

    -- Reason
    local reason = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    reason:SetPoint("LEFT", row, "LEFT", 400, 0)
    reason:SetWidth(100)
    reason:SetJustifyH("LEFT")
    reason:SetTextColor(0.7, 0.7, 0.7)
    row.reason = reason

    row:Hide()
    return row
end

-- Update display
function LootHistory:UpdateDisplay()
    local history = self:GetHistory(self.currentFilters)
    local startIdx = (self.currentPage - 1) * PAGE_SIZE + 1
    local endIdx = math.min(startIdx + PAGE_SIZE - 1, #history)

    for i, row in ipairs(self.rows) do
        local entryIdx = startIdx + i - 1
        local entry = history[entryIdx]

        if entry and entryIdx <= endIdx then
            row.dateText:SetText(date("%m/%d %H:%M", entry.timestamp))
            row.icon:SetTexture(select(10, C_Item.GetItemInfo(entry.itemLink)) or "Interface\\Icons\\INV_Misc_QuestionMark")
            row.itemName:SetText(entry.itemLink or entry.itemName or "Unknown")
            row.winner:SetText(entry.winner or "Unknown")
            row.reason:SetText(entry.reason or "-")
            row.itemLink = entry.itemLink
            row:Show()
        else
            row:Hide()
        end
    end

    -- Update scroll height
    local displayCount = endIdx - startIdx + 1
    self.scrollChild:SetHeight(math.max(displayCount * ROW_HEIGHT, 1))

    -- Update page controls
    local totalPages = math.ceil(#history / PAGE_SIZE)
    self.frame.pageText:SetText(string.format("Page %d of %d", self.currentPage, math.max(totalPages, 1)))
    self.frame.prevBtn:SetEnabled(self.currentPage > 1)
    self.frame.nextBtn:SetEnabled(self.currentPage < totalPages)
end

-- Next/Prev page
function LootHistory:NextPage()
    local history = self:GetHistory(self.currentFilters)
    local totalPages = math.ceil(#history / PAGE_SIZE)

    if self.currentPage < totalPages then
        self.currentPage = self.currentPage + 1
        self:UpdateDisplay()
    end
end

function LootHistory:PrevPage()
    if self.currentPage > 1 then
        self.currentPage = self.currentPage - 1
        self:UpdateDisplay()
    end
end

-- Show the history frame
function LootHistory:Show()
    if not self.frame then
        self:CreateFrame()
    end
    self.currentPage = 1
    self:UpdateDisplay()
    self.frame:Show()
end

-- Hide
function LootHistory:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Toggle
function LootHistory:Toggle()
    if self.frame and self.frame:IsShown() then
        self:Hide()
    else
        self:Show()
    end
end

-- Show export frame
function LootHistory:ShowExportFrame()
    -- Create CSV export
    local history = self:GetHistory(self.currentFilters)
    local csv = "Date,Item,ItemLevel,Winner,Reason,Instance\n"

    for _, entry in ipairs(history) do
        csv = csv .. string.format("%s,%s,%d,%s,%s,%s\n",
            date("%Y-%m-%d %H:%M:%S", entry.timestamp),
            (entry.itemName or "Unknown"):gsub(",", ";"),
            entry.itemLevel or 0,
            entry.winner or "Unknown",
            (entry.reason or ""):gsub(",", ";"),
            (entry.instance or ""):gsub(",", ";")
        )
    end

    -- Show in edit box for copying
    local exportFrame = CreateFrame("Frame", "ELMExportFrame", UIParent, "BackdropTemplate")
    exportFrame:SetSize(400, 300)
    exportFrame:SetPoint("CENTER")
    exportFrame:SetFrameStrata("TOOLTIP")

    exportFrame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 32,
        insets = { left = 8, right = 8, top = 8, bottom = 8 }
    })

    local title = exportFrame:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    title:SetPoint("TOP", exportFrame, "TOP", 0, -15)
    title:SetText("Copy this data (Ctrl+C)")

    local closeBtn = CreateFrame("Button", nil, exportFrame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", exportFrame, "TOPRIGHT", -5, -5)
    closeBtn:SetScript("OnClick", function()
        exportFrame:Hide()
    end)

    local scrollFrame = CreateFrame("ScrollFrame", nil, exportFrame, "UIPanelScrollFrameTemplate")
    scrollFrame:SetPoint("TOPLEFT", title, "BOTTOMLEFT", 10, -10)
    scrollFrame:SetPoint("BOTTOMRIGHT", exportFrame, "BOTTOMRIGHT", -30, 10)

    local editBox = CreateFrame("EditBox", nil, scrollFrame)
    editBox:SetMultiLine(true)
    editBox:SetFontObject("ChatFontNormal")
    editBox:SetWidth(340)
    editBox:SetText(csv)
    editBox:HighlightText()
    editBox:SetAutoFocus(true)
    scrollFrame:SetScrollChild(editBox)

    exportFrame:Show()
end
