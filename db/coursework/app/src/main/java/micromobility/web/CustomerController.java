package micromobility.web;

import jakarta.validation.Valid;
import micromobility.repository.CustomerRepository;
import micromobility.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customers;
    private final CustomerService service;

    public CustomerController(CustomerRepository customers, CustomerService service) {
        this.customers = customers;
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        String text = q == null || q.isBlank() ? null : q.trim();
        model.addAttribute("customers", customers.rows(text == null ? null : micromobility.service.Like.escape(text) + "%"));
        model.addAttribute("q", text);
        return "customers/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new CustomerForm());
        return "customers/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("form", CustomerForm.of(customers.findById(id).orElseThrow()));
        return "customers/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") CustomerForm form, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "customers/form";
        }
        try {
            if (form.id == null) {
                service.create(form.lastName, form.firstName, form.phone, form.email);
                flash.addFlashAttribute("ok", "Клієнта збережено, нараховано бонус 200 грн.");
            } else {
                service.update(form.id, form.lastName, form.firstName, form.phone, form.email);
                flash.addFlashAttribute("ok", "Клієнта збережено.");
            }
        } catch (RuntimeException e) {
            result.reject("error", Errors.text(e));
            return "customers/form";
        }
        return "redirect:/customers";
    }

    @PostMapping("/{id}/verify")
    public String verify(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            service.verifyWithDiia(id);
            flash.addFlashAttribute("ok", "Особу підтверджено через Дію (імітація).");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            service.delete(id);
            flash.addFlashAttribute("ok", "Клієнта видалено разом із бонусним платежем.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/customers";
    }
}
