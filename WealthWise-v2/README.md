# WealthWise — Offline SMS-based Personal Finance Manager

**Read this first:** the original brief asked for a fully production-ready app covering ~200 distinct
features (every Indian bank/broker/insurer/mutual-fund sender, every loan type, full analytics
suite, PDF/CSV/Excel export, biometric lock, tests, etc). That's genuinely a multi-quarter effort
for a small team, not something that can be responsibly generated in one pass. What's in this repo
is a **real, compiling architecture with the hardest parts actually implemented** — the SMS parsing
engine, local classifier, encrypted database, and a fully working, animated dashboard — built so
every remaining feature slots into the same pattern rather than requiring a rewrite. See
[What's implemented vs. what's scaffolded](#whats-implemented-vs-whats-scaffolded) below.

## Architecture

Clean Architecture + MVVM, single `:app` module (split into feature modules once the codebase
grows past this scaffold's size):

```
data/
  local/          Room entities, DAOs, SQLCipher-backed database, TypeConverters
  parser/         SmsFieldExtractor (regex), TransactionClassifier (keyword scoring),
                  KnownFinancialSenders, SmsToTransactionParser (orchestrator)
  repository/     SmsRepository — bridges the SMS content provider to Room
domain/
  model/          TransactionType, Category, PaymentMode enums (the shared vocabulary)
di/               Hilt modules
presentation/
  dashboard/      DashboardViewModel + DashboardScreen (Compose)
  common/theme/   "Ledger Ink" design system (colors, type, shapes)
  common/components/  InkStrokeLineChart, MetricCard — the signature animated pieces
  common/navigation/  NavGraph with custom shared-axis transitions
worker/           WorkManager workers for the initial full scan + incoming-SMS processing
security/         Android Keystore–backed SQLCipher passphrase provider
```

## Why local-only actually holds up

- **No `INTERNET` permission is requested anywhere in the manifest.** This is the real guarantee,
  not a policy statement — the app cannot make network calls even if a future dependency tried to.
- `network_security_config.xml` blocks cleartext traffic as a defense-in-depth backstop.
- The database is SQLCipher-encrypted; the passphrase is a random 256-bit key generated on first
  run, stored only via `EncryptedSharedPreferences` (itself backed by a hardware Android Keystore
  key), and never leaves the device.
- Classification is 100% on-device keyword/regex scoring (`TransactionClassifier`) — see the
  file's doc comment for why this beats a heavyweight on-device model for this problem shape, and
  how a TF-Lite signal could be added later without changing the public contract.

## The "extraordinary transitions" you asked about

- **`InkStrokeLineChart`**: the net-worth/cash-flow trend line draws itself in like a pen tracing
  a ledger line, using `PathMeasure` to progressively trim the path over ~1.1s — not a generic
  fade-in chart.
- **Shared-axis navigation** (`WealthWiseNavGraph`): screens slide+fade along a short horizontal
  axis (24% of screen width) on push/pop, modeled on Material's motion system rather than the
  default abrupt Compose Navigation cross-fade.
- **Staggered dashboard entrance**: each card/row fades and rises in with an increasing delay
  (`StaggeredEntry`), so the dashboard feels like it's settling into place rather than popping in.
- **Spring-physics press feedback** on `MetricCard`: a slight bouncy scale-down on press/release,
  not a linear tween.
- Full design rationale — palette, type pairing, the "Ledger Ink" concept — is documented in
  `presentation/common/theme/Theme.kt` and `Type.kt`.

## What's implemented vs. what's scaffolded

**Fully implemented and testable today:**
- SMS field extraction (amount, account/card last-4, UPI ID, reference number, balance, payment
  mode) — see `SmsFieldExtractorTest`
- Local keyword classifier covering income/expense/investment/loan categories, with OTP/promo
  filtering — see `TransactionClassifierTest`
- Full SMS → Room pipeline (dedupe by hash, batch processing for 50k+ messages, incoming-SMS
  broadcast receiver) — see `TransactionDaoTest`
- Encrypted Room/SQLCipher database wired through Hilt
- Dashboard: monthly income/expense/savings, recent transactions, animated trend chart — reactive
  via `Flow`, so it updates live as new SMS are processed
- Sample anonymized SMS dataset (`app/src/test/resources/sample_sms_dataset.json`) spanning salary,
  UPI spend, EMI, credit card bill, SIP, stock order, FD, insurance premium, gold loan, redemption,
  plus an OTP and a promo message to verify filtering

**Scaffolded (entities/patterns exist, screens/logic don't yet):**
- Loans, credit cards, investments, and insurance have Room entities (`AccountEntities.kt`) but no
  dedicated repositories/ViewModels/screens yet — follow the `SmsRepository` +
  `DashboardViewModel` pattern to add them
- Analytics screen, Search screen, Settings/Backup screen, PIN/biometric lock UI — routes are
  reserved in `Destination.kt`, screens aren't built
- CSV/Excel/PDF export, encrypted backup file format
- Duplicate-transaction detection query exists in the DAO (`findPossibleDuplicates`) but isn't
  wired into a background check yet
- Additional bank/UPI/broker sender IDs beyond the curated list in `KnownFinancialSenders` —
  add fragments as you encounter real-world sender formats, since these vary by telecom circle

## Getting started

1. Open the project root in Android Studio (Koala/2024.1+).
2. Let Gradle sync — it will pull SQLCipher, Room, Hilt, Compose BOM, etc. via standard Maven
   Central/Google repos (dependency resolution needs internet; the **app itself** never does).
3. Run on a device or emulator with an SMS inbox (emulators can have test SMS injected via `adb
   emu sms send <number> <text>`, or use the sample dataset to unit-test the parser directly).
4. Grant SMS permission when prompted — this triggers `SmsScanWorker` for the first full scan.

## Known gaps to close before calling this production-ready

- Merchant extraction (`SmsFieldExtractor.extractMerchant`) is a best-effort regex; expect misses
  on unusual phrasing — this is the single highest-value area to expand with real SMS samples.
- No database migration strategy yet (`fallbackToDestructiveMigration()` is a placeholder).
- No PIN/biometric lock screen wired to app foreground yet, despite `androidx.biometric` being a
  dependency.
- Category-group aggregate queries (e.g. total investments) assume `type` and `category` stay in
  sync, which the classifier currently guarantees but isn't enforced at the DB layer.
