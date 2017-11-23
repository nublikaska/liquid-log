package ru.naumen.sd40.log.parser.IMPLEMENTS_TIME_PARSER;

import ru.naumen.sd40.log.parser.Interfaces.TimeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class GcTimeParser implements TimeParser {
    private final Pattern time_pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4}).*");
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", new Locale("ru", "RU"));

    public GcTimeParser(String timeZone) {
        this.DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    @Override
    public Pattern getTimePattern() {
        return time_pattern;
    }

    @Override
    public SimpleDateFormat getDateFormat() {
        return DATE_FORMAT;
    }
}
