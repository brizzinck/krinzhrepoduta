package micromobility.report;

/** A station with the number of vehicles parked there. */
public record StationLoad(Integer id, String name, String address, Integer capacity, Long parked) {

    public long free() {
        return capacity - parked;
    }
}
