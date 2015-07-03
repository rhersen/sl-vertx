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

    public static String key(String timeTabledDateTime, Integer journeyDirection, String destination) {
        Matcher m = date.matcher(timeTabledDateTime);
        return m.matches() ? m.group(1) + m.group(2) + journeyDirection + destination : timeTabledDateTime;
    }

    public static String key(int site, String time, int dir, String dest) {
        if (range(9525, 9529).noneMatch(i -> i == site)) {
            return key(time, dir, dest);
        }

        try {
            return "S" + hhmm.format(parse(time).plusMinutes(dir == 1 ? (9532 - site) * -3 : (9531 - site) * 3)) + dir + dest;
        } catch (DateTimeParseException e) {
            return time;
        }
    }

}
