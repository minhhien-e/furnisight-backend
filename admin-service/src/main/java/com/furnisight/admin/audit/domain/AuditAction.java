package com.furnisight.admin.audit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {
    // ==========================================
    // ROLE (Vai trò)
    // ==========================================
    CREATE_ROLE("create", "Tạo vai trò", "ROLE"),
    UPDATE_ROLE("update", "Cập nhật vai trò", "ROLE"),
    DELETE_ROLE("delete", "Xóa vai trò", "ROLE"),

    // ==========================================
    // ACCOUNT (Tài khoản)
    // ==========================================
    CREATE_USER("create", "Tạo tài khoản", "ACCOUNT"),
    UPDATE_USER("update", "Cập nhật tài khoản", "ACCOUNT"),
    DELETE_USER("delete", "Xóa tài khoản", "ACCOUNT"),
    BAN_USER("update", "Khóa tài khoản", "ACCOUNT"),
    UNBAN_USER("update", "Mở khóa tài khoản", "ACCOUNT"),
    UPDATE_USER_STATUS("update", "Cập nhật trạng thái tài khoản", "ACCOUNT"),
    ASSIGN_ROLE("update", "Gán vai trò", "ACCOUNT"),
    REVOKE_ROLE("update", "Thu hồi vai trò", "ACCOUNT"),

    // ==========================================
    // CATEGORY (Danh mục)
    // ==========================================
    CREATE_CATEGORY("create", "Tạo danh mục", "CATEGORY"),
    UPDATE_CATEGORY("update", "Cập nhật danh mục", "CATEGORY"),
    DELETE_CATEGORY("delete", "Xóa danh mục", "CATEGORY"),

    // ==========================================
    // PRODUCT (Sản phẩm)
    // ==========================================
    CREATE_PRODUCT("create", "Tạo sản phẩm", "PRODUCT"),
    UPDATE_PRODUCT("update", "Cập nhật sản phẩm", "PRODUCT"),
    DELETE_PRODUCT("delete", "Xóa sản phẩm", "PRODUCT"),

    // ==========================================
    // INVENTORY (Tồn kho)
    // ==========================================
    STOCK_IN("update", "Nhập kho", "INVENTORY"),
    UPDATE_INVENTORY_THRESHOLD("update", "Cập nhật cảnh báo tồn kho", "PRODUCT_VARIANT"),

    // ==========================================
    // MARKETING & NOTIFICATION & VOUCHER
    // ==========================================
    CREATE_CAMPAIGN("create", "Tạo chiến dịch marketing", "MARKETING_CAMPAIGN"),
    UPDATE_CAMPAIGN("update", "Cập nhật chiến dịch marketing", "MARKETING_CAMPAIGN"),
    DELETE_CAMPAIGN("delete", "Xóa chiến dịch marketing", "MARKETING_CAMPAIGN"),

    CREATE_COMBO("create", "Tạo combo khuyến mãi", "PROMOTION_COMBO"),
    UPDATE_COMBO("update", "Cập nhật combo khuyến mãi", "PROMOTION_COMBO"),
    DELETE_COMBO("delete", "Xóa combo khuyến mãi", "PROMOTION_COMBO"),

    CREATE_MARKETING_NOTIFICATION("create", "Tạo thông báo marketing", "MARKETING_NOTIFICATION"),
    UPDATE_MARKETING_NOTIFICATION("update", "Cập nhật thông báo marketing", "MARKETING_NOTIFICATION"),
    DELETE_MARKETING_NOTIFICATION("delete", "Xóa thông báo marketing", "MARKETING_NOTIFICATION"),

    CREATE_MESSAGE_TEMPLATE("create", "Tạo mẫu tin nhắn", "MESSAGE_TEMPLATE"),
    UPDATE_MESSAGE_TEMPLATE("update", "Cập nhật mẫu tin nhắn", "MESSAGE_TEMPLATE"),
    DELETE_MESSAGE_TEMPLATE("delete", "Xóa mẫu tin nhắn", "MESSAGE_TEMPLATE"),

    CREATE_NOTIFICATION("create", "Tạo mẫu thông báo", "NOTIFICATION"),
    UPDATE_NOTIFICATION("update", "Cập nhật mẫu thông báo", "NOTIFICATION"),
    DELETE_NOTIFICATION("delete", "Xóa mẫu thông báo", "NOTIFICATION"),
    
    CREATE_VOUCHER("create", "Tạo voucher", "VOUCHER"),
    UPDATE_VOUCHER("update", "Cập nhật voucher", "VOUCHER"),
    DELETE_VOUCHER("delete", "Xóa voucher", "VOUCHER"),
    PUBLISH_VOUCHER("update", "Phát hành voucher", "VOUCHER_PUBLISH");

    private final String type;
    private final String description;
    private final String resourceType;
}
