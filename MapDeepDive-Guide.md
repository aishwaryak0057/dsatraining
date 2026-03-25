# MapDeepDive.java — Detailed Explanation

> **File:** `MapDeepDive.java`
> **Run:** `javac MapDeepDive.java && java MapDeepDive`
> **Topics:** Map interface, HashMap internals, resizing, Java 8 compute methods, TreeMap, NavigableMap, LinkedHashMap, LRU cache, ConcurrentHashMap, CAS, frequency counting, real-world patterns, common mistakes, interview prep

---

## Table of Contents

1. [What is a Map?](#1-what-is-a-map)
2. [Program Entry Point](#2-program-entry-point)
3. [Section 1 — Map Interface Basics](#3-section-1--map-interface-basics)
4. [Section 2 — HashMap Internals](#4-section-2--hashmap-internals)
5. [Section 3 — HashMap Resizing](#5-section-3--hashmap-resizing)
6. [Section 4 — Java 8 Compute Methods](#6-section-4--java-8-compute-methods)
7. [Section 5 — TreeMap Basics](#7-section-5--treemap-basics)
8. [Section 6 — TreeMap Navigation Methods](#8-section-6--treemap-navigation-methods)
9. [Section 7 — TreeMap Custom Comparator](#9-section-7--treemap-custom-comparator)
10. [Section 8 — LinkedHashMap](#10-section-8--linkedhashmap)
11. [Section 9 — ConcurrentHashMap](#11-section-9--concurrenthashmap)
12. [Section 10 — ConcurrentHashMap Internals](#12-section-10--concurrenthashmap-internals)
13. [Section 11 — Frequency Counting](#13-section-11--frequency-counting)
14. [Section 12 — Real World: Session Cache](#14-section-12--real-world-session-cache)
15. [Section 13 — Real World: Grouping Data](#15-section-13--real-world-grouping-data)
16. [Section 14 — Real World: Anagram Detection](#16-section-14--real-world-anagram-detection)
17. [Section 15 — Common Mistakes](#17-section-15--common-mistakes)
18. [Section 16 — Performance Benchmark](#18-section-16--performance-benchmark)
19. [Section 17 — Interview Summary](#19-section-17--interview-summary)
20. [Key Takeaways](#20-key-takeaways)

---

## 1. What is a Map?

A `Map` in Java stores **key → value pairs**. It is defined by `java.util.Map<K,V>` which is **not** a subtype of `Collection` — it has its own interface hierarchy.

### Core characteristics

| Property | Behaviour |
|---|---|
| Keys must be unique | Duplicate key overwrites the existing value |
| Values can duplicate | Multiple keys can map to the same value |
| Key-based retrieval | Access is always by key, not by index |
| Not a Collection | `Map` does not extend `Collection<E>` |

### Main implementations

```
Map<K,V>  (interface)
  ├── HashMap            → hash table,              O(1) avg,    no order
  ├── TreeMap            → Red-Black Tree,           O(log n),    sorted key order
  ├── LinkedHashMap      → HashMap + linked list,    O(1) avg,    insertion order
  ├── ConcurrentHashMap  → thread-safe hash table,   O(1) avg,    no order
  └── Hashtable          → legacy synchronized map   (avoid in new code)
```

### Common Map methods

| Method | Returns | Behaviour |
|---|---|---|
| `put(K, V)` | old value or `null` | Insert or update |
| `get(K)` | value or `null` | Retrieve by key |
| `getOrDefault(K, V)` | value or default | Safe retrieval |
| `remove(K)` | old value or `null` | Remove entry |
| `containsKey(K)` | `boolean` | O(1) for HashMap |
| `containsValue(V)` | `boolean` | O(n) always |
| `putIfAbsent(K, V)` | old value or `null` | Only inserts if key absent |
| `computeIfAbsent(K, fn)` | value | Computes and stores if absent |
| `merge(K, V, fn)` | new value | Merges value with existing |
| `forEach(BiConsumer)` | void | Iterate all entries |
| `entrySet()` | `Set<Entry<K,V>>` | Most efficient iteration |
| `replaceAll(BiFunction)` | void | Update all values in place |

---

## 2. Program Entry Point

```java
public static void main(String[] args) throws InterruptedException {
    section1_MapInterfaceBasics();
    // ... 17 sections total
}
```

`throws InterruptedException` is needed because sections 9 and 10 use `Thread.join()`. Each section is self-contained — comment out all but one to focus on a specific topic during study.

---

## 3. Section 1 — Map Interface Basics

### `put()` returns the OLD value

```java
map.put("Apple",  10);  // returns null  — key was absent
map.put("Apple",  15);  // returns 10    — old value returned, key updated
```

The return value of `put()` is frequently ignored, but it tells you whether the key already existed. If it returns `null`, the key is new. If it returns a non-null value, an existing entry was overwritten.

### `get()` returns null for missing keys — not an exception

```java
map.get("Mango");   // null — key absent, NO exception thrown
```

This means you must be careful not to auto-unbox a `null` return from `get()` when the value type is a primitive wrapper:

```java
// DANGER — NullPointerException if "Mango" absent
int count = map.get("Mango");   // null.intValue() → NPE

// SAFE
int count = map.getOrDefault("Mango", 0);
```

### `getOrDefault()` — safe retrieval with a fallback

```java
map.getOrDefault("Mango", 0);   // returns 0 if "Mango" absent
```

Important: this does **not** store the default in the map. It only returns it locally. See Section 15 (Common Mistakes) for the `computeIfAbsent` alternative.

### `containsValue()` is O(n) — not O(1)

```java
map.containsValue(20);  // scans all entries — O(n)
```

Unlike `containsKey()` which is O(1) via hash lookup, value lookup has no shortcut — every entry must be checked. If you need fast value lookup, maintain a second inverse map `Map<V, K>`.

### Three ways to iterate — `entrySet()` is most efficient

```java
// Approach 1: entrySet() — one lookup per entry ✅ most efficient
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    String k = entry.getKey();
    Integer v = entry.getValue();
}

// Approach 2: keySet() + get() — two lookups per entry ❌ less efficient
for (String k : map.keySet()) {
    Integer v = map.get(k);   // second hash lookup
}

// Approach 3: forEach (Java 8+) — same as entrySet, more readable
map.forEach((k, v) -> { ... });
```

`keySet() + get()` does two hash lookups per entry. `entrySet()` gives you both key and value directly from the same node — one lookup. For large maps this difference is measurable.

---

## 4. Section 2 — HashMap Internals

### The backing array

```java
transient Node<K,V>[] table;
```

`transient` means this field is excluded from default Java serialization. HashMap provides custom `writeObject()`/`readObject()` methods that serialize only the actual key-value pairs, not the empty slots in the array.

### The Node structure

```java
static class Node<K,V> {
    final int    hash;     // cached hashCode — avoids recomputing on resize
    final K      key;      // immutable reference
    V            value;    // mutable — can be updated by put()
    Node<K,V>    next;     // next node in the same bucket (chaining)
}
```

The `hash` field caches the processed hash value so it doesn't need to be recomputed during resizing. The `key` field is `final` — once a node is created, its key cannot change. The `value` field is mutable — `put()` on an existing key updates this field in place.

### How `put("Apple", 10)` works step by step

```
1. Compute raw hashCode:
     h = "Apple".hashCode()        → 63476538

2. Spread the hash (perturbation function):
     hash = h ^ (h >>> 16)
     This XORs the high 16 bits into the low 16 bits.
     Purpose: reduces collisions for keys that differ only in high bits
              (e.g., keys that are multiples of large powers of 2)

3. Compute bucket index:
     index = (table.length - 1) & hash
     For default capacity 16: (16-1) & hash = 15 & hash = last 4 bits of hash

4. Is table[index] empty?
     YES → Create Node(hash, "Apple", 10, null), place at table[index]
     NO  → Walk the linked list at table[index]:
             For each node n:
               if n.hash == hash && n.key.equals("Apple"):
                 → UPDATE: n.value = 10; return old value
               else continue to n.next
             If chain exhausted → APPEND new Node at end of chain

5. Increment size. If size > threshold → resize (see Section 3)
```

### Why the perturbation `h ^ (h >>> 16)`?

HashMap's index formula only uses the **low bits** of the hash (because `(capacity-1) & hash` masks everything above `log₂(capacity)` bits). For default capacity 16, only the last 4 bits matter. Two keys with the same low 4 bits always collide, regardless of their high bits. The spread function mixes the high bits down into the low bits, reducing this pattern.

### Java 8+ treeification

```
Linked list in bucket (when chain length < 8):
  head → [Node A] → [Node B] → [Node C] → null
  lookup: scan chain = O(n per bucket)

Treeified bucket (when chain length ≥ 8 AND table.length ≥ 64):
  head → Red-Black Tree with A, B, C, ...
  lookup: tree traversal = O(log n per bucket)
```

Two conditions must both be true for treeification:
- Chain length ≥ `TREEIFY_THRESHOLD` (8)
- Table size ≥ `MIN_TREEIFY_CAPACITY` (64)

If the table is small (< 64 buckets), HashMap resizes instead of treeifying — adding more buckets is more effective than treeifying when the table is sparse.

When chains shrink back below `UNTREEIFY_THRESHOLD` (6) after removals, the tree is converted back to a linked list.

### Null key handling

```java
map.put(null, 0);   // valid — null key stored in bucket 0
```

HashMap explicitly handles `null` keys: the hash of `null` is defined as 0, so null keys always land in bucket 0. Only one null key is allowed — a second `put(null, v)` overwrites the first.

---

## 5. Section 3 — HashMap Resizing

### When does resize trigger?

```
Condition: size > capacity * loadFactor

Default settings:
  capacity   = 16
  loadFactor = 0.75
  threshold  = 12 (resize when 13th entry is added)
```

### Resize schedule

```
capacity=    16  threshold= 12
capacity=    32  threshold= 24
capacity=    64  threshold= 48
capacity=   128  threshold= 96
capacity=   256  threshold= 192
...
```

Each resize doubles the capacity. After enough resizes, the probability of collision drops because elements are spread across more buckets.

### What happens during a resize

```
1. Allocate new array: newTable = new Node[2 * oldCapacity]

2. For each existing node in oldTable:
   Recompute bucket index using new capacity.
   Result: index is either:
     - Same as before      (when (hash & oldCapacity) == 0)
     - old index + oldCapacity  (when (hash & oldCapacity) != 0)

3. Place node in newTable at computed index.
4. Discard oldTable.
```

The clever insight is that the new index bit is determined by exactly one bit in the hash. So nodes split cleanly into two groups — half stay, half move to `oldIndex + oldCapacity`. No need to recompute full bucket indices from scratch.

### Amortized O(1) despite O(n) resize

A resize copies n elements. But it only happens every time the count doubles. For n total inserts:
```
resize at 12    → copies 12 nodes
resize at 24    → copies 24 nodes
resize at 48    → copies 48 nodes
...
total copies ≈ 12 + 24 + 48 + ... + n ≈ 2n
```
Total work = n inserts + 2n copies = 3n = O(n) total → **O(1) amortized per insert**.

### Pre-sizing to avoid all resizes

```java
int expectedEntries = 1000;
int initialCapacity = (int)(expectedEntries / 0.75) + 1;   // = 1334
Map<String, Integer> map = new HashMap<>(initialCapacity);
```

This sets the initial capacity high enough that no resize will occur for up to 1000 entries. The benchmark in the code typically shows pre-sizing is 1.2–1.5× faster for large maps because it eliminates all array allocation and rehashing overhead.

---

## 6. Section 4 — Java 8 Compute Methods

### The problem they solve

Before Java 8, a common frequency-counting pattern was:

```java
Integer old = map.get(word);
if (old == null) map.put(word, 1);
else             map.put(word, old + 1);
```

This is three lines, two map lookups, and is not atomic. Java 8 introduced compute methods to replace this.

### `computeIfAbsent(K, Function<K, V>)`

```java
grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
```

- Checks if `key` is absent (or maps to null)
- If absent: calls the function to create a value, stores it, returns it
- If present: returns the existing value without calling the function
- **Stores the value in the map** — unlike `getOrDefault`

The `.add(item)` at the end works because `computeIfAbsent` returns the stored list (either newly created or existing), allowing immediate chaining.

### `merge(K, V, BiFunction<V, V, V>)`

```java
freq.merge(word, 1, Integer::sum);
```

- If key absent → `put(word, 1)`
- If key present → `put(word, oldValue + 1)` (via `Integer::sum`)
- If function returns null → `remove(word)`

This is the most concise approach for frequency counting. `Integer::sum` is a method reference equivalent to `(a, b) -> a + b`.

### `compute(K, BiFunction<K, V, V>)`

```java
map.compute(key, (k, v) -> v == null ? 1 : v + 1);
```

Always invoked, whether the key exists or not. `v` is the current value (or `null` if absent). If the function returns `null`, the entry is removed. More flexible than `computeIfAbsent`/`computeIfPresent` when you need to handle both cases.

### `computeIfPresent(K, BiFunction<K, V, V>)`

```java
scores.computeIfPresent("Alice", (k, v) -> v + 10);
```

Only invoked if the key exists with a non-null value. If the function returns null, the entry is removed. Useful for conditional updates — updating a score only if the player exists.

### `replaceAll(BiFunction<K, V, V>)`

```java
scores.replaceAll((name, score) -> score * 2);
```

Transforms every value in the map in place. The function receives both key and old value, returns new value. Equivalent to iterating entrySet and calling `setValue()` on each entry.

### Critical difference: `getOrDefault` vs `computeIfAbsent`

```java
// getOrDefault — does NOT store the default in the map
List<Integer> list = map.getOrDefault("key", new ArrayList<>());
list.add(42);    // adds to local variable only — NOT in map
// map is still empty!

// computeIfAbsent — STORES the computed value in the map
map.computeIfAbsent("key", k -> new ArrayList<>()).add(42);
// map now contains "key" → [42]
```

This is one of the most common mistakes when first using these methods.

---

## 7. Section 5 — TreeMap Basics

### Internal structure

TreeMap is backed by a **Red-Black Tree**. Each entry is a node with five fields:

```java
static final class Entry<K,V> {
    K           key;
    V           value;
    Entry<K,V>  left;
    Entry<K,V>  right;
    Entry<K,V>  parent;
    boolean     color;    // RED or BLACK
}
```

Unlike HashMap's flat array with chains, TreeMap is a pure tree structure. There are no buckets — every operation traverses the tree from root to the appropriate leaf.

### Sorted iteration — always, automatically

```java
treeMap.put(30, "C");
treeMap.put(10, "A");
treeMap.put(50, "E");
// iteration always yields: 10→A, 20→B, 30→C, 40→D, 50→E
```

TreeMap's internal in-order traversal visits nodes in ascending key order. No sorting step is needed — the tree maintains sorted order as a structural invariant at all times.

### `put()` still returns the old value

```java
String old = treeMap.put(20, "UPDATED");
// old == "B" — the previous value for key 20
```

Same semantics as HashMap — `put()` returns null for new keys, returns the previous value for existing keys.

### `firstKey()` / `lastKey()` — O(log n)

```java
treeMap.firstKey();   // leftmost node in tree = minimum key
treeMap.lastKey();    // rightmost node in tree = maximum key
```

Both traverse the tree to the leftmost or rightmost node — O(log n) for a balanced tree of height log n.

### `firstEntry()` / `lastEntry()` — returns key AND value

```java
Map.Entry<Integer, String> min = treeMap.firstEntry();
min.getKey();    // minimum key
min.getValue();  // value for minimum key
```

More convenient than `firstKey()` + `get(firstKey())` — avoids a second tree traversal.

---

## 8. Section 6 — TreeMap Navigation Methods

### NavigableMap interface

TreeMap implements `NavigableMap` which extends `SortedMap`. This gives access to powerful navigation operations not available on HashMap.

### Key navigation methods

```java
TreeMap<Integer, String> prices = {100, 300, 500, 700, 1000};

prices.floorKey(450)    // 300 — largest key ≤ 450
prices.ceilingKey(450)  // 500 — smallest key ≥ 450
prices.lowerKey(500)    // 300 — strictly less than 500
prices.higherKey(500)   // 700 — strictly greater than 500
```

`floor` and `ceiling` are inclusive of the target value; `lower` and `higher` are exclusive. All return `null` if no such key exists.

### Real-world use: pricing tier lookup

```java
// Find what pricing tier a customer's budget qualifies for
int budget = 450;
Integer tier = prices.floorKey(budget);  // 300 — highest tier they can afford
```

This O(log n) operation replaces a linear scan through a sorted list.

### Range views — backed by the same tree

```java
prices.headMap(500)          // {100=Budget, 300=Mid}          — keys < 500
prices.tailMap(500)          // {500=Premium, 700=Luxury, ...} — keys ≥ 500
prices.subMap(300, 700)      // {300=Mid, 500=Premium}         — 300 ≤ k < 700
prices.descendingMap()       // same map in reverse key order
```

These are **views**, not copies. They share the underlying tree. Modifications to a view (add/remove) modify the original map. Accessing an entry outside the view's range throws `IllegalArgumentException`.

### `pollFirstEntry()` and `pollLastEntry()`

```java
Map.Entry<Integer, String> min = treeMap.pollFirstEntry(); // remove AND return min
Map.Entry<Integer, String> max = treeMap.pollLastEntry();  // remove AND return max
```

These atomically retrieve and remove the minimum/maximum entry. Useful for priority-queue-style processing: always process the cheapest, oldest, or highest-priority item next.

---

## 9. Section 7 — TreeMap Custom Comparator

### Comparator governs both ordering AND equality

```java
TreeMap<String, Integer> caseInsensitive =
    new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

caseInsensitive.put("Apple", 1);
caseInsensitive.put("apple", 99);  // same key — "Apple" updated to 99
caseInsensitive.size();   // 1, not 2
```

If `comparator.compare(k1, k2) == 0`, TreeMap treats them as the same key. This is the same behaviour as TreeSet — the Comparator defines equality for the tree, not `equals()`.

### Multi-level sort with `thenComparing`

```java
TreeMap<String, Integer> byLength = new TreeMap<>(
    Comparator.comparingInt(String::length)
              .thenComparing(Comparator.naturalOrder())
);
```

`thenComparing` is only invoked when the primary comparator returns 0. If two strings have the same length, they are differentiated alphabetically. Without `thenComparing`, strings of equal length would be considered duplicate keys — only one would survive in the TreeMap.

### Absolute value ordering

```java
TreeMap<Integer, String> absOrder =
    new TreeMap<>(Comparator.comparingInt(Math::abs));

absOrder.put(-5, "neg5");
absOrder.put( 2, "pos2");
absOrder.put(-1, "neg1");
absOrder.put( 4, "pos4");
// Order: -1 → 2 → 4 → -5 (by |value|)
```

`Math::abs` is a method reference. `Comparator.comparingInt` extracts an `int` key from each element and compares those ints. Note: -5 and 5 would map to the same absolute value (5) and thus be considered the same key — only one would be stored.

---

## 10. Section 8 — LinkedHashMap

### How LinkedHashMap works

LinkedHashMap extends HashMap and adds a **doubly linked list** that threads through all entries. Every entry has two extra pointers:

```
Hash table:  [bucket3:key1] [bucket7:key2] [bucket1:key3] ...

Linked list: [key1] ↔ [key2] ↔ [key3] ↔ ...
              ↑ first (head)              ↑ last (tail)
```

The hash table provides O(1) key lookups. The linked list provides ordered iteration.

### Mode 1: Insertion order (default)

```java
Map<String, String> config = new LinkedHashMap<>();
config.put("host",    "localhost");
config.put("port",    "8080");
config.put("timeout", "30s");
// Iterates: host, port, timeout — insertion order always preserved
```

Unlike HashMap where iteration order is arbitrary, LinkedHashMap always iterates in the order entries were first inserted. Re-inserting an existing key does not change its position in the iteration order.

### Mode 2: Access order — the LRU cache

```java
Map<Integer, String> lruCache = new LinkedHashMap<>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > MAX_CAPACITY;
    }
};
```

The three constructor arguments:
- `16` — initial capacity of the hash table
- `0.75f` — load factor
- `true` — **access order mode**

In access-order mode, every `get()` and `put()` moves the accessed entry to the **tail** of the linked list. The **head** always holds the least-recently-used entry.

`removeEldestEntry()` is a hook called after every `put()`. When it returns `true`, the head entry (the LRU entry) is automatically evicted. This gives you a bounded LRU cache in about 5 lines.

### Access-order simulation walkthrough

```
put(1, Page A) → list: [1]
put(2, Page B) → list: [1, 2]
put(3, Page C) → list: [1, 2, 3]   ← capacity reached

get(1)         → list: [2, 3, 1]   ← 1 accessed → moved to tail
put(4, Page D) → evicts head (2)   ← 2 is LRU
               → list: [3, 1, 4]
```

---

## 11. Section 9 — ConcurrentHashMap

### Why HashMap is not safe in multi-threaded code

HashMap has three failure modes under concurrent access:

**Lost updates:** Two threads read the same old value and both write a new value based on it — one write is lost.

**Infinite loop (Java 7):** During a resize, two threads simultaneously rehashing could create a circular reference in the linked list, causing `get()` to loop forever.

**ConcurrentModificationException:** One thread modifies the map while another iterates it.

### Why not `Hashtable` or `Collections.synchronizedMap`?

```java
// Hashtable — every method is synchronized on the whole table
public synchronized V get(Object key) { ... }
public synchronized V put(K key, V value) { ... }
```

Only one thread can execute any operation at any time — readers block writers and vice versa. This is a single bottleneck regardless of how many cores you have.

`Collections.synchronizedMap()` wraps any map with `synchronized(mutex)` on every method — same problem.

### How ConcurrentHashMap solves it (Java 8+)

```
Empty bucket insert:   CAS operation — no lock at all
                        if (CAS(table[index], null, newNode)) → done

Non-empty bucket insert: synchronized(bucket.head) — locks only this bucket
                          Other buckets operate freely in parallel

Reads:                  Lock-free — use volatile reads
                        No synchronization needed for get()
```

**CAS (Compare-And-Swap)** is a single CPU instruction:
```
CAS(address, expected, newValue):
  atomically:
    if *address == expected:
      *address = newValue
      return true
    else:
      return false
```

If two threads both try to insert into an empty bucket simultaneously, only one CAS succeeds. The loser retries, now seeing a non-empty bucket, and falls back to the `synchronized(head)` path.

### Why null is not allowed in ConcurrentHashMap

```java
chm.get("key");   // returns null
// Ambiguous: does "key" not exist, or does it map to null?
```

In a single-threaded HashMap, you can follow up with `containsKey()` to disambiguate. In a concurrent context, the map state could change between `get()` and `containsKey()` — making the check unreliable. ConcurrentHashMap eliminates the ambiguity entirely by disallowing null, so `get()` returning null always means "key absent."

### Thread-safety demonstration

```java
// 5 threads × 10,000 inserts each = 50,000 expected entries

// HashMap (unsafe) — actual size < 50,000 due to lost updates
Map<Integer, Integer> unsafeMap = new HashMap<>();

// ConcurrentHashMap (safe) — actual size == 50,000 always
ConcurrentHashMap<Integer, Integer> safeMap = new ConcurrentHashMap<>();
```

The benchmark in the code proves this: HashMap's final size is less than expected (entries lost due to race conditions), while ConcurrentHashMap's size matches exactly.

### Atomic increment with `computeIfAbsent` + `AtomicInteger`

```java
counters.computeIfAbsent("hits", k -> new AtomicInteger(0)).incrementAndGet();
```

`computeIfAbsent` is atomic in ConcurrentHashMap — it guarantees that only one `AtomicInteger` is created and stored, even under concurrent access. `incrementAndGet()` on `AtomicInteger` is itself a CAS-based atomic operation. Combined, this is a correct lock-free counter.

---

## 12. Section 10 — ConcurrentHashMap Internals

### CAS — Compare-And-Swap

CAS is not a Java method — it is a single CPU instruction (`CMPXCHG` on x86). Java exposes it via `sun.misc.Unsafe` (used internally by ConcurrentHashMap) and via `java.util.concurrent.atomic.*` classes (exposed to application code).

CAS enables **lock-free** algorithms: instead of acquiring a lock before modifying shared state, a thread reads the current value, computes the new value, then atomically swaps — but only if the value hasn't changed since it was read. If it has changed (another thread modified it), the CAS fails and the thread retries. This is called **optimistic concurrency**.

### Bulk operations

```java
// Sequential (parallelismThreshold = Long.MAX_VALUE → never parallel)
map.forEach(Long.MAX_VALUE, (k, v) -> System.out.println(k + "=" + v));

// Parallel (parallelismThreshold = 1 → always parallel)
long sum = map.reduceValues(1, Integer::sum);
```

The `parallelismThreshold` parameter controls when parallel execution kicks in:
- If `map.size() < threshold` → sequential execution
- If `map.size() >= threshold` → splits work across `ForkJoinPool.commonPool()`

Setting threshold to `1` forces parallelism regardless of map size. Setting to `Long.MAX_VALUE` forces sequential. A reasonable production value is something like `1000` — parallel only when the map is large enough to benefit.

### `size()` is approximate — use `mappingCount()`

Under concurrent writes, `size()` may return a slightly stale count because ConcurrentHashMap tracks the count using a distributed counter (similar to `LongAdder`) to avoid contention. `mappingCount()` returns a `long` and is more semantically correct for large maps (where `int` would overflow).

---

## 13. Section 11 — Frequency Counting

### Four approaches — all O(n)

**Approach 1 — `getOrDefault`:** most readable, works in Java 7+
```java
map.put(w, map.getOrDefault(w, 0) + 1);
```
Two hash lookups (one for get, one for put). Slightly less efficient than merge.

**Approach 2 — `merge`:** most concise for Java 8+
```java
map.merge(w, 1, Integer::sum);
```
Single atomic read-compute-write. `Integer::sum` is `(a, b) -> a + b`. If the key is absent, stores 1. If present, adds 1 to existing. This is the idiomatic Java 8+ approach.

**Approach 3 — `compute`:** explicit control
```java
map.compute(w, (k, v) -> v == null ? 1 : v + 1);
```
Explicit null check for first occurrence. More verbose but makes the logic explicit — useful when the update logic is more complex than simple addition.

**Approach 4 — Streams + `groupingBy`:**
```java
Map<String, Long> freq = Arrays.stream(words)
    .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
```
Returns `Long` (not `Integer`). Clean when working in a stream pipeline. Less efficient for simple cases due to stream overhead.

### Sorting frequency results

```java
freq.entrySet().stream()
    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
    .forEach(e -> System.out.printf("  %-8s → %d%n", e.getKey(), e.getValue()));
```

`Map.Entry.comparingByValue()` creates a comparator that compares entries by their values. `.reversed()` flips it for descending order. The type witness `<String, Integer>` is needed to help the compiler infer generic types in this static method call.

---

## 14. Section 12 — Real World: Session Cache

### Design decisions for session caching

| Requirement | Solution |
|---|---|
| O(1) session lookup | `ConcurrentHashMap.get()` |
| Thread safety (concurrent requests) | `ConcurrentHashMap` |
| Session expiry | `removeIf` on `entrySet()` |
| Logout / invalidation | `remove(sessionToken)` |

### Using Java records for value objects

```java
record UserSession(String userId, String role, long expiresAt) {}
```

Records are ideal for Map values:
- Immutable — cannot be accidentally mutated
- Auto-generates `equals()`, `hashCode()`, `toString()`
- Compact syntax

### Efficient expiry cleanup

```java
sessionCache.entrySet().removeIf(
    e -> e.getValue().expiresAt() < System.currentTimeMillis()
);
```

`removeIf` on `entrySet()` is the cleanest way to conditionally remove multiple entries. It uses the iterator internally and calls `iterator.remove()` — safe from `ConcurrentModificationException`. On `ConcurrentHashMap`, this is weakly consistent but correct.

---

## 15. Section 13 — Real World: Grouping Data

### The grouping pattern

```java
// computeIfAbsent approach — explicit, good for complex mutations
byDept.computeIfAbsent(employee.dept(), k -> new ArrayList<>())
      .add(employee);
```

This is the most common use of `computeIfAbsent`. The function `k -> new ArrayList<>()` runs only when the department key is first encountered, creating a new list. All subsequent employees for the same department find the list already stored and simply `add()` to it.

### `Collectors.groupingBy` — the stream equivalent

```java
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept));
```

`groupingBy` is declarative — you describe what to group by, not how to do it. The downstream collector defaults to `Collectors.toList()`. You can chain a second downstream collector for aggregation:

```java
// Average salary per department
Collectors.groupingBy(Employee::dept, Collectors.averagingInt(Employee::salary))

// Count per department
Collectors.groupingBy(Employee::dept, Collectors.counting())

// Sum per department  
Collectors.groupingBy(Employee::dept, Collectors.summingInt(Employee::salary))
```

### `Collectors.partitioningBy` — boolean split

```java
Map<Boolean, List<Employee>> partitioned = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.salary() >= 100_000));

partitioned.get(true)   // employees with salary ≥ 100k
partitioned.get(false)  // employees with salary < 100k
```

`partitioningBy` is a special case of `groupingBy` where the key is always `Boolean`. It always returns a map with exactly two entries: `true` and `false`.

---

## 16. Section 14 — Real World: Anagram Detection

### Algorithm

```
For each word:
  1. Sort characters alphabetically  → "eat" → "aet"
  2. Use sorted string as map key
  3. All words with same sorted signature are anagrams
```

This works because any two anagrams, when sorted, produce the same character sequence.

```java
char[] chars = word.toCharArray();
Arrays.sort(chars);                   // O(k log k) where k = word length
String signature = new String(chars);
anagramGroups.computeIfAbsent(signature, k -> new ArrayList<>()).add(word);
```

Total time complexity: O(n × k log k) where n = number of words, k = max word length.

### Two-Sum using HashMap

```java
Map<Integer, Integer> seen = new HashMap<>(); // value → index
for (int i = 0; i < nums.length; i++) {
    int complement = target - nums[i];
    if (seen.containsKey(complement)) {
        // found: seen[complement] and i are the pair
    }
    seen.put(nums[i], i);
}
```

The HashMap transforms a naive O(n²) nested loop solution into O(n). For each element, compute what value would complete the pair (the complement), then check if that complement has been seen before — an O(1) lookup. This pattern appears in many array/string interview problems.

---

## 17. Section 15 — Common Mistakes

### Mistake 1 — Mutable keys

```java
List<Integer> key = new ArrayList<>(List.of(1, 2, 3));
map.put(key, "value");    // stored in bucket based on key.hashCode() = [1,2,3] hash

key.add(4);               // key.hashCode() now = [1,2,3,4] hash — different bucket!
map.get(key);             // looks in wrong bucket → null (entry is lost)
```

The entry still exists in the map — it's just in the bucket for the old hash. It can never be retrieved or removed by the normal API. This is a memory leak and a correctness bug. Always use immutable keys: `String`, `Integer`, `Long`, `UUID`, or Java records.

### Mistake 2 — ConcurrentModificationException

```java
// WRONG — structural modification during iteration
for (String k : map.keySet()) {
    if (condition) map.remove(k);   // modifies the map's modCount → CME
}

// CORRECT — removeIf on entrySet
map.entrySet().removeIf(e -> condition(e));

// CORRECT — iterator with explicit remove
Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
while (it.hasNext()) {
    if (condition(it.next())) it.remove();  // safe — uses iterator's own remove
}
```

### Mistake 3 — `getOrDefault` does not store the default

```java
// WRONG — default list created locally, not stored in map
List<String> list = map.getOrDefault("key", new ArrayList<>());
list.add("item");  // lost — not in the map

// CORRECT — computeIfAbsent stores the created value
map.computeIfAbsent("key", k -> new ArrayList<>()).add("item");
```

### Mistake 4 — `Map.of()` is immutable

```java
Map<String, Integer> m = Map.of("a", 1, "b", 2);
m.put("c", 3);   // UnsupportedOperationException

// Fix
Map<String, Integer> mutable = new HashMap<>(Map.of("a", 1, "b", 2));
```

`Map.of()` (Java 9+) also:
- Throws `NullPointerException` if any key or value is null
- Throws `IllegalArgumentException` for duplicate keys
- Has no guaranteed iteration order

### Mistake 5 — ConcurrentHashMap null prohibition

```java
chm.put(null, "value");   // NullPointerException
chm.put("key", null);     // NullPointerException
```

Use a sentinel object (e.g., `Optional.empty()` or a custom `ABSENT` constant) if you need to represent "no value" in a ConcurrentHashMap.

---

## 18. Section 16 — Performance Benchmark

### What is measured

500,000 random integer inserts into each Map implementation, followed by 100,000 random `containsKey()` lookups.

### Expected results

**Insert:**
```
HashMap:           fastest    — O(n) total
LinkedHashMap:     ~same      — tiny overhead for linked list pointer updates
ConcurrentHashMap: ~1.1-1.3x  — volatile writes + CAS overhead
TreeMap:           slowest    — O(n log n) total, ~log₂(500000) ≈ 19 comparisons per insert
```

**Lookup:**
```
HashMap:   O(1) per lookup   → fastest
TreeMap:   O(log n) per lookup → ~5-15x slower at n=500,000
```

### When TreeMap's overhead is worth it

TreeMap is appropriate when:
- You genuinely need to iterate keys in sorted order frequently
- You need range queries (`headMap`, `tailMap`, `subMap`)
- You need floor/ceiling/lower/higher navigation
- The data set is small enough that O(log n) vs O(1) is irrelevant

For pure key-value storage with no ordering requirements, HashMap is always the right choice.

---

## 19. Section 17 — Interview Summary

### Key interview answers

**Q: Why is HashMap average O(1)?**

`hashCode()` computes a bucket index in O(1) — a few arithmetic operations. In the average case, with a good hash function and reasonable load factor, each bucket contains 0–1 entries. A single array access + at most one `equals()` comparison → O(1). Amortized O(1) for puts because resizing is infrequent.

**Q: Why worst case O(n) (or O(log n) in Java 8+)?**

If all keys collide into one bucket (same hashCode, all different by `equals()`), every operation traverses the entire chain in that bucket — O(n). Java 8+ caps this at O(log n) by treeifying any bucket with ≥ 8 entries. This defends against HashDoS attacks.

**Q: Why did Java 8 introduce tree bins?**

HashDoS (Hash Denial of Service) attacks: an adversary crafts a request with many keys that share the same hashCode, deliberately causing one bucket to grow to O(n) length, making every HashMap operation O(n) and slowing the server to a halt. Tree bins cap the attack impact at O(log n).

**Q: `synchronizedMap` vs `ConcurrentHashMap`?**

| | `synchronizedMap` | `ConcurrentHashMap` |
|---|---|---|
| Lock granularity | Full map | Per-bucket (writes), lock-free (reads) |
| Read throughput | Blocked by writers | Unlimited (lock-free) |
| Iteration | Must manually sync | Weakly consistent (no CME) |
| Null keys/values | Allowed | Not allowed |
| Use case | Legacy, simple needs | High-concurrency production code |

**Q: When to use which Map?**

```
Fast lookup, no order, single-threaded  → HashMap
Sorted keys or range queries            → TreeMap
Insertion order iteration               → LinkedHashMap
LRU cache                               → LinkedHashMap(accessOrder=true)
Multi-threaded access                   → ConcurrentHashMap
Thread-safe + sorted                    → Collections.synchronizedSortedMap(new TreeMap<>())
                                           or external locking around TreeMap
```

---

## 20. Key Takeaways

### Time complexity quick reference

| Operation | HashMap | LinkedHashMap | TreeMap | ConcurrentHashMap |
|---|---|---|---|---|
| `put(k, v)` | O(1) avg | O(1) avg | O(log n) | O(1) avg |
| `get(k)` | O(1) avg | O(1) avg | O(log n) | O(1) avg |
| `remove(k)` | O(1) avg | O(1) avg | O(log n) | O(1) avg |
| `containsKey` | O(1) avg | O(1) avg | O(log n) | O(1) avg |
| `containsValue` | O(n) | O(n) | O(n) | O(n) |
| Iteration order | None | Insertion | Sorted | None |
| Null keys | ✅ (one) | ✅ (one) | ❌ | ❌ |
| Null values | ✅ | ✅ | ✅ | ❌ |
| Thread-safe | ❌ | ❌ | ❌ | ✅ |

### The golden rules

1. **Default to `HashMap`** — fastest for single-threaded, unordered use
2. **Use `TreeMap`** only when sorted keys or range queries are needed
3. **Use `LinkedHashMap`** for insertion-order iteration or LRU cache
4. **Use `ConcurrentHashMap`** for any multi-threaded map access
5. **Never use `Hashtable`** in new code — `ConcurrentHashMap` replaces it
6. **Always override `hashCode()` AND `equals()`** together on custom key objects
7. **Use immutable keys** — `String`, `Integer`, `Long`, `UUID`, or Java records
8. **Pre-size HashMap** with `new HashMap<>(expectedSize / 0.75 + 1)` when size is known
9. **`getOrDefault` does not store** — use `computeIfAbsent` when you need the default stored
10. **`Map.of()` is immutable** — wrap in `new HashMap<>()` for a mutable copy
11. **`entrySet()` iteration** is more efficient than `keySet()` + `get()`
12. **`computeIfAbsent` is the correct pattern** for grouping (map of lists)
13. **`merge` is the most concise** for frequency counting / aggregation
14. **Null in ConcurrentHashMap** → always means key absent (never ambiguous)
15. **`containsValue` is O(n)** — maintain an inverse map if fast value lookup is needed
