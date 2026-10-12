CREATE TABLE `candidate_votes` (
	`scope_key` text NOT NULL,
	`user_id` text NOT NULL,
	`candidate_id` text NOT NULL,
	`created_at` integer NOT NULL,
	PRIMARY KEY(`scope_key`, `user_id`),
	FOREIGN KEY (`candidate_id`) REFERENCES `candidates`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE INDEX `idx_candidate_votes_candidate` ON `candidate_votes` (`candidate_id`);--> statement-breakpoint
CREATE TABLE `candidates` (
	`id` text PRIMARY KEY NOT NULL,
	`event_id` text NOT NULL,
	`episode_group_id` text,
	`scope_key` text NOT NULL,
	`name` text NOT NULL,
	`city` text DEFAULT '' NOT NULL,
	`number` integer NOT NULL,
	`photo_key` text,
	`created_at` integer NOT NULL,
	FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE UNIQUE INDEX `idx_candidate_scope_number` ON `candidates` (`scope_key`,`number`);--> statement-breakpoint
CREATE INDEX `idx_candidate_event` ON `candidates` (`event_id`);--> statement-breakpoint
ALTER TABLE `events` ADD `candidate_voting` integer DEFAULT 0 NOT NULL;--> statement-breakpoint
ALTER TABLE `performances` ADD `candidate_id` text;