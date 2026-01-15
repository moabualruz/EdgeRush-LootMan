-- EdgeRush LootMan FLPS Leaderboard
-- Shows guild FLPS rankings

local ADDON_NAME, ELM = ...

ELM.Leaderboard = {}

local Leaderboard = ELM.Leaderboard

local ROW_HEIGHT = 20
local MAX_ROWS = 15

-- Create the leaderboard frame
function Leaderboard:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushLeaderboard", UIParent, "BackdropTemplate")
    frame:SetSize(350, 400)
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
    frame:SetBackdropColor(0, 0, 0, 0.9)

    -- Title
    local title = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("TOP", frame, "TOP", 0, -15)
    title:SetText("FLPS Leaderboard")
    title:SetTextColor(0, 0.8, 1)

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)

    -- Refresh button
    local refreshBtn = CreateFrame("Button", nil, frame, "UIPanelButtonTemplate")
    refreshBtn:SetSize(80, 22)
    refreshBtn:SetPoint("TOPRIGHT", closeBtn, "TOPLEFT", -5, -3)
    refreshBtn:SetText("Refresh")
    refreshBtn:SetScript("OnClick", function()
        self:RequestSync()
    end)

    -- Header row
    local header = CreateFrame("Frame", nil, frame)
    header:SetSize(320, 20)
    header:SetPoint("TOP", title, "BOTTOM", 0, -10)

    local rankHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    rankHeader:SetPoint("LEFT", header, "LEFT", 5, 0)
    rankHeader:SetText("Rank")
    rankHeader:SetWidth(40)

    local nameHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    nameHeader:SetPoint("LEFT", rankHeader, "RIGHT", 5, 0)
    nameHeader:SetText("Name")
    nameHeader:SetWidth(120)

    local classHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    classHeader:SetPoint("LEFT", nameHeader, "RIGHT", 5, 0)
    classHeader:SetText("Class")
    classHeader:SetWidth(70)

    local scoreHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    scoreHeader:SetPoint("LEFT", classHeader, "RIGHT", 5, 0)
    scoreHeader:SetText("FLPS")
    scoreHeader:SetWidth(60)

    -- Scroll frame
    local scrollFrame = CreateFrame("ScrollFrame", "EdgeRushLeaderboardScroll", frame, "UIPanelScrollFrameTemplate")
    scrollFrame:SetPoint("TOPLEFT", header, "BOTTOMLEFT", 0, -5)
    scrollFrame:SetPoint("BOTTOMRIGHT", frame, "BOTTOMRIGHT", -30, 15)

    local scrollChild = CreateFrame("Frame")
    scrollFrame:SetScrollChild(scrollChild)
    scrollChild:SetWidth(320)
    scrollChild:SetHeight(1) -- Will be adjusted

    -- Create row templates
    self.rows = {}
    for i = 1, MAX_ROWS do
        local row = self:CreateRow(scrollChild, i)
        row:SetPoint("TOPLEFT", scrollChild, "TOPLEFT", 0, -((i - 1) * ROW_HEIGHT))
        self.rows[i] = row
    end

    -- Last updated text
    local lastUpdated = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    lastUpdated:SetPoint("BOTTOMLEFT", frame, "BOTTOMLEFT", 15, 15)
    lastUpdated:SetText("Last updated: Never")
    lastUpdated:SetTextColor(0.5, 0.5, 0.5)
    frame.lastUpdated = lastUpdated

    frame:Hide()
    self.frame = frame
    self.scrollChild = scrollChild

    return frame
end

