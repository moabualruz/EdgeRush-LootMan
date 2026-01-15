-- EdgeRush LootMan Minimap Button
-- Minimap button for quick access

local ADDON_NAME, ELM = ...

ELM.MinimapButton = {}

local MinimapButton = ELM.MinimapButton

-- Create the minimap button using LibDBIcon
function MinimapButton:Create()
    if self.button then return end

    local LDB = LibStub("LibDataBroker-1.1")
    local LDBIcon = LibStub("LibDBIcon-1.0")

    -- Create data broker object
    local dataObj = LDB:NewDataObject("EdgeRushLootMan", {
        type = "launcher",
        icon = "Interface\\Icons\\Achievement_Arena_2v2_7",
        label = "EdgeRush LootMan",
        text = "EdgeRush",

        OnClick = function(_, button)
            if button == "LeftButton" then
                if IsShiftKeyDown() then
                    -- Quick export on shift-click
                    if ELM.GearExport then
                        ELM.GearExport:Export()
                        ELM.Utils:Print("Gear exported")
                    end
                else
                    -- Toggle main frame
                    if ELM.MainFrame then
                        ELM.MainFrame:Toggle()
                    end
                end
            elseif button == "RightButton" then
                -- Show options menu
                self:ShowMenu()
            end
        end,

        OnTooltipShow = function(tooltip)
            tooltip:AddLine("|cff00ccffEdgeRush LootMan|r")
            tooltip:AddLine(" ")

            -- Show FLPS score
            local flps = ELM.Addon.db.char.flps
            local scoreStr = ELM.Utils:ColorByFLPS(ELM.Utils:FormatFLPS(flps.score), flps.score)
            tooltip:AddDoubleLine("FLPS Score:", scoreStr)
            tooltip:AddDoubleLine("Rank:", "#" .. flps.rank)

            tooltip:AddLine(" ")
            tooltip:AddLine("|cffffffffLeft-Click:|r Open window")
            tooltip:AddLine("|cffffffffShift-Click:|r Export gear")
            tooltip:AddLine("|cffffffffRight-Click:|r Menu")
        end,
    })

    -- Register with LibDBIcon
    LDBIcon:Register("EdgeRushLootMan", dataObj, ELM.Addon.db.profile.minimap)

    self.dataObj = dataObj
    self.button = LDBIcon:GetMinimapButton("EdgeRushLootMan")
end

-- Show context menu
function MinimapButton:ShowMenu()
    local menu = {
        { text = "EdgeRush LootMan", isTitle = true, notCheckable = true },
        { text = " ", isTitle = true, notCheckable = true },
        {
            text = "FLPS Display",
            notCheckable = true,
            func = function()
                if ELM.Display then
                    ELM.Display:Toggle()
                end
            end,
        },
        {
            text = "Leaderboard",
            notCheckable = true,
            func = function()
                if ELM.Leaderboard then
                    ELM.Leaderboard:Toggle()
                end
            end,
        },
        {
            text = "Wishlist",
            notCheckable = true,
            func = function()
                if ELM.Wishlist then
                    ELM.Wishlist:Toggle()
                end
            end,
        },
        { text = " ", isTitle = true, notCheckable = true },
        {
            text = "Loot Council",
            notCheckable = true,
            func = function()
                if ELM.LootFrame then
                    ELM.LootFrame:Toggle()
                end
            end,
        },
        { text = " ", isTitle = true, notCheckable = true },
        {
            text = "Export Gear",
            notCheckable = true,
            func = function()
                if ELM.GearExport then
                    ELM.GearExport:Export()
                    ELM.Utils:Print("Gear exported")
                end
            end,
        },
        {
            text = "Gear Report",
            notCheckable = true,
            func = function()
                if ELM.GearExport then
                    ELM.GearExport:PrintReport()
                end
            end,
        },
        { text = " ", isTitle = true, notCheckable = true },
        {
            text = "Options",
            notCheckable = true,
            func = function()
                ELM.Addon:OpenOptions()
            end,
        },
        {
            text = "Hide Button",
            notCheckable = true,
            func = function()
                ELM.Addon:ToggleMinimap()
            end,
        },
        { text = " ", isTitle = true, notCheckable = true },
        { text = "Close", notCheckable = true },
    }

    -- Use EasyMenu or simple dropdown
    local menuFrame = CreateFrame("Frame", "EdgeRushMinimapMenu", UIParent, "UIDropDownMenuTemplate")
    EasyMenu(menu, menuFrame, "cursor", 0, 0, "MENU")
end

-- Refresh button visibility
function MinimapButton:Refresh()
    local LDBIcon = LibStub("LibDBIcon-1.0", true)
    if not LDBIcon then return end

    if ELM.Addon.db.profile.minimap.hide then
        LDBIcon:Hide("EdgeRushLootMan")
    else
        LDBIcon:Show("EdgeRushLootMan")
    end
end

-- Update tooltip text (for dynamic FLPS display)
function MinimapButton:Update()
    -- LibDataBroker handles tooltip updates automatically
    -- Just updating the text property will refresh it
    if self.dataObj then
        local flps = ELM.Addon.db.char.flps
        self.dataObj.text = ELM.Utils:FormatFLPS(flps.score)
    end
end

-- Initialize on addon load
function MinimapButton:Initialize()
    self:Create()
    self:Refresh()

    -- Update periodically
    C_Timer.NewTicker(30, function()
        self:Update()
    end)
end
