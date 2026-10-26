package skelemarket.simulation.map;

import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

import skelemarket.simulation.people.Customer;

import java.util.ArrayList;
import java.util.List;

public class Checkout extends Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private boolean mIsSelfService = false;
	private final List<Customer> mQueue = new ArrayList<>();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Checkout(int tileID, Vec2i position, Vec2i size, Texture textureRef, UV coords, boolean isSelfService) {
		super(tileID, position, size, textureRef, coords);
		mIsSelfService = isSelfService;
	}

	public boolean isSelfService() {
		return mIsSelfService;
	}

	public void join(Customer customer) {
		mQueue.add(customer);
	}

	public boolean isFirst(Customer customer) {
		return !mQueue.isEmpty() && mQueue.get(0) == customer;
	}

	public void leave(Customer customer) {
		mQueue.remove(customer);
	}

	public int getQueueLength() {
		return mQueue.size();
	}
}
