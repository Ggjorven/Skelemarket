package skelemarket.simulation.people;

import skelemarket.core.*;
import skelemarket.simulation.Config;
import skelemarket.simulation.map.Map;

public abstract class Person {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	protected Vec2f mPosition = new Vec2f();
	private Vec2f mSize = new Vec2f();
	private Vec2f mDestination = null;
	private Texture mTextureRef = null;
	private UV mTextureCoords = new UV();
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

	protected void setDestination(Vec2f destination) {
		mDestination = destination;
	}

	protected void stepTowardsDestination() {
		if (hasArrived()) {
			return;
		}

		float dx = mDestination.getX() - mPosition.getX();
		float dy = mDestination.getY() - mPosition.getY();

		float distance = (float) Math.sqrt(dx * dx + dy * dy);

		if (distance <= Config.PERSON_SPEED) {
			mPosition.setX(mDestination.getX());
			mPosition.setY(mDestination.getY());
			mDestination = null;
			return;
		}
		mPosition.setX(mPosition.getX() + dx / distance * Config.PERSON_SPEED);
		mPosition.setY(mPosition.getY() + dy / distance * Config.PERSON_SPEED);
	}

	protected void startAction(int ticks) {
		mRemainingTicks = ticks;
	}

	protected void tickAction(){
		if (mRemainingTicks > 0){
			mRemainingTicks--;
		}
	}
	protected boolean isActionDone(){
		return mRemainingTicks == 0;
	}

	protected boolean hasArrived() {
		return mDestination == null;
	}

	public abstract void update(Map mapRef);

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mTextureRef, mPosition, mSize, mTextureCoords);
	}
}
