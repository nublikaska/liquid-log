package ru.naumen.sd40.log.parser.IMPLEMENTS_TIME_PARSER;

import ru.naumen.sd40.log.parser.Interfaces.TimeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class TopTimeParser implements TimeParser {
    @Override
    public Pattern getTimePattern() {
        return null;
    }

    @Override
    public SimpleDateFormat getDateFormat() {
        return null;
    }

    @Override
    public long parsTime(String line) throws ParseException {
        return 0;
    }
}
