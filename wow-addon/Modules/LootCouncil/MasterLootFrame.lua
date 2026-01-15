-- EdgeRush LootMan Master Loot Frame
-- Full-featured loot council UI for master looter

local ADDON_NAME, ELM = ...

ELM.MasterLootFrame = {}

local MasterLootFrame = ELM.MasterLootFrame

local ITEM_HEIGHT = 50
local CANDIDATE_HEIGHT = 28
local MAX_ITEMS = 10
local MAX_CANDIDATES = 25

-- Create the master loot frame
function MasterLootFrame:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushMasterLootFrame", UIParent, "BackdropTemplate")
    frame:SetSize(700, 500)
    frame:SetPoint("CENTER")
    frame:SetMovable(true)
    frame:EnableMouse(true)
    frame:RegisterForDrag("LeftButton")
    frame:SetScript("OnDragStart", frame.StartMoving)
    frame:SetScript("OnDragStop", frame.StopMovingOrSizing)
    frame:SetFrameStrata("DIALOG")
    frame:SetToplevel(true)
    frame:SetClampedToScreen(true)

    frame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background-Dark",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Gold-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 32,
        insets = { left = 8, right = 8, top = 8, bottom = 8 }
    })
    frame:SetBackdropColor(0.08, 0.08, 0.12, 0.98)

    -- Title bar
    local titleBar = CreateFrame("Frame", nil, frame)
    titleBar:SetSize(680, 30)
    titleBar:SetPoint("TOP", frame, "TOP", 0, -10)

    local title = titleBar:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("LEFT", titleBar, "LEFT", 5, 0)
    title:SetText("EdgeRush Loot Council")
    title:SetTextColor(1, 0.8, 0)

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)
    closeBtn:SetScript("OnClick", function()
        if ELM.SessionManager and ELM.SessionManager.currentSession then
            StaticPopupDialogs["ELM_CLOSE_SESSION"] = {
                text = "A loot session is active. Do you want to end it?",
                button1 = "End Session",
                button2 = "Cancel",
                OnAccept = function()
                    ELM.SessionManager:EndSession()
                end,
                timeout = 0,
                whileDead = true,
                hideOnEscape = true,
            }
            StaticPopup_Show("ELM_CLOSE_SESSION")
        else
            self:Hide()
        end
    end)

    -- Session controls
    local controlsFrame = CreateFrame("Frame", nil, frame)
    controlsFrame:SetSize(680, 30)
    controlsFrame:SetPoint("TOP", titleBar, "BOTTOM", 0, -5)

    local endSessionBtn = CreateFrame("Button", nil, controlsFrame, "UIPanelButtonTemplate")
    endSessionBtn:SetSize(100, 22)
    endSessionBtn:SetPoint("RIGHT", controlsFrame, "RIGHT", -5, 0)
    endSessionBtn:SetText("End Session")
    endSessionBtn:SetScript("OnClick", function()
        if ELM.SessionManager then
            ELM.SessionManager:EndSession()
        end
    end)

    local addItemBtn = CreateFrame("Button", nil, controlsFrame, "UIPanelButtonTemplate")
    addItemBtn:SetSize(80, 22)
    addItemBtn:SetPoint("RIGHT", endSessionBtn, "LEFT", -5, 0)
    addItemBtn:SetText("Add Item")
    addItemBtn:SetScript("OnClick", function()
        self:ShowAddItemDialog()
    end)

    -- Split into left (items) and right (candidates) panels
    local leftPanel = CreateFrame("Frame", nil, frame, "BackdropTemplate")
    leftPanel:SetSize(200, 380)
    leftPanel:SetPoint("TOPLEFT", controlsFrame, "BOTTOMLEFT", 5, -5)
    leftPanel:SetBackdrop({
        bgFile = "Interface\\Buttons\\WHITE8x8",
        edgeFile = "Interface\\Buttons\\WHITE8x8",
        edgeSize = 1,
    })
    leftPanel:SetBackdropColor(0, 0, 0, 0.3)
    leftPanel:SetBackdropBorderColor(0.3, 0.3, 0.3)
    self.leftPanel = leftPanel

    local itemsTitle = leftPanel:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    itemsTitle:SetPoint("TOP", leftPanel, "TOP", 0, -5)
    itemsTitle:SetText("Items")

    -- Items scroll frame
    local itemsScroll = CreateFrame("ScrollFrame", "ELMItemsScroll", leftPanel, "UIPanelScrollFrameTemplate")
    itemsScroll:SetPoint("TOPLEFT", itemsTitle, "BOTTOMLEFT", 5, -5)
    itemsScroll:SetPoint("BOTTOMRIGHT", leftPanel, "BOTTOMRIGHT", -25, 5)

    local itemsChild = CreateFrame("Frame")
    itemsScroll:SetScrollChild(itemsChild)
    itemsChild:SetWidth(170)
    itemsChild:SetHeight(1)
    self.itemsChild = itemsChild

    -- Create item rows
    self.itemRows = {}
    for i = 1, MAX_ITEMS do
        local row = self:CreateItemRow(itemsChild, i)
        row:SetPoint("TOPLEFT", itemsChild, "TOPLEFT", 0, -((i - 1) * ITEM_HEIGHT))
        self.itemRows[i] = row
    end

    -- Right panel (candidates)
    local rightPanel = CreateFrame("Frame", nil, frame, "BackdropTemplate")
    rightPanel:SetSize(470, 380)
    rightPanel:SetPoint("TOPLEFT", leftPanel, "TOPRIGHT", 5, 0)
    rightPanel:SetBackdrop({
        bgFile = "Interface\\Buttons\\WHITE8x8",
        edgeFile = "Interface\\Buttons\\WHITE8x8",
        edgeSize = 1,
    })
    rightPanel:SetBackdropColor(0, 0, 0, 0.3)
    rightPanel:SetBackdropBorderColor(0.3, 0.3, 0.3)
    self.rightPanel = rightPanel

    -- Candidates header
    local candidatesHeader = CreateFrame("Frame", nil, rightPanel)
    candidatesHeader:SetSize(450, 20)
    candidatesHeader:SetPoint("TOP", rightPanel, "TOP", 0, -5)

    local headers = {
        { text = "Name", width = 100, offset = 5 },
        { text = "Response", width = 80, offset = 110 },
        { text = "FLPS", width = 50, offset = 195 },
        { text = "iLvl", width = 40, offset = 250 },
        { text = "Votes", width = 50, offset = 295 },
        { text = "Action", width = 100, offset = 350 },
    }

    for _, h in ipairs(headers) do
        local text = candidatesHeader:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
        text:SetPoint("LEFT", candidatesHeader, "LEFT", h.offset, 0)
        text:SetText(h.text)
        text:SetWidth(h.width)
    end

    -- Candidates scroll frame
    local candidatesScroll = CreateFrame("ScrollFrame", "ELMCandidatesScroll", rightPanel, "UIPanelScrollFrameTemplate")
    candidatesScroll:SetPoint("TOPLEFT", candidatesHeader, "BOTTOMLEFT", 0, -5)
    candidatesScroll:SetPoint("BOTTOMRIGHT", rightPanel, "BOTTOMRIGHT", -25, 5)

    local candidatesChild = CreateFrame("Frame")
    candidatesScroll:SetScrollChild(candidatesChild)
    candidatesChild:SetWidth(430)
    candidatesChild:SetHeight(1)
    self.candidatesChild = candidatesChild

    -- Create candidate rows
    self.candidateRows = {}
    for i = 1, MAX_CANDIDATES do
        local row = self:CreateCandidateRow(candidatesChild, i)
        row:SetPoint("TOPLEFT", candidatesChild, "TOPLEFT", 0, -((i - 1) * CANDIDATE_HEIGHT))
        self.candidateRows[i] = row
    end

    -- Bottom status bar
    local statusBar = CreateFrame("Frame", nil, frame)
    statusBar:SetSize(680, 25)
    statusBar:SetPoint("BOTTOM", frame, "BOTTOM", 0, 10)

    local statusText = statusBar:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    statusText:SetPoint("LEFT", statusBar, "LEFT", 10, 0)
    statusText:SetTextColor(0.7, 0.7, 0.7)
    self.statusText = statusText

    frame:Hide()
    self.frame = frame
    self.selectedItemIndex = 1

    return frame
