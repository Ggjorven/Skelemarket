package skelemarket.simulation.people;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import skelemarket.core.*;
import skelemarket.simulation.Config;
import skelemarket.simulation.map.Map;
import skelemarket.simulation.map.PathNode;

public abstract class Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	protected Vec2f mPosition = new Vec2f();
	private Vec2f mSize = new Vec2f();
	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();

	private Optional<Vec2f> mDestination = Optional.empty();
	private List<PathNode> mPath = new ArrayList<>();
	private int mNextPathNodeIndex;

	protected int mRemainingTicks;

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Person(Vec2f position, Vec2f size, Texture textureRef, UV coords) {
		mPosition = position;
		mSize = size;

		mTextureRef = textureRef;
		mTextureCoords = coords;
	}

	public abstract void update(Map mapRef, PeopleRegister peopleRegisterRef);

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mTextureRef, mPosition, mSize, mTextureCoords);
	}

	protected void setDestination(Vec2f destination) {
		mDestination = Optional.of(destination);
	}

	protected void stepTowardsDestination() {
		if (hasArrived()) {
			return;
		}

		Vec2f destinationRef = mDestination.get();

		float dx = destinationRef.getX() - mPosition.getX();
		float dy = destinationRef.getY() - mPosition.getY();

		float distance = (float) Math.sqrt(dx * dx + dy * dy);

		if (distance <= Config.PERSON_SPEED) {
			mPosition.setX(destinationRef.getX());
			mPosition.setY(destinationRef.getY());

			mDestination = Optional.empty();
			return;
		}

		// NOTE: Divided by distance to prevent higher speed diagonally
		// (X:1/Y:1/X+Y:1.4)
		mPosition.setX(mPosition.getX() + dx / distance * Config.PERSON_SPEED);
		mPosition.setY(mPosition.getY() + dy / distance * Config.PERSON_SPEED);
	}

	protected void startAction(int ticks) {
		mRemainingTicks = ticks;
	}

	protected void tickAction() {
		if (mRemainingTicks > 0) {
			mRemainingTicks--;
		}
	}

	protected boolean isActionDone() {
		return mRemainingTicks == 0;
	}

	protected boolean hasArrived() {
		return mDestination.isEmpty();
	}

	protected boolean planPath(Map mapRef, PathNode target) {
		if (mapRef == null || target == null)
			return false;

		float centerX = mPosition.getX() + mSize.getX() / 2.0f;
		float centerY = mPosition.getY() + mSize.getY();

		Optional<PathNode> nearestNode = mapRef.getNearestNode(centerX, centerY);

		if (nearestNode.isPresent()) {
			Optional<List<PathNode>> path = mapRef.findPath(nearestNode.get(), target);
			if (path.isEmpty()) {
				Logger.error("Failed to find path...");
				return false;
			}

			mPath = path.get();
			mNextPathNodeIndex = 0;
			mDestination = Optional.empty();

			return true;
		}

		Logger.error("Failed to plan path, because no nearest node found.");
		return false;
	}

	protected boolean stepAlongPath() {
		while (hasArrived() && mNextPathNodeIndex < mPath.size()) {
			PathNode node = mPath.get(mNextPathNodeIndex++);
			setDestinationForNode(node);
		}

		stepTowardsDestination();
		return hasArrived() && mNextPathNodeIndex == mPath.size();
	}

	private void setDestinationForNode(PathNode node) {
		float x = node.getLocation().getX() - mSize.getX() / 2.0f;
		float y = node.getLocation().getY() - mSize.getY();

		setDestination(new Vec2f(x, y));
	}
}
