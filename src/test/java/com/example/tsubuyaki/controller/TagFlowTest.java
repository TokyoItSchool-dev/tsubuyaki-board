package com.example.tsubuyaki.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tagflow;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class TagFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("タグ導線_投稿作成後_タグ別一覧に作成した投稿を表示する")
    void タグ導線_投稿作成後_タグ別一覧に作成した投稿を表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .param("author", "alice")
                        .param("avatarColor", "BLUE")
                        .param("body", "Spring Bootを勉強しています。 #Java #SpringBoot #社内勉強会"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/tags/{name}", "SpringBoot"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString(">Spring Bootを勉強しています。</a>")))
                .andExpect(content().string(containsString("href=\"/tags/SpringBoot\"")));

        mockMvc.perform(get("/tags/{name}", "社内勉強会"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("#社内勉強会 の投稿")))
                .andExpect(content().string(containsString("href=\"/tags/%E7%A4%BE%E5%86%85%E5%8B%89%E5%BC%B7%E4%BC%9A\"")));
    }
}
