package skelemarket.simulation.map;

import skelemarket.core.Renderer;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2f;
import skelemarket.core.Vec2i;

import skelemarket.simulation.Config;
import skelemarket.simulation.people.Customer;

import java.util.ArrayList;
import java.util.List;

public class Checkout extends Tile {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private static final Texture CASHIER_TEXTURE = new Texture(Config.STOCKER_TEXTURE);
	private static final UV CASHIER_TEXTURE_COORDS = new UV(Config.STOCKER_TEXTURE_X, Config.STOCKER_TEXTURE_Y,
			Config.STOCKER_TEXTURE_WIDTH, Config.STOCKER_TEXTURE_HEIGHT);

	private final boolean mIsSelfService;
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

	@Override
	public void render(Renderer rendererRef) {
		super.render(rendererRef);

		if (!mIsSelfService)
			renderCashier(rendererRef);
	}

	public int getPaymentTicks(int productTypeCount) {
		int ticksPerProductType = mIsSelfService ? Config.SELF_CHECKOUT_PAY_TICKS : Config.MANNED_CHECKOUT_PAY_TICKS;
		return ticksPerProductType * productTypeCount;
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

	private void renderCashier(Renderer rendererRef) {
		Vec2i checkoutPosition = getPosition();
		Vec2i checkoutSize = getSize();
		Vec2f cashierPosition = new Vec2f(checkoutPosition.getX() - Config.STOCKER_WIDTH,
				checkoutPosition.getY() + checkoutSize.getY() - Config.STOCKER_HEIGHT);
		Vec2f cashierSize = new Vec2f(Config.STOCKER_WIDTH, Config.STOCKER_HEIGHT);

		rendererRef.drawQuad(CASHIER_TEXTURE, cashierPosition, cashierSize, CASHIER_TEXTURE_COORDS);
	}
}
