-- Add recruitment comments table linked to enhanced_applications
-- Links officers' comments to candidate applications

CREATE TABLE IF NOT EXISTS recruitment_comments (
    id BIGSERIAL PRIMARY KEY,
    application_id VARCHAR(255) NOT NULL REFERENCES enhanced_applications(enhanced_application_id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id),
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_recruitment_comments_application_id ON recruitment_comments(application_id);
