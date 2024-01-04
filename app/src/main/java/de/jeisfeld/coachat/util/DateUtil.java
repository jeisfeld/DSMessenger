package de.jeisfeld.coachat.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import de.jeisfeld.coachat.Application;

/**
 * Utilities for date formatting and parsing.
 */
public final class DateUtil {
	/**
	 * Hide constructor.
	 */
	private DateUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Formatter for JSON date.
	 */
	private static final DateTimeFormatter JSON_FORMATTER = new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
			.toFormatter()
			.withZone(ZoneOffset.UTC);

	/**
	 * Parse a JSON date into an Instant.
	 *
	 * @param jsonDate The JSON date.
	 * @return The instant.
	 */
	public static Instant jsonDateToInstant(final String jsonDate) {
		return JSON_FORMATTER.parse(jsonDate, Instant::from);
	}

	/**
	 * Forman an instant into a JSON date.
	 *
	 * @param instant The instant.
	 * @return The JSON date.
	 */
	public static String instantToJsonDate(final Instant instant) {
		return JSON_FORMATTER.format(instant);
	}

	/**
	 * Format a timestamp for GUI display.
	 *
	 * @param timestamp The timestamp.
	 * @return The formatted timestamp.
	 */
	public static String formatTimestamp(final long timestamp) {
		String dateString = android.text.format.DateFormat.getDateFormat(Application.getAppContext()).format(new Date(timestamp));
		String currentDateString = android.text.format.DateFormat.getDateFormat(Application.getAppContext()).format(new Date());
		String timeString = android.text.format.DateFormat.getTimeFormat(Application.getAppContext()).format(new Date(timestamp));
		if (currentDateString.equals(dateString)) {
			return timeString;
		}
		else {
			return dateString + " " + timeString;
		}
	}

}
