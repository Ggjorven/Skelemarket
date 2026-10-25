package skelemarket.simulation;

public class Config {
	public static final String MAP_FILE = "/map/map.json";
	public static final int BASKET_CAPACITY = 5;
	public static final int SHOPPING_CART_CAPACITY = 15;
	public static final int ROLL_CONTAINER_CAPACITY = 20;

	public static final int CUSTOMER_WIDTH = 21;
	public static final int CUSTOMER_HEIGHT = 48;
	public static final String CUSTOMER_TEXTURE = "/textures/carbonova-supermarket/customer1.png";
	public static final int CUSTOMER_TEXTURE_X = 0;
	public static final int CUSTOMER_TEXTURE_Y = 0;
	public static final int CUSTOMER_TEXTURE_WIDTH = 7;
	public static final int CUSTOMER_TEXTURE_HEIGHT = 16;
	public static final int MAX_SHOPPING_LIST_SIZE = 5;

	public static final int MAX_CUSTOMERS = 50;
	public static final int CUSTOMER_SPAWN_INTERVAL_TICKS = 240;
	public static final float PERSON_SPEED = 1.0f; // pixels per tick
}
