package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;
import static java.lang.Math.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class NearestImpl {

    private final List<StopPoint> stopPoints;
    private final Map<String, String> sites;

    public NearestImpl(Stream<String> stopPoints, Stream<String> sites) {
        this.stopPoints = stopPoints
                .skip(1)
                .map(line -> new StopPoint(line.split(";")))
                .collect(toList());

        BinaryOperator<String> dontOverwrite = (oldKey, newKey) -> oldKey;

        this.sites = sites
                .skip(1)
                .map(line -> line.split(";"))
                .filter(fields -> fields.length > 1)
                .collect(toMap(getArea(), getSiteId(), dontOverwrite));
    }

    private Function<String[], String> getSiteId() {
        return getField(0);
    }

    private Function<String[], String> getArea() {
        return getField(2);
    }

    private Function<String[], String> getField(int n) {
        return fields -> fields[n];
    }

    public JsonArray get(double... φλ) {
        Distance comparator = new Distance(φλ);
        List<Object> list = stopPoints
                .stream()
                .parallel()
                .filter(stopPoint -> comparator.get(stopPoint) < 0x8000)
                .sorted(comparator)
                .limit(8)
                .map((Function<StopPoint, Map<String, Object>>) stopPoint -> new LinkedHashMap<String, Object>() {{
                    put("name", stopPoint.name);
                    put("area", stopPoint.area);
                    put("site", sites.get(stopPoint.area));
                    put("distance", round(comparator.get(stopPoint)));
                }})
                .collect(toList());
        return new JsonArray(list);
    }
}

class StopPoint {
    public final double φ;
    public final double λ;
    public final String name;
    public final String area;

    public StopPoint(String[] split) {
        name = split[1];
        area = split[2];
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