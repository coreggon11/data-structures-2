package application.ui.appComponents;

import application.ui.Styles;

import java.awt.*;

public class BlockHeaderItemAdapter extends DatabaseItemAdapter {

	private final int address;
	private final boolean isCongesting;

	public BlockHeaderItemAdapter(int x, int width, int height, int address, boolean isCongesting) {
		super(x, width, height, null);
		this.address = address;
		this.isCongesting = isCongesting;
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Styles.COLOR_SECONDARY_BUTTON_BACKGROUND);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		g.setColor(Styles.COLOR_MAIN_BUTTON_BORDER);
		g.drawRect(getX(), getY(), getWidth(), getHeight());
		String blockName = isCongesting ? "> Preplňujúci blok" : "Blok";
		g.drawString(String.format("%s [%d]", blockName, address), getX() + 10, getY() + 20);
	}
}
