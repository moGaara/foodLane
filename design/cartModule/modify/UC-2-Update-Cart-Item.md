# UC-2 — Update Cart Item

## Use Case

**Name:** Update Cart Item  
**Actor:** Customer  
**Goal:** Allow a customer to modify an existing item in their active cart, including its quantity and, where applicable, customization/note information.

## Preconditions

- The customer is authenticated.
- The customer has an active cart.
- The target cart item exists in the active cart.
- The cart is still editable and has not been converted into an order.

## Main Flow

1. Customer opens the active cart.
2. Customer selects a cart item to edit.
3. Customer changes the desired quantity and/or editable item information.
4. Client sends an update request for the selected cart item.
5. System authenticates the customer and verifies ownership of the cart.
6. System verifies that the cart item belongs to the specified cart.
7. System validates the requested quantity.
8. System verifies that the menu item is still available.
9. System validates the requested customization, if customization is being changed.
10. System updates the cart item.
11. System recalculates the cart subtotal/total using the applicable current item and customization prices.
12. System persists the updated cart.
13. System returns the updated cart.

## Alternative / Exception Flows

### A1 — Invalid Quantity

- Customer submits a quantity less than 1 or otherwise outside the allowed quantity range.
- System rejects the update.
- System does not modify the cart.
- System returns a validation error.

### A2 — Quantity Exceeds Availability

- Requested quantity is greater than the currently available quantity.
- System rejects the update.
- System does not modify the cart.
- System returns an availability/stock error.

### A3 — Cart Not Found

- The specified cart does not exist or is not available to the customer.
- System rejects the request.
- System returns `404 Not Found`.

### A4 — Cart Does Not Belong to Customer

- The cart exists but is owned by another customer.
- System rejects the request.
- System returns an authorization error.
- No cart data is modified.

### A5 — Cart Item Not Found

- The specified cart item does not exist in the cart.
- System rejects the request.
- System returns `404 Not Found`.

### A6 — Menu Item Unavailable

- The menu item has become unavailable after it was added to the cart.
- System rejects the update.
- System informs the customer that the item is no longer available.

### A7 — Invalid Customization

- Customer submits an invalid, unavailable, or incomplete customization.
- System rejects the update.
- Existing cart item remains unchanged.

### A8 — Cart Is No Longer Editable

- The cart has already been submitted/converted to an order or is otherwise locked.
- System rejects the update.
- No cart data is modified.

### A9 — Persistence Failure

- Validation succeeds but the cart cannot be persisted.
- System rolls back the cart modification.
- System returns a server/database error.
- Customer retains the previous cart state.

## Business Rules

- An update must never create a negative or zero quantity cart item.
- Quantity validation must happen before modifying persistent cart data.
- Availability must be checked before accepting an increased quantity.
- The cart total must be recalculated after a successful update.
- A failed update must not partially modify the cart.
- A customer may modify only their own active cart.
- Updating quantity to zero is treated as an invalid quantity in this use case; removing an item is handled by the cart's Remove Item use case.

## Success Result

The customer receives the updated cart, including the modified cart item and recalculated totals.
