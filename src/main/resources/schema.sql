-- ============================================================
-- TALABAT CART MANAGEMENT SYSTEM

create schema foodland;

-- Set search_path so all following statements execute within foodland
SET search_path TO foodland;

-- ============================================================

-- ============================================================
-- 1. USER
-- ============================================================

CREATE TABLE app_user
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
            REFERENCES app_user (user_id)
            ON DELETE cascade

);

-- ============================================================
-- 3. RESTAURANT
-- ============================================================

CREATE TABLE restaurant
(
    restaurant_id BIGSERIAL PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    is_open       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 4. MENU
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
-- 5. CATEGORY
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
-- 6. MENU ITEM
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
-- 7. CUSTOMIZATION GROUP
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
-- 8. CUSTOMIZATION OPTION
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
-- 9. MENU ITEM ↔ CUSTOMIZATION GROUP
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
-- 10. CART
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
-- 11. CART ITEM
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
-- 12. CART ITEM ↔ SELECTED CUSTOMIZATION
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
-- Order Management script
-------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------

-- ============================================================
-- 1. COURIER / DRIVER (SUBTYPE TABLE)
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
            REFERENCES app_user (user_id)
            ON DELETE CASCADE
);


-- ============================================================
-- 2. CUSTOMER SAVED ADDRESSES
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
    contact_phone    VARCHAR(30), -- Included directly in table definition

    CONSTRAINT fk_address_customer
        FOREIGN KEY (customer_id)
            REFERENCES customer (customer_id)
            ON DELETE CASCADE
);

SELECT column_name
FROM information_schema.columns
WHERE table_schema = 'foodland'
  AND table_name = 'customer_address';


UPDATE foodland.customer_address
SET contact_phone = '+201000000000'
WHERE address_id = 15;

-- ============================================================
-- 3. ORDER STATUS LOOKUP / MASTER TABLE
-- ============================================================
CREATE TABLE order_status
(
    status_id   INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);


-- ============================================================
-- 4. ORDERS (CORE FULFILLMENT HEADER)
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Included directly in table definition
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Included directly in table definition

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

-- ============================================================
-- 5. ORDER DELIVERY ADDRESS SNAPSHOT
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
-- 6. ORDER ITEM (WITH JSONB CUSTOMIZATIONS SNAPSHOT)
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
-- 7. PAYMENT MANAGEMENT
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
-- 8. INVOICE (LEGAL & FINANCIAL DOCUMENT)
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
-- 9. ORDER DISCOUNTS APPLIED
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
-- 10. ORDER STATUS HISTORY (AUDIT TRAIL & LOGISTICS TRACKING)
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
            REFERENCES app_user (user_id)
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);



--------------------------------------------------------------------------------------------------------------------


UPDATE foodland.customer_address
SET contact_phone = '+201000000001'
WHERE address_id = 1;