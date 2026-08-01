package edu.miis.web;

import edu.miis.domain.User;
import edu.miis.service.TwiboService;
import edu.miis.service.BrokerConnectionService;
import edu.miis.trading.TradingAgentClient;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TwiboController {
    private final TwiboService service;
    private final TradingAgentClient tradingAgentClient;
    private final BrokerConnectionService brokerConnections;

    public TwiboController(TwiboService service, TradingAgentClient tradingAgentClient,
                           BrokerConnectionService brokerConnections) {
        this.service = service;
        this.tradingAgentClient = tradingAgentClient;
        this.brokerConnections = brokerConnections;
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
                  Model model) {
        if (errors.hasErrors()) return "signup";
        try {
            service.register(signupForm);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "signup";
        }
    }

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/feed")
    String feed(HttpSession session, Model model) {
        Long userId = userId(session);
        model.addAttribute("currentUser", service.requireUser(userId));
        model.addAttribute("posts", service.feed(userId));
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
    String profile(@PathVariable Long id, HttpSession session, Model model) {
        User profile = service.requireUser(id);
        Long current = userId(session);
        model.addAttribute("profile", profile);
        model.addAttribute("posts", service.postsBy(id));
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
        model.addAttribute("users", q.isBlank() ? java.util.List.of() : service.search(q));
        return "search";
    }

    @GetMapping("/trading")
    String trading(HttpSession session, Model model) {
        Long currentUserId = userId(session);
        model.addAttribute("currentUser", service.requireUser(currentUserId));
        model.addAttribute("trading", tradingAgentClient.loadDashboard(
                currentUserId, brokerConnections.hasConnectedPaperAlpaca(currentUserId)));
        return "trading";
    }

    @GetMapping("/settings/connections")
    String connections(HttpSession session, Model model) {
        Long currentUserId = userId(session);
        model.addAttribute("currentUser", service.requireUser(currentUserId));
        model.addAttribute("connections", brokerConnections.connectionsFor(currentUserId));
        return "connections";
    }

    @GetMapping("/recover")
    String recover() { return "recover"; }

    @PostMapping("/recover/question")
    String recoveryQuestion(@RequestParam String username, HttpSession session, Model model) {
        return service.findUser(username).map(user -> {
            session.setAttribute("recoveryUserId", user.getId());
            model.addAttribute("question", user.getSecurityQuestion());
            return "recover-answer";
        }).orElseGet(() -> {
            model.addAttribute("error", "No account has that username.");
            return "recover";
        });
    }

    @PostMapping("/recover/verify")
    String verifyRecovery(@RequestParam String answer, HttpSession session, Model model) {
        Object value = session.getAttribute("recoveryUserId");
        if (!(value instanceof Long id)) return "redirect:/recover";
        User user = service.requireUser(id);
        if (!service.verifyRecoveryAnswer(user, answer)) {
            model.addAttribute("question", user.getSecurityQuestion());
            model.addAttribute("error", "That answer does not match.");
            return "recover-answer";
        }
        session.setAttribute("recoveryVerified", true);
        return "reset-password";
    }

    @PostMapping("/recover/reset")
    String resetPassword(@RequestParam String password, HttpSession session, Model model) {
        Object id = session.getAttribute("recoveryUserId");
        if (!(id instanceof Long userId) || !Boolean.TRUE.equals(session.getAttribute("recoveryVerified"))) {
            return "redirect:/recover";
        }
        try {
            service.resetPassword(userId, password);
            session.removeAttribute("recoveryUserId");
            session.removeAttribute("recoveryVerified");
            model.addAttribute("message", "Password updated. You can sign in now.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "reset-password";
        }
    }

    private Long userId(HttpSession session) { return (Long) session.getAttribute("userId"); }
}
