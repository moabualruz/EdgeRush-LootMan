-- Ace3 Library Mocks for Busted Testing
-- Provides mock implementations of Ace3 libraries

local MockAceLibs = {}

-- Mock registered event handlers
MockAceLibs._eventHandlers = {}
MockAceLibs._commHandlers = {}
MockAceLibs._chatCommands = {}

-- Reset all mock data
function MockAceLibs:Reset()
    self._eventHandlers = {}
    self._commHandlers = {}
    self._chatCommands = {}
end

-- Fire an event for testing
function MockAceLibs:FireEvent(event, ...)
    local handler = self._eventHandlers[event]
    if handler then
        handler(event, ...)
    end
end

-- Fire a comm message for testing
function MockAceLibs:FireComm(prefix, message, distribution, sender)
    local handler = self._commHandlers[prefix]
    if handler then
        handler.callback(prefix, message, distribution, sender)
    end
end

-- Install mocks into global scope
function MockAceLibs:Install()
    local self = self

    -- LibStub mock
    _G.LibStub = function(name, silent)
        if name == "AceAddon-3.0" then
            return {
                NewAddon = function(_, addonName, ...)
                    local addon = {
                        _name = addonName,
                        db = nil,
                    }

                    -- AceConsole-3.0 methods
                    function addon:RegisterChatCommand(cmd, callback)
                        MockAceLibs._chatCommands[cmd] = {
                            addon = self,
                            callback = callback,
                        }
                    end

                    function addon:Print(msg)
                        print(msg)
                    end

                    -- AceEvent-3.0 methods
                    function addon:RegisterEvent(event, callback)
                        callback = callback or event
                        MockAceLibs._eventHandlers[event] = function(...)
                            if type(callback) == "string" then
                                addon[callback](addon, ...)
                            else
                                callback(addon, ...)
                            end
                        end
                    end

                    function addon:UnregisterEvent(event)
                        MockAceLibs._eventHandlers[event] = nil
                    end

                    -- AceComm-3.0 methods
                    function addon:RegisterComm(prefix, callback)
                        callback = callback or "OnCommReceived"
                        MockAceLibs._commHandlers[prefix] = {
                            addon = self,
                            callback = function(...)
                                if type(callback) == "string" then
                                    addon[callback](addon, ...)
                                else
                                    callback(addon, ...)
                                end
                            end,
                        }
                    end

                    function addon:SendCommMessage(prefix, message, distribution, target)
                        -- Store for testing
                        addon._lastCommMessage = {
                            prefix = prefix,
                            message = message,
                            distribution = distribution,
                            target = target,
                        }
                    end

                    -- AceSerializer-3.0 methods
                    function addon:Serialize(msgType, data)
                        -- Simple JSON-like serialization for testing
                        return msgType .. ":" .. (data and tostring(data) or "")
                    end

                    function addon:Deserialize(message)
                        local msgType, data = message:match("^([^:]+):(.*)$")
                        if msgType then
                            return true, msgType, data
                        end
                        return false
                    end

                    return addon
                end,
            }
        elseif name == "AceDB-3.0" then
            return {
                New = function(_, savedVar, defaults, defaultProfile)
                    local db = {
                        global = defaults.global and {} or nil,
                        profile = defaults.profile and {} or nil,
                        char = defaults.char and {} or nil,
                        _callbacks = {},
                    }

                    -- Deep copy defaults
                    local function deepCopy(orig)
                        if type(orig) ~= 'table' then
                            return orig
                        end
                        local copy = {}
                        for k, v in pairs(orig) do
                            copy[k] = deepCopy(v)
                        end
                        return copy
                    end

                    if defaults.global then
                        db.global = deepCopy(defaults.global)
                    end
                    if defaults.profile then
                        db.profile = deepCopy(defaults.profile)
                    end
                    if defaults.char then
                        db.char = deepCopy(defaults.char)
                    end

                    function db:RegisterCallback(obj, callbackName, methodName)
                        table.insert(db._callbacks, {
                            obj = obj,
                            callbackName = callbackName,
                            methodName = methodName,
                        })
                    end

                    return db
                end,
            }
        elseif name == "LibDataBroker-1.1" then
            return {
                NewDataObject = function(_, name, dataObj)
                    return dataObj
                end,
            }
        elseif name == "LibDBIcon-1.0" then
            return {
                Register = function(_, name, dataObj, options)
                end,
                Show = function(_, name)
                end,
                Hide = function(_, name)
                end,
                IsRegistered = function(_, name)
                    return true
                end,
            }
        end

        if not silent then
            error("Unknown library: " .. name)
        end
        return nil
    end

    -- Settings mock (for options)
    _G.Settings = {
        OpenToCategory = function(category)
        end,
    }
end

return MockAceLibs
