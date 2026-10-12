CREATE TABLE `performances` (
	`id` text PRIMARY KEY NOT NULL,
	`singer` text NOT NULL,
	`song` text NOT NULL,
	`city` text DEFAULT '' NOT NULL,
	`object_key` text NOT NULL,
	`mime` text NOT NULL,
	`size` integer NOT NULL,
	`upload_id` text NOT NULL,
	`status` text DEFAULT 'uploading' NOT NULL,
	`created_by` text NOT NULL,
	`created_at` integer NOT NULL,
	`published_at` integer
);
--> statement-breakpoint
CREATE INDEX `idx_performances_status_published` ON `performances` (`status`,`published_at`);--> statement-breakpoint
CREATE TABLE `upload_parts` (
	`entry_id` text NOT NULL,
	`part_number` integer NOT NULL,
	`etag` text NOT NULL,
	PRIMARY KEY(`entry_id`, `part_number`),
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE cascade
);
--> statement-breakpoint
CREATE TABLE `votes` (
	`entry_id` text NOT NULL,
	`user_id` text NOT NULL,
	`created_at` integer NOT NULL,
	PRIMARY KEY(`entry_id`, `user_id`),
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE cascade
);
