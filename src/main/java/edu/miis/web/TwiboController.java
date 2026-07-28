package edu.miis.web;

import edu.miis.domain.User;
import edu.miis.service.TwiboService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Controller
public class TwiboController {
    private static final Duration RECOVERY_WINDOW = Duration.ofMinutes(10);
    private final TwiboService service;
    private final AuthenticationRateLimiter rateLimiter;

    public TwiboController(TwiboService service, AuthenticationRateLimiter rateLimiter) {
        this.service = service;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/")
    String home(HttpSession session) {
        return session.getAttribute("userId") == null ? "home" : "redirect:/feed";
    }

    @GetMapping("/signup")
    String signup(Model model) {
        if (!model.containsAttribute("signupForm")) model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    String signup(@Valid @ModelAttribute SignupForm signupForm, BindingResult errors,
                  HttpServletRequest request, Model model) {
        if (errors.hasErrors()) return "signup";
        try {
            User user = service.register(signupForm);
            signIn(request, user);
            return "redirect:/feed";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "signup";
        }
    }

    @GetMapping("/login")
    String login() { return "login"; }

    @PostMapping("/login")
    String login(@RequestParam String username, @RequestParam String password,
                 HttpServletRequest request, HttpServletResponse response, Model model) {
        if (rateLimiter.loginRetryAfter(username, request.getRemoteAddr()).isPresent()) {
            return throttled(response, model, "Too many sign-in attempts. Wait a few minutes and try again.", "login");
        }
        return service.authenticate(username, password).map(user -> {
            rateLimiter.recordLoginSuccess(username);
            signIn(request, user);
            return "redirect:/feed";
        }).orElseGet(() -> {
            rateLimiter.recordLoginFailure(username, request.getRemoteAddr());
            if (rateLimiter.loginRetryAfter(username, request.getRemoteAddr()).isPresent()) {
                return throttled(response, model,
                        "Too many sign-in attempts. Wait a few minutes and try again.", "login");
            }
            model.addAttribute("error", "Incorrect username or password.");
            return "login";
        });
    }

    @PostMapping("/logout")
    String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/feed")
    String feed(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        Long userId = userId(session);
        Page<edu.miis.domain.Post> postPage = service.feed(userId, page);
        model.addAttribute("currentUser", service.requireUser(userId));
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("postPage", postPage);
        return "feed";
    }

    @PostMapping("/posts")
    String publish(@RequestParam String content, HttpSession session, RedirectAttributes redirect) {
        try { service.publish(userId(session), content); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/feed";
    }

    @GetMapping("/posts/{id}")
    String post(@PathVariable Long id, Model model) {
        model.addAttribute("post", service.requirePost(id));
        model.addAttribute("comments", service.commentsFor(id));
        return "post";
    }

    @PostMapping("/posts/{id}/comments")
    String comment(@PathVariable Long id, @RequestParam String content, HttpSession session,
                   RedirectAttributes redirect) {
        try { service.comment(userId(session), id, content); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/posts/" + id;
    }

    @GetMapping("/users/{id}")
    String profile(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
                   HttpSession session, Model model) {
        User profile = service.requireUser(id);
        Long current = userId(session);
        Page<edu.miis.domain.Post> postPage = service.postsBy(id, page);
        model.addAttribute("profile", profile);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("postPage", postPage);
        model.addAttribute("ownProfile", current.equals(id));
        model.addAttribute("following", !current.equals(id) && service.isFollowing(current, id));
        return "profile";
    }

    @PostMapping("/users/{id}/follow")
    String follow(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            boolean following = service.toggleFollow(userId(session), id);
            redirect.addFlashAttribute("message", following ? "You are now following this user." : "You unfollowed this user.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/users/" + id;
    }

    @GetMapping("/search")
    String search(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("q", q);
        try {
            model.addAttribute("users", q.isBlank() ? List.of() : service.search(q));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("users", List.of());
            model.addAttribute("error", ex.getMessage());
        }
        return "search";
    }

    @GetMapping("/recover")
    String recover(HttpSession session) {
        clearRecovery(session);
        return "recover";
    }

    @PostMapping("/recover/verify")
    String verifyRecovery(@RequestParam String username, @RequestParam String recoveryPhrase,
                          HttpServletRequest request, HttpServletResponse response, Model model) {
        String address = request.getRemoteAddr();
        if (rateLimiter.recoveryRetryAfter(username, address).isPresent()) {
            return throttled(response, model,
                    "Too many recovery attempts. Wait a few minutes and try again.", "recover");
        }
        return service.verifyRecoveryPhrase(username, recoveryPhrase).map(user -> {
            rateLimiter.recordRecoverySuccess(username);
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            HttpSession session = request.getSession(true);
            session.removeAttribute("userId");
            session.removeAttribute("username");
            session.setAttribute("recoveryUserId", user.getId());
            session.setAttribute("recoveryExpiresAt", Instant.now().plus(RECOVERY_WINDOW).toEpochMilli());
            return "reset-password";
        }).orElseGet(() -> {
            rateLimiter.recordRecoveryFailure(username, address);
            if (rateLimiter.recoveryRetryAfter(username, address).isPresent()) {
                return throttled(response, model,
                        "Too many recovery attempts. Wait a few minutes and try again.", "recover");
            }
            model.addAttribute("error", "We could not verify those recovery details.");
            return "recover";
        });
    }

    @PostMapping("/recover/reset")
    String resetPassword(@RequestParam String password, HttpSession session, Model model) {
        Object id = session.getAttribute("recoveryUserId");
        Object expiry = session.getAttribute("recoveryExpiresAt");
        if (!(id instanceof Long userId) || !(expiry instanceof Long expiresAt)
                || Instant.now().toEpochMilli() >= expiresAt) {
            clearRecovery(session);
            return "redirect:/recover";
        }
        try {
            service.resetPassword(userId, password);
            clearRecovery(session);
            model.addAttribute("message", "Password updated. You can sign in now.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "reset-password";
        }
    }

    private void signIn(HttpServletRequest request, User user) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        HttpSession session = request.getSession(true);
        clearRecovery(session);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
    }

    private Long userId(HttpSession session) { return (Long) session.getAttribute("userId"); }

    private String throttled(HttpServletResponse response, Model model, String message, String view) {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", Long.toString(Duration.ofMinutes(15).toSeconds()));
        model.addAttribute("error", message);
        return view;
    }

    private void clearRecovery(HttpSession session) {
        session.removeAttribute("recoveryUserId");
        session.removeAttribute("recoveryExpiresAt");
    }
}
