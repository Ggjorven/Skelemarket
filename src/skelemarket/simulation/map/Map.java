package skelemarket.simulation.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import skelemarket.core.Colour;
import skelemarket.core.Logger;
import skelemarket.core.Renderer;
import skelemarket.core.Vec2i;
import skelemarket.simulation.people.PeopleRegister;

public class Map {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private final Colour mBackgroundColour;
	private final List<MapLayer> mLayers;

	private final java.util.Map<Integer, Tile> mTiles = new HashMap<>();
	private final java.util.Map<Integer, PathNode> mNodes;

	private final java.util.Map<Integer, PathNode> mTileToNode = new HashMap<>();
	private final java.util.Map<String, java.util.Map<String, ProductSpecification>> mCategoryToProducts;
	private final java.util.Map<String, List<Shelf>> mCategoryToShelves = new HashMap<>();

	private final Pathfinder mPathfinder = new Pathfinder();

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Map(List<MapLayer> layers, Colour backgroundColour,
			java.util.Map<String, java.util.Map<String, ProductSpecification>> categoryToProducts,
			java.util.Map<Integer, PathNode> nodes) {
		mLayers = layers;
		mBackgroundColour = backgroundColour;

		mCategoryToProducts = categoryToProducts;
		mNodes = nodes;

		for (String category : mCategoryToProducts.keySet()) {
			mCategoryToShelves.put(category, new ArrayList<Shelf>());
		}

		for (MapLayer layer : mLayers) {
			for (Tile tile : layer.getTiles()) {
				mTiles.put(tile.getID(), tile);

				if (tile instanceof Shelf shelf) {
					mCategoryToShelves.get(shelf.getCategory()).add(shelf);
				}
			}
		}

		for (PathNode node : nodes.values()) {
			if (node.hasTarget()) {
				mTileToNode.put(node.getTarget().getID(), node);
			}
		}
	}

