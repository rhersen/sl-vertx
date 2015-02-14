package name.hersen.sl;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.LocalDateTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.IntStream.range;

public class Keys {
    private static final Pattern date = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\dT(\\d\\d):(\\d\\d):\\d\\d");
    private static final DateTimeFormatter hhmm = ofPattern("HHmm");

    public static String key(String timeTabledDateTime, Integer journeyDirection) {
        Matcher m = date.matcher(timeTabledDateTime);
        return m.matches() ? m.group(1) + m.group(2) + journeyDirection : timeTabledDateTime;
    }

    public static String key(int siteId, String timeTabledDateTime, int journeyDirection) {
        if (range(9525, 9529).noneMatch(i -> i == siteId) || journeyDirection != 2) {
            return key(timeTabledDateTime, journeyDirection);
        }

        try {
            return "S" + hhmm.format(parse(timeTabledDateTime).plusMinutes(offset(siteId))) + journeyDirection;
        } catch (DateTimeParseException e) {
            return timeTabledDateTime;
        }
    }

    private static int offset(int siteId) {
        if (siteId == 9528) return 9;
        if (siteId == 9527) return 12;
        if (siteId == 9526) return 15;
        return 18;
    }
}
