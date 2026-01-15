-- EdgeRush LootMan Main Frame
-- Main addon window with tabbed interface

local ADDON_NAME, ELM = ...

ELM.MainFrame = {}

local MainFrame = ELM.MainFrame

local TABS = {
    { name = "FLPS", icon = "Interface\\Icons\\Achievement_Arena_2v2_7" },
    { name = "Wishlist", icon = "Interface\\Icons\\INV_Misc_Note_01" },
    { name = "Gear", icon = "Interface\\Icons\\INV_Chest_Plate16" },
    { name = "Leaderboard", icon = "Interface\\Icons\\Achievement_GuildPerk_Everyones A Hero" },
}

-- Create the main frame
function MainFrame:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushMainFrame", UIParent, "BackdropTemplate")
    frame:SetSize(450, 400)
    frame:SetPoint("CENTER")
    frame:SetMovable(true)
    frame:EnableMouse(true)
    frame:RegisterForDrag("LeftButton")
    frame:SetScript("OnDragStart", frame.StartMoving)
    frame:SetScript("OnDragStop", frame.StopMovingOrSizing)
    frame:SetFrameStrata("HIGH")
    frame:SetClampedToScreen(true)

    frame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background-Dark",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Gold-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 32,
        insets = { left = 8, right = 8, top = 8, bottom = 8 }
    })
    frame:SetBackdropColor(0.08, 0.08, 0.12, 0.95)

    -- Header
    local header = CreateFrame("Frame", nil, frame)
    header:SetSize(430, 40)
    header:SetPoint("TOP", frame, "TOP", 0, -10)

    -- Logo/Title
    local logo = header:CreateTexture(nil, "ARTWORK")
    logo:SetSize(32, 32)
    logo:SetPoint("LEFT", header, "LEFT", 5, 0)
    logo:SetTexture("Interface\\Icons\\Achievement_Arena_2v2_7")

    local title = header:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("LEFT", logo, "RIGHT", 10, 0)
    title:SetText("EdgeRush LootMan")
    title:SetTextColor(1, 0.8, 0)

    local version = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    version:SetPoint("LEFT", title, "RIGHT", 5, -2)
    version:SetText("v" .. ELM.VERSION)
    version:SetTextColor(0.5, 0.5, 0.5)

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)

    -- Tab bar
    local tabBar = CreateFrame("Frame", nil, frame)
    tabBar:SetSize(430, 30)
    tabBar:SetPoint("TOP", header, "BOTTOM", 0, -5)

    self.tabs = {}
    local prevTab = nil
    for i, tabInfo in ipairs(TABS) do
        local tab = self:CreateTab(tabBar, tabInfo, i)
        if prevTab then
            tab:SetPoint("LEFT", prevTab, "RIGHT", 5, 0)
        else
            tab:SetPoint("LEFT", tabBar, "LEFT", 5, 0)
        end
        self.tabs[i] = tab
        prevTab = tab
    end

    -- Content area
    local content = CreateFrame("Frame", nil, frame, "BackdropTemplate")
    content:SetPoint("TOPLEFT", tabBar, "BOTTOMLEFT", 0, -5)
    content:SetPoint("BOTTOMRIGHT", frame, "BOTTOMRIGHT", -15, 15)
    content:SetBackdrop({
        bgFile = "Interface\\Buttons\\WHITE8x8",
        edgeFile = "Interface\\Buttons\\WHITE8x8",
        edgeSize = 1,
    })
    content:SetBackdropColor(0, 0, 0, 0.3)
    content:SetBackdropBorderColor(0.3, 0.3, 0.3)
    self.content = content

    -- Create content panels
    self.panels = {}
    self.panels[1] = self:CreateFLPSPanel(content)
    self.panels[2] = self:CreateWishlistPanel(content)
    self.panels[3] = self:CreateGearPanel(content)
    self.panels[4] = self:CreateLeaderboardPanel(content)

    -- Select first tab
    self:SelectTab(1)

    frame:Hide()
    self.frame = frame

    return frame
end

