package com.zimono.trg.shared;

public record TripMessage(long tripId, long carId, long driverId) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long tripId;
        private long carId;
        private long driverId;

        public Builder tripId(long tripId) {
            this.tripId = tripId;
            return this;
        }
        public Builder carId(long carId) {
            this.carId = carId;
            return this;
        }
        public Builder driverId(long driverId) {
            this.driverId = driverId;
            return this;
        }

        public TripMessage build() {
            return new TripMessage(tripId, carId, driverId);
        }
    }
}
