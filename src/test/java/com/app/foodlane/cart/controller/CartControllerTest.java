package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.service.IDeleteCartItemService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.exceptionhandling.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDeleteCartItemService deleteCartItemService;

    private static final String AUTH_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjdXN0b21lcklEIjoxfQ.Ub1uXsTzSaOlU9vtoKEbws0KxwSDCASgZpmMIRAtwq0";
    private static final long CUSTOMER_ID = 1L;
    private static final long CART_ITEM_ID = 2L;

    @Test
    @DisplayName("DELETE /cart-item/{id} - Should return 200 OK when item is deleted")
    void removeCartItem_WhenValidRequest_Returns200Ok() throws Exception {
        try (MockedStatic<CommonFunctions> mockedCommonFunctions = mockStatic(CommonFunctions.class)) {
            // Arrange
            mockedCommonFunctions.when(() -> CommonFunctions.extractID(AUTH_TOKEN)).thenReturn(CUSTOMER_ID);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/cart/cart-item/{cartItemId}", CART_ITEM_ID)
                            .header("Authorization", AUTH_TOKEN))
                    .andExpect(status().isOk());

            // Verify delegation to service layer
            verify(deleteCartItemService).deleteItem(CUSTOMER_ID, CART_ITEM_ID);
        }
    }

    @Test
    @DisplayName("DELETE /cart-item/{id} - Should pass exception to advice when service fails")
    void removeCartItem_WhenServiceThrows_ReturnsErrorStatus() throws Exception {
        try (MockedStatic<CommonFunctions> mockedCommonFunctions = mockStatic(CommonFunctions.class)) {
            // Arrange
            mockedCommonFunctions.when(() -> CommonFunctions.extractID(AUTH_TOKEN)).thenReturn(CUSTOMER_ID);
            doThrow(new BusinessException(ErrorMapping.CART_ITEM_NOT_EXIST))
                    .when(deleteCartItemService).deleteItem(CUSTOMER_ID, CART_ITEM_ID);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/cart/cart-item/{cartItemId}", CART_ITEM_ID)
                            .header("Authorization", AUTH_TOKEN))
                    .andExpect(status().is4xxClientError()); // Handled by GlobalExceptionHandler
        }
    }
}