package micromobility.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import micromobility.config.Times;
import org.springframework.stereotype.Component;

/** Template helper: money in hryvnias, times in Kyiv time. Used as ${@fmt.uah(x)}. */
@Component("fmt")
public class Fmt {

    /** 6375 becomes "63,75". */
    public String uah(Number kop) {
        return kop == null ? "" : BigDecimal.valueOf(kop.longValue(), 2).toPlainString().replace('.', ',');
    }

    /** 6375 becomes "63.75" (for input values). */
    public String uahInput(Number kop) {
        return kop == null ? "" : BigDecimal.valueOf(kop.longValue(), 2).toPlainString();
    }

    /** An average with a decimal comma: 14.7 becomes "14,7"; null stays empty. */
    public String dec(Number value) {
        return value == null ? "" : value.toString().replace('.', ',');
    }

    public String dt(LocalDateTime utc) {
        return Times.view(utc);
    }
}
