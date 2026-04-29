package com.hmdp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.http.MediaType;

@AutoConfigureMockMvc
class AuthAndShopFlowIntegrationTest extends BaseIntegrationTest {


    @BeforeEach
    void seedGeo() {
        redisTemplate.opsForGeo().add("shop:geo:1",
                java.util.List.of(
                        new RedisGeoCommands.GeoLocation<>("1", new org.springframework.data.geo.Point(121.490317, 31.222771))
                ));
        redisTemplate.opsForGeo().add("shop:geo:2",
                java.util.List.of(
                        new RedisGeoCommands.GeoLocation<>("2", new org.springframework.data.geo.Point(121.436525, 31.193446))
                ));
    }

    @Test
    void shouldBrowsePublicShopsAndNearbyWithoutLogin() throws Exception {
        mockMvc.perform(get("/shops").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").exists());

        mockMvc.perform(get("/shops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(get("/shops/nearby")
                        .param("typeId", "1")
                        .param("x", "121.490317")
                        .param("y", "31.222771")
                        .param("current", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void shouldSendCodeAndLoginSuccessfully() throws Exception {
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String code = redisTemplate.opsForValue().get("login:code:13800138000");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138000\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.id").value(1));
    }

    @Test
    void shouldReturnUnauthorizedForProtectedOperationWithoutLogin() throws Exception {
        mockMvc.perform(post("/sign"))
                .andExpect(status().isUnauthorized());
    }
}
