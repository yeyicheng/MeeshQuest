package cmsc420.meeshquest.part2;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import cmsc420.meeshquest.part2.Command;
import cmsc420.meeshquest.part2.AvlGTree;
import cmsc420.xml.XmlUtility;
import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * AVL Tree tests
 * @author Adam Hamot
 *
 */
public class TestAVLTree {

	public Comparator<Integer> IntComparator() {
		return new Comparator<Integer>() {
			public int compare(Integer a, Integer b) {
				return (a > b ? 1 : (a < b ? -1 : 0));
			}
		};
	}
	
	public void printArr(Object[] arr) {
		System.out.println("arr------------");
		if (arr.length == 0)
			System.out.print("Empty array!");
		
		for (int i = 0; i < arr.length; i++) {
			System.out.print(arr[i] + " ");
		}
		System.out.println();
	}
	
	public AvlGTree<Integer, Integer> getNewTree(int g) {
		AvlGTree<Integer, Integer> temp = new AvlGTree<Integer, Integer>(IntComparator(), g);

		temp.put(1, 2);
		temp.put(2, 3);
		temp.put(3, 4);
		temp.put(-1, 0);
		temp.put(-2, 0);
		temp.put(100,0);
		temp.put(101, 1);
		temp.put(-5,7);
		temp.put(-10,8);
		
//		try {
//			Document results = XmlUtility.getDocumentBuilder().newDocument();
//			Element rawr = results.createElement("rawr");
//			
//			Command c = new Command();
//			c.setResults(results);
//			c.getResultsNode().appendChild(rawr);
//			
//			c.printAvlGTreeHelper(temp.getRoot(), rawr);
//			XmlUtility.print(results);
//			System.out.println(temp.getRoot().getHeight());
//		} catch (ParserConfigurationException | TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return temp;
	}
	
