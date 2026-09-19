SET search_path TO foodland;


    -- ============================================================
-- 1. INSERT BASE USERS (CUSTOMER)
-- ============================================================
-- IDs generated: 1 to 5
INSERT INTO app_user (user_id, name, email, phone_number, password_hash, user_type, is_active) VALUES
(1, 'Ahmed Hassan', 'ahmed.hassan@example.com', '+96891234567', '$2a$12$e8Uv...hash1', 'CUSTOMER', TRUE),
(2, 'Sara Al-Busaidi', 'sara.busaidi@example.com', '+96898765432', '$2a$12$e8Uv...hash2', 'CUSTOMER', TRUE),
(3, 'Tariq Al-Balushi', 'tariq.driver@example.com', '+96895551122', '$2a$12$e8Uv...hash3', 'COURIER', TRUE),
(4, 'Khaled Courier', 'khaled.courier@example.com', '+96894443311', '$2a$12$e8Uv...hash4', 'COURIER', TRUE),
(5, 'Admin User', 'admin@foodland.com', '+96890000000', '$2a$12$e8Uv...hash5', 'ADMIN', TRUE);

SELECT setval('app_user_user_id_seq', (SELECT MAX(user_id) FROM app_user));

INSERT INTO customer (customer_id) VALUES
                                       (1),
                                       (2),
                                       (3);

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
INSERT INTO cart_item (cart_id, menu_item_id, quantity, unit_price_snapshot, item_note)
VALUES (1, 1, 2, 12.50, 'No pickles please'),
       (1, 3, 1, 4.50, 'Extra crispy'),
       (2, 4, 1, 14.00, NULL);

alter table cart_item
    rename column quantity to selected;

-- 11. CART ITEM ↔ SELECTED CUSTOMIZATION
INSERT INTO cart_item_customization (cart_item_id, customization_option_id, price_snapshot, quantity)
VALUES (1, 1, 0.00, 1), -- Selected Cheddar for Cheeseburger
       (1, 3, 2.00, 1), -- Selected Extra Bacon for Cheeseburger
       (3, 7, 2.50, 1); -- Selected Stuffed Crust for Pizza

alter table cart_item_customization
    rename column quantity to selected;

-- ========================================================================================================================
-- ORDER MANAGEMENT
-- ========================================================================================================================

-- ============================================================
-- 1. INSERT COURIERS (courier)
-- ============================================================
INSERT INTO order_status (status_id, code, description) VALUES
                                                            (1, 'PENDING', 'Order was placed and awaits restaurant confirmation'),
                                                            (2, 'ACCEPTED', 'Order accepted by restaurant'),
                                                            (3, 'PREPARING', 'Kitchen is preparing the food'),
                                                            (4, 'READY_FOR_PICKUP', 'Food is ready for driver pickup'),
                                                            (5, 'OUT_FOR_DELIVERY', 'Courier picked up order and is en route'),
                                                            (6, 'DELIVERED', 'Order handed over to customer'),
                                                            (7, 'CANCELLED', 'Order was cancelled');






-- ============================================================
-- 2. INSERT COURIERS (courier)
-- ============================================================
INSERT INTO courier (courier_id, vehicle_type, license_plate, is_available, current_latitude, current_longitude) VALUES
                                                                                                                     (3, 'MOTORCYCLE', 'OM-10293', TRUE, 23.5880, 58.3829),
                                                                                                                     (4, 'CAR', 'OM-44921', FALSE, 23.6001, 58.4100);

-- ============================================================
-- 3. INSERT CUSTOMER SAVED ADDRESSES (customer_address)
-- ============================================================
INSERT INTO customer_address (address_id, customer_id, label, building_name, floor_number, apartment_number, street_address, latitude, longitude, is_default) VALUES
                                                                                                                                                                  (1, 1, 'Home', 'Favoured Building 1', '4', '45', 'ohio, Al Khuwayr South', 23.5901, 58.3840, TRUE),
                                                                                                                                                                  (2, 1, 'Work', 'Oman Tower', '12', '1204', 'Way 3301, Ruwi', 23.6022, 58.5410, FALSE),
                                                                                                                                                                  (3, 2, 'Home', 'Al-Rams Apartments', '1', '102', 'Al Mouj Street, Seeb', 23.6210, 58.2130, TRUE);

SELECT setval('customer_address_address_id_seq', (SELECT MAX(address_id) FROM customer_address));

-- ============================================================
-- 4. INSERT ORDERS ("order")
-- ============================================================
-- Order 1: Completed / Delivered by Tariq to Ahmed
-- Order 2: In-Progress / Out for delivery
INSERT INTO "order" (order_id, customer_id, restaurant_id, current_status_id, courier_id, subtotal, discount_amount, delivery_fee, service_fee, total_amount, estimated_delivery, placed_at) VALUES
                                                                                                                                                                                                 (1001, 1, 1, 6, 3, 2.40, 0.48, 0.00, 0.07, 1.99, '2026-09-10 12:45:00', '2026-09-10 12:15:00'),
                                                                                                                                                                                                 (1002, 2, 1, 5, 4, 5.50, 0.00, 0.59, 0.10, 6.19, '2026-09-10 13:30:00', '2026-09-10 12:50:00');

SELECT setval('order_order_id_seq', (SELECT MAX(order_id) FROM "order"));

