-- Create application notes table for officer comments
CREATE TABLE application_notes (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,

    -- Note content
    author_id VARCHAR(100) NOT NULL,
    author_name VARCHAR(100),
    content TEXT NOT NULL,
    note_type VARCHAR(50) DEFAULT 'COMMENT',

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_note_application FOREIGN KEY (application_id)
        REFERENCES applications(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_application_notes_application ON application_notes(application_id);
CREATE INDEX idx_application_notes_author ON application_notes(author_id);
CREATE INDEX idx_application_notes_created ON application_notes(created_at DESC);
