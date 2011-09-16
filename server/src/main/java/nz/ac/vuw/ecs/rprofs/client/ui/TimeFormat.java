package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/09/11
 */
public class TimeFormat {

	@LocalizableResource.DefaultLocale
	interface TimeMessages extends com.google.gwt.i18n.client.Messages {
		@DefaultMessage("{0} seconds")
		@AlternateMessage({"one", "1 second"})
		SafeHtml seconds(@PluralCount int seconds);

		@DefaultMessage("{0} minutes")
		@AlternateMessage({"one", "1 minute"})
		SafeHtml minutes(@PluralCount int minutes);

		@DefaultMessage("{0} hours")
		@AlternateMessage({"one", "1 hour"})
		SafeHtml hours(@PluralCount int hours);

		@DefaultMessage("{0} days")
		@AlternateMessage({"one", "1 day"})
		SafeHtml days(@PluralCount int days);

		@DefaultMessage("{0}")
		SafeHtml singlePrecision(SafeHtml primary);

		@DefaultMessage("{0}, {1}")
		SafeHtml doublePrecision(SafeHtml primary, SafeHtml secondary);

		@DefaultMessage("{0}, {1}, and {2}")
		SafeHtml triplePrecision(SafeHtml primary, SafeHtml secondary, SafeHtml ternary);

		@DefaultMessage("error: invalid time format")
		SafeHtml error();
	}

	public static final double SECOND = 1;
	public static final double MINUTE = SECOND * 60;
	public static final double HOUR = MINUTE * 60;
	public static final double DAY = HOUR * 24;

	public static final TimeFormat SINGLE_PRECISION = new TimeFormat(1);
	public static final TimeFormat DOUBLE_PRECISION = new TimeFormat(2);
	public static final TimeFormat TRIPLE_PRECISION = new TimeFormat(3);

	private final TimeMessages messages = GWT.create(TimeMessages.class);
	private final int precision;

	private TimeFormat(int precision) {
		this.precision = precision;
	}

	public SafeHtml format(double seconds) {
		int precision = this.precision;

		double days = seconds / DAY;
		seconds = seconds % DAY;
		double hours = seconds / HOUR;
		seconds = seconds % HOUR;
		double minutes = seconds / MINUTE;

		SafeHtml primary;
		SafeHtml secondary;
		SafeHtml tertiary;

		if (days > 1) {
			primary = messages.days((int) Math.floor(days));
			secondary = messages.hours((int) Math.floor(hours));
			tertiary = messages.minutes((int) Math.ceil(minutes));
		} else if (hours > 1) {
			primary = messages.hours((int) Math.floor(hours));
			secondary = messages.minutes((int) Math.floor(minutes));
			tertiary = messages.seconds((int) Math.ceil(seconds));
		} else if (minutes > 1) {
			primary = messages.minutes((int) Math.floor(minutes));
			secondary = messages.seconds((int) Math.ceil(seconds));
			tertiary = null;
			precision = Math.min(2, precision);
		} else {
			primary = messages.seconds((int) Math.ceil(seconds));
			secondary = null;
			tertiary = null;
			precision = Math.min(1, precision);
		}

		switch (precision) {
			case 1:
				return messages.singlePrecision(primary);
			case 2:
				return messages.doublePrecision(primary, secondary);
			case 3:
				return messages.triplePrecision(primary, secondary, tertiary);
			default:
				return messages.error();
		}
	}
}
