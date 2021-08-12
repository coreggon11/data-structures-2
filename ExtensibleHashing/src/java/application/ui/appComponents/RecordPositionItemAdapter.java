package application.ui.appComponents;

import application.business.Property;
import application.ui.Styles;

import java.awt.*;
import java.util.Locale;

public class RecordPositionItemAdapter extends DatabaseItemAdapter {

	private final String propertyInfo;

	public RecordPositionItemAdapter(int x, int width, int height, Property property) {
		super(x, width, height, null);
		propertyInfo = String.format(Locale.US, "[%f,%f], [%f,%f]", property.getGps1()[0], property.getGps1()[1], property.getGps2()[0], property.getGps2()[1]);
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Styles.COLOR_MAIN_BUTTON_BACKGROUND);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(Styles.COLOR_MAIN_BUTTON_BORDER);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(Styles.COLOR_MAIN_BUTTON_BACKGROUND);
		g.fillRect(getX(), getY(), getWidth(), 1);
		g.setColor(Styles.COLOR_MAIN_BUTTON_BORDER);
		g.drawString(propertyInfo, getX() + 10, getY() + 20);
	}
}
