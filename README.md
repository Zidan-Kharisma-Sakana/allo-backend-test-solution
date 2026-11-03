# Split Bill API

Java 21, Spring Boot, Maven, PostgreSQL and Flyway. Equal splits use BigDecimal.
Rounding remainders go to participants in their saved order.

## Run

With Docker:

```sh
docker compose up --build
```

Or with JDK 21 and a local database:

```sh
docker compose up -d db
export DB_PASSWORD=split_bill_local
./mvnw clean verify
./mvnw spring-boot:run
```

Default database: `jdbc:postgresql://localhost:5432/split_bill`, user `split_bill`.
Override `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` as needed. The Compose password
is for local development. Flyway applies migrations on startup.

Packaged app: `./mvnw package`, then
`java -jar target/split-bill-0.0.1-SNAPSHOT.jar` with the same database variables.
On Windows use `mvnw.cmd`.

API: http://localhost:4110. [Swagger UI](http://localhost:4110/swagger-ui/index.html).
[OpenAPI JSON](http://localhost:4110/v3/api-docs).

## Requests

Keep the same cookie file. Groups are accessible to their creator and sessions
that join with the invite code. Session access is lost after expiry or restart.
Replace the example IDs and invite code with values from responses.

```sh
BASE=http://localhost:4110
curl "$BASE/hello"

# Create group. Copy its id and inviteCode.
curl -c cookies.txt -b cookies.txt -X POST "$BASE/bill-groups" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Weekend","participants":["Alice","Bob","Cara"]}'

GROUP_ID=replace-with-group-id
curl -b cookies.txt "$BASE/bill-groups"
curl -b cookies.txt "$BASE/bill-groups/$GROUP_ID"
curl -b cookies.txt "$BASE/bill-groups/$GROUP_ID/participants"

# Copy participant IDs from the preceding response.
ALICE_ID=replace-with-alice-id
BOB_ID=replace-with-bob-id
CARA_ID=replace-with-cara-id


# Join from a different session using its own cookie file.
curl -c cookies-guest.txt -b cookies-guest.txt -X POST "$BASE/bill-groups/join" \
  -H 'Content-Type: application/json' -d '{"code":"REPLACE_WITH_INVITE_CODE"}'

# A fresh key for each expense. Reuse the same key and body when retrying.
curl -b cookies.txt -X POST "$BASE/bill-groups/$GROUP_ID/expenses" \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: lunch-1' \
  -d "{\"payerId\":\"$ALICE_ID\",\"amount\":100000.00,\"description\":\"Lunch\",\"participantIds\":[\"$ALICE_ID\",\"$BOB_ID\",\"$CARA_ID\"]}"

EXPENSE_ID=replace-with-expense-id
curl -b cookies.txt "$BASE/bill-groups/$GROUP_ID/expenses"
curl -b cookies.txt "$BASE/bill-groups/$GROUP_ID/expenses/$EXPENSE_ID/shares"
curl -b cookies.txt "$BASE/bill-groups/$GROUP_ID/settlement"
```

Missing or inaccessible resources return 404. Reusing an expense key returns
the original expense. Use a new key for each new expense. Settlement returns participant balances and suggested transfers.
The service charge is separate from those transfers.
Errors include `status` and `message`. Validation
errors return 400 and inaccessible resources return 404. Unexpected failures return 500 with a generic message.

## Tests

```sh
./mvnw test
```

Five plain JUnit cases cover settlement, empty groups, balanced payments, multiple
creditors, and service charge. No database is needed for unit tests.
The endpoints can be tried in Swagger UI.

## Personalization

GitHub: `Zidan-Kharisma-Sakana`. Lowercase character sum: **2095**. Service charge:
**2095 % 10 = 5%**. The code calculates this from the username. For total expenses
of 135,000 IDR, the separate charge is 6,750 IDR.

## Design question

What was the hardest design decision you made while building this, and what
trade-off did you accept?
