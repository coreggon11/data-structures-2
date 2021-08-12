package application.ui.components;

import application.state.AppState;

import java.awt.*;

public class PasswordTextEdit extends TextEdit {

	public PasswordTextEdit(int x, int y, int width, int height, AppState owner, Color borderColor, Color backgroundColor, Color textColor, String placeHolder) {
		super(x, y, width, height, owner, borderColor, backgroundColor, textColor, placeHolder);
	}

	private String hideText() {
		return "*".repeat(text.length());
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(borderColor);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(textColor);
		boolean focused = owner.getFocused() == this;
		String text = this.text.trim().isEmpty() && !focused ? placeHolder : hideText() + (focused ? "|" : "");
		int width = g.getFontMetrics().stringWidth(text);
		// center the text
		g.drawString(text, getX() + (centered ? (getWidth() / 2) - (width / 2) : 10), getY() + 20);
	}

}
