package main;

import application.business.Property;
import application.state.StateMain;
import application.ui.AppUI;
import exceptions.KeyAlreadyPresentException;
import structures.ExtensibleHashing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

public class Application extends JFrame {

	public static final String APP_NAME = "GeodApp";
	public static final String VERSION = "2.0 alpha";

	public static final int WIDTH = 715;
	public static final int HEIGHT = 600;

	private final AppUI appUi;
	private final AppData appData;

	public Application() {
		super(String.format("%s v %s", APP_NAME, VERSION));

		Dimension size = new Dimension(WIDTH, HEIGHT);

		this.setSize(size);
		this.setMinimumSize(size);
		this.setUndecorated(true);
		this.setPreferredSize(size);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.appUi = new AppUI(this, size, new StateMain(this));
		this.appData = new AppData();

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				appUi.stop();
			}
		});

		this.add(appUi);

		this.pack();

		this.setVisible(true);
		appUi.run();

	}


	public void insertProperty(Property property) throws IOException, KeyAlreadyPresentException {
		appData.insertProperty(property);
	}

	public Property findProperty(Property property) throws IOException {
		return appData.findProperty(property);
	}

	public void update(Property oldProperty, Property newProperty) throws IOException, KeyAlreadyPresentException {
		appData.update(oldProperty, newProperty);
	}

	public Property delete(Property property) throws IOException {
		return appData.delete(property);
	}

	public List<ExtensibleHashing.Block<Property>> showDb() throws IOException {
		return appData.showDb();
	}

	public void exit() {
		try {
			appData.exit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void generate() throws IOException {
		appData.generate();
	}
}
