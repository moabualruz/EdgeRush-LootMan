-- Migration to make tables JPA-compatible
-- Renames snake_case columns to camelCase for JPA conventions

-- Raiders table - already mostly JPA-friendly
ALTER TABLE raiders 
    RENAME COLUMN character_name TO characterName;
ALTER TABLE raiders 
    RENAME COLUMN character_class TO characterClass;
ALTER TABLE raiders 
    RENAME COLUMN character_realm TO realm;
ALTER TABLE raiders 
    RENAME COLUMN wowaudit_id TO wowauditId;
ALTER TABLE raiders 
    RENAME COLUMN join_date TO joinDate;

-- Attendance stats
ALTER TABLE attendance_stats
    RENAME COLUMN character_name TO characterName;
ALTER TABLE attendance_stats
    RENAME COLUMN character_realm TO characterRealm;
ALTER TABLE attendance_stats
    RENAME COLUMN character_class TO characterClass;
ALTER TABLE attendance_stats
    RENAME COLUMN character_role TO characterRole;
ALTER TABLE attendance_stats
    RENAME COLUMN start_date TO startDate;
ALTER TABLE attendance_stats
    RENAME COLUMN end_date TO endDate;
ALTER TABLE attendance_stats
    RENAME COLUMN attended_amount_of_raids TO attendedAmount;
ALTER TABLE attendance_stats
    RENAME COLUMN total_amount_of_raids TO totalAmount;
ALTER TABLE attendance_stats
    RENAME COLUMN attended_percentage TO attendancePercentage;
ALTER TABLE attendance_stats
    RENAME COLUMN synced_at TO syncedAt;

-- Loot awards
ALTER TABLE loot_awards
    RENAME COLUMN item_id TO itemId;
ALTER TABLE loot_awards
    RENAME COLUMN item_name TO itemName;
ALTER TABLE loot_awards
    RENAME COLUMN item_level TO itemLevel;
ALTER TABLE loot_awards
    RENAME COLUMN character_name TO characterName;
ALTER TABLE loot_awards
    RENAME COLUMN character_realm TO characterRealm;
ALTER TABLE loot_awards
    RENAME COLUMN awarded_at TO awardedAt;

-- Raider gear items
ALTER TABLE raider_gear_items
    RENAME COLUMN raider_id TO raiderId;
ALTER TABLE raider_gear_items
    RENAME COLUMN gear_set TO gearSet;
ALTER TABLE raider_gear_items
    RENAME COLUMN item_id TO itemId;
ALTER TABLE raider_gear_items
    RENAME COLUMN item_level TO itemLevel;
ALTER TABLE raider_gear_items
    RENAME COLUMN upgrade_level TO upgradeLevel;

-- Wishlist items (if table exists)
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'wishlist_items') THEN
        ALTER TABLE wishlist_items RENAME COLUMN item_id TO itemId;
        ALTER TABLE wishlist_items RENAME COLUMN item_name TO itemName;
        ALTER TABLE wishlist_items RENAME COLUMN upgrade_percentage TO upgradePercentage;
        ALTER TABLE wishlist_items RENAME COLUMN spec_name TO specName;
    END IF;
END $$;

-- Loot bans
ALTER TABLE loot_bans
    RENAME COLUMN raider_id TO raiderId;
ALTER TABLE loot_bans
    RENAME COLUMN banned_at TO bannedAt;
ALTER TABLE loot_bans
    RENAME COLUMN expires_at TO expiresAt;
ALTER TABLE loot_bans
    RENAME COLUMN banned_by TO bannedBy;
