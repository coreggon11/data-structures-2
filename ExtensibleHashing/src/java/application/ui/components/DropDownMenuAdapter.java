package application.ui.components;

import lombok.Getter;

import java.awt.*;

public class DropDownMenuAdapter extends RecyclerView.RecyclerViewAdapter {

	@Getter
	private final String text;
	private final Color borderColor;
	private final Color backgroundColor;
	private final Color textColor;

	public DropDownMenuAdapter(int x, int width, int height, DropDownMenu menu, String text,
							   Color borderColor, Color backgroundColor, Color textColor) {
		super(x, width, height, null);
		setAction(() -> {
			menu.setSelected(this);
			menu.doAction();
		});
		this.text = text;
		this.borderColor = borderColor;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(borderColor);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(textColor);
		g.drawString(text, getX() + 10, getY() + 15);
	}
}
