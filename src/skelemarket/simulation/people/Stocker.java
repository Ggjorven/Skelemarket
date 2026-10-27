package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import skelemarket.core.Logger;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;
import skelemarket.simulation.Config;
import skelemarket.simulation.carrier.RollContainer;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.PathNode;
import skelemarket.simulation.map.ProductSpecification;
import skelemarket.simulation.map.Shelf;

public class Stocker extends Employee {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private State mCurrentState = State.Idle;
	private final RollContainer mRollContainer = new RollContainer();
	private final List<Shelf> mTargetShelves = new ArrayList<>();
	private final List<Shelf> mStops = new ArrayList<>();
	private final java.util.Map<String, Shelf> mProductTargets = new HashMap<>();
	private int mNextStopIndex;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Stocker(Vec2f position, Vec2f size, Texture textureRef, UV coords) {
		super(position, size, textureRef, coords);
	}

	@Override
	public void update(Map mapRef, PeopleRegister peopleRegisterRef) {
		if (mCurrentState == State.Idle) {
			claimRestockTasks(mapRef, peopleRegisterRef);
			if (mTargetShelves.isEmpty())
				return;

			if (!prepareStorageStops(mapRef)) {
				completeTasks(peopleRegisterRef);
				return;
			}

			mCurrentState = State.WalkToStorage;
		}

		if (mCurrentState == State.WalkToStorage && stepAlongPath()) {
			loadFromStorage(mapRef, mStops.get(mNextStopIndex - 1));
			if (!planNextStop(mapRef)) {
				prepareTargetStops();
				if (!planNextStop(mapRef)) {
					completeTasks(peopleRegisterRef);
					return;
				}
				mCurrentState = State.WalkToShelf;
			}
		}

		if (mCurrentState == State.WalkToShelf && stepAlongPath()) {
			startAction(Config.RESTOCK_PRODUCT_TICKS);
			mCurrentState = State.RestockShelf;
		}

		if (mCurrentState == State.RestockShelf) {
			tickAction();
			if (!isActionDone())
				return;

			Shelf targetShelf = mStops.get(mNextStopIndex - 1);
			if (restockNextProduct(targetShelf) && hasProductsForTarget(targetShelf)) {
				startAction(Config.RESTOCK_PRODUCT_TICKS);
				return;
			}

			if (planNextStop(mapRef)) {
				mCurrentState = State.WalkToShelf;
				return;
			}

			if (hasMissingProducts() && prepareStorageStops(mapRef)) {
				mCurrentState = State.WalkToStorage;
				return;
			}

			completeTasks(peopleRegisterRef);
		}
	}

	private void claimRestockTasks(Map mapRef, PeopleRegister peopleRegisterRef) {
		for (int i = 0; i < Config.STOCKER_MAX_TASKS_PER_TRIP; i++) {
			Shelf shelf = peopleRegisterRef.getManager().claimNextRestockRequest(mapRef).orElse(null);
			if (shelf == null)
				return;
			mTargetShelves.add(shelf);
		}
	}

	private boolean prepareStorageStops(Map mapRef) {
		mStops.clear();
		for (Shelf targetShelf : mTargetShelves) {
			if (!targetShelf.hasMissingProducts())
				continue;

			Shelf storageShelf = findStorageShelf(mapRef, targetShelf);
			if (storageShelf != null && !mStops.contains(storageShelf))
				mStops.add(storageShelf);
		}

		Collections.shuffle(mStops);
		mNextStopIndex = 0;
		return planNextStop(mapRef);
	}

	private void prepareTargetStops() {
		mStops.clear();
		mStops.addAll(mTargetShelves);
		Collections.shuffle(mStops);
		mNextStopIndex = 0;
	}

	private boolean planNextStop(Map mapRef) {
		while (mNextStopIndex < mStops.size()) {
			Shelf nextStop = mStops.get(mNextStopIndex++);
			if (planPathToShelf(mapRef, nextStop))
				return true;
		}

		return false;
	}

	private Shelf findStorageShelf(Map mapRef, Shelf targetShelf) {
		for (Shelf shelf : mapRef.getShelvesForCategory(targetShelf.getCategory(), true)) {
			if (mapRef.getNodeForShelf(shelf).isEmpty())
				continue;

			for (String productName : targetShelf.getProductCapacities().keySet()) {
				if (shelf.getProductCount(productName) > 0)
					return shelf;
			}
		}

		return null;
	}

	private boolean planPathToShelf(Map mapRef, Shelf shelf) {
		PathNode target = mapRef.getNodeForShelf(shelf).orElse(null);
		return target != null && planPath(mapRef, target);
	}

	private void loadFromStorage(Map mapRef, Shelf storageShelf) {
		for (Shelf targetShelf : mTargetShelves) {
			if (!targetShelf.getCategory().equals(storageShelf.getCategory()))
				continue;

			for (java.util.Map.Entry<String, Integer> entry : targetShelf.getProductCapacities().entrySet()) {
				ProductSpecification product = mapRef.getProduct(entry.getKey()).orElse(null);
				if (product == null)
					continue;

				int missing = entry.getValue() - targetShelf.getProductCount(entry.getKey());
				int loaded = 0;
				while (missing > 0 && mRollContainer.canAdd(product) && storageShelf.takeProduct(entry.getKey())) {
					mRollContainer.add(product);
					mProductTargets.put(product.getName(), targetShelf);
					missing--;
					loaded++;
				}

				if (loaded > 0)
					Logger.trace("Stocker loaded %d %s from magazine shelf %d. Remaining products: %s.", loaded,
							product.getName(), storageShelf.getID(), storageShelf.getProductCounts());
			}
		}
	}

	private boolean restockNextProduct(Shelf targetShelf) {
		List<ProductSpecification> remainingProducts = new ArrayList<>();
		String productName = null;
		int restocked = 0;
		for (ProductSpecification product : mRollContainer.takeAll()) {
			if (productName == null && mProductTargets.get(product.getName()) == targetShelf)
				productName = product.getName();

			if (!product.getName().equals(productName)) {
				remainingProducts.add(product);
				continue;
			}

			int added = targetShelf.addProduct(product.getName(), 1);
			if (added == 0) {
				remainingProducts.add(product);
				continue;
			}

			restocked += added;
		}

		for (ProductSpecification product : remainingProducts)
			mRollContainer.add(product);

		if (restocked == 0)
			return false;

		Logger.trace("Stocker restocked %d %s on normal shelf %d. Remaining products: %s.", restocked,
				productName, targetShelf.getID(), targetShelf.getProductCounts());
		return true;
	}

	private boolean hasProductsForTarget(Shelf targetShelf) {
		for (java.util.Map.Entry<String, Shelf> productTarget : mProductTargets.entrySet()) {
			if (productTarget.getValue() == targetShelf && mRollContainer.hasProduct(productTarget.getKey()))
				return true;
		}

		return false;
	}

	private boolean hasMissingProducts() {
		for (Shelf shelf : mTargetShelves) {
			if (shelf.hasMissingProducts())
				return true;
		}

		return false;
	}

	private void completeTasks(PeopleRegister peopleRegisterRef) {
		mRollContainer.takeAll();
		mProductTargets.clear();
		for (Shelf shelf : mTargetShelves)
			peopleRegisterRef.getManager().completeRestock(shelf);
		mTargetShelves.clear();
		mStops.clear();
		mNextStopIndex = 0;
		mCurrentState = State.Idle;
	}

	private enum State {
		Idle,
		WalkToStorage,
		WalkToShelf,
		RestockShelf
	}
}
