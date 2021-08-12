package application.ui.appComponents;

import application.business.PositionObject;
import application.ui.components.Action;
import application.ui.components.RecyclerView;

import java.awt.*;

public class FoundItemAdapter extends RecyclerView.RecyclerViewAdapter {

	private Color backgroundColor;
	private final Color borderColor;
	private final Color textColor;
	private final PositionObject object;

	public FoundItemAdapter(PositionObject object, int x, int width, int height, Action action, Color backgroundColor, Color borderColor, Color textColor, Color selectedBackgroundColor, boolean selectable) {
		super(x, width, height, action);
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
		this.textColor = textColor;
		this.object = object;
		if (selectable) {
			setOnSelect(() -> this.backgroundColor = selectedBackgroundColor);
			setOnDeselect(() -> this.backgroundColor = backgroundColor);
		}
	}

	public FoundItemAdapter(PositionObject object, int x, int width, int height, Action action, Color backgroundColor, Color borderColor, Color textColor, Color selectedBackgroundColor) {
		this(object, x, width, height, action, backgroundColor, borderColor, textColor, selectedBackgroundColor, true);
	}

	public FoundItemAdapter(PositionObject object, int x, int width, int height, Action action, Color backgroundColor, Color borderColor, Color textColor) {
		this(object, x, width, height, action, backgroundColor, borderColor, textColor, null, false);
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(borderColor);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(textColor);
		g.drawString(object.toString(), getX() + 10, getY() + 20);
	}
}
