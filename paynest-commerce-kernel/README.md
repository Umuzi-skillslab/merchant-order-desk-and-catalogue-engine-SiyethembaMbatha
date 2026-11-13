# PayNest Commerce Kernel — Capstone 1 & 3

Minimal, framework-free Java 21 commerce kernel: products with prices, a
customer, an order made of line items, a grand total printed as a readable
receipt, and **one checkout funnel that accepts any payment rail**.

## How to run the demo

```bash
cd paynest-commerce-kernel
mvn compile exec:java
```

The demo (`PayNestApplication`) builds the Capstone 1 order (a laptop ×1 and a
wireless mouse ×3), then checks it out with `new CardPayment()` through the
`PaymentMethod` interface type. Expected output:

```
Order #1 for Thandiwe Nkosi (thandiwe@example.co.za)
  Laptop               x1   R12,000.00
  Wireless Mouse       x3    R1,050.00
  ------------------------------------
  Grand Total              R13,050.00

Processing card payment for R13,050.00
Payment successful via CARD
Amount: R13,050.00
Order #1 complete for Thandiwe Nkosi via CARD (R13,050.00)
```

The output shows the order summary, the payment rail, the payment amount and
the order completion.

To demonstrate polymorphism, change only the construction line in
`PayNestApplication` (and its import) to `new EftPayment()` or
`new WalletPayment()`. The rest of the checkout code stays the same.

## How to run the tests

```bash
mvn test
```

Requires Java 21 and Maven. No database or network is needed.

This exercises:

**Catalogue, customer and order lines**
- `Product`: valid fields, a free product is allowed, blank/null name and null/negative prices are rejected, prices are stored with exactly two decimal places, and sub-cent prices (e.g. `9.999`) are rejected rather than rounded
- `Customer`: valid fields, blank/null name and email are rejected
- line subtotal = unit price × quantity, including a single-unit line
- constructor guards (zero/negative quantity, null product) throw `IllegalArgumentException`

**Order**
- an empty order totals `0.0`
- grand total = sum of line subtotals across multiple lines
- totals are exact (`0.10 + 0.20` is `0.30`, not `0.30000000000000004`) and always carry two decimal places, including the empty order (`0.00`)
- a rejected `addItem` call does not change the order's total or item list
- `getItems()` returns an unmodifiable view, so callers can't corrupt totals by mutating the backing list directly
- `printSummary()` contains the customer name, each product line, and the grand total
- `OrderService` hands out incrementing ids and rejects a null customer

**Order lifecycle (business rules)**
- a new order is unpaid; a successful checkout marks it paid
- a declined payment leaves it unpaid, and checkout can be retried
- a paid order cannot be charged again (the processor is never called) and cannot be changed
- an empty order cannot be checked out or charged

**Payment rails**
- `CardPayment`, `EftPayment` and `WalletPayment` each succeed, report their own type label (`CARD`, `EFT`, `WALLET`) and print their own message
- `PaymentProcessor` reports success for any `PaymentMethod` and prints the rail and amount
- `PaymentProcessor` reports failure when a rail declines, and rejects a null method and null or negative amounts

**Checkout**
- the amount passed to the payment method equals `order.calculateTotal()` exactly (R13,050.00 for the demo order), including a case where `double` arithmetic would have drifted
- checkout works with card, EFT and wallet (and with an ad-hoc test rail), so polymorphism is actually used
- successful checkout prints the summary, rail, amount and order completion
- a declined payment makes `checkout` return `false`, prints the failure message, and leaves the order unchanged
- a null payment method or null processor is rejected
- a supplied `PaymentProcessor` is called exactly once with the order total

**Design checks**
- `NewRailExtensibilityTest` adds a `BuyNowPayLaterPayment` as one new class (test-only) and checks it out through the existing, unchanged code
- `DependencyRuleTest` reads the sources and fails the build if an inner layer imports an outer one, or if anything except `app` names a concrete rail
- `PayNestApplicationTest` runs the demo and checks the summary, rail, amount and completion lines
- `MoneyTest` and the checkout tests check that rand amounts print as `R13,050.00` everywhere

## Architecture

The structure follows the onion idea from the PayNest onion skeleton: **dependencies point inward only**.

```
  app        composition root: the only place that picks a concrete rail
   |
  service    OrderService: creates orders with ids
   |
  domain     Customer, Product, OrderItem, Order: business rules and lifecycle
   |
  payment    PaymentMethod (the port), PaymentProcessor, and the rails
             CardPayment / EftPayment / WalletPayment (the adapters)
   |
  util       Money: how rand amounts are formatted
```

