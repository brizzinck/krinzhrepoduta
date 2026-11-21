package micromobility.web;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import micromobility.domain.BatteryCompare;
import micromobility.domain.Operator;
import micromobility.domain.Station;
import micromobility.domain.Vehicle;
import micromobility.domain.VehicleStatus;
import micromobility.domain.VehicleType;
import micromobility.repository.OperatorRepository;
import micromobility.repository.StationRepository;
import micromobility.repository.VehicleRepository;
import micromobility.service.VehicleService;
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
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleRepository vehicles;
    private final OperatorRepository operators;
    private final StationRepository stations;
    private final VehicleService service;

    public VehicleController(VehicleRepository vehicles, OperatorRepository operators, StationRepository stations,
                             VehicleService service) {
        this.vehicles = vehicles;
        this.operators = operators;
        this.stations = stations;
        this.service = service;
    }

    @ModelAttribute("operators")
    public List<Operator> operators() {
        return operators.findAllByOrderByNameAsc();
    }

    @ModelAttribute("stations")
    public List<Station> stations() {
        return stations.findAllByOrderByNameAsc();
    }

    @ModelAttribute("types")
    public VehicleType[] types() {
        return VehicleType.values();
    }

    /** The form offers every status except IN_RIDE: only a ride start sets it (BR21). */
    @ModelAttribute("formStatuses")
    public List<VehicleStatus> formStatuses() {
        return Arrays.stream(VehicleStatus.values()).filter(s -> s != VehicleStatus.IN_RIDE).toList();
    }

    @ModelAttribute("statuses")
    public VehicleStatus[] statuses() {
        return VehicleStatus.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) Integer opId, @RequestParam(required = false) VehicleType type,
                       @RequestParam(required = false) VehicleStatus status,
                       @RequestParam(required = false) Integer stationId,
                       @RequestParam(required = false) BatteryCompare batteryCmp,
                       @RequestParam(required = false) Integer battery, Model model) {
        BatteryCompare cmp = batteryCmp == null ? BatteryCompare.ANY : batteryCmp;
        model.addAttribute("vehicles", vehicles.search(opId, type, status, stationId, cmp.min(battery), cmp.max(battery)));
        model.addAttribute("opId", opId);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("stationId", stationId);
        model.addAttribute("batteryCmp", cmp);
        model.addAttribute("battery", battery == null ? 20 : battery);
        model.addAttribute("batteryCmps", BatteryCompare.values());
        return "vehicles/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new VehicleForm());
        return "vehicles/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        Vehicle v = vehicles.findById(id).orElseThrow();
        if (v.getStatus() == VehicleStatus.IN_RIDE) {
            flash.addFlashAttribute("error", "Транспорт у поїздці: змінити його можна після завершення поїздки.");
            return "redirect:/vehicles";
        }
        model.addAttribute("form", VehicleForm.of(v));
        return "vehicles/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") VehicleForm form, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "vehicles/form";
        }
        try {
            if (form.id == null) {
                service.create(form.toData());
            } else {
                service.update(form.id, form.toData());
            }
        } catch (RuntimeException e) {
            result.reject("error", Errors.text(e));
            return "vehicles/form";
        }
        flash.addFlashAttribute("ok", "Транспорт збережено.");
        return "redirect:/vehicles";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            service.delete(id);
            flash.addFlashAttribute("ok", "Транспорт видалено.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/vehicles";
    }
}
