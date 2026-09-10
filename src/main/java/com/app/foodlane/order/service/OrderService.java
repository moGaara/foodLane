package com.app.foodlane.order.service;

import com.app.foodlane.Auth.entity.CustomerAddress;
import com.app.foodlane.Auth.repository.CustomerAddressRepository;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.entity.CartStatus;
import com.app.foodlane.cart.repository.CartRepository;
import com.app.foodlane.order.dto.PaymentMethod;
import com.app.foodlane.order.dto.PaymentResponse;
import com.app.foodlane.order.dto.PlaceOrderRequest;
import com.app.foodlane.order.dto.PlaceOrderResponse;
import com.app.foodlane.order.entity.*;
import com.app.foodlane.order.repository.*;
import com.app.foodlane.payment.entity.Payment;
import com.app.foodlane.payment.repository.PaymentRepository;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.exceptionhandling.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final BigDecimal SERVICE_FEE = new BigDecimal("0.070");

    private final CartRepository cartRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final OrderRepository orderRepository;
    private final OrderDeliveryAddressRepository orderDeliveryAddressRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Transactional
    public PlaceOrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {
        Cart cart = cartRepository.findByCustomerCustomerIdAndStatus(customerId, CartStatus.ACTIVE.name())
                .orElseThrow(() -> new BusinessException(ErrorMapping.ACTIVE_CART_NOT_FOUND));

        if (cart.getCartItemsList().isEmpty()) {
            throw new BusinessException(ErrorMapping.EMPTY_CART);
        }
        if (!Boolean.TRUE.equals(cart.getRestaurant().getIsOpen())) {
            throw new BusinessException(ErrorMapping.RESTAURANT_CLOSED);
        }

        CustomerAddress address = customerAddressRepository
                .findByAddressIdAndCustomerCustomerId(request.deliveryAddressId(), customerId)
                .orElseThrow(() -> new BusinessException(ErrorMapping.DELIVERY_ADDRESS_NOT_FOUND));

        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal deliveryFee = BigDecimal.ZERO;
        BigDecimal serviceFee = SERVICE_FEE;
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(deliveryFee).add(serviceFee);

        OrderStatus pendingStatus = orderStatusRepository.findByCode("PENDING")
                .orElseThrow(() -> new IllegalStateException("PENDING order status is not configured"));
        LocalDateTime now = LocalDateTime.now();
        Order order = orderRepository.save(Order.builder()
                .customer(cart.getCustomer())
                .restaurant(cart.getRestaurant())
                .currentStatus(pendingStatus)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .deliveryFee(deliveryFee)
                .serviceFee(serviceFee)
                .totalAmount(totalAmount)
                .placedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build());

        createOrderItems(cart, order);
        createDeliveryAddressSnapshot(address, order);
        Payment payment = paymentRepository.save(createPayment(order, request.paymentMethod(), totalAmount));
        createInitialStatusHistory(order, pendingStatus, now);

        cart.setStatus(CartStatus.CHECKED_OUT.name());
        cartRepository.save(cart);

        return toResponse(order, payment);
    }

    private void createOrderItems(Cart cart, Order order) {
        for (CartItem cartItem : cart.getCartItemsList()) {
            List<OrderCustomizationSnapshot> snapshots = cartItem.getCartItemCustomizations().stream()
                    .map(customization -> new OrderCustomizationSnapshot(
                            customization.getCustomizationOption().getCustomizationOptionId(),
                            customization.getCustomizationOption().getName(),
                            customization.getPriceSnapshot(),
                            customization.getQuantity()))
                    .toList();

            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .menuItem(cartItem.getMenuItem())
                    .itemNameSnapshot(cartItem.getMenuItem().getName())
                    .unitPriceSnapshot(cartItem.getUnitPriceSnapshot())
                    .quantity(cartItem.getQuantity())
                    .customizationsSnapshot(snapshots)
                    .itemNote(cartItem.getItemNote())
                    .build());
        }
    }

    private void createDeliveryAddressSnapshot(CustomerAddress address, Order order) {
        orderDeliveryAddressRepository.save(OrderDeliveryAddress.builder()
                .order(order)
                .buildingName(address.getBuildingName())
                .streetAddress(address.getStreetAddress())
                .contactPhone(address.getContactPhone())
                .build());
    }

    private Payment createPayment(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
        return Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod.name())
                .paymentStatus("PENDING")
                .amount(amount)
                .build();
    }

    private void createInitialStatusHistory(Order order, OrderStatus status, LocalDateTime createdAt) {
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .notes("Order created")
                .createdAt(createdAt)
                .build());
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getCartItemsList().stream()
                .map(this::calculateCartItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCartItemTotal(CartItem item) {
        BigDecimal customizationTotal = item.getCartItemCustomizations().stream()
                .map(customization -> customization.getPriceSnapshot()
                        .multiply(BigDecimal.valueOf(customization.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return item.getUnitPriceSnapshot().add(customizationTotal)
                .multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private PlaceOrderResponse toResponse(Order order, Payment payment) {
        return new PlaceOrderResponse(
                order.getId(),
                order.getCurrentStatus().getCode(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getDeliveryFee(),
                order.getServiceFee(),
                order.getTotalAmount(),
                new PaymentResponse(payment.getPaymentMethod(), payment.getPaymentStatus()),
                order.getPlacedAt());
    }
}
