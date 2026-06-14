# Promotion Service

Promotion Service owns LuxNest promotion data. It implements voucher management, checkout validation, Marketing Center campaigns, combo promotions, marketing notifications, and voucher publish flows.

## Modules

- `promotion-domain`: JPA entities and enums.
- `promotion-application`: voucher validation, marketing CRUD, combo calculation, dispatch orchestration.
- `promotion-adapter`: REST controllers, security header adapter, and repositories.
- `promotion-bootstrap`: Spring Boot entrypoint and Flyway migrations.

## HTTP API

Client-facing routes are exposed by the gateway under `/promotions/**` and rewritten to `/api/v1/**`.

- `GET /api/v1/vouchers/user`
- `POST /api/v1/vouchers/validate`
- `POST /api/v1/vouchers/{code}/save`
- `GET /api/v1/combos/active`
- `POST /api/v1/combos/validate`

Internal service/admin routes:

- `GET /api/v1/internal/admin/vouchers`
- `GET /api/v1/internal/admin/vouchers/stats`
- `POST /api/v1/internal/admin/vouchers`
- `PUT /api/v1/internal/admin/vouchers/{id}`
- `DELETE /api/v1/internal/admin/vouchers/{id}`
- `POST /api/v1/internal/admin/vouchers/{id}/publish`
- `POST /api/v1/internal/vouchers/validate-order`
- `POST /api/v1/internal/combos/validate-order`
- `GET/POST/PUT/DELETE /api/v1/internal/admin/marketing/campaigns`
- `GET/POST/PUT/DELETE /api/v1/internal/admin/marketing/combos`
- `GET/POST/PUT/DELETE /api/v1/internal/admin/marketing/notifications`

## Validation Rules

- Code must exist and be active.
- `startDate` and `endDate` must include the current time when present.
- Subtotal must satisfy `minOrder`.
- Shop vouchers accept `PERCENT` and `FIXED`.
- Shipping vouchers accept `SHIPPING_CAP`; discount is clamped to the shipping fee.

## Marketing Center

- Campaigns and notifications can be saved as draft, scheduled, or dispatched immediately.
- Scheduled dispatch is scanned by a Spring scheduled job.
- Voucher publish grants `user_vouchers`, writes `marketing_dispatch_logs`, and dispatches notification messages.
- Combos store item snapshots and calculate `originalAmount`, `finalAmount`, and `savedAmount` in the service.

## Cross-Service Integration

The application layer uses ports for target resolution and marketing notification dispatch. The current adapters are safe fallbacks so the service can run before local gRPC contracts are fully wired:

- manual targets are accepted from request user ids;
- `ALL` and `SEGMENT` targets return empty lists with a warning until user/order/cart gRPC adapters are connected;
- notification dispatch records accepted recipients without crashing.

Reserved env keys:

- `NOTIFICATION_SERVICE_GRPC_HOST/PORT`
- `USER_SERVICE_GRPC_HOST/PORT`
- `ORDER_SERVICE_GRPC_HOST/PORT`
- `CART_SERVICE_GRPC_HOST/PORT`
- `CATALOG_SERVICE_GRPC_HOST/PORT`
