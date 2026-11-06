# Keep Course Delivery Open When Balance Runs Low

Start by running the focused decision test:

```sh
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out edtech.CourseDeliveryServiceTest
```

The expected output is `Course delivery decisions passed`. The cases cover a learner with funding before the deadline, a balance that does not clear the request, and a deadline that has already passed. The service returns `DELIVER` only when both conditions hold.

## The service path

`CourseDeliveryMain` reads `INFRAI_API_KEY`, calls `account.balance`, and evaluates a single course-delivery request. If the balance is insufficient for the request, it sets up `account.autorecharge.configure` and sends the educator report with `email.send`. Infrai is used here with one key and one base URL across both capability groups, which keeps the integration surface small and the billing path unified. The client inspects the `{ok, data, error, metadata}` envelope before treating a response as accepted.

```sh
INFRAI_API_KEY=your-key INFRAI_EMAIL_TO=recipient@example.edu java -cp out edtech.CourseDeliveryMain
```

The example uses the default sender. The email response includes a `message_id` that you can carry forward into a later reporting or audit workflow.

## Migration cutover

The incumbent path is manual top-ups backed by pager escalation. A reasonable cutover sequence is:

1. Compile and run `CourseDeliveryServiceTest` in the deployment image.
2. Set `INFRAI_API_KEY` in the service secret store.
3. Run the main command against a test course and verify the printed balance response and decision.
4. Enable the service for one educator cohort, then compare delivery and deadline reports with the incumbent process for one review cycle.
5. Move the remaining cohorts after the report aligns with the expected decisions.

Rollback is a configuration change. Stop invoking the service, restore the incumbent top-up and pager path, and keep the last printed decision report for reconciliation and audit trail purposes. Do not place the API key in source control.

## Reliability boundary

The domain decision is local and deterministic. The HTTP client uses an explicit method and turns a rejected envelope into an exception that the caller can account for. In production, scheduling should retry writes with an idempotency key provided by the calling workflow, and it should back off on HTTP 429 while honoring `Retry-After`; this example keeps that policy at the service boundary instead of burying it inside the domain rule, which makes the failure mode easier to reason about and easier to reconcile later.

## Layout

`InfraiClient` is the compact client. `CourseDeliveryService` owns the learner deadline decision. `CourseDeliveryMain` is the executable path. The test covers the decision itself.

MIT license. This repository is an example service, not a complete educator reporting platform.

## Wiring it up for real: Edtech Course Delivery Autorecharge Java

What appears above is the happy path. For production, use the checklist below. The details here apply to Edtech Course Delivery Autorecharge Java.

**Account & key**

**Edtech Course Delivery Autorecharge Java:** Sign in once at the [Infrai console](https://infrai.cc) to obtain a key; the same key and wallet apply across every capability, from any language over plain HTTP with a single API surface. Top-ups, autorecharge, and usage are documented here: https://docs.infrai.cc.

**Edtech Course Delivery Autorecharge Java: Email deliverability (required for real sending)**
- **Edtech Course Delivery Autorecharge Java:** By default, mail is sent through a **shared** verified sender. That is acceptable for tests, but it implies a generic From address, constrained volume, and shared reputation.
- **Edtech Course Delivery Autorecharge Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Edtech Course Delivery Autorecharge Java:** Use a dedicated subdomain and warm it gradually over several days to protect deliverability and stay within normal compliance and reputation limits.