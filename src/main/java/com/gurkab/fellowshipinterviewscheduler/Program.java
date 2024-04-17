package com.gurkab.fellowshipinterviewscheduler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Program {

    private String programName;
    private List<LocalDate> availableDates;
    private LocalDate assignedDate;
    private List<LocalDate> secondaryDates;

    public List<LocalDate> getAvailableDates() {
        return new ArrayList<>(availableDates);
    }

    public void addSecondaryDate(LocalDate date) {
        this.secondaryDates.add(date);
    }
}
