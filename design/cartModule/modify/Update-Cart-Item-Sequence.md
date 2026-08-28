# Update Cart Item — Sequence Diagram

```mermaid
sequenceDiagram
    actor Customer
    participant Client
    participant Controller as CartController
    participant Service as CartServiceImpl
    participant Repository as CartItemRepository
    participant Mapper as shared CartMapper
    participant DB as PostgreSQL foodland schema

    Customer->>Client: Edit quantity, note, and/or customizations
    Client->>Controller: PATCH /api/v1/carts/{cartId}/items/{cartItemId}
    Note over Client,Controller: Authorization header and partial JSON body
    Controller->>Controller: Validate request and extract customerID
    Controller->>Service: updateCartItem(cartId, cartItemId, customerId, request)
    Service->>Repository: Find item by item/cart/customer/ACTIVE status
    Repository->>DB: SELECT cart item and ownership relations
    DB-->>Repository: Cart item or empty
    Repository-->>Service: Optional CartItem

    alt Cart item not found
        Service-->>Controller: BusinessException E000004
        Controller-->>Client: Mapped 400 response
    else Quantity equals 0
        Service->>Repository: delete(cartItem) and flush
        Repository->>DB: DELETE cart_item
        Note over DB: Customizations deleted by ON DELETE CASCADE
        Service->>Mapper: toCartResponse(cart)
        Mapper->>Repository: findAllByCartCartId(cartId)
        Repository->>DB: SELECT remaining items
        DB-->>Mapper: Remaining items
        Mapper->>Mapper: Map responses and recalculate total
        Mapper-->>Service: Updated CartResponse
        Service-->>Controller: Updated CartResponse
        Controller-->>Client: 200 OK and GenericRes CartResponse
    else Quantity between 1 and 99
        Service->>Service: Validate menu-item inventory
        alt Quantity exceeds inventory
            Service-->>Controller: BusinessException E000006
            Controller-->>Client: Mapped 400 response
        else Inventory available
            Service->>Service: Apply provided note and validate customizations
            Service->>Repository: Replace provided customizations and save CartItem
            Repository->>DB: UPDATE cart_item quantity
            Service->>Mapper: toCartResponse(cart)
            Mapper->>Repository: findAllByCartCartId(cartId)
            Repository->>DB: SELECT cart items
            DB-->>Mapper: Updated items
            Mapper->>Mapper: Map responses and recalculate total
            Mapper-->>Service: Updated CartResponse
            Service-->>Controller: Updated CartResponse
            Controller-->>Client: 200 OK and GenericRes CartResponse
        end
    end

    Client-->>Customer: Display updated cart
```

## Sequence Notes

1. Ownership is enforced by the repository lookup before modification.
2. Quantity `0` follows the delete branch before inventory validation.
3. Quantities `1–99` are checked against inventory before saving.
4. The shared mapper reloads remaining items and returns the entire updated cart.
5. The transaction rolls back if persistence fails.
6. Omitted request fields remain unchanged; provided customizations replace existing selections.
7. Customization quantity `0` removes that option before the replacement list is saved.
8. An empty customization list removes all selections only when all linked groups are optional.
9. `GlobalExceptionHandler` maps update business errors through `ErrorMapping`.
