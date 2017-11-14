package ru.naumen.sd40.log.parser.Interfaces;

import java.io.IOException;
import java.text.ParseException;

public interface DataParser {

    public void parseLine(String line) throws ParseException, IOException;
    public DataParser getDataParser();
    public boolean isNan();
}