end

-- Create item row
function MasterLootFrame:CreateItemRow(parent, index)
    local row = CreateFrame("Button", nil, parent)
    row:SetSize(170, ITEM_HEIGHT - 2)
    row.index = index

    -- Selection highlight
    local highlight = row:CreateTexture(nil, "BACKGROUND")
    highlight:SetAllPoints()
    highlight:SetColorTexture(1, 0.8, 0, 0.2)
    highlight:Hide()
    row.highlight = highlight

    -- Selected indicator
    local selected = row:CreateTexture(nil, "BACKGROUND")
    selected:SetAllPoints()
    selected:SetColorTexture(0.3, 0.3, 0.1, 0.5)
    selected:Hide()
    row.selected = selected

    -- Icon
    local icon = row:CreateTexture(nil, "ARTWORK")
    icon:SetSize(40, 40)
    icon:SetPoint("LEFT", row, "LEFT", 5, 0)
    row.icon = icon

    -- Item name
    local name = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    name:SetPoint("TOPLEFT", icon, "TOPRIGHT", 5, -2)
    name:SetWidth(115)
    name:SetJustifyH("LEFT")
    row.name = name

    -- Item level
    local ilvl = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    ilvl:SetPoint("TOPLEFT", name, "BOTTOMLEFT", 0, -2)
    ilvl:SetTextColor(0.7, 0.7, 0.7)
    row.ilvl = ilvl

    -- Awarded indicator
    local awarded = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    awarded:SetPoint("TOPLEFT", ilvl, "BOTTOMLEFT", 0, -2)
    awarded:SetTextColor(0, 1, 0)
    row.awarded = awarded

    -- Click handler
    row:SetScript("OnClick", function(btn)
        self:SelectItem(btn.index)
    end)

    row:SetScript("OnEnter", function(btn)
        btn.highlight:Show()
        if btn.itemLink then
            GameTooltip:SetOwner(btn, "ANCHOR_RIGHT")
            GameTooltip:SetHyperlink(btn.itemLink)
            GameTooltip:Show()
        end
    end)

    row:SetScript("OnLeave", function(btn)
        btn.highlight:Hide()
        GameTooltip:Hide()
    end)

    row:Hide()
    return row
