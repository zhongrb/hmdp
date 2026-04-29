package com.hmdp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@AutoConfigureMockMvc
class VoucherAndSignFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldBrowseVoucherListWithoutLogin() throws Exception {
        mockMvc.perform(get("/vouchers/seckill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void shouldRejectClaimAndSignWithoutLogin() throws Exception {
        mockMvc.perform(post("/vouchers/seckill/1/claim"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/sign"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginClaimVoucherAndSignSuccessfully() throws Exception {
        mockMvc.perform(post("/auth/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk());

        String code = redisTemplate.opsForValue().get("login:code:13800138000");
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138000\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).path("data").path("token").asText();

        mockMvc.perform(post("/vouchers/seckill/1/claim")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/sign")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/sign/streak")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldBlockDuplicateVoucherClaimAndRepeatSign() throws Exception {
        String token = loginAs("13900139000");

        mockMvc.perform(post("/vouchers/seckill/2/claim")
                        .header("authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/vouchers/seckill/2/claim")
                        .header("authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请勿重复领取同一张优惠券"));

        mockMvc.perform(post("/sign")
                        .header("authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/sign")
                        .header("authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("今天已经签到过了"));
    }

    @Test
    void shouldCountUvOnlyOncePerVisitor() throws Exception {
        mockMvc.perform(get("/vouchers/seckill").header("X-Visitor-Id", "visitor-a"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/vouchers/seckill").header("X-Visitor-Id", "visitor-a"))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(redisTemplate.opsForHyperLogLog().size(todayUvKey()))
                .isEqualTo(1L);
    }
}