-- Create a tab button
function MainFrame:CreateTab(parent, tabInfo, index)
    local tab = CreateFrame("Button", nil, parent)
    tab:SetSize(100, 28)

    local bg = tab:CreateTexture(nil, "BACKGROUND")
    bg:SetAllPoints()
    bg:SetColorTexture(0.2, 0.2, 0.2, 0.5)
    tab.bg = bg

    local icon = tab:CreateTexture(nil, "ARTWORK")
    icon:SetSize(20, 20)
    icon:SetPoint("LEFT", tab, "LEFT", 5, 0)
    icon:SetTexture(tabInfo.icon)

    local text = tab:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    text:SetPoint("LEFT", icon, "RIGHT", 5, 0)
    text:SetText(tabInfo.name)
    tab.text = text

    tab.index = index
    tab:SetScript("OnClick", function()
        self:SelectTab(index)
    end)

    tab:SetScript("OnEnter", function(t)
        if self.selectedTab ~= index then
            t.bg:SetColorTexture(0.3, 0.3, 0.3, 0.5)
        end
    end)

    tab:SetScript("OnLeave", function(t)
        if self.selectedTab ~= index then
            t.bg:SetColorTexture(0.2, 0.2, 0.2, 0.5)
        end
    end)

    return tab
end

-- Select a tab
function MainFrame:SelectTab(index)
    self.selectedTab = index

    -- Update tab appearances
    for i, tab in ipairs(self.tabs) do
        if i == index then
            tab.bg:SetColorTexture(0.4, 0.3, 0.1, 0.8)
            tab.text:SetTextColor(1, 0.8, 0)
        else
            tab.bg:SetColorTexture(0.2, 0.2, 0.2, 0.5)
            tab.text:SetTextColor(1, 1, 1)
        end
    end

    -- Show/hide panels
    for i, panel in ipairs(self.panels) do
        if i == index then
            panel:Show()
            if panel.OnShow then panel:OnShow() end
        else
            panel:Hide()
        end
    end
end

-- Create FLPS panel
function MainFrame:CreateFLPSPanel(parent)
    local panel = CreateFrame("Frame", nil, parent)
    panel:SetAllPoints()

    -- Score display
    local scoreLabel = panel:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    scoreLabel:SetPoint("TOP", panel, "TOP", 0, -20)
    scoreLabel:SetText("Your FLPS Score")
    scoreLabel:SetTextColor(0.7, 0.7, 0.7)

    local scoreValue = panel:CreateFontString(nil, "OVERLAY", "QuestFont_Enormous")
    scoreValue:SetPoint("TOP", scoreLabel, "BOTTOM", 0, -5)
    scoreValue:SetText("0.0%")
    panel.scoreValue = scoreValue

    -- Breakdown
    local breakdown = CreateFrame("Frame", nil, panel, "BackdropTemplate")
    breakdown:SetSize(300, 100)
    breakdown:SetPoint("TOP", scoreValue, "BOTTOM", 0, -20)
    breakdown:SetBackdrop({
        bgFile = "Interface\\Buttons\\WHITE8x8",
        edgeFile = "Interface\\Buttons\\WHITE8x8",
        edgeSize = 1,
    })
    breakdown:SetBackdropColor(0, 0, 0, 0.3)
    breakdown:SetBackdropBorderColor(0.3, 0.3, 0.3)

    local rmsLabel = breakdown:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    rmsLabel:SetPoint("TOPLEFT", breakdown, "TOPLEFT", 20, -15)
    rmsLabel:SetText("RMS (Raider Merit):")

    local rmsValue = breakdown:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    rmsValue:SetPoint("TOPRIGHT", breakdown, "TOPRIGHT", -20, -15)
    rmsValue:SetText("0.0%")
    panel.rmsValue = rmsValue

    local ipiLabel = breakdown:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    ipiLabel:SetPoint("TOPLEFT", rmsLabel, "BOTTOMLEFT", 0, -10)
    ipiLabel:SetText("IPI (Item Priority):")

    local ipiValue = breakdown:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    ipiValue:SetPoint("TOPRIGHT", rmsValue, "BOTTOMRIGHT", 0, -10)
    ipiValue:SetText("0.0%")
    panel.ipiValue = ipiValue

    local rdfLabel = breakdown:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    rdfLabel:SetPoint("TOPLEFT", ipiLabel, "BOTTOMLEFT", 0, -10)
    rdfLabel:SetText("RDF (Recency Decay):")

    local rdfValue = breakdown:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    rdfValue:SetPoint("TOPRIGHT", ipiValue, "BOTTOMRIGHT", 0, -10)
    rdfValue:SetText("0.0%")
    panel.rdfValue = rdfValue

    -- Rank
    local rankText = panel:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    rankText:SetPoint("TOP", breakdown, "BOTTOM", 0, -20)
    rankText:SetText("Guild Rank: #0")
    panel.rankText = rankText

    -- Last updated
    local lastUpdated = panel:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    lastUpdated:SetPoint("BOTTOM", panel, "BOTTOM", 0, 20)
    lastUpdated:SetTextColor(0.5, 0.5, 0.5)
    lastUpdated:SetText("Last updated: Never")
    panel.lastUpdated = lastUpdated

    function panel:OnShow()
        local flps = ELM.Addon.db.char.flps
        self.scoreValue:SetText(ELM.Utils:ColorByFLPS(ELM.Utils:FormatFLPS(flps.score), flps.score))
        self.rmsValue:SetText(string.format("%.1f%%", flps.rms * 100))
        self.ipiValue:SetText(string.format("%.1f%%", flps.ipi * 100))
        self.rdfValue:SetText(string.format("%.1f%%", flps.rdf * 100))
        self.rankText:SetText("Guild Rank: #" .. flps.rank)

        if flps.lastUpdated then
            self.lastUpdated:SetText("Last updated: " .. ELM.Utils:FormatTimestamp(flps.lastUpdated))
        end
    end

    panel:Hide()
    return panel
