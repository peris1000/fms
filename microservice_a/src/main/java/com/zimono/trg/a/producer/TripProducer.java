package com.zimono.trg.a.producer;

import com.zimono.trg.shared.TripMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class TripProducer {

    @Inject
    @Channel("trip-starts")
    Emitter<TripMessage> tripStartsEmitter;
    @Inject
    @Channel("trip-stops")
    Emitter<TripMessage> tripStopsEmitter;


    public void startTrip(TripMessage tripMessage) {
        tripStartsEmitter.send(tripMessage);
    }

    public void stopTrip(TripMessage tripMessage) {
        tripStopsEmitter.send(tripMessage);
    }
}
