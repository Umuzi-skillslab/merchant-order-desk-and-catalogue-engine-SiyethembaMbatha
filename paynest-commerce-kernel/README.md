# PayNest Commerce Kernel — Capstone 1

Minimal, framework-free Java commerce kernel: products with prices, a
customer, an order made of line items, and a grand total printed
as a readable receipt.

```
## How to run the demo

```bash
cd paynest
mvn compile exec:java


## How to run the tests

```bash
mvn test
```

This exercises:
- line subtotal = unit price × quantity, including a single-unit line
- constructor guards (zero/negative quantity, null product) throw `IllegalArgumentException`
- an empty order totals `0.0`
- grand total = sum of line subtotals across multiple lines
- a rejected `addItem` call does not change the order's total or item list
- `getItems()` returns an unmodifiable view, so callers can't corrupt totals by mutating the backing list directly
- `printSummary()` contains the customer name, each product line, and the grand total

## why it's shaped this way

- **`OrderItem` is its own class** rather than Order tracking two parallel
  lists (products + quantities). This keeps the per-line subtotal
  calculation next to the data it depends on, and gives future per-line
  concerns (discounts) a home without touching
  `Product` or `Order`'s public API.
- **`Order` never exposes its mutable list.** `getItems()` returns
  `Collections.unmodifiableList(...)`, so the only way to change an order's
  contents is `addItem(Product, int)`, which validates quantity > 0. 

- **`calculateTotal()` is the single definition of "what the customer
  owes."** `printSummary()` calls it rather than re-summing the lines a
  second way, so the printed grand total can never drift from the value the
  rest of the system would compute.
- **`OrderService` (not `Order`'s constructor) owns id assignment.** This
  keeps `Order` a plain data/behaviour owner and gives a natural extension
  point later (persistence, distributed id generation) without changing
  `Order`.
- **Extending with new product fields (e.g. SKU, category, tax code) doesn't
  touch checkout math**, because `OrderItem.calculateTotal()` only ever
  reads `product.getPrice()`. Adding fields to `Product` has no effect on
  `Order`/`OrderItem` unless the pricing formula itself changes.

