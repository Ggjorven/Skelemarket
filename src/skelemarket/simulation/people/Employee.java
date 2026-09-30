package skelemarket.simulation.people;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;

public abstract class Employee extends Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	// TODO: shared employee behaviour (tasks?)

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Employee(Vec2f position, Vec2f size, Texture textureRef, UV coords) {
		super(position, size, textureRef, coords);
	}
}
