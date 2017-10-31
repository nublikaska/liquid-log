package ru.naumen.perfhouse;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import ru.naumen.sd40.log.parser.App;

@SpringBootApplication(scanBasePackages = { "ru.naumen" })
public class PerfhouseApplication extends SpringBootServletInitializer
{
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PerfhouseApplication.class);
    }


    public static void main(String[] args) throws IOException, ParseException
    {
        SpringApplication.run(PerfhouseApplication.class, args);
    }

}
