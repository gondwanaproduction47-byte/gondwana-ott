ALTER TABLE `performances` ADD `content_type` text DEFAULT 'singing' NOT NULL;--> statement-breakpoint
ALTER TABLE `performances` ADD `genre` text DEFAULT '' NOT NULL;--> statement-breakpoint
ALTER TABLE `performances` ADD `series_title` text DEFAULT '' NOT NULL;--> statement-breakpoint
ALTER TABLE `performances` ADD `season` integer;--> statement-breakpoint
ALTER TABLE `performances` ADD `episode` integer;