package com.jeff.inventorymanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SmokeIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("inventory_db")
        .withUsername("inventory_user")
        .withPassword("inventory_password");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("security.jwt.secret",
            () -> "test-secret-key-that-is-long-enough-for-hs256-tests!");
        registry.add("security.jwt.expiration-seconds", () -> "3600");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin@example.com", "Admin@123!");
    }

    @Test
    void purchaseOrderReceipt_updatesInventory() throws Exception {
        long productId = createProduct("SMOKE-PO-" + UUID.randomUUID(), "Smoke PO Product", 1L);
        int quantityBefore = getInventoryQuantity(productId, 1L);

        MvcResult createResult = mockMvc.perform(post("/api/purchase-orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "supplierId": 1,
                      "destinationLocationId": 1,
                      "notes": "Smoke PO receipt test"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        long purchaseOrderId = readId(createResult);

        mockMvc.perform(post("/api/purchase-orders/" + purchaseOrderId + "/items")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": %d,
                      "orderedQuantity": 10,
                      "unitCost": 5.00
                    }
                    """.formatted(productId)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/purchase-orders/" + purchaseOrderId + "/submit")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUBMITTED"));

        mockMvc.perform(post("/api/purchase-orders/" + purchaseOrderId + "/receive")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RECEIVED"));

        int quantityAfter = getInventoryQuantity(productId, 1L);
        assertThat(quantityAfter).isEqualTo(quantityBefore + 10);

        MvcResult transactionsResult = mockMvc.perform(get("/api/inventory/transactions")
                .param("productId", String.valueOf(productId))
                .param("locationId", "1")
                .param("type", "STOCK_IN")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode transactions = objectMapper.readTree(
            transactionsResult.getResponse().getContentAsString()
        ).get("content");

        assertThat(transactions.isArray()).isTrue();
        assertThat(transactions).anyMatch(tx ->
            tx.get("quantityChange").asInt() == 10
                && tx.get("transactionType").asText().equals("STOCK_IN")
                && tx.get("reason").asText().contains("PO #" + purchaseOrderId)
        );

        mockMvc.perform(post("/api/purchase-orders/" + purchaseOrderId + "/receive")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    void stockTransfer_movesInventoryBetweenLocations() throws Exception {
        long productId = createProduct("SMOKE-TR-" + UUID.randomUUID(), "Smoke Transfer Product", 1L);

        mockMvc.perform(post("/api/inventory/stock-in")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": %d,
                      "locationId": 1,
                      "quantity": 20,
                      "reason": "Seed stock for transfer smoke test"
                    }
                    """.formatted(productId)))
            .andExpect(status().isOk());

        MvcResult transferResult = mockMvc.perform(post("/api/inventory/transfer")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "productId": %d,
                      "fromLocationId": 1,
                      "toLocationId": 2,
                      "quantity": 7,
                      "reason": "Transfer smoke test"
                    }
                    """.formatted(productId)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode transferResponse = objectMapper.readTree(
            transferResult.getResponse().getContentAsString()
        );
        assertThat(transferResponse).hasSize(2);

        int locationA = getInventoryQuantity(productId, 1L);
        int locationB = getInventoryQuantity(productId, 2L);
        assertThat(locationA).isEqualTo(13);
        assertThat(locationB).isEqualTo(7);
        assertThat(locationA + locationB).isEqualTo(20);

        MvcResult transactionsResult = mockMvc.perform(get("/api/inventory/transactions")
                .param("productId", String.valueOf(productId))
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode transactions = objectMapper.readTree(
            transactionsResult.getResponse().getContentAsString()
        ).get("content");

        JsonNode transferOut = null;
        JsonNode transferIn = null;
        for (JsonNode tx : transactions) {
            String type = tx.get("transactionType").asText();
            if (type.equals("TRANSFER_OUT") && tx.get("quantityChange").asInt() == 7) {
                transferOut = tx;
            }
            if (type.equals("TRANSFER_IN") && tx.get("quantityChange").asInt() == 7) {
                transferIn = tx;
            }
        }

        assertThat(transferOut).isNotNull();
        assertThat(transferIn).isNotNull();
        assertThat(transferOut.get("locationId").asLong()).isEqualTo(1L);
        assertThat(transferIn.get("locationId").asLong()).isEqualTo(2L);
        assertThat(transferOut.get("relatedTransactionId").asLong())
            .isEqualTo(transferIn.get("id").asLong());
        assertThat(transferIn.get("relatedTransactionId").asLong())
            .isEqualTo(transferOut.get("id").asLong());
    }

    @Test
    void protectedWorkflow_rejectsUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/inventory"))
            .andExpect(status().isUnauthorized());

        String staffEmail = "staff-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "StaffPass1"
                    }
                    """.formatted(staffEmail)))
            .andExpect(status().isCreated());

        String staffToken = login(staffEmail, "StaffPass1");

        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sku": "STAFF-BLOCKED-%s",
                      "name": "Should Be Blocked",
                      "unitPrice": 1.00,
                      "supplierId": 1,
                      "categoryId": 1
                    }
                    """.formatted(UUID.randomUUID())))
            .andExpect(status().isForbidden());

        String sku = "ADMIN-OK-" + UUID.randomUUID();
        MvcResult createResult = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sku": "%s",
                      "name": "Admin Created Product",
                      "unitPrice": 9.99,
                      "supplierId": 1,
                      "categoryId": 1
                    }
                    """.formatted(sku)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sku").value(sku))
            .andReturn();

        long productId = readId(createResult);

        mockMvc.perform(get("/api/products/" + productId)
                .header("Authorization", "Bearer " + staffToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.sku").value(sku))
            .andExpect(jsonPath("$.name").value("Admin Created Product"));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("accessToken")
            .asText();
    }

    private long createProduct(String sku, String name, long supplierId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sku": "%s",
                      "name": "%s",
                      "unitPrice": 12.50,
                      "supplierId": %d,
                      "categoryId": 1
                    }
                    """.formatted(sku, name, supplierId)))
            .andExpect(status().isCreated())
            .andReturn();

        return readId(result);
    }

    private int getInventoryQuantity(long productId, long locationId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory")
                .param("productId", String.valueOf(productId))
                .param("locationId", String.valueOf(locationId))
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("content");

        if (content == null || !content.isArray() || content.isEmpty()) {
            return 0;
        }

        return content.get(0).get("quantity").asInt();
    }

    private long readId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id")
            .asLong();
    }
}
