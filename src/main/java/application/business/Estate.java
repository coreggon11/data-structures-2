package application.business;

import java.util.List;

/**
 * class holding info about an estate
 */
public class Estate extends PositionObject<Plot> {

	public Estate(int id, String description, List<Plot> plots, Position position) {
		super(id, description, plots, position);
		if (plots != null) {
			for (Plot plot : plots) {
				plot.addObject(this);
			}
		}
	}

}
