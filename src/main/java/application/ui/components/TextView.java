package application.ui.components;

import java.awt.*;

public class TextView extends Clickable {

	private final Color borderColor;
	private final Color backgroundColor;
	private Color textColor;
	private String text;
	private final boolean centered;

	public TextView(int x, int y, int width, int height, Color borderColor, Color backgroundColor, Color textColor, String text, boolean centered) {
		super(x, y, width, height, null);
		this.borderColor = borderColor;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
		this.centered = centered;
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
		g.drawString(text, getX() + (centered ? ((getWidth() / 2) - (width / 2)) : 0), getY() + 20);
	}

	public void setText(String text, Color color) {
		this.text = text;
		this.textColor = color;
	}
}