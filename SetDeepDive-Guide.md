# SetDeepDive.java — Detailed Explanation

> **File:** `SetDeepDive.java`
> **Run:** `javac SetDeepDive.java && java SetDeepDive`
> **Topics:** Set interface, HashSet internals, hashCode/equals contract, collisions, TreeSet, Red-Black Tree, LinkedHashSet, set operations, real-world patterns, common mistakes, interview prep

---

## Table of Contents

1. [What is a Set?](#1-what-is-a-set)
2. [Program Entry Point](#2-program-entry-point)
3. [Section 1 — Set Interface Basics](#3-section-1--set-interface-basics)
4. [Section 2 — HashSet Internals](#4-section-2--hashset-internals)
5. [Section 3 — hashCode() and equals() Contract](#5-section-3--hashcode-and-equals-contract)
6. [Section 4 — Collisions and Bucket Chaining](#6-section-4--collisions-and-bucket-chaining)
7. [Section 5 — TreeSet Basics](#7-section-5--treeset-basics)
8. [Section 6 — TreeSet with Custom Objects](#8-section-6--treeset-with-custom-objects)
9. [Section 7 — LinkedHashSet](#9-section-7--linkedhashset)
10. [Section 8 — Set Operations](#10-section-8--set-operations)
11. [Section 9 — Real World: Duplicate Removal](#11-section-9--real-world-duplicate-removal)
12. [Section 10 — Real World: Leaderboard](#12-section-10--real-world-leaderboard)
13. [Section 11 — Real World: Unique Visitor Tracking](#13-section-11--real-world-unique-visitor-tracking)
14. [Section 12 — Custom Comparator Patterns](#14-section-12--custom-comparator-patterns)
15. [Section 13 — Common Mistakes](#15-section-13--common-mistakes)
16. [Section 14 — Performance Benchmark](#16-section-14--performance-benchmark)
17. [Section 15 — Interview Summary](#17-section-15--interview-summary)
18. [Key Takeaways](#18-key-takeaways)

---

## 1. What is a Set?

A `Set` in Java models the mathematical concept of a set — a group of distinct elements. It is defined by the `java.util.Set<E>` interface which extends `java.util.Collection<E>`.

### Core characteristics

| Property | Behaviour |
|---|---|
| No duplicates | Two elements where `e1.equals(e2)` cannot both exist |
| May or may not be ordered | Depends on implementation |
| At most one null | HashSet/LinkedHashSet allow null; TreeSet does not |

### Main implementations

```
Set<E>  (interface)
  ├── HashSet        → HashMap internally,   O(1) avg,    no order
  ├── TreeSet        → Red-Black Tree,        O(log n),    sorted order
  └── LinkedHashSet  → HashMap + LinkedList,  O(1) avg,    insertion order
```

### Common Set methods

| Method | Returns | Meaning |
|---|---|---|
| `add(E e)` | `boolean` | `true` if added, `false` if duplicate |
| `remove(Object o)` | `boolean` | `true` if removed, `false` if not found |
| `contains(Object o)` | `boolean` | `true` if element exists |
| `size()` | `int` | Number of elements |
| `isEmpty()` | `boolean` | `true` if no elements |
| `addAll(Collection c)` | `boolean` | Union — adds all from `c` |
| `retainAll(Collection c)` | `boolean` | Intersection — keeps only elements in `c` |
| `removeAll(Collection c)` | `boolean` | Difference — removes all elements in `c` |
| `containsAll(Collection c)` | `boolean` | `true` if this set contains all of `c` |

---

## 2. Program Entry Point

```java
public static void main(String[] args) {
    section1_SetInterfaceBasics();
    section2_HashSetInternals();
    // ... 15 sections total
}
```

Each section is self-contained — it demonstrates one concept, prints results, and returns. During study, comment out all but one section to focus on a single topic at a time.

---

## 3. Section 1 — Set Interface Basics

```java
Set<Integer> set = new HashSet<>();
```

### Program to the interface

Declaring the variable as `Set<Integer>` rather than `HashSet<Integer>` is the **program to interface** principle. You can later swap `new HashSet<>()` for `new TreeSet<>()` or `new LinkedHashSet<>()` without changing any downstream code.

### `add()` returns a boolean — use it

```java
System.out.println("add(10): " + set.add(10)); // true  — new element
System.out.println("add(10): " + set.add(10)); // false — duplicate silently ignored
```

Most code ignores the return value of `add()`, but it is useful for detecting duplicates inline without a separate `contains()` check. The boolean tells you whether the set was actually modified.

### `remove()` also returns a boolean

```java
set.remove(20); // true  — element existed and was removed
set.remove(99); // false — element was not in the set
```

This is useful for detecting whether a removal actually had any effect — for example, in an idempotency guard.

### Iteration order is undefined for HashSet

```java
for (int val : set) System.out.print(val + " ");
```

The iteration order of `HashSet` is determined by bucket positions in the internal array, which is based on hash codes. It is not random — but it is **not guaranteed** to be any particular order, and it can change when elements are added (due to rehashing). Never rely on HashSet iteration order.

---

## 4. Section 2 — HashSet Internals

### HashSet is a wrapper around HashMap

HashSet's source code has these two declarations:

```java
private transient HashMap<E, Object> map;
private static final Object PRESENT = new Object();
```

Every Set operation delegates to the underlying HashMap:

| Set operation | Delegated to |
|---|---|
| `set.add(e)` | `map.put(e, PRESENT)` |
| `set.contains(e)` | `map.containsKey(e)` |
| `set.remove(e)` | `map.remove(e)` |
| `set.size()` | `map.size()` |
| `set.iterator()` | `map.keySet().iterator()` |

The value slot is always the same single `PRESENT` object — it wastes minimal memory since it's only one object reference shared by all entries.

### HashMap internal structure

```
HashMap table (internal array):
  index 0:  [Node: "Apple" → PRESENT] → null
  index 1:  null
  index 2:  [Node: "Cherry" → PRESENT] → [Node: "Banana" → PRESENT] → null  ← collision
  index 3:  null
  ...
  index 15: [Node: "Mango" → PRESENT] → null
```

Key parameters:

| Parameter | Default | Meaning |
|---|---|---|
| Initial capacity | `16` | Number of buckets in the array |
| Load factor | `0.75` | Resize threshold = capacity × loadFactor |
| Resize trigger | `size > 12` | When 12 elements, resize to 32 |

### How `add("Apple")` works step by step

```
1. Compute hashCode("Apple")           → 1980657840
2. Spread the hash: (h >>> 16) ^ h    → some reduced hash
3. Bucket index = (capacity-1) & hash → e.g. 0
4. Is bucket 0 empty?
     YES → place node at bucket 0 directly
     NO  → walk the chain in bucket 0
           For each node: does node.key.equals("Apple")?
             YES (equals match) → duplicate, return false
             NO  (collision)    → continue chain, add at end
```

### Bucket index formula

```java
int bucket = (16 - 1) & hash;  // (n-1) & hash
```

This is equivalent to `hash % 16` but uses bitwise AND which is faster. It only works correctly when capacity is a power of 2 — which HashMap always maintains.

### Rehashing

When `size > capacity * loadFactor` (i.e., `size > 12` for default setup):
1. New array allocated with `newCapacity = oldCapacity * 2` (doubles)
2. All existing entries rehashed and placed in new array
3. Old array discarded

This is O(n) but happens rarely — it is the same amortized O(1) pattern as ArrayList resizing.

### Java 8+ treeification

When a single bucket accumulates ≥ 8 entries AND the total table size is ≥ 64, the linked list in that bucket is converted to a Red-Black Tree:

```
Before treeify:  bucket → [A] → [B] → [C] → [D] → [E] → [F] → [G] → [H]
                          O(n) lookup per bucket
                          
After treeify:   bucket → Red-Black Tree with A..H
                          O(log n) lookup per bucket
```

This changes the absolute worst-case from O(n) to O(log n) — but in practice with good hash functions, buckets rarely exceed even 3 entries.

---

## 5. Section 3 — hashCode() and equals() Contract

### The contract

Java mandates:
```
IF a.equals(b) == true  THEN a.hashCode() == b.hashCode()
```

The reverse is NOT required:
```
Same hashCode does NOT imply equal  (this is a collision)
```

### What breaks when the contract is violated

**Case 1: `equals()` overridden, `hashCode()` not overridden**

```java
class PersonBroken {
    String name; int age;
    @Override public boolean equals(Object o) { ... }  // overridden
    // hashCode() NOT overridden → uses Object.hashCode() = memory address
}

Set<PersonBroken> set = new HashSet<>();
set.add(new PersonBroken("Alice", 30));
set.add(new PersonBroken("Alice", 30)); // two different objects → different hashCodes
// → different buckets → equals() never called → BOTH inserted → duplicate!
```

**Case 2: `hashCode()` overridden, `equals()` not overridden**

```java
// hashCode() returns same value for equal objects → same bucket
// But equals() uses Object default → reference comparison (==)
// → different references → both inserted → duplicate!
```

**Case 3: Both correctly overridden ✅**

```java
record Person(String name, int age) {}
// Java records auto-generate equals() and hashCode() from all fields
```

### Using `Objects.hash()` for clean hashCode

```java
@Override
public int hashCode() {
    return Objects.hash(name, age);  // null-safe, multi-field hash utility
}
```

`Objects.hash(a, b, c)` internally calls `Arrays.hashCode(new Object[]{a, b, c})` — it handles nulls safely and combines multiple fields into one hash value.

### Java records — the modern solution

```java
record Person(String name, int age) {}
```

Records (Java 16+) automatically generate:
- `equals()` — field-by-field comparison
- `hashCode()` — hash of all fields
- `toString()` — formatted field output
- Accessors — `name()`, `age()`

For value objects like Person, Student, Point — always prefer records over manual `equals/hashCode`.

---

## 6. Section 4 — Collisions and Bucket Chaining

### What is a collision?

A collision occurs when two **different** elements produce the same bucket index. This is not a bug — it is an expected part of how hash tables work. The hash function maps a large universe of keys to a small number of buckets, so some collisions are inevitable.

### The classic Java collision: `"Aa"` and `"BB"`

```java
"Aa".hashCode() == 2112
"BB".hashCode() == 2112  // same hash, different strings
```

Both map to the same bucket. Java resolves this by chaining — both entries coexist in the bucket, distinguished by `equals()`:

```
bucket 2112:
  [Node: "Aa" → PRESENT] → [Node: "BB" → PRESENT] → null
```

When searching for `"Aa"`:
1. Compute hash → bucket 2112
2. Check `"Aa".equals("Aa")` → true → found ✅

When searching for `"BB"`:
1. Compute hash → bucket 2112
2. Check `"BB".equals("Aa")` → false → continue chain
3. Check `"BB".equals("BB")` → true → found ✅

### Performance impact of collisions

| Bucket state | Lookup cost | Cause |
|---|---|---|
| Empty or single element | O(1) | Normal case |
| Short chain (2–7 nodes) | O(1) amortized | Few collisions |
| Long chain (8+ nodes, pre-Java 8) | O(n) | Poor hash function or adversarial input |
| Treeified bucket (8+ nodes, Java 8+) | O(log n) | Same cause, better outcome |

### Why `(n-1) & hash` instead of `hash % n`?

Bitwise AND is a single CPU instruction. The modulo operation requires integer division which is 5–20× slower on most hardware. They are equivalent only when `n` is a power of 2 — which is why HashMap always keeps its capacity as a power of 2.

---

## 7. Section 5 — TreeSet Basics

### TreeSet is a wrapper around TreeMap

Just like HashSet wraps HashMap, TreeSet wraps TreeMap:

```java
private transient NavigableMap<E, Object> m;
private static final Object PRESENT = new Object();
```

`set.add(e)` → `m.put(e, PRESENT)`, and so on.

### Red-Black Tree properties

A Red-Black Tree is a self-balancing Binary Search Tree with these invariants:

```
1. Every node is RED or BLACK
2. Root is always BLACK
3. No path has two consecutive RED nodes
4. All paths from root to null have the same number of BLACK nodes
```

These rules guarantee:
```
Tree height ≤ 2 × log₂(n + 1)
```

This means every operation traverses at most `2 × log₂(n+1)` nodes — guaranteed O(log n) with no worst-case degradation. This is the key advantage over a plain BST, which can become a linked list (O(n) height) if elements are inserted in sorted order.

### NavigableSet methods — TreeSet's superpowers

```java
TreeSet<Integer> treeSet = new TreeSet<>(Set.of(10, 20, 30, 40, 50));

treeSet.first()          // 10  — minimum element
treeSet.last()           // 50  — maximum element
treeSet.floor(25)        // 20  — largest element ≤ 25 (or null)
treeSet.ceiling(25)      // 30  — smallest element ≥ 25 (or null)
treeSet.lower(30)        // 20  — strictly less than 30 (or null)
treeSet.higher(30)       // 40  — strictly greater than 30 (or null)
treeSet.headSet(30)      // [10, 20]      — exclusive of 30
treeSet.tailSet(30)      // [30, 40, 50]  — inclusive of 30
treeSet.subSet(20, 40)   // [20, 30]      — from inclusive, to exclusive
treeSet.descendingSet()  // [50, 40, 30, 20, 10]
```

These range operations are O(log n) to find the start/end positions plus O(k) to iterate k elements — very efficient for range queries.

### `pollFirst()` and `pollLast()`

```java
treeSet.pollFirst();  // retrieves AND removes the minimum element → O(log n)
treeSet.pollLast();   // retrieves AND removes the maximum element → O(log n)
```

These are useful for priority-queue-like patterns: always process the smallest or largest element next.

---

## 8. Section 6 — TreeSet with Custom Objects

### Two ways to provide ordering for TreeSet

**Way 1 — Natural ordering via `Comparable<T>`**

```java
record Student(String name, int score) implements Comparable<Student> {
    @Override
    public int compareTo(Student other) {
        int cmp = Integer.compare(other.score, this.score); // desc score
        return cmp != 0 ? cmp : this.name.compareTo(other.name); // asc name
    }
}
```

`compareTo()` contract:
- Returns negative if `this` comes before `other`
- Returns zero if they are considered equal (TreeSet treats this as duplicate)
- Returns positive if `this` comes after `other`

**Way 2 — External ordering via `Comparator`**

```java
TreeSet<String> byLength = new TreeSet<>(
    Comparator.comparingInt(String::length)
              .thenComparing(Comparator.naturalOrder())
);
```

The Comparator overrides any natural ordering. This is useful when:
- You cannot modify the element class (e.g., third-party library)
- You need a different ordering than the natural one
- The element class does not implement `Comparable`

### TreeSet uses `compareTo()` for equality — not `equals()`

This is a critical distinction:

```java
// Two objects where compareTo() returns 0 are treated as duplicates by TreeSet
// even if equals() returns false
```

The `BigDecimal` example in the code demonstrates this:
```java
new BigDecimal("2.0").equals(new BigDecimal("2.00"))     // false — different scale
new BigDecimal("2.0").compareTo(new BigDecimal("2.00"))  // 0    — same numeric value

TreeSet<BigDecimal> set = new TreeSet<>();
set.add(new BigDecimal("2.0"));
set.add(new BigDecimal("2.00")); // compareTo == 0 → treated as duplicate → rejected
// set.size() == 1, not 2
```

This inconsistency between `compareTo()` and `equals()` in `BigDecimal` is a known Java quirk. Always be aware of which method your chosen Set uses for equality.

---

## 9. Section 7 — LinkedHashSet

### How LinkedHashSet works internally

```java
private transient LinkedHashMap<E, Object> map;
```

`LinkedHashMap` extends `HashMap` and adds a **doubly linked list** that runs through all entries in insertion order:

```
Hash table:  [bucket 3] → [bucket 7] → [bucket 1] ...
                ↓              ↓            ↓
Linked list: [42] ↔ [7] ↔ [15] ↔ [3] ↔ [99] ↔ [18]  ← insertion order
```

The hash table provides O(1) lookups. The linked list provides ordered iteration. Both structures are updated on every `add()` and `remove()`.

### Order comparison across all three implementations

```java
List<Integer> input = List.of(42, 7, 15, 3, 99, 7, 42, 18);

new HashSet<>(input)       // [1, 18, 3, 99, 7, 42, 15] ← arbitrary
new LinkedHashSet<>(input) // [42, 7, 15, 3, 99, 18]    ← insertion order (dups removed)
new TreeSet<>(input)       // [3, 7, 15, 18, 42, 99]    ← sorted
```

### When to use LinkedHashSet

Use `LinkedHashSet` when you need all of:
- No duplicates (Set contract)
- O(1) average operations (HashSet speed)
- Iteration in the order elements were first inserted

Classic example — deduplicate a user's browsing history while preserving the visit order:

```java
List<String> pageVisits = List.of("/home", "/products", "/home", "/cart", "/products");
Set<String> uniqueVisits = new LinkedHashSet<>(pageVisits);
// [/home, /products, /cart] — first occurrence order preserved
```

---

## 10. Section 8 — Set Operations

### The four fundamental set operations

**Union A ∪ B** — all elements from both sets:
```java
Set<Integer> union = new HashSet<>(setA);
union.addAll(setB);
```

**Intersection A ∩ B** — only elements present in both:
```java
Set<Integer> intersection = new HashSet<>(setA);
intersection.retainAll(setB);  // removes from setA anything not in setB
```

**Difference A − B** — elements in A but not in B:
```java
Set<Integer> difference = new HashSet<>(setA);
difference.removeAll(setB);
```

**Symmetric Difference A △ B** — elements in either but not both:
```java
Set<Integer> symDiff = new HashSet<>(union);
symDiff.removeAll(intersection);
// = (A ∪ B) − (A ∩ B)
```

### Important: all operations modify the set in place

`addAll`, `retainAll`, and `removeAll` mutate the calling set. Always work on a **copy** if you need to preserve the originals:

```java
// WRONG — destroys setA
setA.retainAll(setB);

// CORRECT — preserves setA
Set<Integer> intersection = new HashSet<>(setA);
intersection.retainAll(setB);
```

### Non-destructive alternatives with Stream API

```java
// Union
Set<Integer> union = Stream.concat(setA.stream(), setB.stream())
                           .collect(Collectors.toSet());

// Intersection
Set<Integer> intersect = setA.stream()
                             .filter(setB::contains)
                             .collect(Collectors.toSet());
```

Streams create new sets without touching the originals. The trade-off is slightly more object allocation.

### Subset check

```java
setA.containsAll(subset);  // true if every element of subset is in setA
```

O(n) where n = size of `subset`. Each element of `subset` is checked via `contains()` which is O(1) for HashSet.

---

## 11. Section 9 — Real World: Duplicate Removal

### Three approaches to remove duplicates from a List

**Approach 1 — HashSet (fast, no order):**
```java
List<Integer> noOrder = new ArrayList<>(new HashSet<>(withDups));
```
- O(n) time — single pass to build HashSet
- Order of elements in result is unpredictable

**Approach 2 — LinkedHashSet (fast, order preserved):**
```java
List<Integer> withOrder = new ArrayList<>(new LinkedHashSet<>(withDups));
```
- O(n) time — same speed as HashSet
- Elements appear in the order of their **first occurrence** in the input
- This is usually what users expect when "removing duplicates"

**Approach 3 — `stream().distinct()` (readable):**
```java
List<Integer> result = withDups.stream().distinct().collect(Collectors.toList());
```
- O(n) time — internally uses a LinkedHashSet
- Preserves encounter order (same as LinkedHashSet approach)
- Most expressive if you are already working in a Stream pipeline

**Never use nested loops:**
```java
// O(n²) — avoid this
for (int i = 0; i < list.size(); i++)
    for (int j = i + 1; j < list.size(); j++)
        if (list.get(i).equals(list.get(j))) list.remove(j);
```

### Real use: idempotent request processing

```java
// add() returns false if already seen → skip duplicate
if (processedIds.add(requestId)) {
    processRequest(requestId);   // only runs once per unique ID
}
```

This pattern is cleaner than a `contains()` check followed by `add()` because it avoids a redundant hash lookup.

---

## 12. Section 10 — Real World: Leaderboard

### Why TreeSet fits leaderboard requirements

| Requirement | How TreeSet satisfies it |
|---|---|
| Sorted by score | Elements always maintained in sorted order |
| No duplicate entries | Set contract — one entry per player |
| Efficient insert/update | O(log n) add and remove |
| Top N query | Stream with `limit(n)` — first elements are highest score |
| Range query (score > X) | `stream().filter()` or `tailSet()` |

### Updating a score — remove + add pattern

```java
leaderboard.remove(new PlayerScore("Alice", 1500));  // O(log n)
leaderboard.add(new PlayerScore("Alice", 2500));      // O(log n)
```

TreeSet has no "update" operation. The only way to change an element's position in the tree is to remove it and re-insert with the new value. This is O(log n) — acceptable for a leaderboard.

### `compareTo()` in the PlayerScore record

```java
@Override
public int compareTo(PlayerScore other) {
    int cmp = Integer.compare(other.score, this.score); // desc by score
    return cmp != 0 ? cmp : this.player.compareTo(other.name); // asc by name
}
```

The secondary sort by name ensures that two players with identical scores are not considered duplicates — they get distinct positions in the tree. Without the `thenComparing` fallback, adding Carol with score 95 when Alice already has score 95 would silently reject Carol as a "duplicate" (since `compareTo` returns 0).

---

## 13. Section 11 — Real World: Unique Visitor Tracking

### Why HashSet is ideal for membership tracking

The fundamental operation is: "Have I seen this ID before?" This maps directly to `Set.contains()` — which is O(1) average for HashSet. For 10 million requests, this means 10 million O(1) lookups — vastly more efficient than sorting or searching a list.

### Using `add()` return value for idempotency

```java
boolean isNew = uniqueVisitors.add(userId);
if (!isNew) duplicateRequests++;
```

`add()` atomically checks-and-inserts. It returns `true` only when the element is genuinely new. This is more efficient than:

```java
// Two hash lookups instead of one
if (!set.contains(userId)) {
    set.add(userId);
}
```

### Idempotency pattern

```java
for (String txn : txnQueue) {
    if (processedTxns.add(txn)) {   // false if already processed
        processTransaction(txn);    // runs exactly once per unique txn
    }
}
```

This pattern appears in:
- Message queue deduplication
- Database transaction idempotency
- Event sourcing (process each event exactly once)
- API retry handling (ignore retried requests)

---

## 14. Section 12 — Custom Comparator Patterns

### Comparator factory methods (Java 8+)

| Method | Use case |
|---|---|
| `Comparator.naturalOrder()` | Use element's `compareTo()` as-is |
| `Comparator.reverseOrder()` | Reverse natural ordering |
| `Comparator.comparingInt(fn)` | Compare by int-valued function |
| `Comparator.comparingLong(fn)` | Compare by long-valued function |
| `Comparator.comparing(fn)` | Compare by any Comparable-returning function |
| `comparator.thenComparing(fn)` | Secondary sort key |
| `comparator.reversed()` | Flip any comparator |
| `Comparator.nullsFirst(cmp)` | Nulls sort before non-nulls |
| `Comparator.nullsLast(cmp)` | Nulls sort after non-nulls |
| `String.CASE_INSENSITIVE_ORDER` | Built-in case-insensitive String comparator |

### Case-insensitive TreeSet

```java
TreeSet<String> caseInsensitive = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
caseInsensitive.add("Apple");
caseInsensitive.add("apple"); // compareTo("Apple", "apple") == 0 → duplicate
// size == 1, not 2
```

`String.CASE_INSENSITIVE_ORDER` is a built-in `Comparator<String>` that calls `compareToIgnoreCase()`. Useful for case-insensitive keyword sets, tag systems, and configuration key registries.

### Multi-key sort with `thenComparing`

```java
TreeSet<String> byLength = new TreeSet<>(
    Comparator.comparingInt(String::length)
              .thenComparing(Comparator.naturalOrder())
);
```

This first compares by string length (shorter first). If two strings have the same length, it falls back to alphabetical order. The chaining via `thenComparing` makes multi-level sorting readable without nested ternary expressions.

---

## 15. Section 13 — Common Mistakes

### Mistake 1 — null in TreeSet

```java
TreeSet<String> ts = new TreeSet<>();
ts.add("Apple");
ts.add(null);  // NullPointerException
```

TreeSet uses `compareTo()` / `Comparator.compare()` to place elements. Comparing `null` with a non-null value causes `NullPointerException` because `null.compareTo(...)` is invalid. `HashSet` and `LinkedHashSet` allow exactly one `null` element because they use `hashCode()` which handles null (`null` hashes to bucket 0).

### Mistake 2 — Mutating a HashSet element after insertion

```java
Set<Tag> tags = new HashSet<>();
Tag t = new Tag("java");
tags.add(t);            // stored in bucket based on "java".hashCode()

t.label = "python";    // mutates the element — changes its hashCode

tags.contains(t);      // false! Now hashes to "python" bucket, not "java" bucket
```

The element is not lost from the internal array — it still sits in the "java" bucket. But when you search for it, the new hash takes you to the "python" bucket where it is not found. The element is effectively "orphaned" in a wrong bucket.

**Rule:** Always use **immutable fields** in `hashCode()` and `equals()`. Java records (which are immutable) enforce this naturally.

### Mistake 3 — Expecting order from HashSet

HashSet's iteration order is:
- Not random (it is deterministic for a given JVM run)
- Not insertion order
- Not sorted order
- Based entirely on hash bucket positions

Between JVM versions, hash randomization changes, so the order may differ across versions too. Never rely on it.

### Mistake 4 — `Set.of()` is immutable

```java
Set<String> s = Set.of("A", "B", "C");
s.add("D");  // UnsupportedOperationException

// Fix — wrap in a new mutable Set
Set<String> mutable = new HashSet<>(Set.of("A", "B", "C"));
```

`Set.of()` (Java 9+) returns an unmodifiable set. It also throws `NullPointerException` if you pass `null` as any element, and `IllegalArgumentException` for duplicate elements. Use it when you want a guaranteed-immutable constant set.

---

## 16. Section 14 — Performance Benchmark

### What is being measured

The benchmark inserts 500,000 integers (with duplicates, since values are `rng.ints(n, 0, n*2)`) into all three Set implementations and measures wall-clock time. Then it performs 100,000 `contains()` lookups.

### Expected results and why

**Insert benchmark:**
```
HashSet:       fastest — O(n) total
LinkedHashSet: slightly slower — same O(n) but maintains linked list (extra pointer writes)
TreeSet:       slowest — O(n log n) total; each insert does ~log₂(500000) ≈ 19 comparisons
```

**Contains benchmark:**
```
HashSet:  O(1) per lookup — one hash computation, typically one bucket check
TreeSet:  O(log n) per lookup — ~19 comparisons traversing the tree
```

The factor difference scales with n. At 500,000 elements, TreeSet is typically 5–15× slower for both operations, which confirms the theoretical complexity gap.

### When TreeSet's cost is worth paying

TreeSet's O(log n) overhead is only justified when you genuinely need:
- Always-sorted iteration without re-sorting
- Range queries (`headSet`, `tailSet`, `subSet`)
- `floor`, `ceiling`, `lower`, `higher` navigation

For pure membership testing or deduplication, HashSet is always the right choice.

---

## 17. Section 15 — Interview Summary

### Key interview questions and answers

**Q: Why is HashSet O(1) for add/contains/remove?**

It uses a hash table. `hashCode()` computes the bucket index in O(1) — a single arithmetic operation. In the average case, the bucket contains 0–1 elements, so the operation completes in O(1). Even with occasional O(n) rehashing, the amortized cost per operation is O(1).

**Q: What is a collision? How does Java handle it?**

A collision is when two different objects hash to the same bucket index. Java resolves collisions using chaining: multiple entries coexist in the same bucket as a linked list. Java 8+ treeifies buckets with ≥ 8 entries into a Red-Black Tree, reducing per-bucket worst case from O(n) to O(log n).

**Q: Why is TreeSet O(log n)?**

It is backed by a Red-Black Tree — a self-balancing Binary Search Tree. The tree's height is always bounded by `2 × log₂(n+1)`. Every operation (insert, find, delete) traverses from root to a leaf, which takes at most `height` steps = O(log n).

**Q: Why Red-Black Tree specifically?**

A plain BST degrades to O(n) height when elements are inserted in sorted order. Red-Black Trees self-balance using colour-based rotation rules, guaranteeing O(log n) height regardless of insertion order. The balancing overhead is low (at most 2 rotations per insert/delete), making it more practical than perfectly balanced alternatives like AVL trees.

**Q: When to use LinkedHashSet over HashSet?**

When you need Set semantics (deduplication, O(1) operations) but also need to iterate in insertion order. Example: maintain a list of unique tags in the order the user added them.

**Q: What happens if `hashCode()` is not overridden?**

`Object.hashCode()` returns a memory-address-based value. Two logically equal objects created separately have different memory addresses → different hashCodes → placed in different buckets → both pass the duplicate check → both inserted. Result: silent duplicates in the Set, which is a correctness bug.

**Q: Can TreeSet contain null?**

No. `compareTo()` and `Comparator.compare()` cannot compare `null` with a non-null value without a `NullPointerException`. HashSet and LinkedHashSet allow exactly one `null` element (null hashes to bucket 0).

---

## 18. Key Takeaways

### Time complexity quick reference

| Operation | HashSet | LinkedHashSet | TreeSet |
|---|---|---|---|
| `add(e)` | O(1) avg | O(1) avg | O(log n) |
| `remove(e)` | O(1) avg | O(1) avg | O(log n) |
| `contains(e)` | O(1) avg | O(1) avg | O(log n) |
| `first() / last()` | N/A | N/A | O(log n) |
| `floor / ceiling` | N/A | N/A | O(log n) |
| `headSet / tailSet` | N/A | N/A | O(log n) |
| Iteration order | None | Insertion | Sorted |
| Null allowed | ✅ (one) | ✅ (one) | ❌ |
| Memory per entry | Low | Medium | High |

### When to use which

```
Need fast deduplication / membership test?
  → HashSet  (O(1), no order needed)

Need deduplication AND preserve insertion order?
  → LinkedHashSet  (O(1), first-occurrence order)

Need sorted unique elements?
  → TreeSet  (O(log n), always sorted)

Need range queries? (all values between X and Y)
  → TreeSet  (headSet, tailSet, subSet)

Need to process elements in priority order (min/max first)?
  → TreeSet with pollFirst() / pollLast()
  → or PriorityQueue if duplicates are allowed

Deduplicating a large dataset (millions of records)?
  → HashSet  (O(n) total vs TreeSet's O(n log n))
```

### The golden rules

1. **Always override `equals()` AND `hashCode()` together** — violating the contract causes silent duplicates
2. **Use immutable fields in `hashCode/equals`** — mutable keys cause elements to become unreachable in the Set
3. **Default to `HashSet`** — fastest for most use cases
4. **Use `TreeSet`** only when sorted order or range queries are genuinely needed
5. **Use `LinkedHashSet`** when insertion order must be preserved alongside deduplication
6. **`Set.of()` is immutable** — wrap in `new HashSet<>()` if you need to mutate
7. **TreeSet uses `compareTo()` for equality** — inconsistency with `equals()` causes subtle bugs
8. **Never put null in a `TreeSet`** — comparisons will throw `NullPointerException`
9. **Use `add()` return value** for clean idempotency checks instead of `contains()` + `add()`
10. **Work on copies** before calling `addAll` / `retainAll` / `removeAll` — they mutate in place
