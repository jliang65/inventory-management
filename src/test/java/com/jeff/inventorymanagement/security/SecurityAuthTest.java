package com.jeff.inventorymanagement.security;

import com.jeff.inventorymanagement.config.SecurityConfig;
import com.jeff.inventorymanagement.controller.AdminUserController;
import com.jeff.inventorymanagement.controller.AuthController;
import com.jeff.inventorymanagement.controller.ProductController;
import com.jeff.inventorymanagement.controller.PurchaseOrderController;
import com.jeff.inventorymanagement.dto.AuthResponse;
import com.jeff.inventorymanagement.dto.UserResponse;
import com.jeff.inventorymanagement.entity.Role;
import com.jeff.inventorymanagement.exception.GlobalExceptionHandler;
import com.jeff.inventorymanagement.service.AuthService;
import com.jeff.inventorymanagement.service.ProductService;
import com.jeff.inventorymanagement.service.PurchaseOrderService;
import com.jeff.inventorymanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
    AuthController.class,
    AdminUserController.class,
    ProductController.class,
    PurchaseOrderController.class
})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SecurityAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private ProductService productService;

    @MockBean(name = "purchaseOrderService")
    private PurchaseOrderService purchaseOrderService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void validLogin_returnsToken() throws Exception {
        when(authService.login(any())).thenReturn(
            new AuthResponse("test-token", "Bearer", 3600)
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "staff@example.com",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("test-token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void invalidCredentials_areRejected() throws Exception {
        when(authService.login(any())).thenThrow(
            new BadCredentialsException("Bad credentials")
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "staff@example.com",
                      "password": "wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void protectedEndpoint_rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUsers_cannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(put("/api/admin/users/1/role")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "role": "ADMIN"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminUsers_canChangeRoles() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("staff@example.com");
        response.setRole(Role.ADMIN);
        response.setEnabled(true);
        response.setCreatedAt(LocalDateTime.now());

        when(userService.updateRole(eq(1L), eq(Role.ADMIN))).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/1/role")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "role": "ADMIN"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void staffCannotCancelSubmittedOrders() throws Exception {
        when(purchaseOrderService.isDraft(1L)).thenReturn(false);

        mockMvc.perform(post("/api/purchase-orders/1/cancel")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF"))))
            .andExpect(status().isForbidden());
    }
}