-- Create a single row
function Leaderboard:CreateRow(parent, index)
    local row = CreateFrame("Frame", nil, parent)
    row:SetSize(320, ROW_HEIGHT)

    -- Highlight on hover
    row:EnableMouse(true)
    row:SetScript("OnEnter", function(f)
        f.highlight:Show()
    end)
    row:SetScript("OnLeave", function(f)
        f.highlight:Hide()
    end)

    local highlight = row:CreateTexture(nil, "BACKGROUND")
    highlight:SetAllPoints()
    highlight:SetColorTexture(1, 1, 1, 0.1)
    highlight:Hide()
    row.highlight = highlight

    -- Rank
    local rank = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    rank:SetPoint("LEFT", row, "LEFT", 5, 0)
    rank:SetWidth(40)
    rank:SetJustifyH("LEFT")
    row.rank = rank

    -- Name
    local name = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    name:SetPoint("LEFT", rank, "RIGHT", 5, 0)
    name:SetWidth(120)
    name:SetJustifyH("LEFT")
    row.name = name

    -- Class
    local class = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    class:SetPoint("LEFT", name, "RIGHT", 5, 0)
    class:SetWidth(70)
    class:SetJustifyH("LEFT")
    row.class = class

    -- Score
    local score = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    score:SetPoint("LEFT", class, "RIGHT", 5, 0)
    score:SetWidth(60)
    score:SetJustifyH("RIGHT")
    row.score = score

    row:Hide()
    return row
end

-- Update leaderboard display
function Leaderboard:Update()
    if not self.frame then
        self:Create()
    end

    local data = ELM.Addon.db.global.flpsLeaderboard or {}

    -- Sort by score descending
    table.sort(data, function(a, b)
        return (a.score or 0) > (b.score or 0)
    end)

    -- Update rows
    for i, row in ipairs(self.rows) do
        local entry = data[i]
        if entry then
            row.rank:SetText("#" .. i)
            row.name:SetText(entry.name or "Unknown")

            -- Class coloring
            local classColor = RAID_CLASS_COLORS[entry.class]
            if classColor then
                row.class:SetText(entry.class)
                row.class:SetTextColor(classColor.r, classColor.g, classColor.b)
            else
                row.class:SetText(entry.class or "?")
                row.class:SetTextColor(1, 1, 1)
            end

            -- Score with FLPS coloring
            local scoreStr = ELM.Utils:FormatFLPS(entry.score or 0)
            row.score:SetText(ELM.Utils:ColorByFLPS(scoreStr, entry.score or 0))

            -- Highlight current player
            local playerName = ELM.Utils:GetCharacterName()
            if entry.name == playerName then
                row.rank:SetTextColor(0, 1, 0)
                row.name:SetTextColor(0, 1, 0)
            else
                row.rank:SetTextColor(1, 1, 1)
                row.name:SetTextColor(1, 1, 1)
            end

            row:Show()
        else
            row:Hide()
        end
    end

    -- Adjust scroll child height
    self.scrollChild:SetHeight(math.max(#data * ROW_HEIGHT, 1))

    -- Update timestamp
    local lastSync = ELM.Addon.db.global.lastLeaderboardSync
    if lastSync then
        self.frame.lastUpdated:SetText("Last updated: " .. ELM.Utils:FormatTimestamp(lastSync))
    end
end

-- Update a single player's data
function Leaderboard:UpdatePlayer(name, flpsData)
    local data = ELM.Addon.db.global.flpsLeaderboard or {}

    -- Find or create entry
    local found = false
    for _, entry in ipairs(data) do
        if entry.name == name then
            entry.score = flpsData.score
            entry.rms = flpsData.rms
            entry.ipi = flpsData.ipi
            entry.rdf = flpsData.rdf
            found = true
            break
        end
    end

    if not found then
        table.insert(data, {
            name = name,
            score = flpsData.score,
            rms = flpsData.rms,
            ipi = flpsData.ipi,
            rdf = flpsData.rdf,
        })
    end

    ELM.Addon.db.global.flpsLeaderboard = data

    if self.frame and self.frame:IsShown() then
        self:Update()
    end
end

-- Request leaderboard sync from raid
function Leaderboard:RequestSync()
    ELM.Utils:Print("Requesting FLPS data from raid members...")
    ELM.Comms:RequestLeaderboardSync()

    -- Also request from server if API is available
    -- (Would need desktop client sync)
end

-- Show the leaderboard
function Leaderboard:Show()
    if not self.frame then
        self:Create()
    end
    self:Update()
    self.frame:Show()
end

-- Hide the leaderboard
function Leaderboard:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Toggle the leaderboard
function Leaderboard:Toggle()
    if self.frame and self.frame:IsShown() then
        self:Hide()
    else
        self:Show()
    end
end
