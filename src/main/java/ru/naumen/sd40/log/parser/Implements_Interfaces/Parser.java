package ru.naumen.sd40.log.parser.Implements_Interfaces;

import ru.naumen.sd40.log.parser.ErrorParser;
import ru.naumen.sd40.log.parser.Interfaces.DataParser;
import ru.naumen.sd40.log.parser.Interfaces.TimeParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Parser implements DataParser, TimeParser {
    protected final Pattern time_pattern;
    protected final Pattern regex;
    protected Matcher matcher;
    protected final SimpleDateFormat DATE_FORMAT;
    protected final String timeZone;

    public Parser(String timeZone, String time_pattern, String regex, String DATE_FORMAT) {
        this.time_pattern = Pattern.compile(time_pattern);
        this.regex = Pattern.compile(regex);
        this.DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT, new Locale("ru", "RU"));
        this.DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(timeZone));
        this.timeZone = timeZone;
    }

    @Override
    public abstract void parseLine(String line) throws ParseException;

    @Override
    public long parsTime(String line) throws ParseException {
        Matcher matcher = time_pattern.matcher(line);
        if (matcher.find())
        {
            Date parse = DATE_FORMAT.parse(matcher.group(1));
            return parse.getTime();
        }
        return 0L;
    }

    @Override
    public abstract Parser getDataParser();

    @Override
    public abstract boolean isNan();

    public abstract Parser getNewDataParser();

    public void Parsing(BufferedReader br, HashMap<Long, Parser> data) throws IOException, ParseException {
        String line;
        while ((line = br.readLine()) != null)
        {
            long time = this.parsTime(line);

            if (time == 0)
            {
                continue;
            }

            int min5 = 5 * 60 * 1000;
            long count = time / min5;
            long key = count * min5;

            data.computeIfAbsent(key, k -> this.getNewDataParser()).parseLine(line);
        }
    }
}
