package application.state;


import application.business.Estate;
import application.business.Plot;
import application.business.Position;
import application.business.PositionObject;
import application.ui.Application;
import application.ui.Styles;
import application.ui.appComponents.FoundItemAdapter;
import application.ui.components.Button;
import application.ui.components.*;
import application.utils.Timer;
import exceptions.KeysCountDoesNotMatchException;
import exceptions.WrongPositionDataException;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class StateMain extends AppState {

	public StateMain(Application application) {
		super();

		int startX = 50;
		int y = 50;
		int x = startX;
		int extraLargeButton = 500;
		int largeButton = 200;
		int smallButton = 100;
		int buttonHeight = 30;
		int verticalSpacing = 15;
		int horizontalSpacing = 15;

		AtomicReference<TextView> addInfoReference = new AtomicReference<>();
		AtomicReference<PositionObject> editingObjectReference = new AtomicReference<>();

		TextEdit teDataCount = new TextEdit(x, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Počet dát");
		addClickable(teDataCount);

		Button addRandomEstates = new Button(x += horizontalSpacing + smallButton, y, largeButton + horizontalSpacing, buttonHeight, () -> {
			TextView infoTextView = addInfoReference.get();
			new Thread(() -> {
				try {
					Timer.start();
					int count = teDataCount.getInt();
					application.populate(count);
					infoTextView.setText(String.format("Pridaných %d objektov! (%d ms)", count, Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
				} catch (NumberFormatException e) {
					infoTextView.setText("Nesprávny formát čísla!", Styles.COLOR_ERROR_TEXT);
				} catch (KeysCountDoesNotMatchException e) {
					infoTextView.setText("Chyba pri pridávaní!", Styles.COLOR_ERROR_TEXT);
				}
			}).start();
			infoTextView.setText("Pridávanie začalo", Styles.COLOR_SUCCESS_TEXT);
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Vygenerovať dáta");
		addClickable(addRandomEstates);

		TextView tvFoundObjects = new TextView(x += largeButton + horizontalSpacing * 2, y, extraLargeButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Nájdené objekty", true);
		addClickable(tvFoundObjects);

		TextView tvRelationships = new TextView(x += extraLargeButton + horizontalSpacing, y, extraLargeButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Objekty zvoleného objektu", true);
		addClickable(tvRelationships);

		TextView tvTypeEstate = new TextView(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Nehnuteľnosť", true);
		addClickable(tvTypeEstate);

		AtomicReference<TextEdit> teEstateIdReference = new AtomicReference<>();
		AtomicReference<Switch> switchReference = new AtomicReference<>();

		Switch swType = new Switch(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight / 2,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_ERROR_TEXT, () -> teEstateIdReference.get().setPlaceHolder(!switchReference.get().isChecked() ? "Súpisné číslo" : "Číslo parcely"));
		addClickable(swType);
		switchReference.set(swType);

		TextView tvTypePlot = new TextView(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Parcela", true);
		addClickable(tvTypePlot);

		RecyclerView<FoundItemAdapter> rvFoundObject = new RecyclerView<>(x += smallButton + horizontalSpacing, y, extraLargeButton, buttonHeight, 13,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, this);
		addClickable(rvFoundObject);

		RecyclerView<FoundItemAdapter> rvOtherObjectsOnSelected = new RecyclerView<>(x += horizontalSpacing + extraLargeButton, y, extraLargeButton, buttonHeight, 13,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, this);
		addClickable(rvOtherObjectsOnSelected);

		TextEdit teItemId = new TextEdit(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Súpisné číslo");
		addClickable(teItemId);
		teEstateIdReference.set(teItemId);

		TextEdit teItemDesc = new TextEdit(x += smallButton + horizontalSpacing, y, largeButton + horizontalSpacing, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Popis", false, 16);
		addClickable(teItemDesc);

		TextView tvWidth = new TextView(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Šírka", false);
		addClickable(tvWidth);

		TextEdit teWidth = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "N/S", 1);
		addClickable(teWidth);

		TextEdit teWidthMax = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "N/S", 1);
		addClickable(teWidthMax);

		TextView tvWidthPos = new TextView(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Pozícia", false);
		addClickable(tvWidthPos);

		TextEdit teWidthPos = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "49.096983");
		addClickable(teWidthPos);

		TextEdit teWidthPosMax = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "49.096983");
		addClickable(teWidthPosMax);

		TextView tvLength = new TextView(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Dĺžka", false);
		addClickable(tvLength);

		TextEdit teLength = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "E/W", 1);
		addClickable(teLength);

		TextEdit teLengthMax = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "E/W", 1);
		addClickable(teLengthMax);

		TextView tvLengthPos = new TextView(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "Pozícia", false);
		addClickable(tvLengthPos);

		TextEdit teLengthPos = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "19.307601");
		addClickable(teLengthPos);

		TextEdit teLengthPosMax = new TextEdit(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, this,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT_EDIT_BACKGROUND, Styles.COLOR_MAIN_TEXT, "19.307601");
		addClickable(teLengthPosMax);

		TextView actionInfo = new TextView(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight,
				Styles.COLOR_TRANSPARENT, Styles.COLOR_TRANSPARENT, Styles.COLOR_MAIN_TEXT, "", false);
		addClickable(actionInfo);
		addInfoReference.set(actionInfo);

		Button addButton = new Button(x, y += buttonHeight + verticalSpacing, smallButton, buttonHeight, () -> {
			String type = swType.isChecked() ? "Parcela" : "Nehnuteľnosť";
			int done = 0;
			try {
				if (teWidth.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná šírka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char width = teWidth.getText().toUpperCase().charAt(0);
				done++;
				if (teLength.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná dĺžka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char length = teLength.getText().toUpperCase().charAt(0);
				if (teWidthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia šírky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double widthPosition = teWidthPos.getDouble();
				if (teLengthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia dĺžky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double lengthPosition = teLengthPos.getDouble();
				done++;
				Position position = new Position(width, length, widthPosition, lengthPosition);
				if (teItemId.getText().isEmpty()) {
					actionInfo.setText("Nie je zadané súpisné číslo!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				int itemId = teItemId.getInt();
				String desc = teItemDesc.getText();
				new Thread(() -> {
					Timer.start();
					if (swType.isChecked()) {
						try {
							application.addPlot(position, itemId, desc);
						} catch (KeysCountDoesNotMatchException e) {
							actionInfo.setText("Problém pri pridávaní!", Styles.COLOR_ERROR_TEXT);
						}
					} else {
						try {
							application.addEstate(position, itemId, desc);
						} catch (KeysCountDoesNotMatchException e) {
							actionInfo.setText("Problém pri pridávaní!", Styles.COLOR_ERROR_TEXT);
						}
					}
					actionInfo.setText(String.format("%s pridaná (%d ms)", type, Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
				}).start();
			} catch (WrongPositionDataException e) {
				actionInfo.setText(e.getMessage(), Styles.COLOR_ERROR_TEXT);
			} catch (NumberFormatException e) {
				String error = switch (done) {
					case 0 -> "Nesprávna pozícia šírky!";
					case 1 -> "Nesprávna pozícia dĺžky!";
					case 2 -> "Nesprávne súpisné číslo!";
					default -> "";
				};
				actionInfo.setText(error, Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Pridať");
		addClickable(addButton);

		Button searchButton = new Button(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, () -> {
			int done = 0;
			String type = swType.isChecked() ? "parcel" : "nehnuteľností";
			try {
				if (teWidth.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná šírka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char width = teWidth.getText().toUpperCase().charAt(0);
				done++;
				if (teLength.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná dĺžka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char length = teLength.getText().toUpperCase().charAt(0);
				if (teWidthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia šírky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double widthPosition = teWidthPos.getDouble();
				if (teLengthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia dĺžky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double lengthPosition = teLengthPos.getDouble();
				done++;
				Position position = new Position(width, length, widthPosition, lengthPosition);
				new Thread(() -> {
					try {
						Timer.start();
						int count;
						AtomicInteger counter = new AtomicInteger(0);
						if (swType.isChecked()) {
							List<Plot> plots = application.findPlots(position);
							rvFoundObject.setItems(plots.stream().map(
									plot -> mapToFoundItemAdapter(plot, rvFoundObject.getX(), extraLargeButton, buttonHeight,
											teItemId, teItemDesc, teWidth, teWidthPos, teLength, teLengthPos,
											editingObjectReference, counter, rvFoundObject, rvOtherObjectsOnSelected)
							).collect(Collectors.toList()));
							count = plots.size();
						} else {
							List<Estate> estates = application.findEstates(position);
							rvFoundObject.setItems(estates.stream().map(
									estate -> mapToFoundItemAdapter(estate, rvFoundObject.getX(), extraLargeButton, buttonHeight,
											teItemId, teItemDesc, teWidth, teWidthPos, teLength, teLengthPos,
											editingObjectReference, counter, rvFoundObject, rvOtherObjectsOnSelected)
							).collect(Collectors.toList()));
							count = estates.size();
						}
						actionInfo.setText(String.format("Našlo sa %d %s! (%d ms)", count, type, Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
					} catch (KeysCountDoesNotMatchException e) {
						actionInfo.setText("Chyba s pozíciami!", Styles.COLOR_ERROR_TEXT);
					}
				}).start();
				actionInfo.setText("Vyhľadávanie začalo", Styles.COLOR_SUCCESS_TEXT);
			} catch (WrongPositionDataException e) {
				actionInfo.setText(e.getMessage(), Styles.COLOR_ERROR_TEXT);
			} catch (NumberFormatException e) {
				String error = switch (done) {
					case 0 -> "Nesprávna pozícia šírky!";
					case 1 -> "Nesprávna pozícia dĺžky!";
					case 2 -> "Nesprávne súpisné číslo!";
					default -> "";
				};
				actionInfo.setText(error, Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Nájsť");
		addClickable(searchButton);

		Button searchIntervalButton = new Button(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, () -> {
			int done = 0;
			try {
				if (teWidth.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná šírka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char width = teWidth.getText().toUpperCase().charAt(0);
				done++;
				if (teLength.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná dĺžka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char length = teLength.getText().toUpperCase().charAt(0);
				if (teWidthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia šírky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double widthPosition = teWidthPos.getDouble();
				if (teLengthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia dĺžky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double lengthPosition = teLengthPos.getDouble();
				done++;
				Position positionMin = new Position(width, length, widthPosition, lengthPosition);
				if (teWidthMax.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná maximálna šírka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char widthMax = teWidthMax.getText().toUpperCase().charAt(0);
				done++;
				if (teLengthMax.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná maximálna dĺžka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char lengthMax = teLengthMax.getText().toUpperCase().charAt(0);
				if (teWidthPosMax.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná maximálna pozícia šírky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double widthPositionMax = teWidthPosMax.getDouble();
				if (teLengthPosMax.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná maximálna pozícia dĺžky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double lengthPositionMax = teLengthPosMax.getDouble();
				done++;
				Position positionMax = new Position(widthMax, lengthMax, widthPositionMax, lengthPositionMax);
				new Thread(() -> {
					try {
						Timer.start();
						List<PositionObject> objects = application.findObjects(positionMin, positionMax);
						AtomicInteger counter = new AtomicInteger(0);
						rvFoundObject.setItems(objects.stream().map(
								object -> mapToFoundItemAdapter(object, rvFoundObject.getX(), extraLargeButton, buttonHeight,
										teItemId, teItemDesc, teWidth, teWidthPos, teLength, teLengthPos,
										editingObjectReference, counter, rvFoundObject, rvOtherObjectsOnSelected)
						).collect(Collectors.toList()));
						actionInfo.setText(String.format("Našlo sa %d objektov! (%d ms)", objects.size(), Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
					} catch (KeysCountDoesNotMatchException e) {
						actionInfo.setText("Chyba s pozíciami!", Styles.COLOR_ERROR_TEXT);
					}
				}).start();
				actionInfo.setText("Vyhľadávanie začalo", Styles.COLOR_SUCCESS_TEXT);
			} catch (WrongPositionDataException e) {
				actionInfo.setText(e.getMessage(), Styles.COLOR_ERROR_TEXT);
			} catch (NumberFormatException e) {
				String error = switch (done) {
					case 0 -> "Nesprávna pozícia šírky!";
					case 1 -> "Nesprávna pozícia dĺžky!";
					case 2 -> "Nesprávne súpisné číslo!";
					default -> "";
				};
				actionInfo.setText(error, Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Nájsť všetky");
		addClickable(searchIntervalButton);

		Button editButton = new Button(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight, () -> {
			if (editingObjectReference.get() == null) {
				actionInfo.setText("Nie je zvolený objekt!", Styles.COLOR_ERROR_TEXT);
				return;
			}
			String type = editingObjectReference.get() instanceof Plot ? "Parcela" : "Nehnuteľnosť";
			int done = 0;
			try {
				if (teWidth.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná šírka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char width = teWidth.getText().toUpperCase().charAt(0);
				done++;
				if (teLength.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná dĺžka!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				char length = teLength.getText().toUpperCase().charAt(0);
				if (teWidthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia šírky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double widthPosition = teWidthPos.getDouble();
				if (teLengthPos.getText().isEmpty()) {
					actionInfo.setText("Nie je zadaná pozícia dĺžky!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				double lengthPosition = teLengthPos.getDouble();
				done++;
				Position position = new Position(width, length, widthPosition, lengthPosition);
				if (teItemId.getText().isEmpty()) {
					actionInfo.setText("Nie je zadané súpisné číslo!", Styles.COLOR_ERROR_TEXT);
					return;
				}
				int itemId = teItemId.getInt();
				String desc = teItemDesc.getText();
				new Thread(() -> {
					Timer.start();
					try {
						application.edit(editingObjectReference.get(), itemId, desc, position);
					} catch (KeysCountDoesNotMatchException e) {
						actionInfo.setText("Chyba pri upravovaní!", Styles.COLOR_ERROR_TEXT);
					}
					actionInfo.setText(String.format("%s upravená (%d ms)", type, Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
				}).start();
			} catch (WrongPositionDataException e) {
				actionInfo.setText(e.getMessage(), Styles.COLOR_ERROR_TEXT);
			} catch (NumberFormatException e) {
				String error = switch (done) {
					case 0 -> "Nesprávna pozícia šírky!";
					case 1 -> "Nesprávna pozícia dĺžky!";
					case 2 -> "Nesprávne súpisné číslo!";
					default -> "";
				};
				actionInfo.setText(error, Styles.COLOR_ERROR_TEXT);
			}
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Upraviť");
		addClickable(editButton);

		Button deleteButton = new Button(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, () -> {
			if (editingObjectReference.get() == null) {
				actionInfo.setText("Nie je zvolený objekt!", Styles.COLOR_ERROR_TEXT);
				return;
			}
			String type = editingObjectReference.get() instanceof Plot ? "Parcela" : "Nehnuteľnosť";
			new Thread(() -> {
				Timer.start();
				try {
					if (application.delete(editingObjectReference.get())) {
						rvFoundObject.removeCurrent();
						rvOtherObjectsOnSelected.clear();
						editingObjectReference.set(null);
						actionInfo.setText(String.format("%s odstránená (%d ms)", type, Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
					}
				} catch (KeysCountDoesNotMatchException e) {
					actionInfo.setText("Chyba pri odstraňovaní!", Styles.COLOR_ERROR_TEXT);
				}
			}).start();
			actionInfo.setText("Odstraňujem...", Styles.COLOR_SUCCESS_TEXT);
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Odstrániť");
		addClickable(deleteButton);

		Button saveButton = new Button(x = startX, y += buttonHeight + verticalSpacing, smallButton, buttonHeight, () -> {
			actionInfo.setText("Ukladanie začalo", Styles.COLOR_SUCCESS_TEXT);
			new Thread(() -> {
				Timer.start();
				try {
					application.save();
				} catch (IOException e) {
					actionInfo.setText("Vyskytol sa problém pri ukladaní!", Styles.COLOR_ERROR_TEXT);
				}
				actionInfo.setText(String.format("Dáta uložené! (%d ms)", Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
			}).start();
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Uložiť");
		addClickable(saveButton);

		Button loadButton = new Button(x += smallButton + horizontalSpacing, y, smallButton, buttonHeight, () -> {
			actionInfo.setText("Načítavanie začalo", Styles.COLOR_SUCCESS_TEXT);
			new Thread(() -> {
				Timer.start();
				try {
					application.load();
				} catch (FileNotFoundException e) {
					actionInfo.setText("Nenašiel sa súbor!", Styles.COLOR_ERROR_TEXT);
				} catch (WrongPositionDataException | KeysCountDoesNotMatchException e) {
					actionInfo.setText("Vyskytol sa problém pri načítavaní!", Styles.COLOR_ERROR_TEXT);
				}
				actionInfo.setText(String.format("Dáta načítané! (%d ms)", Timer.stop()), Styles.COLOR_SUCCESS_TEXT);
			}).start();
		}, Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_BUTTON_BACKGROUND, Styles.COLOR_MAIN_TEXT, "Načítať");
		addClickable(loadButton);

	}

	private FoundItemAdapter mapToFoundItemAdapter(PositionObject object, int x, int width, int height,
												   TextEdit teId, TextEdit teDesc, TextEdit teWidth, TextEdit teWidthPos, TextEdit teLength, TextEdit teLengthPos,
												   AtomicReference<PositionObject> editingObjectReference, AtomicInteger counter, RecyclerView rvFoundObject, RecyclerView<FoundItemAdapter> rvRelationships) {
		AtomicInteger thisId = new AtomicInteger(counter.get());
		return new FoundItemAdapter(object, x, width, height, () -> {
			teId.setText(object.getId());
			teDesc.setText(object.getDescription());
			teWidth.setText(object.getPosition().getWidth().getEntry().getKey());
			teWidthPos.setText(object.getPosition().getWidth().getEntry().getValue());
			teLength.setText(object.getPosition().getLength().getEntry().getKey());
			teLengthPos.setText(object.getPosition().getLength().getEntry().getValue());
			editingObjectReference.set(object);
			rvFoundObject.setSelectedIndex(thisId.get());
			AtomicInteger counter2 = new AtomicInteger(0);
			rvRelationships.setItems((List<FoundItemAdapter>) object.getObjects().stream()
					.map(o -> mapToRelationshipObject((PositionObject) o, rvRelationships.getX(), width, height, counter2)).collect(Collectors.toList()));
		}, counter.getAndIncrement() % 2 == 0 ? Styles.COLOR_MAIN_BUTTON_BACKGROUND : Styles.COLOR_SECONDARY_BUTTON_BACKGROUND,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT, Styles.COLOR_SELECTED_BUTTON_BACKGROUND);
	}

	private FoundItemAdapter mapToRelationshipObject(PositionObject object, int x, int width, int height, AtomicInteger counter) {
		return new FoundItemAdapter(object, x, width, height, null, counter.getAndIncrement() % 2 == 0 ? Styles.COLOR_MAIN_BUTTON_BACKGROUND : Styles.COLOR_SECONDARY_BUTTON_BACKGROUND,
				Styles.COLOR_MAIN_BUTTON_BORDER, Styles.COLOR_MAIN_TEXT);
	}

	@Override
	public void render(Graphics g) {
		super.render(g);
	}

}
