package com.app.foodlane.order.service;

import com.app.foodlane.Auth.entity.CustomerAddress;
import com.app.foodlane.Auth.entity.User;
import com.app.foodlane.Auth.entity.UserRole;
import com.app.foodlane.Auth.repository.CustomerAddressRepository;
import com.app.foodlane.Auth.repository.UserRepository;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.entity.CartStatus;
import com.app.foodlane.cart.repository.CartRepository;
import com.app.foodlane.order.dto.OrderStatusHistoryResponse;
import com.app.foodlane.order.dto.PaymentMethod;
import com.app.foodlane.order.dto.PaymentResponse;
import com.app.foodlane.order.dto.PlaceOrderRequest;
import com.app.foodlane.order.dto.PlaceOrderResponse;
import com.app.foodlane.order.dto.TrackOrderStatusResponse;
import com.app.foodlane.order.dto.UpdateOrderStatusRequest;
import com.app.foodlane.order.entity.*;
import com.app.foodlane.order.repository.*;
import com.app.foodlane.payment.entity.Payment;
import com.app.foodlane.payment.repository.PaymentRepository;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.exceptionhandling.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
        private static final BigDecimal SERVICE_FEE = new BigDecimal("0.070");

        private final CartRepository cartRepository;
        private final CustomerAddressRepository customerAddressRepository;
        private final OrderRepository orderRepository;
        private final OrderDeliveryAddressRepository orderDeliveryAddressRepository;
        private final PaymentRepository paymentRepository;
        private final OrderStatusRepository orderStatusRepository;
        private final OrderStatusHistoryRepository orderStatusHistoryRepository;
        private final UserRepository userRepository;

        private static final Map<String, Set<String>> ALLOWED_STATUS_TRANSITIONS = Map.of(
                        "PENDING", Set.of("ACCEPTED", "CANCELLED"),
                        "ACCEPTED", Set.of("PREPARING", "CANCELLED"),
                        "PREPARING", Set.of("READY_FOR_PICKUP"),
                        "READY_FOR_PICKUP", Set.of("OUT_FOR_DELIVERY"),
                        "OUT_FOR_DELIVERY", Set.of("DELIVERED"),
                        "DELIVERED", Set.of(),
                        "CANCELLED", Set.of());

        private static final Set<String> RESTAURANT_TARGET_STATUSES = Set.of(
                        "ACCEPTED",
                        "PREPARING",
                        "READY_FOR_PICKUP",
                        "CANCELLED");

        private static final Set<String> COURIER_TARGET_STATUSES = Set.of(
                        "OUT_FOR_DELIVERY",
                        "DELIVERED");

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
                createDeliveryAddressSnapshot(address, order, request.deliveryInstructions());
                Payment payment = paymentRepository.save(createPayment(order, request.paymentMethod(), totalAmount));
                createInitialStatusHistory(order, pendingStatus, now);

                cart.setStatus(CartStatus.CHECKED_OUT.name());
                cartRepository.save(cart);

                return toResponse(order, payment);
        }

        @Transactional
        public PlaceOrderResponse updateOrderStatus(
                        Long orderId,
                        Long userId,
                        UserRole role,
                        UpdateOrderStatusRequest request) {
                log.info("Updating status for orderId={}", orderId);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> {
                                        log.warn("User not found for status update: userId={}", userId);
                                        return new BusinessException(ErrorMapping.USER_NOT_FOUND);
                                });

                if (!Boolean.TRUE.equals(user.getActive()) || user.getRole() != role) {
                        log.warn(
                                        "Rejected status update identity: userId={}, suppliedRole={}, actualRole={}",
                                        userId,
                                        role,
                                        user.getRole());
                        throw new BusinessException(ErrorMapping.ORDER_STATUS_UPDATE_FORBIDDEN);
                }

                Order order = findOrder(orderId);

                String requestCode = request.status().trim().toUpperCase(Locale.ROOT);
                OrderStatus reqStatus = findOrderStatus(requestCode);
                validateStatusUpdateActor(order, user, role, requestCode);
                if (!isTransitionAllowed(order.getCurrentStatus(), reqStatus)) {
                        log.warn("Rejected status transition for orderId={}: {} -> {}",
                                        orderId, order.getCurrentStatus().getCode(), requestCode);
                        throw new BusinessException(ErrorMapping.INVALID_ORDER_STATUS_TRANSITION);
                }

                LocalDateTime now = LocalDateTime.now();
                order.setCurrentStatus(reqStatus);
                order.setUpdatedAt(now);
                orderRepository.save(order);

                orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                                .order(order)
                                .status(reqStatus)
                                .changedBy(user)
                                .notes("Status updated to " + requestCode)
                                .createdAt(now)
                                .build());

                log.info("Updated orderId={} status to {} and recorded status history", orderId, requestCode);
                return toResponse(order, order.getPayment());
        }

        private void createOrderItems(Cart cart, Order order) {
                for (CartItem cartItem : cart.getCartItemsList()) {
                        List<OrderCustomizationSnapshot> snapshots = cartItem.getCartItemCustomizations().stream()
                                        .map(customization -> new OrderCustomizationSnapshot(
                                                        customization.getCustomizationOption()
                                                                        .getCustomizationOptionId(),
                                                        customization.getCustomizationOption().getName(),
                                                        customization.getPriceSnapshot(),
                                                        customization.getSelected()))
                                        .toList();

                        order.getItems().add(OrderItem.builder()
                                        .order(order)
                                        .menuItem(cartItem.getMenuItem())
                                        .itemNameSnapshot(cartItem.getMenuItem().getName())
                                        .unitPriceSnapshot(cartItem.getUnitPriceSnapshot())
                                        .quantity(cartItem.getSelected())
                                        .customizationsSnapshot(snapshots)
                                        .itemNote(cartItem.getItemNote())
                                        .build());
                }
        }

        private void createDeliveryAddressSnapshot(
                        CustomerAddress address,
                        Order order,
                        String deliveryInstructions) {
                orderDeliveryAddressRepository.save(OrderDeliveryAddress.builder()
                                .order(order)
                                .buildingName(address.getBuildingName())
                                .streetAddress(address.getStreetAddress())
                                .contactPhone(address.getContactPhone())
                                .deliveryInstructions(deliveryInstructions)
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
                                                .multiply(BigDecimal.valueOf(customization.getSelected())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                return item.getUnitPriceSnapshot().add(customizationTotal)
                                .multiply(BigDecimal.valueOf(item.getSelected()));
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

        private boolean isTransitionAllowed(
                        OrderStatus currentStatus,
                        OrderStatus requestedStatus) {
                return ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus.getCode(), Set.of())
                                .contains(requestedStatus.getCode());
        }

        private void validateStatusUpdateActor(
                        Order order,
                        User user,
                        UserRole role,
                        String requestedStatus) {
                if (role == UserRole.COURIER) {
                        boolean courierStatus = COURIER_TARGET_STATUSES.contains(requestedStatus);
                        boolean assignedCourier = order.getCourier() != null
                                        && order.getCourier().getId().equals(user.getId());

                        if (!courierStatus || !assignedCourier) {
                                log.warn(
                                                "Rejected courier status update: orderId={}, userId={}, requestedStatus={}",
                                                order.getId(),
                                                user.getId(),
                                                requestedStatus);
                                throw new BusinessException(ErrorMapping.ORDER_STATUS_UPDATE_FORBIDDEN);
                        }
                        return;
                }

                if (role == UserRole.RESTAURANT_OWNER) {
                        // TODO Replace this role-only check with restaurant ownership validation
                        // when authentication provides the user's restaurant relationship.
                        if (!RESTAURANT_TARGET_STATUSES.contains(requestedStatus)) {
                                log.warn(
                                                "Rejected restaurant status update: orderId={}, userId={}, requestedStatus={}",
                                                order.getId(),
                                                user.getId(),
                                                requestedStatus);
                                throw new BusinessException(ErrorMapping.ORDER_STATUS_UPDATE_FORBIDDEN);
                        }
                        return;
                }

                log.warn(
                                "Rejected status update role: orderId={}, userId={}, role={}",
                                order.getId(),
                                user.getId(),
                                role);
                throw new BusinessException(ErrorMapping.ORDER_STATUS_UPDATE_FORBIDDEN);
        }

        private Order findOrder(Long orderId) {
                return orderRepository.findById(orderId)
                                .orElseThrow(() -> {
                                        log.warn("Order not found for status update: orderId={}", orderId);
                                        return new BusinessException(ErrorMapping.ORDER_NOT_FOUND);
                                });
        }

        private OrderStatus findOrderStatus(String orderStatus) {
                return orderStatusRepository.findByCode(orderStatus)
                                .orElseThrow(() -> {
                                        log.warn("Unknown order status code requested: {}", orderStatus);
                                        return new BusinessException(ErrorMapping.ORDER_STATUS_NOT_FOUND);
                                });
        }

        @Transactional
        public TrackOrderStatusResponse trackOrderStatus(Long orderId, Long customerId) {
                log.info(
                                "Tracking order: orderId={}, customerId={}",
                                orderId,
                                customerId);
                Order order = orderRepository.findByIdAndCustomerCustomerId(orderId, customerId).orElseThrow(() -> {
                        log.warn(
                                        "Order not found for customer: orderId={}, customerId={}",
                                        orderId,
                                        customerId);
                        return new BusinessException(ErrorMapping.ORDER_NOT_FOUND);
                });

                List<OrderStatusHistoryResponse> history = orderStatusHistoryRepository
                                .findByOrderOrderByCreatedAtAsc(order).stream()
                                .map(historyItem -> new OrderStatusHistoryResponse(historyItem.getStatus().getCode(),
                                                historyItem.getCreatedAt()))
                                .toList();
                String deliveryInstructions = order.getDeliveryAddress() == null ? null
                                : order.getDeliveryAddress().getDeliveryInstructions();

                return new TrackOrderStatusResponse(
                                order.getId(),
                                order.getCurrentStatus().getCode(),
                                order.getEstimatedDelivery(),
                                deliveryInstructions,
                                history);

        }
}
