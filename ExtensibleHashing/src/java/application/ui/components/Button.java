package application.ui.components;

import java.awt.*;

public class Button extends Clickable {

	private final Color borderColor;
	private final Color backgroundColor;
	private final Color textColor;
	private final String text;

	public Button(int x, int y, int width, int height, Action action, Color borderColor, Color backgroundColor, Color textColor, String text) {
		super(x, y, width, height, action);
		this.borderColor = borderColor;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
		this.text = text;
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(borderColor);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(textColor);
		int width = g.getFontMetrics().stringWidth(text);
		// center the text
		g.drawString(text, getX() + (getWidth() / 2) - (width / 2), getY() + 20);
	}
}
