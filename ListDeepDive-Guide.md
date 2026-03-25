# ListDeepDive.java — Detailed Explanation

> **File:** `ListDeepDive.java`
> **Run:** `javac ListDeepDive.java && java ListDeepDive`
> **Topics:** List interface, ArrayList internals, LinkedList internals, time complexity, iteration safety, real-world patterns, LRU cache, common mistakes, interview prep

---

## Table of Contents

1. [What is a List?](#1-what-is-a-list)
2. [Program Entry Point](#2-program-entry-point)
3. [Section 1 — ArrayList Basics](#3-section-1--arraylist-basics)
4. [Section 2 — ArrayList Internals and Resizing](#4-section-2--arraylist-internals-and-resizing)
5. [Section 3 — Time Complexity Demo](#5-section-3--time-complexity-demo)
6. [Section 4 — LinkedList Basics](#6-section-4--linkedlist-basics)
7. [Section 5 — Memory and Iteration Overhead](#7-section-5--memory-and-iteration-overhead)
8. [Section 6 — LinkedList Time Complexity](#8-section-6--linkedlist-time-complexity)
9. [Section 7 — Decision Matrix](#9-section-7--decision-matrix)
10. [Section 8 — Safe Removal During Iteration](#10-section-8--safe-removal-during-iteration)
11. [Section 9 — Real World: Chat Application](#11-section-9--real-world-chat-application)
12. [Section 10 — LRU Cache Simulation](#12-section-10--lru-cache-simulation)
13. [Section 11 — Performance Thought Exercise](#13-section-11--performance-thought-exercise)
14. [Section 12 — Common Mistakes](#14-section-12--common-mistakes)
15. [Section 13 — Interview Summary and ArrayDeque Bonus](#15-section-13--interview-summary-and-arraydeque-bonus)
16. [Key Takeaways](#16-key-takeaways)

---

## 1. What is a List?

A `List` in Java is an **ordered collection** (also called a sequence). It is defined by the `java.util.List<E>` interface and extended by several concrete implementations.

### Core characteristics

| Property | Behaviour |
|---|---|
| Ordered | Elements maintain the order in which they were inserted |
| Duplicates allowed | The same value can appear multiple times |
| Index-based access | Every element has a position starting from `0` |

### Common List methods

| Method | What it does | Time (ArrayList) |
|---|---|---|
| `add(E e)` | Appends element at end | O(1)* |
| `add(int i, E e)` | Inserts at index `i` | O(n) |
| `get(int i)` | Returns element at index `i` | O(1) |
| `set(int i, E e)` | Replaces element at index `i` | O(1) |
| `remove(int i)` | Removes element at index `i` | O(n) |
| `remove(Object o)` | Removes first occurrence of object | O(n) |
| `size()` | Returns number of elements | O(1) |
| `contains(Object o)` | Returns true if element exists | O(n) |

### Main implementations

```
List<E>  (interface)
  ├── ArrayList   → backed by a resizable array
  ├── LinkedList  → backed by a doubly linked list
  └── Vector      → legacy, synchronized ArrayList (avoid in modern code)
```

---

## 2. Program Entry Point

```java
public static void main(String[] args) {
    section1_ArrayListBasics();
    section2_ArrayListInternals();
    section3_ArrayListTimComplexity();
    // ... 13 sections total
}
```

The `main` method simply calls each section method in order. Each section is **self-contained** — it demonstrates one specific concept, prints its output, and returns. This makes it easy to run just one section during study by commenting out the others.

---

## 3. Section 1 — ArrayList Basics

```java
List<String> fruits = new ArrayList<>();
```

### Why declare as `List<String>` not `ArrayList<String>`?

This is the **program to interface** principle. By declaring the variable as the interface type `List`, you can swap the implementation (e.g., to `LinkedList`) without changing any of the code that uses `fruits`. This is a best practice in Java.

```java
List<String> fruits = new ArrayList<>();  // ✅ flexible — can swap to LinkedList later
ArrayList<String> fruits = new ArrayList<>();  // ❌ locked to ArrayList
```

### Key operations demonstrated

**Adding elements — `add()`**
```java
fruits.add("Apple");    // appends at end → O(1) amortized
fruits.add("Banana");
fruits.add("Mango");
fruits.add("Banana");   // duplicates are allowed — no error
```

The list after these calls: `[Apple, Banana, Mango, Banana]`

**Reading elements — `get(index)`**
```java
fruits.get(1);  // returns "Banana" → O(1) direct array access
```

`get()` is O(1) because ArrayList is backed by an array. Index access in arrays is simply `baseAddress + (index * elementSize)` — one memory operation regardless of list size.

**Replacing elements — `set(index, value)`**
```java
fruits.set(2, "Grapes");  // replaces "Mango" with "Grapes" → O(1)
```

Like `get()`, this is O(1) because it directly overwrites the array slot at the given index.

**Checking existence — `contains()`**
```java
fruits.contains("Apple");   // true  → O(n) linear scan
fruits.contains("Papaya");  // false → O(n) linear scan
```

`contains()` must scan every element from index 0 until it finds a match or exhausts the list. There is no shortcut — it is always O(n).

**Removing by index — `remove(int)`**
```java
fruits.remove(0);  // removes "Apple" → O(n)
```

Removing at index 0 is the worst case. Every element after index 0 must shift one position to the left to fill the gap. For a list of size n, this means n-1 shifts — O(n).

```
Before: [Apple, Banana, Grapes, Banana]
Remove index 0
After:  [Banana, Grapes, Banana]
         ←── all shifted left by 1
```

**Removing by object — `remove(Object)`**
```java
fruits.remove("Banana");  // removes first occurrence → O(n)
```

First scans to find the element (O(n)), then shifts everything after it left (O(n)). Total: O(n).

**Inserting at index — `add(int, E)`**
```java
fruits.add(0, "Strawberry");  // inserts at head → O(n)
```

Every existing element must shift one position to the right to make room. Worst case is inserting at index 0 — all n elements shift. O(n).

```
Before: [Banana, Grapes, Banana]
add(0, "Strawberry")
After:  [Strawberry, Banana, Grapes, Banana]
                     ──────────────── all shifted right by 1
```

---

## 4. Section 2 — ArrayList Internals and Resizing

### Internal structure

ArrayList's internal storage is declared as:

```java
transient Object[] elementData;
```

Two things to note:

**`transient`** means this field is excluded from default Java serialization. ArrayList uses a custom `writeObject()` method to serialize only the actual elements (not the empty slots in the array). This saves space in the serialized output.

**`Object[]`** means all elements are stored as references to `Object`. When you write `List<String>`, the generic type `String` is erased at runtime — internally everything is an `Object[]`. This is called **type erasure**.

### Default capacity and growth formula

```
Default initial capacity = 10

When array is full:
newCapacity = oldCapacity + (oldCapacity >> 1)
```

The bitwise right shift `>> 1` divides by 2. So:

```
newCapacity = oldCapacity + oldCapacity / 2 = 1.5 × oldCapacity
```

### Capacity growth simulation

```java
int capacity = 10;
for (int i = 0; i < 6; i++) {
    int newCapacity = capacity + (capacity >> 1);
    capacity = newCapacity;
}
```

This produces:

```
Initial:  10
Resize:   10 → 15  (added 5 slots)
Resize:   15 → 22  (added 7 slots)
Resize:   22 → 33  (added 11 slots)
Resize:   33 → 49  (added 16 slots)
Resize:   49 → 73  (added 24 slots)
Resize:   73 → 109 (added 36 slots)
```

Each resize allocates a new array, copies all existing elements, then discards the old array. The copy operation is O(n) but happens infrequently — see amortized analysis below.

### Why `add()` is amortized O(1)

Consider adding n elements one by one:

- Most adds: just write to `elementData[size]` and increment `size` → O(1)
- Resize at sizes 10, 15, 22, 33 ...: copy all elements → O(n) but rare

Total copy operations across n adds form a geometric series:
```
copies = 10 + 15 + 22 + 33 + ... ≤ 3n
```

So total work for n adds = n + 3n = 4n = **O(n) total**, meaning **O(1) per add** on average. This is **amortized O(1)**.

### Pre-allocation — avoiding resize entirely

```java
List<Integer> preallocated = new ArrayList<>(10_000);
```

If you know the expected size upfront, pre-allocate. This sets the internal array to size 10,000 immediately — no resizing ever occurs, saving both time and memory churn.

### Trimming unused capacity

```java
((ArrayList<Integer>) preallocated).trimToSize();
```

After populating a list, if you added fewer elements than the capacity, `trimToSize()` shrinks the internal array to exactly `size` elements. This reclaims the unused memory. Useful when building a list during initialization and then keeping it read-only.

---

## 5. Section 3 — Time Complexity Demo

This section uses `System.nanoTime()` to directly measure each operation and confirm the theoretical time complexities in practice.

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30, 40, 50));
```

`List.of(...)` is a Java 9+ factory method that creates an **immutable** list. Passing it to `new ArrayList<>()` creates a **mutable copy** — the ArrayList constructor accepts any Collection.

### get(index) — O(1)

```java
int val = list.get(4);  // accesses last element
```

Even accessing the last element takes the same time as accessing the first. The JVM translates `get(i)` to a direct array offset calculation. No iteration involved.

### add at end — O(1) amortized

```java
list.add(60);  // appends to end
```

If capacity permits, this is a single write + increment. Benchmarks typically show this in single-digit nanoseconds.

### add at index 0 — O(n)

```java
list.add(0, 5);  // inserts at front
```

Every existing element must shift right. For a list of size n this is n memory writes — measurably slower than `add(60)` especially at large sizes.

### remove at index 0 — O(n)

```java
list.remove(0);  // removes first element
```

All elements shift left. Same O(n) cost as inserting at head.

---

## 6. Section 4 — LinkedList Basics

### Internal structure

LinkedList is backed by a **doubly linked list**. Each element is wrapped in a `Node` object:

```java
private static class Node<E> {
    E item;       // the actual data
    Node<E> next; // pointer to the next node
    Node<E> prev; // pointer to the previous node
}
```

The list holds two references:
- `first` → points to the head node
- `last`  → points to the tail node

Memory layout visualization:

```
null ← [prev | "Alice" | next] ↔ [prev | "Bob" | next] ↔ [prev | "Carol" | next] → null
            ↑                                                      ↑
          first (head)                                           last (tail)
```

### Head and tail operations — O(1)

```java
list.addFirst(10);   // updates first pointer + new node's next
list.addFirst(5);    // 5 → 10 → ...
list.addLast(20);    // ... → 10 → 20, updates last pointer
list.addLast(30);    // ... → 20 → 30, updates last pointer
```

These are O(1) because only pointer updates are needed — no shifting, no array resizing. The JVM doesn't need to touch any existing nodes.

```java
int removed = list.removeFirst();  // updates first pointer → O(1)
int removedLast = list.removeLast(); // updates last pointer → O(1)
```

### get(index) — O(n)

```java
list.get(1);  // must traverse from head or tail to reach index 1
```

LinkedList has no array — there's no formula to jump to index `i`. It must walk the chain node by node. As an optimization, Java's LinkedList checks whether the index is closer to the head or tail and traverses from the nearer end, but worst case is still O(n/2) = O(n).

### Peek operations — O(1), non-destructive

```java
list.peekFirst();  // returns head value without removing
list.peekLast();   // returns tail value without removing
```

`peek` returns `null` if the list is empty (unlike `getFirst()` which throws `NoSuchElementException`).

### LinkedList as a Queue

Because LinkedList implements the `Deque` interface, it can serve as a FIFO queue:

```java
Queue<String> messageQueue = new LinkedList<>();
messageQueue.offer("msg1");  // addLast internally → O(1)
messageQueue.poll();          // removeFirst internally → O(1)
```

---

## 7. Section 5 — Memory and Iteration Overhead

### Memory comparison

| Structure | Bytes per element |
|---|---|
| `ArrayList` | ~8 bytes (one object reference in array) |
| `LinkedList` | ~40 bytes (Node object: header 16 + item 8 + next 8 + prev 8) |

For 1 million integers:

```
ArrayList  ≈  8 MB
LinkedList ≈ 40 MB
```

LinkedList uses roughly **5× more memory** per element due to node overhead.

### Cache locality — why ArrayList is faster to iterate

**ArrayList** stores elements in a contiguous block of memory:

```
Memory: [10][20][30][40][50][60][70][80]...
         ↑
         CPU loads this cache line (typically 64 bytes = 8 integers)
         → next 7 elements are already in cache → O(1) access each
```

**LinkedList** stores nodes anywhere in the heap:

```
Memory: [Node@0x1A00] ... [Node@0x8F20] ... [Node@0x3C40] ...
                                                ↑
         Each node could be anywhere in memory
         → each access = potential cache miss → expensive memory fetch
```

Modern CPUs execute a cache hit in 1–4 cycles. A cache miss requires fetching from RAM — 100–300 cycles. For a 100,000 element iteration this adds up dramatically.

### Benchmark code

```java
long start = System.nanoTime();
long sum1 = 0;
for (int val : arrayList) sum1 += val;
long arrayTime = System.nanoTime() - start;
```

The benchmark sums all elements to prevent the JIT compiler from optimizing the loop away (dead code elimination). Using `System.nanoTime()` gives nanosecond resolution.

Typical results on modern hardware:
```
ArrayList  iteration: ~3,000,000 ns
LinkedList iteration: ~12,000,000 ns
→ LinkedList is ~4× slower for iteration
```

---

## 8. Section 6 — LinkedList Time Complexity

### Where LinkedList genuinely wins — head inserts

```java
// O(n²) total — each add(0, i) shifts all existing elements right
for (int i = 0; i < n; i++) arrayList.add(0, i);

// O(n) total — each addFirst is a single pointer update
for (int i = 0; i < n; i++) linkedList.addFirst(i);
```

For 50,000 head inserts:
- ArrayList: O(n²) = 2.5 billion operations
- LinkedList: O(n) = 50,000 operations

This is where LinkedList's theoretical advantage becomes measurable in practice.

### The O(n) surprise for middle operations

A common misconception: "LinkedList insert at middle is O(1) because you just update pointers."

This is only half true. Pointer update is O(1), but **finding the position first costs O(n)**:

```
add(index, element):
  Step 1: traverse to index  → O(n)   ← this dominates
  Step 2: update pointers    → O(1)
  Total:                        O(n)
```

So for middle insert/remove, ArrayList and LinkedList are **both O(n)**. ArrayList's advantage is cache locality — even though it's the same O(n), ArrayList's array shifts are faster in practice due to CPU cache.

---

## 9. Section 7 — Decision Matrix

The decision matrix is printed directly to the console from a text block:

```java
System.out.println("""
    Feature              ArrayList        LinkedList
    ───────────────────────────────────────────────
    Memory layout        Contiguous       Scattered
    Random access        O(1) ✅           O(n) ❌
    ...
    """);
```

Java text blocks (introduced in Java 15) allow multi-line strings with `"""`. Leading whitespace is stripped based on the indentation of the closing `"""`. This is the cleanest way to print formatted tables without string concatenation.

### Summary of when to use each

**Use ArrayList when:**
- You read elements frequently by index
- You iterate over the whole list often
- You mostly append at the end
- Memory efficiency matters
- CPU cache performance matters (always, basically)

**Use LinkedList when:**
- You need O(1) insertions/removals at the head or tail exclusively
- You are implementing a Queue or Deque
- You never need random index access
- You have confirmed via profiling that LinkedList wins

**Reality check:** In practice, `ArrayList` outperforms `LinkedList` in almost every benchmark including cases where theory suggests LinkedList should win, because cache locality beats pointer math at modern CPU speeds.

---

## 10. Section 8 — Safe Removal During Iteration

### Why `ConcurrentModificationException` happens

ArrayList tracks an internal `modCount` (modification count) that increments on every structural change (add, remove, etc.). When you create an iterator via `for (T item : list)`, it captures the current `modCount`. On every call to `next()`, it checks whether `modCount` still matches. If you remove inside the loop, `modCount` changes → the check fails → exception.

### The wrong way

```java
for (Integer val : badList) {
    if (val == 3) badList.remove(val);  // ❌ modifies modCount during iteration
}
// throws: ConcurrentModificationException
```

### Four correct approaches

**Approach 1 — `Iterator.remove()`** — the classic, works everywhere

```java
Iterator<Integer> it = list1.iterator();
while (it.hasNext()) {
    if (it.next() % 2 == 0) it.remove();  // updates modCount safely
}
```

`Iterator.remove()` is the designated safe removal method. It updates `modCount` in sync with the iterator's internal expected count.

**Approach 2 — `removeIf()`** — Java 8+, most readable

```java
list2.removeIf(n -> n % 2 == 0);
```

Internally ArrayList's `removeIf()` uses a bulk operation that avoids shifting on each removal — it marks elements for removal and shifts once at the end. More efficient than repeated `iterator.remove()` for large lists.

**Approach 3 — `stream().filter()`** — functional, returns new list

```java
List<Integer> odds = list3.stream()
                           .filter(n -> n % 2 != 0)
                           .toList();  // Java 16+ unmodifiable list
```

Does not modify the original list. Creates a new list containing only the elements that pass the predicate. Use when you want to keep the original intact.

**Approach 4 — reverse index loop** — useful when index context needed

```java
for (int i = list4.size() - 1; i >= 0; i--) {
    if (list4.get(i) % 2 == 0) list4.remove(i);
}
```

Iterating backwards means removing at index `i` does not affect the indices of elements at `0` to `i-1` — those are already processed. If you iterated forwards, removing at index 3 would make the old index 4 become the new index 3, causing you to skip it.

---

## 11. Section 9 — Real World: Chat Application

This section models a typical chat window to demonstrate which List implementation fits which operation pattern.

### Operations in a chat window

| Operation | Frequency | Best structure |
|---|---|---|
| Append new message | Very high | ArrayList O(1)* |
| Render all messages | High | ArrayList (cache-friendly) |
| Scroll to message N | Moderate | ArrayList O(1) |
| Delete last message | Low | Both O(1) for tail |
| Process notification queue | Variable | LinkedList as Queue |

### ArrayList for the chat window

```java
List<String> chatMessages = new ArrayList<>();
chatMessages.add("[10:01] Alice: Hey!");
```

Appending is the dominant operation — `add()` at end is O(1) amortized. Rendering the chat iterates all messages — ArrayList's cache-friendly layout makes this fast.

### Random access for scroll position

```java
// User scrolled to message N — O(1) with ArrayList
chatMessages.get(2);
```

If users can jump to a specific message (e.g. "scroll to unread"), ArrayList's O(1) index access makes this instant. LinkedList would require traversal.

### LinkedList as a notification queue

```java
Queue<String> messageQueue = new LinkedList<>();
messageQueue.offer("[10:07] Notification: User joined");  // addLast O(1)
messageQueue.poll();  // removeFirst O(1) — FIFO processing
```

For a notification queue where messages arrive at the tail and are processed from the head, LinkedList's O(1) `offer/poll` is appropriate. However, `ArrayDeque` is preferred in production (see Section 13).

---

## 12. Section 10 — LRU Cache Simulation

### What is LRU?

LRU (Least Recently Used) is a cache eviction strategy: when the cache is full and a new item must be added, the item that was accessed least recently is discarded.

### Classic implementation: HashMap + Doubly LinkedList

```
HashMap       → O(1) lookup: key → Node
LinkedList    → O(1) move accessed node to tail (most recent end)
              → O(1) evict head node (least recent end)
```

Combined, this gives O(1) for both `get()` and `put()`.

### Java's built-in LRU: `LinkedHashMap` with `accessOrder=true`

```java
Map<Integer, String> lruCache = new LinkedHashMap<>(CAPACITY, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > CAPACITY;
    }
};
```

The three arguments to `LinkedHashMap`:
- `CAPACITY` — initial capacity of the hash table
- `0.75f` — load factor (resize hash table when 75% full)
- `true` — **access order mode**: on every `get()`, the accessed entry moves to the tail of the iteration order

`removeEldestEntry()` is called after every `put()`. Returning `true` triggers automatic eviction of the head entry (the least recently accessed). Returning `false` keeps the entry.

### Walkthrough of the simulation

```
put(1, Page A)  →  cache: {1}
put(2, Page B)  →  cache: {1, 2}
put(3, Page C)  →  cache: {1, 2, 3}  ← capacity reached

get(1)          →  cache: {2, 3, 1}  ← 1 moved to tail (most recent)

put(4, Page D)  →  cache full, evict head (2) → {3, 1, 4}
```

### Why this matters for LinkedList understanding

The LRU cache is the canonical example that justifies the existence of doubly linked lists. The `prev` pointer is what makes `removeLast()` O(1) — without it, you'd need to traverse to find the second-to-last node every time an eviction occurs.

---

## 13. Section 11 — Performance Thought Exercise

### Scenario A: Read-heavy workload

```java
// 10,000 reads by index from product catalog
for (int i = 0; i < 10_000; i++) readSum += productCatalog.get(i);
```

10,000 O(1) reads with ArrayList = fast. Even if there are occasional O(n) inserts, they are vastly outnumbered by reads. ArrayList is the correct choice for read-heavy workloads like product catalogs, search results, and data grids.

### Scenario B: Queue/Deque pattern

```java
Deque<String> taskQueue = new LinkedList<>();
taskQueue.addLast("Task 1");       // O(1)
taskQueue.addFirst("Priority Task"); // O(1)
taskQueue.pollFirst();               // O(1)
```

If the access pattern is exclusively adding/removing from the ends, LinkedList's O(1) head/tail operations are appropriate. Though `ArrayDeque` is still faster in practice (Section 13).

### Scenario C: Pagination with ArrayList

```java
int page = 2, pageSize = 3;
int from = (page - 1) * pageSize;  // = 3
int to   = Math.min(from + pageSize, catalog.size());  // = 6
catalog.subList(from, to);  // O(1) — backed by same array, no copy
```

`subList()` returns a **view** of the original list (not a copy). It is O(1) to create — it just stores the start and end indices and delegates all reads to the original array. This makes ArrayList ideal for paginated data displays.

---

## 14. Section 12 — Common Mistakes

### Mistake 1 — Using LinkedList for random access

```java
List<Integer> linked = new LinkedList<>();
linked.get(9_999);  // O(n) — traverses 9999 nodes
```

Every `get(index)` call forces LinkedList to walk from the head (or tail if index > size/2) to the target index. For index 9,999 in a 10,000 element list, this traverses 4,999+ nodes. ArrayList's equivalent is a single array dereference.

### Mistake 2 — `List<Integer>` vs `int[]` for numeric data

```java
List<Integer> boxedList = new ArrayList<>(n);
for (int i = 0; i < n; i++) boxedList.add(i);  // autoboxing: int → Integer object
```

Every `int` value added to a `List<Integer>` is **autoboxed** — wrapped in an `Integer` object on the heap. This has two costs:
- Memory: each `Integer` object is ~16 bytes vs 4 bytes for a primitive `int`
- Time: object allocation + garbage collection pressure

```java
int[] primitiveArray = new int[n];
for (int i = 0; i < n; i++) primitiveArray[i] = i;  // no boxing
```

Use `int[]` when you have a large, fixed-size collection of primitives. Use `List<Integer>` when you need List API methods (add, remove, contains) or interoperability with collections.

### Mistake 3 — Removing inside for-each (covered in Section 8)

The `ConcurrentModificationException` case. Use `removeIf()` or `Iterator.remove()`.

### Mistake 4 — `Arrays.asList()` returns a fixed-size list

```java
List<String> fixed = Arrays.asList("A", "B", "C");
fixed.add("D");  // throws UnsupportedOperationException
```

`Arrays.asList()` returns a fixed-size list backed by the original array. You can call `set()` (replacing elements) but not `add()` or `remove()` (changing size). This surprises many developers because the return type is `List<String>`.

Fixes:
```java
// Mutable copy
List<String> mutable = new ArrayList<>(Arrays.asList("A", "B", "C"));

// Immutable list (Java 9+) — explicit about immutability
List<String> immutable = List.of("A", "B", "C");
```

### Mistake 5 — Assuming LinkedList middle insert is fast

```java
linkedList.add(5000, element);  // O(n) — traverse 5000 nodes to get there
```

The pointer update itself is O(1), but finding node 5000 requires traversal. Net complexity: O(n). This is the same as ArrayList for middle operations, but ArrayList is faster in practice due to cache locality.

---

## 15. Section 13 — Interview Summary and ArrayDeque Bonus

### Key interview question answers

**Q: Why is ArrayList faster than LinkedList for most operations?**

Cache locality. ArrayList stores elements in contiguous memory. When the CPU loads element at index 0, it simultaneously loads a cache line of ~64 bytes, meaning elements 1–7 are already in cache. LinkedList nodes are allocated at arbitrary heap addresses — each node access is a potential cache miss requiring a slow memory fetch.

**Q: Why is LinkedList rarely used in practice?**

Despite O(1) head/tail operations on paper, real-world benchmarks show LinkedList losing to ArrayList even for those cases at typical data sizes, because cache miss penalties far outweigh the theoretical pointer-update advantage. Additionally, `ArrayDeque` exists and provides O(1) head/tail with cache-friendly storage.

**Q: What is amortized complexity?**

The average cost per operation across a sequence of n operations, rather than the cost of a single operation in isolation. ArrayList's `add()` is amortized O(1): most adds are O(1), but occasional O(n) resizes are rare enough that the average across all adds is O(1).

**Q: How does ArrayList resizing work?**

When `size == elementData.length`, ArrayList allocates a new array of `oldCapacity + (oldCapacity >> 1)` ≈ 1.5× the current size, copies all existing elements using `Arrays.copyOf()`, then adds the new element. The old array is then eligible for garbage collection.

### Bonus — ArrayDeque vs LinkedList

```java
Deque<Integer> arrayDeque  = new ArrayDeque<>();
Deque<Integer> linkedDeque = new LinkedList<>();

for (int i = 0; i < 50_000; i++) {
    arrayDeque.addFirst(i);
    arrayDeque.pollLast();
}
```

**ArrayDeque** uses an internally resizable circular array. It wraps around the ends:

```
Internal array: [_, _, 5, 4, 3, 2, 1, _, _, _]
                              ↑               ↑
                            head            tail (wraps around)
```

`addFirst` decrements the head index (mod capacity). `addLast` increments the tail index. Both are O(1) with no node allocation, no garbage, and cache-friendly memory access.

Typical results:
```
ArrayDeque  (50,000 head ops): ~2,000,000 ns
LinkedList  (50,000 head ops): ~8,000,000 ns
→ ArrayDeque is ~4× faster even for head/tail operations
```

**Rule:** When you need a Queue, Stack, or Deque — always use `ArrayDeque` over `LinkedList`.

---

## 16. Key Takeaways

### Time complexity quick reference

| Operation | ArrayList | LinkedList |
|---|---|---|
| `get(i)` | **O(1)** | O(n) |
| `add(end)` | **O(1)*** | O(1) |
| `add(head)` | O(n) | **O(1)** |
| `add(middle)` | O(n) | O(n) |
| `remove(head)` | O(n) | **O(1)** |
| `remove(middle)` | O(n) | O(n) |
| `contains` | O(n) | O(n) |
| Iteration | **Fast** (cache) | Slow (pointer chasing) |
| Memory per element | **~8 bytes** | ~40 bytes |

*amortized

### The golden rules

1. **Default to ArrayList** — it wins 90% of real-world benchmarks
2. **Pre-allocate** with `new ArrayList<>(expectedSize)` when size is known
3. **Never use LinkedList for `get(index)`** — it is O(n) traversal
4. **Use `removeIf()` or `Iterator.remove()`** when removing during iteration
5. **Use `ArrayDeque`** instead of `LinkedList` for Queue/Stack/Deque patterns
6. **Use `int[]`** instead of `List<Integer>` for large collections of primitives
7. **Program to the `List` interface**, not the implementation class
8. **Profile before optimizing** — theory and benchmarks sometimes disagree

### When each structure wins

```
Read-heavy workload?         → ArrayList
Pagination / random access?  → ArrayList
Mostly appending at end?     → ArrayList
Queue / FIFO processing?     → ArrayDeque (not LinkedList)
Stack / LIFO processing?     → ArrayDeque (not LinkedList)
Frequent add at head only?   → LinkedList (or ArrayDeque)
LRU Cache internals?         → LinkedHashMap (uses doubly linked list)
Large primitive collections? → int[] / long[] (not List<Integer>)
```
