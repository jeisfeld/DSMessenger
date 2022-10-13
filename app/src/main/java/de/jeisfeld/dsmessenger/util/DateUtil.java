package de.jeisfeld.dsmessenger.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

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
	private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
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
		return FORMATTER.parse(jsonDate, Instant::from);
	}

	/**
	 * Forman an instant into a JSON date.
	 *
	 * @param instant The instant.
	 * @return The JSON date.
	 */
	public static String instantToJsonDate(final Instant instant) {
		return FORMATTER.format(instant);
	}
}
