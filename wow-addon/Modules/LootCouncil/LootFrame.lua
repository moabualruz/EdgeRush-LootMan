-- EdgeRush LootMan Loot Council Frame
-- Main loot council interface for distributing loot

local ADDON_NAME, ELM = ...

ELM.LootFrame = {}

local LootFrame = ELM.LootFrame

local ROW_HEIGHT = 30
local MAX_CANDIDATES = 20

-- Create the loot frame
function LootFrame:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushLootFrame", UIParent, "BackdropTemplate")
    frame:SetSize(500, 450)
    frame:SetPoint("CENTER")
    frame:SetMovable(true)
    frame:EnableMouse(true)
    frame:RegisterForDrag("LeftButton")
    frame:SetScript("OnDragStart", frame.StartMoving)
    frame:SetScript("OnDragStop", frame.StopMovingOrSizing)
    frame:SetFrameStrata("DIALOG")
    frame:SetToplevel(true)

    frame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background-Dark",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Gold-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 32,
        insets = { left = 8, right = 8, top = 8, bottom = 8 }
    })
    frame:SetBackdropColor(0.1, 0.1, 0.15, 0.95)

    -- Title
    local title = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("TOP", frame, "TOP", 0, -15)
    title:SetText("Loot Council")
    title:SetTextColor(1, 0.8, 0)
    frame.title = title

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)

    -- Item display area
    local itemFrame = CreateFrame("Frame", nil, frame, "BackdropTemplate")
    itemFrame:SetSize(460, 60)
    itemFrame:SetPoint("TOP", title, "BOTTOM", 0, -10)
    itemFrame:SetBackdrop({
        bgFile = "Interface\\Buttons\\WHITE8x8",
        edgeFile = "Interface\\Buttons\\WHITE8x8",
        edgeSize = 1,
    })
    itemFrame:SetBackdropColor(0, 0, 0, 0.5)
    itemFrame:SetBackdropBorderColor(0.5, 0.5, 0.5)

    -- Item icon
    local itemIcon = itemFrame:CreateTexture(nil, "ARTWORK")
    itemIcon:SetSize(50, 50)
    itemIcon:SetPoint("LEFT", itemFrame, "LEFT", 5, 0)
    frame.itemIcon = itemIcon

    -- Item name
    local itemName = itemFrame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    itemName:SetPoint("TOPLEFT", itemIcon, "TOPRIGHT", 10, -5)
    itemName:SetText("No item selected")
    frame.itemName = itemName

    -- Item level
    local itemLevel = itemFrame:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    itemLevel:SetPoint("TOPLEFT", itemName, "BOTTOMLEFT", 0, -5)
    itemLevel:SetText("Item Level: 0")
    frame.itemLevel = itemLevel

    -- Item slot
    local itemSlot = itemFrame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    itemSlot:SetPoint("TOPLEFT", itemLevel, "BOTTOMLEFT", 0, -3)
    itemSlot:SetTextColor(0.7, 0.7, 0.7)
    frame.itemSlot = itemSlot

    -- Header row
    local header = CreateFrame("Frame", nil, frame)
    header:SetSize(460, 20)
    header:SetPoint("TOP", itemFrame, "BOTTOM", 0, -10)

    local rankHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    rankHeader:SetPoint("LEFT", header, "LEFT", 5, 0)
    rankHeader:SetWidth(30)
    rankHeader:SetText("#")

    local nameHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    nameHeader:SetPoint("LEFT", rankHeader, "RIGHT", 5, 0)
    nameHeader:SetWidth(100)
    nameHeader:SetText("Name")

    local classHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    classHeader:SetPoint("LEFT", nameHeader, "RIGHT", 5, 0)
    classHeader:SetWidth(60)
    classHeader:SetText("Class")

    local flpsHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    flpsHeader:SetPoint("LEFT", classHeader, "RIGHT", 5, 0)
    flpsHeader:SetWidth(50)
    flpsHeader:SetText("FLPS")

    local upgradeHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    upgradeHeader:SetPoint("LEFT", flpsHeader, "RIGHT", 5, 0)
    upgradeHeader:SetWidth(50)
    upgradeHeader:SetText("+iLvl")

    local actionHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    actionHeader:SetPoint("LEFT", upgradeHeader, "RIGHT", 5, 0)
    actionHeader:SetWidth(100)
    actionHeader:SetText("Action")

    -- Scroll frame for candidates
    local scrollFrame = CreateFrame("ScrollFrame", "EdgeRushLootScrollFrame", frame, "UIPanelScrollFrameTemplate")
    scrollFrame:SetPoint("TOPLEFT", header, "BOTTOMLEFT", 0, -5)
    scrollFrame:SetPoint("BOTTOMRIGHT", frame, "BOTTOMRIGHT", -30, 50)

    local scrollChild = CreateFrame("Frame")
    scrollFrame:SetScrollChild(scrollChild)
    scrollChild:SetWidth(430)
    scrollChild:SetHeight(1)

    -- Create candidate rows
    self.candidateRows = {}
    for i = 1, MAX_CANDIDATES do
        local row = self:CreateCandidateRow(scrollChild, i)
        row:SetPoint("TOPLEFT", scrollChild, "TOPLEFT", 0, -((i - 1) * ROW_HEIGHT))
        self.candidateRows[i] = row
    end

    -- Bottom buttons
    local announceBtn = CreateFrame("Button", nil, frame, "UIPanelButtonTemplate")
    announceBtn:SetSize(100, 25)
    announceBtn:SetPoint("BOTTOMLEFT", frame, "BOTTOMLEFT", 15, 15)
    announceBtn:SetText("Announce")
    announceBtn:SetScript("OnClick", function()
        self:AnnounceItem()
    end)

    local passBtn = CreateFrame("Button", nil, frame, "UIPanelButtonTemplate")
    passBtn:SetSize(80, 25)
    passBtn:SetPoint("LEFT", announceBtn, "RIGHT", 10, 0)
    passBtn:SetText("Pass All")
    passBtn:SetScript("OnClick", function()
        self:PassAll()
    end)

    local refreshBtn = CreateFrame("Button", nil, frame, "UIPanelButtonTemplate")
    refreshBtn:SetSize(80, 25)
    refreshBtn:SetPoint("BOTTOMRIGHT", frame, "BOTTOMRIGHT", -15, 15)
    refreshBtn:SetText("Refresh")
    refreshBtn:SetScript("OnClick", function()
        self:RefreshCandidates()
    end)

    frame:Hide()
    self.frame = frame
    self.scrollChild = scrollChild

    return frame
