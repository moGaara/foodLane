-- ============================================================
-- TALABAT CART MANAGEMENT SYSTEM

create schema FoodLand;

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

    CONSTRAINT chk_customization_min_select
        CHECK (min_select >= 0),

    CONSTRAINT chk_customization_max_select
        CHECK (max_select >= min_select),

    CONSTRAINT chk_customization_max_select_limit
        CHECK (max_select <= 6)
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
        CHECK (quantity >= 1),

    CONSTRAINT uq_cart_item_option
        UNIQUE (cart_item_id, customization_option_id)
);

