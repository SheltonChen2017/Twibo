package edu.miis.Controllers;

import edu.miis.DataTransferPojo.RegistrationForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class GeneralController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "catopia";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", new RegistrationForm());
        }
        return "signup";
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        return principal == null ? "login" : "redirect:/mainBlog";
    }
}
