package skelemarket.simulation.map;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import skelemarket.core.Texture;

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
	private java.util.Map<String, List<Product>> mItems = new HashMap<>();

	///////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////
	public MapLoader() {
		mJSONMapper = new ObjectMapper();
	}

	public Map loadFromPath(String path) throws MapLoadException {
		// Load contents
		String rawJson = readFile(path);

		// Parse contents
		try {
			mRootNode = mJSONMapper.readTree(rawJson);
		} catch (Exception ex) {
			throw new MapLoadException("Failed to parse JSON due to error: %s.", ex.toString());
		}

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
	private String readFile(String path) throws MapLoadException {
		File file = new File(path);

		String result = new String();
		try (Scanner reader = new Scanner(file)) {
			while (reader.hasNextLine()) {
				result.concat(reader.nextLine());
			}
		} catch (Exception ex) {
			throw new MapLoadException("Failed to read \"%s\"'s file contents. Exception: %s.", path, ex.toString());
		}

		return result;
	}

	private void parseSpecifications() throws MapLoadException {
		int width = mRootNode.get("width").asInt();
		int height = mRootNode.get("height").asInt();

		if (width != skelemarket.core.Config.WIDTH || height != skelemarket.core.Config.HEIGHT) {
			throw new MapLoadException(
					"Map's width (%i) and height (%i) don't line up with the window's width (%i) and height (%i).",
					width, height, skelemarket.core.Config.WIDTH, skelemarket.core.Config.HEIGHT);
		}
	}

	private void parseLayers() throws MapLoadException {
		for (JsonNode _layer : mRootNode.get("layers")) {
			// String name = layer.get("name").asText();
			// boolean visibility = layer.get("visibility").asBoolean();

			// We don't need the name or visibility
			mLayers.add(new MapLayer());
		}
	}

	private void parseCategories() throws MapLoadException {
		for (JsonNode category : mRootNode.get("categories")) {
			mItems.put(category.asText(), new ArrayList<>());
		}
	}

	private void parseItems() throws MapLoadException {
		for (JsonNode item : mRootNode.get("items")) {
			String name = item.get("name").asText();
		}
	}

	private void parseTiles() throws MapLoadException {
	}

	private void parsePaths() throws MapLoadException {
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
