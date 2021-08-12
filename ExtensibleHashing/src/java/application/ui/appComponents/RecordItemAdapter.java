package application.ui.appComponents;

import application.business.Property;
import application.ui.Styles;

import java.awt.*;

public class RecordItemAdapter extends DatabaseItemAdapter {

	private final String propertyInfo;

	public RecordItemAdapter(int x, int width, int height, Property property) {
		super(x, width, height, null);
		propertyInfo = property.toString();
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Styles.COLOR_MAIN_BUTTON_BACKGROUND);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(Styles.COLOR_MAIN_BUTTON_BORDER);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(Styles.COLOR_MAIN_BUTTON_BACKGROUND);
		g.fillRect(getX() + 1, getY() + getHeight(), getWidth() - 1, 1);
		g.setColor(Styles.COLOR_MAIN_BUTTON_BORDER);
		g.drawString(propertyInfo, getX() + 10, getY() + 20);
	}
}
