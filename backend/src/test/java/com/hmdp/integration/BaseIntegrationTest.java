package com.hmdp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.utils.RedisConstants;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
public abstract class BaseIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.39")
            .withDatabaseName("hmdp")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/schema.sql");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(6379);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected VoucherMapper voucherMapper;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @BeforeEach
    void resetSqlInitMode() {
        preloadVoucherStocks();
    }

    protected String loginAs(String phone) throws Exception {
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\"}"))
                .andExpect(status().isOk());
        String code = redisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.path("data").path("token").asText();
    }

    protected String todayUvKey() {
        return RedisConstants.UV_KEY + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private void preloadVoucherStocks() {
        voucherMapper.selectList(null).forEach(voucher -> {
            redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(), String.valueOf(voucher.getStock()));
            redisTemplate.delete(RedisConstants.SECKILL_ORDER_KEY + voucher.getId());
        });
    }
}
