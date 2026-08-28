package com.app.foodlane.cart.service.impl;

import com.app.foodlane.cart.entity.CartStatus;
import com.app.foodlane.cart.repository.CartItemRepository;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.exceptionhandling.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private DeleteCartItemServiceImpl deleteCartItemService;

    private static final Long CUSTOMER_ID = 100L;
    private static final Long CART_ITEM_ID = 50L;

    @Test
    @DisplayName("Should delete cart item successfully when item exists in active cart")
    void deleteItem_WhenItemExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(cartItemRepository.deleteByCartItemIdAndCustomerId(
                CART_ITEM_ID, CUSTOMER_ID, CartStatus.ACTIVE.name()))
                .thenReturn(1);

        // Act & Assert
        assertThatNoException().isThrownBy(() ->
                deleteCartItemService.deleteItem(CUSTOMER_ID, CART_ITEM_ID)
        );

        // Verify side-effect
        verify(cartItemRepository).deleteByCartItemIdAndCustomerId(
                CART_ITEM_ID, CUSTOMER_ID, CartStatus.ACTIVE.name()
        );
    }

    @Test
    @DisplayName("Should throw BusinessException when cart item does not exist or cart is inactive")
    void deleteItem_WhenItemDoesNotExist_ShouldThrowBusinessException() {
        // Arrange
        when(cartItemRepository.deleteByCartItemIdAndCustomerId(
                CART_ITEM_ID, CUSTOMER_ID, CartStatus.ACTIVE.name()))
                .thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> deleteCartItemService.deleteItem(CUSTOMER_ID, CART_ITEM_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorMapping.CART_ITEM_NOT_EXIST.getCode())
                .hasFieldOrPropertyWithValue("desc", ErrorMapping.CART_ITEM_NOT_EXIST.getDesc());

        verify(cartItemRepository).deleteByCartItemIdAndCustomerId(
                CART_ITEM_ID, CUSTOMER_ID, CartStatus.ACTIVE.name()
        );
    }
}