-- Test Environment Initialization
-- Sets up mocks and loads addon modules for testing

local MockWowApi = require("spec.mocks.wow_api")
local MockAceLibs = require("spec.mocks.ace_libs")

local TestEnv = {}

-- Initialize test environment
function TestEnv:Setup()
    -- Reset all mocks
    MockWowApi:Reset()
    MockAceLibs:Reset()

    -- Install mock API functions
    MockWowApi:Install()
    MockAceLibs:Install()

    -- Clear print output
    _G._printOutput = {}

    -- Create addon namespace
    _G.EdgeRushLootMan = {}

    return MockWowApi, MockAceLibs
end

-- Load addon module for testing
function TestEnv:LoadModule(modulePath)
    -- Create addon namespace if not exists
    local ADDON_NAME = "EdgeRushLootMan"
    local ELM = _G.EdgeRushLootMan or {}
    _G.EdgeRushLootMan = ELM

    -- Load module (simulating WoW's vararg passing)
    local chunk, err = loadfile(modulePath)
    if not chunk then
        error("Failed to load module: " .. modulePath .. "\nError: " .. tostring(err))
    end

    -- Execute with addon name and namespace
    local success, result = pcall(chunk, ADDON_NAME, ELM)
    if not success then
        error("Failed to execute module: " .. modulePath .. "\nError: " .. tostring(result))
    end

    return ELM
end

-- Get print output
function TestEnv:GetPrintOutput()
    return _G._printOutput or {}
end

-- Clear print output
function TestEnv:ClearPrintOutput()
    _G._printOutput = {}
end

return TestEnv