end

-- Create a candidate row
function LootFrame:CreateCandidateRow(parent, index)
    local row = CreateFrame("Frame", nil, parent, "BackdropTemplate")
    row:SetSize(430, ROW_HEIGHT)

    -- Alternating background
    if index % 2 == 0 then
        row:SetBackdrop({
            bgFile = "Interface\\Buttons\\WHITE8x8",
        })
        row:SetBackdropColor(1, 1, 1, 0.03)
    end

    -- Rank
    local rank = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    rank:SetPoint("LEFT", row, "LEFT", 5, 0)
    rank:SetWidth(30)
    rank:SetJustifyH("CENTER")
    row.rank = rank

    -- Name
    local name = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    name:SetPoint("LEFT", rank, "RIGHT", 5, 0)
    name:SetWidth(100)
    name:SetJustifyH("LEFT")
    row.name = name

    -- Class
    local class = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    class:SetPoint("LEFT", name, "RIGHT", 5, 0)
    class:SetWidth(60)
    class:SetJustifyH("LEFT")
    row.class = class

    -- FLPS
    local flps = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    flps:SetPoint("LEFT", class, "RIGHT", 5, 0)
    flps:SetWidth(50)
    flps:SetJustifyH("RIGHT")
    row.flps = flps

    -- Upgrade value
    local upgrade = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    upgrade:SetPoint("LEFT", flps, "RIGHT", 5, 0)
    upgrade:SetWidth(50)
    upgrade:SetJustifyH("RIGHT")
    row.upgrade = upgrade

    -- Award button
    local awardBtn = CreateFrame("Button", nil, row, "UIPanelButtonTemplate")
    awardBtn:SetSize(60, 22)
    awardBtn:SetPoint("LEFT", upgrade, "RIGHT", 10, 0)
    awardBtn:SetText("Award")
    awardBtn.index = index
    awardBtn:SetScript("OnClick", function(btn)
        self:AwardToCandidate(btn.index)
    end)
    row.awardBtn = awardBtn

    row:Hide()
    return row
end

-- Set the item being distributed
function LootFrame:SetItem(itemLink)
    if not self.frame then
        self:Create()
    end

    self.currentItem = itemLink

    if itemLink then
        local itemInfo = ELM.Utils:GetItemInfo(itemLink)
        if itemInfo then
            self.frame.itemIcon:SetTexture(itemInfo.icon)
            self.frame.itemName:SetText(itemLink)
            self.frame.itemLevel:SetText("Item Level: " .. (itemInfo.itemLevel or 0))
            self.frame.itemSlot:SetText(itemInfo.equipLoc or "")
        end
    else
        self.frame.itemIcon:SetTexture(nil)
        self.frame.itemName:SetText("No item selected")
        self.frame.itemLevel:SetText("")
        self.frame.itemSlot:SetText("")
    end

    self:RefreshCandidates()
end

