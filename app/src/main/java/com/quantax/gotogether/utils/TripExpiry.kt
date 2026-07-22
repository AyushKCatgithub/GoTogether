package com.quantax.gotogether.utils

import com.quantax.gotogether.domain.model.Trip
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Trips store their dates as human-readable strings in the "dd MMM yyyy" format
 * (see CreateTripViewModel). These helpers centralize parsing/expiry logic so the
 * feed, "My Trips" and cleanup code all agree on what "expired" means.
 */
private val TRIP_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

/** Safely parses a "dd MMM yyyy" trip date, returning null if blank or malformed. */
fun parseTripDate(value: String): LocalDate? =
    value.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalDate.parse(it, TRIP_DATE_FORMATTER) }.getOrNull()
    }

/**
 * A trip is expired once its end date is strictly before today. Falls back to the
 * start date when no end date is present. If neither can be parsed we conservatively
 * treat the trip as NOT expired so we never delete data we can't reason about.
 */
fun Trip.isExpired(today: LocalDate = LocalDate.now()): Boolean {
    val relevantDate = parseTripDate(endDate) ?: parseTripDate(startDate) ?: return false
    return relevantDate.isBefore(today)
}
