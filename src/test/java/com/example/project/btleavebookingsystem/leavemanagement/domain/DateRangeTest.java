package com.example.project.btleavebookingsystem.leavemanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    @Test
    void validRangeIsAccepted() {
        DateRange range = new DateRange(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10));
        assertThat(range.numberOfDays()).isEqualTo(5);
    }

    @Test
    void singleDayRangeCountsAsOneDay() {
        LocalDate day = LocalDate.of(2026, 10, 6);
        DateRange range = new DateRange(day, day);
        assertThat(range.numberOfDays()).isEqualTo(1);
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        assertThatThrownBy(() ->
                new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 6)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    void nullStartDateIsRejected() {
        assertThatThrownBy(() -> new DateRange(null, LocalDate.of(2026, 10, 10)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}