| Rule | Where it is enforced |
|---|---|
| `util` imports nothing from the project | `DependencyRuleTest` |
| `payment` knows amounts, never orders, services or the app | `DependencyRuleTest` |
| `domain` never knows `service` or `app` | `DependencyRuleTest` |
| Only `app` names `CardPayment`, `EftPayment`, `WalletPayment` | `DependencyRuleTest` |
| An order can only be paid through `checkout`, and only once | `Order` + `OrderLifecycleTest` |

The brief fixes the payment classes in `com.paynestsystem.payment`, so the
port and its adapters share that package. The architecture test therefore
guards how they are *used*: `Order` and `PaymentProcessor` see only the
`PaymentMethod` interface.

Not included, because Capstone 3 does not ask for them: an `OrderRepository`
port with a persistence adapter, and a web layer with DTOs. The same pattern
would add them (a new port in the inner ring, an adapter in the outer ring,
wired in `app`).

## Why it's shaped this way

- **`OrderItem` is its own class** rather than Order tracking two parallel
  lists (products + quantities). This keeps the per-line subtotal
  calculation next to the data it depends on, and gives future per-line
  concerns (discounts) a home without touching
  `Product` or `Order`'s public API.
- **`Order` never exposes its mutable list.** `getItems()` returns
  `Collections.unmodifiableList(...)`, so the only way to change an order's
  contents is `addItem(Product, int)`, which validates quantity > 0 and
  refuses changes once the order is paid.
- **`calculateTotal()` is the single definition of "what the customer
  owes."** `printSummary()` and `checkout()` both call it rather than
  re-summing the lines a second way, so the printed grand total and the
  amount charged can never drift apart.
- **`OrderService` (not `Order`'s constructor) owns id assignment.** This
  keeps `Order` free of id-generation concerns and gives a natural extension
  point later (persistence, distributed id generation) without changing
  `Order`.
- **`Order` depends on the `PaymentMethod` interface, not on any rail.**
  `checkout(PaymentMethod)` uses a default `PaymentProcessor`, and
  `checkout(PaymentMethod, PaymentProcessor)` lets a different processor be
  supplied (this is how the tests observe exactly what gets charged).
  `Order` also prints the checkout narrative to the console, as the brief
  requires; the arithmetic itself stays in `calculateTotal()`.
- **Checkout is safe to call twice.** Success marks the order paid, and a
  paid order is rejected before any charge happens, so a customer can't be
  charged twice. A declined payment leaves the order unpaid so it can be retried.
- **`Money.formatRand` is the one place amounts are formatted**, so a rail
  message can never show `R13050.00` while the receipt shows `R13,050.00`.
- **Extending with new product fields (e.g. SKU, category, tax code) doesn't
  touch checkout math**, because `OrderItem.calculateTotal()` only ever
  reads `product.getPrice()`. Adding fields to `Product` has no effect on
  `Order`/`OrderItem` unless the pricing formula itself changes.
- **Money is `BigDecimal`, not `double`.** Binary floating point can't represent
  values like R0.10 exactly (`0.1 + 0.2` is `0.30000000000000004`), and the
  error grows with every sum or charge. Prices, line subtotals, the order total
  and the amount passed to `PaymentMethod.processPayment(BigDecimal amount)` are
  all exact. Rules that follow from this:
  - prices have at most two decimal places (cents). A price like `9.999` is
    rejected instead of being silently rounded;
  - every stored price and every total has exactly two decimal places, so
    `new BigDecimal("13050.00")` is what a checkout charges, and amounts are
    always built from strings (`new BigDecimal("350.00")`), never from `double`;
  - the brief's starter code used `processPayment(double amount)`. This is a
    deliberate, tested migration of that one type; the method names, the
    `boolean` return value and the "charge exactly the order total" rule are
    unchanged.

## Why interfaces beat a single mega-method

Using a big method with if/else or switch statements for every payment type would keep growing the method every time a new payment option is added and would risk breaking existing payment types by editing it. Instead using the PaymentMethod interface is better so Order and Payment processor just call processPayment() & getPaymentType() and each payment class handles its own details separately. So if I had to add BuyNowPayLater I'd just create one new class without changing any existing code or risking breaking any existing payment types.

```
              <<interface>>
              PaymentMethod
        --------------------------
        + processPayment(amount)
        + getPaymentType()
                  /\
                  ||
      ____________||_____________
     ||           ||            ||
      CardPayment  EftPayment  WalletPayment
```
