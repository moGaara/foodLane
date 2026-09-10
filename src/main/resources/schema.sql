-- ============================================================
-- TALABAT CART MANAGEMENT SYSTEM

create schema foodland;

-- Set search_path so all following statements execute within foodland
SET search_path TO foodland;

-- ============================================================

-- ============================================================
-- 1. CUSTOMER
-- ============================================================

CREATE TABLE customer
(
    customer_id BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. RESTAURANT
-- ============================================================

CREATE TABLE restaurant
(
    restaurant_id BIGSERIAL PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    is_open       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 3. MENU
-- ============================================================

CREATE TABLE menu
(
    menu_id       BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,

    CONSTRAINT fk_menu_restaurant
        FOREIGN KEY (restaurant_id)
            REFERENCES restaurant (restaurant_id)
            ON DELETE CASCADE,

    -- force 1 to 1 relationship
    CONSTRAINT uq_menu_restaurant
        UNIQUE (menu_id, restaurant_id)
);


-- ============================================================
-- 4. CATEGORY
-- ============================================================

CREATE TABLE category
(
    category_id BIGSERIAL PRIMARY KEY,
    menu_id     BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,

    CONSTRAINT fk_category_menu
        FOREIGN KEY (menu_id)
            REFERENCES menu (menu_id)
            ON DELETE CASCADE,
    -- make sure that no dublicated category in the same menue
    CONSTRAINT uq_category_menu_name
        UNIQUE (menu_id, name)
);


-- ============================================================
-- 5. MENU ITEM
-- ============================================================

CREATE TABLE menu_item
(
    menu_item_id       BIGSERIAL PRIMARY KEY,

    category_id        BIGINT         NOT NULL,

    name               VARCHAR(150)   NOT NULL,

    description        TEXT,

    price              NUMERIC(10, 2) NOT NULL,

    image_url          TEXT,

    inventory_quantity INTEGER        NOT NULL DEFAULT 0,

    CONSTRAINT fk_menu_item_category
        FOREIGN KEY (category_id)
            REFERENCES category (category_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_menu_item_price
        CHECK (price >= 0)
);


-- ============================================================
-- 6. CUSTOMIZATION GROUP
-- ============================================================

CREATE TABLE customization_group
(
    customization_group_id BIGSERIAL PRIMARY KEY,

    name                   VARCHAR(150) NOT NULL,

    -- Example:
    -- Crust       -> required = true
    -- Add-ons     -> required = false
    required               BOOLEAN      NOT NULL DEFAULT FALSE,

    min_select             INTEGER      NOT NULL DEFAULT 0,
    max_select             INTEGER      NOT NULL DEFAULT 1,

   CONSTRAINT chk_customization_required_select
    CHECK (
        (required = TRUE  AND min_select = 1 AND max_select = 1)
        OR
        (required = FALSE AND min_select = 0)
    )
);


-- ============================================================
-- 7. CUSTOMIZATION OPTION
-- ============================================================

CREATE TABLE customization_option
(
    customization_option_id BIGSERIAL PRIMARY KEY,

    customization_group_id  BIGINT         NOT NULL,

    name                    VARCHAR(150)   NOT NULL,
    price                   NUMERIC(10, 2) NOT NULL DEFAULT 0,
    inventory_quantity      INTEGER        NOT NULL DEFAULT 0,

    CONSTRAINT fk_option_group
        FOREIGN KEY (customization_group_id)
            REFERENCES customization_group (customization_group_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_option_price
        CHECK (price >= 0),
    -- one option is existed in one group
    CONSTRAINT uq_option_group_name
        UNIQUE (customization_group_id, name)
);


-- ============================================================
-- 8. MENU ITEM ↔ CUSTOMIZATION GROUP
-- ============================================================

CREATE TABLE menu_item_customization_group
(
    menu_item_id           BIGINT NOT NULL,
    customization_group_id BIGINT NOT NULL,

    PRIMARY KEY (
                 menu_item_id,
                 customization_group_id
        ),

    CONSTRAINT fk_micg_menu_item
        FOREIGN KEY (menu_item_id)
            REFERENCES menu_item (menu_item_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_micg_customization_group
        FOREIGN KEY (customization_group_id)
            REFERENCES customization_group (customization_group_id)
            ON DELETE CASCADE
);


-- ============================================================
-- 9. CART
-- ============================================================


CREATE TABLE cart
(
    cart_id       BIGSERIAL PRIMARY KEY,

    customer_id   BIGINT      NOT NULL,
    restaurant_id BIGINT      NOT NULL,

    status        VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer (customer_id),

    CONSTRAINT fk_cart_restaurant
        FOREIGN KEY (restaurant_id)
            REFERENCES restaurant (restaurant_id)
);


-- Partial Unique Index => retuen active cart
CREATE UNIQUE INDEX uq_cart_one_active_per_customer
    ON cart (customer_id) WHERE status = 'ACTIVE';

-- ============================================================
-- 10. CART ITEM
-- ============================================================

CREATE TABLE cart_item
(
    cart_item_id        BIGSERIAL PRIMARY KEY,

    cart_id             BIGINT         NOT NULL,
    menu_item_id        BIGINT         NOT NULL,

    quantity            INTEGER        NOT NULL,

    -- Price at the moment the item was added/updated.
    unit_price_snapshot NUMERIC(10, 2) NOT NULL,

    item_note           TEXT,

    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id)
            REFERENCES cart (cart_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_cart_item_menu_item
        FOREIGN KEY (menu_item_id)
            REFERENCES menu_item (menu_item_id),

    CONSTRAINT chk_cart_item_quantity
        CHECK (quantity BETWEEN 1 AND 99),

    CONSTRAINT chk_cart_item_price
        CHECK (unit_price_snapshot >= 0)
);


-- ============================================================
-- 11. CART ITEM ↔ SELECTED CUSTOMIZATION
-- ============================================================

CREATE TABLE cart_item_customization
(
    cart_item_customization_id BIGSERIAL PRIMARY KEY,

    cart_item_id               BIGINT         NOT NULL,
    customization_option_id    BIGINT         NOT NULL,

    -- Price of the option when the customer selected it.
    price_snapshot             NUMERIC(10, 2) NOT NULL DEFAULT 0,

    quantity                   INTEGER        NOT NULL DEFAULT 1,

    CONSTRAINT fk_cic_cart_item
        FOREIGN KEY (cart_item_id)
            REFERENCES cart_item (cart_item_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_cic_customization_option
        FOREIGN KEY (customization_option_id)
            REFERENCES customization_option (customization_option_id),

    CONSTRAINT chk_cic_price
        CHECK (price_snapshot >= 0),

    CONSTRAINT chk_cic_quantity
    CHECK (quantity BETWEEN 1 AND 6),

    CONSTRAINT uq_cart_item_option
        UNIQUE (cart_item_id, customization_option_id)
);

-------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------
Order Management script
-------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------

CREATE TABLE "user"
(
    user_id       BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone_number  VARCHAR(30)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type     VARCHAR(20)  NOT NULL CHECK (user_type IN ('CUSTOMER', 'COURIER', 'ADMIN', 'RESTAURANT_OWNER')),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 2. CUSTOMER (SUBTYPE TABLE)
-- ============================================================

CREATE TABLE customer
(
    -- 1:1 Inheritance link to user
    customer_id      BIGINT PRIMARY KEY,

    CONSTRAINT fk_customer_user
        FOREIGN KEY (customer_id)
            REFERENCES user (user_id)
            ON DELETE CASCADE,

);

--alter table customer 
--add  CONSTRAINT fk_customer_user
--        FOREIGN KEY (customer_id)
--            REFERENCES "user" (user_id)
--            ON DELETE cascade
--            
            
-- ============================================================
-- 3. COURIER / DRIVER (SUBTYPE TABLE)
-- ============================================================

CREATE TABLE courier
(
    -- 1:1 Inheritance link to user
    courier_id        BIGINT PRIMARY KEY,
    vehicle_type      VARCHAR(50)  NOT NULL DEFAULT 'MOTORCYCLE', -- 'MOTORCYCLE', 'BICYCLE', 'CAR'
    license_plate     VARCHAR(30),
    is_available      BOOLEAN      NOT NULL DEFAULT TRUE,
    current_latitude  NUMERIC(10, 8),
    current_longitude NUMERIC(11, 8),

    CONSTRAINT fk_courier_user
        FOREIGN KEY (courier_id)
            REFERENCES "user" (user_id)
            ON DELETE CASCADE
);


-- ============================================================
-- 4. CUSTOMER SAVED ADDRESSES
-- ============================================================

CREATE TABLE customer_address
(
    address_id       BIGSERIAL PRIMARY KEY,
    customer_id      BIGINT       NOT NULL,
    label            VARCHAR(50)  NOT NULL DEFAULT 'Home', -- e.g., 'Home', 'Work'
    building_name    VARCHAR(100) NOT NULL,
    floor_number     VARCHAR(20),
    apartment_number VARCHAR(20),
    street_address   TEXT         NOT NULL,
    latitude         NUMERIC(10, 8),
    longitude        NUMERIC(11, 8),
    is_default       BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_address_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer (customer_id)
            ON DELETE CASCADE
);


  ALTER TABLE foodland.customer_address
  ADD COLUMN contact_phone VARCHAR(30);

  SELECT column_name
  FROM information_schema.columns
  WHERE table_schema = 'foodland'
    AND table_name = 'customer_address';

 
  UPDATE foodland.customer_address
  SET contact_phone = '+201000000000'
  WHERE address_id = 15;

-- ============================================================
-- 5. ORDER STATUS LOOKUP / MASTER TABLE
-- ============================================================

CREATE TABLE order_status
(
    status_id   INTEGER PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE, 
    description VARCHAR(255) NOT NULL
);

INSERT INTO order_status (status_id, code, description) VALUES
(1, 'PLACED', 'Order submitted by customer'),
(2, 'ACCEPTED', 'Order accepted by restaurant'),
(3, 'PREPARING', 'Kitchen is preparing the food'),
(4, 'READY_FOR_PICKUP', 'Food is ready for driver pickup'),
(5, 'OUT_FOR_DELIVERY', 'Courier picked up order and is en route'),
(6, 'DELIVERED', 'Order handed over to customer'),
(7, 'CANCELLED', 'Order was cancelled'),
(8 , 'PENDING' , 'Order was placed and awaits restaurant confirmation');



 CREATE SEQUENCE IF NOT EXISTS foodland.order_status_status_id_seq;

  ALTER TABLE foodland.order_status
      ALTER COLUMN status_id
      SET DEFAULT nextval('foodland.order_status_status_id_seq'::regclass);

  ALTER SEQUENCE foodland.order_status_status_id_seq
      OWNED BY foodland.order_status.status_id;

  SELECT setval(
      'foodland.order_status_status_id_seq',
      COALESCE((SELECT MAX(status_id) FROM foodland.order_status), 0) + 1,
      false
  );


-- ============================================================
-- 6. ORDERS (CORE FULFILLMENT HEADER)
-- ============================================================

CREATE TABLE "order"
(
    order_id            BIGSERIAL PRIMARY KEY,

    -- Foreign Key references
    customer_id         BIGINT         NOT NULL,
    restaurant_id       BIGINT         NOT NULL,
    current_status_id   INTEGER        NOT NULL DEFAULT 1,
    courier_id          BIGINT         NULL,

    -- Financial Summary (Snapshots at checkout)
    subtotal            NUMERIC(10, 2) NOT NULL,
    discount_amount     NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    delivery_fee        NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    service_fee         NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total_amount        NUMERIC(10, 2) NOT NULL,

    -- Timestamps
    estimated_delivery  TIMESTAMP,
    placed_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer (customer_id),

    CONSTRAINT fk_order_restaurant
        FOREIGN KEY (restaurant_id)
            REFERENCES restaurant (restaurant_id),

    CONSTRAINT fk_order_status
        FOREIGN KEY (current_status_id)
            REFERENCES order_status (status_id),

    CONSTRAINT fk_order_courier
        FOREIGN KEY (courier_id)
            REFERENCES courier (courier_id),

    CONSTRAINT chk_order_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_order_discount CHECK (discount_amount >= 0),
    CONSTRAINT chk_order_delivery CHECK (delivery_fee >= 0),
    CONSTRAINT chk_order_service CHECK (service_fee >= 0),
    CONSTRAINT chk_order_total CHECK (total_amount >= 0)
);

 SELECT column_name, data_type, is_nullable, column_default
  FROM information_schema.columns
  WHERE table_schema = 'foodland'
    AND table_name = 'order'
  ORDER BY ordinal_position;


  ALTER TABLE foodland."order"
      ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
  
-- ============================================================
-- 7. ORDER DELIVERY ADDRESS SNAPSHOT
-- ============================================================

CREATE TABLE order_delivery_address
(
    order_address_id      BIGSERIAL PRIMARY KEY,
    order_id              BIGINT       NOT NULL UNIQUE,

    -- Snapshot address fields
    building_name         VARCHAR(100) NOT NULL,
    floor_number          VARCHAR(20),
    apartment_number      VARCHAR(20),
    street_address        TEXT         NOT NULL,
    contact_phone         VARCHAR(30)  NOT NULL,
    latitude              NUMERIC(10, 8),
    longitude             NUMERIC(11, 8),

    -- Explicit Driver Action Toggles (e.g., 'Call on arrival')
    delivery_instructions TEXT,

    CONSTRAINT fk_order_address_order
        FOREIGN KEY (order_id)
            REFERENCES "order" (order_id)
            ON DELETE CASCADE
);


-- ============================================================
-- 8. ORDER ITEM (WITH JSONB CUSTOMIZATIONS SNAPSHOT)
-- ============================================================

CREATE TABLE order_item
(
    order_item_id          BIGSERIAL PRIMARY KEY,

    order_id               BIGINT         NOT NULL,
    menu_item_id           BIGINT         NOT NULL,

    item_name_snapshot    VARCHAR(150)   NOT NULL,
    unit_price_snapshot   NUMERIC(10, 2) NOT NULL,
    selected               INTEGER        NOT NULL,
    item_note              TEXT,

    -- Customizations serialized directly in JSONB format
    customizations_snapshot JSONB          NOT NULL DEFAULT '[]'::jsonb,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
            REFERENCES "order" (order_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_order_item_menu_item
        FOREIGN KEY (menu_item_id)
            REFERENCES menu_item (menu_item_id),

    CONSTRAINT chk_order_item_quantity CHECK (selected BETWEEN 1 AND 99),
    CONSTRAINT chk_order_item_unit_price CHECK (unit_price_snapshot >= 0)
);


-- ============================================================
-- 9. PAYMENT MANAGEMENT
-- ============================================================

CREATE TABLE payment
(
    payment_id            BIGSERIAL PRIMARY KEY,
    order_id              BIGINT         NOT NULL UNIQUE,

    payment_method        VARCHAR(30)    NOT NULL, -- 'CASH', 'CREDIT_CARD', 'APPLE_PAY'
    payment_status        VARCHAR(30)    NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'CAPTURED', 'FAILED', 'REFUNDED'
    amount                NUMERIC(10, 2) NOT NULL,
    transaction_reference VARCHAR(255),
    processed_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id)
            REFERENCES "order" (order_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_payment_amount CHECK (amount >= 0)
);


-- ============================================================
-- 10. INVOICE (LEGAL & FINANCIAL DOCUMENT)
-- ============================================================

CREATE TABLE invoice
(
    invoice_id      BIGSERIAL PRIMARY KEY,
    order_id        BIGINT         NOT NULL UNIQUE,
    invoice_number  VARCHAR(100)   NOT NULL UNIQUE, -- e.g., 'INV-2026-000109'

    tax_amount      NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    subtotal        NUMERIC(10, 2) NOT NULL,
    discount_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    delivery_fee    NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    service_fee     NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total_amount    NUMERIC(10, 2) NOT NULL,

    issued_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_order
        FOREIGN KEY (order_id)
            REFERENCES "order" (order_id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_invoice_tax CHECK (tax_amount >= 0),
    CONSTRAINT chk_invoice_total CHECK (total_amount >= 0)
);


-- ============================================================
-- 11. ORDER DISCOUNTS APPLIED
-- ============================================================

CREATE TABLE order_discount
(
    order_discount_id BIGSERIAL PRIMARY KEY,
    order_id          BIGINT         NOT NULL,

    discount_type     VARCHAR(50)    NOT NULL, -- 'PROMO_CODE', 'WELCOME_GIFT'
    promo_code        VARCHAR(50),
    discount_amount   NUMERIC(10, 2) NOT NULL,

    CONSTRAINT fk_discount_order
        FOREIGN KEY (order_id)
            REFERENCES "order" (order_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_discount_amount CHECK (discount_amount >= 0)
);


-- ============================================================
-- 12. ORDER STATUS HISTORY (AUDIT TRAIL & LOGISTICS TRACKING)
-- ============================================================

CREATE TABLE order_status_history
(
    history_id         BIGSERIAL PRIMARY KEY,
    order_id           BIGINT    NOT NULL,
    status_id          INTEGER   NOT NULL,
    changed_by_user_id BIGINT,   -- FK to user (Customer, Courier, or Admin)
    notes              TEXT,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_order
        FOREIGN KEY (order_id)
            REFERENCES "order" (order_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_history_status
        FOREIGN KEY (status_id)
            REFERENCES order_status (status_id),

    CONSTRAINT fk_history_user
        FOREIGN KEY (changed_by_user_id)
            REFERENCES "user" (user_id)
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);



--------------------------------------------------------------------------------------------------------------------


 UPDATE foodland.customer_address
  SET contact_phone = '+201000000001'
  WHERE address_id = 1;


alter table cart_item 
rename column quantity to selected


alter table cart_item_customization  
rename column quantity to selected



ALTER TABLE customer 
DROP COLUMN email;

ALTER TABLE customer 
DROP COLUMN name;

ALTER TABLE customer 
DROP COLUMN created_at;



 
-----------------------------------------

-- ============================================================
-- 1. INSERT BASE USERS (user)
-- ============================================================
-- IDs generated: 1 to 5
INSERT INTO "user" (user_id, name, email, phone_number, password_hash, user_type, is_active) VALUES
(1, 'Ahmed Hassan', 'ahmed.hassan@example.com', '+96891234567', '$2a$12$e8Uv...hash1', 'CUSTOMER', TRUE),
(2, 'Sara Al-Busaidi', 'sara.busaidi@example.com', '+96898765432', '$2a$12$e8Uv...hash2', 'CUSTOMER', TRUE),
(3, 'Tariq Al-Balushi', 'tariq.driver@example.com', '+96895551122', '$2a$12$e8Uv...hash3', 'COURIER', TRUE),
(4, 'Khaled Courier', 'khaled.courier@example.com', '+96894443311', '$2a$12$e8Uv...hash4', 'COURIER', TRUE),
(5, 'Admin User', 'admin@foodland.com', '+96890000000', '$2a$12$e8Uv...hash5', 'ADMIN', TRUE);

SELECT setval('user_user_id_seq', (SELECT MAX(user_id) FROM "user"));

-- ============================================================
-- 2. INSERT CUSTOMERS (customer)
-- ============================================================
INSERT INTO customer (customer_id) VALUES
(1),
(2);


-- ============================================================
-- 3. INSERT COURIERS (courier)
-- ============================================================
INSERT INTO courier (courier_id, vehicle_type, license_plate, is_available, current_latitude, current_longitude) VALUES
(3, 'MOTORCYCLE', 'OM-10293', TRUE, 23.5880, 58.3829),
(4, 'CAR', 'OM-44921', FALSE, 23.6001, 58.4100);

-- ============================================================
-- 4. INSERT CUSTOMER SAVED ADDRESSES (customer_address)
-- ============================================================
INSERT INTO customer_address (address_id, customer_id, label, building_name, floor_number, apartment_number, street_address, latitude, longitude, is_default) VALUES
(1, 1, 'Home', 'Favoured Building 1', '4', '45', 'ohio, Al Khuwayr South', 23.5901, 58.3840, TRUE),
(2, 1, 'Work', 'Oman Tower', '12', '1204', 'Way 3301, Ruwi', 23.6022, 58.5410, FALSE),
(3, 2, 'Home', 'Al-Rams Apartments', '1', '102', 'Al Mouj Street, Seeb', 23.6210, 58.2130, TRUE);

SELECT setval('customer_address_address_id_seq', (SELECT MAX(address_id) FROM customer_address));

-- ============================================================
-- 5. INSERT ORDERS ("order")
-- ============================================================
-- Order 1: Completed / Delivered by Tariq to Ahmed
-- Order 2: In-Progress / Out for delivery
INSERT INTO "order" (order_id, customer_id, restaurant_id, current_status_id, courier_id, subtotal, discount_amount, delivery_fee, service_fee, total_amount, estimated_delivery, placed_at) VALUES
(1001, 1, 1, 6, 3, 2.40, 0.48, 0.00, 0.07, 1.99, '2026-09-10 12:45:00', '2026-09-10 12:15:00'),
(1002, 2, 1, 5, 4, 5.50, 0.00, 0.59, 0.10, 6.19, '2026-09-10 13:30:00', '2026-09-10 12:50:00');

SELECT setval('order_order_id_seq', (SELECT MAX(order_id) FROM "order"));

-- ============================================================
-- 6. INSERT ORDER DELIVERY ADDRESS SNAPSHOT (order_delivery_address)
-- ============================================================
INSERT INTO order_delivery_address (order_address_id, order_id, building_name, floor_number, apartment_number, street_address, contact_phone, latitude, longitude, delivery_instructions) VALUES
(1, 1001, 'Favoured Building 1', '4', '45', 'ohio, Al Khuwayr South', '+96891234567', 23.5901, 58.3840, 'Call on arrival. Don''t ring bell.'),
(2, 1002, 'Al-Rams Apartments', '1', '102', 'Al Mouj Street, Seeb', '+96898765432', 23.6210, 58.2130, 'Leave at reception');

SELECT setval('order_delivery_address_order_address_id_seq', (SELECT MAX(order_address_id) FROM order_delivery_address));

-- ============================================================
-- 7. INSERT ORDER ITEMS WITH JSONB CUSTOMIZATIONS SNAPSHOT (order_item)
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
-- 8. INSERT PAYMENTS (payment)
-- ============================================================
INSERT INTO payment (payment_id, order_id, payment_method, payment_status, amount, transaction_reference, processed_at) VALUES
(1, 1001, 'CASH', 'CAPTURED', 1.99, 'COD-REF-99201', '2026-09-10 12:45:10'),
(2, 1002, 'CREDIT_CARD', 'CAPTURED', 6.19, 'PAY-TAP-88492011', '2026-09-10 12:50:45');

SELECT setval('payment_payment_id_seq', (SELECT MAX(payment_id) FROM payment));

-- ============================================================
-- 9. INSERT INVOICES (invoice)
-- ============================================================
INSERT INTO invoice (invoice_id, order_id, invoice_number, tax_amount, subtotal, discount_amount, delivery_fee, service_fee, total_amount, issued_at) VALUES
(1, 1001, 'INV-2026-000101', 0.00, 2.40, 0.48, 0.00, 0.07, 1.99, '2026-09-10 12:45:15');

SELECT setval('invoice_invoice_id_seq', (SELECT MAX(invoice_id) FROM invoice));

-- ============================================================
-- 10. INSERT ORDER DISCOUNTS APPLIED (order_discount)
-- ============================================================
INSERT INTO order_discount (order_discount_id, order_id, discount_type, promo_code, discount_amount) VALUES
(1, 1001, 'WELCOME_GIFT', 'WELCOME2026', 0.48);

SELECT setval('order_discount_order_discount_id_seq', (SELECT MAX(order_discount_id) FROM order_discount));

-- ============================================================
-- 11. INSERT ORDER STATUS HISTORY (order_status_history)
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

```





