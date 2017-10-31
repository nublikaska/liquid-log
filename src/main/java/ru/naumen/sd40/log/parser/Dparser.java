package ru.naumen.sd40.log.parser;

public class Dparser {
    private String NameInfluxDB;
    private String ParseMode;
    private String Path;

    public String getNameInfluxDB() {
        return NameInfluxDB;
    }

    public void setNameInfluxDB(String nameInfluxDB) {
        NameInfluxDB = nameInfluxDB;
    }

    public String getParseMode() {
        return ParseMode;
    }

    public void setParseMode(String parseMode) {
        ParseMode = parseMode;
    }

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }
}
