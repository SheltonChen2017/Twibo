package edu.miis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TwiboApplicationTests {
    @Autowired MockMvc mvc;

    @Test
    void homePageLoads() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
    }

    @Test
    void protectedPageRedirectsToLogin() throws Exception {
        mvc.perform(get("/feed")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    @DirtiesContext
    void accountPostAndCommentFlowWorks() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/signup").with(csrf()).session(session)
                        .param("username", "student_user")
                        .param("password", "correct-horse-battery")
                        .param("birthday", "2000-01-01")
                        .param("securityQuestion", "First pet?")
                        .param("securityAnswer", "Mochi"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        mvc.perform(post("/posts").with(csrf()).session(session).param("content", "My first repaired post"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/feed").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("My first repaired post")));

        mvc.perform(post("/posts/1/comments").with(csrf()).session(session).param("content", "It works!"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/posts/1").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("It works!")));
    }

    @Test
    void csrfProtectsStateChangingRequests() throws Exception {
        mvc.perform(post("/login").param("username", "nobody").param("password", "nothing"))
                .andExpect(status().isForbidden());
    }
}
