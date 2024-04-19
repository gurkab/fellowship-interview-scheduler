package com.gurkab.fellowshipinterviewscheduler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProgramUtil {

    public static List<Program> buildProgramsFromResource(String resourceName) {
        List<Program> programs = new ArrayList<>();
        try (
            InputStream is = ProgramUtil.class.getClassLoader().getResourceAsStream(resourceName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            // Read all lines from the resource
            List<String> lines = reader.lines().toList();
            for (String line : lines) {
                // Split each line by the pipe symbol
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String programName = parts[0];
                    List<LocalDate> dates = parseDates(parts[1]);
                    programs.add(new Program(programName, dates, null, new ArrayList<>()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return programs;
    }

    private static List<LocalDate> parseDates(String datesString) {
        return Stream.of(datesString.split(",")).map(LocalDate::parse).toList();
    }
}
