package skelemarket.simulation.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import skelemarket.core.Colour;
import skelemarket.core.Logger;
import skelemarket.core.Renderer;
import skelemarket.core.Vec2i;

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

		for (MapLayer layer : mLayers) {
			for (Tile tile : layer.getTiles()) {
				mTiles.put(tile.getID(), tile);

				if (tile instanceof Shelf shelf)
					mCategoryToShelves.computeIfAbsent(shelf.getCategory(), ignored -> new ArrayList<>()).add(shelf);
				if (tile instanceof Entrance entrance)
					mEntrance = entrance;
			}
		}

		for (PathNode node : nodes.values()) {
			if (node.hasTarget()) {
				mTileToNode.put(node.getTarget().getID(), node);
			}
		}
	}

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mBackgroundColour, new Vec2i(0, 0),
				new Vec2i(skelemarket.core.Config.WIDTH, skelemarket.core.Config.HEIGHT));

		for (MapLayer layerRef : mLayers)
			layerRef.render(rendererRef);

		// DEBUG: Remove this line to hide path nodes and their target links.
		PathNodeDebugRenderer.render(rendererRef, mNodes.values());
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

	public List<Shelf> getShelvesForCategory(String category) {
		return List.copyOf(mCategoryToShelves.getOrDefault(category, List.of()));
	}

	public Optional<PathNode> getNodeForTile(int tileID) {
		return Optional.ofNullable(mTileToNode.get(tileID));
	}

	public Optional<PathNode> getNodeForShelf(Shelf shelf) {
		return Optional.ofNullable(mTileToNode.get(shelf.getID()));
	}

	public Optional<PathNode> getDestinationNodeForProduct(String productName) {
		Optional<String> category = getCategoryForProduct(productName);

		if (category.isPresent()) {
			return getDestinationNodeForCategory(category.get());
		}

		Logger.warn("Tried retrieving a node for product: %s, but there is no category present.", productName);
		return Optional.empty();
	}

	// TODO: Based on current location
	public Optional<PathNode> getDestinationNodeForCategory(String category) {
		List<Shelf> shelves = getShelvesForCategory(category);

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
		return mEntrance;
	}

	public void render(Renderer rendererRef) {
		rendererRef.drawQuad(mBackgroundColour, new Vec2i(0, 0),
				new Vec2i(skelemarket.core.Config.WIDTH, skelemarket.core.Config.HEIGHT));
		for (MapLayer layerRef : mLayers)
			layerRef.render(rendererRef);
	}

	public void update() {
		for (MapLayer layerRef : mLayers) {
			layerRef.update();
		}
	}
}
