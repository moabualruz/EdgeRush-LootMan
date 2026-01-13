-- Create wishlist_items table for structured wishlist storage
CREATE TABLE IF NOT EXISTS wishlist_items (
    id SERIAL PRIMARY KEY,
    raiderId BIGINT NOT NULL,
    itemId BIGINT NOT NULL,
    itemName TEXT NOT NULL,
    priority INTEGER NOT NULL,
    upgradePercentage DOUBLE PRECISION NOT NULL,
    specName TEXT,
    createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updatedAt TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (raiderId, itemId)
);

CREATE INDEX IF NOT EXISTS idx_wishlist_items_raider ON wishlist_items(raiderId);
CREATE INDEX IF NOT EXISTS idx_wishlist_items_item ON wishlist_items(itemId);