	public void update(PeopleRegister peopleRegisterRef) {
		for (MapLayer layerRef : mLayers) {
			layerRef.update(this, peopleRegisterRef);
		}
	}

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mBackgroundColour, new Vec2i(0, 0),
				new Vec2i(skelemarket.core.Config.WIDTH, skelemarket.core.Config.HEIGHT));

		for (MapLayer layerRef : mLayers)
			layerRef.render(rendererRef);

		// TODO: Remove this line to hide path nodes and their target links
		// PathNodeDebugRenderer.render(rendererRef, mNodes.values());
	}

	public Optional<ProductSpecification> getProduct(String productName) {
		for (java.util.Map.Entry<String, java.util.Map<String, ProductSpecification>> categoryProduct : mCategoryToProducts
				.entrySet()) {
			if (categoryProduct.getValue().containsKey(productName))
				return Optional.of(categoryProduct.getValue().get(productName));
		}

		return Optional.empty();
	}

	public Optional<String> getCategoryForProduct(String productName) {
		return getProduct(productName).map(ProductSpecification::getCategory);
	}

	public List<Shelf> getShelvesForCategory(String category, boolean isMagazine) {
		return mCategoryToShelves.getOrDefault(category, new ArrayList<>()).stream()
				.filter(shelf -> shelf.isMagazine() == isMagazine)
				.toList();
	}

	public List<Shelf> getLowestStockShelves(Set<Integer> excludedShelfIDs) {
		List<Shelf> lowestStockShelves = new ArrayList<>();
		float lowestStockPercentage = Float.POSITIVE_INFINITY;

		for (List<Shelf> shelves : mCategoryToShelves.values()) {
			for (Shelf shelf : shelves) {
				if (shelf.isMagazine() || !shelf.hasMissingProducts() || excludedShelfIDs.contains(shelf.getID()))
					continue;

				float stockPercentage = shelf.getLowestStockPercentage();
				if (stockPercentage < lowestStockPercentage) {
					lowestStockPercentage = stockPercentage;
					lowestStockShelves.clear();
				}

				if (stockPercentage == lowestStockPercentage)
					lowestStockShelves.add(shelf);
			}
		}

		return lowestStockShelves;
	}

	public List<String> getProductNames() {
		List<String> names = new ArrayList<>();

		for (java.util.Map<String, ProductSpecification> products : mCategoryToProducts.values()) {
			names.addAll(products.keySet());
		}

		return names;
	}

	public Optional<PathNode> getNodeForTile(int tileID) {
		return Optional.ofNullable(mTileToNode.get(tileID));
	}

	public Optional<PathNode> getNodeForShelf(Shelf shelf) {
		return Optional.ofNullable(mTileToNode.get(shelf.getID()));
	}

	public Optional<PathNode> getDestinationNodeForProduct(String productName, boolean isMagazine) {
		Optional<String> category = getCategoryForProduct(productName);

		if (category.isPresent()) {
			List<Shelf> shelves = getShelvesForCategory(category.get(), isMagazine);

			for (Shelf shelf : shelves) {
				if (shelf.getProductCount(productName) == 0)
					continue;

				Optional<PathNode> node = getNodeForShelf(shelf);
				if (node.isPresent())
					return node;
			}

			Logger.warn("No node/shelf has %s's", productName);
			return Optional.empty();
		}

		Logger.warn("Tried retrieving a node for product: %s, but there is no category present.", productName);
		return Optional.empty();
	}

	// TODO: Based on current location
	public Optional<PathNode> getDestinationNodeForCategory(String category, boolean isMagazine) {
		List<Shelf> shelves = getShelvesForCategory(category, isMagazine);

		for (Shelf shelf : shelves) {
			Optional<PathNode> node = getNodeForShelf(shelf);

			if (node.isPresent())
				return node;
		}

		return Optional.empty();
	}

	public Optional<PathNode> getNode(int id) {
		return Optional.ofNullable(mNodes.get(id));
	}

	public Optional<PathNode> getNearestNode(float x, float y) {
		PathNode nearestNode = null;
		double nearestDistance = Double.POSITIVE_INFINITY;

		for (PathNode node : mNodes.values()) {
			double deltaX = node.getLocation().getX() - x;
			double deltaY = node.getLocation().getY() - y;

			// NOTE: Safer way of: Math.sqrt(deltaX * deltaX + deltaY * deltaY)
			double distance = Math.hypot(deltaX, deltaY);

			if (distance < nearestDistance) {
				nearestNode = node;
				nearestDistance = distance;
			}
		}

		return Optional.ofNullable(nearestNode);
	}

	public Optional<List<PathNode>> findPath(PathNode start, PathNode target) {
		List<PathNode> path = mPathfinder.findPath(start, target);
		return path.isEmpty() ? Optional.empty() : Optional.of(path);
	}

	public Optional<List<PathNode>> findPathToProduct(PathNode start, String productName, boolean isMagazine) {
		Optional<PathNode> destination = getDestinationNodeForProduct(productName, isMagazine);

		if (destination.isPresent()) {
			return findPath(start, destination.get());
		}

		return Optional.empty();
	}

	public Optional<Tile> getTile(int id) {
		return Optional.ofNullable(mTiles.get(id));
	}

	public java.util.Map<Integer, PathNode> getNodes() {
		return mNodes;
	}

	public List<MapLayer> getLayers() {
		return mLayers;
	}

	public Entrance getEntrance() {
		for (MapLayer layer : mLayers) {
			if (layer.getEntrance() != null)
				return layer.getEntrance();
		}

		Logger.error("No MapLayer had an Entrance.");
		return null;
	}

	public List<Checkout> getCheckouts() {
		List<Checkout> checkouts = new ArrayList<>();

		for (MapLayer layer : mLayers) {
			checkouts.addAll(layer.getCheckouts());
		}

		return checkouts;
	}

	public Exit getExit() {
		for (MapLayer layer : mLayers) {
			if (layer.getExit() != null)
				return layer.getExit();
		}

		Logger.error("No MapLayer had an Exit.");
		return null;
	}
}
