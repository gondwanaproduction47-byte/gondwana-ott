CREATE TABLE `customer_profiles` (
	`user_id` text PRIMARY KEY NOT NULL,
	`name` text NOT NULL,
	`mobile` text NOT NULL,
	`city` text NOT NULL,
	`updated_at` integer NOT NULL
);
--> statement-breakpoint
CREATE TABLE `manual_payments` (
	`order_id` text PRIMARY KEY NOT NULL,
	`upi_id` text NOT NULL,
	`payee_name` text NOT NULL,
	`utr` text,
	`submitted_at` integer,
	`reviewed_by` text,
	`reviewed_at` integer,
	`note` text DEFAULT '' NOT NULL,
	FOREIGN KEY (`order_id`) REFERENCES `payment_orders`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE UNIQUE INDEX `idx_manual_utr` ON `manual_payments` (`utr`);--> statement-breakpoint
CREATE TABLE `payment_settings` (
	`id` integer PRIMARY KEY NOT NULL,
	`upi_id` text DEFAULT '' NOT NULL,
	`payee_name` text DEFAULT '' NOT NULL,
	`manual_enabled` integer DEFAULT 0 NOT NULL,
	`gateway_enabled` integer DEFAULT 0 NOT NULL,
	`gateway_key_id` text DEFAULT '' NOT NULL,
	`gateway_encrypted` text,
	`updated_by` text NOT NULL,
	`updated_at` integer NOT NULL
);
--> statement-breakpoint
ALTER TABLE `payment_orders` ADD `method` text DEFAULT 'gateway' NOT NULL;--> statement-breakpoint
ALTER TABLE `payment_orders` ADD `customer_name` text DEFAULT '' NOT NULL;--> statement-breakpoint
ALTER TABLE `payment_orders` ADD `customer_mobile` text DEFAULT '' NOT NULL;--> statement-breakpoint
ALTER TABLE `payment_orders` ADD `customer_city` text DEFAULT '' NOT NULL;--> statement-breakpoint
ALTER TABLE `payment_orders` ADD `customer_email` text DEFAULT '' NOT NULL;