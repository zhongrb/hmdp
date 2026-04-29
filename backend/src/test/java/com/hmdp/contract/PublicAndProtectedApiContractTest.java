package com.hmdp.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hmdp.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@AutoConfigureMockMvc
class PublicAndProtectedApiContractTest extends BaseIntegrationTest {

    @Test
    void shouldMatchPublicApiContracts() throws Exception {
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").isEmpty());

        String code = redisTemplate.opsForValue().get("login:code:13800138000");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138000\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.id").exists())
                .andExpect(jsonPath("$.data.user.nickName").exists());

        mockMvc.perform(get("/shops").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].name").exists());

        mockMvc.perform(get("/shops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").exists());

        mockMvc.perform(get("/vouchers/seckill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].status").exists());

        mockMvc.perform(get("/blogs/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.offset").exists());

        mockMvc.perform(get("/blogs/hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].liked").exists());
    }

    @Test
    void shouldRejectProtectedApisWithoutLogin() throws Exception {
        mockMvc.perform(post("/vouchers/seckill/1/claim"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先完成登录后再继续操作"));

        mockMvc.perform(post("/sign"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先完成登录后再继续操作"));

        mockMvc.perform(get("/sign/streak"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先完成登录后再继续操作"));

        mockMvc.perform(post("/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"新店体验\",\"content\":\"环境很好\",\"shopId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先完成登录后再继续操作"));

        mockMvc.perform(post("/blogs/1/like"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先完成登录后再继续操作"));

        mockMvc.perform(get("/follows/common/2"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先完成登录后再继续操作"));
    }

    @Test
    void shouldReturnBadRequestForInvalidContractInputs() throws Exception {
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请求参数不合法"));

        mockMvc.perform(get("/shops").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请求参数不合法"));

        mockMvc.perform(get("/shops/nearby")
                        .param("typeId", "1")
                        .param("x", "-1")
                        .param("y", "31.222771")
                        .param("current", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请求参数不合法"));
    }
}
