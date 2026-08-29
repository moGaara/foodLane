# UC-2 — Update Cart Item

## Use Case

**Name:** Update Cart Item  
**Actor:** Customer  
**Goal:** Allow an authenticated customer to partially update an item's quantity, note, and selected customizations. Quantity `0` removes the item.

## Preconditions

- The customer supplies an authorization token containing their customer ID.
- The customer has an `ACTIVE` cart.
- The target cart item belongs to that cart and customer.

## Main Flow

1. The client sends a `PATCH` request containing at least one editable field.
2. The controller validates quantity, note length, and nested customization values when present.
3. The service finds the item using `cartItemId`, `cartId`, `customerId`, and cart status `ACTIVE`.
4. If quantity is provided, the service verifies that it does not exceed menu-item inventory.
5. If a note is provided, the service replaces the item note.
6. If customizations are provided, the service validates option existence, menu-item eligibility, duplicates, and group selection limits, then replaces the saved selections using server-side price snapshots.
7. The service updates and saves the cart item.
8. The shared `CartMapper` retrieves all items remaining in the cart.
9. The mapper creates item/customization responses and calculates all totals.
10. The system returns `200 OK` with the updated cart.

## Alternative Flow — Quantity 0

1. The service finds and verifies the target cart item.
2. The service deletes the cart item instead of storing quantity `0`.
3. Related `cart_item_customization` records are deleted through `ON DELETE CASCADE`.
4. The shared mapper retrieves the remaining items and recalculates the cart total.
5. The system returns `200 OK` with the updated cart, excluding the deleted item.

## Exception Flows

### Invalid Quantity

- A negative quantity, quantity greater than `99`, oversized note, or empty PATCH body is rejected with `400 Bad Request` and error code `E000005`.

### Quantity Exceeds Inventory

- A quantity greater than the menu item's inventory is rejected with `400 Bad Request` and error code `E000006`.
- The cart remains unchanged.

### Cart Item Not Found

- If the item does not match the supplied cart, customer, and `ACTIVE` status, `BusinessException(CART_ITEM_NOT_EXIST)` is raised and mapped to error code `E000004`.

### Invalid Customizations

- Duplicate, unknown, unavailable, or invalid group selections raise a mapped `BusinessException`.
- The response uses error codes `E000007` through `E000010` instead of an unhandled `500`.

### Persistence Failure

- The service method is transactional.
- A failed update or deletion is rolled back.

## Business Rules

- Quantity must be between `0` and `99`.
- At least one of quantity, item note, or customizations must be supplied.
- Omitted fields remain unchanged.
- An option submitted with quantity `0` removes that option but the final selection must still satisfy required groups.
- An empty customization list removes all customizations only when all linked groups are optional (`min_select = 0`).
- Quantity `0` means delete; it is never stored in `cart_item`.
- Customization quantity `0` removes that selected option and is never persisted.
- Stored quantities remain between `1` and `99`, matching the database constraint.
- A customer can modify only an item in their own active cart.
- Client-provided prices are not accepted.
- Totals use the stored unit-price snapshot.
- Cart totals include customization price snapshots.
- The shared `CartMapper` is used by add-cart and update-cart response flows.
- JPA entities map to the mainline PostgreSQL `foodland` schema.

## Success Result

The customer receives the updated cart, its remaining items, and the recalculated total.
