-- EdgeRush LootMan Voting Frame
-- Officer voting interface for loot decisions

local ADDON_NAME, ELM = ...

ELM.VotingFrame = {}

local VotingFrame = ELM.VotingFrame

-- Vote options
local VOTE_OPTIONS = {
    { value = "BIS", label = "Best in Slot", color = { r = 0, g = 1, b = 0 } },
    { value = "UPGRADE", label = "Major Upgrade", color = { r = 0.5, g = 1, b = 0 } },
    { value = "MINOR", label = "Minor Upgrade", color = { r = 1, g = 1, b = 0 } },
    { value = "OFFSPEC", label = "Offspec/Alt", color = { r = 1, g = 0.5, b = 0 } },
    { value = "PASS", label = "Pass", color = { r = 0.5, g = 0.5, b = 0.5 } },
}

-- Create the voting frame
function VotingFrame:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushVotingFrame", UIParent, "BackdropTemplate")
    frame:SetSize(300, 200)
    frame:SetPoint("CENTER", 300, 0)
    frame:SetMovable(true)
    frame:EnableMouse(true)
    frame:RegisterForDrag("LeftButton")
    frame:SetScript("OnDragStart", frame.StartMoving)
    frame:SetScript("OnDragStop", frame.StopMovingOrSizing)
    frame:SetFrameStrata("DIALOG")

    frame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background-Dark",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 32,
        insets = { left = 8, right = 8, top = 8, bottom = 8 }
    })
    frame:SetBackdropColor(0.1, 0.1, 0.15, 0.95)

    -- Title
    local title = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("TOP", frame, "TOP", 0, -15)
    title:SetText("Cast Your Vote")
    title:SetTextColor(1, 0.8, 0)

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)

    -- Candidate name
    local candidateName = frame:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    candidateName:SetPoint("TOP", title, "BOTTOM", 0, -10)
    candidateName:SetText("Candidate: ")
    frame.candidateName = candidateName

    -- Item being voted on
    local itemName = frame:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    itemName:SetPoint("TOP", candidateName, "BOTTOM", 0, -5)
    itemName:SetText("")
    frame.itemName = itemName

    -- Vote buttons
    self.voteButtons = {}
    local lastBtn = nil
    for i, option in ipairs(VOTE_OPTIONS) do
        local btn = CreateFrame("Button", nil, frame, "UIPanelButtonTemplate")
        btn:SetSize(150, 25)
        if lastBtn then
            btn:SetPoint("TOP", lastBtn, "BOTTOM", 0, -5)
        else
            btn:SetPoint("TOP", itemName, "BOTTOM", 0, -15)
        end
        btn:SetText(option.label)
        btn.voteValue = option.value
        btn.color = option.color
        btn:SetScript("OnClick", function(b)
            self:CastVote(b.voteValue)
        end)

        self.voteButtons[i] = btn
        lastBtn = btn
    end

    frame:Hide()
    self.frame = frame

    return frame
end

-- Show voting for a candidate
function VotingFrame:ShowForCandidate(candidate, itemLink)
    if not self.frame then
        self:Create()
    end

    self.currentCandidate = candidate
    self.currentItem = itemLink

    self.frame.candidateName:SetText("Candidate: |cffffffff" .. candidate .. "|r")
    self.frame.itemName:SetText(itemLink or "Unknown Item")

    self.frame:Show()
end

-- Cast a vote
function VotingFrame:CastVote(voteValue)
    if not self.currentCandidate or not self.currentItem then return end

    -- Send vote to raid officers
    ELM.Comms:SendVote(self.currentItem, self.currentCandidate, voteValue)

    ELM.Utils:Print("Voted " .. voteValue .. " for " .. self.currentCandidate)

    -- Store our vote
    self:RecordVote(ELM.Utils:GetCharacterID(), self.currentCandidate, voteValue)

    self:Hide()
end

-- Record a vote (either our own or received)
function VotingFrame:RecordVote(voter, candidate, vote)
    if not self.votes then
        self.votes = {}
    end

    if not self.votes[self.currentItem] then
        self.votes[self.currentItem] = {}
    end

    if not self.votes[self.currentItem][candidate] then
        self.votes[self.currentItem][candidate] = {}
    end

    self.votes[self.currentItem][candidate][voter] = vote

    -- Update loot frame if showing
    if ELM.LootFrame and ELM.LootFrame.frame and ELM.LootFrame.frame:IsShown() then
        ELM.LootFrame:UpdateVoteDisplay()
    end
end

-- Process incoming vote
function VotingFrame:ProcessVote(sender, data)
    self:RecordVote(sender, data.candidate, data.vote)

    ELM.Utils:Debug("Vote received from " .. sender .. ": " .. data.vote .. " for " .. data.candidate)
end

-- Get votes for a candidate
function VotingFrame:GetVotesForCandidate(itemLink, candidate)
    if not self.votes or not self.votes[itemLink] or not self.votes[itemLink][candidate] then
        return {}
    end
    return self.votes[itemLink][candidate]
end

-- Get vote summary for a candidate
function VotingFrame:GetVoteSummary(itemLink, candidate)
    local votes = self:GetVotesForCandidate(itemLink, candidate)

    local summary = {
        total = 0,
        BIS = 0,
        UPGRADE = 0,
        MINOR = 0,
        OFFSPEC = 0,
        PASS = 0,
    }

    for _, vote in pairs(votes) do
        summary.total = summary.total + 1
        if summary[vote] then
            summary[vote] = summary[vote] + 1
        end
    end

    return summary
end

-- Clear votes for an item
function VotingFrame:ClearVotes(itemLink)
    if self.votes then
        self.votes[itemLink] = nil
    end
end

-- Hide the frame
function VotingFrame:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Toggle the frame
function VotingFrame:Toggle()
    if self.frame and self.frame:IsShown() then
        self:Hide()
    elseif self.currentCandidate then
        self.frame:Show()
    end
end
