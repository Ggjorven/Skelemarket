package skelemarket.simulation.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import skelemarket.core.Colour;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;
import skelemarket.core.Logger;

public class MapLoader {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private ObjectMapper mJSONMapper = new ObjectMapper();

	private java.util.Map<String, Texture> mTextures = new HashMap<>();

	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	public Map loadFromPath(String path) throws MapLoadException {
		try (InputStream stream = getClass().getResourceAsStream(path)) {
			if (stream == null)
				throw new MapLoadException("Resource not found: %s.", path);

			return parse(mJSONMapper.readTree(stream));
		} catch (IOException ex) {
			throw new MapLoadException("Failed to load/parse JSON, error: %s.", ex.getMessage());
		}
	}

	public Map parse(JsonNode root) throws MapLoadException {
		validateCanvas(root);

		List<MapLayer> layers = parseLayers(root, "layers");

		java.util.Map<String, java.util.Map<String, ProductSpecification>> products = parseProducts(root);

		java.util.Map<Integer, Tile> tiles = new HashMap<>();
		parseTiles(root, "tiles", layers, products, tiles);

		java.util.Map<Integer, PathNode> nodes = parseNodes(root.path("paths"), tiles);
		parseEdges(root.path("paths"), nodes);

		return new Map(layers, requireColour(root, "background"), products, nodes);
	}

	private void validateCanvas(JsonNode root) throws MapLoadException {
		int width = requireInt(root, "width"), height = requireInt(root, "height");
		if (width != skelemarket.core.Config.WIDTH || height != skelemarket.core.Config.HEIGHT)
			throw new MapLoadException("Map dimensions (%d, %d) must be (%d, %d).", width, height,
					skelemarket.core.Config.WIDTH, skelemarket.core.Config.HEIGHT);
	}

	private List<MapLayer> parseLayers(JsonNode root, String name) throws MapLoadException {
		List<MapLayer> layers = new ArrayList<>();

		for (JsonNode layer : root.get("layers")) {
			String layerName = layer.get("name").asText();
			// boolean visibility = layer.get("visibility").asBoolean();

			Logger.trace("Found layer: %s.", layerName);

			// We don't need the name or visibility
			layers.add(new MapLayer());
		}

		if (layers.isEmpty())
			throw new MapLoadException("A map must have at least one layer.");

		return layers;
	}

	private java.util.Map<String, java.util.Map<String, ProductSpecification>> parseProducts(JsonNode root)
			throws MapLoadException {
		java.util.Map<String, java.util.Map<String, ProductSpecification>> result = new HashMap<>();
		Set<String> productNames = new HashSet<>();

		for (JsonNode category : requireArray(root, "categories")) {
			String name = category.asText();

			if (name.isBlank())
				throw new MapLoadException("Categories must have unique non-empty names.");

			result.putIfAbsent(name, new HashMap<>());
		}

		for (JsonNode item : requireArray(root, "items")) {
			String name = requireText(item, "name");
			String category = requireText(item, "category");
			int weight = requireInt(item, "weight");

			if (weight <= 0)
				throw new MapLoadException("Weight must be a positive non 0 integer.");
			if (!result.containsKey(category))
				throw new MapLoadException("Product's category does not exist.");
			if (!productNames.add(name))
				throw new MapLoadException("Product names must be unique: \"%s\" found multiple times.", name);

			Texture texture = requireTexture(item, "image");
			UV uv = requireUV(item, "source");
			result.get(category).put(name, new ProductSpecification(name, weight, category, texture, uv));
		}

		return result;
	}

	private void parseTiles(JsonNode rootNode, String tilesName, JsonNode rawTiles, List<MapLayer> layers,
			java.util.Map<String, java.util.Map<String, ProductSpecification>> products,
			java.util.Map<Integer, Tile> tiles) throws MapLoadException {
		for (JsonNode raw : rawTiles) {
			int id = requireInt(raw, "id");
			int layerIndex = requireInt(raw, "layer");

			if (tiles.containsKey(id))
				throw new MapLoadException("Duplicate tile id: %d.", id);
			if (layerIndex < 0 || layerIndex >= layers.size())
				throw new MapLoadException("Invalid layer: %d.", layerIndex);

			String type = requireText(raw, "type");
			Texture texture = requireTexture(raw, "image");
			UV uv = requireUV(raw, "source");
			Vec2i size = requireVec2i(raw, "size"), location = requireVec2i(raw, "location");
			MapLayer layer = layers.get(layerIndex);

			Tile tile;
			switch (type) {
				case "SHELF": {
					String category = requireText(raw, "category");

					if (category.isBlank() || !products.containsKey(category))
						throw new MapLoadException("Shelf %d has unknown category %s.", id, category);

					java.util.Map<String, ProductSpecification> categoryProducts = products.get(category);

					tile = new Shelf(id, location, size, texture, uv, category, parseInventory(raw, categoryProducts),
							products);
					layer.addShelf((Shelf) tile);
				}
				case "ENTRANCE": {
					tile = new Entrance(id, location, size, texture, uv);
					layer.addEntrance((Entrance) tile);
				}
				case "EXIT": {
					tile = new Exit(id, location, size, texture, uv);
					layer.addExit((Exit) tile);
				}
				case "MANNED_CHECKOUT": {
					tile = new Checkout(id, location, size, texture, uv, false);
					layer.addCheckout((Checkout) tile);
				}
				case "SELF_CHECKOUT": {
					tile = new Checkout(id, location, size, texture, uv, true);
					layer.addCheckout((Checkout) tile);
				}

				default: {
					tile = new Tile(id, location, size, texture, uv);
					layer.addTile(tile);
				}
			}

			tiles.put(id, tile);
		}
	}

