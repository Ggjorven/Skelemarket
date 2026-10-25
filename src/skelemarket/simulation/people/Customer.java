package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;
import skelemarket.simulation.carrier.Basket;
import skelemarket.simulation.carrier.Carrier;
import skelemarket.simulation.carrier.ShoppingCart;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.PathNode;
import skelemarket.simulation.Config;
import skelemarket.simulation.map.ProductSpecification;

public class Customer extends Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private State mCurrentState = State.TakeCart;
	private final List<String> mShoppingList = new ArrayList<>();
	private Carrier mCarrier = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Customer(Vec2f position, Vec2f size, Texture textureRef, UV coords, Map mapRef) {
		super(position, size, textureRef, coords);

		int totalUnits = 0;

		if (mapRef != null) {
			List<String> productNames = mapRef.getProductNames();

			if (!productNames.isEmpty()) {
				Random random = new Random();
				int amount = random.nextInt(Config.MAX_SHOPPING_LIST_SIZE) + 1;

				for (int i = 0; i < amount; i++) {
					String name = productNames.get(random.nextInt(productNames.size()));
					mShoppingList.add(name);

					Optional<ProductSpecification> product = mapRef.getProduct(name);
					if (product.isPresent()) {
						totalUnits = totalUnits + product.get().getWeight();
					}
				}
			}
		}

		if (totalUnits <= Config.BASKET_CAPACITY) {
			mCarrier = new Basket();
		} else {
			mCarrier = new ShoppingCart();
		}
	}

	public Optional<PathNode> getNextDestination(skelemarket.simulation.map.Map map) {
		// TODO: Make readable
		return mShoppingList.stream().map(product -> map.getDestinationNodeForProduct(product, false))
				.flatMap(Optional::stream).findFirst();
	}

	@Override
	public void update(Map mapRef) {
		// Temporary walk test: walk 50 pixels down, then finish
		if (mCurrentState == State.TakeCart) {
			setDestination(new Vec2f(mPosition.getX(), mPosition.getY() + 50));
			mCurrentState = State.Walk;
		}

		if (mCurrentState == State.Walk) {
			stepTowardsDestination();

			if (hasArrived()) {
				mCurrentState = State.Finished;
			}
		}

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
