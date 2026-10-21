package skelemarket.simulation.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import skelemarket.core.Colour;
import skelemarket.core.Renderer;
import skelemarket.core.Vec2i;

/** Read-only lookup facade over the loaded store map and navigation graph. */
public class Map {
	private final java.util.Map<String, java.util.Map<String, ProductSpecification>> mCategoryToProducts;
	private final List<MapLayer> mLayers;
	private final Colour mBackgroundColour;
	private Entrance mEntrance = null;
	private final java.util.Map<Integer, PathNode> mNodes;
	private final java.util.Map<Integer, Tile> mTiles = new HashMap<>();
	private final java.util.Map<Integer, List<PathNode>> mTileToNodes = new HashMap<>();
	private final java.util.Map<String, List<Shelf>> mCategoryToShelves = new HashMap<>();

	public Map(List<MapLayer> layers, Colour backgroundColour,
			java.util.Map<String, java.util.Map<String, ProductSpecification>> categoryToProducts,
			java.util.Map<Integer, PathNode> nodes) {
		mLayers = List.copyOf(layers);
		mBackgroundColour = backgroundColour;
		mCategoryToProducts = copyProducts(categoryToProducts);
		mNodes = Collections.unmodifiableMap(new HashMap<>(nodes));
		for (MapLayer layer : mLayers)
			for (Tile tile : layer.getTiles()) {
				mTiles.put(tile.getID(), tile);
				if (tile instanceof Shelf shelf)
					mCategoryToShelves.computeIfAbsent(shelf.getCategory(), ignored -> new ArrayList<>()).add(shelf);
				if (tile instanceof Entrance entrance)
					mEntrance = entrance;
			}
		for (PathNode node : nodes.values())
			for (Tile target : node.getTargets())
				mTileToNodes.computeIfAbsent(target.getID(), ignored -> new ArrayList<>()).add(node);
	}

	private java.util.Map<String, java.util.Map<String, ProductSpecification>> copyProducts(
			java.util.Map<String, java.util.Map<String, ProductSpecification>> source) {
		java.util.Map<String, java.util.Map<String, ProductSpecification>> copy = new HashMap<>();
		for (var entry : source.entrySet())
			copy.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
		return Collections.unmodifiableMap(copy);
	}

	public Optional<ProductSpecification> getProduct(String productName) {
		return mCategoryToProducts.values().stream().map(products -> products.get(productName))
				.filter(java.util.Objects::nonNull).findFirst();
	}

	public Optional<String> getCategoryForProduct(String productName) {
		return getProduct(productName).map(ProductSpecification::getCategory);
	}

	public List<Shelf> getShelvesForCategory(String category) {
		return List.copyOf(mCategoryToShelves.getOrDefault(category, List.of()));
	}

	public List<PathNode> getNodesForTile(int tileID) {
		return List.copyOf(mTileToNodes.getOrDefault(tileID, List.of()));
	}

	public Optional<PathNode> getNodeForShelf(Shelf shelf) {
		return getNodesForTile(shelf.getID()).stream().min(Comparator.comparingInt(PathNode::getID));
	}

	/** Resolves product -> category -> shelf -> authored destination node. */
	public Optional<PathNode> getDestinationNodeForProduct(String productName) {
		return getCategoryForProduct(productName).flatMap(this::getDestinationNodeForCategory);
	}

	/** Chooses the first authored shelf destination in stable tile-id order. */
	public Optional<PathNode> getDestinationNodeForCategory(String category) {
		return getShelvesForCategory(category).stream().sorted(Comparator.comparingInt(Tile::getID))
				.map(this::getNodeForShelf).flatMap(Optional::stream).findFirst();
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
