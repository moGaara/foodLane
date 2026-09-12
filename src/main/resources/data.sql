SET search_path TO foodland;

-- 1. CUSTOMER
INSERT INTO customer (name, email)
VALUES ('Sarah Ahmed', 'sarah.ahmed@example.com'),
       ('Omar Hassan', 'omar.hassan@example.com'),
       ('Mariam Ali', 'mariam.ali@example.com');

-- 2. RESTAURANT
INSERT INTO restaurant (name, is_open)
VALUES ('Urban Burger', true),
       ('Pizza Bella', true),
       ('Sushi Master', false);

-- 3. MENU (Unique constraint requires uq_menu_restaurant alignment)
INSERT INTO menu (restaurant_id)
VALUES (1), -- Menu for Urban Burger
       (2), -- Menu for Pizza Bella
       (3);
-- Menu for Sushi Master

-- 4. CATEGORY
INSERT INTO category (menu_id, name)
VALUES (1, 'Burgers'),
       (1, 'Sides'),
       (2, 'Pizzas'),
       (2, 'Beverages');

-- 5. MENU ITEM
INSERT INTO menu_item (category_id, name, description, price, image_url, inventory_quantity)
VALUES (1, 'Classic Cheeseburger', 'Angus beef patty with cheddar cheese and fresh veggies', 12.50,
        'https://example.com/images/cheeseburger.jpg', 50),
       (1, 'Double Bacon Burger', 'Double patty with smoked bacon and barbecue sauce', 16.00,
        'https://example.com/images/baconburger.jpg', 30),
       (2, 'Crispy French Fries', 'Golden salted potato fries', 4.50, 'https://example.com/images/fries.jpg', 100),
       (3, 'Margherita Pizza', 'Classic tomato, mozzarella, and basil pizza', 14.00,
        'https://example.com/images/margherita.jpg', 40),
       (4, 'Iced Tea', 'Freshly brewed lemon iced tea', 3.50, 'https://example.com/images/icedtea.jpg', 80);

-- 6. CUSTOMIZATION GROUP
INSERT INTO customization_group (name, required, min_select, max_select)
VALUES ('Choice of Cheese', true, 1, 1),
       ('Extra Toppings', false, 0, 3),
       ('Crust Type', true, 1, 1);

-- 7. CUSTOMIZATION OPTION
INSERT INTO customization_option (customization_group_id, name, price)
VALUES (1, 'Cheddar', 0.00),
       (1, 'Swiss Cheese', 1.00),
       (2, 'Extra Bacon', 2.00),
       (2, 'Jalapenos', 0.75),
       (2, 'Grilled Onions', 0.50),
       (3, 'Thin Crust', 0.00),
       (3, 'Stuffed Crust', 2.50);

-- 8. MENU ITEM ↔ CUSTOMIZATION GROUP
INSERT INTO menu_item_customization_group (menu_item_id, customization_group_id)
VALUES (1, 1), -- Cheeseburger -> Choice of Cheese
       (1, 2), -- Cheeseburger -> Extra Toppings
       (2, 2), -- Double Bacon Burger -> Extra Toppings
       (4, 3);
-- Margherita Pizza -> Crust Type

-- 9. CART
INSERT INTO cart (customer_id, restaurant_id, status)
VALUES (1, 1, 'ACTIVE'),
       (2, 2, 'ACTIVE'),
       (3, 1, 'ABANDONED');

-- 10. CART ITEM
INSERT INTO cart_item (cart_id, menu_item_id, selected, unit_price_snapshot, item_note)
VALUES (1, 1, 2, 12.50, 'No pickles please'),
       (1, 3, 1, 4.50, 'Extra crispy'),
       (2, 4, 1, 14.00, NULL);

-- 11. CART ITEM ↔ SELECTED CUSTOMIZATION
INSERT INTO cart_item_customization (cart_item_id, customization_option_id, price_snapshot, selected)
VALUES (1, 1, 0.00, 1), -- Selected Cheddar for Cheeseburger
       (1, 3, 2.00, 1), -- Selected Extra Bacon for Cheeseburger
       (3, 7, 2.50, 1); -- Selected Stuffed Crust for Pizza

-- 12. APPLICATION USERS USED BY ORDER HISTORY AND COURIERS
INSERT INTO app_user (user_id, name, email, password_hash, role, is_active)
VALUES (1, 'Ahmed Hassan', 'ahmed.hassan@example.com', '$2a$12$hash1', 'CUSTOMER', TRUE),
       (2, 'Sara Al-Busaidi', 'sara.busaidi@example.com', '$2a$12$hash2', 'CUSTOMER', TRUE),
       (3, 'Tariq Al-Balushi', 'tariq.driver@example.com', '$2a$12$hash3', 'COURIER', TRUE),
       (4, 'Khaled Courier', 'khaled.courier@example.com', '$2a$12$hash4', 'COURIER', TRUE),
       (5, 'Restaurant Owner', 'owner@foodland.com', '$2a$12$hash5', 'RESTAURANT_OWNER', TRUE);

SELECT setval('app_user_user_id_seq', (SELECT MAX(user_id) FROM app_user));

-- 13. COURIERS
INSERT INTO courier (courier_id, user_id, phone_number, is_available)
VALUES (1, 3, '+96895551122', TRUE),
       (2, 4, '+96894443311', FALSE);

SELECT setval('courier_courier_id_seq', (SELECT MAX(courier_id) FROM courier));