end

-- Create Wishlist panel
function MainFrame:CreateWishlistPanel(parent)
    local panel = CreateFrame("Frame", nil, parent)
    panel:SetAllPoints()

    local text = panel:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    text:SetPoint("CENTER")
    text:SetText("Wishlist loaded from EdgeRush server.\nUse desktop client to sync.")

    local openBtn = CreateFrame("Button", nil, panel, "UIPanelButtonTemplate")
    openBtn:SetSize(120, 25)
    openBtn:SetPoint("TOP", text, "BOTTOM", 0, -20)
    openBtn:SetText("Open Wishlist")
    openBtn:SetScript("OnClick", function()
        if ELM.Wishlist then
            ELM.Wishlist:Toggle()
        end
    end)

    panel:Hide()
    return panel
end

-- Create Gear panel
function MainFrame:CreateGearPanel(parent)
    local panel = CreateFrame("Frame", nil, parent)
    panel:SetAllPoints()

    local exportBtn = CreateFrame("Button", nil, panel, "UIPanelButtonTemplate")
    exportBtn:SetSize(120, 25)
    exportBtn:SetPoint("TOP", panel, "TOP", 0, -30)
    exportBtn:SetText("Export Gear")
    exportBtn:SetScript("OnClick", function()
        if ELM.GearExport then
            ELM.GearExport:Export()
            ELM.Utils:Print("Gear exported to SavedVariables")
        end
    end)

    local reportBtn = CreateFrame("Button", nil, panel, "UIPanelButtonTemplate")
    reportBtn:SetSize(120, 25)
    reportBtn:SetPoint("TOP", exportBtn, "BOTTOM", 0, -10)
    reportBtn:SetText("Gear Report")
    reportBtn:SetScript("OnClick", function()
        if ELM.GearExport then
            ELM.GearExport:PrintReport()
        end
    end)

    local bagBtn = CreateFrame("Button", nil, panel, "UIPanelButtonTemplate")
    bagBtn:SetSize(120, 25)
    bagBtn:SetPoint("TOP", reportBtn, "BOTTOM", 0, -10)
    bagBtn:SetText("Find Upgrades")
    bagBtn:SetScript("OnClick", function()
        if ELM.BagExport then
            ELM.BagExport:PrintUpgrades()
        end
    end)

    panel:Hide()
    return panel
end

-- Create Leaderboard panel
function MainFrame:CreateLeaderboardPanel(parent)
    local panel = CreateFrame("Frame", nil, parent)
    panel:SetAllPoints()

    local text = panel:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    text:SetPoint("CENTER")
    text:SetText("Guild FLPS Leaderboard")

    local openBtn = CreateFrame("Button", nil, panel, "UIPanelButtonTemplate")
    openBtn:SetSize(120, 25)
    openBtn:SetPoint("TOP", text, "BOTTOM", 0, -20)
    openBtn:SetText("Open Leaderboard")
    openBtn:SetScript("OnClick", function()
        if ELM.Leaderboard then
            ELM.Leaderboard:Toggle()
        end
    end)

    panel:Hide()
    return panel
end

-- Show the main frame
function MainFrame:Show()
    if not self.frame then
        self:Create()
    end
    self.frame:Show()
end

-- Hide the main frame
function MainFrame:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Toggle the main frame
function MainFrame:Toggle()
    if self.frame and self.frame:IsShown() then
        self:Hide()
    else
        self:Show()
    end
end
