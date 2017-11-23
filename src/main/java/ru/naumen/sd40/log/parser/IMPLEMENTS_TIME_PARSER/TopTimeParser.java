package ru.naumen.sd40.log.parser.IMPLEMENTS_TIME_PARSER;

import org.springframework.web.multipart.MultipartFile;
import ru.naumen.sd40.log.parser.Interfaces.TimeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopTimeParser implements TimeParser {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm");
    private Pattern timeRegex = Pattern.compile("^_+ (\\S+)");
    private String date;

    public TopTimeParser(String timezone) {
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
    }

    @Override
    public Pattern getTimePattern() {
        return timeRegex;
    }

    @Override
    public SimpleDateFormat getDateFormat() {
        return sdf;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public long parsTime(String line) throws ParseException {
        Matcher matcher = timeRegex.matcher(line);
        if (matcher.find()) {
            return sdf.parse(date + matcher.group(1)).getTime();
        }
        return 0L;
    }
}
