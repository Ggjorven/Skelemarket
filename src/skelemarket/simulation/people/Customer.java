package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import skelemarket.core.*;
import skelemarket.simulation.carrier.Basket;
import skelemarket.simulation.carrier.Carrier;
import skelemarket.simulation.carrier.ShoppingCart;
import skelemarket.simulation.map.*;
import skelemarket.simulation.Config;

public class Customer extends Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private State mCurrentState = State.TakeCart;
	private final List<String> mShoppingList = new ArrayList<>();
	private Carrier mCarrier = null;
	private String mCurrentProduct  = null;
	private Shelf mTargetShelf  = null;

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
		if (mCurrentState == State.TakeCart) {
			chooseNextProduct(mapRef);
		}

		if (mCurrentState == State.Walk) {
			stepTowardsDestination();

			if (hasArrived()) {
				startAction(Config.TAKE_PRODUCT_TICKS);
				mCurrentState = State.TakeProduct;
			}
		}

		if (mCurrentState == State.TakeProduct) {
			tickAction();

			if (isActionDone()) {
				takeCurrentProduct(mapRef);
				mShoppingList.remove(0);
				chooseNextProduct(mapRef);
			}
		}

		// TODO: wait in line
		// TODO: Buy the products
		// TODO: walk to the end point of the Store
		// TODO: Customer Finished
	}
	private void takeCurrentProduct(Map mapRef) {
		Optional<ProductSpecification> product = mapRef.getProduct(mCurrentProduct);

		if (product.isPresent() && mTargetShelf != null
				&& mCarrier.canAdd(product.get())
				&& mTargetShelf.takeProduct(mCurrentProduct)) {
			mCarrier.add(product.get());
		}
	}
	public boolean isFinished() {
		return mCurrentState == State.Finished;
	}
	private void chooseNextProduct(Map mapRef) {
		if (mShoppingList.isEmpty()) {
			mCurrentState = State.Finished;   // later: naar de kassa
			return;
		}

		mCurrentProduct = mShoppingList.get(0);

		Optional<PathNode> node = mapRef.getDestinationNodeForProduct(mCurrentProduct, false);
		if (!node.isPresent()){
			mShoppingList.remove(0);
			chooseNextProduct(mapRef);
			return;
		}

		mTargetShelf = null;

		Tile target = node.get().getTarget();
		if (target instanceof Shelf shelf) {
			mTargetShelf = shelf;
		}
		// TODO: Replace straight-line walking with a route from Jorben's pathfinding:
		//       walk the List<PathNode> node by node instead of straight to the shelf node.
		Vec2i location = node.get().getLocation();
		setDestination(new Vec2f(location.getX() - Config.CUSTOMER_WIDTH / 2, location.getY() - Config.CUSTOMER_HEIGHT));
		mCurrentState = State.Walk;
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