-- ============================================================
-- 5. INSERT ORDER DELIVERY ADDRESS SNAPSHOT (order_delivery_address)
-- ============================================================
INSERT INTO order_delivery_address (order_address_id, order_id, building_name, floor_number, apartment_number, street_address, contact_phone, latitude, longitude, delivery_instructions) VALUES
                                                                                                                                                                                              (1, 1001, 'Favoured Building 1', '4', '45', 'ohio, Al Khuwayr South', '+96891234567', 23.5901, 58.3840, 'Call on arrival. Don''t ring bell.'),
                                                                                                                                                                                              (2, 1002, 'Al-Rams Apartments', '1', '102', 'Al Mouj Street, Seeb', '+96898765432', 23.6210, 58.2130, 'Leave at reception');

SELECT setval('order_delivery_address_order_address_id_seq', (SELECT MAX(order_address_id) FROM order_delivery_address));

-- ============================================================
-- 6. INSERT ORDER ITEMS WITH JSONB CUSTOMIZATIONS SNAPSHOT (order_item)
-- ============================================================
INSERT INTO order_item (order_item_id, order_id, menu_item_id, item_name_snapshot, unit_price_snapshot, selected, item_note, customizations_snapshot) VALUES
                                                                                                                                                          (1, 1001, 1, 'Asoom Special Burger', 2.40, 1, 'Extra spicy please',
                                                                                                                                                           '[
                                                                                                                                                             {"group_name": "Cheese Choice", "option_id": 101, "option_name": "Cheddar Cheese", "price_snapshot": 0.40, "selected": 1},
                                                                                                                                                             {"group_name": "Sauces", "option_id": 204, "option_name": "Garlic Mayo", "price_snapshot": 0.00, "selected": 1}
                                                                                                                                                           ]'::jsonb
                                                                                                                                                          ),
                                                                                                                                                          (2, 1002, 1, 'Asoom Special Burger', 2.40, 2, NULL,
                                                                                                                                                           '[
                                                                                                                                                             {"group_name": "Cheese Choice", "option_id": 102, "option_name": "Swiss Cheese", "price_snapshot": 0.50, "selected": 1}
                                                                                                                                                           ]'::jsonb
                                                                                                                                                          ),
                                                                                                                                                          (3, 1002, 2, 'Crispy Fries', 0.70, 1, 'Well done', '[]'::jsonb);

SELECT setval('order_item_order_item_id_seq', (SELECT MAX(order_item_id) FROM order_item));

-- ============================================================
-- 7. INSERT PAYMENTS (payment)
-- ============================================================
INSERT INTO payment (payment_id, order_id, payment_method, payment_status, amount, transaction_reference, processed_at) VALUES
                                                                                                                            (1, 1001, 'CASH', 'CAPTURED', 1.99, 'COD-REF-99201', '2026-09-10 12:45:10'),
                                                                                                                            (2, 1002, 'CREDIT_CARD', 'CAPTURED', 6.19, 'PAY-TAP-88492011', '2026-09-10 12:50:45');

SELECT setval('payment_payment_id_seq', (SELECT MAX(payment_id) FROM payment));

-- ============================================================
-- 8. INSERT INVOICES (invoice)
-- ============================================================
INSERT INTO invoice (invoice_id, order_id, invoice_number, tax_amount, subtotal, discount_amount, delivery_fee, service_fee, total_amount, issued_at) VALUES
    (1, 1001, 'INV-2026-000101', 0.00, 2.40, 0.48, 0.00, 0.07, 1.99, '2026-09-10 12:45:15');

SELECT setval('invoice_invoice_id_seq', (SELECT MAX(invoice_id) FROM invoice));

-- ============================================================
-- 9. INSERT ORDER DISCOUNTS APPLIED (order_discount)
-- ============================================================
INSERT INTO order_discount (order_discount_id, order_id, discount_type, promo_code, discount_amount) VALUES
    (1, 1001, 'WELCOME_GIFT', 'WELCOME2026', 0.48);

SELECT setval('order_discount_order_discount_id_seq', (SELECT MAX(order_discount_id) FROM order_discount));

-- ============================================================
-- 10. INSERT ORDER STATUS HISTORY (order_status_history)
-- ============================================================
INSERT INTO order_status_history (history_id, order_id, status_id, changed_by_user_id, notes, created_at) VALUES
-- Order 1001 Lifecycle
(1, 1001, 1, 1, 'Customer placed order via Android app', '2026-09-10 12:15:00'),
(2, 1001, 2, 5, 'Merchant accepted order', '2026-09-10 12:17:30'),
(3, 1001, 3, 5, 'Kitchen started preparation', '2026-09-10 12:20:00'),
(4, 1001, 4, 5, 'Food ready for pickup', '2026-09-10 12:32:00'),
(5, 1001, 5, 3, 'Courier Tariq picked up order', '2026-09-10 12:35:00'),
(6, 1001, 6, 3, 'Order handed to customer', '2026-09-10 12:45:00'),

-- Order 1002 Lifecycle (In Progress)
(7, 1002, 1, 2, 'Customer placed order via Web app', '2026-09-10 12:50:00'),
(8, 1002, 2, 5, 'Merchant accepted order', '2026-09-10 12:52:00'),
(9, 1002, 3, 5, 'Kitchen started preparation', '2026-09-10 12:55:00'),
(10, 1002, 4, 5, 'Order ready for pickup', '2026-09-10 13:10:00'),
(11, 1002, 5, 4, 'Courier Khaled picked up order', '2026-09-10 13:14:00');

SELECT setval('order_status_history_history_id_seq', (SELECT MAX(history_id) FROM order_status_history));