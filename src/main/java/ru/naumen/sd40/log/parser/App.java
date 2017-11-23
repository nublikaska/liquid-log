package ru.naumen.sd40.log.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;

import org.influxdb.dto.BatchPoints;

import org.springframework.web.multipart.MultipartFile;
import ru.naumen.perfhouse.influx.InfluxDAO;
import ru.naumen.sd40.log.parser.IMPLEMENTS_DATA_PARSER.SdngParser;
import ru.naumen.sd40.log.parser.IMPLEMENTS_DATA_PARSER.TopParser;
import ru.naumen.sd40.log.parser.IMPLEMENTS_TIME_PARSER.GcTimeParser;
import ru.naumen.sd40.log.parser.IMPLEMENTS_TIME_PARSER.SdngTimeParser;
import ru.naumen.sd40.log.parser.IMPLEMENTS_TIME_PARSER.TopTimeParser;
import ru.naumen.sd40.log.parser.Interfaces.DataParser;
import ru.naumen.sd40.log.parser.IMPLEMENTS_DATA_PARSER.GCParser;
import ru.naumen.sd40.log.parser.Interfaces.TimeParser;

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

        HashMap<Long, DataParser> data = new HashMap<>();

        DataParser dataParser;
        TimeParser timeParser;
        int sz = 32 * 1024 * 1024;;

        switch (ParseMode)
        {
        case "sdng":
            //Parse sdng
            timeParser = new SdngTimeParser(timeZone);
            dataParser = new SdngParser();
            break;
        case "gc":
            //Parse gc log
            timeParser = new GcTimeParser(timeZone);
            dataParser = new GCParser();
            break;
        case "top":
            timeParser = new TopTimeParser(timeZone);
            dataParser = new TopParser(file, data, timeParser);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown parse mode! Availiable modes: sdng, gc, top. Requested mode: " + ParseMode);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()), sz)) {
            String line;
            while ((line = br.readLine()) != null) {
                long time = timeParser.parsTime(line);

                if (dataParser.Condition(time, line)) {
                    int min5 = 5 * 60 * 1000;
                    long count = time / min5;
                    long key = count * min5;

                    data.computeIfAbsent(key, k -> dataParser.getNewDataParser()).parseLine(line);
                }
            }
        }

        if (ResultLog)
        {
            System.out.print("Timestamp;Actions;Min;Mean;Stddev;50%%;95%%;99%%;99.9%%;Max;Errors\n");
        }

        BatchPoints finalPoints = points;
        data.forEach((k, set) ->
        {
            if(set instanceof SdngParser) {
                SdngParser dones = ((SdngParser)set).getDataParser();
                dones.calculate();
                SdngParser.ErrorParser erros = ((SdngParser)set).getErrors();
                if (ResultLog)
                {
                    System.out.print(String.format("%d;%d;%f;%f;%f;%f;%f;%f;%f;%f;%d\n", k,
                            dones.getCount(),
                            dones.getMin(),
                            dones.getMean(),
                            dones.getStddev(),
                            dones.getPercent50(),
                            dones.getPercent95(),
                            dones.getPercent99(),
                            dones.getPercent999(),
                            dones.getMax(),
                            erros.getErrorCount()));
                }
                if (!dones.isNan())
                {
                    finalStorage.storeActionsFromLog(finalPoints, finalInfluxDb, k, dones, erros);
                }
            }

            if(set instanceof GCParser) {
                ru.naumen.sd40.log.parser.IMPLEMENTS_DATA_PARSER.GCParser gc = ((GCParser)set).getDataParser();
                if (!gc.isNan()) {
                    finalStorage.storeGc(finalPoints, finalInfluxDb, k, gc);
                }
            }

            if(set instanceof TopParser) {
                TopParser.TopData cpuData = ((TopParser)set).cpuData();
                if (!cpuData.isNan()) {
                    finalStorage.storeTop(finalPoints, finalInfluxDb, k, cpuData);
                }
            }
        });
        storage.writeBatch(points);
    }
}
