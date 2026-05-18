package ru.itmo.ekairaliev.storage;

import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.Seal;

import java.util.List;

public final class AppState {
    private final List<Sample> samples;
    private final List<Seal> seals;
    private final List<CustodyEvent> custodyEvents;

    public AppState(List<Sample> samples, List<Seal> seals, List<CustodyEvent> custodyEvents) {
        this.samples = List.copyOf(samples);
        this.seals = List.copyOf(seals);
        this.custodyEvents = List.copyOf(custodyEvents);
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public List<Seal> getSeals() {
        return seals;
    }

    public List<CustodyEvent> getCustodyEvents() {
        return custodyEvents;
    }
}
