CREATE TABLE user_characters (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    character_name VARCHAR(255) NOT NULL,
    realm VARCHAR(255) NOT NULL,
    character_class VARCHAR(50) NOT NULL,
    level INTEGER NOT NULL,
    playable_race VARCHAR(50) NOT NULL,
    faction VARCHAR(50) NOT NULL,
    blizzard_id BIGINT,
    last_synced_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, character_name, realm)
);

CREATE INDEX idx_user_characters_user_id ON user_characters(user_id);
