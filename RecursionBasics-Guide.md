# Recursion & Mathematical Thinking in Java
---

## How to Run

```bash
javac RecursionBasics.java
java RecursionBasics
```

> Requires Java 8 or higher. Check with `java -version`.

---

## What is Recursion?

Recursion is when a **function calls itself** to solve a smaller version of the same problem.

Think of it like **Russian nesting dolls (Matryoshka)**:
- You open a doll → find a smaller doll → open that → find a smaller one...
- Until you reach the **tiny solid doll that doesn't open** → that's the **base case**.

Every recursive function needs exactly **2 things**:

| Part | What it means |
|------|--------------|
| **Base Case** | The stopping condition. Without it → infinite loop → `StackOverflowError` 💀 |
| **Recursive Case** | Call yourself with a **smaller / simpler** input |

> **Golden Rule:** Every recursive call MUST move closer to the base case.

---

## Code Structure Overview

```
RecursionBasics.java
│
├── factorial(int n)           → Recursive factorial
├── factorialIterative(int n)  → Same thing with a loop (for comparison)
│
├── fibRecursive(int n)        → Naive recursive Fibonacci (slow)
├── fibMemo(int n)             → Memoized Fibonacci (fast)
│   ├── memo[]                 → Array storing computed fib values
│   └── computed[]             → Array tracking what's been calculated
│
└── main()                     → Runs and prints all demos
```

---

## Example 1 — Factorial

### What is factorial?

```
5! = 5 × 4 × 3 × 2 × 1 = 120
4! = 4 × 3 × 2 × 1     = 24
1! = 1
0! = 1   ← defined by convention in mathematics
```

### Mathematical Recurrence Relation

Notice the repeating pattern:
```
5! = 5 × 4!
4! = 4 × 3!
3! = 3 × 2!
2! = 2 × 1!
1! = 1         ← STOP (base case)
```

This gives us the formal definition:
```
factorial(n) = 1                       if n == 0 or n == 1   ← BASE CASE
factorial(n) = n × factorial(n - 1)                          ← RECURSIVE CASE
```

This mathematical definition maps **directly** into Java code — line for line.

### The Code

```java
static long factorial(int n) {

    // BASE CASE: we know the answer — no more recursion needed
    if (n == 0 || n == 1) {
        return 1;
    }

    // RECURSIVE CASE: n! = n × (n-1)!
    // We trust factorial(n-1) to work correctly for the smaller input
    return n * factorial(n - 1);
}
```

**Why `long` instead of `int`?**
Factorials grow extremely fast. `10! = 3,628,800` and `20! = 2,432,902,008,176,640,000`. An `int` would overflow — `long` handles up to ~9.2 × 10¹⁸.

### Call Stack Trace for `factorial(4)`

```
factorial(4)
  └─ returns 4 × factorial(3)
                 └─ returns 3 × factorial(2)
                                └─ returns 2 × factorial(1)
                                               └─ returns 1   ← BASE CASE hit
                                └─ 2 × 1 = 2   ↑ unwinds here
                 └─ 3 × 2 = 6   ↑ unwinds here
  └─ 4 × 6 = 24  ✓
```

The stack **builds down** while calling, then **unwinds back up** returning values — like stacking plates and then picking them off one by one.

### What happens WITHOUT a base case?

```java
// DANGEROUS — no base case!
static long factorial(int n) {
    return n * factorial(n - 1);  // never stops!
}
```

```
factorial(4) → factorial(3) → factorial(2) → factorial(1)
→ factorial(0) → factorial(-1) → factorial(-2) → ... forever
→ Exception in thread "main" java.lang.StackOverflowError  💀
```

### Iterative Version (for comparison)

```java
static long factorialIterative(int n) {
    long result = 1;
    for (int i = 2; i <= n; i++) {
        result *= i;
    }
    return result;
}
```

Both give **identical results**. The recursive version mirrors the math more cleanly. The iterative version is slightly more memory-efficient (no call stack overhead).

### Output Table (from `main()`)

```
┌────┬────────────────┬──────────────────┐
│  n │  Recursive     │  Iterative       │
├────┼────────────────┼──────────────────┤
│  0 │              1 │                1 │
│  1 │              1 │                1 │
│  2 │              2 │                2 │
│  3 │              6 │                6 │
│  4 │             24 │               24 │
│  5 │            120 │              120 │
│ 10 │        3628800 │          3628800 │
└────┴────────────────┴──────────────────┘
```

---

## Example 2 — Fibonacci Sequence

### What is Fibonacci?

Each number equals the **sum of the two numbers before it**.

```
Sequence:  0,  1,  1,  2,  3,  5,  8,  13,  21,  34,  55 ...
Index:     0   1   2   3   4   5   6    7    8    9   10
```

**Where does it appear in real life?**
- Flower petals (sunflowers always have 34, 55, or 89 petals)
- Spiral shells (nautilus)
- Tree branch growth patterns
- Stock market analysis
- Signal processing and compression algorithms

### Mathematical Recurrence Relation

```
fib(0) = 0                          ← BASE CASE 1
fib(1) = 1                          ← BASE CASE 2
fib(n) = fib(n-1) + fib(n-2)       ← RECURSIVE CASE
```

