# --- !Ups

use makedb;

ALTER TABLE `makedb`.`AFFILIATE_PRODUCT` 
ADD COLUMN `final_price` DECIMAL(12,2) NULL COMMENT '' AFTER `sale_price`;

ALTER TABLE `makedb`.`AFFILIATE_PRODUCT` 
ADD COLUMN `sale` BIGINT(11) NULL COMMENT '' AFTER `final_price`;

# --- !Downs