package com.app.foodlane.cart.service.impl;

import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.exceptionhandling.CartNotFoundException;
import com.app.foodlane.cart.repo.CartItemRepository;
import com.app.foodlane.cart.repo.CartRepository;
import com.app.foodlane.cart.service.CartService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    Logger logger = Logger.getLogger(CartServiceImpl.class.getName());

    @Override
    public List<CartItem> viewCart(Long cartId, Long CustomerId)
    {

        Cart cart = getCart(cartId);
        checkCartOwnership(cart,CustomerId);
        checkCartStatus(cart,cartId);

        return cartItemRepository.findyByCart_Id(cartId);

    }


    @Override
    public void clearCart(Long cartId, Long CustomerId)
    {

        Cart cart = getCart(cartId);
        checkCartOwnership(cart,CustomerId);
        checkCartStatus(cart,cartId);

        cartRepository.deleteById(cartId);
    }

    public void checkCartStatus(Cart cart, Long cartId)
    {
        logger.info("Checking if the cart is active ");
        if(!cart.getStatus().equals("ACTIVE"))
        {
            logger.warning("Cart is not active");
            throw new RuntimeException(
                    "Cart is not active");
        }
        logger.info("Cart is active: " + cartId);
    }

    private void checkCartOwnership(Cart cart, Long CustomerId)
    {

        logger.info("Checking if the User owns the cart");
        if(!cart.getCustomer().getCustomerId().equals(CustomerId))
        {
            logger.warning("User does not own the cart");
            throw new RuntimeException(
                    "User does not own the cart");
        }
    }

    private Cart getCart(Long cartId)
    {
        logger.info("Fetching cart from DB");
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));
        logger.info("Cart found with id: " + cartId );
        return cart;
    }
}
