package skelemarket.simulation;

public class Config {
	public static final String MAP_FILE = "/map/map.json";

	public static final int BASKET_CAPACITY = 50;
	public static final int SHOPPING_CART_CAPACITY = 300;
	public static final int ROLL_CONTAINER_CAPACITY = 1000;
	public static final int STOCKER_COUNT = 3;
	public static final int STOCKER_MAX_TASKS_PER_TRIP = 3;

	public static final int CUSTOMER_WIDTH = 21;
	public static final int CUSTOMER_HEIGHT = 48;
	public static final String CUSTOMER_TEXTURE = "/textures/carbonova-supermarket/customer1.png";
	public static final int CUSTOMER_TEXTURE_X = 0;
	public static final int CUSTOMER_TEXTURE_Y = 0;
	public static final int CUSTOMER_TEXTURE_WIDTH = 7;
	public static final int CUSTOMER_TEXTURE_HEIGHT = 16;

	public static final int STOCKER_WIDTH = 21;
	public static final int STOCKER_HEIGHT = 48;
	public static final String STOCKER_TEXTURE = "/textures/carbonova-supermarket/employee.png";
	public static final int STOCKER_TEXTURE_X = 0;
	public static final int STOCKER_TEXTURE_Y = 0;
	public static final int STOCKER_TEXTURE_WIDTH = 7;
	public static final int STOCKER_TEXTURE_HEIGHT = 16;

	public static final int TAKE_PRODUCT_TICKS = 60;
	public static final int RESTOCK_PRODUCT_TICKS = 180;
	public static final int SELF_CHECKOUT_PAY_TICKS = 180;
	public static final int MANNED_CHECKOUT_PAY_TICKS = 210;
	public static final int MAX_SHOPPING_LIST_SIZE = 5;
	public static final int MAX_PRODUCTS_PER_TYPE = 5;
	public static final float SHELF_MINIMUM_STOCK_PERCENTAGE = 0.20f;
	public static final float SHELF_LOW_STOCK_PERCENTAGE = 0.30f;

	public static final int MAX_CUSTOMERS = 50;
	public static final int CUSTOMER_SPAWN_INTERVAL_TICKS = 240;
	public static final float PERSON_SPEED = 3.0f; // Pixels per tick
}
