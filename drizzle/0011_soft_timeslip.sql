CREATE TABLE `content_access` (
	`entry_id` text PRIMARY KEY NOT NULL,
	`paid` integer DEFAULT 0 NOT NULL,
	`subscription` integer DEFAULT 0 NOT NULL,
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE TABLE `payment_orders` (
	`id` text PRIMARY KEY NOT NULL,
	`user_id` text NOT NULL,
	`offer_id` text NOT NULL,
	`entry_id` text,
	`kind` text NOT NULL,
	`title` text NOT NULL,
	`amount` integer NOT NULL,
	`duration_days` integer,
	`status` text DEFAULT 'creating' NOT NULL,
	`provider_order_id` text,
	`payment_id` text,
	`paid_at` integer,
	`expires_at` integer,
	`created_at` integer NOT NULL
);
--> statement-breakpoint
CREATE UNIQUE INDEX `idx_payment_pending` ON `payment_orders` (`user_id`,`offer_id`) WHERE "payment_orders"."status" IN ('creating','pending');--> statement-breakpoint
CREATE UNIQUE INDEX `idx_payment_provider_order` ON `payment_orders` (`provider_order_id`);--> statement-breakpoint
CREATE UNIQUE INDEX `idx_payment_id` ON `payment_orders` (`payment_id`);--> statement-breakpoint
CREATE INDEX `idx_payment_user_access` ON `payment_orders` (`user_id`,`status`,`entry_id`,`expires_at`);--> statement-breakpoint
PRAGMA foreign_keys=OFF;--> statement-breakpoint
CREATE TABLE `__new_content_offers` (
	`id` text PRIMARY KEY NOT NULL,
	`entry_id` text,
	`kind` text NOT NULL,
	`price_paise` integer NOT NULL,
	`duration_days` integer,
	`enabled` integer DEFAULT 0 NOT NULL,
	`updated_at` integer NOT NULL,
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
INSERT INTO `__new_content_offers`("id", "entry_id", "kind", "price_paise", "duration_days", "enabled", "updated_at") SELECT "id", "entry_id", "kind", "price_paise", "duration_days", "enabled", "updated_at" FROM `content_offers`;--> statement-breakpoint
DROP TABLE `content_offers`;--> statement-breakpoint
ALTER TABLE `__new_content_offers` RENAME TO `content_offers`;--> statement-breakpoint
PRAGMA foreign_keys=ON;--> statement-breakpoint
CREATE INDEX `idx_offers_entry` ON `content_offers` (`entry_id`,`enabled`);