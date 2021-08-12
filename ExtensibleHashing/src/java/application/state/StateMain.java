package application.state;

import application.business.Property;
import application.ui.Styles;
import application.ui.appComponents.BlockHeaderItemAdapter;
import application.ui.appComponents.DatabaseItemAdapter;
import application.ui.appComponents.RecordItemAdapter;
import application.ui.appComponents.RecordPositionItemAdapter;
import application.ui.components.Button;
import application.ui.components.RecyclerView;
import application.ui.components.TextEdit;
import application.ui.components.TextView;
import exceptions.KeyAlreadyPresentException;
import main.Application;
import structures.ExtensibleHashing;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class StateMain extends AppState {

	public StateMain(Application application) {

		int startPos = 50;
		int x = startPos;
		int y = startPos;

		int smallFieldWidth = 100;
		int mediumFieldWidth = 200;
		int largeFieldWidth = 300;
		int extraLargeFieldWidth = 500;
		int buttonHeight = 30;
		int horizontalSpacing = 15;
		int verticalSpacing = 15;

		int buttonWidth = 145;
		int buttonSpace = 10;

		TextView tvId = new TextView(x, y, smallFieldWidth, buttonHeight, Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT,
				"Identifikačné číslo", false);
		addClickable(tvId);

		TextEdit teId = new TextEdit(x += smallFieldWidth, y, mediumFieldWidth, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Identifikačné číslo", false);
		addClickable(teId);

		TextView tvNumber = new TextView(x = startPos, y += verticalSpacing + buttonHeight, smallFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Súpisné číslo", false);
		addClickable(tvNumber);

		TextEdit teNumber = new TextEdit(x += smallFieldWidth, y, mediumFieldWidth, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Súpisné číslo", false);
		addClickable(teNumber);

		TextView tvDesc = new TextView(x = startPos, y += verticalSpacing + buttonHeight, smallFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Popis", false);
		addClickable(tvDesc);

		TextEdit teDesc = new TextEdit(x += smallFieldWidth, y, mediumFieldWidth, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Popis", false, Property.DESC_LENGTH);
		addClickable(teDesc);

		TextView tvGps11 = new TextView(x = startPos, y += verticalSpacing + buttonHeight, smallFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Šírka 1", false);
		addClickable(tvGps11);

		TextEdit teGps11 = new TextEdit(x += smallFieldWidth, y, mediumFieldWidth, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Šírka 1", false, Property.DESC_LENGTH);
		addClickable(teGps11);

		TextView tvGps12 = new TextView(x = startPos, y += verticalSpacing + buttonHeight, smallFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Dĺžka 1", false);
		addClickable(tvGps12);

		TextEdit teGps12 = new TextEdit(x += smallFieldWidth, y, mediumFieldWidth, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Dĺžka 1", false, Property.DESC_LENGTH);
		addClickable(teGps12);

		TextView tvGps21 = new TextView(x = startPos, y += verticalSpacing + buttonHeight, smallFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Šírka 2", false);
		addClickable(tvGps21);

		TextEdit teGps21 = new TextEdit(x += smallFieldWidth, y, mediumFieldWidth, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Šírka 2", false, Property.DESC_LENGTH);
		addClickable(teGps21);

		TextView tvGps22 = new TextView(x = startPos, y += verticalSpacing + buttonHeight, smallFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Dĺžka 2", false);
		addClickable(tvGps22);

		TextEdit teGps22 = new TextEdit(x += smallFieldWidth, y, mediumFieldWidth, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Dĺžka 2", false, Property.DESC_LENGTH);
		addClickable(teGps22);

		TextView tvResult = new TextView(x = startPos, y += verticalSpacing + buttonHeight, extraLargeFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "", false);
		addClickable(tvResult);

		AtomicReference<TextView> propertyReference = new AtomicReference<>();
		AtomicReference<TextView> gpsReference = new AtomicReference<>();
		AtomicReference<Property> foundPropertyReference = new AtomicReference<>();

		Button addButton = new Button(x, y += verticalSpacing + buttonHeight, buttonWidth, buttonHeight, () -> {
			if (teId.getText().trim().isEmpty()) {
				tvResult.setText("Nie je zadané identifikačné číslo!", Styles.COLOR_ERROR_TEXT);
				return;
			}
			try {
				int id = teId.getInt();
				int number = teNumber.getInt();
				String description = teDesc.getText();
				double gps11 = teGps11.getDouble();
				double gps12 = teGps12.getDouble();
				double gps21 = teGps21.getDouble();
				double gps22 = teGps22.getDouble();
				Property property = new Property(id, number, description, new double[]{gps11, gps12}, new double[]{gps21, gps22});
				application.insertProperty(property);
				foundPropertyReference.set(null);
				propertyReference.get().clear();
				gpsReference.get().clear();
				tvResult.setText("Nehnuteľnosť pridaná!", Styles.COLOR_SUCCESS_TEXT);
			} catch (NumberFormatException e) {
				tvResult.setText("Nesprávny formát čísla!", Styles.COLOR_ERROR_TEXT);
			} catch (IOException e) {
				tvResult.setText("Chyba pri vkladaní!", Styles.COLOR_ERROR_TEXT);
			} catch (KeyAlreadyPresentException e) {
				tvResult.setText("Nehnuteľnosť so zadaným id už existuje!", Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Pridať");
		addClickable(addButton);

		Button findButton = new Button(x += buttonWidth + buttonSpace, y, buttonWidth, buttonHeight, () -> {
			if (teId.getText().trim().isEmpty()) {
				tvResult.setText("Nie je zadané identifikačné číslo!", Styles.COLOR_ERROR_TEXT);
				return;
			}
			try {
				Property property = application.findProperty(new Property(teId.getInt()));
				if (property == null) {
					tvResult.setText("Nehnuteľnosť sa nenašla!", Styles.COLOR_ERROR_TEXT);
					foundPropertyReference.set(null);
					propertyReference.get().clear();
					gpsReference.get().clear();
				} else {
					teId.setText(property.getId());
					teNumber.setText(property.getListingNumber());
					teDesc.setText(property.getDescription());
					teGps11.setText(property.getGps1()[0]);
					teGps12.setText(property.getGps1()[1]);
					teGps21.setText(property.getGps2()[0]);
					teGps22.setText(property.getGps2()[1]);
					tvResult.setText("Nehnuteľnosť nájdená!", Styles.COLOR_SUCCESS_TEXT);
					foundPropertyReference.set(property);
					propertyReference.get().setText(property.toString(), Styles.COLOR_MAIN_TEXT);
					gpsReference.get().setText(String.format("[%f,%f], [%f, %f]", property.getGps1()[0], property.getGps1()[1], property.getGps2()[0], property.getGps2()[1]),
							Styles.COLOR_MAIN_TEXT);
				}
			} catch (IOException e) {
				tvResult.setText("Chyba pri vyhľadávaní!", Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Vyhľadať");
		addClickable(findButton);

		Button editButton = new Button(x = startPos, y += verticalSpacing + buttonHeight, buttonWidth, buttonHeight, () -> {
			if (foundPropertyReference.get() == null) {
				tvResult.setText("Nie je vyhľadaná nehnuteľnosť na upravenie!", Styles.COLOR_ERROR_TEXT);
				return;
			}
			if (teId.getText().trim().isEmpty()) {
				tvResult.setText("Nie je zadané identifikačné číslo!", Styles.COLOR_ERROR_TEXT);
				return;
			}
			try {
				int id = teId.getInt();
				int number = teNumber.getInt();
				String description = teDesc.getText();
				double gps11 = teGps11.getDouble();
				double gps12 = teGps12.getDouble();
				double gps21 = teGps21.getDouble();
				double gps22 = teGps22.getDouble();
				Property property = new Property(id, number, description, new double[]{gps11, gps12}, new double[]{gps21, gps22});
				application.update(foundPropertyReference.get(), property);
				foundPropertyReference.set(property);
				propertyReference.get().setText(property.toString(), Styles.COLOR_MAIN_TEXT);
				gpsReference.get().setText(String.format("[%f,%f], [%f, %f]", property.getGps1()[0], property.getGps1()[1], property.getGps2()[0], property.getGps2()[1]),
						Styles.COLOR_MAIN_TEXT);
				tvResult.setText("Nehnuteľnosť upravená!", Styles.COLOR_SUCCESS_TEXT);
			} catch (NumberFormatException e) {
				tvResult.setText("Nesprávny formát čísla!", Styles.COLOR_ERROR_TEXT);
			} catch (IOException e) {
				tvResult.setText("Chyba pri upravovaní!", Styles.COLOR_ERROR_TEXT);
			} catch (KeyAlreadyPresentException e) {
				tvResult.setText("Nehnuteľnosť so zadaným id už existuje!", Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Upraviť");
		addClickable(editButton);

		Button deleteButton = new Button(x += buttonWidth + buttonSpace, y, buttonWidth, buttonHeight, () -> {
			if (teId.getText().trim().isEmpty()) {
				tvResult.setText("Nie je zadané identifikačné číslo!", Styles.COLOR_ERROR_TEXT);
				return;
			}
			try {
				Property property = application.delete(new Property(teId.getInt()));
				if (property == null) {
					tvResult.setText("Nehnuteľnosť sa nenašla!", Styles.COLOR_ERROR_TEXT);
				} else {
					tvResult.setText("Nehnuteľnosť odstránená!", Styles.COLOR_SUCCESS_TEXT);
				}
				foundPropertyReference.set(null);
				propertyReference.get().clear();
				gpsReference.get().clear();
			} catch (IOException e) {
				tvResult.setText("Chyba pri odstraňovaní!", Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Odstrániť");
		addClickable(deleteButton);

		TextView tvProperty = new TextView(x = startPos, y += verticalSpacing + buttonHeight, largeFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "", false);
		addClickable(tvProperty);
		propertyReference.set(tvProperty);

		TextView tvGps = new TextView(x = startPos, y += buttonHeight, largeFieldWidth, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "", false);
		addClickable(tvGps);
		gpsReference.set(tvGps);

		y = startPos;
		startPos = startPos + smallFieldWidth + mediumFieldWidth + horizontalSpacing;

		AtomicReference<RecyclerView<DatabaseItemAdapter>> dbRvReference = new AtomicReference<>();

		Button showDbButton = new Button(x = startPos, y, largeFieldWidth, buttonHeight, () -> {
			try {
				List<ExtensibleHashing.Block<Property>> blocks = application.showDb();
				RecyclerView<DatabaseItemAdapter> rv = dbRvReference.get();
				List<DatabaseItemAdapter> rvItemsList = new ArrayList<>();
				for (ExtensibleHashing.Block<Property> block : blocks) {
					handleBlock(rv, buttonHeight, block, rvItemsList, false);
					// add congesting
					while (block.hasNext()) {
						block = block.getNext();
						handleBlock(rv, buttonHeight, block, rvItemsList, true);
					}
				}
				rv.setItems(rvItemsList);
			} catch (IOException e) {
				tvResult.setText("Chyba pri zobrazovaní databázy!", Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Zobraziť databázu");
		addClickable(showDbButton);

		RecyclerView<DatabaseItemAdapter> rvDatabase = new RecyclerView<>(startPos, y += buttonHeight + verticalSpacing, largeFieldWidth, buttonHeight, 13,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, this);
		addClickable(rvDatabase);
		dbRvReference.set(rvDatabase);

		Button exitButton = new Button(x += mediumFieldWidth, y += buttonHeight * 13 + verticalSpacing, smallFieldWidth, buttonHeight, application::exit,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Ukončiť aplikáciu");
		addClickable(exitButton);

		Button generateButton = new Button(x -= smallFieldWidth + verticalSpacing, y, smallFieldWidth, buttonHeight, () -> {
			try {
				tvResult.setText("Generovanie dát začalo!", Styles.COLOR_SUCCESS_TEXT);
				application.generate();
				tvResult.setText("Dáta úspešne vygenerované!", Styles.COLOR_SUCCESS_TEXT);
			} catch (IOException e) {
				tvResult.setText("Chyba pri generovaní dát!", Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Vygenerovať dáta");
		addClickable(generateButton);

	}

	private void handleBlock(RecyclerView<DatabaseItemAdapter> rv, int buttonHeight, ExtensibleHashing.Block<Property> block,
							 List<DatabaseItemAdapter> rvItemsList, boolean isCongesting) {
		rvItemsList.add(new BlockHeaderItemAdapter(rv.getX(), rv.getWidth(), buttonHeight, block.getAddress(), isCongesting));
		// add all items
		for (Property property : block.getValidRecords()) {
			rvItemsList.add(new RecordItemAdapter(rv.getX(), rv.getWidth(), buttonHeight, property));
			rvItemsList.add(new RecordPositionItemAdapter(rv.getX(), rv.getWidth(), buttonHeight, property));
		}
	}

}
