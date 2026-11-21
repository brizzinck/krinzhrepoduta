package micromobility.web;

import java.time.LocalDate;
import java.util.List;
import micromobility.domain.Operator;
import micromobility.domain.Station;
import micromobility.domain.VehicleType;
import micromobility.repository.OperatorRepository;
import micromobility.repository.StationRepository;
import micromobility.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Reports R1, R3-R7 (R2 is the rides list). Empty filter = no filter; R6 and R7 thresholds default to 100 UAH and 20 %. */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reports;
    private final OperatorRepository operators;
    private final StationRepository stations;

    public ReportController(ReportService reports, OperatorRepository operators, StationRepository stations) {
        this.reports = reports;
        this.operators = operators;
        this.stations = stations;
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

    @GetMapping
    public String index() {
        return "reports/index";
    }

    @GetMapping("/r1")
    public String r1(@RequestParam(required = false) Integer opId, @RequestParam(required = false) VehicleType type,
                     @RequestParam(required = false) Integer stationId,
                     @RequestParam(required = false) Integer minBattery, Model model) {
        model.addAttribute("rows", reports.r1(opId, type, stationId, minBattery));
        model.addAttribute("opId", opId);
        model.addAttribute("type", type);
        model.addAttribute("stationId", stationId);
        model.addAttribute("minBattery", minBattery);
        return "reports/r1";
    }

    @GetMapping("/r3")
    public String r3(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                     Model model) {
        model.addAttribute("rows", reports.r3(from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "reports/r3";
    }

    @GetMapping("/r4")
    public String r4(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                     @RequestParam(required = false) Integer minRides, Model model) {
        model.addAttribute("rows", reports.r4(from, to, minRides));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("minRides", minRides);
        return "reports/r4";
    }

    @GetMapping("/r5")
    public String r5(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                     Model model) {
        model.addAttribute("rows", reports.r5(from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "reports/r5";
    }

    @GetMapping("/r6")
    public String r6(@RequestParam(required = false) Integer threshold, Model model) {
        int value = threshold == null ? 100 : threshold;
        model.addAttribute("rows", reports.r6(value));
        model.addAttribute("threshold", value);
        return "reports/r6";
    }

    @GetMapping("/r7")
    public String r7(@RequestParam(required = false) Integer level, Model model) {
        int value = level == null ? 20 : level;
        model.addAttribute("rows", reports.r7(value));
        model.addAttribute("level", value);
        return "reports/r7";
    }
}
