-- Raid Plans: Canvas-based strategy planning for raid encounters
-- Similar to RaidPlan.io functionality

CREATE TABLE raid_plans (
    id VARCHAR(36) PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    encounter_id INT NOT NULL,
    encounter_name VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    visibility VARCHAR(50) NOT NULL DEFAULT 'GUILD',
    share_token VARCHAR(64) UNIQUE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE raid_plan_steps (
    id BIGSERIAL PRIMARY KEY,
    plan_id VARCHAR(36) NOT NULL REFERENCES raid_plans(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    notes TEXT,
    UNIQUE(plan_id, step_order)
);

CREATE TABLE raid_plan_markers (
    id BIGSERIAL PRIMARY KEY,
    step_id BIGINT NOT NULL REFERENCES raid_plan_steps(id) ON DELETE CASCADE,
    marker_type VARCHAR(50) NOT NULL,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    label VARCHAR(100),
    color VARCHAR(20)
);

CREATE TABLE raid_plan_shapes (
    id BIGSERIAL PRIMARY KEY,
    step_id BIGINT NOT NULL REFERENCES raid_plan_steps(id) ON DELETE CASCADE,
    shape_type VARCHAR(50) NOT NULL,
    x1 DOUBLE PRECISION NOT NULL,
    y1 DOUBLE PRECISION NOT NULL,
    x2 DOUBLE PRECISION,
    y2 DOUBLE PRECISION,
    radius DOUBLE PRECISION,
    color VARCHAR(20),
    stroke_width INT DEFAULT 2
);

-- Indexes for efficient querying
CREATE INDEX idx_raid_plans_guild ON raid_plans(guild_id);
CREATE INDEX idx_raid_plans_encounter ON raid_plans(guild_id, encounter_id);
CREATE INDEX idx_raid_plans_share ON raid_plans(share_token);
CREATE INDEX idx_raid_plan_steps_plan ON raid_plan_steps(plan_id);
CREATE INDEX idx_raid_plan_markers_step ON raid_plan_markers(step_id);
CREATE INDEX idx_raid_plan_shapes_step ON raid_plan_shapes(step_id);
