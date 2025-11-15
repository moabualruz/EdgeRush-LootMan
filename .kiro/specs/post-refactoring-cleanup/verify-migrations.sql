-- Database Migration Verification Script
-- This script verifies all Flyway migrations and database schema integrity

-- ============================================================================
-- 1. Check Flyway Migration Status
-- ============================================================================
\echo '=== FLYWAY MIGRATION STATUS ==='
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;

\echo ''
\echo '=== MIGRATION SUMMARY ==='
SELECT 
    COUNT(*) as total_migrations,
    SUM(CASE WHEN success THEN 1 ELSE 0 END) as successful_migrations,
    SUM(CASE WHEN NOT success THEN 1 ELSE 0 END) as failed_migrations
FROM flyway_schema_history;

-- ============================================================================
-- 2. Verify All Tables Exist
-- ============================================================================
\echo ''
\echo '=== DATABASE TABLES ==='
SELECT 
    schemaname,
    tablename,
    tableowner
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;

\echo ''
\echo '=== TABLE COUNT ==='
SELECT COUNT(*) as total_tables
FROM pg_tables
WHERE schemaname = 'public';

-- ============================================================================
-- 3. Verify Foreign Key Relationships
-- ============================================================================
\echo ''
\echo '=== FOREIGN KEY CONSTRAINTS ==='
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;

\echo ''
\echo '=== FOREIGN KEY COUNT ==='
SELECT COUNT(DISTINCT tc.constraint_name) as total_foreign_keys
FROM information_schema.table_constraints AS tc
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public';

-- ============================================================================
-- 4. Verify Indexes
-- ============================================================================
\echo ''
\echo '=== DATABASE INDEXES ==='
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

\echo ''
\echo '=== INDEX COUNT ==='
SELECT COUNT(*) as total_indexes
FROM pg_indexes
WHERE schemaname = 'public';

-- ============================================================================
-- 5. Verify Primary Keys
-- ============================================================================
\echo ''
\echo '=== PRIMARY KEY CONSTRAINTS ==='
SELECT
    tc.table_name,
    kcu.column_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
WHERE tc.constraint_type = 'PRIMARY KEY'
    AND tc.table_schema = 'public'
ORDER BY tc.table_name;

-- ============================================================================
-- 6. Verify Unique Constraints
-- ============================================================================
\echo ''
\echo '=== UNIQUE CONSTRAINTS ==='
SELECT
    tc.table_name,
    kcu.column_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
WHERE tc.constraint_type = 'UNIQUE'
    AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;

-- ============================================================================
-- 7. Check for Orphaned Tables (tables without primary keys)
-- ============================================================================
\echo ''
\echo '=== TABLES WITHOUT PRIMARY KEYS ==='
SELECT t.table_name
FROM information_schema.tables t
LEFT JOIN information_schema.table_constraints tc
    ON t.table_name = tc.table_name
    AND tc.constraint_type = 'PRIMARY KEY'
    AND tc.table_schema = 'public'
WHERE t.table_schema = 'public'
    AND t.table_type = 'BASE TABLE'
    AND tc.constraint_name IS NULL
    AND t.table_name != 'flyway_schema_history'
ORDER BY t.table_name;

-- ============================================================================
-- 8. Verify Column Data Types for Key Tables
-- ============================================================================
\echo ''
\echo '=== COLUMN DEFINITIONS FOR KEY TABLES ==='
SELECT 
    table_name,
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
    AND table_name IN (
        'guilds',
        'raiders',
        'loot_awards',
        'attendance_records',
        'raids',
        'applications',
        'flps_modifiers',
        'behavioral_actions',
        'loot_bans',
        'warcraft_logs_reports',
        'warcraft_logs_config'
    )
ORDER BY table_name, ordinal_position;

-- ============================================================================
-- 9. Check Database Size and Statistics
-- ============================================================================
\echo ''
\echo '=== DATABASE SIZE ==='
SELECT 
    pg_size_pretty(pg_database_size(current_database())) as database_size;

\echo ''
\echo '=== TABLE SIZES ==='
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    pg_total_relation_size(schemaname||'.'||tablename) AS size_bytes
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY size_bytes DESC
LIMIT 20;

-- ============================================================================
-- 10. Verify Sequences
-- ============================================================================
\echo ''
\echo '=== DATABASE SEQUENCES ==='
SELECT 
    schemaname,
    sequencename,
    last_value,
    start_value,
    increment_by
FROM pg_sequences
WHERE schemaname = 'public'
ORDER BY sequencename;
