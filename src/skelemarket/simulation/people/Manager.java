package skelemarket.simulation.people;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.Shelf;

public class Manager {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private final List<Shelf> mRestockRequests = new ArrayList<>();
	private final Set<Integer> mOutstandingShelfIDs = new HashSet<>();

	private final Random mRandom = new Random();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public void notifyLowStock(Shelf shelf) {
		if (!shelf.needsRestock() || !mOutstandingShelfIDs.add(shelf.getID()))
			return;

		mRestockRequests.add(shelf);
	}

	public Optional<Shelf> claimNextRestockRequest(Map mapRef) {
		if (!mRestockRequests.isEmpty())
			return Optional.of(mRestockRequests.remove(mRandom.nextInt(mRestockRequests.size())));

		List<Shelf> lowestStockShelves = mapRef.getLowestStockShelves(mOutstandingShelfIDs);
		if (lowestStockShelves.isEmpty())
			return Optional.empty();

		Shelf shelf = lowestStockShelves.get(mRandom.nextInt(lowestStockShelves.size()));
		mOutstandingShelfIDs.add(shelf.getID());
		return Optional.of(shelf);
	}

	public void completeRestock(Shelf shelf) {
		mOutstandingShelfIDs.remove(shelf.getID());
	}

	public int getPendingRestockCount() {
		return mRestockRequests.size();
	}
}
