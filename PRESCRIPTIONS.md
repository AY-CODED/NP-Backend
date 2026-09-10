# Prescription approval flow

Prescription documents are private customer records. Nuges stores their binary contents in the
`prescription_documents` database table, separate from listable prescription metadata. The files
are never returned by product, cart, order, or prescription-list responses.

## Customer endpoints

- `POST /api/prescriptions` — multipart upload with `productId`, `quantity`, and `file`
- `GET /api/prescriptions` — the authenticated customer's review history
- `GET /api/prescriptions/{id}/file` — the authenticated customer's own file

## Pharmacist endpoints

- `GET /api/admin/prescriptions?status=PENDING`
- `GET /api/admin/prescriptions/{id}/file`
- `PUT /api/admin/prescriptions/{id}/review`

An approval request uses `status: APPROVED`, an `approvedQuantity`, and an optional reason. A
rejection uses `status: REJECTED` and requires a reason. The backend records the reviewer and
review time.

## Checkout rules

Each prescription is tied to one customer and one product. Order creation locks an approved
prescription and reserves only the approved remaining quantity. Cancelling or expiring an unpaid
order restores that quantity. Paid orders keep the quantity consumed.

## Upload controls

- JPEG, PNG, and PDF file signatures only
- 5 MB maximum by default
- authenticated upload and download routes
- `Cache-Control: no-store` and `X-Content-Type-Options: nosniff` on file responses

The limit can be configured with `PRESCRIPTION_MAX_FILE_SIZE_BYTES`.
