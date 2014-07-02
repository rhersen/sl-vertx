package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;

public class NearestImpl {

    private final List<StopPoint> lines;

    public NearestImpl(Stream<String> lines) {
        this.lines = lines.map(line -> new StopPoint(line.split(";"))).collect(Collectors.toList());
    }

    public JsonArray get(double... s) {
        Optional<StopPoint> min = lines.stream().min(new Distance(s[0]));
        return new JsonArray(asList(min.get().name));
    }

}

class StopPoint {
    public final double φ;
    public final String name;

    public StopPoint(String[] split) {
        name = split[1];
        φ = parseDouble(split[3]);
    }
}

class Distance implements Comparator<StopPoint> {
    private double v;

    public Distance(double v) {
        this.v = v;
    }

    public int compare(StopPoint o1, StopPoint o2) {
        return Math.abs(v - o1.φ) < Math.abs(v - o2.φ) ? -1 : 1;
    }
}