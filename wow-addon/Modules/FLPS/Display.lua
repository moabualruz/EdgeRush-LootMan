-- EdgeRush LootMan FLPS Display
-- Shows FLPS score and breakdown

local ADDON_NAME, ELM = ...

ELM.Display = {}

local Display = ELM.Display

-- Create the FLPS display frame
function Display:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushFLPSDisplay", UIParent, "BackdropTemplate")
    frame:SetSize(200, 100)
    frame:SetPoint("TOP", UIParent, "TOP", 0, -100)
    frame:SetMovable(true)
    frame:EnableMouse(true)
    frame:RegisterForDrag("LeftButton")
    frame:SetScript("OnDragStart", frame.StartMoving)
    frame:SetScript("OnDragStop", frame.StopMovingOrSizing)

    frame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 16,
        insets = { left = 4, right = 4, top = 4, bottom = 4 }
    })
    frame:SetBackdropColor(0, 0, 0, 0.8)

    -- Title
    local title = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("TOP", frame, "TOP", 0, -10)
    title:SetText("FLPS Score")
    frame.title = title

    -- Score display
    local scoreText = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalHuge")
    scoreText:SetPoint("TOP", title, "BOTTOM", 0, -5)
    scoreText:SetText("0.0%")
    frame.scoreText = scoreText

    -- Breakdown
    local breakdown = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    breakdown:SetPoint("TOP", scoreText, "BOTTOM", 0, -5)
    breakdown:SetJustifyH("CENTER")
    breakdown:SetText("RMS: 0.0 | IPI: 0.0 | RDF: 0.0")
    frame.breakdown = breakdown

    -- Rank
    local rank = frame:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    rank:SetPoint("TOP", breakdown, "BOTTOM", 0, -5)
    rank:SetText("Rank: #0")
    frame.rank = rank

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -2, -2)

    frame:Hide()
    self.frame = frame

    return frame
end

-- Update the display with current FLPS data
function Display:Update()
    if not self.frame then
        self:Create()
    end

    local flps = ELM.Addon.db.char.flps

    -- Update score with color
    local scoreStr = ELM.Utils:FormatFLPS(flps.score)
    self.frame.scoreText:SetText(ELM.Utils:ColorByFLPS(scoreStr, flps.score))

    -- Update breakdown
    self.frame.breakdown:SetText(string.format(
        "RMS: %.1f%% | IPI: %.1f%% | RDF: %.1f%%",
        flps.rms * 100,
        flps.ipi * 100,
        flps.rdf * 100
    ))

    -- Update rank
    self.frame.rank:SetText("Rank: #" .. flps.rank)
end

-- Show the display
function Display:Show()
    if not self.frame then
        self:Create()
    end
    self:Update()
    self.frame:Show()
end

-- Hide the display
function Display:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Toggle the display
function Display:Toggle()
    if self.frame and self.frame:IsShown() then
        self:Hide()
    else
        self:Show()
    end
end

-- Refresh the display
function Display:Refresh()
    if self.frame and self.frame:IsShown() then
        self:Update()
    end
end

-- Hook into tooltip to show FLPS on player hover
function Display:HookTooltip()
    if self.tooltipHooked then return end

    -- Hook GameTooltip for player units
    GameTooltip:HookScript("OnTooltipSetUnit", function(tooltip)
        if not ELM.Addon.db.profile.display.showTooltip then return end

        local _, unit = tooltip:GetUnit()
        if not unit then return end

        -- Only show for raid/party members
        if not UnitInRaid(unit) and not UnitInParty(unit) then return end

        local name = UnitName(unit)
        if not name then return end

        -- Look up FLPS data (would need to be synced from server/comms)
        local flpsData = self:GetPlayerFLPS(name)
        if flpsData then
            tooltip:AddLine(" ")
            tooltip:AddLine("|cff00ccff[EdgeRush FLPS]|r")
            tooltip:AddDoubleLine(
                "Score:",
                ELM.Utils:ColorByFLPS(ELM.Utils:FormatFLPS(flpsData.score), flpsData.score)
            )
            tooltip:AddDoubleLine("Rank:", "#" .. flpsData.rank)
            tooltip:Show()
        end
    end)

    self.tooltipHooked = true
end

-- Get FLPS data for a player (from cache/comms)
function Display:GetPlayerFLPS(name)
    -- Check leaderboard cache
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

-- Create minibar display (small FLPS score near minimap)
function Display:CreateMinibar()
    if self.minibar then return self.minibar end

    local frame = CreateFrame("Frame", "EdgeRushFLPSMinibar", UIParent, "BackdropTemplate")
    frame:SetSize(80, 24)
    frame:SetPoint("TOPRIGHT", MinimapCluster, "BOTTOMRIGHT", 0, -5)

    frame:SetBackdrop({
        bgFile = "Interface\\Buttons\\WHITE8x8",
        edgeFile = "Interface\\Buttons\\WHITE8x8",
        edgeSize = 1,
    })
    frame:SetBackdropColor(0, 0, 0, 0.6)
    frame:SetBackdropBorderColor(0.3, 0.3, 0.3, 1)

    local label = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    label:SetPoint("LEFT", frame, "LEFT", 4, 0)
    label:SetText("FLPS:")
    label:SetTextColor(0, 0.8, 1)

    local score = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    score:SetPoint("RIGHT", frame, "RIGHT", -4, 0)
    score:SetText("0.0%")
    frame.score = score

    -- Click to show full display
    frame:EnableMouse(true)
    frame:SetScript("OnMouseUp", function()
        self:Toggle()
    end)

    -- Tooltip
    frame:SetScript("OnEnter", function(f)
        GameTooltip:SetOwner(f, "ANCHOR_LEFT")
        GameTooltip:AddLine("EdgeRush FLPS")
        GameTooltip:AddLine("Click to toggle full display", 1, 1, 1)
        GameTooltip:Show()
    end)
    frame:SetScript("OnLeave", function()
        GameTooltip:Hide()
    end)

    self.minibar = frame
    return frame
end

-- Update minibar
function Display:UpdateMinibar()
    if not self.minibar then return end

    local flps = ELM.Addon.db.char.flps
    local scoreStr = ELM.Utils:FormatFLPS(flps.score)
    self.minibar.score:SetText(ELM.Utils:ColorByFLPS(scoreStr, flps.score))
end

-- Show/hide minibar
function Display:SetMinibarVisible(visible)
    if not self.minibar then
        self:CreateMinibar()
    end

    if visible then
        self.minibar:Show()
        self:UpdateMinibar()
    else
        self.minibar:Hide()
    end
end
