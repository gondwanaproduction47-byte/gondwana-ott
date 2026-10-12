CREATE TABLE `playback_policy` (
	`id` integer PRIMARY KEY NOT NULL,
	`enabled` integer DEFAULT 0 NOT NULL,
	`max_devices` integer DEFAULT 3 NOT NULL,
	`max_screens` integer DEFAULT 1 NOT NULL
);
--> statement-breakpoint
CREATE TABLE `team_members` (
	`email` text PRIMARY KEY NOT NULL,
	`role` text NOT NULL,
	`updated_at` integer NOT NULL
);
--> statement-breakpoint
CREATE TABLE `viewer_devices` (
	`id` text PRIMARY KEY NOT NULL,
	`user_id` text NOT NULL,
	`name` text NOT NULL,
	`revoked` integer DEFAULT 0 NOT NULL,
	`created_at` integer NOT NULL,
	`last_seen` integer NOT NULL
);
--> statement-breakpoint
CREATE INDEX `idx_devices_user` ON `viewer_devices` (`user_id`,`revoked`);--> statement-breakpoint
CREATE TABLE `viewing_sessions` (
	`id` text PRIMARY KEY NOT NULL,
	`entry_id` text NOT NULL,
	`viewer_key` text NOT NULL,
	`user_id` text,
	`device_id` text NOT NULL,
	`position` integer DEFAULT 0 NOT NULL,
	`duration` integer DEFAULT 0 NOT NULL,
	`watched` integer DEFAULT 0 NOT NULL,
	`completed` integer DEFAULT 0 NOT NULL,
	`ended` integer DEFAULT 0 NOT NULL,
	`expires_at` integer NOT NULL,
	`created_at` integer NOT NULL,
	`updated_at` integer NOT NULL,
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE INDEX `idx_sessions_user_active` ON `viewing_sessions` (`user_id`,`ended`,`expires_at`);--> statement-breakpoint
CREATE INDEX `idx_sessions_entry` ON `viewing_sessions` (`entry_id`,`created_at`);