package com.app.foodlane.order.service;

import com.app.foodlane.Auth.entity.Courier;
import com.app.foodlane.Auth.entity.User;
import com.app.foodlane.Auth.entity.UserRole;
import com.app.foodlane.Auth.repository.CustomerAddressRepository;
import com.app.foodlane.Auth.repository.UserRepository;
import com.app.foodlane.cart.repository.CartRepository;
import com.app.foodlane.order.dto.TrackOrderStatusResponse;
import com.app.foodlane.order.dto.UpdateOrderStatusRequest;
import com.app.foodlane.order.entity.Order;
import com.app.foodlane.order.entity.OrderDeliveryAddress;
import com.app.foodlane.order.entity.OrderStatus;
import com.app.foodlane.order.entity.OrderStatusHistory;
import com.app.foodlane.order.repository.OrderDeliveryAddressRepository;
import com.app.foodlane.order.repository.OrderRepository;
import com.app.foodlane.order.repository.OrderStatusHistoryRepository;
import com.app.foodlane.order.repository.OrderStatusRepository;
import com.app.foodlane.payment.entity.Payment;
import com.app.foodlane.payment.repository.PaymentRepository;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.exceptionhandling.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTrackingTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CustomerAddressRepository customerAddressRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDeliveryAddressRepository orderDeliveryAddressRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderStatusRepository orderStatusRepository;
    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void trackOrderStatusReturnsCurrentStatusInstructionsAndOrderedHistory() {
        LocalDateTime pendingAt = LocalDateTime.of(2026, 9, 20, 12, 0);
        OrderStatus pending = status("PENDING");
        OrderStatus preparing = status("PREPARING");
        Order order = Order.builder()
                .id(1002L)
                .currentStatus(preparing)
                .estimatedDelivery(LocalDateTime.of(2026, 9, 20, 13, 0))
                .build();
        order.setDeliveryAddress(OrderDeliveryAddress.builder()
                .order(order)
                .buildingName("Building A")
                .streetAddress("Main Street")
                .contactPhone("123")
                .deliveryInstructions("Leave at the front door")
                .build());

        List<OrderStatusHistory> history = List.of(
                OrderStatusHistory.builder()
                        .order(order)
                        .status(pending)
                        .createdAt(pendingAt)
                        .build(),
                OrderStatusHistory.builder()
                        .order(order)
                        .status(preparing)
                        .createdAt(pendingAt.plusMinutes(10))
                        .build());

        when(orderRepository.findByIdAndCustomerCustomerId(1002L, 2L))
                .thenReturn(Optional.of(order));
        when(orderStatusHistoryRepository.findByOrderOrderByCreatedAtAsc(order))
                .thenReturn(history);

        TrackOrderStatusResponse response = orderService.trackOrderStatus(1002L, 2L);

        assertEquals(1002L, response.orderId());
        assertEquals("PREPARING", response.currentStatus());
        assertEquals("Leave at the front door", response.deliveryInstructions());
        assertEquals(List.of("PENDING", "PREPARING"),
                response.statusHistory().stream().map(item -> item.status()).toList());
    }

    @Test
    void assignedCourierCanAdvanceDeliveryAndIsRecordedInHistory() {
        User courierUser = user(5L, UserRole.COURIER);
        OrderStatus ready = status("READY_FOR_PICKUP");
        OrderStatus outForDelivery = status("OUT_FOR_DELIVERY");
        Payment payment = Payment.builder()
                .paymentMethod("CASH")
                .paymentStatus("PENDING")
                .amount(BigDecimal.TEN)
                .build();
        Order order = orderForUpdate(1002L, ready, courierUser, payment);

        when(userRepository.findById(5L)).thenReturn(Optional.of(courierUser));
        when(orderRepository.findById(1002L)).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByCode("OUT_FOR_DELIVERY"))
                .thenReturn(Optional.of(outForDelivery));

        orderService.updateOrderStatus(
                1002L,
                5L,
                UserRole.COURIER,
                new UpdateOrderStatusRequest("OUT_FOR_DELIVERY"));

        assertSame(outForDelivery, order.getCurrentStatus());
        ArgumentCaptor<OrderStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository).save(historyCaptor.capture());
        assertSame(courierUser, historyCaptor.getValue().getChangedBy());
    }

    @Test
    void courierNotAssignedToOrderIsRejected() {
        User caller = user(4L, UserRole.COURIER);
        User assignedCourier = user(5L, UserRole.COURIER);
        Order order = orderForUpdate(
                1002L,
                status("READY_FOR_PICKUP"),
                assignedCourier,
                null);

        when(userRepository.findById(4L)).thenReturn(Optional.of(caller));
        when(orderRepository.findById(1002L)).thenReturn(Optional.of(order));
        when(orderStatusRepository.findByCode("OUT_FOR_DELIVERY"))
                .thenReturn(Optional.of(status("OUT_FOR_DELIVERY")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.updateOrderStatus(
                        1002L,
                        4L,
                        UserRole.COURIER,
                        new UpdateOrderStatusRequest("OUT_FOR_DELIVERY")));

        assertEquals(ErrorMapping.ORDER_STATUS_UPDATE_FORBIDDEN.getCode(), exception.getCode());
        verify(orderRepository, never()).save(any());
        verify(orderStatusHistoryRepository, never()).save(any());
    }

    private User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .name("Test User")
                .email("user" + id + "@example.com")
                .passwordHash("hash")
                .role(role)
                .active(true)
                .build();
    }

    private OrderStatus status(String code) {
        return OrderStatus.builder().code(code).description(code).build();
    }

    private Order orderForUpdate(
            Long orderId,
            OrderStatus currentStatus,
            User courierUser,
            Payment payment) {
        Courier courier = Courier.builder()
                .id(courierUser.getId())
                .user(courierUser)
                .vehicleType("CAR")
                .available(true)
                .build();
        return Order.builder()
                .id(orderId)
                .courier(courier)
                .currentStatus(currentStatus)
                .subtotal(BigDecimal.TEN)
                .discountAmount(BigDecimal.ZERO)
                .deliveryFee(BigDecimal.ZERO)
                .serviceFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN)
                .payment(payment)
                .placedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
