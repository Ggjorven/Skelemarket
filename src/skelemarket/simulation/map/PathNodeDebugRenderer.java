package skelemarket.simulation.map;

import java.util.Collection;

import skelemarket.core.Colour;
import skelemarket.core.Renderer;
import skelemarket.core.Vec2i;

// TODO: Remove
public class PathNodeDebugRenderer {
	///////////////////////////////////////////////////////////
	// Variables
	///////////////////////////////////////////////////////////
	private static final Colour LINK_COLOUR = new Colour(255, 0, 255, 192);
	private static final Colour CONNECTION_COLOUR = new Colour(255, 255, 0, 192);
	private static final Colour NODE_COLOUR = new Colour(0, 255, 255, 255);
	private static final Vec2i NODE_SIZE = new Vec2i(8, 8);

	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	private PathNodeDebugRenderer() {
	}

	public static void render(Renderer rendererRef, Collection<PathNode> nodes) {
		for (PathNode node : nodes) {
			for (PathNode neighbour : node.getNeighbours())
				if (node.getID() < neighbour.getID())
					rendererRef.drawLine(CONNECTION_COLOUR, node.getLocation(), neighbour.getLocation());

			if (node.hasTarget())
				rendererRef.drawLine(LINK_COLOUR, node.getLocation(), getCentre(node.getTarget()));
		}

		for (PathNode node : nodes)
			rendererRef.drawQuad(NODE_COLOUR, getTopLeft(node), NODE_SIZE);
	}

	private static Vec2i getCentre(Tile tile) {
		return new Vec2i(tile.getPosition().getX() + tile.getSize().getX() / 2,
				tile.getPosition().getY() + tile.getSize().getY() / 2);
	}

	private static Vec2i getTopLeft(PathNode node) {
		return new Vec2i(node.getLocation().getX() - NODE_SIZE.getX() / 2,
				node.getLocation().getY() - NODE_SIZE.getY() / 2);
	}
}