	@Test
	public void testPut() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		assertEquals(agtree.size(), 9);
		try {
			agtree.put(null, null);
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.NullPointerException");
		}
		assertEquals(agtree.size(), 9);
		assertEquals(agtree.put(-10, 9), new Integer(8));
		assertEquals(agtree.get(-10), new Integer(9));
	}
	
	@Test
	public void testMaxHeightFor1() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		assertEquals(agtree.getHeight(), 4);
	}
	
	@Test
	public void testContainsKey1() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		assertEquals(agtree.containsKey(101), true);
		assertEquals(agtree.containsKey(-5), true);
		agtree.clear();
		assertEquals(agtree.containsKey(-5), false);
		assertEquals(agtree.size(), 0);
		agtree.put(1, 0);
		assertEquals(agtree.size(), 1);
		try {
			assertEquals(agtree.containsKey(null), false);
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.NullPointerException");
		}
	}
	
	@Test
	public void testContainsValue1() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		assertEquals(agtree.containsValue(2), true);
		assertEquals(agtree.containsValue(8), true);
		assertEquals(agtree.containsValue(-100), false);
		agtree.clear();
		assertEquals(agtree.containsValue(8), false);
		try {
			assertEquals(agtree.containsValue(null), false);
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.NullPointerException");
		}
	}

	@Test
	public void testGet1() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		assertEquals(agtree.get(2), new Integer(3));
		assertEquals(agtree.get(3), new Integer(4));
		assertEquals(agtree.get(-100), null);
		try {
			assertEquals(agtree.get(null), null);
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.NullPointerException");
		}
	}
	
	@Test
	public void testPutAll1() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		TreeMap<Integer, Integer> manMap = new TreeMap<Integer, Integer>(IntComparator());
		TreeMap<Integer, Integer> emptyMap = new TreeMap<Integer, Integer>(IntComparator());
		Object[] expected = { -10, -5, -2, -1, 1, 2, 3, 100, 101, 300, 400, 700 };

		manMap.put(400, -7);
		manMap.put(300, 0);
		manMap.put(100, 0);
		manMap.put(700, 0);
		agtree.putAll(manMap);
		agtree.putAll(emptyMap);
		
		
		
		try {
			agtree.putAll(null);
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.NullPointerException");
		}
		Object[] arr = agtree.keySet().toArray();

		assertArrayEquals(expected, arr);
	}
	
	@Test
	public void testFirstKey() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		assertEquals(agtree.firstKey(), new Integer(-10));
		agtree.clear();
		try {
			agtree.firstKey();
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.util.NoSuchElementException");
		}
		
		agtree.put(1, 0);
		agtree.put(2, 0);
		assertEquals(agtree.firstKey(), new Integer(1));
	}
	
	@Test
	public void testLastKey() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		assertEquals(agtree.lastKey(), new Integer(101));
		agtree.clear();
		try {
			agtree.lastKey();
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.util.NoSuchElementException");
		}
		
		agtree.put(1, 0);
		agtree.put(-1, 0);
		assertEquals(agtree.lastKey(), new Integer(1));
	}
	
	@Test
	public void testConcurrentModEx() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		Iterator<Map.Entry<Integer, Integer>> it = agtree.entrySet().iterator();
		try {
			while (it.hasNext()) {
				it.next();
				agtree.put(7000, 2);
			}
			fail("Should have thrown ConcurrentModificationException!");
		}
		catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.util.ConcurrentModificationException");
		}
		
		try {
			while (it.hasNext()) {
				it.next();
				agtree.put(1, 7);
			}
			fail("Should have thrown ConcurrentModificationException!");
		}
		catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.util.ConcurrentModificationException");
		}
	}
	
	@Test
	public void testNoSuchElementEx() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		Iterator<Map.Entry<Integer, Integer>> it = agtree.entrySet().iterator();
		try {
			while (it.hasNext()) {
				it.next();
			}
			it.next();
		}
		catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.util.NoSuchElementException");
		}
	}
	
	@Test
	public void testEntrySetUpdating() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		Set<Map.Entry<Integer, Integer>> tempSet = agtree.entrySet();
		assertFalse(tempSet.contains(new AbstractMap.SimpleEntry<Integer,Integer>(1234, 1111)));
		agtree.put(1234, 1111);
		assertTrue(tempSet.contains(new AbstractMap.SimpleEntry<Integer,Integer>(1234, 1111)));
		try {
			agtree.put(null, null);
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.NullPointerException");
		}
		assertFalse(tempSet.contains(null));
	}
	
	@Test
	public void testColValue() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		Object[] expected = { 8, 7, 0, 0, 2, 3, 4, 0, 1 };
		Object[] arr = agtree.values().toArray();
		
		assertArrayEquals(expected, arr);
	}
	
	@Test
	public void testEmpty() throws Exception {
		AvlGTree<Integer, Integer> agtree = new AvlGTree<Integer, Integer>(IntComparator(), 1);
		assertEquals(agtree.containsKey(1), false);
		assertEquals(agtree.containsValue(1), false);
		assertEquals(agtree.get(5), null);
		assertEquals(agtree.size(), 0);
		assertEquals(agtree.entrySet().size(), 0);
		assertEquals(agtree.values().size(), 0);
		assertEquals(agtree.keySet().size(), 0);

//		agtree = getNewTree(1);
//		
//		Object[] arr = new Object[11]; 
//		arr = agtree.values().toArray(arr);
//		System.out.println("--");
//		for (int i = 0; i < arr.length; i++) {
//			System.out.println(arr[i] + " ");
//		}
//		System.out.println("--");
//		
//		arr = new Object[11]; 
//		arr = agtree.entrySet().toArray(arr);
//		System.out.println("--");
//		for (int i = 0; i < arr.length; i++) {
//			System.out.println(arr[i] + " ");
//		}
//		System.out.println("--");
	}
	
	@Test
	public void testMapEquals() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		TreeMap<Integer, Integer> temp = new TreeMap<Integer, Integer>();
		temp.put(1, 2);
		temp.put(2, 3);
		temp.put(3, 4);
		temp.put(-1, 0);
		temp.put(-2, 0);
		temp.put(100,0);
		temp.put(101, 1);
		temp.put(-5,7);
		temp.put(-10,8);
		assertEquals(agtree.equals(temp), true);
		assertEquals(temp.equals(agtree), true);
		agtree.put(-7, 0);
		assertFalse(temp.equals(agtree));
		assertFalse(agtree.equals(temp));
	}
	
	@Test
	public void testEntrySetEquals() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		Set<Map.Entry<Integer,Integer>> agSet = agtree.entrySet();
		TreeMap<Integer, Integer> temp = new TreeMap<Integer, Integer>();
		temp.put(1, 2);
		temp.put(2, 3);
		temp.put(3, 4);
		temp.put(-1, 0);
		temp.put(-2, 0);
		temp.put(100,0);
		temp.put(101, 1);
		temp.put(-5,7);
		temp.put(-10,8);
		
		HashSet<Map.Entry<Integer,Integer>> tempSet = new HashSet<Map.Entry<Integer,Integer>>();
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(1, 2));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(2, 3));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(3, 4));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(-1, 0));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(-2, 0));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(100, 0));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(101, 1));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(-5, 7));
		tempSet.add(new AbstractMap.SimpleEntry<Integer,Integer>(-10, 8));
		
		assertEquals(agSet.equals(tempSet), true);
		assertEquals(tempSet.equals(agSet), true);
		assertEquals(temp.entrySet().equals(agtree.entrySet()), true);
		assertEquals(temp.keySet().equals(agtree.keySet()), true);
		assertEquals(agtree.values().equals(temp.values()), true);
		
		assertEquals(agtree.keySet().hashCode() == temp.keySet().hashCode(), true);
		assertEquals(agtree.entrySet().hashCode() == temp.entrySet().hashCode(), true);
		assertEquals(tempSet.hashCode() == agtree.entrySet().hashCode(), true);
		
		assertArrayEquals(temp.entrySet().toArray(), agtree.entrySet().toArray());
		assertArrayEquals(temp.keySet().toArray(), agtree.keySet().toArray());
		assertArrayEquals(temp.values().toArray(), agtree.values().toArray());
		
		agtree.clear();
		assertEquals(agtree.values().equals(agtree.values()), true);
	}
	
	@Test
	public void testKeySetEquals() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(1);
		Set<Integer> agSet = agtree.keySet();
		
		TreeMap<Integer, Integer> temp = new TreeMap<Integer, Integer>();
		temp.put(1, 2);
		temp.put(2, 3);
		temp.put(3, 4);
		temp.put(-1, 0);
		temp.put(-2, 0);
		temp.put(100,0);
		temp.put(101, 1);
		temp.put(-5,7);
		temp.put(-10,8);
		
		
		HashSet<Integer> tempSet = new HashSet<Integer>();
		tempSet.add(1);
		tempSet.add(2);
		tempSet.add(3);
		tempSet.add(-1);
		tempSet.add(-2);
		tempSet.add(100);
		tempSet.add(101);
		tempSet.add(-5);
		tempSet.add(-10);
		
		assertEquals(agSet.equals(tempSet), true);
		assertEquals(tempSet.equals(agSet), true);
		assertEquals(temp.keySet().equals(agtree.keySet()), true);
	}
	
	@Test
	public void testClear() throws Exception {
		AvlGTree<Integer, Integer> agtree = getNewTree(4);
		agtree.clear();
		assertEquals(agtree.size(), 0);
		assertEquals(agtree.containsKey(11), false);
		agtree.put(11, 11);
		assertEquals(agtree.size(), 1);
		assertEquals(agtree.containsKey(11), true);
		agtree.clear();
		assertEquals(agtree.size(), 0);
		assertEquals(agtree.containsKey(11), false);
		agtree = getNewTree(1);
		Set<Integer> tempSet = agtree.keySet();
		Iterator<Integer> it = tempSet.iterator();
		agtree.clear();
		while (it.hasNext()) {
			fail("Iterator should not be running!");
		}
		assertEquals(tempSet.size(), 0);
		printArr(agtree.keySet().toArray());
		printArr(agtree.entrySet().toArray());
		printArr(agtree.values().toArray());
		Object myDirtyArray[] = new Object[2];
		agtree.put(1, 1);
		agtree.put(2, 1);
		agtree.put(3, 1);
		agtree.keySet().toArray(myDirtyArray);
		printArr(myDirtyArray);
	}
	
	@Test
	public void testSubMap() throws Exception {
		AvlGTree<Integer, Integer> tempTree = new AvlGTree<Integer, Integer>(IntComparator(), 1);
		
		tempTree.put(7, 0);
		tempTree.put(4, 0);
		tempTree.put(9, 0);
		tempTree.put(5, 0);
		tempTree.put(8, 0);
		tempTree.put(10, 0);
		tempTree.put(2, 1);
		
//		tempTree.printTree();

		/**
		 * HeadMap -> return a view of map strictly less than (exclusive) toKey, IllegalArgumentException if insert out of bounds
		 * TailMap -> return a view of map strictly greater than or equal to (inclusive) fromKey, IAE if insert out of bounds
		 * SubMap -> Keys range from (inclusive) fromKey to (exclusive) toKey, if fromKey == toKey then map empty, IAE same cond
		 * @author adam
		 *
		 */
		SortedMap<Integer, Integer> headMap = tempTree.headMap(7);
		SortedMap<Integer, Integer> tailMap = tempTree.tailMap(7);
		SortedMap<Integer, Integer> subMap = tempTree.subMap(2, 10);
		
		SortedMap<Integer, Integer> emptyMap = tempTree.subMap(4,4);
		
		assertFalse(emptyMap.containsKey(4));
		assertEquals(emptyMap.size(), 0);
		
		Set<Integer> keySet = headMap.keySet();
		assertEquals(keySet.contains(7), false);
		assertEquals(headMap.containsKey(7), false);
		
		assertEquals(headMap.containsValue(1), true);
		
		assertEquals(tailMap.containsValue(0), true);
		
		assertEquals(tailMap.size(), 4);
		assertEquals(subMap.size(), 6);
		
		tempTree.put(6, 0);
		tempTree.put(-1, 0);
		tempTree.put(3, 0);
		tempTree.put(1, 0);
		
		Object[] temp = new Object[1];
		temp = keySet.toArray(temp);
		printArr(temp);
		
		assertEquals(headMap.size(), temp.length);
		assertEquals(tailMap.size(), 4);
		assertEquals(subMap.size(), 8);
		
		assertTrue(headMap.containsKey(4));
		assertFalse(tailMap.containsKey(4));
		
		assertEquals(subMap.firstKey(), new Integer(2));
		assertEquals(subMap.lastKey(), new Integer(9));
		
		assertEquals(headMap.firstKey(), new Integer(-1));
		
		tempTree.put(11, 0);
		tempTree.put(12, 0);
		tempTree.put(13, 0);
		tempTree.put(14, 0);
		tempTree.put(16, 0);
		
//		tempTree.printTree();
		
		assertEquals(tailMap.lastKey(), new Integer(16));
		assertEquals(tailMap.firstKey(), new Integer(7));
		
		assertEquals(headMap.lastKey(), new Integer(6));
		
		assertEquals(subMap.firstKey(), new Integer(2));
		assertEquals(subMap.lastKey(), new Integer(9));
		printArr(subMap.keySet().toArray());
		
		assertEquals(subMap.size(), 8);
		
		try {
			tailMap.subMap(2, 5).keySet().toArray();
			fail("You should be throwing an illegal exception here!");
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.IllegalArgumentException");
		}
		printArr(temp);
		
		// Questionable..
//		assertEquals(tailMap.headMap(9).firstKey(), new Integer(-1));
//		printArr(tailMap.headMap(9).keySet().toArray());
		
		try {
			tailMap.tailMap(6);
			fail("You should be throwing an illegal exception here!");
		} catch (Exception e) {
			assertEquals(e.getClass().getName(), "java.lang.IllegalArgumentException");
		}
		
		assertEquals(headMap.subMap(2, 5).lastKey(), new Integer(4));
		
		TreeMap<Integer, Integer> treeTemp = new TreeMap<Integer, Integer>(IntComparator());
		treeTemp.put(2, 1);
		treeTemp.put(3, 0);
		treeTemp.put(4, 0);
		
		assertTrue(treeTemp.equals(headMap.subMap(2,5)));
		assertTrue(headMap.subMap(2,5).equals(treeTemp));
		assertTrue(treeTemp.keySet().equals(headMap.subMap(2,5).keySet()));
		assertEquals(headMap.subMap(2,5).equals(treeTemp.keySet()), false);
		
		assertEquals(treeTemp.put(2, 5), new Integer(1));
		assertEquals(treeTemp.get(1), null);
		
		printArr(headMap.keySet().toArray());
//		printArr(headMap.tailMap(7).keySet().toArray());
		
		TreeMap<Integer, Integer> s = new TreeMap<Integer, Integer>(IntComparator());
		s.put(1, 2);
		s.put(7, 0);
		s.put(4, 0);
		s.put(9, 0);
		s.put(5, 0);
		s.put(8, 1);
		s.put(10, 0);
		s.put(2, 0);
		s.put(6, 0);
		s.put(-1, 0);
		s.put(3, 0);
		s.put(1, 0);
		s.put(11, 0);
		s.put(12, 0);
		s.put(13, 0);
		s.put(14, 0);
		s.put(16, 0);
		printArr(s.headMap(7).keySet().toArray());
		printArr(s.headMap(7).tailMap(6).keySet().toArray());
		printArr(headMap.keySet().toArray());
		printArr(headMap.tailMap(6).keySet().toArray());
		System.out.println(s.headMap(7).tailMap(6).lastKey());
		
		assertEquals(headMap.tailMap(6).firstKey(), s.headMap(7).tailMap(6).firstKey());
		assertEquals(headMap.tailMap(6).lastKey(), s.headMap(7).tailMap(6).lastKey());
		assertArrayEquals(headMap.tailMap(5).keySet().toArray(), s.headMap(7).tailMap(5).keySet().toArray());
		
		printArr(tailMap.keySet().toArray());
//		printArr(tailMap.headMap(14).keySet().toArray());
//		printArr(tailMap.headMap(14).tailMap(10).headMap(12).keySet().toArray());
//		System.out.println(tailMap.headMap(14).tailMap(10).headMap(12).lastKey());
		printArr(tailMap.subMap(7, 12).subMap(7, 11).entrySet().toArray());
		tempTree.put(11, 1);
		
		System.out.println(tailMap.headMap(12).containsValue(1));
		
//		System.out.println("a " + tailMap.subMap(7,8).lastKey());
//		tailMap.
		
		printArr(tailMap.headMap(12).keySet().toArray());
		s.tailMap(7).headMap(12).put(7, 1);
		tailMap.headMap(12).put(7, 1);
		System.out.println(tailMap.headMap(12).size());
		System.out.println(s.tailMap(7).headMap(12).size());
		System.out.println(s.tailMap(7).toString());
		printArr(tailMap.entrySet().toArray());
//		Iterator<Map.Entry<Integer, Integer>> it = tailMap.headMap(12).entrySet().iterator();
//		while (it.hasNext()) {
//			System.out.println(it.next());
//		}
	}
	
	
}