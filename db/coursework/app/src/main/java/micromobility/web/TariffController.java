package micromobility.web;

import jakarta.validation.Valid;
import java.util.List;
import micromobility.domain.Operator;
import micromobility.domain.VehicleType;
import micromobility.repository.OperatorRepository;
import micromobility.repository.TariffRepository;
import micromobility.service.CatalogService;
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
@RequestMapping("/tariffs")
public class TariffController {

    private final TariffRepository tariffs;
    private final OperatorRepository operators;
    private final CatalogService catalog;

    public TariffController(TariffRepository tariffs, OperatorRepository operators, CatalogService catalog) {
        this.tariffs = tariffs;
        this.operators = operators;
        this.catalog = catalog;
    }

    @ModelAttribute("operators")
    public List<Operator> operators() {
        return operators.findAllByOrderByNameAsc();
    }

    @ModelAttribute("types")
    public VehicleType[] types() {
        return VehicleType.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) Integer opId, @RequestParam(required = false) VehicleType type,
                       Model model) {
        model.addAttribute("tariffs", tariffs.search(opId, type));
        model.addAttribute("opId", opId);
        model.addAttribute("type", type);
        return "tariffs/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new TariffForm());
        return "tariffs/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("form", TariffForm.of(tariffs.findById(id).orElseThrow()));
        return "tariffs/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") TariffForm form, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "tariffs/form";
        }
        try {
            catalog.saveTariff(form.id, form.operatorId, form.vehicleType, form.unlockFeeKop(), form.perMinuteKop());
        } catch (RuntimeException e) {
            result.reject("error", Errors.text(e));
            return "tariffs/form";
        }
        flash.addFlashAttribute("ok", "Тариф збережено.");
        return "redirect:/tariffs";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            catalog.deleteTariff(id);
            flash.addFlashAttribute("ok", "Тариф видалено.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/tariffs";
    }
}
