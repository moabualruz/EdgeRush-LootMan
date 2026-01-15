@echo off
REM EdgeRush LootMan WoW Addon Test Runner
REM Requires: luarocks, busted

echo ======================================
echo EdgeRush LootMan - WoW Addon Tests
echo ======================================

REM Check for busted
where busted >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: busted is not installed
    echo Install with: luarocks install busted
    exit /b 1
)

echo.
echo Running unit tests...
busted --verbose --pattern="_spec" spec\

if %ERRORLEVEL% neq 0 (
    echo.
    echo Tests FAILED!
    exit /b 1
)

echo.
echo ======================================
echo All tests passed!
echo ======================================
