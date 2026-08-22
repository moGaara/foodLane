# Update Cart Item — Flowchart

```mermaid
flowchart TD
    A([Start]) --> B[Customer selects item in active cart]
    B --> C[Submit cart item update]
    C --> D{Cart exists?}

    D -->|No| E[Return 404 Cart Not Found]
    D -->|Yes| F{Cart belongs to customer?}

    F -->|No| G[Return authorization error]
    F -->|Yes| H{Cart editable?}

    H -->|No| I[Return Cart Not Editable error]
    H -->|Yes| J{Cart item exists?}

    J -->|No| K[Return 404 Cart Item Not Found]
    J -->|Yes| L{Quantity valid?}

    L -->|No| M[Return validation error]
    L -->|Yes| N{Item available?}

    N -->|No| O[Return Item Unavailable error]
    N -->|Yes| P{Requested quantity available?}

    P -->|No| Q[Return Insufficient Availability error]
    P -->|Yes| R{Customization valid?}

    R -->|No| S[Return Customization validation error]
    R -->|Yes| T[Update Cart Item]

    T --> U[Recalculate cart subtotal and total]
    U --> V{Persist update successful?}

    V -->|No| W[Rollback / keep previous cart state]
    W --> X[Return server error]

    V -->|Yes| Y[Return updated cart]
    Y --> Z([End])

    E --> Z
    G --> Z
    I --> Z
    K --> Z
    M --> Z
    O --> Z
    Q --> Z
    S --> Z
    X --> Z
```

## Flow Notes

- **Quantity = 0** is rejected here. The existing Remove Item use case should be used to remove an item.
- Availability is checked before persisting an increased quantity.
- The cart is recalculated only after validation succeeds.
- Persistence failure must not leave a partially updated cart.
