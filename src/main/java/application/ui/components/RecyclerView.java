package application.ui.components;

import application.state.AppState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecyclerView<T extends RecyclerView.RecyclerViewAdapter> extends Clickable implements Scrollable {

	private boolean[] invalidIndices;

	private final Color borderColor;
	private final Color backgroundColor;

	private List<T> items;
	private final List<T> showingItems;
	private int offset;
	private final int maxItems;

	private final AppState owner;
	private int selectedIndex;

	public RecyclerView(int x, int y, int width, int buttonHeight, int maxItems, Color borderColor, Color backgroundColor, AppState owner) {
		super(x, y, width, buttonHeight * maxItems, null);
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
		items = new ArrayList<>();
		showingItems = new ArrayList<>();
		this.maxItems = maxItems;
		this.owner = owner;
		invalidIndices = null;
	}

	public void moveUp() {
		if (offset < items.size() - maxItems) {
			++offset;
		}
		int selectedIndexBefore = selectedIndex;
		update();
		selectedIndex = selectedIndexBefore;
	}

	public void moveDown() {
		if (offset > 0) {
			--offset;
		}
		int selectedIndexBefore = selectedIndex;
		update();
		selectedIndex = selectedIndexBefore;
	}

	public void removeCurrent() {
		offset = 0;
		int plus = 0;
		for (boolean invalidIndex : invalidIndices) {
			if (invalidIndex) {
				plus++;
			}
		}
		invalidIndices[selectedIndex + plus] = true;
		this.items.remove(selectedIndex);
		update();
	}

	public void setSelectedIndex(int index) {
		int minus = 0;
		for (int i = 0; i < index; ++i) {
			if (invalidIndices[i]) {
				minus++;
			}
		}
		index -= minus;
		if (selectedIndex != -1) {
			Optional.ofNullable(items.get(selectedIndex).getOnDeselect()).ifPresent(Action::perform);
		}
		this.selectedIndex = index;
		Optional.ofNullable(items.get(selectedIndex).getOnSelect()).ifPresent(Action::perform);
	}

	public void update() {
		selectedIndex = -1;
		owner.removeClickables(showingItems.stream().map(Clickable.class::cast).collect(Collectors.toList()));
		showingItems.clear();
		for (int i = offset; i < offset + maxItems && i < items.size(); ++i) {
			RecyclerViewAdapter item = items.get(i);
			item.setY(getY() + (i - offset) * item.getHeight());
			showingItems.add(items.get(i));
		}
		owner.addClickablesAtBeginning(showingItems.stream().map(Clickable.class::cast).collect(Collectors.toList()));
	}

	public void setItems(List<T> items) {
		this.items = items;
		invalidIndices = new boolean[items.size()];
		Arrays.fill(invalidIndices, false);
		offset = 0;
		update();
	}

	public void clear() {
		setItems(new ArrayList<>());
	}

	@Override
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(borderColor);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
	}

	@Getter
	@Setter(AccessLevel.PROTECTED)
	public static abstract class RecyclerViewAdapter extends Clickable {

		private Action onSelect;
		private Action onDeselect;

		public RecyclerViewAdapter(int x, int width, int height, Action action) {
			super(x, 0, width, height, action);
		}
	}

}
