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
	private Set<Tile> mTargets = new LinkedHashSet<>();

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

	public Set<PathNode> getNeighbours() {
		return Collections.unmodifiableSet(mNeighbours);
	}

	public Set<Tile> getTargets() {
		return Collections.unmodifiableSet(mTargets);
	}

	void addNeighbour(PathNode neighbour) {
		if (neighbour != this)
			mNeighbours.add(neighbour);
	}

	void addTarget(Tile tile) {
		mTargets.add(tile);
	}
}
