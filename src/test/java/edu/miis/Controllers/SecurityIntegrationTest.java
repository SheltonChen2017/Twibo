package edu.miis.Controllers;

import edu.miis.DataTransferPojo.RegistrationForm;
import edu.miis.Entities.Article;
import edu.miis.Entities.UserBean;
import edu.miis.Service.CaptchaService;
import edu.miis.Service.IUserServ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserServ userService;

    private UserBean testUser;

    @BeforeEach
    void createUser() {
        testUser = userService.findByUsername("security-user").orElseGet(() -> {
            RegistrationForm form = new RegistrationForm();
            form.setUsername("security-user");
            form.setPassword("correct horse battery staple");
            form.setBirthday(LocalDate.of(1990, 1, 1));
            form.setSecurityQuestion("Question?");
            form.setSecurityAnswer("Answer");
            form.setCaptcha("unused");
            return userService.register(form);
        });
    }

    @Test
    void publicPageIncludesSecurityHeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("default-src 'self'")));
    }

    @Test
    void protectedPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/mainBlog"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void stateChangingRequestRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/publish")
                        .with(user("security-user").roles("USER"))
                        .param("content", "blocked"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/publish")
                        .with(user("security-user").roles("USER"))
                        .with(csrf())
                        .param("content", "allowed"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void actualLoginAuthenticatesWithStoredPassword() throws Exception {
        mockMvc.perform(formLogin("/loginto")
                        .user("security-user")
                        .password("correct horse battery staple"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("security-user"));
    }

    @Test
    void primaryTemplatesRenderForAuthenticatedUsers() throws Exception {
        Article article = userService.publish("security-user", "A safe rendered post");

        mockMvc.perform(get("/mainBlog").with(user("security-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("mainBlog"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello, security-user")));

        mockMvc.perform(get("/users/" + testUser.getId()).with(user("security-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/individualpage"));

        mockMvc.perform(get("/posts/" + article.getId()).with(user("security-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("Posts/IndividualPost"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("A safe rendered post")));
    }

    @Test
    void registrationRequiresMatchingCaptcha() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CaptchaService.SESSION_KEY, "ABCDE");

        mockMvc.perform(post("/addUser")
                        .session(session)
                        .with(csrf())
                        .param("username", "new-user")
                        .param("password", "a sufficiently long password")
                        .param("birthday", "1991-02-03")
                        .param("securityQuestion", "Question?")
                        .param("securityAnswer", "Answer")
                        .param("captcha", "ABCDE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }
}
