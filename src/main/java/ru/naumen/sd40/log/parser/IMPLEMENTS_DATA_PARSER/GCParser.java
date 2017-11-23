package ru.naumen.sd40.log.parser.IMPLEMENTS_DATA_PARSER;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import ru.naumen.sd40.log.parser.Interfaces.DataParser;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.naumen.sd40.log.parser.NumberUtils.getSafeDouble;
import static ru.naumen.sd40.log.parser.NumberUtils.roundToTwoPlaces;

public class GCParser implements DataParser{

    private final Pattern regex = Pattern.compile(".*real=(.*)secs.*");
    private Matcher matcher;

    public GCParser() {
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
        return new GCParser();
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
