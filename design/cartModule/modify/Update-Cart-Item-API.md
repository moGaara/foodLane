# Update Cart Item — API Design

## Endpoint

```http
PATCH /api/v1/carts/{cartId}/items/{cartItemId}
```

## Purpose

Updates one existing cart item without replacing the entire cart.

## Authentication

```http
Authorization: Bearer <access-token>
Content-Type: application/json
```

The authenticated customer must own the specified cart.

## Path Parameters

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `cartId` | UUID/Long | Yes | Identifier of the customer's cart |
| `cartItemId` | UUID/Long | Yes | Identifier of the cart item to update |

> Use the project's existing ID type if the implementation already defines one.

## Request Body

All fields are optional so the endpoint can support partial modification.

```json
{
  "quantity": 2,
  "customizations": [
    {
      "id": 12,
      "quantity": 1
    }
  ],
  "note": "No onions"
}
```

### Request Fields

| Field | Type | Required | Rules |
|---|---|---:|---|
| `quantity` | integer | No | Must be >= 1 and within the allowed availability/quantity limit |
| `customizations` | array | No | Must contain only valid customizations for the menu item |
| `note` | string | No | Must satisfy the project's maximum note length |

At least one editable field should be supplied.

## Success Response

### `200 OK`

```json
{
  "cartId": "cart-123",
  "items": [
    {
      "cartItemId": "item-456",
      "menuItemId": "menu-789",
      "quantity": 2,
      "customizations": [],
      "note": "No onions",
      "unitPrice": 12.50,
      "subtotal": 25.00
    }
  ],
  "subtotal": 25.00,
  "total": 25.00
}
```

The exact response fields should be aligned with the project's existing Cart DTO/model.

## Error Responses

### `400 Bad Request`

Used for invalid request data.

Examples:

```json
{
  "code": "INVALID_QUANTITY",
  "message": "Quantity must be at least 1."
}
```

```json
{
  "code": "INVALID_CUSTOMIZATION",
  "message": "The selected customization is not valid for this menu item."
}
```

### `401 Unauthorized`

Customer is not authenticated.

### `403 Forbidden`

The authenticated customer does not own the specified cart.

### `404 Not Found`

Possible cases:

- Cart does not exist.
- Cart item does not exist.

Example:

```json
{
  "code": "CART_ITEM_NOT_FOUND",
  "message": "Cart item was not found."
}
```

### `409 Conflict`

Used when the request conflicts with the current cart/menu state.

Examples:

- Cart is no longer editable.
- Menu item is unavailable.
- Requested quantity exceeds current availability.

Example:

```json
{
  "code": "INSUFFICIENT_AVAILABILITY",
  "message": "The requested quantity is not currently available."
}
```

### `500 Internal Server Error`

Unexpected persistence or server failure.

## Update Rules

### Increasing Quantity

If the customer changes:

```text
quantity: 2 → 5
```

the system must verify that the item can currently be supplied in quantity `5` before saving.

### Decreasing Quantity

If the customer changes:

```text
quantity: 5 → 2
```

the system validates the new quantity and updates the item.

### Quantity = 0

Quantity `0` is not accepted by this endpoint.

```text
PATCH quantity = 0
        ↓
400 INVALID_QUANTITY
```

The Remove Item endpoint/use case should be used instead.

## Transaction / Consistency Requirement

The update should behave as one logical operation:

```text
Validate
  ↓
Update CartItem
  ↓
Recalculate Totals
  ↓
Persist
```

If persistence fails, the previous cart state must remain unchanged.

## API Summary

| Method | Endpoint | Purpose |
|---|---|---|
| `PATCH` | `/api/v1/carts/{cartId}/items/{cartItemId}` | Partially update one cart item |

## Notes for Implementation

- Do not trust the client-provided price.
- Recalculate the item's subtotal and cart total on the server.
- Re-check availability when the requested quantity could affect availability constraints.
- Keep the update transactional.
- Do not allow a customer to update another customer's cart.
