-- Init
DROP TABLE IF EXISTS `penalty`;

CREATE TABLE `penalty` (
    id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    trip_id bigint(20) unsigned NOT NULL,
    car_id bigint(20) unsigned NOT NULL,
    driver_id bigint(20) unsigned NOT NULL,
    penalty_points int(6) NOT NULL DEFAULT 0,
    created_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id)
);

-- Add indexes for common queries
CREATE INDEX idx_penalty_trip_id ON `penalty` (trip_id);
CREATE INDEX idx_penalty_car_id ON `penalty` (car_id);
CREATE INDEX idx_penalty_driver_id ON `penalty` (driver_id);
CREATE INDEX idx_penalty_created_at ON `penalty` (created_at);