-- 14. CUSTOMER ADDRESSES
INSERT INTO customer_address
    (address_id, customer_id, label, building_name, floor_number,
     apartment_number, street_address, contact_phone, latitude, longitude, is_default)
VALUES (1, 1, 'Home', 'Favoured Building 1', '4', '45',
        'Al Khuwayr South', '+96891234567', 23.5901, 58.3840, TRUE),
       (2, 2, 'Home', 'Al-Rams Apartments', '1', '102',
        'Al Mouj Street, Seeb', '+96898765432', 23.6210, 58.2130, TRUE);

SELECT setval('customer_address_address_id_seq', (SELECT MAX(address_id) FROM customer_address));

-- 15. ORDER STATUSES
INSERT INTO order_status (status_id, code, description)
VALUES (1, 'PENDING', 'Order was placed and awaits restaurant confirmation'),
       (2, 'ACCEPTED', 'Order accepted by restaurant'),
       (3, 'PREPARING', 'Kitchen is preparing the food'),
       (4, 'READY_FOR_PICKUP', 'Food is ready for driver pickup'),
       (5, 'OUT_FOR_DELIVERY', 'Courier picked up order and is en route'),
       (6, 'DELIVERED', 'Order handed over to customer'),
       (7, 'CANCELLED', 'Order was cancelled');

SELECT setval('order_status_status_id_seq', (SELECT MAX(status_id) FROM order_status));

-- 16. ORDERS
INSERT INTO "order"
    (order_id, customer_id, restaurant_id, current_status_id, courier_id,
     subtotal, discount_amount, delivery_fee, service_fee, total_amount,
     estimated_delivery, placed_at, created_at, updated_at)
VALUES (1001, 1, 1, 6, 1, 12.50, 0.00, 0.00, 0.07, 12.57,
        '2026-09-10 12:45:00', '2026-09-10 12:15:00',
        '2026-09-10 12:15:00', '2026-09-10 12:45:00'),
       (1002, 2, 1, 5, 2, 16.00, 0.00, 0.59, 0.10, 16.69,
        '2026-09-10 13:30:00', '2026-09-10 12:50:00',
        '2026-09-10 12:50:00', '2026-09-10 13:14:00');

SELECT setval('order_order_id_seq', (SELECT MAX(order_id) FROM "order"));

-- 17. ORDER ADDRESS SNAPSHOTS
INSERT INTO order_delivery_address
    (order_address_id, order_id, building_name, floor_number, apartment_number,
     street_address, contact_phone, latitude, longitude, delivery_instructions)
VALUES (1, 1001, 'Favoured Building 1', '4', '45', 'Al Khuwayr South',
        '+96891234567', 23.5901, 58.3840, 'Call on arrival'),
       (2, 1002, 'Al-Rams Apartments', '1', '102', 'Al Mouj Street, Seeb',
        '+96898765432', 23.6210, 58.2130, 'Leave at reception');

SELECT setval('order_delivery_address_order_address_id_seq',
              (SELECT MAX(order_address_id) FROM order_delivery_address));

-- 18. ORDER ITEMS
INSERT INTO order_item
    (order_item_id, order_id, menu_item_id, item_name_snapshot,
     unit_price_snapshot, selected, item_note, customizations_snapshot)
VALUES (1, 1001, 1, 'Classic Cheeseburger', 12.50, 1,
        'No pickles please', '[]'::jsonb),
       (2, 1002, 2, 'Double Bacon Burger', 16.00, 1,
        NULL, '[]'::jsonb);

SELECT setval('order_item_order_item_id_seq', (SELECT MAX(order_item_id) FROM order_item));

-- 19. PAYMENTS
INSERT INTO payment
    (payment_id, order_id, payment_method, payment_status, amount,
     transaction_reference, processed_at)
VALUES (1, 1001, 'CASH', 'CAPTURED', 12.57, 'COD-REF-99201', '2026-09-10 12:45:10'),
       (2, 1002, 'CREDIT_CARD', 'CAPTURED', 16.69, 'PAY-TAP-88492011', '2026-09-10 12:50:45');

SELECT setval('payment_payment_id_seq', (SELECT MAX(payment_id) FROM payment));

-- 20. ORDER STATUS HISTORY
INSERT INTO order_status_history
    (history_id, order_id, status_id, changed_by_user_id, notes, created_at)
VALUES (1, 1001, 1, 1, 'Customer placed order', '2026-09-10 12:15:00'),
       (2, 1001, 2, 5, 'Restaurant accepted order', '2026-09-10 12:17:30'),
       (3, 1001, 3, 5, 'Kitchen started preparation', '2026-09-10 12:20:00'),
       (4, 1001, 4, 5, 'Food ready for pickup', '2026-09-10 12:32:00'),
       (5, 1001, 5, 3, 'Courier picked up order', '2026-09-10 12:35:00'),
       (6, 1001, 6, 3, 'Order delivered', '2026-09-10 12:45:00'),
       (7, 1002, 1, 2, 'Customer placed order', '2026-09-10 12:50:00'),
       (8, 1002, 2, 5, 'Restaurant accepted order', '2026-09-10 12:52:00'),
       (9, 1002, 3, 5, 'Kitchen started preparation', '2026-09-10 12:55:00'),
       (10, 1002, 4, 5, 'Order ready for pickup', '2026-09-10 13:10:00'),
       (11, 1002, 5, 4, 'Courier picked up order', '2026-09-10 13:14:00');

SELECT setval('order_status_history_history_id_seq',
              (SELECT MAX(history_id) FROM order_status_history));
