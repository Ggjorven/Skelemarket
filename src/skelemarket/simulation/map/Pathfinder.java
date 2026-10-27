package skelemarket.simulation.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Pathfinder {
	///////////////////////////////////////////////////////////
	// Methods
	///////////////////////////////////////////////////////////
	public Pathfinder() {
	}

	// NOTE: Returns a list from start to finish, empty if not reachable
	public List<PathNode> findPath(PathNode start, PathNode target) {
		if (start == null || target == null)
			return new ArrayList<>();

		List<PathNode> nodesToVisit = new ArrayList<>();
		Set<PathNode> visitedNodes = new HashSet<>();
		Map<PathNode, Double> costFromStart = new HashMap<>();
		Map<PathNode, PathNode> previousNode = new HashMap<>();

		nodesToVisit.add(start);
		costFromStart.put(start, 0.0);

		while (!nodesToVisit.isEmpty()) {
			PathNode current = getMostPromisingNode(nodesToVisit, costFromStart, target);
			nodesToVisit.remove(current);

			if (current == target)
				return buildPath(previousNode, target);

			visitedNodes.add(current);

			double currentCost = costFromStart.get(current);
			for (PathNode neighbour : current.getNeighbours()) {
				if (visitedNodes.contains(neighbour))
					continue;

				double costThroughCurrent = currentCost + estimateDistance(current, neighbour);
				if (costThroughCurrent < costFromStart.getOrDefault(neighbour, Double.POSITIVE_INFINITY)) {
					costFromStart.put(neighbour, costThroughCurrent);
					previousNode.put(neighbour, current);

					if (!nodesToVisit.contains(neighbour))
						nodesToVisit.add(neighbour);
				}
			}
		}

		return new ArrayList<>();
	}

	private PathNode getMostPromisingNode(List<PathNode> nodesToVisit, Map<PathNode, Double> costFromStart,
			PathNode target) {
		PathNode bestNode = nodesToVisit.get(0);
		double bestEstimatedCost = getEstimatedTotalCost(bestNode, costFromStart, target);

		for (PathNode node : nodesToVisit) {
			double estimatedCost = getEstimatedTotalCost(node, costFromStart, target);

			if (estimatedCost < bestEstimatedCost) {
				bestNode = node;
				bestEstimatedCost = estimatedCost;
			}
		}

		return bestNode;
	}

	private double getEstimatedTotalCost(PathNode node, Map<PathNode, Double> costFromStart,
			PathNode target) {
		return costFromStart.get(node) + estimateDistance(node, target);
	}

	private List<PathNode> buildPath(Map<PathNode, PathNode> cameFrom, PathNode target) {
		List<PathNode> path = new ArrayList<>();

		for (PathNode node = target; node != null; node = cameFrom.get(node))
			path.add(node);

		Collections.reverse(path);
		return path;
	}

	private double estimateDistance(PathNode first, PathNode second) {
		double deltaX = first.getLocation().getX() - second.getLocation().getX();
		double deltaY = first.getLocation().getY() - second.getLocation().getY();

		// NOTE: Safer way of: Math.sqrt(deltaX * deltaX + deltaY * deltaY)
		return Math.hypot(deltaX, deltaY);
	}

}
