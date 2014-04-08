package cmsc420.meeshquest.part2;


//import java.io.File;
//import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.drawing.CanvasPlus;
import cmsc420.xml.XmlUtility;


public class MeeshQuest {

	/* input stream/file */
	//private final InputStream xmlInput = System.in;
	
	
	// UNCOMMENT THIS TO TEST WITH FILES
	//private File xmlInput = new File("part1.rangeCitiesAndSaveMap.input.xml");
	private File xmlOutput = new File("test1.txt");

	private File xmlInput = new File("dyLand2.xml");
	
	

	/* output DOM Document tree */
	private Document results;

	/* processes each command */
	private final Command command = new Command();
	
	/*
	 * graphical mapping tool (needs to be disposed after all commands have been processed)
     * Doesn't work on non-graphical submit server
	 */
	private final CanvasPlus canvas = new CanvasPlus("MeeshQuest");
	//private CanvasPlus canvas;

	public static void main(String[] args) {
		final MeeshQuest m = new MeeshQuest();
		
		// UNCOMMENT to run XML code
		m.processInput();
		
		/*// Testing for AVL-G Tree
		System.out.println("dylan your the best");
		
		AvlGTree tree = new AvlGTree(1);
		City a1 = new City("a1", 3, 1,1, "red");
		City a2 = new City("a2", 3, 1,1, "red");
		City a3 = new City("a3", 3, 1,1, "red");
		City a4 = new City("a4", 3, 1,1, "red");
		
		tree.insert("a4", a4);
		tree.insert("a1", a1);
		tree.insert("a2", a2);
		tree.insert("a3", a3);
		
		
		tree.printTree();
		*/
		
	}

	public void processInput() {
		try {
			// testing purposes
			//final String testName = "unmapTest2";
			//xmlInput = new File("part1." + testName + ".input.xml");
			//xmlOutput = new File("part1." + testName + ".output.xml");

			/* create output */
			results = XmlUtility.getDocumentBuilder().newDocument();
			command.setResults(results);
			
			/* set canvas for command */
			command.setCanvas(canvas);

			/* validate document */
			Document doc = XmlUtility.validateNoNamespace(xmlInput);

			/* process commands element */
			Element commandNode = doc.getDocumentElement();
			processCommand(commandNode);

			/* process each command */
			final NodeList nl = commandNode.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
					/* need to check if Element (ignore comments) */
					commandNode = (Element) nl.item(i);
					processCommand(commandNode);
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
			addFatalError();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			addFatalError();
		} catch (IOException e) {
			e.printStackTrace();
			addFatalError();
		} finally {
			/* dispose canvas */
			if (canvas != null)
                canvas.dispose();
			try {
				/* print results to XML */
				
				// UNCOMMENT THIS TO TEST WITH FILES
				XmlUtility.write(results, xmlOutput);
				
				XmlUtility.print(results);
			} catch (TransformerException e) {
				System.exit(-1);
			}
			// UNCOMMENT THIS TO TEST WITH FILES
			 catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void addFatalError() {
		try {
			results = XmlUtility.getDocumentBuilder().newDocument();
			final Element fatalError = results.createElement("fatalError");
			results.appendChild(fatalError);
		} catch (ParserConfigurationException e) {
			System.exit(-1);
		}
	}

	private void processCommand(final Element commandNode) throws IOException {
		final String name = commandNode.getNodeName();

		if (name.equals("commands")) {
			command.processCommands(commandNode);
		} else if (name.equals("createCity")) {
			command.processCreateCity(commandNode);
		} else if (name.equals("deleteCity")) {
			command.processDeleteCity(commandNode);
		} else if (name.equals("clearAll")) {
			command.processClearAll(commandNode);
		} else if (name.equals("listCities")) {
			command.processListCities(commandNode);
		} else if (name.equals("mapCity")) {
			command.processMapCity(commandNode);
		} else if (name.equals("unmapCity")) {
			command.processUnmapCity(commandNode);
		} else if (name.equals("saveMap")) {
			command.processSaveMap(commandNode);
		} else if (name.equals("printPRQuadtree")) {
			command.processPrintPRQuadtree(commandNode);
		} else if (name.equals("rangeCities")) {
			command.processRangeCities(commandNode);
		} else if (name.equals("nearestCity")) {
			command.processNearestCity(commandNode);
		} else if (name.equals("nearestIsolatedCity")) {
			command.processNearestIsolatedCity(commandNode);
		} else if (name.equals("nearestRoad")) {	// Part 2 
			command.processNearestRoad(commandNode);			
		} else if (name.equals("nearestCityToRoad")) {	// Part 2 
			command.processNearestCityToRoad(commandNode);			
		} else if (name.equals("mapRoad")) {		// PART 2
			command.processMapRoad(commandNode);
		} else if (name.equals("rangeRoads")) {		// PART 2
			command.processRangeRoads(commandNode);
		} else if (name.equals("printAvlTree")) {		// PART 2
			command.processPrintAvlTree(commandNode);
		} else if (name.equals("printPMQuadtree")) {		// PART 2 
			command.processPrintPRQuadtree(commandNode); // TODO ***WARINING THIS CALLS THE WRONG METHOD MUST CALL PM NOT PR
		} else if (name.equals("shortestPath")) {		// PART 2
			command.processShortestPath(commandNode);
		} else {
			/* problem with the Validator */
			System.exit(-1);
		}
	}
}
