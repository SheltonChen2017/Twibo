package edu.miis.security;

import edu.miis.service.TwiboService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TwiboAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final TwiboService service;

    public TwiboAuthenticationSuccessHandler(TwiboService service) {
        this.service = service;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        edu.miis.domain.User user = service.findUser(authentication.getName())
                .orElseThrow(() -> new ServletException("Authenticated Twibo account no longer exists."));
        request.getSession(true).setAttribute("userId", user.getId());
        request.getSession().setAttribute("username", user.getUsername());
        response.sendRedirect(request.getContextPath() + "/feed");
    }
}
