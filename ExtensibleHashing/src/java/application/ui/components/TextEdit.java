package application.ui.components;

import application.state.AppState;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class TextEdit extends Clickable {

	protected final Color borderColor;
	protected final Color backgroundColor;
	protected final Color textColor;

	@Setter
	protected String placeHolder;
	@Getter
	protected String text;

	private int maxLength;
	protected boolean centered;

	protected AppState owner;

	public TextEdit(int x, int y, int width, int height, AppState owner, Color borderColor, Color backgroundColor, Color textColor, String placeHolder) {
		super(x, y, width, height, null);
		this.borderColor = borderColor;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
		this.placeHolder = placeHolder;
		text = "";
		maxLength = Integer.MAX_VALUE;
		setAction(() -> owner.setFocused(this));
		centered = true;
		this.owner = owner;
	}

	public TextEdit(int x, int y, int width, int height, AppState owner, Color borderColor, Color backgroundColor, Color textColor, String placeHolder, String beginText) {
		this(x, y, width, height, owner, borderColor, backgroundColor, textColor, placeHolder);
		text = beginText;
	}

	public TextEdit(int x, int y, int width, int height, AppState owner, Color borderColor, Color backgroundColor, Color textColor, String placeHolder, String beginText, boolean centered) {
		this(x, y, width, height, owner, borderColor, backgroundColor, textColor, placeHolder, beginText);
		this.centered = centered;
	}

	public TextEdit(int x, int y, int width, int height, AppState owner, Color borderColor, Color backgroundColor, Color textColor, String placeHolder, boolean centered) {
		this(x, y, width, height, owner, borderColor, backgroundColor, textColor, placeHolder);
		this.centered = centered;
	}

	public TextEdit(int x, int y, int width, int height, AppState owner, Color borderColor, Color backgroundColor, Color textColor, String placeHolder, boolean centered, int maxLength) {
		this(x, y, width, height, owner, borderColor, backgroundColor, textColor, placeHolder, centered);
		this.maxLength = maxLength;
	}

	public TextEdit(int x, int y, int width, int height, AppState owner, Color borderColor, Color backgroundColor, Color textColor, String placeHolder, int maxLength) {
		this(x, y, width, height, owner, borderColor, backgroundColor, textColor, placeHolder);
		this.maxLength = maxLength;
	}

	public void setText(char c) {
		this.text = Character.toString(c);
	}

	public void setText(int i) {
		this.text = Integer.toString(i);
	}

	public void setText(double d) {
		this.text = Double.toString(d);
	}

	public void setText(String s) {
		this.text = s;
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(borderColor);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(textColor);
		boolean focused = owner.getFocused() == this;
		String text = this.text.trim().isEmpty() && !focused ? placeHolder : this.text + (focused ? "|" : "");
		int width = g.getFontMetrics().stringWidth(text);
		String editedText = text;
		int actualLength = text.length();
		// cut text
		while (width > getWidth() - 20) {
			actualLength /= 2;
			editedText = text.substring(text.length() - actualLength);
			width = g.getFontMetrics().stringWidth(editedText);
		}
		if (g.getFontMetrics().stringWidth(text) > getWidth() - 20) {
			int difference = (text.length() - editedText.length() + 3) / 2;
			boolean add = true;
			boolean running = true;
			while (running) {
				actualLength += difference * (add ? 1 : -1);
				editedText = text.substring(text.length() - actualLength);
				width = g.getFontMetrics().stringWidth(editedText);
				add = width < getWidth() - 20;
				running = difference != 0;
				difference /= 2;
			}
		}
		// center the text
		g.drawString(editedText, getX() + (centered ? ((getWidth() / 2) - (width / 2)) : 10), getY() + 20);
	}

	public void write(char c) {
		if (text.length() >= maxLength) {
			return;
		}
		text += c;
	}

	public void backSpace() {
		if (text.length() <= 0) {
			return;
		}
		text = text.substring(0, text.length() - 1);
	}

	public int getInt() {
		if (text.trim().isEmpty()) {
			return 0;
		}
		return Integer.parseInt(text);
	}

	public double getDouble() {
		if (text.trim().isEmpty()) {
			return 0d;
		}
		return Double.parseDouble(text);
	}
}
