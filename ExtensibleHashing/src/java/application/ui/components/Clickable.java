package application.ui.components;

import application.ui.Renderable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter(AccessLevel.PROTECTED)
public abstract class Clickable implements Renderable {

	private int x;
	private int y;
	private int width;
	private int height;
	private Action action;

	public void doAction() {
		if (action != null) {
			action.perform();
		}
	}

	public boolean intersects(int mx, int my) {
		return mx >= x && mx <= x + width && my >= y && my <= y + height;
	}

}
