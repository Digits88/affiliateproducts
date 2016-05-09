# --- !Ups

use makedb;

CREATE TABLE `AFFILIATE_BATCH_JOBS` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(64) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `is_running` bit(1) NOT NULL,
  `last_run` datetime DEFAULT NULL,
  `last_elapsed_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `AFFILIATE_BRAND` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_AFFILIATE_BRAND_UNIQUE1` (`name`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `AFFILIATE_BRAND_PRODUCT` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `brand_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_AFFILIATE_BRAND_PRODUCT_1` (`brand_id`),
  KEY `IDX_AFFILIATE_BRAND_PRODUCT_2` (`product_id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `AFFILIATE_CATEGORY` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_AFFILIATE_CATEGORY_UNIQUE1` (`name`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `AFFILIATE_CATEGORY_PRODUCT` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_AFFILIATE_CATEGORY_PRODUCT_1` (`category_id`),
  KEY `IDX_AFFILIATE_CATEGORY_PRODUCT_2` (`product_id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `AFFILIATE_PRODUCT` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `advertiser_id` bigint(11) NOT NULL,
  `seller_id` bigint(11) NOT NULL,
  `advertiser_category` varchar(1024) DEFAULT NULL,
  `category_id` bigint(11) DEFAULT NULL,
  `buy_url` varchar(2048) DEFAULT NULL,
  `description` text,
  `image_url` varchar(1024) DEFAULT NULL,
  `in_stock` tinyint(2) DEFAULT NULL,
  `manufacturer_name` varchar(255) DEFAULT NULL,
  `brand_id` bigint(11) DEFAULT NULL,
  `name` varchar(1025) DEFAULT NULL,
  `keywords` varchar(2048) DEFAULT NULL,
  `price` decimal(12,2) DEFAULT NULL,
  `retail_price` decimal(12,2) DEFAULT NULL,
  `sale_price` decimal(12,2) DEFAULT NULL,
  `sku` varchar(255) DEFAULT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_AFFILIATE_PRODUCT_UNIQUE1` (`advertiser_id`,`sku`),
  KEY `IDX_AFFILIATE_PRODUCT_1` (`seller_id`),
  KEY `IDX_AFFILIATE_PRODUCT_2` (`brand_id`),
  KEY `IDX_AFFILIATE_PRODUCT_3` (`category_id`),
  KEY `IDX_AFFILIATE_PRODUCT_4` (`keywords`(255))
) DEFAULT CHARSET=utf8;

CREATE TABLE `AFFILIATE_SELLER` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `advertiser_id` bigint(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_AFFILIATE_SELLER_UNIQUE1` (`advertiser_id`),
  UNIQUE KEY `IDX_AFFILIATE_SELLER_UNIQUE2` (`name`)
) DEFAULT CHARSET=utf8;

CREATE TABLE `AFFILIATE_SELLER_PRODUCT` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `seller_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_AFFILIATE_SELLER_PRODUCT_1` (`seller_id`),
  KEY `IDX_AFFILIATE_SELLER_PRODUCT_2` (`product_id`)
) DEFAULT CHARSET=utf8;

ALTER TABLE `AFFILIATE_CATEGORY` 
ADD COLUMN `syw_tag_id` BIGINT(11) NULL AFTER `name`;

CREATE TABLE `AFFILIATE_LOG_REQUESTS` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `server_host` varchar(64) DEFAULT NULL,
  `ip_address` varchar(64) DEFAULT NULL,
  `source` varchar(64) DEFAULT NULL,
  `channel` varchar(64) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `request_id` varchar(255) DEFAULT NULL,
  `http_method` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `class` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `message` text,
  `params` text,
  `exception` text,
  `begin` datetime DEFAULT NULL,
  `end` datetime DEFAULT NULL,
  `elapsed_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_LOG_REQUESTS_1` (`user_id`),
  KEY `IDX_LOG_REQUESTS_2` (`request_id`),
  KEY `IDX_LOG_REQUESTS_3` (`path`),
  KEY `IDX_LOG_REQUESTS_4` (`class`),
  KEY `IDX_LOG_REQUESTS_5` (`method`),
  KEY `IDX_LOG_REQUESTS_6` (`begin`),
  KEY `IDX_LOG_REQUESTS_7` (`end`),
  KEY `IDX_LOG_REQUESTS_8` (`source`),
  KEY `IDX_LOG_REQUESTS_9` (`channel`)
) DEFAULT CHARSET=utf8;

ALTER TABLE `makedb`.`AFFILIATE_PRODUCT` 
DROP INDEX `IDX_AFFILIATE_PRODUCT_UNIQUE1` ,
ADD UNIQUE INDEX `IDX_AFFILIATE_PRODUCT_UNIQUE1` (`advertiser_id` ASC, `sku` ASC);

ALTER TABLE `makedb`.`AFFILIATE_PRODUCT` 
ADD INDEX `IDX_AFFILIATE_PRODUCT_5` (`advertiser_id` ASC),
ADD INDEX `IDX_AFFILIATE_PRODUCT_6` (`sku` ASC);

CREATE TABLE `makedb`.`AFFILIATE_AFFILIATE` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `creation_date` datetime NOT NULL,
  `updation_date` datetime NOT NULL,
  `version` bigint(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_AFFILIATE_AFFILIATE_UNIQUE1` (`name`)
) DEFAULT CHARSET=utf8;


ALTER TABLE `makedb`.`AFFILIATE_SELLER` 
ADD COLUMN `affiliate_id` BIGINT(11) NOT NULL AFTER `name`,
ADD COLUMN `min_comm` DECIMAL(12,2) NULL AFTER `affiliate_id`,
ADD COLUMN `max_comm` DECIMAL(12,2) NULL AFTER `min_comm`;

# --- !Downs