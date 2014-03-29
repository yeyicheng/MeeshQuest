/*
 * @(#)Command.java        1.0 2007/01/23
 *
 * Copyright Ben Zoller (University of Maryland, College Park), 2007
 * All rights reserved. Permission is granted for use and modification in CMSC420 
 * at the University of Maryland.
 */
package canonicalsolution;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Float;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;




// Import for intersections
import canonicalsolution.PRQuadtree.CityAlreadyMappedException;
import canonicalsolution.PRQuadtree.CityOutOfBoundsException;
import canonicalsolution.PRQuadtree.InternalNode;
import canonicalsolution.PRQuadtree.LeafNode;
import canonicalsolution.PRQuadtree.EmptyNode;
import canonicalsolution.PRQuadtree.Node;
import cmsc420.drawing.CanvasPlus;
import cmsc420.drawing.Drawable2D;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;

/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 * 
 * @author Ben Zoller
 * @version 2.0, 23 Jan 2007
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/**
	 * stores created cities sorted by their names (used with listCities
	 * command)
	 */
	protected final TreeMap<String, City> citiesByName = new TreeMap<String, City>();

	/**
	 * stores created cities sorted by their locations (used with listCities
	 * command)
	 */
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(
			new CityLocationComparator());

	/** stores mapped cities in a spatial data structure */
	protected final PRQuadtree prQuadtree = new PRQuadtree();

	/** spatial width and height of the PR Quadtree */
	protected int spatialWidth, spatialHeight, g, pmOrder;

	/** graphical mapping tool (used with saveMap command) */
	protected CanvasPlus canvas;

	/**
	 * Set the graphical mapping tool to be used.
	 * 
	 * @param canvas
	 *            graphical mapping tool
	 */
	public void setCanvas(CanvasPlus canvas) {
		this.canvas = canvas;
	}

	/**
	 * Set the DOM Document tree to send the of processed commands to. Creates
	 * the root results node.
	 * 
	 * @param results
	 *            DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes the command name.
	 * 
	 * @param node
	 *            the command node to be processed
	 * @return the results node for the command
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		commandNode.setAttribute("name", node.getNodeName());
		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters node of the results. Should not throw a number format
	 * exception if the attribute has been defined to be an integer in the
	 * schema and the XML has been validated beforehand.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            integer attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add the parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the integer value */
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters node of the results.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            string attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the string value */
		return value;
	}

	/**
	 * Reports that the requested command could not be performed because of an
	 * error. Appends information about the error to the results.
	 * 
	 * @param type
	 *            type of error that occurred
	 * @param command
	 *            command node being processed
	 * @param parameters
	 *            parameters of command
	 */
	private void addErrorNode(final String type, final Element command,
			final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 * 
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 */
	private void addSuccessNode(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map and send the data to the appropriate data
	 * structures.
	 * 
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		spatialWidth = Integer.parseInt(node.getAttribute("spatialWidth"));
		spatialHeight = Integer.parseInt(node.getAttribute("spatialHeight"));
		g = Integer.parseInt(node.getAttribute("g"));
		pmOrder = Integer.parseInt(node.getAttribute("pmOrder"));

		/* initialize canvas */
		canvas.setFrameSize(spatialWidth, spatialHeight);
		/* add a rectangle to show where the bounds of the map are located */
		canvas.addRectangle(0, 0, (spatialWidth > spatialHeight) ? spatialWidth
				: spatialHeight, (spatialWidth > spatialHeight) ? spatialWidth
				: spatialHeight, Color.WHITE, true);
		canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
				false);

		/* set PR Quadtree range */
		prQuadtree.setRange(spatialWidth, spatialHeight);
		prQuadtree.setCanvas(canvas);
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		final String color = processStringAttribute(node, "color",
				parametersNode);

		/* create the city */
		final City city = new City(name, x, y, radius, color);

		if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode,
					parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a deleteCity command. Deletes a city from the dictionary. An
	 * error occurs if the city does not exist or is currently mapped.
	 * 
	 * @param node
	 *            deleteCity node being processed
	 */
	public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String name = processStringAttribute(node, "name", parametersNode);

		if (!citiesByName.containsKey(name)) {
			/* city with name does not exist */
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
		} else {
			/* delete city */
			final Element outputNode = results.createElement("output");
			final City deletedCity = citiesByName.get(name);

			if (prQuadtree.contains(name)) {
				/* city is mapped */
				prQuadtree.remove(deletedCity);
				addCityNode(outputNode, "cityUnmapped", deletedCity);
			}

			citiesByName.remove(name);
			citiesByLocation.remove(deletedCity);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 * 
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear data structures */
		citiesByName.clear();
		citiesByLocation.clear();
		prQuadtree.clear();

		/* clear canvas */
		canvas.clear();
		/* add a rectangle to show where the bounds of the map are located */
		canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
				false);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 * 
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy",
				parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");

			Collection<City> cityCollection = null;
			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				addCityNode(cityListNode, c);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName,
			final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("x", Integer.toString((int) city.getX()));
		cityNode.setAttribute("y", Integer.toString((int) city.getY()));
		cityNode.setAttribute("radius",
				Integer.toString((int) city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}

	/**
	 * Creates a road node containing information about a road
	 * @param node
	 * @param road
	 * @param roadNodeName 
	 */
	private void addRoadNode(final Element node, final Road road) {
		final Element roadNode = results.createElement("road");

		roadNode.setAttribute("end", road.getCities()[1].toString());
		roadNode.setAttribute("start", road.getCities()[0].toString());

		node.appendChild(roadNode);
	}
	

	/**
	 * Maps a city to the spatial map.
	 * 
	 * @param node
	 *            mapCity command node to be processed
	 */
	public void processMapCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else if (prQuadtree.contains(name)) {
			addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
		} else {
			City city = citiesByName.get(name);
			try {
				/* insert city into PR Quadtree */
				prQuadtree.add(city);

				/* add city to canvas */
				canvas.addPoint(city.getName(), city.getX(), city.getY(),
						Color.BLACK);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (CityAlreadyMappedException e) {
				addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
			} catch (CityOutOfBoundsException e) {
				addErrorNode("cityOutOfBounds", commandNode, parametersNode);
			}
		}
	}

	/**
	 * Removes a city from the spatial map.
	 * 
	 * @param node
	 *            unmapCity command node to be processed
	 */
	public void processUnmapCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else if (!prQuadtree.contains(name)) {
			addErrorNode("cityNotMapped", commandNode, parametersNode);
		} else {
			City city = citiesByName.get(name);

			/* unmap the city in the PR Quadtree */
			prQuadtree.remove(city);

			/* remove city from canvas */
			canvas.removePoint(city.getName(), city.getX(), city.getY(),
					Color.BLACK);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a saveMap command. Saves the graphical map to a given file.
	 * 
	 * @param node
	 *            saveMap command to be processed
	 * @throws IOException
	 *             problem accessing the image file
	 */
	public void processSaveMap(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		/* save canvas to '<name>.png' */
		canvas.save(name);

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Prints out the structure of the PR Quadtree in a human-readable format.
	 * 
	 * @param node
	 *            printPRQuadtree command to be processed
	 */
	public void processPrintPRQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (prQuadtree.isEmpty()) {
			/* empty PR Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			/* print PR Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			quadtreeNode.setAttribute("order", String.valueOf(pmOrder));
			printPRQuadtreeHelper(prQuadtree.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Traverses each node of the PR Quadtree.
	 * 
	 * @param currentNode
	 *            PR Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PR Quadtree node
	 */
	private void printPRQuadtreeHelper(final Node currentNode,
			final Element xmlNode) {
		// TODO Need to fix this method so that white nodes print <white/>
		// TODO Nodes that have a road associated with them are black
		final Element city;
		final Element node;
		if (currentNode.getType() == Node.EMPTY) {
			node = results.createElement("white");
			xmlNode.appendChild(node);

		} else {
			if (currentNode.getType() == Node.LEAF) {
				/* leaf node */
				final LeafNode currentLeaf = (LeafNode) currentNode;

				node = results.createElement("black");
				node.setAttribute("cardinality",
						String.valueOf(1 + currentLeaf.roads.size()));
				xmlNode.appendChild(node);

				// Isolated city
				if (currentLeaf.roads.size() == 0) {
					city = results.createElement("isolatedCity");
				} else {
					// City tag
					city = results.createElement("city");
				}

				city.setAttribute("color", "black");
				city.setAttribute("name", currentLeaf.getCity().getName());
				city.setAttribute("radius",
						String.valueOf(currentLeaf.getCity().radius));
				city.setAttribute("x",
						Integer.toString((int) currentLeaf.getCity().getX()));
				city.setAttribute("y",
						Integer.toString((int) currentLeaf.getCity().getY()));

				// Appends black node city
				xmlNode.appendChild(city);

				// Appends node roads
				for (Road r : currentLeaf.roads) {
					addRoadNode(xmlNode,r);
				}
			} else {
				/* internal node */
				final InternalNode currentInternal = (InternalNode) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x",
						Integer.toString((int) currentInternal.getCenterX()));
				gray.setAttribute("y",
						Integer.toString((int) currentInternal.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printPRQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City> citiesInRange = new TreeSet<City>(
				new CityNameComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}

		if (radius == 0) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		} else {
			/* get cities within range */
			final Point2D.Double point = new Point2D.Double(x, y);
			rangeCitiesHelper(point, radius, prQuadtree.getRoot(),
					citiesInRange);

			/* print out cities within range */
			if (citiesInRange.isEmpty()) {
				addErrorNode("noCitiesExistInRange", commandNode,
						parametersNode);
			} else {
				/* get city list */
				final Element cityListNode = results.createElement("cityList");
				for (City city : citiesInRange) {
					addCityNode(cityListNode, city);
				}
				outputNode.appendChild(cityListNode);

				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);

				if (pathFile.compareTo("") != 0) {
					/* save canvas to file with range circle */
					canvas.addCircle(x, y, radius, Color.BLUE, false);
					canvas.save(pathFile);
					canvas.removeCircle(x, y, radius, Color.BLUE, false);
				}
			}
		}
	}

	/**
	 * Determines if any cities within the PR Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            PR Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void rangeCitiesHelper(final Point2D.Double point,
			final int radius, final Node node, final TreeSet<City> citiesInRange) {
		if (node.getType() == Node.LEAF) {
			final LeafNode leaf = (LeafNode) node;
			final double distance = point.distance(leaf.getCity().toPoint2D());
			if (distance <= radius) {
				/* city is in range */
				final City city = leaf.getCity();
				citiesInRange.add(city);
			}
		} else if (node.getType() == Node.INTERNAL) {
			/* check each quadrant of internal node */
			final InternalNode internal = (InternalNode) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				if (prQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeCitiesHelper(point, radius, internal.getChild(i),
							citiesInRange);
				}
			}
		}
	}

	/**
	 * Finds the nearest city to a given point.
	 * 
	 * @param node
	 *            nearestCity command being processed
	 */
	public void processNearestCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		// Check if there are cities on the map
		if (citiesByName.size() <= 0) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		// Check to see if root is empty
		if (prQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {

			City n = nearestCityHelper2(prQuadtree.getRoot(), point);
			addCityNode(outputNode, n);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * 2/25/2011
	 * 
	 * @param root
	 * @param point
	 */
	private City nearestCityHelper2(Node root, Point2D.Float point) {
		PriorityQueue<QuadrantDistance> q = new PriorityQueue<QuadrantDistance>();
		Node currNode = root;
		while (currNode.getType() != Node.LEAF) {
			InternalNode g = (InternalNode) currNode;
			for (int i = 0; i < 4; i++) {
				Node kid = g.children[i];
				if (kid.getType() == Node.LEAF){
					if (((LeafNode) kid).getCity() != null){
						q.add(new QuadrantDistance(kid, point));
					}		
				} else if (kid.getType() != Node.EMPTY) {
					q.add(new QuadrantDistance(kid, point));
				}
			}
			currNode = q.remove().quadtreeNode;
		}

		return ((LeafNode) currNode).getCity();
	}

	// Says that you are a certain distance from a quadrant or a city
	class QuadrantDistance implements Comparable<QuadrantDistance> {
		public Node quadtreeNode;
		private double distance;

		public QuadrantDistance(Node node, Point2D.Float pt) {
			quadtreeNode = node;
			if (node.getType() == Node.INTERNAL) {
				InternalNode gray = (InternalNode) node;
				// Calculates distance from point to gray node quadrant 
				distance = Shape2DDistanceCalculator.distance(pt,
						new Rectangle2D.Float(gray.origin.x, gray.origin.y,
								gray.width, gray.height));
			} else if (node.getType() == Node.LEAF) {
				LeafNode leaf = (LeafNode) node;
				distance = pt.distance(leaf.getCity().pt);
			} else {
				throw new IllegalArgumentException(
						"Only leaf or internal node can be passed in");
			}
		}

		public int compareTo(QuadrantDistance qd) {
			if (distance < qd.distance) {
				return -1;
			} else if (distance > qd.distance) {
				return 1;
			} else {
				if (quadtreeNode.getType() != qd.quadtreeNode.getType()) {
					if (quadtreeNode.getType() == Node.INTERNAL) {
						return -1;
					} else {
						return 1;
					}
				} else if (quadtreeNode.getType() == Node.LEAF) {
					// both are leaves
					return ((LeafNode) quadtreeNode)
							.getCity()
							.getName()
							.compareTo(
									((LeafNode) qd.quadtreeNode).getCity()
											.getName());
				} else {
					// both are internals
					return 0;
				}
			}
		}
	}
	
	/**
	 * Finds the nearest city to a given point.
	 * 
	 * @param node
	 *            nearestCity command being processed
	 */
	public void processNearestIsolatedCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);

		// Check if there are cities on the map
		if (citiesByName.size() <= 0) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		// Check to see if root is empty
		if (prQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {

			City n = processNearestIsolatedCityHelper2(prQuadtree.getRoot(), point);
			addCityNode(outputNode, n);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Helper for finding the nearest isolated city
	 * 
	 * @param root
	 * @param point
	 */
	private City processNearestIsolatedCityHelper2(Node root, Point2D.Float point) {
		PriorityQueue<QuadrantDistance> q = new PriorityQueue<QuadrantDistance>();
		Node currNode = root;
		while (currNode.getType() != Node.LEAF) {
			InternalNode g = (InternalNode) currNode;
			for (int i = 0; i < 4; i++) {
				Node kid = g.children[i];
				// Checks that child is a leaf
				if (kid.getType() == Node.LEAF){
					// Checks that Leaf has a city
					if (((LeafNode) kid).getCity() != null){
						// Checks that Leaf nodes City is Isolated
						if (((LeafNode) kid).roads.size() == 0){
							q.add(new QuadrantDistance(kid, point));
						}						
					}	
				// Check to make sure a gray node
				} else if (kid.getType() == Node.INTERNAL) {
					q.add(new QuadrantDistance(kid, point));
				}
				// Does not add empty nodes to priority queue
			}
			currNode = q.remove().quadtreeNode;
		}

		return ((LeafNode) currNode).getCity();
	}
	
	public void processNearestRoad(final Element node){
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);
		
		// Check if there are cities on the map, Must have 2 to have road
		if (citiesByName.size() <= 1) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		// Check to see if root is empty
		if (prQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {

			Road n = processNearestRoadHelper(prQuadtree.getRoot(), point);
			addRoadNode(outputNode, n);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
		
	private Road processNearestRoadHelper(Node root, Point2D.Float point) {
		PriorityQueue<RoadQuadrantDistance> q = new PriorityQueue<RoadQuadrantDistance>();
		Node currNode = root;	
		HashSet<Road> roads_added = new HashSet<Road>();
		//RoadQuadrantDistance currDistance = new RoadQuadrantDistance(currNode, point);
		RoadQuadrantDistance currDistance = null;
		
		// Iterates until the first node in priority queue is not a gray node
		while (currNode.getType() == Node.INTERNAL){
			InternalNode g = (InternalNode) currNode;
			
			// Looping over children of current gray node
			for (int i = 0; i < 4; i++){
				Node kid = g.children[i];
				
				// Check that child is gray
				if (kid.getType() == Node.INTERNAL){
					q.add(new RoadQuadrantDistance(kid, point));

				} else if (kid.getType() == Node.LEAF){
					for (Road r : ((LeafNode) kid).roads){
						// Only add a road once
						
						if (!roads_added.contains(r)){
							q.add(new RoadQuadrantDistance(r, kid, point));
							roads_added.add(r);
						}
							
					}
				}
			}
			// pops off first closest distance, 
			// if its not a gray it will not continue the loop
			currDistance = q.remove();
			currNode = currDistance.quadtreeNode;
		}
		return currDistance.road;
	}
	
	
	/**
	 * Calculates distance between a quadrant(gray node) or a road and a specific point
	 * @author Dylan Zingler
	 *
	 */
	class RoadQuadrantDistance implements Comparable<RoadQuadrantDistance> {
		public Node quadtreeNode;
		private double distance;
		public Road road;

		/**
		 * Constructor that takes in a node and not a road (For Internal Only)
		 * @param node
		 * @param pt
		 */
		public RoadQuadrantDistance(Node node, Point2D.Float pt) {
			quadtreeNode = node;
			road = null;
			if (node.getType() == Node.INTERNAL) {
				InternalNode gray = (InternalNode) node;
				// Calculates distance from point to gray node quadrant 
				distance = Shape2DDistanceCalculator.distance(pt,
						new Rectangle2D.Float(gray.origin.x, gray.origin.y,
								gray.width, gray.height));
			} else {
				throw new IllegalArgumentException(
						"Only internal node can be passed in");
			}
		}

		/**
		 * Constructor that takes in a road
		 * @param r
		 * @param node
		 * @param point
		 */
		public RoadQuadrantDistance(Road r, Node node, Point2D.Float point) {
			quadtreeNode = node;
			distance = r.getLine().ptSegDist(point);
			road = r;
		}

		public int compareTo(RoadQuadrantDistance qd) {
			if (distance < qd.distance) {
				return -1;
			} else if (distance > qd.distance) {
				return 1;
			} else {
				if (quadtreeNode.getType() != qd.quadtreeNode.getType()) {
					if (quadtreeNode.getType() == Node.INTERNAL) {
						return -1;
					} else {
						return 1;
					}
				} else if (quadtreeNode.getType() == Node.LEAF) {
					// both are leaves
					return ((LeafNode) quadtreeNode)
							.getCity()
							.getName()
							.compareTo(
									((LeafNode) qd.quadtreeNode).getCity()
											.getName());
				} else {
					// both are internals
					return 0;
				}
			}
		}
	}

	/**
	 * Processes the adding of a new Road to the map
	 * 
	 * @param node
	 */
	public void processMapRoad(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String start_city = processStringAttribute(node, "start",
				parametersNode);
		final String end_city = processStringAttribute(node, "end",
				parametersNode);
		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(start_city)
				|| !citiesByName.containsKey(end_city)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else {
			City s_city = citiesByName.get(start_city);
			City e_city = citiesByName.get(end_city);
			Road road = new Road(s_city, e_city);

			canvas.addLine(s_city.getX(), s_city.getY(), e_city.getX(),
					e_city.getY(), Color.CYAN);

			// Check to make sure root is gray, if not you can't have a road
			if (prQuadtree.getRoot().getType() != Node.INTERNAL) {
				addErrorNode("cannotHaveARoad", commandNode, parametersNode);
			} else { // Root is gray. Check what quadrants the road hits

				prQuadtree.add(road);

				// processMapRoadHelper(prQuadtree.getRoot(), road);
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}
	

	/*
	*//**
	 * Helper Method: Adds road to a Nodes Road list if it intersects
	 * 
	 * @param currentNode
	 * @param road
	 */
	/*
	 * private void processMapRoadHelper(Node currentNode, Road road) { if
	 * (currentNode.getType() == Node.LEAF) { final LeafNode currentLeaf =
	 * (LeafNode) currentNode; // Check to see if road intersects quadrant if
	 * (Inclusive2DIntersectionVerifier.intersects(road.getLine(),
	 * currentLeaf.rect)) { currentNode.add_road(road);
	 * 
	 * // TODO: TESTING - Highlights Quadrant that includes Road
	 * canvas.addRectangle(currentLeaf.rect.getX(), currentLeaf.rect.getY(),
	 * currentLeaf.rect.getWidth(), currentLeaf.rect.getHeight(), Color.RED,
	 * false); } } else if (currentNode.getType() == Node.EMPTY) { final
	 * EmptyNode currentLeaf = (EmptyNode) currentNode; // Check to see if road
	 * intersects quadrant if
	 * (Inclusive2DIntersectionVerifier.intersects(road.getLine(),
	 * currentLeaf.rect)) { currentNode.add_road(road);
	 * 
	 * // TODO: TESTING - Highlights Quadrant that includes Road
	 * canvas.addRectangle(currentLeaf.rect.getX(), currentLeaf.rect.getY(),
	 * currentLeaf.rect.getWidth(), currentLeaf.rect.getHeight(), Color.RED,
	 * false); } } else { final InternalNode currentInternal = (InternalNode)
	 * currentNode;
	 * 
	 * // Recursive call on all 4 children for (int i = 0; i < 4; i++) {
	 * processMapRoadHelper(currentInternal.getChild(i), road); } } }
	 */
	
	public void processRangeRoads(Element node){
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		// Gathers info from attributes
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		final Point2D.Float point = new Point2D.Float(x, y);
		
		HashSet<Road> roads_in_range = new HashSet<Road>();
		
		
		// Check if there are cities on the map, Must have 2 to have road
		if (citiesByName.size() <= 1) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
			return;
		}

		// Check to see if root is empty
		if (prQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			roads_in_range = processRangeRoadsHelper(prQuadtree.getRoot(), point, radius);
			
			// Must sort roads_in_range First!!!!!
			
			for (Road r: roads_in_range){
				addRoadNode(outputNode, r);
				
			}
			
			addSuccessNode(commandNode, parametersNode, outputNode);
			
			
		}
	}
	
	
	
	private HashSet<Road> processRangeRoadsHelper(Node root, Float point, int radius) {
		PriorityQueue<RoadQuadrantDistance> q = new PriorityQueue<RoadQuadrantDistance>();
		Node currNode = root;
		HashSet<Road> roads_added = new HashSet<Road>();
		HashSet<Road> roads_in_range = new HashSet<Road>();

		RoadQuadrantDistance currDistance = null;
		
		// Iterates until the first node in priority queue is not a gray node
		while (currNode.getType() == Node.INTERNAL){
			InternalNode g = (InternalNode) currNode;
			
			// Looping over children of current gray node
			for (int i = 0; i < 4; i++){
				Node kid = g.children[i];
				
				// Check that child is gray
				if (kid.getType() == Node.INTERNAL){
					q.add(new RoadQuadrantDistance(kid, point));

				} else if (kid.getType() == Node.LEAF){
					for (Road r : ((LeafNode) kid).roads){
						// Only add a road once
						
						if (!roads_added.contains(r)){
							q.add(new RoadQuadrantDistance(r, kid, point));
							roads_added.add(r);
						}
							
					}
				}
			}
			
			// Removes Leaf nodes from beginning of priority queue
			while (q.peek() != null && q.peek().quadtreeNode.getType() == Node.LEAF){
				currDistance = q.remove();
				if (currDistance.distance <= radius){
					roads_in_range.add(currDistance.road);
				}
			}
			// Removing a gray node
			if (q.peek() != null){
				currDistance = q.remove();
				currNode = currDistance.quadtreeNode;
				if (currDistance.distance > radius){
					break;
				}
			} else {
				break;
			}
			
		}
		return roads_in_range;
	}

	

/*	public void processRangeRoads(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		// Gathers info from attributes
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		// Creates Circle or area in question for road intersection
		final Point2D.Double pt = new Point2D.Double(x, y);
		final Circle2D.Double circle = new Circle2D.Double(pt, radius);

		Set<Road> road_intersections = new HashSet<Road>();
		
		// Looping over Roads (Might have to do this recursively DFS)
		for (Road r : prQuadtree.roads) {

			ArrayList<Point2D> intersections = (ArrayList<Point2D>) CircleLine
					.getCircleLineIntersectionPoint(r.getLine().getP1(), r
							.getLine().getP2(), pt, radius);

			System.out.println(intersections.toString());

			
			for (Point2D p : intersections){
				if (r.point_close_enough(p)){
					road_intersections.add(r);
				}
				
//				if (r.getLine().contains(p.getX(), p.getY())){
//					road_intersections.add(r);
//				}
			}
		}
		
		System.out.println(road_intersections.toString());

	}

*/	public void processPrintAvlTree(Element commandNode) {
		// TODO Auto-generated method stub

	}

	public void processPrintPMQuadtree(Element commandNode) {
		// TODO Auto-generated method stub

	}

	public void processShortestPath(Element commandNode) {
		// TODO Auto-generated method stub

	}
}
