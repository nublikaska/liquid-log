package ru.naumen.sd40.log.parser.Interfaces;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface TimeParser {
    Pattern getTimePattern();
    SimpleDateFormat getDateFormat();

    default long parsTime(String line) throws ParseException {
        Matcher matcher = getTimePattern().matcher(line);
        if (matcher.find())
        {
            Date parse = getDateFormat().parse(matcher.group(1));
            return parse.getTime();
        }
        return 0L;
    }
}
