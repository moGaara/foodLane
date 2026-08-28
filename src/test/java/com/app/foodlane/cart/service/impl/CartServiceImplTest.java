package com.app.foodlane.cart.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.foodlane.cart.dto.request.CartItemCustomizationRequest;
import com.app.foodlane.cart.dto.request.UpdateCartItemRequest;
import com.app.foodlane.cart.dto.response.CartResponse;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.mapper.CartMapper;
import com.app.foodlane.cart.repository.CartItemCustomizationRepository;
import com.app.foodlane.cart.repository.CartItemRepository;
import com.app.foodlane.cart.repository.CustomizationOptionRepository;
import com.app.foodlane.restaurant.entity.CustomizationGroup;
import com.app.foodlane.restaurant.entity.CustomizationOption;
import com.app.foodlane.restaurant.entity.MenuItem;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.exceptionhandling.BusinessException;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartItemCustomizationRepository cartItemCustomizationRepository;
    @Mock
    private CustomizationOptionRepository customizationOptionRepository;
    @Mock
    private CartMapper cartMapper;
    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private CartItem cartItem;
    private MenuItem menuItem;
    private CartResponse expectedResponse;

    @BeforeEach
    void setup() {
        menuItem = MenuItem.builder().menuItemId(1L).inventoryQuantity(10).build();
        cart = Cart.builder().cartId(1L).build();
        cartItem = CartItem.builder()
                .cartItemId(5L)
                .cart(cart)
                .menuItem(menuItem)
                .quantity(2)
                .unitPriceSnapshot(new BigDecimal("12.50"))
                .build();
        expectedResponse = CartResponse.builder().cartId(1L).build();
    }

    @Test
    void updateCartItem_shouldUpdateQuantity() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(4);
        mockExistingCartItem();
        when(cartMapper.toCartResponse(cart)).thenReturn(expectedResponse);

        CartResponse response = cartService.updateCartItem(1L, 5L, 1L, request);

        assertEquals(4, cartItem.getQuantity());
        assertSame(expectedResponse, response);
        verify(cartItemRepository).save(cartItem);
        verify(cartMapper).toCartResponse(cart);
    }

    @Test
    void updateCartItem_whenQuantityIsZero_shouldDeleteItem() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);
        mockExistingCartItem();
        when(cartMapper.toCartResponse(cart)).thenReturn(expectedResponse);

        CartResponse result = cartService.updateCartItem(1L, 5L, 1L, request);

        verify(cartItemRepository).delete(cartItem);
        verify(cartItemRepository).flush();
        verify(cartItemRepository, never()).save(any());
        assertSame(expectedResponse, result);
    }

    @Test
    void updateCartItem_whenItemDoesNotExist_shouldThrowBusinessException() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(2);
        when(cartItemRepository
                .findByCartItemIdAndCartCartIdAndCartCustomerCustomerIdAndCartStatus(
                        99L, 1L, 1L, "ACTIVE"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateCartItem(1L, 99L, 1L, request));

        assertEquals(ErrorMapping.CART_ITEM_NOT_EXIST.getCode(), exception.getCode());
        verify(cartItemRepository, never()).save(any());
        verifyNoInteractions(cartMapper);
    }

    @Test
    void updateCartItem_whenQuantityExceedsInventory_shouldThrowBusinessException() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(11);
        mockExistingCartItem();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateCartItem(1L, 5L, 1L, request));

        assertEquals(ErrorMapping.INSUFFICIENT_INVENTORY.getCode(), exception.getCode());
        assertEquals(2, cartItem.getQuantity());
        verify(cartItemRepository, never()).save(any());
        verifyNoInteractions(cartMapper);
    }

    @Test
    void updateCartItem_shouldUpdateItemNote() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setItemNote("Extra sauce");
        mockExistingCartItem();
        when(cartMapper.toCartResponse(cart)).thenReturn(expectedResponse);

        CartResponse result = cartService.updateCartItem(1L, 5L, 1L, request);

        assertEquals("Extra sauce", cartItem.getItemNote());
        assertSame(expectedResponse, result);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateCartItem_whenOptionalCustomizationQuantityIsZero_shouldRemoveIt() {
        CustomizationGroup optionalGroup = optionalCustomizationGroup();
        CustomizationOption bacon = CustomizationOption.builder()
                .customizationOptionId(3L)
                .customizationGroup(optionalGroup)
                .price(new BigDecimal("2.00"))
                .build();
        menuItem.setCustomizationGroups(Set.of(optionalGroup));

        CartItemCustomizationRequest customization = new CartItemCustomizationRequest();
        customization.setCustomizationOptionId(3L);
        customization.setQuantity(0);
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setCustomizations(List.of(customization));

        mockExistingCartItem();
        when(customizationOptionRepository.findAllById(Set.of(3L)))
                .thenReturn(List.of(bacon));
        when(cartMapper.toCartResponse(cart)).thenReturn(expectedResponse);

        CartResponse result = cartService.updateCartItem(1L, 5L, 1L, request);

        verify(cartItemCustomizationRepository).deleteAllByCartItemCartItemId(5L);
        verify(cartItemCustomizationRepository).flush();
        verify(cartItemCustomizationRepository).saveAll(List.of());
        assertSame(expectedResponse, result);
    }

    @Test
    void updateCartItem_whenAllGroupsAreOptionalAndListIsEmpty_shouldRemoveAllCustomizations() {
        menuItem.setCustomizationGroups(Set.of(optionalCustomizationGroup()));
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setCustomizations(List.of());

        mockExistingCartItem();
        when(customizationOptionRepository.findAllById(Set.of())).thenReturn(List.of());
        when(cartMapper.toCartResponse(cart)).thenReturn(expectedResponse);

        CartResponse result = cartService.updateCartItem(1L, 5L, 1L, request);

        verify(cartItemCustomizationRepository).deleteAllByCartItemCartItemId(5L);
        verify(cartItemCustomizationRepository).flush();
        verify(cartItemCustomizationRepository).saveAll(List.of());
        verify(cartItemRepository).save(cartItem);
        assertSame(expectedResponse, result);
    }

    private CustomizationGroup optionalCustomizationGroup() {
        return CustomizationGroup.builder()
                .customizationGroupId(2L)
                .name("Extra Toppings")
                .minSelect(0)
                .maxSelect(3)
                .build();
    }

    private void mockExistingCartItem() {
        when(cartItemRepository
                .findByCartItemIdAndCartCartIdAndCartCustomerCustomerIdAndCartStatus(
                        5L, 1L, 1L, "ACTIVE"))
                .thenReturn(Optional.of(cartItem));
    }
}
