package edu.miis.Controllers;

import edu.miis.DataTransferPojo.ArticleTransferPojo;
import edu.miis.DataTransferPojo.CommentTransferPojo;
import edu.miis.DataTransferPojo.RegistrationForm;
import edu.miis.DataTransferPojo.UserTransferPojo;
import edu.miis.Entities.UserBean;
import edu.miis.Service.CaptchaService;
import edu.miis.Service.IUserServ;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private static final String RESET_USERNAME = "passwordResetUsername";
    private static final String RESET_EXPIRES_AT = "passwordResetExpiresAt";
    private static final String RESET_FAILURES = "passwordResetFailures";
    private static final String RESET_LOCKED_UNTIL = "passwordResetLockedUntil";
    private static final int MAX_RESET_FAILURES = 5;

    private final IUserServ userService;
    private final CaptchaService captchaService;
    private final long passwordResetTtlMinutes;

    public UserController(
            IUserServ userService,
            CaptchaService captchaService,
            @Value("${twibo.password-reset.ttl-minutes:10}") long passwordResetTtlMinutes
    ) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.passwordResetTtlMinutes = Math.max(1, passwordResetTtlMinutes);
    }

    @PostMapping("/addUser")
    public String addUser(
            @Valid @ModelAttribute("registration") RegistrationForm registration,
            BindingResult bindingResult,
            HttpSession session
    ) {
        if (!captchaService.matches(registration.getCaptcha(), session.getAttribute(CaptchaService.SESSION_KEY))) {
            bindingResult.rejectValue("captcha", "captcha.invalid", "The verification code is incorrect.");
        }
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            userService.register(registration);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("username", "username.unavailable", exception.getMessage());
            return "signup";
        }
        session.removeAttribute(CaptchaService.SESSION_KEY);
        return "redirect:/login?registered";
    }

    @GetMapping("/mainBlog")
    public String mainBlog(Principal principal, Model model) {
        model.addAttribute("currentUser", userService.requireUser(principal.getName()));
        return "mainBlog";
    }

    @GetMapping("/verify")
    @ResponseBody
    public Map<String, Boolean> verifyUsername(@RequestParam String username) {
        return Map.of("available", userService.findByUsername(username).isEmpty());
    }

    @GetMapping("/Retrieve")
    public String retrievePassword() {
        return "passwordRetrieval";
    }

    @GetMapping(value = "/veriCode", produces = MediaType.IMAGE_PNG_VALUE)
    public void verificationCode(HttpSession session, HttpServletResponse response) throws IOException {
        String code = captchaService.createCode();
        session.setAttribute(CaptchaService.SESSION_KEY, code);
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader("Cache-Control", CacheControl.noStore().getHeaderValue());
        ImageIO.write(captchaService.render(code), "png", response.getOutputStream());
    }

    @GetMapping("/password-recovery/questions")
    @ResponseBody
    public List<String> passwordRecoveryQuestions(@RequestParam String username) {
        return userService.loadSecurityQuestions(username);
    }

    @PostMapping("/verifyQA")
    public String verifySecurityAnswer(
            @RequestParam String username,
            @RequestParam String question,
            @RequestParam String answer,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Object lockedUntil = session.getAttribute(RESET_LOCKED_UNTIL);
        if (lockedUntil instanceof Instant locked && Instant.now().isBefore(locked)) {
            redirectAttributes.addFlashAttribute("error", "Too many attempts. Please wait a few minutes and try again.");
            return "redirect:/Retrieve";
        }
        if (!userService.verifySecurityAnswer(username, question, answer)) {
            int failures = session.getAttribute(RESET_FAILURES) instanceof Integer count ? count + 1 : 1;
            session.setAttribute(RESET_FAILURES, failures);
            if (failures >= MAX_RESET_FAILURES) {
                session.setAttribute(RESET_LOCKED_UNTIL, Instant.now().plus(5, ChronoUnit.MINUTES));
                session.removeAttribute(RESET_FAILURES);
            }
            redirectAttributes.addFlashAttribute("error", "The account information did not match.");
            return "redirect:/Retrieve";
        }
        session.removeAttribute(RESET_FAILURES);
        session.removeAttribute(RESET_LOCKED_UNTIL);
        session.setAttribute(RESET_USERNAME, username);
        session.setAttribute(RESET_EXPIRES_AT, Instant.now().plus(passwordResetTtlMinutes, ChronoUnit.MINUTES));
        return "redirect:/new-password";
    }

    @GetMapping("/new-password")
    public String newPassword(HttpSession session, Model model) {
        if (!hasValidPasswordReset(session)) {
            return "redirect:/Retrieve";
        }
        model.addAttribute("resetUsername", session.getAttribute(RESET_USERNAME));
        return "newpassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!hasValidPasswordReset(session)) {
            redirectAttributes.addFlashAttribute("error", "The password reset link has expired.");
            return "redirect:/Retrieve";
        }
        try {
            userService.resetPassword((String) session.getAttribute(RESET_USERNAME), password);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/new-password";
        }
        session.removeAttribute(RESET_USERNAME);
        session.removeAttribute(RESET_EXPIRES_AT);
        session.removeAttribute(RESET_FAILURES);
        session.removeAttribute(RESET_LOCKED_UNTIL);
        return "redirect:/login?passwordReset";
    }

    @PostMapping("/publish")
    public String publish(
            Principal principal,
            @RequestParam String content,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.publish(principal.getName(), content);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/mainBlog";
    }

    @GetMapping("/users/{userId}")
    public String visitUser(@PathVariable Long userId, Principal principal, Model model) {
        UserBean user = userService.requireUser(userId);
        UserBean currentUser = userService.requireUser(principal.getName());
        model.addAttribute("individual", user);
        model.addAttribute("ownProfile", currentUser.getId().equals(userId));
        model.addAttribute("following", userService.isFollowing(principal.getName(), userId));
        return "user/individualpage";
    }

    @GetMapping("/user/visituserID={userId}")
    public String redirectLegacyUserUrl(@PathVariable Long userId) {
        return "redirect:/users/" + userId;
    }

    @GetMapping("/posts/{articleId}")
    public String viewPost(@PathVariable Long articleId, Model model) {
        model.addAttribute("article", userService.requireArticle(articleId));
        return "Posts/IndividualPost";
    }

    @GetMapping("/Posts/articleId={articleId}")
    public String redirectLegacyPostUrl(@PathVariable Long articleId) {
        return "redirect:/posts/" + articleId;
    }

    @PostMapping("/users/{userId}/follow")
    public String toggleFollow(
            @PathVariable Long userId,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean followed = userService.toggleFollow(principal.getName(), userId);
            redirectAttributes.addFlashAttribute("message", followed ? "You are now following this user." : "User unfollowed.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/users/" + userId;
    }

    @PostMapping("/posts/{articleId}/comments")
    public String addComment(
            @PathVariable Long articleId,
            @RequestParam String content,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.addComment(principal.getName(), articleId, content);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/posts/" + articleId;
    }

    @GetMapping("/api/feed")
    @ResponseBody
    public List<ArticleTransferPojo> feed(
            Principal principal,
            @RequestParam(defaultValue = "0") int page
    ) {
        return userService.loadFeed(principal.getName(), page);
    }

    @GetMapping("/api/posts/{articleId}/comments")
    @ResponseBody
    public List<CommentTransferPojo> comments(@PathVariable Long articleId) {
        return userService.loadComments(articleId);
    }

    @GetMapping("/api/users/{userId}/posts")
    @ResponseBody
    public List<ArticleTransferPojo> individualPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return userService.loadIndividualPage(userId, page);
    }

    @PostMapping("/search")
    public String search(@RequestParam String name, Model model) {
        List<UserTransferPojo> users = userService.searchByName(name);
        model.addAttribute("users", users);
        model.addAttribute("query", name);
        return "searchPage";
    }

    @PostMapping("/posts/{articleId}/reposts")
    public String repost(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "") String comment,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean created = userService.repost(principal.getName(), articleId, comment);
            redirectAttributes.addFlashAttribute(
                    created ? "message" : "error",
                    created ? "Post shared." : "You have already shared this post."
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/posts/" + articleId;
    }

    private static boolean hasValidPasswordReset(HttpSession session) {
        Object username = session.getAttribute(RESET_USERNAME);
        Object expiresAt = session.getAttribute(RESET_EXPIRES_AT);
        if (!(username instanceof String) || !(expiresAt instanceof Instant expiry)) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            session.removeAttribute(RESET_USERNAME);
            session.removeAttribute(RESET_EXPIRES_AT);
            return false;
        }
        return true;
    }
}
