package ru.naumen.sd40.log.parser.Implements_Interfaces;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.naumen.sd40.log.parser.NumberUtils.getSafeDouble;
import static ru.naumen.sd40.log.parser.NumberUtils.roundToTwoPlaces;

public class GCParser extends Parser {

    public GCParser(String timeZone) {
        super(
                timeZone,
                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4}).*",
                ".*real=(.*)secs.*",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    @Override
    public void parseLine(String line) {

        matcher = regex.matcher(line);

        if (matcher.find())
        {
            ds.addValue(Double.parseDouble(matcher.group(1).trim().replace(',', '.')));
        }
    }

    @Override
    public GCParser getDataParser() {
        return this;
    }

    @Override
    public boolean isNan() {
        return getGcTimes() == 0;
    }

    @Override
    public GCParser getNewDataParser() {
        return new GCParser(timeZone);
    }

    private DescriptiveStatistics ds = new DescriptiveStatistics();

    public double getCalculatedAvg()
    {
        return roundToTwoPlaces(getSafeDouble(ds.getMean()));
    }

    public long getGcTimes()
    {
        return ds.getN();
    }

    public double getMaxGcTime()
    {
        return roundToTwoPlaces(getSafeDouble(ds.getMax()));
    }
}
