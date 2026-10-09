# Automatic payments setup

Razorpay Standard Checkout and manually reviewed direct UPI are available. Neither payment method starts until the owner configures and enables it. No live transaction has been tested. Configure settlement bank details in the merchant dashboard.

The main owner can add UPI ID/payee and Razorpay credentials in Admin → Rent / Buy / Plans. Uploader and event-manager roles cannot read or modify these settings or approve payments. Gateway secrets are encrypted with AES-GCM using the hosted PAYMENT_SETTINGS_KEY; responses never return secrets. Preserve that environment key. Existing environment credentials remain a fallback until encrypted credentials are saved. Keep Gateway disabled until merchant activation and the end-to-end acceptance test are complete. Credential replacement is blocked while gateway orders need reconciliation; rotation requires a planned migration rather than invalidating existing purchases.

Users save name, Indian mobile number and city without OTP. Mobile is unverified. Each new order snapshots profile fields and amount. Direct UPI orders snapshot the payee, accept one 12-digit UTR/RRN, and grant no access until owner approval after checking the bank. UTR is unique across orders. Approval requires confirmation of the exact amount, records the reviewer and time, and starts validity once. Repeated approvals do not extend validity. Rejection requires a reason and grants no access. Admin receives a polling count of submitted UPI requests awaiting review; there are no outgoing notification messages.

Webhook URL: /api/payments/webhook on the published site. Subscribe to payment.captured, order.paid and refund.processed. Enable automatic payment capture in the Razorpay dashboard. Verify webhook delivery and real-account checkout/refund before accepting customers. Local integration tests mock the provider; they do not prove merchant activation, network reachability or settlement.

Monthly means 30 days; yearly means 365 days. No recurring mandate or automatic renewal. Rent starts at first server-confirmed capture and lasts the configured days. Buy has no expiry while the content remains available. Subscription grants only explicitly included paid videos. A full refund revokes the corresponding order; partial refunds retain access. Existing free videos remain free until Admin marks them paid.

Payment totals are available to the owner; personal purchase history is user-isolated. Legacy unverified pending requests remain stored in entitlements/subscriptions, are not paid access and are not converted to confirmed orders. Existing offers are retained. Revenue sharing and producer payouts remain separate; this update does not automate them.

Official integration references:
- https://razorpay.com/docs/payments/payment-gateway/web-integration/standard/integration-steps/
- https://razorpay.com/docs/webhooks/validate-test/
- https://razorpay.com/docs/api/payments/fetch-with-id/
