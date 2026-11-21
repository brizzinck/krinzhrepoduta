package micromobility.web;

import java.util.List;
import micromobility.domain.Customer;
import micromobility.repository.CustomerRepository;
import micromobility.repository.PaymentRepository;
import micromobility.service.PaymentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Wallet history (newest first) and top-up. Payments are never edited or deleted (BR14). */
@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository payments;
    private final CustomerRepository customers;
    private final PaymentService service;

    public PaymentController(PaymentRepository payments, CustomerRepository customers, PaymentService service) {
        this.payments = payments;
        this.customers = customers;
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Integer customerId, Model model) {
        List<Customer> all = customers.findAll(org.springframework.data.domain.Sort.by("lastName", "firstName", "id"));
        model.addAttribute("customers", all);
        model.addAttribute("customerId", customerId);
        model.addAttribute("payments", payments.history(customerId, PageRequest.of(0, 200)));
        model.addAttribute("balanceKop", customerId == null ? null : service.balanceKop(customerId));
        model.addAttribute("topups", PaymentService.TOPUP_UAH);
        return "payments/list";
    }

    @PostMapping("/topup")
    public String topUp(@RequestParam Integer customerId, @RequestParam int amount, RedirectAttributes flash) {
        try {
            service.topUp(customerId, amount);
            flash.addFlashAttribute("ok", "Гаманець поповнено на " + amount + " грн.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/payments?customerId=" + customerId;
    }
}
