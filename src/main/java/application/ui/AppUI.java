package application.ui;

import application.state.AppState;
import application.state.StateMain;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class AppUI extends Canvas implements Runnable {

	private boolean running;
	private AppState appState;

	public AppUI(Application application, Dimension d) {
		this.setSize(d);
		this.setVisible(true);
		this.setBackground(Color.GRAY);

		changeState(new StateMain(application));
	}

	@Override
	public void run() throws NullPointerException {
		running = true;

		while (running) {

			try {
				render();
			} catch (Exception e) {
				if (!(e instanceof java.util.ConcurrentModificationException))
					e.printStackTrace();
			}
		}

		System.exit(1);
	}

	public void stop() {
		running = false;
	}

	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();

		g.setColor(Styles.COLOR_BACKGROUND);
		g.fillRect(0, 0, Application.WIDTH, Application.HEIGHT);

		// >>>>>>>>>> DRAW HERE <<<<<<<<<< \\

		appState.render(g);

		// >>>>>>> END OF DRAWING <<<<<<<< \\

		g.dispose();
		bs.show();

	}

	public void changeState(AppState newState) {
		if (appState != null) {
			removeMouseListener(appState);
			removeKeyListener(appState);
			removeMouseWheelListener(appState);
		}
		appState = newState;
		addMouseListener(newState);
		addKeyListener(newState);
		addMouseWheelListener(newState);
	}

}
