import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/* 
 * Initially I used LinkedHashMap but soon realized this is a mistake 
 * for a couple of reasons. First, you cannot access the elements 
 * inside the map differently for peek and get. More importantly, 
 * the benefit that the entries can be accessed in a particular order
 * thanks to the doubly linked list is irrelevant because when we dump, 
 * we do it in alphabetical order. In other words, we need to sort 
 * every time dump is called or keep another data structure like TreeMap. 
 * 
 * I proceeded to keep two data structure: a good old HashMap, and a queue,
 * implemented with a LinkedList. Once our cache is full, we dequeue before
 * queuing a new entry. Problem is set() on elements are in the middle of the
 * queue. If we use the HashMap's value to be the Node in the queue, 
 * access is now O(1), but we need a way to requeue in O(1) time as well.
 * Oracle's LinkedList implementation does not allow this; we must iterate
 * until we find the first occurrence of a particular node. 
 * 
 * It gets messy at this point because InterviewStreet only allows one class,
 * but I copied and pasted a Queue I implemented long ago, with requeue().
 * Our HashMap will contain the reference to corresponding node in the Queue
 * and the nodes will contain the value. This allows O(1) access as well as
 * O(1) write times and we can leave the queue alone when we call peek().
 * 
 * The last major decision was to decide whether to keep a third data structure,
 * TreeMap, so that dump() goes from O(n log n) to O(n). I decided to only 
 * create TreeMap when we dump and disregard it for two reasons: 1) this is a 
 * cache, and I expect under normal circumstances, we wouldn't dump too often.
 * 2) Maintaining the TreeMap is amortized O(n log n) operation because insertion
 * would take O(log n). Not significantly better as long as dump isn't called all the time.
 * 
 * Well, as test cases on InterviewStreet would have it, it seems last test case 
 * calls dump quite frequently.
 */

public class Solution {
	static class LRU {
		int size;
		int entries;
		HashMap<String, Node> cache;
		Queue q;
		TreeSet<Node> ts;

		LRU(int bound) {
			size = bound;
			cache = new HashMap<String, Node>();
			q = new Queue();
			ts = new TreeSet<Node>();
		}

		void bound(int bound) {
			int i = entries - bound;
			while (i > 0) {
				Node n = q.dequeue();
				
				if (n == null) { break;	} // Nothing being returned, queue's empty
				else {
					cache.remove(n.key);
				}
				--i;
			}
			size = bound;
			entries = entries > size ? size : entries;
		}

		void set(String key, String value) {
			if (cache.containsKey(key)) {
				Node requeueMe = cache.get(key);
				requeueMe.value = value; // Still have to assign the new value
				q.requeue(requeueMe);
			}
			else {
				Node n = new Node(key, value);
				cache.put(key, n);
				q.enqueue(n);
				
				if (entries >= size) {
					cache.remove(q.dequeue().key); // Dequeue and remove the entry from the cache
				} else {
					++entries;
				}
			}
		}

		// Leaving the printing to the main loop because that's how
		// I would implement the LRU class in other circumstances
		String get(String key) {
			String ret = null;
			Node requeueMe = cache.get(key);
			
			if (requeueMe != null) { // Assumes input always give at least 1 char for value
				q.requeue(requeueMe);
				ret = requeueMe.value;
			}
			
			return ret;
		}

		// Likewise, as for get(), we leave the printing to main
		// and return the String value
		String peek(String key) {
			Node n = cache.get(key);
			return n != null ? n.value : null;
		}

		void dump() {
			TreeSet<Node> printMe = q.returnSorted();
			
			for (Node n : printMe) {
				System.out.println(n.key + " "+ n.value);
			}
		}  

		static class Queue {
			private Node head;
			private Node tail;

			Queue() {
				head = null;
				tail = null;
			}

			void enqueue(Node node) {
				if (head == null) {
					head = node;
				} else {
					node.prev = tail;
					tail.next = node;
				}
				node.next = null;
				tail = node;
			}

			Node dequeue() {
				try {
					Node ret = head;
					head = head.next;
					return ret;
				} catch (NullPointerException ex) {
					// If queue is empty, accessing head.next will cause problems
					return null;
				}
			}
			
			void requeue(Node requeueMe) {
				// For tail, you need not do anything; it's already at the back
				if (requeueMe == tail) { return; }
				if (requeueMe == head) {
					enqueue(dequeue()); // Moves the current head to the back
				} else { // Everything not the head or the tail
					requeueMe.prev.next = requeueMe.next;
					requeueMe.next.prev = requeueMe.prev;
					this.enqueue(requeueMe);
				}
			}
			
			TreeSet<Node> returnSorted() {
				TreeSet<Node> ts = new TreeSet<Node>();
				
				Node tmp = this.head;
				while (tmp != null) {
					ts.add(tmp);
					tmp = tmp.next;
				}
				
				return ts;
			}
		}
		
		// Many OOP principles are being violated here for brevity
		static class Node implements Comparable<Node> {
			String key;
			String value;
			Node next; // Node
			Node prev; 

			Node (String key, String value) {
				this.key = key;
				this.value = value;
				this.next = null;
				this.prev = null;
			}

			@Override
			public int compareTo(Node o) {
				return key.compareTo(o.key);
			}
		}
	}

public static void main(String[] args) {
	int number_of_commands;
	String[] line;
	String command;

	try {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		LRU lru = null;
		number_of_commands = Integer.parseInt(br.readLine());

		for ( int i = 0; i < number_of_commands; ++i) {
			line = br.readLine().split(" ");
			command = line[0];

			if (command.equalsIgnoreCase("BOUND")) {
				if (lru == null) {
					lru =  new LRU(Integer.parseInt(line[1]));
				}
				else {
					lru.bound(Integer.parseInt(line[1]));
				}
			}
			else if (command.equalsIgnoreCase("SET")) {
				lru.set(line[1], line[2]);
			} 
			else if (command.equalsIgnoreCase("GET")) {
				String output = lru.get(line[1]);
				System.out.println(output != null ? output : "NULL"); // In case it's case sensitive
			} 
			else if (command.equalsIgnoreCase("PEEK")) {
				String output = lru.peek(line[1]);
				System.out.println(output != null ? output : "NULL"); // In case it's case sensitive
			}
			else if (command.equalsIgnoreCase("DUMP")) {
				lru.dump();
			}
		}
		br.close();
	}
	catch (java.io.IOException ex) {
		System.out.println(ex);
	} 
}
}