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

        String siteId = m.group(1);
        System.out.println(siteId);
        return "/api2/realtimedepartures.json?key=" + key + "&timeWindow=60&siteId=" + siteId;
    }
}
