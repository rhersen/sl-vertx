package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;
import static java.lang.Math.*;
import static java.util.stream.Collectors.toList;

public class NearestImpl {

    private final List<StopPoint> lines;

    public NearestImpl(Stream<String> lines) {
        this.lines = lines.map(line -> new StopPoint(line.split(";"))).collect(toList());
    }

    public JsonArray get(double... φλ) {
        Distance comparator = new Distance(φλ);
        return new JsonArray(lines
                .stream()
                .sorted(comparator)
                .limit(2)
                .map((Function<StopPoint, Map<String, Object>>) stopPoint -> new LinkedHashMap<String, Object>() {{
                    put("name", stopPoint.name);
                    put("distance", round(comparator.get(stopPoint)));
                }})
                .collect(toList()));
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
        return get(o1) < get(o2) ? -1 : 1;
    }

    double get(StopPoint that) {
        double λ1 = toRadians(λ);
        double φ1 = toRadians(φ);
        double λ2 = toRadians(that.λ);
        double φ2 = toRadians(that.φ);

        return 6378160.446 * acos(sin(λ1) * sin(λ2) + cos(λ1) * cos(λ2) * cos(φ1 - φ2));
    }
}