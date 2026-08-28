package com.app.foodlane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.repository.CartItemRepository;
import com.app.foodlane.cart.service.impl.CartServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock

    private CartItemRepository cartItemRepository;
    @InjectMocks
    private CartServiceImpl cartServiceImpl;

    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setup(){
         
    }

}
