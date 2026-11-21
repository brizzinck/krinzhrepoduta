package micromobility.web;

import jakarta.validation.Valid;
import micromobility.domain.Operator;
import micromobility.repository.OperatorRepository;
import micromobility.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/operators")
public class OperatorController {

    private final OperatorRepository operators;
    private final CatalogService catalog;

    public OperatorController(OperatorRepository operators, CatalogService catalog) {
        this.operators = operators;
        this.catalog = catalog;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("operators", operators.findAllByOrderByNameAsc());
        return "operators/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new OperatorForm());
        return "operators/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        Operator o = operators.findById(id).orElseThrow();
        model.addAttribute("form", OperatorForm.of(o));
        return "operators/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") OperatorForm form, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "operators/form";
        }
        try {
            catalog.saveOperator(form.id, form.toEntity());
        } catch (RuntimeException e) {
            result.reject("error", Errors.text(e));
            return "operators/form";
        }
        flash.addFlashAttribute("ok", "Оператора збережено.");
        return "redirect:/operators";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            catalog.deleteOperator(id);
            flash.addFlashAttribute("ok", "Оператора видалено.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/operators";
    }
}
