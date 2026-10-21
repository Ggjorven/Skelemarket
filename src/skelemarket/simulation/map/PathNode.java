package skelemarket.simulation.map;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import skelemarket.core.Vec2i;

public class PathNode {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private int mID = 0;

	private Vec2i mLocation = new Vec2i();

	private Set<PathNode> mNeighbours = new LinkedHashSet<>();
	private Tile mTarget = null;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public PathNode(int id, Vec2i location) {
		mID = id;
		mLocation = location;
	}

	public int getID() {
		return mID;
	}

	public Vec2i getLocation() {
		return mLocation;
	}

	void addNeighbour(PathNode neighbour) {
		if (neighbour != this)
			mNeighbours.add(neighbour);
	}

	public Set<PathNode> getNeighbours() {
		return Collections.unmodifiableSet(mNeighbours);
	}

	void setTarget(Tile tile) {
		mTarget = tile;
	}

	public Tile getTarget() {
		return mTarget;
	}

	public boolean hasTarget() {
		return mTarget != null;
	}
}
