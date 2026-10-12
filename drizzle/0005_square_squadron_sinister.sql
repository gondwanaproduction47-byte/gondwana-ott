CREATE TABLE `revenue_contracts` (
	`entry_id` text PRIMARY KEY NOT NULL,
	`producer_name` text NOT NULL,
	`producer_email` text NOT NULL,
	`enabled` integer DEFAULT 0 NOT NULL,
	`producer_percent` integer DEFAULT 70 NOT NULL,
	`terms` text DEFAULT '' NOT NULL,
	`updated_by` text NOT NULL,
	`updated_at` integer NOT NULL,
	FOREIGN KEY (`entry_id`) REFERENCES `performances`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE INDEX `idx_revenue_producer` ON `revenue_contracts` (`producer_email`,`enabled`);--> statement-breakpoint
CREATE TABLE `revenue_ledger` (
	`id` text PRIMARY KEY NOT NULL,
	`entry_id` text NOT NULL,
	`kind` text NOT NULL,
	`gross_paise` integer DEFAULT 0 NOT NULL,
	`deductions_paise` integer DEFAULT 0 NOT NULL,
	`producer_paise` integer DEFAULT 0 NOT NULL,
	`platform_paise` integer DEFAULT 0 NOT NULL,
	`payout_paise` integer DEFAULT 0 NOT NULL,
	`producer_percent` integer NOT NULL,
	`reference` text NOT NULL,
	`note` text DEFAULT '' NOT NULL,
	`occurred_at` integer NOT NULL,
	`created_by` text NOT NULL,
	`created_at` integer NOT NULL,
	FOREIGN KEY (`entry_id`) REFERENCES `revenue_contracts`(`entry_id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE INDEX `idx_revenue_ledger_entry` ON `revenue_ledger` (`entry_id`);--> statement-breakpoint
CREATE UNIQUE INDEX `idx_revenue_reference` ON `revenue_ledger` (`reference`);