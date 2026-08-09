package com.furnisight.admin.web;

import com.furnisight.admin.account.role.web.RoleController;
import com.furnisight.admin.account.user.web.UserController;
import com.furnisight.admin.audit.web.AuditLogController;
import com.furnisight.admin.catalog.category.web.CategoryController;
import com.furnisight.admin.catalog.inventory.web.InventoryController;
import com.furnisight.admin.catalog.product.web.ProductController;
import com.furnisight.admin.dashboard.web.DashboardController;
import com.furnisight.admin.order.web.OrderController;
import com.furnisight.admin.revenue.web.RevenueController;
import com.furnisight.admin.stats.web.StatsController;
import com.furnisight.admin.voucher.web.VoucherController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerContractTest {

    @Test
    void keepsPublicAdminBasePaths() {
        assertBasePath(UserController.class, "/admin/users");
        assertBasePath(RoleController.class, "/admin");
        assertBasePath(ProductController.class, "/admin/products");
        assertBasePath(CategoryController.class, "/admin/categories");
        assertBasePath(InventoryController.class, "/admin/inventory");
        assertBasePath(OrderController.class, "/admin/orders");
        assertBasePath(VoucherController.class, "/admin/vouchers");
        assertBasePath(DashboardController.class, "/admin/dashboard");
        assertBasePath(StatsController.class, "/admin/stats");
        assertBasePath(RevenueController.class, "/admin");
        assertBasePath(AuditLogController.class, "/admin/audit-logs");

        assertMethodPath(RoleController.class, "getRoles", GetMapping.class, "/roles");
        assertMethodPath(RoleController.class, "updateUserRole", PutMapping.class, "/users/{id}/role");
        assertMethodPath(CategoryController.class, "getCategoryIconOptions", GetMapping.class, "/icon-options");
        assertMethodPath(InventoryController.class, "stockInVariant", PostMapping.class, "/stock-in");
        assertMethodPath(InventoryController.class, "updateVariantThreshold", PutMapping.class,
                "/variants/{variantId}/threshold");
        assertMethodPath(RevenueController.class, "getRevenueSummary", GetMapping.class, "/revenue");
        assertMethodPath(ProductController.class, "deleteProduct", DeleteMapping.class, "/{id}");
    }

    private void assertBasePath(Class<?> controllerType, String expected) {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).containsExactly(expected);
    }

    private void assertMethodPath(
            Class<?> controllerType,
            String methodName,
            Class<? extends Annotation> annotationType,
            String expected) {
        Method method = java.util.Arrays.stream(controllerType.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        Annotation annotation = method.getAnnotation(annotationType);
        assertThat(annotation).isNotNull();
        String[] values;
        if (annotation instanceof GetMapping mapping) {
            values = mapping.value();
        } else if (annotation instanceof PostMapping mapping) {
            values = mapping.value();
        } else if (annotation instanceof PutMapping mapping) {
            values = mapping.value();
        } else if (annotation instanceof DeleteMapping mapping) {
            values = mapping.value();
        } else {
            throw new IllegalArgumentException("Unsupported mapping annotation");
        }
        assertThat(values).containsExactly(expected);
    }
}
