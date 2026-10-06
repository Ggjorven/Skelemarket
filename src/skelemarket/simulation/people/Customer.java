package skelemarket.simulation.people;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;

public class Customer extends Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private State mCurrentState = State.TakeCart;
	// TODO: carrier (basket or cart), type = shared superclass of both
	// TODO: shopping list, a list of products (filled when spawning)

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Customer(Vec2f position, Vec2f size, Texture textureRef, UV coords) {
		super(position, size, textureRef, coords);
	}

	@Override
	public void update() {
		// TODO: Take a cart
		// TODO: Get first item from shopping list
		// TODO: Walk to the first shelf
		// TODO: take item, if no item than skip item (maybe in the future angry customer)
		// TODO: walk to the next shelf, or walk to the cash register
		// TODO: wait in line
		// TODO: Buy the products
		// TODO: walk to the end point of the Store

	}

	private enum State {
		TakeCart,
		Walk,
		TakeProduct,
		WaitInLine,
		Buy
	}
}
