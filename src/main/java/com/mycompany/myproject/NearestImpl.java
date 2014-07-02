package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;
import static java.lang.Math.*;
import static java.util.Arrays.asList;

public class NearestImpl {

    private final List<StopPoint> lines;

    public NearestImpl(Stream<String> lines) {
        this.lines = lines.map(line -> new StopPoint(line.split(";"))).collect(Collectors.toList());
    }

    public JsonArray get(double... φλ) {
        return new JsonArray(asList(lines.stream().min(new Distance(φλ)).get().name));
    }

}

class StopPoint {
    public final double φ;
    public final double λ;
    public final String name;

    public StopPoint(String[] split) {
        name = split[1];
        φ = parseDouble(split[3]);
        λ = parseDouble(split[4]);
    }
}

class Distance implements Comparator<StopPoint> {
    private double φ;
    private double λ;

    public Distance(double[] φλ) {
        φ = φλ[0];
        λ = φλ[1];
    }

    public int compare(StopPoint o1, StopPoint o2) {
        return distance(o1) < distance(o2) ? -1 : 1;
    }

    private double distance(StopPoint that) {
        double λ1 = toRadians(λ);
        double φ1 = toRadians(φ);
        double λ2 = toRadians(that.λ);
        double φ2 = toRadians(that.φ);

        return toDegrees(acos(sin(λ1) * sin(λ2) + cos(λ1) * cos(λ2) * cos(φ1 - φ2)));
    }
}