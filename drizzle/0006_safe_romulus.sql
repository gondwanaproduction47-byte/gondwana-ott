CREATE TABLE `episode_groups` (
	`id` text PRIMARY KEY NOT NULL,
	`parent_key` text NOT NULL,
	`event_id` text,
	`series_title` text DEFAULT '' NOT NULL,
	`genre` text DEFAULT '' NOT NULL,
	`season` integer DEFAULT 1 NOT NULL,
	`episode` integer NOT NULL,
	`title` text NOT NULL,
	`description` text DEFAULT '' NOT NULL,
	`duration_seconds` integer DEFAULT 0 NOT NULL,
	`voting_enabled` integer DEFAULT 0 NOT NULL,
	`created_at` integer NOT NULL,
	FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE UNIQUE INDEX `idx_episode_parent_number` ON `episode_groups` (`parent_key`,`season`,`episode`);--> statement-breakpoint
CREATE TABLE `watch_progress` (
	`user_id` text NOT NULL,
	`entry_id` text NOT NULL,
	`position_seconds` integer NOT NULL,
	`duration_seconds` integer NOT NULL,
	`completed` integer DEFAULT 0 NOT NULL,
	`updated_at` integer NOT NULL,
	PRIMARY KEY(`user_id`, `entry_id`),
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
ALTER TABLE `events` ADD `episode_mode` integer DEFAULT 0 NOT NULL;--> statement-breakpoint
ALTER TABLE `performances` ADD `episode_group_id` text;--> statement-breakpoint
ALTER TABLE `performances` ADD `description` text DEFAULT '' NOT NULL;--> statement-breakpoint
ALTER TABLE `performances` ADD `duration_seconds` integer DEFAULT 0 NOT NULL;--> statement-breakpoint
ALTER TABLE `performances` ADD `vote_eligible` integer DEFAULT 1 NOT NULL;