-- EdgeRush LootMan Response Frame
-- UI for raiders to submit their loot responses

local ADDON_NAME, ELM = ...

ELM.ResponseFrame = {}

local ResponseFrame = ELM.ResponseFrame

-- Create the response frame
function ResponseFrame:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushResponseFrame", UIParent, "BackdropTemplate")
    frame:SetSize(350, 300)
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
    frame:SetBackdropColor(0.1, 0.1, 0.15, 0.98)

    -- Title
    local title = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("TOP", frame, "TOP", 0, -15)
    title:SetText("Select Your Response")
    title:SetTextColor(1, 0.8, 0)

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)

    -- Item display
    local itemFrame = CreateFrame("Frame", nil, frame, "BackdropTemplate")
    itemFrame:SetSize(310, 60)
    itemFrame:SetPoint("TOP", title, "BOTTOM", 0, -10)
    itemFrame:SetBackdrop({
        bgFile = "Interface\\Buttons\\WHITE8x8",
        edgeFile = "Interface\\Buttons\\WHITE8x8",
        edgeSize = 1,
    })
    itemFrame:SetBackdropColor(0, 0, 0, 0.4)
    itemFrame:SetBackdropBorderColor(0.4, 0.4, 0.4)

    local itemIcon = itemFrame:CreateTexture(nil, "ARTWORK")
    itemIcon:SetSize(50, 50)
    itemIcon:SetPoint("LEFT", itemFrame, "LEFT", 5, 0)
    frame.itemIcon = itemIcon

    local itemName = itemFrame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    itemName:SetPoint("TOPLEFT", itemIcon, "TOPRIGHT", 10, -5)
    itemName:SetText("Item Name")
    frame.itemName = itemName

    local itemInfo = itemFrame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    itemInfo:SetPoint("TOPLEFT", itemName, "BOTTOMLEFT", 0, -5)
    itemInfo:SetText("Item Level 000")
    itemInfo:SetTextColor(0.7, 0.7, 0.7)
    frame.itemInfo = itemInfo

    -- Current gear comparison
    local comparisonFrame = CreateFrame("Frame", nil, frame)
    comparisonFrame:SetSize(310, 30)
    comparisonFrame:SetPoint("TOP", itemFrame, "BOTTOM", 0, -5)

    local currentGearLabel = comparisonFrame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    currentGearLabel:SetPoint("LEFT", comparisonFrame, "LEFT", 5, 0)
    currentGearLabel:SetText("Current:")
    currentGearLabel:SetTextColor(0.7, 0.7, 0.7)

    local currentGearText = comparisonFrame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    currentGearText:SetPoint("LEFT", currentGearLabel, "RIGHT", 5, 0)
    frame.currentGearText = currentGearText

    -- Response buttons container
    local buttonsContainer = CreateFrame("Frame", nil, frame)
    buttonsContainer:SetSize(310, 150)
    buttonsContainer:SetPoint("TOP", comparisonFrame, "BOTTOM", 0, -10)

    -- Create response buttons
    self.responseButtons = {}
    local buttonWidth = 145
    local buttonHeight = 30
    local padding = 5

    for i, response in ipairs(ELM.SessionManager.RESPONSES) do
        if response.id ~= "AUTOPASS" then
            local btn = CreateFrame("Button", nil, buttonsContainer, "UIPanelButtonTemplate")
            btn:SetSize(buttonWidth, buttonHeight)

            local row = math.floor((i - 1) / 2)
            local col = (i - 1) % 2

            btn:SetPoint("TOPLEFT", buttonsContainer, "TOPLEFT",
                col * (buttonWidth + padding),
                -row * (buttonHeight + padding))

            btn:SetText(response.text)
            btn.responseId = response.id
            btn.responseColor = response.color

            -- Color the button
            btn:SetScript("OnEnter", function(b)
                b:GetFontString():SetTextColor(b.responseColor.r, b.responseColor.g, b.responseColor.b)
            end)
            btn:SetScript("OnLeave", function(b)
                b:GetFontString():SetTextColor(1, 1, 1)
            end)

            btn:SetScript("OnClick", function(b)
                self:SubmitResponse(b.responseId)
            end)

            self.responseButtons[i] = btn
        end
    end

    -- Note input
    local noteLabel = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    noteLabel:SetPoint("BOTTOMLEFT", frame, "BOTTOMLEFT", 20, 55)
    noteLabel:SetText("Note (optional):")
    noteLabel:SetTextColor(0.7, 0.7, 0.7)

    local noteBox = CreateFrame("EditBox", nil, frame, "InputBoxTemplate")
    noteBox:SetSize(270, 20)
    noteBox:SetPoint("TOPLEFT", noteLabel, "BOTTOMLEFT", 5, -2)
    noteBox:SetAutoFocus(false)
    noteBox:SetMaxLetters(100)
    frame.noteBox = noteBox

    -- Time remaining
    local timerText = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    timerText:SetPoint("BOTTOM", frame, "BOTTOM", 0, 10)
    timerText:SetTextColor(1, 0.3, 0.3)
    frame.timerText = timerText

    frame:Hide()
    self.frame = frame

    return frame
