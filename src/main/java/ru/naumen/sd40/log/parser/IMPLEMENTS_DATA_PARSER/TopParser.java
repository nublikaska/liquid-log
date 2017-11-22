package ru.naumen.sd40.log.parser.IMPLEMENTS_DATA_PARSER;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.web.multipart.MultipartFile;
import ru.naumen.sd40.log.parser.IMPLEMENTS_TIME_PARSER.TopTimeParser;
import ru.naumen.sd40.log.parser.Interfaces.DataParser;
import ru.naumen.sd40.log.parser.Interfaces.TimeParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.naumen.sd40.log.parser.NumberUtils.getSafeDouble;
import static ru.naumen.sd40.log.parser.NumberUtils.roundToTwoPlaces;

public class TopParser implements DataParser {

    private TopData cpuData = new TopData();

    private MultipartFile file;

    private Map<Long, DataParser> existing;

    private Pattern cpuAndMemPattren = Pattern
            .compile("^ *\\d+ \\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ \\S+ +(\\S+) +(\\S+) +\\S+ java");

    private TopParser currentSet;
    private TimeParser timeParser;

    public TopParser(MultipartFile file, Map<Long, DataParser> existingDataSet, TimeParser timeParser) throws IllegalArgumentException {
        //Supports these masks in file name: YYYYmmdd, YYY-mm-dd i.e. 20161101, 2016-11-01
        Matcher matcher = Pattern.compile("\\d{8}|\\d{4}-\\d{2}-\\d{2}").matcher(file.getOriginalFilename());
        if (!matcher.find()) {
            throw new IllegalArgumentException();
        }
        ((TopTimeParser)timeParser).setDate(matcher.group(0).replaceAll("-", ""));
        this.file = file;
        this.existing = existingDataSet;
        this.timeParser = timeParser;
    }

    @Override
    public boolean Condition(long time, String line) throws IOException, ParseException {
        if (time != 0L) {
            long key = prepareDate(time);
            currentSet = (TopParser) existing.computeIfAbsent(key, k -> this.getNewDataParser());
            return false;
        }
        parseLine(line);
        return false;
    }

    @Override
    public void parseLine(String line) throws IOException, ParseException {
        if (currentSet != null) {
            //get la
            Matcher la = Pattern.compile(".*load average:(.*)").matcher(line);
            if (la.find()) {
                currentSet.cpuData().addLa(Double.parseDouble(la.group(1).split(",")[0].trim()));
                return;
            }

            //get cpu and mem
            Matcher cpuAndMemMatcher = cpuAndMemPattren.matcher(line);
            if (cpuAndMemMatcher.find()) {
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
        return new TopParser(file, existing, timeParser);
    }

    private long prepareDate(long parsedDate)
    {
        int min5 = 5 * 60 * 1000;
        long count = parsedDate / min5;
        return count * min5;
    }

    public TopData cpuData()
    {
        return cpuData;
    }

    public class TopData
    {
        private DescriptiveStatistics laStat = new DescriptiveStatistics();
        private DescriptiveStatistics cpuStat = new DescriptiveStatistics();
        private DescriptiveStatistics memStat = new DescriptiveStatistics();

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

        public boolean isNan()
        {
            return laStat.getN() == 0 && cpuStat.getN() == 0 && memStat.getN() == 0;
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
    }
}
