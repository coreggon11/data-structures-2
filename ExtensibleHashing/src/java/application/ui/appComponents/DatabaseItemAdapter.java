package application.ui.appComponents;

import application.ui.components.Action;
import application.ui.components.RecyclerView;

public abstract class DatabaseItemAdapter extends RecyclerView.RecyclerViewAdapter {

	public DatabaseItemAdapter(int x, int width, int height, Action action) {
		super(x, width, height, action);
	}

}
