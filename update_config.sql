UPDATE guild_configurations 
SET wowaudit_api_key_encrypted = 'c54b2a68a71ac498ea8cac01c3a806ef5daf7b9469ae7c7abcac970a48468fa5', 
    wowaudit_guild_uri = 'https://wowaudit.com/eu/twisting-nether/dod/main/profile', 
    sync_enabled = true, 
    sync_run_on_startup = true 
WHERE "guildId" = 'default';
