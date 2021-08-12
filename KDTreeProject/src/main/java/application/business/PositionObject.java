package application.business;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
/**
 * class holding info about a position object (plot or estate)
 */
public abstract class PositionObject<E extends PositionObject> {
	private int id;
	private String description;
	private List<E> objects;
	private Position position;

	public String toString() {
		return String.format("(%s) Id: %d, %s, %s", this instanceof Estate ? "N" : "P", id, description, position.toString());
	}

	public void clearList() {
		for (E e : objects) {
			//noinspection unchecked
			e.removeObject(this);
		}
		objects.clear();
	}

	public void removeObject(E object) {
		objects.remove(object);
	}

	public boolean equals(Object other) {
		PositionObject object = (PositionObject) other;
		return this.getClass().equals(other.getClass()) && this.getId() == (object).getId() &&
				this.getDescription().equals((object).getDescription()) && this.position.equals(object.position);
	}

	protected void addObject(E object) {
		objects.add(object);
	}
}
