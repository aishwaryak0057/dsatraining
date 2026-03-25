import java.util.*;
import java.util.stream.*;

/**
 * ============================================================
 *  SET DEEP DIVE — HashSet vs TreeSet vs LinkedHashSet
 *  Run: javac SetDeepDive.java && java SetDeepDive
 * ============================================================
 *
 *  WHAT IS A SET?
 *  --------------
 *  Set is a collection that models the mathematical concept of a set.
 *
 *  Key characteristics:
 *    - Does NOT allow duplicate elements
 *    - May or may not maintain order (depends on implementation)
 *    - Uses hashing or tree structure internally
 *
 *  Main implementations:
 *    - HashSet       → backed by HashMap, O(1) avg, no order
 *    - TreeSet       → backed by Red-Black Tree, O(log n), sorted order
 *    - LinkedHashSet → backed by HashMap + LinkedList, O(1) avg, insertion order
 *
 *  Common Set methods:
 *    add(E e)           → adds element, returns false if already present
 *    remove(Object o)   → removes element, returns false if not found
 *    contains(Object o) → true if element exists
 *    size()             → number of elements
 *    isEmpty()          → true if no elements
 *    iterator()         → iterates in implementation-specific order
 *    addAll(Collection) → union
 *    retainAll(Collection) → intersection
 *    removeAll(Collection) → difference
 */
public class SetDeepDive {

