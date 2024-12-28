package com.finadem.utilities;

import com.finadem.exception.exceptions.InvalidDateFormatException;
import com.finadem.exception.exceptions.StartDateAfterEndDateException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class DateUtilities {
     public LocalDate validateAndParseDate(String date, DateTimeFormatter formatter) throws InvalidDateFormatException {
        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("Invalid date format for "+date+". Expected format: dd-MM-yyyy");
        }
    }

    public void isStartDateAfterEndDate(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new StartDateAfterEndDateException("From date cannot be after To date.");
        }
    }
}
