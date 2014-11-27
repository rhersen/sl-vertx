package name.hersen.sl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafiklabAddress {
    private final Pattern pattern = Pattern.compile("/departures/(\\d+).*");

    public String getUrl(String path, String key) {
        Matcher m = pattern.matcher(path);

        if (!m.matches()) {
            throw new RuntimeException("could not parse " + path);
        }

        return "/api2/realtimedepartures.json?key=" + key + "&timeWindow=60&siteId=" + m.group(1);
    }
}
