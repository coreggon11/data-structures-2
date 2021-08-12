package application.state;

import application.ui.Renderable;
import application.ui.components.Clickable;
import application.ui.components.Scrollable;
import application.ui.components.TextEdit;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AppState implements Renderable, MouseListener, KeyListener, MouseWheelListener {

	private final List<Clickable> clickables = new ArrayList<>();

	@Setter
	@Getter
	private Clickable focused;

	public AppState() {
		focused = null;
	}

	public void addClickablesAtBeginning(List<Clickable> clickables) {
		this.clickables.addAll(0, clickables);
	}

	public void removeClickables(List<Clickable> clickables) {
		this.clickables.removeAll(clickables);
	}

	public void addClickable(Clickable clickable) {
		clickables.add(clickable);
	}


	protected void renderClickables(Graphics g) {
		for (int i = clickables.size() - 1; i >= 0; --i) {
			clickables.get(i).render(g);
		}
	}

	@Override
	public void render(Graphics g) {
		renderClickables(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (focused != null && !focused.intersects(e.getX(), e.getY())) {
			focused = null;
		}
		clickables.stream()
				.filter(clickable -> clickable.intersects(e.getX(), e.getY()))
				.findFirst()
				.ifPresent(Clickable::doAction);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO
	}

	@Override
	public void keyTyped(KeyEvent e) {
		Optional.ofNullable(focused)
				.filter(TextEdit.class::isInstance)
				.map(TextEdit.class::cast)
				.ifPresent(textEdit -> {
					if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
						textEdit.backSpace();
					} else {
						textEdit.write(e.getKeyChar());
					}
				});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		clickables.stream()
				.filter(Scrollable.class::isInstance)
				.filter(clickable -> clickable.intersects(e.getX(), e.getY()))
				.map(Scrollable.class::cast)
				.findFirst()
				.ifPresent(scrollable -> {
					if (e.getWheelRotation() <= -1) {
						scrollable.moveDown();
					} else if (e.getWheelRotation() >= 1) {
						scrollable.moveUp();
					}
				});
	}

}
