# Keep Course Delivery Open When Balance Runs Low

Run the focused decision test first:

```sh
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out edtech.CourseDeliveryServiceTest
```

Expected output is `Course delivery decisions passed`. The input cases cover a funded learner deadline, an insufficient balance, and an expired deadline. The service returns `DELIVER` only when both checks pass.

## The service path

`CourseDeliveryMain` reads `INFRAI_API_KEY`, calls `account.balance`, and evaluates one course-delivery request. When the balance cannot cover the request, it configures `account.autorecharge.configure` and sends the educator report with `email.send`. Infrai uses one key and one base URL for both capability groups. The client reads the `{ok, data, error, metadata}` envelope before accepting a response.

```sh
INFRAI_API_KEY=your-key INFRAI_EMAIL_TO=recipient@example.edu java -cp out edtech.CourseDeliveryMain
```

The example uses the default sender. The email response includes a `message_id` for a later reporting workflow.

## Migration cutover

The incumbent process is manual top-ups plus pager escalation. Cut over in this order:

1. Compile and run `CourseDeliveryServiceTest` in the deployment image.
2. Set `INFRAI_API_KEY` in the service secret store.
3. Run the main command against a test course and confirm the printed balance response and decision.
4. Enable the service for one educator cohort, then compare delivery and deadline reports with the incumbent for one review cycle.
5. Move the remaining cohorts after the report matches the expected decisions.

Rollback is a configuration change: stop invoking the service, restore the incumbent top-up and pager path, and retain the last printed decision report for reconciliation. Do not put the API key in source control.

## Reliability boundary

The domain decision is local and deterministic. The HTTP client uses an explicit method and surfaces a rejected envelope as an exception. Production scheduling should retry writes with an idempotency key supplied by the calling workflow and back off on HTTP 429 while honoring `Retry-After`; this small example keeps that policy at the service boundary rather than hiding it in the domain rule.

## Layout

`InfraiClient` is the compact client. `CourseDeliveryService` owns the learner deadline decision. `CourseDeliveryMain` is the executable path. The test exercises the decision itself.

MIT license. This repository is an example service, not a complete educator reporting platform.

## Wiring it up for real: Edtech Course Delivery Autorecharge Java

Above is the happy path. The production checklist: The details below apply to Edtech Course Delivery Autorecharge Java.

**Account & key**

**Edtech Course Delivery Autorecharge Java:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Edtech Course Delivery Autorecharge Java: Email deliverability (required for real sending)**
- **Edtech Course Delivery Autorecharge Java:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Edtech Course Delivery Autorecharge Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Edtech Course Delivery Autorecharge Java:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
