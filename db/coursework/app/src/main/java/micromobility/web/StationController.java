package micromobility.web;

import jakarta.validation.Valid;
import micromobility.repository.StationRepository;
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
@RequestMapping("/stations")
public class StationController {

    private final StationRepository stations;
    private final CatalogService catalog;

    public StationController(StationRepository stations, CatalogService catalog) {
        this.stations = stations;
        this.catalog = catalog;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("stations", stations.loads());
        return "stations/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new StationForm());
        return "stations/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("form", StationForm.of(stations.findById(id).orElseThrow()));
        return "stations/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") StationForm form, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "stations/form";
        }
        try {
            catalog.saveStation(form.id, form.toEntity());
        } catch (RuntimeException e) {
            result.reject("error", Errors.text(e));
            return "stations/form";
        }
        flash.addFlashAttribute("ok", "Станцію збережено.");
        return "redirect:/stations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            catalog.deleteStation(id);
            flash.addFlashAttribute("ok", "Станцію видалено.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/stations";
    }
}
