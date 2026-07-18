package com.wealthwise.app.domain.model

/**
 * High-level nature of a parsed financial event.
 */
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
    INVESTMENT_BUY,
    INVESTMENT_SELL,
    INVESTMENT_SIP,
    LOAN_DISBURSEMENT,
    LOAN_EMI,
    LOAN_CLOSURE,
    CREDIT_CARD_BILL,
    CREDIT_CARD_PAYMENT,
    INSURANCE_PREMIUM,
    INSURANCE_CLAIM,
    FD_CREATED,
    FD_MATURED,
    UNKNOWN
}

enum class Category(val displayName: String, val group: CategoryGroup) {
    // Income
    SALARY("Salary", CategoryGroup.INCOME),
    BONUS("Bonus", CategoryGroup.INCOME),
    CASH_DEPOSIT("Cash Deposit", CategoryGroup.INCOME),
    REFUND("Refund", CategoryGroup.INCOME),
    INTEREST("Interest", CategoryGroup.INCOME),
    DIVIDEND("Dividend", CategoryGroup.INCOME),
    RENTAL_INCOME("Rental Income", CategoryGroup.INCOME),
    TAX_REFUND("Tax Refund", CategoryGroup.INCOME),
    CASHBACK("Cashback", CategoryGroup.INCOME),
    INVESTMENT_MATURITY("Investment Maturity", CategoryGroup.INCOME),
    INSURANCE_CLAIM("Insurance Claim", CategoryGroup.INCOME),

    // Expenses
    FOOD("Food", CategoryGroup.EXPENSE),
    FUEL("Fuel", CategoryGroup.EXPENSE),
    SHOPPING("Shopping", CategoryGroup.EXPENSE),
    MEDICAL("Medical", CategoryGroup.EXPENSE),
    TRAVEL("Travel", CategoryGroup.EXPENSE),
    EDUCATION("Education", CategoryGroup.EXPENSE),
    UTILITIES("Utilities", CategoryGroup.EXPENSE),
    INTERNET("Internet", CategoryGroup.EXPENSE),
    MOBILE_RECHARGE("Mobile Recharge", CategoryGroup.EXPENSE),
    SUBSCRIPTIONS("Subscriptions", CategoryGroup.EXPENSE),
    ENTERTAINMENT("Entertainment", CategoryGroup.EXPENSE),
    RESTAURANTS("Restaurants", CategoryGroup.EXPENSE),
    ATM_WITHDRAWAL("ATM Withdrawal", CategoryGroup.EXPENSE),
    UPI_PAYMENT("UPI Payment", CategoryGroup.EXPENSE),
    NEFT("NEFT", CategoryGroup.EXPENSE),
    IMPS("IMPS", CategoryGroup.EXPENSE),
    RTGS("RTGS", CategoryGroup.EXPENSE),
    CREDIT_CARD_PAYMENT("Credit Card Payment", CategoryGroup.EXPENSE),
    INSURANCE_PREMIUM("Insurance Premium", CategoryGroup.EXPENSE),
    RENT("Rent", CategoryGroup.EXPENSE),
    TAXES("Taxes", CategoryGroup.EXPENSE),

    // Investment
    MUTUAL_FUND("Mutual Fund", CategoryGroup.INVESTMENT),
    STOCKS("Stocks", CategoryGroup.INVESTMENT),
    FIXED_DEPOSIT("Fixed Deposit", CategoryGroup.INVESTMENT),
    RECURRING_DEPOSIT("Recurring Deposit", CategoryGroup.INVESTMENT),
    EPF("EPF", CategoryGroup.INVESTMENT),
    PPF("PPF", CategoryGroup.INVESTMENT),
    NPS("NPS", CategoryGroup.INVESTMENT),
    SGB("Sovereign Gold Bond", CategoryGroup.INVESTMENT),
    DIGITAL_GOLD("Digital Gold", CategoryGroup.INVESTMENT),

    // Loans
    PERSONAL_LOAN("Personal Loan", CategoryGroup.LOAN),
    CAR_LOAN("Car Loan", CategoryGroup.LOAN),
    HOME_LOAN("Home Loan", CategoryGroup.LOAN),
    EDUCATION_LOAN("Education Loan", CategoryGroup.LOAN),
    GOLD_LOAN("Gold Loan", CategoryGroup.LOAN),
    CONSUMER_LOAN("Consumer Loan", CategoryGroup.LOAN),

    UNCATEGORIZED("Uncategorized", CategoryGroup.OTHER)
}

enum class CategoryGroup { INCOME, EXPENSE, INVESTMENT, LOAN, OTHER }

enum class PaymentMode { UPI, NEFT, IMPS, RTGS, CARD, CASH, CHEQUE, AUTO_DEBIT, NET_BANKING, WALLET, UNKNOWN }
