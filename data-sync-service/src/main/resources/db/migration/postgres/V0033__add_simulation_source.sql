-- Add external_id and source to simulation_requests to support Raidbots
ALTER TABLE simulation_requests
ADD COLUMN external_id VARCHAR(255),
ADD COLUMN source VARCHAR(50) DEFAULT 'LOCAL';

-- Add index for external_id lookup
CREATE INDEX idx_sim_requests_external_id ON simulation_requests(external_id);
