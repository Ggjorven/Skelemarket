package skelemarket.simulation.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import skelemarket.core.Logger;
import skelemarket.core.Texture;
import skelemarket.core.UV;
import skelemarket.core.Vec2i;

///////////////////////////////////////////////////////////
// MapLoader
///////////////////////////////////////////////////////////
public class MapLoader {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private ObjectMapper mJSONMapper = null;

	private JsonNode mRootNode = null;

	private java.util.Map<String, Texture> mTextures = new HashMap<>();

	private List<MapLayer> mLayers = new ArrayList<>();
	private java.util.Map<String, List<Product>> mProductsPerCategory = new HashMap<>();

	///////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////
	public MapLoader() {
		mJSONMapper = new ObjectMapper();
	}

	public Map loadFromPath(String path) throws MapLoadException {
		// Load file
		try (InputStream stream = getClass().getResourceAsStream(path)) {
			if (stream == null) {
				throw new MapLoadException("Resource not found: %s.", path);
			}

			mRootNode = mJSONMapper.readTree(stream);
		} catch (IOException ex) {
			throw new MapLoadException("Failed to load/parse JSON, error: %s.", ex.getMessage());
		}

		// Parse file
		parseSpecifications();
		parseLayers();
		parseCategories();
		parseItems();
		parseTiles();
		parsePaths();

		return null;
	}

	///////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////
	private void parseSpecifications() throws MapLoadException {
		int width = mRootNode.get("width").asInt();
		int height = mRootNode.get("height").asInt();

		Logger.trace("Loading map with width and height: %d, %d.", width, height);

		if (width != skelemarket.core.Config.WIDTH || height != skelemarket.core.Config.HEIGHT) {
			throw new MapLoadException(
					"Map's width (%d) and height (%d) don't line up with the window's width (%d) and height (%d).",
					width, height, skelemarket.core.Config.WIDTH, skelemarket.core.Config.HEIGHT);
		}
	}

	private void parseLayers() throws MapLoadException {
		for (JsonNode layer : mRootNode.get("layers")) {
			String name = layer.get("name").asText();
			// boolean visibility = layer.get("visibility").asBoolean();

			Logger.trace("Found layer: %s.", name);

			// We don't need the name or visibility
			mLayers.add(new MapLayer());
		}
	}

	private void parseCategories() throws MapLoadException {
		for (JsonNode category : mRootNode.get("categories")) {
			mProductsPerCategory.put(category.asText(), new ArrayList<>());
			Logger.trace("Loaded category: %s.", category);
		}
	}

	private void parseItems() throws MapLoadException {
		for (JsonNode item : mRootNode.get("items")) {
			String name = item.get("name").asText();
			int weight = item.get("weight").asInt();

			// Category
			String category = item.get("category").asText();

			if (!mProductsPerCategory.containsKey(category)) {
				throw new MapLoadException(
						"Found item with category: %s, but this was not previously seen during category loading.",
						category);
			}

			// Image & UV
			Texture textureRef = parseTextureFromNode(item, "image");
			UV uv = parseUVFromNode(item, "source");

			// Create product
			List<Product> products = mProductsPerCategory.get(category);
			products.add(new Product(name, weight, category, textureRef, uv));

			Logger.trace("Loaded product with name = %s, weight = %d, category = %s", name, weight, category);
		}
	}

	private void parseTiles() throws MapLoadException {
		for (JsonNode tile : mRootNode.get("tiles")) {
			int id = tile.get("id").asInt();
			String type = tile.get("type").asText();
			Texture textureRef = parseTextureFromNode(tile, "image");
			int layer = tile.get("layer").asInt();
			UV uv = parseUVFromNode(tile, "source");
		}
	}

	private void parsePaths() throws MapLoadException {
	}

	///////////////////////////////////////////////////////////
	// Helper methods
	///////////////////////////////////////////////////////////
	private Texture parseTextureFromNode(JsonNode node, String textureName) {
		String imagePath = node.get(textureName).asText();

		if (!mTextures.containsKey(imagePath)) {
			mTextures.put(imagePath, new Texture(imagePath));
		}

		return mTextures.get(imagePath);
	}

	private UV parseUVFromNode(JsonNode node, String uvName) throws MapLoadException {
		List<Integer> rawSourceUV = new ArrayList<>(4);
		for (JsonNode sourceValue : node.get(uvName)) {
			rawSourceUV.add(sourceValue.asInt());
		}

		if (rawSourceUV.size() != 4) {
			throw new MapLoadException("UV array is longer than 4 ints (%d).", rawSourceUV.size());
		}

		return new UV(rawSourceUV.get(0), rawSourceUV.get(1), rawSourceUV.get(2), rawSourceUV.get(3));
	}

	private Vec2i parseVec2iFromNode(JsonNode node, String vecName) throws MapLoadException {
		List<Integer> rawVec2 = new ArrayList<>(4);
		for (JsonNode value : node.get(vecName)) {
			rawVec2.add(value.asInt());
		}

		if (rawVec2.size() != 2) {
			throw new MapLoadException("Vec2 array is longer than 2 ints (%d).", rawVec2.size());
		}

		return new Vec2i(rawVec2.get(0), rawVec2.get(1));
	}
}

///////////////////////////////////////////////////////////
// MapLoadException
///////////////////////////////////////////////////////////
class MapLoadException extends Exception {
	public MapLoadException(String errorMessage) {
		super(errorMessage);
	}

	public MapLoadException(String format, Object... args) {
		super(String.format(format, args));
	}
}
