package com.mycompany.myproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafiklabAddress {
    private final Pattern pattern = Pattern.compile(".*/(\\d+)");

    public String getUrl(String path, String key) {
        Matcher m = pattern.matcher(path);
        String siteId = m.matches() ? m.group(1) : "9525";
        return "/api2/realtimedepartures.json?key=" + key + "&timeWindow=60&siteId=" + siteId;
    }
}