end

-- Create candidate row
function MasterLootFrame:CreateCandidateRow(parent, index)
    local row = CreateFrame("Frame", nil, parent)
    row:SetSize(430, CANDIDATE_HEIGHT - 2)
    row.index = index

    -- Alternating background
    if index % 2 == 0 then
        local bg = row:CreateTexture(nil, "BACKGROUND")
        bg:SetAllPoints()
        bg:SetColorTexture(1, 1, 1, 0.02)
    end

    -- Hover highlight
    row:EnableMouse(true)
    local highlight = row:CreateTexture(nil, "BACKGROUND")
    highlight:SetAllPoints()
    highlight:SetColorTexture(1, 1, 1, 0.05)
    highlight:Hide()
    row.highlight = highlight

    row:SetScript("OnEnter", function(r) r.highlight:Show() end)
    row:SetScript("OnLeave", function(r) r.highlight:Hide() end)

    -- Name (with class color)
    local name = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    name:SetPoint("LEFT", row, "LEFT", 5, 0)
    name:SetWidth(100)
    name:SetJustifyH("LEFT")
    row.name = name

    -- Response
    local response = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    response:SetPoint("LEFT", row, "LEFT", 110, 0)
    response:SetWidth(80)
    response:SetJustifyH("LEFT")
    row.response = response

    -- FLPS
    local flps = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    flps:SetPoint("LEFT", row, "LEFT", 195, 0)
    flps:SetWidth(50)
    flps:SetJustifyH("RIGHT")
    row.flps = flps

    -- Current iLvl
    local ilvl = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    ilvl:SetPoint("LEFT", row, "LEFT", 250, 0)
    ilvl:SetWidth(40)
    ilvl:SetJustifyH("RIGHT")
    row.ilvl = ilvl

    -- Votes
    local votes = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    votes:SetPoint("LEFT", row, "LEFT", 295, 0)
    votes:SetWidth(50)
    votes:SetJustifyH("CENTER")
    row.votes = votes

    -- Award button
    local awardBtn = CreateFrame("Button", nil, row, "UIPanelButtonTemplate")
    awardBtn:SetSize(55, 20)
    awardBtn:SetPoint("LEFT", row, "LEFT", 350, 0)
    awardBtn:SetText("Award")
    awardBtn.candidateIndex = index
    awardBtn:SetScript("OnClick", function(btn)
        self:AwardToCandidate(btn.candidateIndex)
    end)
    row.awardBtn = awardBtn

    -- Vote button (for council)
    local voteBtn = CreateFrame("Button", nil, row, "UIPanelButtonTemplate")
    voteBtn:SetSize(40, 20)
    voteBtn:SetPoint("LEFT", awardBtn, "RIGHT", 2, 0)
    voteBtn:SetText("Vote")
    voteBtn.candidateIndex = index
    voteBtn:SetScript("OnClick", function(btn)
        self:ShowVoteMenu(btn.candidateIndex)
    end)
    row.voteBtn = voteBtn

    row:Hide()
    return row
