package micromobility.web;

import java.time.LocalDate;
import java.util.List;
import micromobility.domain.Operator;
import micromobility.domain.PaymentKind;
import micromobility.domain.Ride;
import micromobility.domain.VehicleType;
import micromobility.repository.CustomerRepository;
import micromobility.repository.OperatorRepository;
import micromobility.repository.PaymentRepository;
import micromobility.repository.RideRepository;
import micromobility.repository.StationRepository;
import micromobility.service.Billing;
import micromobility.service.BusinessException;
import micromobility.service.PaymentService;
import micromobility.service.ReportService;
import micromobility.service.RideService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Rides: the list is report R2 (ride search); start, receipt, finish, cancel, refund. */
@Controller
@RequestMapping("/rides")
public class RideController {

    private final RideRepository rides;
    private final CustomerRepository customers;
    private final StationRepository stations;
    private final PaymentRepository payments;
    private final OperatorRepository operators;
    private final RideService rideService;
    private final PaymentService paymentService;
    private final ReportService reports;

    public RideController(RideRepository rides, CustomerRepository customers, StationRepository stations,
                          PaymentRepository payments, OperatorRepository operators, RideService rideService,
                          PaymentService paymentService, ReportService reports) {
        this.rides = rides;
        this.customers = customers;
        this.stations = stations;
        this.payments = payments;
        this.operators = operators;
        this.rideService = rideService;
        this.paymentService = paymentService;
        this.reports = reports;
    }

    @ModelAttribute("operators")
    public List<Operator> operators() {
        return operators.findAllByOrderByNameAsc();
    }

    @ModelAttribute("types")
    public VehicleType[] types() {
        return VehicleType.values();
    }

    /** R2: ride search; every filter is optional. status: active, finished or empty. */
    @GetMapping
    public String list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) Integer opId, @RequestParam(required = false) VehicleType type,
                       @RequestParam(required = false) String q, @RequestParam(required = false) String status,
                       Model model) {
        Boolean active = "active".equals(status) ? Boolean.TRUE : "finished".equals(status) ? Boolean.FALSE : null;
        model.addAttribute("rows", reports.r2(from, to, opId, type, q, active));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("opId", opId);
        model.addAttribute("type", type);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "rides/list";
    }

    @GetMapping("/start")
    public String startForm(Model model) {
        model.addAttribute("customers", customers.findByDiiaVerifiedAtIsNotNullOrderByLastNameAscIdAsc());
        model.addAttribute("available", reports.startableVehicles());
        return "rides/start";
    }

    @PostMapping("/start")
    public String start(@RequestParam Integer customerId, @RequestParam Integer vehicleId, RedirectAttributes flash) {
        try {
            Ride ride = rideService.start(customerId, vehicleId);
            flash.addFlashAttribute("ok", "Поїздку розпочато.");
            return "redirect:/rides/" + ride.getId();
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
            return "redirect:/rides/start";
        }
    }

    @GetMapping("/{id}")
    public String receipt(@PathVariable Integer id, Model model) {
        Ride ride = rides.findForReceipt(id).orElseThrow();
        Integer minutes = ride.isActive() ? null : Billing.minutes(ride.getStartedAt(), ride.getEndedAt());
        Integer charge = payments.findByRideIdAndKind(id, PaymentKind.RIDE).map(p -> -p.getAmountKop()).orElse(null);
        Integer refund = payments.findByRideIdAndKind(id, PaymentKind.REFUND).map(p -> p.getAmountKop()).orElse(null);
        model.addAttribute("receipt", new Receipt(ride, minutes, charge, refund));
        model.addAttribute("stations", stations.loads());
        return "rides/receipt";
    }

    @PostMapping("/{id}/finish")
    public String finish(@PathVariable Integer id, @RequestParam(required = false) Integer stationId,
                         RedirectAttributes flash) {
        try {
            rideService.finish(id, stationId);
            flash.addFlashAttribute("ok", "Поїздку завершено, оплату списано.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
        }
        return "redirect:/rides/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Integer id, @RequestParam(required = false) Integer stationId,
                         RedirectAttributes flash) {
        try {
            rideService.cancel(id, stationId);
            flash.addFlashAttribute("ok", "Поїздку скасовано, оплату не списано.");
            return "redirect:/rides";
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", Errors.text(e));
            return "redirect:/rides/" + id;
        }
    }

    @PostMapping("/{id}/refund")
    public String refund(@PathVariable Integer id, @RequestParam java.math.BigDecimal amount, RedirectAttributes flash) {
        try {
            if (amount.scale() > 2) {
                throw new BusinessException("Сума: не більше двох знаків після коми.");
            }
            paymentService.refund(id, amount.movePointRight(2).intValueExact());
            flash.addFlashAttribute("ok", "Повернення оформлено.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e instanceof ArithmeticException ? "Невірна сума." : Errors.text(e));
        }
        return "redirect:/rides/" + id;
    }
}
