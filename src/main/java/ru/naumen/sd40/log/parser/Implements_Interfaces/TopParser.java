package ru.naumen.sd40.log.parser.Implements_Interfaces;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import ru.naumen.sd40.log.parser.DataSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.naumen.sd40.log.parser.NumberUtils.getSafeDouble;
import static ru.naumen.sd40.log.parser.NumberUtils.roundToTwoPlaces;

public class TopParser extends Parser {

    private String dataDate;

    private Pattern cpuAndMemPattren = Pattern
            .compile("^ *\\d+ \\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ \\S+ +(\\S+) +(\\S+) +\\S+ java");

    private DescriptiveStatistics laStat = new DescriptiveStatistics();
    private DescriptiveStatistics cpuStat = new DescriptiveStatistics();
    private DescriptiveStatistics memStat = new DescriptiveStatistics();

    public TopParser(String timeZone) {
        super(
                timeZone,
                "\\d{8}|\\d{4}-\\d{2}-\\d{2}",
                "^_+ (\\S+)",
                "yyyyMMddHH:mm");
    }

    @Override
    public void parseLine throws IOException, ParseException
    {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream())))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                parseLine(line);
            }
        }
    }

    public void parseLine2(String line) throws ParseException {
        //check time
        long time = 0;
        Matcher matcher = regex.matcher(line);
        if (matcher.find())
        {
            time = prepareDate(DATE_FORMAT.parse(dataDate + matcher.group(1)).getTime());
            currentSet = existing.computeIfAbsent(time, k -> new DataSet());
            return;
        }
        if (currentSet != null)
        {
            //get la
            Matcher la = Pattern.compile(".*load average:(.*)").matcher(line);
            if (la.find())
            {
                currentSet.cpuData().addLa(Double.parseDouble(la.group(1).split(",")[0].trim()));
                return;
            }

            //get cpu and mem
            Matcher cpuAndMemMatcher = cpuAndMemPattren.matcher(line);
            if (cpuAndMemMatcher.find())
            {
                currentSet.cpuData().addCpu(Double.valueOf(cpuAndMemMatcher.group(1)));
                currentSet.cpuData().addMem(Double.valueOf(cpuAndMemMatcher.group(2)));
                return;
            }
        }
    }

    @Override
    public TopParser getDataParser() {
        return this;
    }

    @Override
    public boolean isNan() {
        return true;
    }

    @Override
    public TopParser getNewDataParser() {
        return new TopParser(timeZone);
    }

    public void addLa(double la)
    {
        laStat.addValue(la);
    }

    public void addCpu(double cpu)
    {
        cpuStat.addValue(cpu);
    }

    public void addMem(double mem)
    {
        memStat.addValue(mem);
    }

    public double getAvgLa()
    {
        return roundToTwoPlaces(getSafeDouble(laStat.getMean()));
    }

    public double getAvgCpuUsage()
    {
        return roundToTwoPlaces(getSafeDouble(cpuStat.getMean()));
    }

    public double getAvgMemUsage()
    {
        return roundToTwoPlaces(getSafeDouble(memStat.getMean()));
    }

    public double getMaxLa()
    {
        return roundToTwoPlaces(getSafeDouble(laStat.getMax()));
    }

    public double getMaxCpu()
    {
        return roundToTwoPlaces(getSafeDouble(cpuStat.getMax()));
    }

    public double getMaxMem()
    {
        return roundToTwoPlaces(getSafeDouble(memStat.getMax()));
    }

    private long prepareDate(long parsedDate)
    {
        int min5 = 5 * 60 * 1000;
        long count = parsedDate / min5;
        return count * min5;
    }
}
