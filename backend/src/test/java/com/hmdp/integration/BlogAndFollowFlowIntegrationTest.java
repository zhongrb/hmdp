package com.hmdp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@AutoConfigureMockMvc
class BlogAndFollowFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldBrowseFeedAndHotBlogsWithoutLogin() throws Exception {
        mockMvc.perform(get("/blogs/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].title").exists());

        mockMvc.perform(get("/blogs/hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists());
    }

    @Test
    void shouldRejectPublishLikeAndCommonFollowWithoutLogin() throws Exception {
        mockMvc.perform(post("/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"新店体验\",\"content\":\"环境很好\",\"shopId\":1}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/blogs/1/like"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/follows/common/2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPublishLikeAndFollowAfterLogin() throws Exception {
        String token = loginAs("13800138000");

        mockMvc.perform(post("/blogs")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"夜宵新发现\",\"content\":\"出餐很快，口味在线。\",\"shopId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());

        mockMvc.perform(post("/blogs/1/like")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/follows/3")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/follows/common/2")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/follows/3")
                        .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
