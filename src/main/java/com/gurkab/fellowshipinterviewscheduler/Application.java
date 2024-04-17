package com.gurkab.fellowshipinterviewscheduler;

import java.util.List;

public class Application  {

    public static void main(String[] args) {
        DateAssigner dateAssigner = new DateAssigner();
        List<Program> rmaddingPrograms = ProgramUtil.buildProgramsFromResource("rmadding.txt");
        dateAssigner.assignDates(rmaddingPrograms);
    }
}