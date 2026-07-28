package edu.miis;

import edu.miis.domain.Post;
import edu.miis.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TwiboApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired PostRepository posts;

    @Test
    void homePageLoads() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Share what matters")))
                .andExpect(content().string(not(containsString("Catopia"))));
    }

    @Test
    void protectedPageRedirectsToLogin() throws Exception {
        mvc.perform(get("/feed")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void accountPostAndCommentFlowWorks() throws Exception {
        MvcResult signupResult = signUp("student_user", "correct-horse-battery", "Mochi keeps my secrets");
        Cookie sessionCookie = signupResult.getResponse().getCookie("SESSION");

        mvc.perform(post("/posts").with(csrf()).cookie(sessionCookie).param("content", "My first repaired post"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/feed").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My first repaired post")));

        Long postId = posts.findAll().stream()
                .filter(post -> post.getContent().equals("My first repaired post"))
                .map(Post::getId)
                .findFirst()
                .orElseThrow();

        mvc.perform(post("/posts/{id}/comments", postId).with(csrf()).cookie(sessionCookie)
                        .param("content", "It works!"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/posts/{id}", postId).cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("It works!")));
    }

    @Test
    void csrfProtectsStateChangingRequests() throws Exception {
        mvc.perform(post("/login").param("username", "nobody").param("password", "nothing"))
                .andExpect(status().isForbidden());
    }

    @Test
    void signupCollectsLessPersonalInformation() throws Exception {
        mvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No email or birthday required")))
                .andExpect(content().string(not(containsString("type=\"date\""))))
                .andExpect(content().string(not(containsString("Recovery question"))));
    }

    @Test
    void credentialEndpointsUseGenericErrorsAndThrottleRepeatedFailures() throws Exception {
        signUp("recovery_user", "a-secure-password", "the quiet cedar path");
        signUp("bcrypt_boundary", "a".repeat(72), "another private phrase");

        mvc.perform(post("/login").with(csrf())
                        .param("username", "bcrypt_boundary")
                        .param("password", "a".repeat(72) + "different-suffix"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Incorrect username or password.")));

        mvc.perform(post("/recover/verify").with(csrf())
                        .param("username", "recovery_user")
                        .param("recoveryPhrase", "definitely wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("We could not verify those recovery details.")));

        mvc.perform(post("/recover/verify").with(csrf())
                        .param("username", "account_that_does_not_exist")
                        .param("recoveryPhrase", "definitely wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("We could not verify those recovery details.")));

        for (int attempt = 1; attempt < 10; attempt++) {
            mvc.perform(post("/login").with(csrf())
                            .with(request -> { request.setRemoteAddr("192.0.2.50"); return request; })
                            .param("username", "rate_limited_user")
                            .param("password", "wrong-password"))
                    .andExpect(status().isOk());
        }
        mvc.perform(post("/login").with(csrf())
                        .with(request -> { request.setRemoteAddr("192.0.2.50"); return request; })
                        .param("username", "rate_limited_user")
                        .param("password", "wrong-password"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"))
                .andExpect(content().string(containsString("Too many sign-in attempts")));
    }

    @Test
    void recoveryAuthorizationIsShortLivedAndCanResetPassword() throws Exception {
        MvcResult signupResult = signUp("reset_user", "original-password", "silver rain on sunday");
        Cookie sessionCookie = signupResult.getResponse().getCookie("SESSION");

        MvcResult verifyResult = mvc.perform(post("/recover/verify").with(csrf()).cookie(sessionCookie)
                        .param("username", "reset_user")
                        .param("recoveryPhrase", "silver rain on sunday"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(content().string(containsString("expires in 10 minutes")))
                .andReturn();

        Cookie rotatedCookie = verifyResult.getResponse().getCookie("SESSION");
        if (rotatedCookie == null) rotatedCookie = sessionCookie;

        mvc.perform(post("/recover/reset").with(csrf()).cookie(rotatedCookie)
                        .param("password", "new-password-that-is-safe"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Password updated")));

        mvc.perform(post("/login").with(csrf())
                        .param("username", "reset_user")
                        .param("password", "new-password-that-is-safe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));
    }

    @Test
    void responsesIncludeDefenseInDepthSecurityHeaders() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy", containsString("camera=()")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void missingResourcesHaveAFriendly404Page() throws Exception {
        mvc.perform(get("/this-page-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("That page wandered off")));
    }

    @Test
    void readinessEndpointIsAvailableToCloudProbes() throws Exception {
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private MvcResult signUp(String username, String password, String recoveryPhrase) throws Exception {
        return mvc.perform(post("/signup").with(csrf())
                        .param("username", username)
                        .param("password", password)
                        .param("recoveryPhrase", recoveryPhrase))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"))
                .andReturn();
    }
}
