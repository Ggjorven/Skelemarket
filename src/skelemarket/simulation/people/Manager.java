package skelemarket.simulation.people;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import skelemarket.simulation.map.Shelf;

/** Coordinates low-stock notifications and assigns each shelf to one stocker. */
public class Manager {
	private final Queue<Shelf> mRestockRequests = new ArrayDeque<>();
	private final Set<Integer> mOutstandingShelfIDs = new HashSet<>();

	public void notifyLowStock(Shelf shelf) {
		if (!shelf.needsRestock() || !mOutstandingShelfIDs.add(shelf.getID()))
			return;

		mRestockRequests.add(shelf);
	}

	public Optional<Shelf> claimNextRestockRequest() {
		return Optional.ofNullable(mRestockRequests.poll());
	}

	public void completeRestock(Shelf shelf) {
		mOutstandingShelfIDs.remove(shelf.getID());
	}

	public int getPendingRestockCount() {
		return mRestockRequests.size();
	}
}