    public static void main(String[] args) {
        System.out.println("=== SET DEEP DIVE: HashSet vs TreeSet vs LinkedHashSet ===\n");

        section1_SetInterfaceBasics();
        section2_HashSetInternals();
        section3_HashCodeAndEquals();
        section4_HashSetCollisions();
        section5_TreeSetBasics();
        section6_TreeSetAdvanced();
        section7_LinkedHashSet();
        section8_SetOperations();
        section9_RealWorldDuplicateRemoval();
        section10_RealWorldLeaderboard();
        section11_RealWorldUniqueUserIDs();
        section12_CustomComparator();
        section13_CommonMistakes();
        section14_PerformanceBenchmark();
        section15_InterviewSummary();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 1 — Set Interface Basics
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * SET INTERFACE
     * -------------
     * java.util.Set<E> extends java.util.Collection<E>
     *
     * The contract: no two elements e1 and e2 where e1.equals(e2) == true.
     * At most one null element (HashSet / LinkedHashSet allow null; TreeSet does not).
     *
     * add() returns boolean:
     *   true  → element was added (was not present)
     *   false → element was NOT added (duplicate detected)
     *
     * This return value is commonly ignored but is useful for
     * detecting whether a value was already in the set.
     */
    static void section1_SetInterfaceBasics() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 1: Set Interface Basics");
        System.out.println("─────────────────────────────────────────");

        // Program to the Set interface — not the implementation
        Set<Integer> set = new HashSet<>();

        // add() returns true if new, false if duplicate
        System.out.println("add(10): " + set.add(10)); // true
        System.out.println("add(20): " + set.add(20)); // true
        System.out.println("add(10): " + set.add(10)); // false — duplicate silently ignored
        System.out.println("add(30): " + set.add(30)); // true

        System.out.println("Set contents: " + set);      // order not guaranteed
        System.out.println("Size: " + set.size());        // 3, not 4

        // contains() — O(1) for HashSet
        System.out.println("Contains 20? " + set.contains(20)); // true
        System.out.println("Contains 99? " + set.contains(99)); // false

        // remove() — returns true if element existed
        System.out.println("remove(20): " + set.remove(20)); // true
        System.out.println("remove(99): " + set.remove(99)); // false
        System.out.println("After remove(20): " + set);

        // Iteration — order depends on implementation
        System.out.print("Iterating: ");
        for (int val : set) System.out.print(val + " ");
        System.out.println("\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 2 — HashSet Internals
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * HASHSET INTERNAL STRUCTURE
     * ---------------------------
     * HashSet is literally a wrapper around HashMap:
     *
     *   private transient HashMap<E, Object> map;
     *   private static final Object PRESENT = new Object();
     *
     * When you call set.add("A"):
     *   internally → map.put("A", PRESENT)
     *
     * When you call set.contains("A"):
     *   internally → map.containsKey("A")
     *
     * When you call set.remove("A"):
     *   internally → map.remove("A")
     *
     * So all HashSet operations are exactly HashMap key operations.
     * The "value" slot is always the same dummy PRESENT object.
     *
     * HashMap INTERNAL STRUCTURE (Java 8+):
     * ----------------------------------------
     * - An array of "buckets": Node<K,V>[] table
     * - Default initial capacity: 16 buckets
     * - Default load factor: 0.75
     * - When size > capacity * loadFactor → rehash (double capacity)
     *
     * HOW add("Apple") WORKS:
     * 1. Compute hashCode("Apple") → e.g. 1980657840
     * 2. Bucket index = (n-1) & hash  → e.g. bucket 0
     * 3. If bucket empty → place element directly
     * 4. If bucket has elements → check equals() for each
     *    a. equals() true  → duplicate, do not insert, return false
     *    b. equals() false → collision, chain in same bucket
     *
     * Java 8+ TREEIFICATION:
     * When a single bucket has ≥ 8 elements (TREEIFY_THRESHOLD),
     * the linked list in that bucket becomes a Red-Black Tree.
     * This changes worst-case from O(n) to O(log n) per bucket.
     */
    static void section2_HashSetInternals() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 2: HashSet Internals");
        System.out.println("─────────────────────────────────────────");

        Set<String> set = new HashSet<>();
        String[] words = {"Apple", "Banana", "Mango", "Apple", "Cherry", "Banana"};

        System.out.println("Adding elements and observing add() return values:");
        for (String w : words) {
            boolean added = set.add(w);
            System.out.printf("  add(%-8s) → %s  (hashCode=%d)%n",
                "\"" + w + "\"", added ? "inserted" : "DUPLICATE", w.hashCode());
        }

        System.out.println("\nFinal set: " + set);
        System.out.println("Size: " + set.size() + " (6 adds, 2 duplicates rejected)\n");

        // Show hash bucket distribution concept
        System.out.println("Simulating bucket assignment (capacity=16):");
        for (String w : new String[]{"Apple","Banana","Mango","Cherry"}) {
            int hash = w.hashCode();
            int bucket = (16 - 1) & hash; // (n-1) & hash formula
            System.out.printf("  %-8s hashCode=%-12d bucket=%d%n", w, hash, bucket);
        }

        System.out.println();
        System.out.println("Load factor demo — when does rehashing trigger?");
        System.out.printf("  Default capacity=16, loadFactor=0.75%n");
        System.out.printf("  Rehash when size > 16 * 0.75 = %d elements%n", (int)(16 * 0.75));
        System.out.printf("  New capacity after rehash = 32%n");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 3 — hashCode() and equals() Contract
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * THE HASHCODE / EQUALS CONTRACT
     * --------------------------------
     * Java mandates:
     *   IF a.equals(b) == true  THEN a.hashCode() == b.hashCode()
     *
     * The reverse is NOT required:
     *   Same hashCode does NOT mean equal (this is a collision)
     *
     * What happens if you break the contract?
     *
     * CASE 1: equals() overridden but hashCode() NOT overridden
     *   Two logically equal objects get different hashCodes.
     *   They land in different buckets.
     *   HashSet finds no match in that bucket → inserts as NEW element.
     *   RESULT: Duplicates appear in the set. ← BUG
     *
     * CASE 2: hashCode() overridden but equals() NOT overridden
     *   Two objects land in the same bucket.
     *   equals() uses Object reference comparison (==).
     *   Different references → not equal → both inserted.
     *   RESULT: Duplicates appear in the set. ← BUG
     *
     * CASE 3: Both overridden correctly
     *   Same hashCode → same bucket → equals() returns true → duplicate rejected.
     *   RESULT: Correct behaviour. ✅
     */
    static void section3_HashCodeAndEquals() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 3: hashCode() and equals() Contract");
        System.out.println("─────────────────────────────────────────");

        // ── Case 1: class WITHOUT proper overrides ──────────────────────────
        class PersonBroken {
            String name; int age;
            PersonBroken(String n, int a) { name = n; age = a; }
            @Override public String toString() { return name + "(" + age + ")"; }
            // No equals() or hashCode() override → uses Object defaults
            // Object.equals() = reference comparison (==)
            // Object.hashCode() = memory address based
        }

        Set<PersonBroken> brokenSet = new HashSet<>();
        brokenSet.add(new PersonBroken("Alice", 30));
        brokenSet.add(new PersonBroken("Alice", 30)); // logically same — should be duplicate
        System.out.println("PersonBroken (no overrides) — expected size 1, actual: "
                           + brokenSet.size() + " ← DUPLICATE inserted!");

        // ── Case 2: class WITH proper overrides ────────────────────────────
        // Using a record — Java 16+, auto-generates equals() and hashCode()
        record Person(String name, int age) {}

        Set<Person> correctSet = new HashSet<>();
        correctSet.add(new Person("Alice", 30));
        correctSet.add(new Person("Alice", 30)); // duplicate correctly rejected
        correctSet.add(new Person("Bob",   25));
        System.out.println("Person record (auto overrides) — expected size 2, actual: "
                           + correctSet.size() + " ✅");

        // ── Case 3: Manual override (pre-record style) ──────────────────────
        class PersonCorrect {
            String name; int age;
            PersonCorrect(String n, int a) { name = n; age = a; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;             // same reference → equal
                if (!(o instanceof PersonCorrect p)) return false;
                return age == p.age && name.equals(p.name);
            }

            @Override
            public int hashCode() {
                // Objects.hash() is a null-safe, multi-field hash utility
                return Objects.hash(name, age);
            }

            @Override public String toString() { return name + "(" + age + ")"; }
        }

        Set<PersonCorrect> manualSet = new HashSet<>();
        manualSet.add(new PersonCorrect("Alice", 30));
        manualSet.add(new PersonCorrect("Alice", 30));
        manualSet.add(new PersonCorrect("Bob",   25));
        System.out.println("PersonCorrect (manual overrides) — expected size 2, actual: "
                           + manualSet.size() + " ✅");

        // Show hashCode values
        System.out.println("\nhashCode consistency check:");
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Alice", 30);
        System.out.println("  p1.equals(p2): " + p1.equals(p2));
        System.out.println("  p1.hashCode(): " + p1.hashCode());
        System.out.println("  p2.hashCode(): " + p2.hashCode());
        System.out.println("  Same hashCode: " + (p1.hashCode() == p2.hashCode()) + " ✅");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 4 — Collisions
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * WHAT IS A COLLISION?
     * ----------------------
     * A collision occurs when two DIFFERENT elements produce the same bucket index.
     *
     *   hash("Aa") = 2112
     *   hash("BB") = 2112  ← same hash as "Aa" — classic Java collision
     *
     * Both must go into the same bucket.
     * Java resolves this using:
     *   - CHAINING (Java 7-): linked list of nodes in the bucket
     *   - CHAINING + TREEIFY (Java 8+): linked list until 8 elements,
     *     then convert bucket to Red-Black Tree
     *
     * Performance impact:
     *   No collisions  → O(1) per operation  (direct bucket access)
     *   Few collisions → O(1) amortized      (short chains)
     *   Many collisions→ O(n) worst case      (all in one bucket, pre-Java 8)
     *                  → O(log n) worst case  (treeified bucket, Java 8+)
     *
     * Why does Java use (n-1) & hash instead of hash % n?
     *   Bitwise AND is faster than modulo.
     *   Works correctly only when capacity is a power of 2 (which HashMap guarantees).
     */
    static void section4_HashSetCollisions() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 4: Collisions and Bucket Chaining");
        System.out.println("─────────────────────────────────────────");

        // "Aa" and "BB" are a famous Java String pair with equal hashCodes
        String s1 = "Aa";
        String s2 = "BB";
        System.out.println("Collision example — different strings, same hashCode:");
        System.out.printf("  hashCode(\"Aa\") = %d%n", s1.hashCode());
        System.out.printf("  hashCode(\"BB\") = %d%n", s2.hashCode());
        System.out.printf("  \"Aa\".equals(\"BB\") = %b%n", s1.equals(s2));
        System.out.println("  → Same bucket, but equals() distinguishes them → both stored ✅");

        Set<String> set = new HashSet<>();
        set.add("Aa");
        set.add("BB");
        set.add("Aa"); // duplicate
        System.out.println("  Set after adding \"Aa\", \"BB\", \"Aa\": " + set
                           + " (size=" + set.size() + ")");

        // Java 8 treeification threshold
        System.out.println("\nJava 8+ Treeification:");
        System.out.println("  Bucket node count < 8  → linked list (fast insert, moderate lookup)");
        System.out.println("  Bucket node count >= 8 → Red-Black Tree (O(log n) per bucket)");
        System.out.println("  Table size < 64         → resize instead of treeify");
        System.out.println("  This means worst-case degrades from O(n) to O(log n) in Java 8+");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 5 — TreeSet Basics
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * TREESET OVERVIEW
     * -----------------
     * Backed by: TreeMap<E, Object>   (same PRESENT dummy value trick as HashSet)
     * TreeMap is backed by: Red-Black Tree (a self-balancing BST)
     *
     * Properties:
     *   - All elements are sorted (natural order or custom Comparator)
     *   - No null elements allowed (Comparator cannot compare null)
     *   - Implements NavigableSet → provides floor(), ceiling(), higher(), lower()
     *
     * RED-BLACK TREE PROPERTIES:
     *   1. Every node is RED or BLACK
     *   2. Root is always BLACK
     *   3. No two consecutive RED nodes on any path
     *   4. Every path from root to null has the same number of BLACK nodes
     *
     * These rules guarantee:
     *   Height ≤ 2 * log₂(n+1)
     *   → All operations guaranteed O(log n) — no worst case degradation
     *
     * This is different from a plain BST which can degrade to O(n)
     * if elements are inserted in sorted order (becomes a linked list).
     */
    static void section5_TreeSetBasics() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 5: TreeSet Basics");
        System.out.println("─────────────────────────────────────────");

        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.add(30);
        treeSet.add(10);
        treeSet.add(50);
        treeSet.add(20);
        treeSet.add(40);
        treeSet.add(10); // duplicate — ignored

        System.out.println("Added: 30, 10, 50, 20, 40, 10(dup)");
        System.out.println("TreeSet: " + treeSet); // always sorted: [10, 20, 30, 40, 50]

        // NavigableSet navigation methods
        System.out.println("\nNavigation methods:");
        System.out.println("  first():        " + treeSet.first());         // 10
        System.out.println("  last():         " + treeSet.last());          // 50
        System.out.println("  floor(25):      " + treeSet.floor(25));       // 20 — largest ≤ 25
        System.out.println("  ceiling(25):    " + treeSet.ceiling(25));     // 30 — smallest ≥ 25
        System.out.println("  lower(30):      " + treeSet.lower(30));       // 20 — strictly < 30
        System.out.println("  higher(30):     " + treeSet.higher(30));      // 40 — strictly > 30
        System.out.println("  headSet(30):    " + treeSet.headSet(30));     // [10, 20] — exclusive
        System.out.println("  tailSet(30):    " + treeSet.tailSet(30));     // [30, 40, 50] — inclusive
        System.out.println("  subSet(20,40):  " + treeSet.subSet(20, 40)); // [20, 30] — from incl, to excl

        // Descending view
        System.out.println("  descendingSet(): " + treeSet.descendingSet()); // [50, 40, 30, 20, 10]

        // pollFirst / pollLast — retrieve AND remove
        TreeSet<Integer> copy = new TreeSet<>(treeSet);
        System.out.println("\npollFirst(): " + copy.pollFirst() + " → remaining: " + copy);
        System.out.println("pollLast():  " + copy.pollLast()  + " → remaining: " + copy);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 6 — TreeSet with Custom Objects and Comparator
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * TREESET WITH CUSTOM OBJECTS
     * ----------------------------
     * TreeSet needs to compare elements to maintain sorted order.
     * Two ways to provide comparison:
     *
     * WAY 1: Natural ordering — element class implements Comparable<T>
     *   TreeSet uses compareTo() automatically.
     *
     * WAY 2: Custom Comparator — pass to TreeSet constructor
     *   TreeSet<T> set = new TreeSet<>(Comparator)
     *   This overrides natural ordering.
     *
     * IMPORTANT: TreeSet uses compareTo() / compare() for BOTH
     * ordering AND equality. It does NOT call equals().
     *
     * If compareTo() returns 0 → elements are considered equal → duplicate rejected.
     * This can cause unexpected behaviour if compareTo() is inconsistent with equals().
     */
    static void section6_TreeSetAdvanced() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 6: TreeSet with Custom Objects");
        System.out.println("─────────────────────────────────────────");

        // Natural ordering with Comparable
        record Student(String name, int score) implements Comparable<Student> {
            @Override
            public int compareTo(Student other) {
                // Primary sort: score descending (higher score first)
                int cmp = Integer.compare(other.score, this.score);
                // Secondary sort: name ascending (break ties alphabetically)
                return cmp != 0 ? cmp : this.name.compareTo(other.name);
            }
        }

        TreeSet<Student> leaderboard = new TreeSet<>();
        leaderboard.add(new Student("Alice", 95));
        leaderboard.add(new Student("Bob",   87));
        leaderboard.add(new Student("Carol", 95)); // same score as Alice — name breaks tie
        leaderboard.add(new Student("Dave",  72));
        leaderboard.add(new Student("Alice", 95)); // duplicate — compareTo returns 0

        System.out.println("Leaderboard (score desc, name asc):");
        int rank = 1;
        for (Student s : leaderboard) {
            System.out.printf("  #%d %-8s score=%d%n", rank++, s.name(), s.score());
        }

        // Custom Comparator — reverse alphabetical order
        TreeSet<String> reverseAlpha = new TreeSet<>(Comparator.reverseOrder());
        reverseAlpha.addAll(List.of("Banana", "Apple", "Mango", "Cherry"));
        System.out.println("\nReverse alphabetical: " + reverseAlpha);

        // Comparator.comparing() — sort strings by length, then alphabetically
        TreeSet<String> byLength = new TreeSet<>(
            Comparator.comparingInt(String::length)
                      .thenComparing(Comparator.naturalOrder())
        );
        byLength.addAll(List.of("Kiwi", "Fig", "Apple", "Banana", "Pear", "Mango"));
        System.out.println("By length then alpha: " + byLength);

        // TreeSet compareTo vs equals inconsistency demo
        System.out.println("\ncompareTo vs equals inconsistency warning:");
        // BigDecimal 2.0 and 2.00 are equals() but compareTo() != 0
        java.math.BigDecimal bd1 = new java.math.BigDecimal("2.0");
        java.math.BigDecimal bd2 = new java.math.BigDecimal("2.00");
        System.out.println("  new BigDecimal(\"2.0\").equals(new BigDecimal(\"2.00\"))     = "
                           + bd1.equals(bd2));
        System.out.println("  new BigDecimal(\"2.0\").compareTo(new BigDecimal(\"2.00\")) = "
                           + bd1.compareTo(bd2));
        TreeSet<java.math.BigDecimal> bdSet = new TreeSet<>();
        bdSet.add(bd1); bdSet.add(bd2);
        System.out.println("  TreeSet with both: " + bdSet
                           + " (size=" + bdSet.size() + ") ← compareTo=0 treated as duplicate");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 7 — LinkedHashSet
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * LINKEDHASHSET OVERVIEW
     * -----------------------
     * Backed by: LinkedHashMap<E, Object>
     *
     * LinkedHashMap extends HashMap and adds a DOUBLY LINKED LIST
     * running through all entries in INSERTION ORDER.
     *
     * So LinkedHashSet gives you:
     *   - HashSet's O(1) add/remove/contains (from the hash table)
     *   - Predictable iteration order (insertion order, from the linked list)
     *
     * Memory: slightly more than HashSet due to prev/next pointers per entry.
     *
     * When to use:
     *   - You need no duplicates AND need to maintain insertion order
     *   - Example: tracking unique page visits in order visited
     *   - Example: deduplicating a list while preserving element order
     */
    static void section7_LinkedHashSet() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 7: LinkedHashSet — Insertion Order");
        System.out.println("─────────────────────────────────────────");

        // Compare iteration order across all three Set implementations
        List<Integer> input = List.of(42, 7, 15, 3, 99, 7, 42, 18);
        System.out.println("Input:         " + input);

        Set<Integer> hashSet       = new HashSet<>(input);
        Set<Integer> linkedHashSet = new LinkedHashSet<>(input);
        Set<Integer> treeSet       = new TreeSet<>(input);

        System.out.println("HashSet:       " + hashSet       + " ← no order guaranteed");
        System.out.println("LinkedHashSet: " + linkedHashSet + " ← insertion order preserved");
        System.out.println("TreeSet:       " + treeSet       + " ← sorted order");

        System.out.println();

        // Real use: deduplication preserving order (useful for UI lists)
        List<String> pageVisits = List.of(
            "/home", "/products", "/home", "/cart", "/products", "/checkout"
        );
        Set<String> uniqueVisits = new LinkedHashSet<>(pageVisits);
        System.out.println("Page visits (raw):    " + pageVisits);
        System.out.println("Unique visits (order preserved): " + uniqueVisits);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 8 — Set Operations (Union, Intersection, Difference)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * MATHEMATICAL SET OPERATIONS
     * ----------------------------
     *
     * Union (A ∪ B):        all elements from both sets
     *   set1.addAll(set2)   → modifies set1 in place
     *
     * Intersection (A ∩ B): only elements present in BOTH sets
     *   set1.retainAll(set2) → modifies set1 in place
     *
     * Difference (A − B):   elements in A that are NOT in B
     *   set1.removeAll(set2) → modifies set1 in place
     *
     * Symmetric Difference (A △ B): elements in either but not both
     *   (A ∪ B) − (A ∩ B) → requires two steps
     *
     * IMPORTANT: addAll/retainAll/removeAll modify the SET IN PLACE.
     * Always work on copies if you need to preserve the original sets.
     *
     * Time complexity:
     *   All operations → O(n) where n = size of the argument collection
     *   Each element checked against the hash set → O(1) per element
     */
    static void section8_SetOperations() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 8: Set Operations");
        System.out.println("─────────────────────────────────────────");

        Set<Integer> setA = new HashSet<>(Set.of(1, 2, 3, 4, 5));
        Set<Integer> setB = new HashSet<>(Set.of(4, 5, 6, 7, 8));
        System.out.println("Set A: " + new TreeSet<>(setA)); // TreeSet for sorted display
        System.out.println("Set B: " + new TreeSet<>(setB));

        // Union — A ∪ B
        Set<Integer> union = new HashSet<>(setA);
        union.addAll(setB);
        System.out.println("\nUnion A ∪ B (addAll):          " + new TreeSet<>(union));

        // Intersection — A ∩ B
        Set<Integer> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        System.out.println("Intersection A ∩ B (retainAll): " + new TreeSet<>(intersection));

        // Difference — A − B
        Set<Integer> differenceAB = new HashSet<>(setA);
        differenceAB.removeAll(setB);
        System.out.println("Difference A − B (removeAll):   " + new TreeSet<>(differenceAB));

        // Difference — B − A
        Set<Integer> differenceBA = new HashSet<>(setB);
        differenceBA.removeAll(setA);
        System.out.println("Difference B − A (removeAll):   " + new TreeSet<>(differenceBA));

        // Symmetric difference — A △ B = (A ∪ B) − (A ∩ B)
        Set<Integer> symDiff = new HashSet<>(union);
        symDiff.removeAll(intersection);
        System.out.println("Symmetric Diff A △ B:           " + new TreeSet<>(symDiff));

        // Subset check
        Set<Integer> subset = new HashSet<>(Set.of(1, 2, 3));
        System.out.println("\nIs {1,2,3} a subset of A? " + setA.containsAll(subset)); // true
        System.out.println("Is {1,2,6} a subset of A? "
                + setA.containsAll(Set.of(1, 2, 6))); // false

        // Stream-based alternative (non-destructive, creates new sets)
        System.out.println("\nStream-based (non-destructive):");
        Set<Integer> streamUnion = Stream.concat(setA.stream(), setB.stream())
                                         .collect(Collectors.toSet());
        System.out.println("  Stream union: " + new TreeSet<>(streamUnion));

        Set<Integer> streamIntersect = setA.stream()
                                           .filter(setB::contains)
                                           .collect(Collectors.toSet());
        System.out.println("  Stream intersect: " + new TreeSet<>(streamIntersect));
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 9 — Real World: Duplicate Removal
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * REAL WORLD — REMOVING DUPLICATES FROM A LIST
     * ----------------------------------------------
     * Common interview question: remove duplicates from a list efficiently.
     *
     * Approach 1: new ArrayList<>(new HashSet<>(list))
     *   - O(n) time, O(n) space
     *   - Does NOT preserve order
     *
     * Approach 2: new ArrayList<>(new LinkedHashSet<>(list))
     *   - O(n) time, O(n) space
     *   - Preserves insertion order ← usually what you want
     *
     * Approach 3: stream().distinct()
     *   - O(n) time, O(n) space
     *   - Preserves encounter order (insertion order for lists)
     *   - Readable, functional style
     *
     * Naive approach: nested loops O(n²) — never use this.
     */
    static void section9_RealWorldDuplicateRemoval() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 9: Real World — Duplicate Removal");
        System.out.println("─────────────────────────────────────────");

        List<Integer> withDups = new ArrayList<>(
            List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9)
        );
        System.out.println("Original list: " + withDups);

        // Approach 1 — HashSet (fast, order lost)
        List<Integer> noOrder = new ArrayList<>(new HashSet<>(withDups));
        System.out.println("HashSet (no order):         " + noOrder);

        // Approach 2 — LinkedHashSet (fast, order preserved)
        List<Integer> withOrder = new ArrayList<>(new LinkedHashSet<>(withDups));
        System.out.println("LinkedHashSet (order kept): " + withOrder);

        // Approach 3 — stream().distinct() (readable, order preserved)
        List<Integer> streamDistinct = withDups.stream()
                                               .distinct()
                                               .collect(Collectors.toList());
        System.out.println("stream().distinct():        " + streamDistinct);

        // Real use case: deduplicate incoming API request IDs
        System.out.println("\nReal use: deduplicate incoming request IDs");
        List<String> requestIds = List.of(
            "req-001", "req-002", "req-001", "req-003", "req-002", "req-004"
        );
        Set<String> processedIds = new LinkedHashSet<>(requestIds);
        System.out.println("  Raw:        " + requestIds);
        System.out.println("  Unique IDs: " + processedIds);
        System.out.printf("  Skipped %d duplicate requests%n%n",
                          requestIds.size() - processedIds.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 10 — Real World: Leaderboard
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * REAL WORLD — LEADERBOARD WITH TREESET
     * ---------------------------------------
     * A game leaderboard needs:
     *   - No duplicate player entries (one entry per player)
     *   - Always sorted by score (descending)
     *   - Efficient score update (remove old, insert new)
     *   - Top N players query
     *
     * TreeSet is a natural fit:
     *   - Sorted always → no re-sort needed after insert
     *   - O(log n) insert and remove
     *   - Efficient range queries via headSet/tailSet/subSet
     */
    static void section10_RealWorldLeaderboard() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 10: Real World — Game Leaderboard");
        System.out.println("─────────────────────────────────────────");

        record PlayerScore(String player, int score) implements Comparable<PlayerScore> {
            @Override
            public int compareTo(PlayerScore other) {
                int cmp = Integer.compare(other.score, this.score); // desc by score
                return cmp != 0 ? cmp : this.player.compareTo(other.player); // asc by name
            }
        }

        TreeSet<PlayerScore> leaderboard = new TreeSet<>();
        leaderboard.add(new PlayerScore("Alice", 1500));
        leaderboard.add(new PlayerScore("Bob",   2300));
        leaderboard.add(new PlayerScore("Carol", 1800));
        leaderboard.add(new PlayerScore("Dave",  2300));
        leaderboard.add(new PlayerScore("Eve",    950));

        System.out.println("Initial leaderboard:");
        printLeaderboard(leaderboard);

        // Update Alice's score — remove old entry, insert new
        // (TreeSet has no update operation — must remove+add)
        leaderboard.remove(new PlayerScore("Alice", 1500));
        leaderboard.add(new PlayerScore("Alice", 2500));
        System.out.println("\nAfter Alice scores 2500:");
        printLeaderboard(leaderboard);

        // Top 3 players
        System.out.println("Top 3 players:");
        leaderboard.stream().limit(3).forEach(p ->
            System.out.printf("  %-8s %d%n", p.player(), p.score()));

        // Players with score above 2000
        System.out.println("Players scoring > 2000:");
        leaderboard.stream()
                   .filter(p -> p.score() > 2000)
                   .forEach(p -> System.out.printf("  %-8s %d%n", p.player(), p.score()));
        System.out.println();
    }

    static void printLeaderboard(TreeSet<?> lb) {
        int rank = 1;
        for (Object entry : lb) {
            System.out.printf("  #%d %s%n", rank++, entry);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 11 — Real World: Unique User IDs / Seen Tracking
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * REAL WORLD — UNIQUE VISITOR TRACKING
     * ---------------------------------------
     * Common in web analytics, fraud detection, and rate limiting:
     *   - Has this user/IP/session been seen before?
     *   - Track unique visitors to a page
     *   - Detect duplicate transactions
     *
     * HashSet is ideal:
     *   - O(1) average for contains() and add()
     *   - No ordering needed for "seen before" checks
     *   - Memory proportional to unique count, not total requests
     */
    static void section11_RealWorldUniqueUserIDs() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 11: Real World — Unique Visitor Tracking");
        System.out.println("─────────────────────────────────────────");

        // Simulate web request log
        List<String> requestLog = List.of(
            "user-101", "user-205", "user-101", "user-308",
            "user-205", "user-410", "user-101", "user-512",
            "user-308", "user-615", "user-205", "user-717"
        );

        Set<String>  uniqueVisitors    = new HashSet<>();
        int          totalRequests     = 0;
        int          duplicateRequests = 0;

        for (String userId : requestLog) {
            totalRequests++;
            boolean isNew = uniqueVisitors.add(userId); // O(1) avg
            if (!isNew) duplicateRequests++;
        }

        System.out.printf("Total requests:     %d%n", totalRequests);
        System.out.printf("Unique visitors:    %d%n", uniqueVisitors.size());
        System.out.printf("Duplicate requests: %d%n", duplicateRequests);
        System.out.println("Unique user IDs:    " + new TreeSet<>(uniqueVisitors));

        // Idempotency check — process each transaction ID only once
        System.out.println("\nIdempotency — process each transaction exactly once:");
        Set<String> processedTxns = new HashSet<>();
        List<String> txnQueue = List.of("txn-A", "txn-B", "txn-A", "txn-C", "txn-B");

        for (String txn : txnQueue) {
            if (processedTxns.add(txn)) { // add() returns false if already seen
                System.out.println("  ✅ Processing: " + txn);
            } else {
                System.out.println("  ⏭ Skipping duplicate: " + txn);
            }
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 12 — Custom Comparator Patterns
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * CUSTOM COMPARATOR PATTERNS FOR TREESET
     * ----------------------------------------
     * Comparator.reverseOrder()         → reverses natural ordering
     * Comparator.comparingInt(fn)        → compare by int field
     * Comparator.comparingLong(fn)       → compare by long field
     * Comparator.comparing(fn)           → compare by any Comparable field
     * comparator.thenComparing(fn)       → secondary sort key
     * comparator.reversed()              → flip any comparator
     * Comparator.nullsFirst(comparator)  → nulls sort before non-nulls
     * Comparator.nullsLast(comparator)   → nulls sort after non-nulls
     */
    static void section12_CustomComparator() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 12: Custom Comparator Patterns");
        System.out.println("─────────────────────────────────────────");

        // Reverse alphabetical
        TreeSet<String> revAlpha = new TreeSet<>(Comparator.reverseOrder());
        revAlpha.addAll(List.of("Banana", "Apple", "Mango", "Cherry", "Fig"));
        System.out.println("Reverse alpha:       " + revAlpha);

        // Sort strings by length, then alphabetically
        TreeSet<String> byLen = new TreeSet<>(
            Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())
        );
        byLen.addAll(List.of("Kiwi", "Apple", "Fig", "Mango", "Plum", "Banana"));
        System.out.println("By length then alpha:" + byLen);

        // Sort integers descending
        TreeSet<Integer> descInts = new TreeSet<>(Comparator.reverseOrder());
        descInts.addAll(List.of(5, 2, 8, 1, 9, 3, 7));
        System.out.println("Integers descending: " + descInts);

        // Case-insensitive string set — "Apple" and "apple" treated as same
        TreeSet<String> caseInsensitive = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitive.add("Apple");
        caseInsensitive.add("apple"); // duplicate by case-insensitive compare
        caseInsensitive.add("BANANA");
        caseInsensitive.add("banana"); // duplicate
        caseInsensitive.add("Mango");
        System.out.println("Case-insensitive:    " + caseInsensitive
                           + " (size=" + caseInsensitive.size() + ")");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 13 — Common Mistakes
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * COMMON MISTAKES WITH SET
     * -------------------------
     *
     * 1. Missing equals()/hashCode() override → duplicates in HashSet
     * 2. Mutating an element that is already in a HashSet → corruption
     * 3. Expecting sorted order from HashSet
     * 4. Adding null to TreeSet → NullPointerException
     * 5. Using mutable fields in hashCode → element "lost" in set
     * 6. compareTo() inconsistent with equals() in TreeSet
     */
    static void section13_CommonMistakes() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 13: Common Mistakes");
        System.out.println("─────────────────────────────────────────");

        // Mistake 1 — null in TreeSet
        System.out.println("Mistake 1: null in TreeSet");
        TreeSet<String> ts = new TreeSet<>();
        ts.add("Apple");
        try {
            ts.add(null); // TreeSet cannot compare null → NullPointerException
        } catch (NullPointerException e) {
            System.out.println("  ❌ TreeSet.add(null) → NullPointerException");
            System.out.println("  ✅ HashSet / LinkedHashSet allow one null element");
        }

        // Mistake 2 — mutating a key already in HashSet
        System.out.println("\nMistake 2: Mutating a HashSet element after insertion");
        class Tag {
            String label;
            Tag(String l) { label = l; }
            @Override public int hashCode() { return label.hashCode(); }
            @Override public boolean equals(Object o) {
                return o instanceof Tag t && label.equals(t.label);
            }
            @Override public String toString() { return label; }
        }

        Set<Tag> tags = new HashSet<>();
        Tag t = new Tag("java");
        tags.add(t);
        System.out.println("  Before mutation: " + tags + " contains \"java\": "
                           + tags.contains(t));
        t.label = "python"; // ← mutate the key in place
        System.out.println("  After mutation:  " + tags + " contains \"python\": "
                           + tags.contains(t));
        System.out.println("  ❌ Element is 'lost' — hashCode changed, wrong bucket");
        System.out.println("  ✅ Always use immutable fields in hashCode/equals");

        // Mistake 3 — expecting order from HashSet
        System.out.println("\nMistake 3: Expecting order from HashSet");
        Set<Integer> hset = new HashSet<>(List.of(5, 3, 1, 4, 2));
        System.out.println("  HashSet of [5,3,1,4,2]: " + hset + " ← order NOT guaranteed");
        System.out.println("  ✅ Use TreeSet for sorted, LinkedHashSet for insertion order");

        // Mistake 4 — Set.of() is immutable
        System.out.println("\nMistake 4: Set.of() is immutable");
        Set<String> immutable = Set.of("A", "B", "C");
        try {
            immutable.add("D");
        } catch (UnsupportedOperationException e) {
            System.out.println("  ❌ Set.of().add() → UnsupportedOperationException");
            System.out.println("  ✅ Use new HashSet<>(Set.of(...)) for a mutable copy");
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 14 — Performance Benchmark
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * PERFORMANCE COMPARISON
     * -----------------------
     *
     * HashSet:
     *   add/contains/remove  → O(1) average, O(n) worst (rare with Java 8 treeify)
     *   10M duplicate check  → O(n) total
     *
     * TreeSet:
     *   add/contains/remove  → O(log n) guaranteed
     *   10M element insert   → O(n log n) total
     *
     * LinkedHashSet:
     *   add/contains/remove  → O(1) average (same as HashSet)
     *   Slightly higher constant due to maintaining linked list
     *
     * For 10 million unique elements:
     *   HashSet fill  ≈ O(n)       = fastest
     *   TreeSet fill  ≈ O(n log n) = slower (each insert does log n comparisons)
     */
    static void section14_PerformanceBenchmark() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 14: Performance Benchmark");
        System.out.println("─────────────────────────────────────────");

        int n = 500_000;
        Random rng = new Random(42);
        int[] data = rng.ints(n, 0, n * 2).toArray();

        // HashSet benchmark
        long start = System.nanoTime();
        Set<Integer> hashSet = new HashSet<>();
        for (int v : data) hashSet.add(v);
        long hashTime = System.nanoTime() - start;

        // LinkedHashSet benchmark
        start = System.nanoTime();
        Set<Integer> linkedSet = new LinkedHashSet<>();
        for (int v : data) linkedSet.add(v);
        long linkedTime = System.nanoTime() - start;

        // TreeSet benchmark
        start = System.nanoTime();
        Set<Integer> treeSet = new TreeSet<>();
        for (int v : data) treeSet.add(v);
        long treeTime = System.nanoTime() - start;

        System.out.printf("Insert %,d elements (with duplicates):%n", n);
        System.out.printf("  HashSet:       %,8d ns  unique=%,d%n", hashTime, hashSet.size());
        System.out.printf("  LinkedHashSet: %,8d ns  unique=%,d%n", linkedTime, linkedSet.size());
        System.out.printf("  TreeSet:       %,8d ns  unique=%,d%n", treeTime, treeSet.size());
        System.out.printf("  TreeSet was %.1fx slower than HashSet (O(n log n) vs O(n))%n%n",
                          (double) treeTime / hashTime);

        // contains() benchmark
        int[] lookups = rng.ints(100_000, 0, n * 2).toArray();

        start = System.nanoTime();
        int hHits = 0; for (int v : lookups) if (hashSet.contains(v)) hHits++;
        long hLookup = System.nanoTime() - start;

        start = System.nanoTime();
        int tHits = 0; for (int v : lookups) if (treeSet.contains(v)) tHits++;
        long tLookup = System.nanoTime() - start;

        System.out.printf("100,000 contains() lookups:%n");
        System.out.printf("  HashSet  O(1):      %,8d ns  hits=%,d%n", hLookup, hHits);
        System.out.printf("  TreeSet  O(log n):  %,8d ns  hits=%,d%n", tLookup, tHits);
        System.out.printf("  TreeSet was %.1fx slower for contains()%n%n",
                          (double) tLookup / hLookup);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION 15 — Interview Summary
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * INTERVIEW ANSWERS
     * ------------------
     *
     * Q: Why is HashSet O(1) for add/contains/remove?
     * A: It uses a hash table. hashCode() computes the bucket index in O(1).
     *    In the best case, the bucket is empty or has one element → O(1).
     *    Amortized O(1) across all operations due to rehashing.
     *
     * Q: What is a collision and how does Java handle it?
     * A: A collision is when two different objects hash to the same bucket.
     *    Java uses chaining: multiple elements stored in the same bucket
     *    as a linked list. Java 8+ treeifies buckets with ≥ 8 entries
     *    into a Red-Black Tree, changing per-bucket worst case to O(log n).
     *
     * Q: Why is TreeSet O(log n)?
     * A: It uses a Red-Black Tree (a self-balancing BST). The tree height
     *    is always O(log n), guaranteeing that every operation (insert,
     *    find, delete) traverses at most O(log n) nodes.
     *
     * Q: Why Red-Black Tree specifically?
     * A: A plain BST degrades to O(n) for sorted input. Red-Black Tree
     *    self-balances using colour-based rotation rules, ensuring height
     *    ≤ 2*log₂(n+1). Other balanced BSTs exist (AVL, B-Tree) but
     *    Red-Black provides a good balance of rotation cost and height guarantee.
     *
     * Q: When to use LinkedHashSet?
     * A: When you need Set semantics (no duplicates, O(1) operations)
     *    but also need to iterate in insertion order. Example: unique
     *    items in a shopping cart maintaining the order they were added.
     *
     * Q: What happens if you don't override hashCode()?
     * A: Object.hashCode() returns memory-address-based hash.
     *    Two logically equal objects get different hashes → different buckets
     *    → both inserted → duplicates in the Set. This is a common bug.
     *
     * Q: Can TreeSet contain null?
     * A: No. Comparison (compareTo/Comparator) cannot handle null without
     *    special handling → NullPointerException. HashSet allows one null.
     */
    static void section15_InterviewSummary() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("SECTION 15: Interview Cheat Sheet");
        System.out.println("─────────────────────────────────────────");

        System.out.println("""
                HASHSET
                  Internal:     HashMap<E, Object> (PRESENT dummy value)
                  add():        O(1) avg, O(log n) worst (treeified bucket, Java 8+)
                  contains():   O(1) avg
                  remove():     O(1) avg
                  Order:        None guaranteed
                  Null:         One null allowed
                  Best for:     Deduplication, membership test, fast lookup
                  Requires:     hashCode() + equals() on element class
                
                TREESET
                  Internal:     TreeMap → Red-Black Tree
                  add():        O(log n)
                  contains():   O(log n)
                  remove():     O(log n)
                  Order:        Always sorted (natural or Comparator)
                  Null:         Not allowed
                  Best for:     Sorted unique elements, range queries
                  Requires:     Comparable or Comparator
                  Extras:       floor, ceiling, higher, lower, headSet, tailSet
                
                LINKEDHASHSET
                  Internal:     LinkedHashMap (HashMap + doubly linked list)
                  add():        O(1) avg
                  contains():   O(1) avg
                  remove():     O(1) avg
                  Order:        Insertion order preserved
                  Null:         One null allowed
                  Best for:     Deduplication with order preservation
                
                GOLDEN RULES
                  1. Always override equals() AND hashCode() together
                  2. Use immutable fields in hashCode/equals
                  3. Default to HashSet — fastest for most use cases
                  4. Use TreeSet when sorted order or range queries needed
                  5. Use LinkedHashSet when insertion order must be preserved
                  6. Set.of() and Set.copyOf() return immutable sets
                  7. TreeSet uses compareTo() for equality — not equals()
                  8. Use ArrayDeque for Queue/Stack (not Set)
                """);

        // Quick operation comparison
        System.out.println("Operation comparison at n=1,000,000:");
        System.out.println("  HashSet   add/contains: O(1)       → ~1 ns per op");
        System.out.println("  TreeSet   add/contains: O(log n)   → ~20 ns per op (20 comparisons)");
        System.out.println("  LinkedHashSet:          O(1) + ptr → ~2 ns per op");
        System.out.println("\n  For duplicate check on 10M records:");
        System.out.println("  HashSet  → O(n)       ← use this");
        System.out.println("  TreeSet  → O(n log n) ← use only if sorted order needed");
        System.out.println("\n=== END OF SET DEEP DIVE ===");
    }
}
