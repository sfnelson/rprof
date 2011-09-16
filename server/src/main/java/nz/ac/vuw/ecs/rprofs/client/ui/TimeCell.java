package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/09/11
 */
public class TimeCell extends AbstractCell<Double> {

	private final TimeFormat format;

	public TimeCell(TimeFormat format) {
		this.format = format;
	}

	@Override
	public void render(Context context, Double value, SafeHtmlBuilder sb) {
		if (value == null) value = 0.0;
		sb.append(format.format(value));
	}
}
