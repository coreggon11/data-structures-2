package application.business;

import java.util.List;

/**
 * class holding info about a plot
 */
public class Plot extends PositionObject<Estate> {

	public Plot(int id, String description, List<Estate> estates, Position position) {
		super(id, description, estates, position);
		if (estates != null) {
			for (Estate estate : estates) {
				estate.addObject(this);
			}
		}
	}

}
