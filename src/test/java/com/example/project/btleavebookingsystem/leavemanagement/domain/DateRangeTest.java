package com.example.project.btleavebookingsystem.leavemanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    @Test
    void fullWorkingWeekCountsAsFiveDays() {
        DateRange range = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9));
        assertThat(range.workingDays()).isEqualTo(5);
    }

    @Test
    void weekendDaysAreNotCharged() {
        DateRange range = new DateRange(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 10));
        assertThat(range.workingDays()).isEqualTo(4);
    }

    @Test
    void rangeSpanningAWeekendExcludesBothWeekendDays() {
        DateRange range = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 16));
        assertThat(range.workingDays()).isEqualTo(10);
    }

    @Test
    void singleWeekdayCountsAsOneDay() {
        LocalDate monday = LocalDate.of(2026, 10, 5);
        DateRange range = new DateRange(monday, monday);
        assertThat(range.workingDays()).isEqualTo(1);
    }

    @Test
    void weekendOnlyRangeIsRejected() {
        assertThatThrownBy(() ->
                new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one working day");
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        assertThatThrownBy(() ->
                new DateRange(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    void nullStartDateIsRejected() {
        assertThatThrownBy(() -> new DateRange(null, LocalDate.of(2026, 10, 9)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullEndDateIsRejected() {
        assertThatThrownBy(() -> new DateRange(LocalDate.of(2026, 10, 5), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void overlappingRangesAreDetected() {
        DateRange a = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9));
        DateRange b = new DateRange(LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 14));
        assertThat(a.overlaps(b)).isTrue();
        assertThat(b.overlaps(a)).isTrue();
    }

    @Test
    void rangesSharingABoundaryDayOverlap() {
        DateRange a = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9));
        DateRange b = new DateRange(LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 13));
        assertThat(a.overlaps(b)).isTrue();
    }

    @Test
    void separateRangesDoNotOverlap() {
        DateRange a = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9));
        DateRange b = new DateRange(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 16));
        assertThat(a.overlaps(b)).isFalse();
    }
}