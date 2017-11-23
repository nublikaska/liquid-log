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
    default boolean Condition(long time, String line) throws IOException, ParseException {
        if (time == 0) {
            return false;
        }
        return true;
    };
}
