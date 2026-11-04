# Split Bill API

Java 21, Spring Boot, Maven, PostgreSQL and Flyway. Equal and exact splits use BigDecimal.
Amounts use whole rupiah. Rounding remainders go to participants in their saved
order. Fractional expenses are rejected, and service charges round down to whole
rupiah without carrying the discarded fraction forward.

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

## Exact split

Gunakan `allocations` sebagai pengganti `participantIds` untuk pembagian nominal
manual. Jangan mengirim kedua field sekaligus. Contoh Bob membayar Rp100.000:

```sh
curl -b cookies.txt -X POST "$BASE/bill-groups/$GROUP_ID/expenses" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: dinner-1' \
  -d "{\"payerId\":\"$BOB_ID\",\"amount\":100000,\"description\":\"Dinner\",\"allocations\":{\"$ALICE_ID\":50000,\"$BOB_ID\":30000,\"$CARA_ID\":20000}}"
```

Payer harus berada dalam grup, tetapi tidak harus menerima share. Alokasi bernilai
nol diperbolehkan. Endpoint shares dan settlement tetap sama.

## Tests

```sh
./mvnw test
```

Twelve JUnit tests cover equal and exact splits, invalid amounts, settlement,
empty groups, balanced payments, multiple creditors, and service charge. No database is needed for unit tests.
The endpoints can be tried in Swagger UI.

## Personalization

GitHub: `Zidan-Kharisma-Sakana`. Lowercase character sum: **2095**. Service charge:
**2095 % 10 = 5%**. The code calculates this from the username. For total expenses
of 135,000 IDR, the separate charge is 6,750 IDR.

## Design question

Dengan waktu assignment yang terbatas, saya fokus ke konsistensi expense,
retry yang aman, dan perhitungan uang. Tiga keputusan utamanya:

### 1. Transaksi expense dan recovery race condition

**Atomic write:**

1. Expense dan seluruh share disimpan dalam satu `TransactionTemplate`.
2. Kalau penyimpanan share gagal, expense ikut rollback.
3. Kalau semuanya berhasil, transaksi commit. `saveAllAndFlush()` sendiri hanya melakukan flush.

**Dua request dengan grup dan key yang sama:**

Bisa terjadi saat:

- Client timeout karena jaringan lambat atau respons belum sampai, lalu retry padahal request pertama masih diproses server.
- User double-click tombol submit dan client mengirim dua request dengan key yang sama. Kalau key-nya berbeda, proteksi ini tidak mencegah duplicate expense.

Alurnya:

1. Request A dan B mengecek key, keduanya belum menemukan expense.
2. A berhasil menyimpan dan commit.
3. B mencoba menyimpan, tapi ditolak `UNIQUE (bill_group_id, idempotency_key)`.
4. Setelah B rollback, aplikasi membuka transaksi baru dan mengembalikan expense milik A, dengan access check ulang.

- Client harus mempertahankan key yang sama selama retry.
- Recovery hanya untuk SQLSTATE `23505` pada constraint expense key. Error lain tetap diteruskan.
- **Tradeoff:** validation, persistence, dan retry handling masih di ExpenseService. Alurnya lebih padat dan deteksi constraint PostgreSQL-specific.
- `ExpenseController.create()` dan `ExpenseService.create()` tidak memakai `@Transactional`, jadi transaksi write selesai rollback sebelum transaksi recovery dimulai.

**Settlement masih punya potensi inconsistent read:**

1. Settlement membaca expenses, expense baru belum ada.
2. Request lain commit expense baru beserta shares.
3. Settlement membaca shares, shares baru sudah ikut.
4. Utang baru ikut dihitung, tapi pembayarannya belum. Total balance bisa non-zero.

- Proteksi duplicate write belum berarti seluruh flow bebas race condition.
- Perbaikan yang belum dibuat: satu transaksi `REPEATABLE_READ` untuk seluruh pembacaan settlement.
- `readOnly = true` saja belum menjamin snapshot yang sama.

### 2. Presisi uang dan rounding

Saya pakai `BigDecimal` dan `NUMERIC`, dengan kebijakan whole rupiah.
`10000.00` diterima lalu dinormalisasi memakai `setScale(0, UNNECESSARY)`,
sedangkan `10000.50` ditolak.

| Operasi | Keputusan | Konsekuensi |
|---|---|---|
| Equal split | Round down, lalu distribusikan sisa sesuai saved participant order | `10000 / 3` menjadi `3334, 3333, 3333`, tapi peserta awal bisa berulang kali mendapat tambahan rupiah |
| Exact split | Total allocation harus persis sama dengan expense | Tidak ada silent adjustment pada nominal yang diinput client |
| Service charge | Hitung dari total expense, lalu `DOWN` sekali ke whole rupiah | Tidak overcharge, tapi pecahan kurang dari Rp1 dilepas per perhitungan |

Kedua mode menghasilkan struktur share yang sama untuk settlement. Share nol
diperbolehkan. Rotasi sisa belum dibuat karena butuh state tambahan.

Charge 5% dari Rp10.019 menjadi Rp500, terpisah dari transfer peserta.
`HALF_UP` dan `HALF_EVEN` bisa membulatkan naik, jadi tidak dipakai. Data historis yang masih pecahan
ditolak saat settlement, bukan dibulatkan diam-diam.

### 3. Production concerns yang deferred

Testing gap terbesar. Unit test baru di calculator, API checks masih lokal, dan belum ada coverage report.
Prioritas pertama yaitu regression tests otomatis, terutama transaksi, concurrency, dan access control.

Lainnya:
- **Error handling:**  kontrak & classification yang jelas dan deskriptif.
- **Distributed tracing:** butuh OpenTelemetry dan context propagation untuk melacak request antarservice.
- **Metrics dan monitoring:** latency, error rate.
- **Logging:**  structured logs & centralized collection
- **Health checks:** application readiness dan liveness checks.
- **Coverage report** butuh pengukuran line dan branch coverage untuk melihat bagian yang belum teruji, bukan sekadar mengejar persentase.
- **Read consistency:** butuh snapshot yang konsisten saat menghitung settlement.
- **Identity:** butuh accounts dan persistent sessions untuk akses yang lebih durable.
- **Abuse protection:** butuh rate limiting untuk invite dan write endpoints.
- **Pagination:** butuh pembacaan bertahap supaya list tidak selalu dimuat seluruhnya.

## AI disclosure

I used AI to help with documentation, wording, variable & name choices, reviews,
and discussions of edge cases.

I set the overall direction for data modeling, API design, monetary handling, and code structure, and made the final design and tradeoff decisions.
