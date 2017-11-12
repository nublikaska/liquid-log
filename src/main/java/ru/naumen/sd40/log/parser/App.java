package ru.naumen.sd40.log.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;

import org.influxdb.dto.BatchPoints;

import org.springframework.web.multipart.MultipartFile;
import ru.naumen.perfhouse.influx.InfluxDAO;
import ru.naumen.sd40.log.parser.GCParser.GCTimeParser;

/**
 * Created by doki on 22.10.16.
 */
public class App
{
    /**
     * 
     * @param NameInfluxDB - database name
     * @param file - log
     * @param ParseMode - mod
     * @param timeZone - timeZone
     * @param ResultLog
     * @throws IOException
     * @throws ParseException
     */
    public static void main(String NameInfluxDB, MultipartFile file, String ParseMode, String timeZone, boolean ResultLog) throws IOException, ParseException
    {
        String influxDb = NameInfluxDB;
        influxDb = influxDb.replaceAll("-", "_");

        InfluxDAO storage = null;
        if (influxDb != null)
        {
            storage = new InfluxDAO(System.getProperty("influx.host"), System.getProperty("influx.user"),
                    System.getProperty("influx.password"));
            storage.init();
            storage.connectToDB(influxDb);
        }
        InfluxDAO finalStorage = storage;
        String finalInfluxDb = influxDb;
        BatchPoints points = null;

        if (storage != null)
        {
            points = storage.startBatchPoints(influxDb);
        }

        HashMap<Long, DataSet> data = new HashMap<>();

        TimeParser timeParser = new TimeParser(timeZone);
        GCTimeParser gcTime = new GCTimeParser(timeZone);

        switch (ParseMode)
        {
        case "sdng":
            //Parse sdng
            try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()), 32 * 1024 * 1024))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    long time = timeParser.parseLine(line);

                    if (time == 0)
                    {
                        continue;
                    }

                    int min5 = 5 * 60 * 1000;
                    long count = time / min5;
                    long key = count * min5;

                    data.computeIfAbsent(key, k -> new DataSet()).parseLine(line);
                }
            }
            break;
        case "gc":
            //Parse gc log
            try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream())))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    long time = gcTime.parseTime(line);

                    if (time == 0)
                    {
                        continue;
                    }

                    int min5 = 5 * 60 * 1000;
                    long count = time / min5;
                    long key = count * min5;
                    data.computeIfAbsent(key, k -> new DataSet()).parseGcLine(line);
                }
            }
            break;
        case "top":
            TopParser topParser = new TopParser(file, data);
            topParser.configureTimeZone(timeZone);
            //Parse top
            topParser.parse();
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown parse mode! Availiable modes: sdng, gc, top. Requested mode: " + ParseMode);
        }

        if (ResultLog)
        {
            System.out.print("Timestamp;Actions;Min;Mean;Stddev;50%%;95%%;99%%;99.9%%;Max;Errors\n");
        }
        BatchPoints finalPoints = points;
        data.forEach((k, set) ->
        {
            ActionDoneParser dones = set.getActionsDone();
            dones.calculate();
            ErrorParser erros = set.getErrors();
            if (ResultLog)
            {
                System.out.print(String.format("%d;%d;%f;%f;%f;%f;%f;%f;%f;%f;%d\n", k, dones.getCount(),
                        dones.getMin(), dones.getMean(), dones.getStddev(), dones.getPercent50(), dones.getPercent95(),
                        dones.getPercent99(), dones.getPercent999(), dones.getMax(), erros.getErrorCount()));
            }
            if (!dones.isNan())
            {
                finalStorage.storeActionsFromLog(finalPoints, finalInfluxDb, k, dones, erros);
            }

            GCParser gc = set.getGc();
            if (!gc.isNan())
            {
                finalStorage.storeGc(finalPoints, finalInfluxDb, k, gc);
            }

            TopData cpuData = set.cpuData();
            if (!cpuData.isNan())
            {
                finalStorage.storeTop(finalPoints, finalInfluxDb, k, cpuData);
            }
        });
        storage.writeBatch(points);
    }
}
