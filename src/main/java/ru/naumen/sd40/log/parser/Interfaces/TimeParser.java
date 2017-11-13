package ru.naumen.sd40.log.parser.Interfaces;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public interface TimeParser {
    public long parsTime(String zoneId) throws ParseException;
}
