package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import skelemarket.core.Logger;
import skelemarket.core.Vec2f;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.Shelf;
import skelemarket.simulation.map.Checkout;
import skelemarket.simulation.map.ProductSpecification;
import skelemarket.simulation.map.PathNode;
import skelemarket.simulation.map.Exit;
import skelemarket.simulation.map.Tile;
import skelemarket.simulation.carrier.Basket;
import skelemarket.simulation.carrier.Carrier;
import skelemarket.simulation.carrier.ShoppingCart;
import skelemarket.simulation.Config;

public class Customer extends Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private State mCurrentState = State.TakeCart;
	private final List<String> mShoppingList = new ArrayList<>();
	private Carrier mCarrier = null;
	private String mCurrentProduct = null;
	private Shelf mTargetShelf = null;
	private Checkout mTargetCheckout = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Customer(Vec2f position, Vec2f size, Texture textureRef, UV coords, Map mapRef) {
		super(position, size, textureRef, coords);

		int totalUnits = 0;

		if (mapRef != null) {
			List<String> productNames = new ArrayList<>(mapRef.getProductNames());

			Random random = new Random();

			if (!productNames.isEmpty()) {
				Collections.shuffle(productNames, random);
				int amount = random.nextInt(Math.min(Config.MAX_SHOPPING_LIST_SIZE, productNames.size())) + 1;

				for (String name : productNames) {
					if (mShoppingList.size() == amount)
						break;

					Optional<ProductSpecification> product = mapRef.getProduct(name);
					if (product.isEmpty() || totalUnits + product.get().getWeight() > Config.SHOPPING_CART_CAPACITY)
						continue;

					mShoppingList.add(name);
					totalUnits += product.get().getWeight();
				}
			}
		}

		if (totalUnits <= Config.BASKET_CAPACITY) {
			mCarrier = new Basket();
		} else {
			mCarrier = new ShoppingCart();
		}
	}

	@Override
	public void update(Map mapRef, PeopleRegister peopleRegisterRef) {
		if (mCurrentState == State.TakeCart) {
			chooseNextProduct(mapRef);
		}

		if (mCurrentState == State.Walk) {
			if (stepAlongPath()) {
				startAction(Config.TAKE_PRODUCT_TICKS);
				mCurrentState = State.TakeProduct;
			}
		}

		if (mCurrentState == State.TakeProduct) {
			tickAction();

			if (isActionDone()) {
				if (takeCurrentProduct(mapRef))
					mShoppingList.remove(0);
				chooseNextProduct(mapRef);
			}
		}

		if (mCurrentState == State.WalkToCheckout) {
			if (stepAlongPath()) {
				mTargetCheckout.join(this);
				mCurrentState = State.WaitInLine;
			}
		}

		if (mCurrentState == State.WaitInLine) {
			if (mTargetCheckout.isFirst(this)) {
				startAction(mTargetCheckout.getPaymentTicks());
				mCurrentState = State.Buy;
			}
		}

		if (mCurrentState == State.Buy) {
			tickAction();

			if (isActionDone()) {
				mCarrier.takeAll();
				mTargetCheckout.leave(this);
				chooseExit(mapRef);
			}
		}

		if (mCurrentState == State.WalkToExit) {
			if (stepAlongPath()) {
				mCurrentState = State.Finished;
			}
		}
	}

	private boolean takeCurrentProduct(Map mapRef) {
		Optional<ProductSpecification> product = mapRef.getProduct(mCurrentProduct);

		if (product.isPresent() && mTargetShelf != null
				&& mCarrier.canAdd(product.get())
				&& mTargetShelf.takeProduct(mCurrentProduct)) {
			mCarrier.add(product.get());
			return true;
		}

		return false;
	}

	public List<String> getShoppingList() {
		return List.copyOf(mShoppingList);
	}

	public boolean isFinished() {
		return mCurrentState == State.Finished;
	}

	private void chooseNextProduct(Map mapRef) {
		if (mShoppingList.isEmpty()) {
			chooseCheckout(mapRef);
			return;
		}

		mCurrentProduct = mShoppingList.get(0);

		Optional<PathNode> node = mapRef.getDestinationNodeForProduct(mCurrentProduct, false);

		if (!node.isPresent()) {
			Logger.warn("Skipping unavailable product: %s.", mCurrentProduct);
			mShoppingList.remove(0);
			chooseNextProduct(mapRef);
			return;
		}

		mTargetShelf = null;

		Tile target = node.get().getTarget();
		if (target instanceof Shelf shelf) {
			mTargetShelf = shelf;
		}
		if (planPath(mapRef, node.get())) {
			mCurrentState = State.Walk;
		} else {
			mShoppingList.remove(0);
			chooseNextProduct(mapRef);
		}
	}

	private void chooseCheckout(Map mapRef) {
		Checkout best = null;

		for (Checkout checkout : mapRef.getCheckouts()) {
			if (best == null || checkout.getQueueLength() < best.getQueueLength()) {
				best = checkout;
			}
		}

		if (best == null) {
			mCurrentState = State.Finished;
			return;
		}

		mTargetCheckout = best;

		Optional<PathNode> node = mapRef.getNodeForTile(mTargetCheckout.getID());
		if (!node.isPresent()) {
			mCurrentState = State.Finished;
			return;
		}

		if (planPath(mapRef, node.get())) {
			mCurrentState = State.WalkToCheckout;
		} else {
			mCurrentState = State.Finished;
		}
	}

	private void chooseExit(Map mapRef) {
		Exit exit = mapRef.getExit();
		if (exit == null) {
			mCurrentState = State.Finished;
			return;
		}

		Optional<PathNode> node = mapRef.getNodeForTile(exit.getID());
		if (!node.isPresent()) {
			mCurrentState = State.Finished;
			return;
		}

		if (planPath(mapRef, node.get())) {
			mCurrentState = State.WalkToExit;
		} else {
			mCurrentState = State.Finished;
		}
	}

	private enum State {
		TakeCart,
		Walk,
		TakeProduct,
		WalkToCheckout,
		WaitInLine,
		Buy,
		WalkToExit,
		Finished
	}
}
