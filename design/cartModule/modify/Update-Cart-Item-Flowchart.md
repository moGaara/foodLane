# Update Cart Item — Flowchart

```mermaid
flowchart TD
    A([PATCH cart-item request]) --> B{Authorization header present?}
    B -->|No| C[Return 400]
    B -->|Yes| D[Extract customerID]
    D --> E{At least one valid editable field?}
    E -->|No| F[Return 400 validation error]
    E -->|Yes| G[Find item by item ID, cart ID, customer ID and ACTIVE status]
    G --> H{Item found?}
    H -->|No| I[Raise CartItemNotFoundException]
    H -->|Yes| J{Quantity equals 0?}
    J -->|Yes| K[Delete cart item and flush]
    J -->|No| L{Provided quantity within inventory?}
    L -->|No| M[Raise insufficient inventory error]
    L -->|Yes| N[Update provided quantity and note]
    N --> T{Customizations provided?}
    T -->|Yes| U[Validate and replace customizations]
    T -->|No| O
    U --> O
    K --> O[Load remaining cart items]
    O --> P[Map items and calculate item totals]
    P --> Q[Calculate cart total]
    Q --> R[Return 200 with updated cart]
    R --> S([End])
    C --> S
    F --> S
    I --> S
    M --> S
```

## Flow Notes

- Quantity `0` deletes the item; it is never saved as a database quantity.
- Quantities `1–99` update the item after inventory validation.
- Omitted fields remain unchanged.
- Provided customizations replace previous selections after option and group validation.
- A customization with quantity `0` is removed instead of being stored.
- The repository lookup enforces cart ownership and `ACTIVE` status.
- The returned cart excludes a deleted item and includes recalculated totals.
- The operation runs in one transaction.
