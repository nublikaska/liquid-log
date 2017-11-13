package ru.naumen.sd40.log.parser.Interfaces;

import ru.naumen.sd40.log.parser.ErrorParser;

import java.text.ParseException;
import java.util.regex.Pattern;

import static ru.naumen.sd40.log.parser.NumberUtils.getSafeDouble;
import static ru.naumen.sd40.log.parser.NumberUtils.roundToTwoPlaces;

public interface DataParser {

    public void parseLine(String line) throws ParseException;
    public DataParser getDataParser();
    public boolean isNan();
}
