# Update Cart Item — Sequence Diagram

```mermaid
sequenceDiagram
    actor Customer
    participant Client
    participant CartService
    participant MenuService
    participant CartRepository

    Customer->>Client: Edit cart item
    Customer->>Client: Change quantity/customization/note

    Client->>CartService: PATCH /carts/{cartId}/items/{cartItemId}

    CartService->>CartService: Authenticate customer
    CartService->>CartRepository: Find cart
    CartRepository-->>CartService: Cart / not found

    alt Cart not found
        CartService-->>Client: 404 Cart Not Found
        Client-->>Customer: Show error
    else Cart found
        CartService->>CartService: Verify cart ownership

        alt Cart belongs to another customer
            CartService-->>Client: 403 Forbidden
            Client-->>Customer: Show authorization error
        else Cart belongs to customer
            CartService->>CartService: Verify cart is editable

            alt Cart is not editable
                CartService-->>Client: 409 Cart Not Editable
                Client-->>Customer: Show error
            else Cart is editable
                CartService->>CartRepository: Find cart item
                CartRepository-->>CartService: Cart item / not found

                alt Cart item not found
                    CartService-->>Client: 404 Cart Item Not Found
                    Client-->>Customer: Show error
                else Cart item found
                    CartService->>CartService: Validate quantity

                    alt Invalid quantity
                        CartService-->>Client: 400 Invalid Quantity
                        Client-->>Customer: Show validation error
                    else Quantity valid
                        CartService->>MenuService: Check item availability
                        MenuService-->>CartService: Availability result

                        alt Item unavailable
                            CartService-->>Client: 409 Item Unavailable
                            Client-->>Customer: Show error
                        else Item available
                            CartService->>MenuService: Validate customization
                            MenuService-->>CartService: Customization result

                            alt Customization invalid
                                CartService-->>Client: 400 Invalid Customization
                                Client-->>Customer: Show validation error
                            else Customization valid
                                CartService->>CartService: Update cart item
                                CartService->>CartService: Recalculate totals
                                CartService->>CartRepository: Save updated cart

                                alt Persistence failure
                                    CartRepository-->>CartService: Save failed
                                    CartService->>CartService: Rollback update
                                    CartService-->>Client: 500 Internal Server Error
                                    Client-->>Customer: Show error
                                else Save successful
                                    CartRepository-->>CartService: Updated cart
                                    CartService-->>Client: 200 OK + Updated Cart
                                    Client-->>Customer: Display updated cart
                                end
                            end
                        end
                    end
                end
            end
        end
    end
```

## Sequence Notes

1. The service verifies ownership before modifying any cart data.
2. Quantity validation happens before the update.
3. Availability is checked especially when the requested quantity increases.
4. Customization validation is performed when customization data is included.
5. Totals are recalculated after the item has been validated.
6. The persistence operation should be transactional so a failed save does not leave a partially modified cart.
