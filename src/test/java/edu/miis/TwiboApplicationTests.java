package edu.miis;

import edu.miis.domain.BrokerConnection;
import edu.miis.domain.BrokerEnvironment;
import edu.miis.domain.BrokerProvider;
import edu.miis.repository.BrokerConnectionRepository;
import edu.miis.service.TwiboService;
import edu.miis.web.SignupForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.Cookie;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TwiboApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired TwiboService service;
    @Autowired BrokerConnectionRepository brokerConnections;

    @Test
    void homePageLoads() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
    }

    @Test
    void protectedPageRedirectsToLogin() throws Exception {
        mvc.perform(get("/feed")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/trading")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/settings/connections")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DirtiesContext
    void accountPostAndCommentFlowWorks() throws Exception {
        mvc.perform(post("/signup").with(csrf())
                        .param("username", "student_user")
                        .param("password", "correct-horse-battery")
                        .param("birthday", "2000-01-01")
                        .param("securityQuestion", "First pet?")
                        .param("securityAnswer", "Mochi"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        var loginResult = mvc.perform(post("/login").with(csrf())
                        .param("username", "student_user")
                        .param("password", "correct-horse-battery"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"))
                .andReturn();
        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");

        mvc.perform(post("/posts").with(csrf()).cookie(sessionCookie).param("content", "My first repaired post"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/feed").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("My first repaired post")));

        mvc.perform(post("/posts/1/comments").with(csrf()).cookie(sessionCookie).param("content", "It works!"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/posts/1").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("It works!")));

        mvc.perform(get("/settings/connections").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Connect Alpaca (coming later)")));
    }

    @Test
    void csrfProtectsStateChangingRequests() throws Exception {
        mvc.perform(post("/login").param("username", "nobody").param("password", "nothing"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DirtiesContext
    void brokerConnectionMetadataIsScopedToItsOwner() throws Exception {
        service.register(signup("first_trader"));
        var second = service.register(signup("second_trader"));
        brokerConnections.save(new BrokerConnection(second, BrokerProvider.ALPACA, BrokerEnvironment.PAPER));

        Cookie firstSession = login("first_trader");
        mvc.perform(get("/settings/connections").cookie(firstSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("ALPACA PAPER"))));

        Cookie secondSession = login("second_trader");
        mvc.perform(get("/settings/connections").cookie(secondSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ALPACA PAPER")));
    }

    @Test
    void readinessEndpointIsAvailableToCloudProbes() throws Exception {
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private SignupForm signup(String username) {
        SignupForm form = new SignupForm();
        form.setUsername(username);
        form.setPassword("correct-horse-battery");
        form.setBirthday(LocalDate.of(2000, 1, 1));
        form.setSecurityQuestion("First pet?");
        form.setSecurityAnswer("Mochi");
        return form;
    }

    private Cookie login(String username) throws Exception {
        return mvc.perform(post("/login").with(csrf())
                        .param("username", username)
                        .param("password", "correct-horse-battery"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"))
                .andReturn().getResponse().getCookie("SESSION");
    }
}