-- Refresh candidate list
function LootFrame:RefreshCandidates()
    if not self.currentItem or not IsInRaid() then
        self:ClearCandidates()
        return
    end

    -- Get raid roster
    local roster = ELM.Utils:GetRaidRoster()

    -- Build candidate list with FLPS data
    local candidates = {}
    for _, member in ipairs(roster) do
        if member.online then
            local flpsData = self:GetPlayerFLPS(member.name)
            local upgradeValue = self:CalculateUpgrade(member.name, self.currentItem)

            table.insert(candidates, {
                name = member.name,
                class = member.class,
                flps = flpsData and flpsData.score or 0,
                upgrade = upgradeValue,
            })
        end
    end

    -- Sort by FLPS (or upgrade value based on settings)
    if ELM.Addon.db.profile.lootCouncil.sortByFLPS then
        table.sort(candidates, function(a, b)
            return a.flps > b.flps
        end)
    else
        table.sort(candidates, function(a, b)
            return a.upgrade > b.upgrade
        end)
    end

    -- Update display
    self.candidates = candidates
    self:UpdateCandidateDisplay()
end

-- Update candidate row display
function LootFrame:UpdateCandidateDisplay()
    for i, row in ipairs(self.candidateRows) do
        local candidate = self.candidates and self.candidates[i]
        if candidate then
            row.rank:SetText("#" .. i)
            row.name:SetText(candidate.name)

            -- Class coloring
            local classColor = RAID_CLASS_COLORS[candidate.class]
            if classColor then
                row.class:SetText(candidate.class)
                row.class:SetTextColor(classColor.r, classColor.g, classColor.b)
            else
                row.class:SetText(candidate.class or "?")
            end

            -- FLPS with coloring
            local flpsStr = ELM.Utils:FormatFLPS(candidate.flps)
            row.flps:SetText(ELM.Utils:ColorByFLPS(flpsStr, candidate.flps))

            -- Upgrade value
            if candidate.upgrade > 0 then
                row.upgrade:SetText("|cff00ff00+" .. candidate.upgrade .. "|r")
            elseif candidate.upgrade < 0 then
                row.upgrade:SetText("|cffff0000" .. candidate.upgrade .. "|r")
            else
                row.upgrade:SetText("0")
            end

            row:Show()
        else
            row:Hide()
        end
    end

    -- Update scroll height
    local numCandidates = self.candidates and #self.candidates or 0
    self.scrollChild:SetHeight(math.max(numCandidates * ROW_HEIGHT, 1))
end

-- Clear candidates
function LootFrame:ClearCandidates()
    self.candidates = {}
    for _, row in ipairs(self.candidateRows) do
        row:Hide()
    end
end

-- Get FLPS for a player
function LootFrame:GetPlayerFLPS(name)
    local leaderboard = ELM.Addon.db.global.flpsLeaderboard
    if leaderboard then
        for _, entry in ipairs(leaderboard) do
            if entry.name == name then
                return entry
            end
        end
    end
    return nil
end

-- Calculate upgrade value for a player
function LootFrame:CalculateUpgrade(playerName, itemLink)
    -- This would need actual equipped item data from the player
    -- For now, return 0 as a placeholder
    local itemInfo = ELM.Utils:GetItemInfo(itemLink)
    return itemInfo and itemInfo.itemLevel or 0
end

-- Award item to candidate
function LootFrame:AwardToCandidate(index)
    local candidate = self.candidates and self.candidates[index]
    if not candidate then return end

    -- Confirm dialog
    StaticPopupDialogs["EDGERUSH_AWARD_CONFIRM"] = {
        text = "Award " .. (self.currentItem or "item") .. " to " .. candidate.name .. "?",
        button1 = "Award",
        button2 = "Cancel",
        OnAccept = function()
            ELM.Comms:AnnounceLootAward(self.currentItem, candidate.name, "FLPS #" .. index)
            self:Hide()
        end,
        timeout = 0,
        whileDead = true,
        hideOnEscape = true,
    }
    StaticPopup_Show("EDGERUSH_AWARD_CONFIRM")
end

-- Announce item to raid
function LootFrame:AnnounceItem()
    if not self.currentItem or not IsInRaid() then return end

    SendChatMessage(
        "[EdgeRush] Now distributing: " .. self.currentItem,
        "RAID"
    )
end

-- Pass all (close frame)
function LootFrame:PassAll()
    SendChatMessage(
        "[EdgeRush] " .. (self.currentItem or "Item") .. " - No distribution (passed)",
        "RAID"
    )
    self:Hide()
end

-- Process award from another player
function LootFrame:ProcessAward(sender, data)
    ELM.Utils:Print(data.item .. " awarded to " .. data.winner .. " by " .. sender)
end

-- Show the frame
function LootFrame:Show()
    if not self.frame then
        self:Create()
    end
    self.frame:Show()
end

-- Hide the frame
function LootFrame:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Toggle the frame
function LootFrame:Toggle()
    if self.frame and self.frame:IsShown() then
        self:Hide()
    else
        self:Show()
    end
end
