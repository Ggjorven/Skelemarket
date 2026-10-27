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

	private final List<ShoppingListItem> mShoppingList = new ArrayList<>();
	private Carrier mCarrier = null;

	private String mCurrentProduct = null;
	private int mCurrentProductAmount;

	private Shelf mTargetShelf = null;
	private Checkout mTargetCheckout = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Customer(Vec2f position, Vec2f size, Texture textureRef, UV coords, Map mapRef) {
		super(position, size, textureRef, coords);

		int totalWeight = 0;

		if (mapRef != null) {
			List<String> productNames = new ArrayList<>(mapRef.getProductNames());

			Random random = new Random();

			if (!productNames.isEmpty()) {
				Collections.shuffle(productNames, random);
				int productTypeAmount = random.nextInt(Math.min(Config.MAX_SHOPPING_LIST_SIZE, productNames.size()))
						+ 1;

				for (String name : productNames) {
					if (mShoppingList.size() == productTypeAmount)
						break;

					Optional<ProductSpecification> product = mapRef.getProduct(name);
					if (product.isEmpty())
						continue;

					int availableCapacity = Config.SHOPPING_CART_CAPACITY - totalWeight;
					int maximumAmount = Math.min(Config.MAX_PRODUCTS_PER_TYPE,
							availableCapacity / product.get().getWeight());
					if (maximumAmount == 0)
						continue;

					int productAmount = random.nextInt(maximumAmount) + 1;
					mShoppingList.add(new ShoppingListItem(name, productAmount));
					totalWeight += product.get().getWeight() * productAmount;
				}
			}
		}

		if (totalWeight <= Config.BASKET_CAPACITY) {
			mCarrier = new Basket();
		} else {
			mCarrier = new ShoppingCart();
		}
	}

	@Override
	public void update(Map mapRef, PeopleRegister peopleRegisterRef) {
		switch (mCurrentState) {
			case TakeCart: chooseNextProduct(mapRef);
			break;

			case Walk: {
				if (stepAlongPath()) {
					startAction(Config.TAKE_PRODUCT_TICKS);
					mCurrentState = State.TakeProduct;
				}
				break;
			}

			case TakeProduct: {
				tickAction();
				if (isActionDone()) {
					if (takeCurrentProduct(mapRef))
						mShoppingList.remove(0);
					chooseNextProduct(mapRef);
				}
				break;
			}

			case WalkToCheckout: {
				if (stepAlongPath())
					mCurrentState = State.WaitInLine;
				break;
			}

			case WaitInLine: {
				if (mTargetCheckout.isFirst(this))
					startCheckoutProduct(mapRef);
				break;
			}

			case Buy: {
				tickAction();

				if (isActionDone()) {
					List<ProductSpecification> checkedOutProducts = mCarrier.takeProducts(mCurrentProduct);
					Logger.trace("Customer checked out %d %s at %s checkout %d. Remaining products: %s.",
							checkedOutProducts.size(), mCurrentProduct,
							mTargetCheckout.isSelfService() ? "self-service" : "manned", mTargetCheckout.getID(),
							mCarrier.getProductCounts());

					if (mCarrier.hasProducts())
						startCheckoutProduct(mapRef);
					else
						finishCheckout(mapRef);
				}
				break;
			}

			case WalkToExit: {
				if (stepAlongPath())
					mCurrentState = State.Finished;
				break;
			}

			case Finished: Logger.error("Switch case should not come here!");
				break;
		}
	}

	private boolean takeCurrentProduct(Map mapRef) {
		Optional<ProductSpecification> product = mapRef.getProduct(mCurrentProduct);

		if (product.isPresent() && mTargetShelf != null) {
			int taken = 0;
			while (taken < mCurrentProductAmount && mCarrier.canAdd(product.get())
					&& mTargetShelf.takeProduct(mCurrentProduct)) {
				mCarrier.add(product.get());
				taken++;
			}

			Logger.trace("Customer wanted %d %s from %s shelf %d and took %d. Remaining products: %s.",
					mCurrentProductAmount,
					mCurrentProduct, mTargetShelf.isMagazine() ? "magazine" : "normal", mTargetShelf.getID(),
					taken, mTargetShelf.getProductCounts());

			if (taken == 0)
				return false;

			return true;
		}

		return false;
	}

	public List<ShoppingListItem> getShoppingList() {
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

		ShoppingListItem nextProduct = mShoppingList.get(0);
		mCurrentProduct = nextProduct.productName();
		mCurrentProductAmount = nextProduct.amount();

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
			mTargetCheckout.join(this);
			Logger.trace("Customer reserved %s checkout %d. Queue length: %d.",
					mTargetCheckout.isSelfService() ? "self-service" : "manned", mTargetCheckout.getID(),
					mTargetCheckout.getQueueLength());
			mCurrentState = State.WalkToCheckout;
		} else {
			mCurrentState = State.Finished;
		}
	}

	private void startCheckoutProduct(Map mapRef) {
		mCurrentProduct = mCarrier.getNextProductName();
		if (mCurrentProduct == null) {
			finishCheckout(mapRef);
			return;
		}

		startAction(mTargetCheckout.getPaymentTicks(1));
		mCurrentState = State.Buy;
	}

	private void finishCheckout(Map mapRef) {
		mTargetCheckout.leave(this);
		chooseExit(mapRef);
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

	public record ShoppingListItem(String productName, int amount) {
	}
}