**Why TWO base cases?**
Fibonacci depends on the **previous two values**. If we only defined `fib(0) = 0`, then `fib(1)` would try to compute `fib(0) + fib(-1)` — which doesn't exist. Both `fib(0)` and `fib(1)` must be anchored explicitly.

### Version A — Naive Recursive (easy to understand)

```java
static long fibRecursive(int n) {
    if (n == 0) return 0;    // BASE CASE 1
    if (n == 1) return 1;    // BASE CASE 2

    return fibRecursive(n - 1) + fibRecursive(n - 2);  // RECURSIVE CASE
}
```

### Call Tree for `fib(5)`

```
                    fib(5)
                  /        \
              fib(4)       fib(3)
             /     \       /    \
         fib(3)  fib(2) fib(2) fib(1)=1
         /   \    /  \   /  \
      fib(2) f(1) f(1)f(0) f(1)f(0)
       /  \   =1   =1  =0   =1  =0
     f(1) f(0)
      =1   =0
```

Adding up from the leaves → `fib(5) = 5` ✓

**The hidden problem:** Look closely — `fib(3)` is computed **twice**, `fib(2)` is computed **three times**. The same work is being repeated over and over.

For `fib(50)`, this naive version would make **2⁵⁰ ≈ 1 trillion calls**. On modern hardware, that takes minutes.

### Version B — Memoized (fast)

**Memoization** = cache the result the first time, look it up every time after.

```java
static long[] memo = new long[50];        // stores fib(i) results
static boolean[] computed = new boolean[50]; // tracks what we've done

static long fibMemo(int n) {
    if (n == 0) return 0;
    if (n == 1) return 1;

    // Already computed? Return instantly from cache
    if (computed[n]) return memo[n];

    // First time? Compute it, store it, return it
    memo[n] = fibMemo(n - 1) + fibMemo(n - 2);
    computed[n] = true;
    return memo[n];
}
```

**Why a separate `computed[]` boolean array?**
Because `fib(0) = 0`, and `memo[0]` starts as `0` too — so we can't tell if `memo[0]` was computed or just has its default value. The `computed[]` flag removes this ambiguity cleanly.

### Performance Comparison

```
fib(40) results:

Naive recursive  → 102334155   Time: ~800 ms   (makes ~330 million calls!)
Memoized         → 102334155   Time: ~0 ms     (makes only 40 calls!)
```

Same answer. Wildly different performance. This is the point where **mathematical thinking meets engineering discipline**.

| | Naive Recursive | Memoized |
|--|--|--|
| Time complexity | O(2ⁿ) — exponential | O(n) — linear |
| Space complexity | O(n) call stack | O(n) memo array |
| fib(10) calls | 177 | 10 |
| fib(40) calls | ~330 million | 40 |
| fib(50) calls | ~1 trillion | 50 |

---

## How `main()` Works

The `main()` method is purely for **demonstration** — it doesn't contain any logic. It:

1. Prints a step-by-step manual trace of `factorial(5)` so students can follow along
2. Runs both factorial versions from `n = 0` to `n = 10` and prints a comparison table
3. Prints the first 15 Fibonacci numbers in sequence
4. Runs both Fibonacci versions from `n = 0` to `n = 15` in a table
5. Times both Fibonacci versions on `fib(40)` to show the performance gap live

The `System.currentTimeMillis()` calls around `fibRecursive(40)` and `fibMemo(40)` capture real wall-clock time so students see the difference in their own terminal.

---

## Key Takeaways

**1. Always define the base case first**
It's the anchor of the entire function. Writing recursive code without it first is like building a staircase with no floor.

**2. Each call must move toward the base case**
`factorial(n)` calls `factorial(n-1)` — n decreases every time. If n didn't decrease, you'd never reach the base case.

**3. Recursion mirrors mathematics directly**
The math definition `f(n) = n × f(n-1)` becomes the Java code `return n * factorial(n - 1)`. One maps to the other, character for character.

**4. Naive recursion can be dangerously slow**
`fibRecursive(50)` would take longer than your lifetime. Recognising redundant computation and applying memoization is a fundamental engineering skill.

**5. The call stack is memory**
Every recursive call occupies a stack frame. Very deep recursion (thousands of levels) can cause a `StackOverflowError`. For such cases, iterative solutions or tail-recursion-optimized languages are preferred.

---

## Quick Reference

```java
// Template for any recursive function
static returnType solve(input) {

    // Step 1: Base case — stop condition
    if (simplestPossibleInput) {
        return knownAnswer;
    }

    // Step 2: Recursive case — solve smaller version, build answer on top
    return combine(currentPart, solve(smallerInput));
}
```

---

## File Reference

| Method | Lines | Purpose |
|--------|-------|---------|
| `factorial(n)` | 74–87 | Recursive factorial |
| `factorialIterative(n)` | 93–99 | Iterative factorial for comparison |
| `fibRecursive(n)` | 150–158 | Naive Fibonacci — O(2ⁿ) |
| `fibMemo(n)` | 166–177 | Memoized Fibonacci — O(n) |
| `main()` | 184–274 | Demonstration and output |
