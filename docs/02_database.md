# Database


## MVP 1

- pakai PostgreSQL + Flyway buat migration
- `bill_groups`: nama trip, currency IDR, session ID pembuat
- `participants`: peserta per trip, pakai UUID. nama boleh sama
- `expenses`: pembayar, nominal, trip
- `expense_shares`: peserta yang ikut pengeluaran + nominal bagian masing-masing
- validasi di service: pembayar dan peserta pengeluaran harus dari trip yang sama
- 1 session bisa punya beberapa trip, 1 trip hanya punya 1 owner
- session masih di memory. kalau expired / server restart, akses trip bisa hilang
- belum ada tabel akun, invite, atau payment

## Uang / split

- IDR saja. Java pakai BigDecimal, database NUMERIC. precision / scale ditambahkan nanti
- validasi di aplikasi: nominal pengeluaran harus > 0, bagian peserta boleh 0
- equal split saja. sisa pembulatan dibagi sesuai urutan peserta
- simpan expense + semua shares dalam 1 transaction
- validasi di service: peserta trip dan penerima pengeluaran tidak boleh kosong
- validasi precision / scale ditambahkan nanti. total shares harus sama dengan expense
- summary dihitung dari expenses + shares, tidak perlu tabel sendiri
- service charge dihitung dari username GitHub, terpisah dari utang peserta

## Local

- container: `allo-split-bill-postgres`, port localhost `55432`
- database / user: `split_bill`
- Flyway jalan saat app startup. setelah schema awal selesai, migration yang sudah applied jangan diedit, tambah versi baru
- V1 sedang disederhanakan. database local yang sudah memakai V1 lama perlu database baru untuk mencoba versi ini
- build bisa tanpa database, startup app perlu database

```sh
DB_URL=jdbc:postgresql://localhost:55432/split_bill \
DB_PASSWORD=split_bill_local \
./mvnw spring-boot:run
```

```
-- UUIDs and the owner session ID are supplied by the application.
-- Only IDR is supported in this MVP. Service charge and settlement are derived.
CREATE TABLE trips (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    owner_session_id VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT trips_name_not_blank CHECK (length(btrim(name)) > 0),
    CONSTRAINT trips_currency_idr CHECK (currency = 'IDR')
);

CREATE INDEX trips_owner_session_idx ON trips (owner_session_id);

CREATE TABLE participants (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips (id),
    name VARCHAR(100) NOT NULL,
    -- Stable ordering determines who receives rounding remainders.
    position INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT participants_name_not_blank CHECK (length(btrim(name)) > 0),
    CONSTRAINT participants_position_nonnegative CHECK (position >= 0),
    CONSTRAINT participants_trip_position_unique UNIQUE (trip_id, position),
    CONSTRAINT participants_trip_id_unique UNIQUE (trip_id, id)
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips (id),
    payer_id UUID NOT NULL,
    amount NUMERIC NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT expenses_amount_positive CHECK (amount > 0 AND amount <> 'NaN'::numeric),
    CONSTRAINT expenses_payer_in_trip FOREIGN KEY (trip_id, payer_id)
        REFERENCES participants (trip_id, id),
    CONSTRAINT expenses_trip_id_unique UNIQUE (trip_id, id)
);

CREATE INDEX expenses_trip_payer_idx ON expenses (trip_id, payer_id);

CREATE TABLE expense_shares (
    trip_id UUID NOT NULL,
    expense_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    -- Persist the rounded equal share so later summaries reproduce the allocation.
    amount NUMERIC NOT NULL,
    PRIMARY KEY (expense_id, participant_id),
    CONSTRAINT expense_shares_amount_nonnegative
        CHECK (amount >= 0 AND amount <> 'NaN'::numeric),
    CONSTRAINT expense_shares_expense_in_trip FOREIGN KEY (trip_id, expense_id)
        REFERENCES expenses (trip_id, id),
    CONSTRAINT expense_shares_participant_in_trip FOREIGN KEY (trip_id, participant_id)
        REFERENCES participants (trip_id, id)
);

CREATE INDEX expense_shares_trip_participant_idx ON expense_shares (trip_id, participant_id);

```
## Model / repository

- pakai JPA. Flyway buat tabel, Hibernate hanya validate schema
- model: BillGroup, Participant, Expense, ExpenseShare
- ExpenseShareId buat primary key gabungan expense + participant
- repository pakai JpaRepository, belum ada service / validasi bisnis
- integration test pakai database khusus, data test di-rollback

```sh
RUN_DATABASE_TESTS=true \
DB_URL=jdbc:postgresql://localhost:55432/split_bill_jpa_check \
DB_PASSWORD=split_bill_local \
./mvnw test
```

## Rename grup

- nama umum: BillGroup / `bill_groups`, bisa untuk trip atau pengeluaran bersama lain
- V2 rename `trips` ke `bill_groups`, `trip_id` ke `bill_group_id`
- V1 tetap supaya history migration tidak berubah
- model / repository pakai BillGroup dan BillGroupRepository

## TODO sebelum project selesai

- ganti penyimpanan `owner_session_id` mentah dengan hash token session
- ingatkan kembali saat final review / sebelum submission
