package ua.duikt.lavrishko.rental.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.duikt.lavrishko.rental.dto.RegistrationForm;
import ua.duikt.lavrishko.rental.repo.UserRepository;

@Controller
public class RegistrationController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public RegistrationController(UserRepository users) {
        this.users = users;
    }

    @GetMapping({"/", "/register"})
    public String form(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String submit(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                         BindingResult errors, HttpServletResponse response, RedirectAttributes redirect) {
        // Унікальність перевіряємо лише тоді, коли формат полів уже коректний
        if (!errors.hasFieldErrors("login") && users.existsByLogin(form.getLogin())) {
            errors.rejectValue("login", "duplicate", "Такий логін уже зайнятий");
        }
        if (!errors.hasFieldErrors("email") && users.existsByEmail(form.getEmail())) {
            errors.rejectValue("email", "duplicate", "Користувач із такою поштою вже існує");
        }
        if (errors.hasErrors()) {
            return invalid(response);
        }
        try {
            users.insert(form, encoder.encode(form.getPassword()));
        } catch (DuplicateKeyException race) {
            // дві одночасні реєстрації з однаковим логіном: остаточне слово за унікальним індексом БД
            errors.rejectValue("login", "duplicate", "Такий логін або пошта вже зайняті");
            return invalid(response);
        }
        redirect.addFlashAttribute("login", form.getLogin());
        return "redirect:/register/success";
    }

    @GetMapping("/register/success")
    public String success() {
        return "success";
    }

    /** Повторний показ форми з помилками: статус 400, щоб клієнти без браузера бачили відмову. */
    private String invalid(HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return "register";
    }
}
