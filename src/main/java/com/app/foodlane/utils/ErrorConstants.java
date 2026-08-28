package com.app.foodlane.utils;

public class ErrorConstants {
    // codes
    public static final String SUCCESS_CODE = "I000000";
    public static final String DEFAULT_ERROR_CODE = "E000000";
    public static final String REQUIRED_CART_ITEM_ID_CODE = "E000001";
    public static final String INVALID_CART_ITEM_ID_CODE = "E000002";
    public static final String CART_ITEM_NOT_EXIST_CODE = "E000004";
    public static final String INVALID_CART_UPDATE_CODE = "E000005";
    public static final String INSUFFICIENT_INVENTORY_CODE = "E000006";
    public static final String DUPLICATE_CUSTOMIZATION_CODE = "E000007";
    public static final String CUSTOMIZATION_NOT_EXIST_CODE = "E000008";
    public static final String CUSTOMIZATION_NOT_ALLOWED_CODE = "E000009";
    public static final String INVALID_CUSTOMIZATION_SELECTION_CODE = "E000010";


    // descriptions
    public static final String SUCCESS_DESC = "Success";
    public static final String DEFAULT_ERROR_DESC = "Something went wrong";
    public static final String REQUIRED_CART_ITEM_ID_DESC = "Cart item id is required";
    public static final String INVALID_CART_ITEM_ID_DESC = "Cart item id is not valid";
    public static final String CART_ITEM_NOT_EXIST_DESC = "Cart item does not exists";
    public static final String INVALID_CART_UPDATE_DESC = "Cart item update request is not valid";
    public static final String INSUFFICIENT_INVENTORY_DESC = "Requested quantity exceeds available inventory";
    public static final String DUPLICATE_CUSTOMIZATION_DESC = "Duplicate customization options are not allowed";
    public static final String CUSTOMIZATION_NOT_EXIST_DESC = "One or more customization options do not exist";
    public static final String CUSTOMIZATION_NOT_ALLOWED_DESC = "Customization option is not available for this menu item";
    public static final String INVALID_CUSTOMIZATION_SELECTION_DESC = "Invalid number of customization selections";
}
