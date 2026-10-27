package skelemarket.simulation.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import skelemarket.simulation.Config;
public class MapLoaderTest {
	private static final String MAP = """
			{
			  "width": 1280,
			  "height": 738,
			  "background": [0, 0, 0, 255],
			  "layers": [{"name": "Floor"}, {"name": "Objects"}],
			  "categories": ["FRUIT"],
			  "items": [{
				"name": "APPLE",
				"category": "FRUIT",
				"weight": 1,
				"image": "/textures/enhanced/walls_floor.png",
				"source": [0, 0, 32, 32]
			  }],
			  "tiles": [
				{"id": 1, "type": "SHELF", "category": "FRUIT", "magazine": false, "image": "/textures/enhanced/walls_floor.png", "layer": 1, "source": [0, 0, 32, 32], "size": [32, 32], "location": [32, 0], "inventory": [{"item": "APPLE", "capacity": 4}]},
				{"id": 2, "type": "ENTRANCE", "category": "", "image": "/textures/enhanced/walls_floor.png", "layer": 1, "source": [0, 0, 32, 32], "size": [32, 32], "location": [64, 0], "inventory": []},
				{"id": 3, "type": "EXIT", "category": "", "image": "/textures/enhanced/walls_floor.png", "layer": 1, "source": [0, 0, 32, 32], "size": [32, 32], "location": [96, 0], "inventory": []},
				{"id": 4, "type": "SELF_CHECKOUT", "category": "", "image": "/textures/enhanced/walls_floor.png", "layer": 1, "source": [0, 0, 32, 32], "size": [32, 32], "location": [128, 0], "inventory": []}
			  ],
			  "paths": {
				"nodes": [
				  {"id": 1, "location": [64, 0], "targets": ["t2"]},
				  {"id": 2, "location": [32, 0], "targets": ["t1"]},
				  {"id": 3, "location": [96, 0], "targets": ["t3"]}
				],
				"edges": [["n1", "n2"], ["n2", "n3"]]
			  }
			}
			""";

	private Map load(String json) throws Exception {
		MapLoader loader = new MapLoader();
		ObjectMapper mapper = new ObjectMapper();
		return loader.parse(mapper.readTree(json));
	}

	@Test
	void nodesConnectToTypedTiles() throws Exception {
		Map map = load(MAP);
		PathNode entranceNode = map.getNode(1).orElseThrow();
		PathNode destinationNode = map.getNode(3).orElseThrow();

		assertInstanceOf(Entrance.class, map.getTile(2).orElseThrow());
		assertInstanceOf(Exit.class, map.getTile(3).orElseThrow());
		assertInstanceOf(Checkout.class, map.getTile(4).orElseThrow());
		assertEquals(map.getTile(2).orElseThrow(), entranceNode.getTarget());
		assertEquals(map.getTile(3).orElseThrow(), destinationNode.getTarget());
	}

	@Test
	void nodeConnectionsAreBidirectional() throws Exception {
		Map map = load(MAP);
		PathNode first = map.getNode(1).orElseThrow();
		PathNode second = map.getNode(2).orElseThrow();

		assertTrue(first.getNeighbours().contains(second));
		assertTrue(second.getNeighbours().contains(first));
	}

	@Test
	void nodeCanBeADeadEnd() throws Exception {
		String deadEndMap = MAP.replace("[[\"n1\", \"n2\"], [\"n2\", \"n3\"]]", "[]");
		Map map = load(deadEndMap);
		PathNode node = map.getNode(1).orElseThrow();

		assertEquals(0, node.getNeighbours().size());
		assertEquals(map.getTile(2).orElseThrow(), node.getTarget());
	}

	@Test
	void productTextureAndSourceAreRequired() {
		String invalidMap = MAP.replaceFirst(
				"\"image\": \"/textures/enhanced/walls_floor\\.png\",\\s*\"source\": \\[0, 0, 32, 32\\]",
				"\"image\": null,\n    \"source\": null");

		assertThrows(MapLoadException.class, () -> load(invalidMap));
	}

	@Test
	void destinationNodesMatchTheRequestedShelfType() throws Exception {
		Map normalShelfMap = load(MAP);
		Map magazineShelfMap = load(MAP.replace("\"magazine\": false", "\"magazine\": true"));

		assertEquals(normalShelfMap.getNode(2).orElseThrow(),
				normalShelfMap.getDestinationNodeForProduct("APPLE", false).orElseThrow());
		assertTrue(normalShelfMap.getDestinationNodeForProduct("APPLE", true).isEmpty());
		assertEquals(magazineShelfMap.getNode(2).orElseThrow(),
				magazineShelfMap.getDestinationNodeForProduct("APPLE", true).orElseThrow());
		assertTrue(magazineShelfMap.getDestinationNodeForProduct("APPLE", false).isEmpty());
	}

	@Test
	void mannedCheckoutsTakeLongerThanSelfCheckouts() throws Exception {
		Checkout selfCheckout = (Checkout) load(MAP).getTile(4).orElseThrow();
		Checkout mannedCheckout = (Checkout) load(MAP.replace("SELF_CHECKOUT", "MANNED_CHECKOUT")).getTile(4)
				.orElseThrow();

		assertEquals(Config.SELF_CHECKOUT_PAY_TICKS * 3, selfCheckout.getPaymentTicks(3));
		assertEquals(Config.MANNED_CHECKOUT_PAY_TICKS * 3, mannedCheckout.getPaymentTicks(3));
	}
}
