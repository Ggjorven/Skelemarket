package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;
import skelemarket.simulation.map.PathNode;

public class Customer extends Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private State mCurrentState = State.TakeCart;
	private final List<String> mShoppingList = new ArrayList<>();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Customer(Vec2f position, Vec2f size, Texture textureRef, UV coords) {
		super(position, size, textureRef, coords);

		// TODO: Generate the shopping list

		// TODO: new Carrier based on the amount of products
	}

	public List<String> getShoppingList() {
		return List.copyOf(mShoppingList);
	}

	public Optional<PathNode> getNextDestination(skelemarket.simulation.map.Map map) {
		// TODO: Make readable
		return mShoppingList.stream().map(map::getDestinationNodeForProduct).flatMap(Optional::stream).findFirst();
	}

	@Override
	public void update() {
		// TODO: Take a cart
		// TODO: Get first item from shopping list
		// TODO: Walk to the first shelf
		// TODO: take item, if no item than skip item (maybe in the future angry
		// customer)
		// TODO: walk to the next shelf, or walk to the cash register
		// TODO: wait in line
		// TODO: Buy the products
		// TODO: walk to the end point of the Store
		// TODO: Customer Finished
	}

	public boolean isFinished() {
		return mCurrentState == State.Finished;
	}

	private enum State {
		TakeCart,
		Walk,
		TakeProduct,
		WaitInLine,
		Buy,
		Finished
	}
}
