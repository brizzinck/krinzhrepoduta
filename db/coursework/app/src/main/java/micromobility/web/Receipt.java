package micromobility.web;

import micromobility.domain.Ride;

/** What the receipt page shows; chargeKop and refundKop are null while there is no such payment. */
public record Receipt(Ride ride, Integer minutes, Integer chargeKop, Integer refundKop) {

    /** The most that can still be refunded: the whole charge, once. */
    public Integer refundableKop() {
        return refundKop == null ? chargeKop : null;
    }
}
