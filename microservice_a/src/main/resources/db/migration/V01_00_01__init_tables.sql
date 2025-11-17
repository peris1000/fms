DROP table if exists `trip`;
DROP table if exists `car`;
DROP table if exists `driver`;

CREATE TABLE `driver` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `first_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    `last_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `driving_license` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    `penalty_points` int(6) unsigned DEFAULT '0',
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- create table driver_SEQ (
--     next_val bigint
-- ) engine=InnoDB;

CREATE TABLE `car` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `brand` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    `model` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    `serial_number` varchar(128) COLLATE utf8mb4_unicode_ci NULL,
    `license_plate` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    `assignedDriver_id` bigint(20) unsigned NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `license_plate` (`license_plate`),
    KEY `fk_car_driver` (`assignedDriver_id`),
    CONSTRAINT `fk_car_driver` FOREIGN KEY (`assignedDriver_id`) REFERENCES `driver` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- create table car_SEQ (
--     next_val bigint
-- ) engine=InnoDB;

CREATE TABLE `trip` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `car_id` bigint(20) unsigned NOT NULL,
    `driver_id` bigint(20) unsigned NOT NULL,
    `planned_start_time` datetime(3) NULL,
    `planned_end_time` datetime(3) NULL,
    `start_time` datetime(3) NULL,
    `end_time` datetime(3) NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `fk_trip_car` (`car_id`),
    KEY `fk_trip_driver` (`driver_id`),
    CONSTRAINT `fk_trip_car` FOREIGN KEY (`car_id`) REFERENCES `car` (`id`),
    CONSTRAINT `fk_trip_driver` FOREIGN KEY (`driver_id`) REFERENCES `driver` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- create table trip_SEQ (
--     next_val bigint
-- ) engine=InnoDB;