end

-- Set session data
function MasterLootFrame:SetSession(session)
    self.session = session
    self:UpdateItems()
    self:SelectItem(1)
end

-- Update items list
function MasterLootFrame:UpdateItems()
    if not self.session then return end

    for i, row in ipairs(self.itemRows) do
        local item = self.session.items[i]
        if item then
            row.icon:SetTexture(item.icon)
            row.name:SetText(ELM.Utils:ColorByQuality(item.name or "Unknown", item.quality or 4))
            row.ilvl:SetText("iLvl " .. (item.itemLevel or 0))
            row.itemLink = item.link

            if item.awarded then
                row.awarded:SetText("-> " .. item.awardedTo)
            else
                row.awarded:SetText("")
            end

            row:Show()
        else
            row:Hide()
        end
    end

    -- Update scroll height
    self.itemsChild:SetHeight(math.max(#self.session.items * ITEM_HEIGHT, 1))

    -- Update selection highlight
    for i, row in ipairs(self.itemRows) do
        if i == self.selectedItemIndex then
            row.selected:Show()
        else
            row.selected:Hide()
        end
    end
end

-- Select an item
function MasterLootFrame:SelectItem(index)
    self.selectedItemIndex = index
    self:UpdateItems()
    self:UpdateCandidates()
end

-- Update candidates for selected item
function MasterLootFrame:UpdateCandidates()
    if not self.session then return end

    local responses = self.session.responses[self.selectedItemIndex] or {}
    local votes = self.session.votes[self.selectedItemIndex] or {}

    -- Build sorted candidate list
    local candidates = {}
    for _, candidate in ipairs(self.session.candidates) do
        local response = responses[candidate.name]
        local candidateVotes = votes[candidate.name] or {}
        local voteCount = 0
        for _ in pairs(candidateVotes) do
            voteCount = voteCount + 1
        end

        table.insert(candidates, {
            name = candidate.name,
            class = candidate.class,
            flps = candidate.flps,
            response = response and response.response or nil,
            currentGear = response and response.currentGear or nil,
            voteCount = voteCount,
        })
    end

    -- Sort: responses first, then by FLPS
    table.sort(candidates, function(a, b)
        -- Non-pass responses first
        local aHasResponse = a.response and a.response ~= "PASS" and a.response ~= "AUTOPASS"
        local bHasResponse = b.response and b.response ~= "PASS" and b.response ~= "AUTOPASS"

        if aHasResponse and not bHasResponse then return true end
        if bHasResponse and not aHasResponse then return false end

        -- Then by votes
        if a.voteCount ~= b.voteCount then
            return a.voteCount > b.voteCount
        end

        -- Then by FLPS
        return a.flps > b.flps
    end)

    -- Update rows
    self.displayedCandidates = candidates
    for i, row in ipairs(self.candidateRows) do
        local candidate = candidates[i]
        if candidate then
            -- Name with class color
            local classColor = RAID_CLASS_COLORS[candidate.class]
            if classColor then
                row.name:SetText(candidate.name)
                row.name:SetTextColor(classColor.r, classColor.g, classColor.b)
            else
                row.name:SetText(candidate.name)
                row.name:SetTextColor(1, 1, 1)
            end

            -- Response
            if candidate.response then
                local color = ELM.SessionManager:GetResponseColor(candidate.response)
                row.response:SetText(ELM.SessionManager:GetResponseText(candidate.response))
                row.response:SetTextColor(color.r, color.g, color.b)
            else
                row.response:SetText("-")
                row.response:SetTextColor(0.5, 0.5, 0.5)
            end

            -- FLPS
            row.flps:SetText(ELM.Utils:ColorByFLPS(ELM.Utils:FormatFLPS(candidate.flps), candidate.flps))

            -- Current iLvl
            if candidate.currentGear then
                row.ilvl:SetText(tostring(candidate.currentGear.itemLevel or "?"))
            else
                row.ilvl:SetText("?")
            end

            -- Votes
            row.votes:SetText(candidate.voteCount > 0 and tostring(candidate.voteCount) or "-")

            -- Show/hide buttons based on permissions
            local isML = ELM.SessionManager:IsMasterLooter()
            local isCouncil = ELM.SessionManager:IsOnCouncil()
            row.awardBtn:SetShown(isML)
            row.voteBtn:SetShown(isCouncil and not isML)

            row:Show()
        else
            row:Hide()
        end
    end

    -- Update scroll height
    self.candidatesChild:SetHeight(math.max(#candidates * CANDIDATE_HEIGHT, 1))

    -- Update status
    local responseCount = 0
    for _ in pairs(responses) do
        responseCount = responseCount + 1
    end
    self.statusText:SetText(string.format("Responses: %d/%d", responseCount, #self.session.candidates))
end

-- Update responses (called when new response received)
function MasterLootFrame:UpdateResponses(itemIndex)
    if itemIndex == self.selectedItemIndex then
        self:UpdateCandidates()
    end
end

-- Update votes
function MasterLootFrame:UpdateVotes(itemIndex)
    if itemIndex == self.selectedItemIndex then
        self:UpdateCandidates()
    end
end

-- Update single item
function MasterLootFrame:UpdateItem(itemIndex)
    self:UpdateItems()
    if itemIndex == self.selectedItemIndex then
        self:UpdateCandidates()
    end
end

-- Award to candidate
function MasterLootFrame:AwardToCandidate(candidateIndex)
    local candidate = self.displayedCandidates[candidateIndex]
    if not candidate then return end

    local item = self.session.items[self.selectedItemIndex]
    if not item or item.awarded then return end

    -- Confirmation
    StaticPopupDialogs["ELM_AWARD_CONFIRM"] = {
        text = string.format("Award %s to %s?", item.link, candidate.name),
        button1 = "Award",
        button2 = "Cancel",
        OnAccept = function()
            local reason = candidate.response or "Council Decision"
            ELM.SessionManager:AwardItem(self.selectedItemIndex, candidate.name, reason)
        end,
        timeout = 0,
        whileDead = true,
        hideOnEscape = true,
    }
    StaticPopup_Show("ELM_AWARD_CONFIRM")
end

-- Show vote menu
function MasterLootFrame:ShowVoteMenu(candidateIndex)
    local candidate = self.displayedCandidates[candidateIndex]
    if not candidate then return end

    local menuList = {
        { text = "Vote for " .. candidate.name, isTitle = true, notCheckable = true },
    }

    for _, response in ipairs(ELM.SessionManager.RESPONSES) do
        if response.id ~= "AUTOPASS" then
            table.insert(menuList, {
                text = response.text,
                notCheckable = true,
                func = function()
                    ELM.SessionManager:CastVote(self.selectedItemIndex, candidate.name, response.id)
                end,
            })
        end
    end

    local menuFrame = CreateFrame("Frame", "ELMVoteMenu", UIParent, "UIDropDownMenuTemplate")
    EasyMenu(menuList, menuFrame, "cursor", 0, 0, "MENU")
end

-- Show add item dialog
function MasterLootFrame:ShowAddItemDialog()
    StaticPopupDialogs["ELM_ADD_ITEM"] = {
        text = "Enter item link or shift-click an item:",
        button1 = "Add",
        button2 = "Cancel",
        hasEditBox = true,
        OnAccept = function(self)
            local itemLink = self.editBox:GetText()
            if itemLink and itemLink:match("|H") then
                -- Would add item to session
                ELM.Utils:Print("Adding item: " .. itemLink)
            end
        end,
        EditBoxOnEscapePressed = function(self)
            self:GetParent():Hide()
        end,
        timeout = 0,
        whileDead = true,
        hideOnEscape = true,
    }
    StaticPopup_Show("ELM_ADD_ITEM")
end

-- Show the frame
function MasterLootFrame:Show()
    if not self.frame then
        self:Create()
    end
    self.frame:Show()
end

-- Hide the frame
function MasterLootFrame:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Check if shown
function MasterLootFrame:IsShown()
    return self.frame and self.frame:IsShown()
end
