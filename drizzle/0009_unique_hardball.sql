CREATE TABLE `content_offers` (
	`id` text PRIMARY KEY NOT NULL,
	`entry_id` text NOT NULL,
	`kind` text NOT NULL,
	`price_paise` integer NOT NULL,
	`duration_days` integer,
	`enabled` integer DEFAULT 0 NOT NULL,
	`updated_at` integer NOT NULL,
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE INDEX `idx_offers_entry` ON `content_offers` (`entry_id`,`enabled`);--> statement-breakpoint
CREATE TABLE `entitlements` (
	`id` text PRIMARY KEY NOT NULL,
	`user_id` text NOT NULL,
	`entry_id` text,
	`offer_kind` text NOT NULL,
	`status` text DEFAULT 'pending' NOT NULL,
	`started_at` integer,
	`expires_at` integer,
	`order_ref` text NOT NULL,
	`created_at` integer NOT NULL
);
--> statement-breakpoint
CREATE INDEX `idx_entitlements_user` ON `entitlements` (`user_id`,`status`,`expires_at`);--> statement-breakpoint
CREATE TABLE `subscriptions` (
	`id` text PRIMARY KEY NOT NULL,
	`user_id` text NOT NULL,
	`plan` text NOT NULL,
	`price_paise` integer NOT NULL,
	`status` text DEFAULT 'pending' NOT NULL,
	`started_at` integer,
	`expires_at` integer,
	`order_ref` text NOT NULL,
	`created_at` integer NOT NULL
);
--> statement-breakpoint
CREATE INDEX `idx_subscriptions_user` ON `subscriptions` (`user_id`,`status`,`expires_at`);