end

-- Show for an item
function ResponseFrame:ShowForItem(sessionId, itemIndex, itemLink, timeout)
    if not self.frame then
        self:Create()
    end

    self.sessionId = sessionId
    self.itemIndex = itemIndex
    self.itemLink = itemLink

    -- Get item info
    local itemInfo = ELM.Utils:GetItemInfo(itemLink)
    if itemInfo then
        self.frame.itemIcon:SetTexture(itemInfo.icon)
        self.frame.itemName:SetText(itemLink)
        self.frame.itemInfo:SetText("Item Level " .. (itemInfo.itemLevel or 0))
    end

    -- Get current equipped gear for comparison
    local currentGear = ELM.SessionManager:GetCurrentGearForSlot(itemInfo and itemInfo.equipLoc)
    if currentGear then
        self.frame.currentGearText:SetText(currentGear.link .. " (iLvl " .. currentGear.itemLevel .. ")")
    else
        self.frame.currentGearText:SetText("Empty slot")
    end

    -- Check for auto-pass
    local playerClass = ELM.Utils:GetCharacterClass()
    if ELM.SessionManager:ShouldAutoPass(itemLink, playerClass) then
        -- Auto-pass and don't show frame
        ELM.SessionManager:SubmitResponse(itemIndex, "AUTOPASS", nil)
        ELM.Utils:Print("Auto-passed on " .. itemLink .. " (not usable)")
        return
    end

    -- Clear note
    self.frame.noteBox:SetText("")

    -- Setup timer
    if timeout and timeout > 0 then
        self.timeout = timeout
        self.startTime = GetTime()
        self:StartTimer()
    else
        self.frame.timerText:SetText("")
    end

    self.frame:Show()
end

-- Start countdown timer
function ResponseFrame:StartTimer()
    if self.timerTicker then
        self.timerTicker:Cancel()
    end

    self.timerTicker = C_Timer.NewTicker(1, function()
        local elapsed = GetTime() - self.startTime
        local remaining = math.max(0, self.timeout - elapsed)

        if remaining > 0 then
            self.frame.timerText:SetText(string.format("Time remaining: %d seconds", remaining))
        else
            self.frame.timerText:SetText("Time's up!")
            -- Auto-pass on timeout
            self:SubmitResponse("PASS")
        end
    end)
end

-- Submit response
function ResponseFrame:SubmitResponse(responseId)
    if not self.sessionId or not self.itemIndex then return end

    local note = self.frame.noteBox:GetText()
    if note == "" then note = nil end

    ELM.SessionManager:SubmitResponse(self.itemIndex, responseId, note)

    local responseText = ELM.SessionManager:GetResponseText(responseId)
    ELM.Utils:Print("Response submitted: " .. responseText)

    self:Hide()
end

-- Hide the frame
function ResponseFrame:Hide()
    if self.timerTicker then
        self.timerTicker:Cancel()
        self.timerTicker = nil
    end

    if self.frame then
        self.frame:Hide()
    end
end

-- Check if shown
function ResponseFrame:IsShown()
    return self.frame and self.frame:IsShown()
end
