package ru.naumen.sd40.log.parser.Interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

public interface DataParser {

    void parseLine(String line) throws ParseException, IOException;
    DataParser getDataParser();
    boolean isNan();
    DataParser getNewDataParser();

    default void Parsing(BufferedReader br, HashMap<Long, DataParser> data, TimeParser timeParser) throws IOException, ParseException {
        String line;
        while ((line = br.readLine()) != null) {
            long time = timeParser.parsTime(line);

            if (time == 0) {
                continue;
            }

            int min5 = 5 * 60 * 1000;
            long count = time / min5;
            long key = count * min5;

            data.computeIfAbsent(key, k -> this.getNewDataParser()).parseLine(line);
        }
    }
}
