# Transactional email setup

The backend uses Spring's provider-neutral SMTP support. No email secret is
required by either frontend.

## Production environment variables

Configure these on the backend host:

```text
APP_MAIL_ENABLED=true
MAIL_FROM=no-reply@your-verified-domain.example
MAIL_BRAND_NAME=Nuges Pharmacy
SMTP_HOST=<provider SMTP host>
SMTP_PORT=587
SMTP_USERNAME=<provider username>
SMTP_PASSWORD=<provider password or SMTP key>
SMTP_AUTH=true
SMTP_STARTTLS=true
SMTP_STARTTLS_REQUIRED=true
EMAIL_VERIFICATION_TTL_MINUTES=1440
PASSWORD_RESET_TTL_MINUTES=30
```

First deploy with `EMAIL_VERIFICATION_REQUIRED=false`. Confirm that password
reset and order emails arrive, then change it to `true`. This prevents an SMTP
configuration mistake from locking customers out.

The backend intentionally refuses to start if email verification is required
while mail delivery is disabled.

## Email events

- account verification and verification resend
- password reset
- order created with payment pending
- verified payment
- pharmacist prescription approval or rejection
- admin order-status change

Reset and verification tokens are generated from 32 random bytes. Only their
SHA-256 hashes are stored in PostgreSQL. Tokens expire and are single-use.
Links carry tokens in the browser URL fragment so they are not sent to frontend
hosting access logs or HTTP referrer headers.
Changing a password increments the user's credential version, invalidating
older application JWT cookies.

Configure SPF, DKIM, and DMARC for the sending domain in the chosen provider
before production launch.
