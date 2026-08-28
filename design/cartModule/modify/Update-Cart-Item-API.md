# Update Cart Item — API Design

## Endpoint

```http
PATCH /api/v1/carts/{cartId}/items/{cartItemId}
```

## Authentication

```http
Authorization: Bearer <token>
Content-Type: application/json
```

The token payload currently contains `customerID`. The repository uses that ID to restrict the update to the customer's active cart.

## Path Parameters

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `cartId` | Long | Yes | Cart identifier |
| `cartItemId` | Long | Yes | Cart-item identifier |

## Request Body

```json
{
  "quantity": 2,
  "itemNote": "No pickles please",
  "customizations": [
    { "customizationOptionId": 1, "quantity": 1 },
    { "customizationOptionId": 3, "quantity": 1 }
  ]
}
```

| Field | Type | Required | Rules |
|---|---|---:|---|
| `quantity` | integer | No | `0–99`; `0` deletes and `1–99` updates |
| `itemNote` | string | No | Maximum 1000 characters; an empty string clears the visible note |
| `customizations` | array | No | Replaces existing selections; options and group limits are validated |

At least one editable field is required. Omitted fields remain unchanged.
Each customization quantity accepts `0–6`; quantity `0` removes that option.

## Success Response

### `200 OK`

```json
{
  "header": {
    "statusCode": "I000000",
    "statusDesc": "Success"
  },
  "body": {
    "cartId": 1,
    "customerId": 1,
    "restaurantId": 1,
    "items": [
      {
        "cartId": 1,
        "cartItemId": 1,
        "menuItemId": 1,
        "menuItemName": "Classic Cheeseburger",
        "quantity": 2,
        "unitPrice": 12.50,
        "totalPrice": 29.00,
        "itemNote": "No pickles please",
        "customizations": [
          {
            "customizationOptionId": 3,
            "name": "Extra Bacon",
            "quantity": 1,
            "unitPrice": 2.00,
            "totalPrice": 2.00
          }
        ]
      }
    ],
    "totalPrice": 29.00
  }
}
```

For quantity `0`, the deleted item is absent from `items` and `totalPrice` is recalculated.

## Validation and Errors

| Condition | Current result |
|---|---|
| Missing authorization header | `400 Bad Request` |
| Invalid body, negative/greater-than-99 quantity, oversized note, or no editable field | `400`, `E000005` |
| Cart item does not match cart/customer/active status | `400`, `E000004` |
| Quantity exceeds inventory | `400`, `E000006` |
| Duplicate customization options | `400`, `E000007` |
| Customization option does not exist | `400`, `E000008` |
| Customization is not available for the menu item | `400`, `E000009` |
| Required/min/max customization rule fails | `400`, `E000010` |
| Unexpected database failure | `500 Internal Server Error` |

Business failures are thrown as `BusinessException` and converted by `GlobalExceptionHandler` using `ErrorMapping`.

## Transaction Flow

```text
Find and authorize CartItem
  ↓
quantity = 0? ── Yes → Delete and flush
  │
  No
  ↓
Validate provided fields → Update quantity/note/customizations
  ↓
Shared CartMapper loads remaining items
  ↓
Recalculate totals
  ↓
Return updated cart
```

## Database Notes

- Entities use the mainline PostgreSQL `foodland` schema.
- Stored `cart_item.quantity` values are from `1` through `99`.
- Quantity `0` causes deletion and is never persisted.
- Customization quantity `0` removes the selected option and is never persisted.
- An empty customization list deletes all customization rows only if every linked group permits zero selections.
- Deleting a cart item cascades to its customization rows.
- Customization prices come from server-side option prices and are saved as snapshots.
- The shared `CartMapper` builds item/customization responses and recalculates totals.
