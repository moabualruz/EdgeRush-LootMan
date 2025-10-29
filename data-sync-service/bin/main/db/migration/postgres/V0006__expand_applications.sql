ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS main_character_race TEXT,
    ADD COLUMN IF NOT EXISTS main_character_faction TEXT,
    ADD COLUMN IF NOT EXISTS main_character_level INTEGER,
    ADD COLUMN IF NOT EXISTS main_character_region TEXT;

ALTER TABLE application_alts
    ADD COLUMN IF NOT EXISTS region TEXT,
    ADD COLUMN IF NOT EXISTS faction TEXT,
    ADD COLUMN IF NOT EXISTS race TEXT;

ALTER TABLE application_questions
    ADD COLUMN IF NOT EXISTS position INTEGER;

CREATE TABLE IF NOT EXISTS application_question_files (
    id SERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(application_id) ON DELETE CASCADE,
    question_position INTEGER,
    question TEXT,
    original_filename TEXT,
    url TEXT
);