	private java.util.Map<String, Integer> parseInventory(JsonNode raw,
			java.util.Map<String, ProductSpecification> products)
			throws MapLoadException {
		java.util.Map<String, Integer> result = new HashMap<>();

		for (JsonNode entry : requireArray(raw, "inventory")) {
			String product = requireText(entry, "item");
			int capacity = requireInt(entry, "capacity");

			if (!products.containsKey(product))
				throw new MapLoadException("Inventory contains a product that was not previously created.");
			if (capacity <= 0)
				throw new MapLoadException("Capacity of inventory must be positive and non-zero.");

			result.put(product, capacity);
		}

		return result;
	}

	private java.util.Map<Integer, PathNode> parseNodes(JsonNode paths, java.util.Map<Integer, Tile> tiles)
			throws MapLoadException {
		java.util.Map<Integer, PathNode> nodes = new HashMap<>();

		// for (JsonNode raw : requireArray(paths, "nodes")) {
		// int id = parseReferenceId(raw.get("id"), "node");
		//
		// if (nodes.containsKey(id))
		// throw new MapLoadException("Duplicate node id: %d.", id);
		//
		// PathNode node = new PathNode(id, requireVec2i(raw, "location"));
		// for (JsonNode target : requireArray(raw, "targets")) {
		// Tile tile = tiles.get(parseReferenceId(target, "tile"));
		//
		// if (tile == null)
		// throw new MapLoadException("Node %d targets an unknown tile.", id);
		//
		// node.addTarget(tile);
		// }
		//
		// nodes.put(id, node);
		// }

		return nodes;
	}

	private void parseEdges(JsonNode paths, java.util.Map<Integer, PathNode> nodes) throws MapLoadException {
		// if (paths.isMissingNode() || paths.isNull())
		// return;
		// Set<String> seen = new HashSet<>();
		// for (JsonNode edge : requireArray(paths, "edges")) {
		// if (!edge.isArray() || edge.size() != 2)
		// throw new MapLoadException("Every path edge must contain two node ids.");
		// int a = parseReferenceId(edge.get(0), "node"), b =
		// parseReferenceId(edge.get(1), "node");
		// PathNode first = nodes.get(a), second = nodes.get(b);
		// if (first == null || second == null || a == b)
		// throw new MapLoadException("Invalid edge between %d and %d.", a, b);
		// String key = Math.min(a, b) + ":" + Math.max(a, b);
		// if (!seen.add(key))
		// throw new MapLoadException("Duplicate edge between %d and %d.", a, b);
		// first.addNeighbour(second);
		// second.addNeighbour(first);
		// }
	}

	private Texture requireTexture(JsonNode node, String name) throws MapLoadException {
		String imagePath = node.get(name).asText();

		if (!mTextures.containsKey(imagePath)) {
			mTextures.put(imagePath, new Texture(imagePath));
		}

		return mTextures.get(imagePath);
	}

	private UV requireUV(JsonNode node, String name) throws MapLoadException {
		List<Integer> rawSourceUV = new ArrayList<>(4);

		for (JsonNode sourceValue : node.get(name)) {
			rawSourceUV.add(sourceValue.asInt());
		}

		if (rawSourceUV.size() != 4) {
			throw new MapLoadException("UV array is longer than 4 ints (%d).", rawSourceUV.size());
		}

		return new UV(rawSourceUV.get(0), rawSourceUV.get(1), rawSourceUV.get(2), rawSourceUV.get(3));
	}

	private Vec2i requireVec2i(JsonNode node, String name) throws MapLoadException {
		List<Integer> rawVec2 = new ArrayList<>(2);

		for (JsonNode value : node.get(name)) {
			rawVec2.add(value.asInt());
		}

		if (rawVec2.size() != 2) {
			throw new MapLoadException("Vec2 array is longer than 2 ints (%d).", rawVec2.size());
		}

		return new Vec2i(rawVec2.get(0), rawVec2.get(1));
	}

	private Colour requireColour(JsonNode node, String name) throws MapLoadException {
		List<Integer> rawCol = new ArrayList<>(4);

		for (JsonNode value : node.get(name)) {
			rawCol.add(value.asInt());
		}

		if (rawCol.size() != 4) {
			throw new MapLoadException("Colour's array is longer than 4 ints (%d).", rawCol.size());
		}

		return new Colour(rawCol.get(0), rawCol.get(1), rawCol.get(2), rawCol.get(3));
	}

	private String requireText(JsonNode node, String name) throws MapLoadException {
		JsonNode value = node.get(name);

		if (value == null || value.isNull())
			throw new MapLoadException("%s must be text.", name);

		return value.asText();
	}

	private int requireInt(JsonNode node, String name) throws MapLoadException {
		JsonNode value = node.get(name);

		if (value == null || !value.canConvertToInt())
			throw new MapLoadException("%s must be an integer.", name);

		return value.asInt();
	}

	// private int parseReferenceId(JsonNode value, String type) throws
	// MapLoadException {
	// if (value == null || value.isNull())
	// throw new MapLoadException("Missing %s id.", type);
	// String raw = value.asText();
	// try {
	// return Integer.parseInt(raw.replaceFirst("^[A-Za-z]+", ""));
	// } catch (NumberFormatException ex) {
	// throw new MapLoadException("Invalid %s id: %s.", type, raw);
	// }
	// }
}

class MapLoadException extends Exception {
	MapLoadException(String message) {
		super(message);
	}

	MapLoadException(String format, Object... args) {
		super(String.format(format, args));
	}
}
