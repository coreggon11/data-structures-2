package application.ui.components;

import application.state.AppState;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DropDownMenu extends RecyclerView<DropDownMenuAdapter> {

	@Setter
	private boolean opened;
	private final int baseHeight;
	private final List<DropDownMenuAdapter> items;
	private final Color textColor;
	private final String placeHolder;
	@Setter
	@Getter
	private DropDownMenuAdapter selected;

	public DropDownMenu(int x, int y, int width, int buttonHeight, int maxItems, Color borderColor, Color backgroundColor, Color textColor,
						AppState owner, List<String> items, String placeHolder) {
		super(x, y, width, buttonHeight, maxItems, borderColor, backgroundColor, owner);
		setHeight(buttonHeight);
		this.textColor = textColor;
		this.baseHeight = buttonHeight;
		this.placeHolder = placeHolder;
		this.items = items.stream().map(item ->
				new DropDownMenuAdapter(x, width, buttonHeight, this, item, borderColor, backgroundColor, textColor))
				.collect(Collectors.toList());
		opened = false;
		selected = null;
		setAction(() -> {
			if (opened) {
				close();
			} else {
				open();
			}
			opened = !opened;
		});
	}

	public void open() {
		setItems(items);
	}

	public void close() {
		clear();
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY(), getWidth(), baseHeight);
		g.setColor(borderColor);
		g.drawRect(getX(), getY(), getWidth(), baseHeight);
		g.setColor(textColor);
		g.drawString(Optional.ofNullable(selected).map(DropDownMenuAdapter::getText).orElse(placeHolder), getX() + 10, getY() + 20);
		if (opened) {
			super.render(g);
		}
	}

	@Override
	protected boolean drawFreeSpace() {
		return false;
	}

	@Override
	protected int getBaseOffsetY() {
		return baseHeight;
	}

	@Override
	protected boolean canScroll() {
		return opened;
	}

}
