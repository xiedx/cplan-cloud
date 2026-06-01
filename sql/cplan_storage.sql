-- ============================================================
-- cplan_storage schema — Storage Service database (meta-data)
-- ============================================================

CREATE DATABASE IF NOT EXISTS cplan_storage
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- File metadata can be tracked in cplan_creation.t_video_segment;
-- this schema is reserved for storage-native tables if needed later.
