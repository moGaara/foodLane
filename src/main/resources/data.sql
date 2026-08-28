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
INSERT INTO cart_item (cart_id, menu_item_id, quantity, unit_price_snapshot, item_note)
VALUES (1, 1, 2, 12.50, 'No pickles please'),
       (1, 3, 1, 4.50, 'Extra crispy'),
       (2, 4, 1, 14.00, NULL);

-- 11. CART ITEM ↔ SELECTED CUSTOMIZATION
INSERT INTO cart_item_customization (cart_item_id, customization_option_id, price_snapshot, quantity)
VALUES (1, 1, 0.00, 1), -- Selected Cheddar for Cheeseburger
       (1, 3, 2.00, 1), -- Selected Extra Bacon for Cheeseburger
       (3, 7, 2.50, 1); -- Selected Stuffed Crust for Pizza
