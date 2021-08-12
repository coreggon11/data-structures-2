package application.ui.components;

import lombok.Getter;

import java.awt.*;

public class Switch extends Clickable {

	private final Color borderColor;
	private final Color backgroundColor;
	private final Color switchColor;
	private final int switchWidth;
	private final int switchHeight;

	@Getter
	private boolean checked;

	public Switch(int x, int y, int width, int height, Color borderColor, Color backgroundColor, Color switchColor) {
		super(x, y, width, height, null);
		setAction(() -> checked = !checked);
		// switch is half width by default
		switchWidth = width / 3;
		switchHeight = height * 2;
		this.borderColor = borderColor;
		this.backgroundColor = backgroundColor;
		this.switchColor = switchColor;
	}

	public Switch(int x, int y, int width, int height, Color borderColor, Color backgroundColor, Color switchColor, Action secondaryAction) {
		this(x, y, width, height, borderColor, backgroundColor, switchColor);
		Action now = getAction();
		setAction(() -> {
			now.perform();
			secondaryAction.perform();
		});
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY() + (switchHeight - getHeight()) / 2, getWidth(), getHeight());
		g.setColor(borderColor);
		g.drawRect(getX(), getY() + (switchHeight - getHeight()) / 2, getWidth(), getHeight());
		g.setColor(switchColor);
		g.fillRoundRect(getX() + (checked ? getWidth() - switchWidth + 5 : -5), getY(), switchWidth, switchHeight, 45, 45);
		g.setColor(borderColor);
		g.drawRoundRect(getX() + (checked ? getWidth() - switchWidth + 5 : 0 - 5), getY(), switchWidth, switchHeight, 45, 45);
	}

	public boolean intersects(int mx, int my) {
		return (mx >= getX() && mx <= getX() + getWidth() && my >= getY() + (switchHeight - getHeight()) / 2 && my <= getY() + (switchHeight - getHeight()) / 2 + getHeight())
				|| (mx >= getX() + (checked ? getWidth() - switchWidth + 5 : -5) && mx <= getX() + (checked ? getWidth() - switchWidth + 5 : -5) + switchWidth
				&& my >= getY() && my <= getY() + switchHeight);
	}
}


