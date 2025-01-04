**Demo Prototype for Simple Banking Application**

## About API:

The Banking API provides a comprehensive set of endpoints for managing accounts, transactions, and financial records. This API is designed to handle operations such as account creation, balance inquiries, fund transfers, deposits, withdrawals, and transaction history retrieval.

**Version:** 1.0.0

## How To Build and Run:

Currently, there is no specific deployment feature configured for this project. A simple **jar** build and running in a local environment is possible.

1. Clone the project from the repository:

**git clone https://github.com/sujathak625/payment-app-prototype-demo.git**

2. Build the jar using Maven:

**mvn clean package**

- **Maven Version:** 3.9.9
- **Java Version:** 21
- **Spring:** 3.4.1

3. Once the jar is built, you can run it using the following command:

**java -jar markant-demo-1.0.0.jar**


**Note:** The project uses an embedded Tomcat server, so no external Tomcat installation is required.

## Tools and Libraries:

### Build Configuration:
- **Java Version:** 21
- **Source Directory:**
- Main: `${project.basedir}/src/main/kotlin`
- Test: `${project.basedir}/src/test/kotlin`
- **Dependencies:**
- `spring-boot-starter-parent`
- `spring-boot-starter-actuator` (Monitoring and management features)
- `spring-boot-starter-data-jpa` (Spring Data JPA for ORM)
- `spring-boot-starter-validation` (Validation support)
- `spring-boot-starter-web` (Web and RESTful API support)
- `spring-boot-maven-plugin` (Build and package Spring Boot applications)
- `spring-boot-starter-test` (Testing tools and libraries for Spring Boot)

### External Libraries:
1. **Lombok**: For getters, setters, and constructors.
2. **H2 Database**: In-memory database for development and testing.
3. **IBAN4j**: For generation and validation of IBANs.
4. **SLF4J**: For logging.
5. **JetBrains Annotations**: For code annotations.

### External API:
- **FreeCurrencyAPI**: To retrieve exchange rates and convert currencies to Euros.

## Test Data:
This data is pre-loaded during the start of the service for testing purposes.

| **Field**              | **Value**                               |
|------------------------|-----------------------------------------|
| **ACCOUNT_HOLDER_NAME** | John Doe                               |
| **BIC**                 | DEUTDEFF                                |
| **CREATED_AT**          | 2025-01-02 11:38:12.495774             |
| **CURRENCY**            | EUR                                     |
| **CURRENT_BALANCE**    | 1500.00                                 |
| **IBAN**                | DE89370400440532013000                  |
| **STATUS**              | ACTIVE                                  |
| **TAX_ID**              | TAX123456                               |
| **UPDATED_AT**          | 2025-01-02 11:38:12.495781             |

## Enums:
The following enums are pre-configured to maintain consistency across transactions. This ensures that any consumer of this API will have to comply with the transaction status, transaction source, transaction type, and currency code, avoiding inconsistencies.

- **AccountStatus:** An account can only be in one of these statuses - `ACTIVE`, `INACTIVE`,`SUSPENDED`,`ACTIVE_KYC_NOT_COMPLETED` and `CLOSED`.
- **CurrencyEnum:** As of now, only `EUR` is supported. If the transaction currency is other than EUR, the latest exchange rate will be retrieved and the amount will be converted to EUR.
- **TransactionType:** `DEPOSIT`, `WITHDRAWAL`, `CREDIT_TRANSFER`, `DEBIT_TRANSFER`.
- **TransactionSource:** `BANK_COUNTER`, `ATM`, `FUND_TRANSFER`.
- **TransactionStatus:** `SUCCESS`, `FAILED`.

## Endpoints:

### Account Management Endpoints:

1. **Get Account Balance:** Retrieves the balance of a specified account.
- **GET** `http://localhost:8080/api/v1/accounts/balance/{accountNumber}`
- Example: `http://localhost:8080/api/v1/accounts/balance/DE89370400440532013000`

### Transaction Endpoints:

2. **Deposit Funds:** Deposits funds into an account.
- **POST** `http://localhost:8080/api/v1/transactions/deposit`
- Sample Request:
  ```json
  {
    "iban": "DE89370400440532013000",
    "amount": "1200",
    "currency": "EUR",
    "transactionType": "DEPOSIT",
    "transactionRemarks": "Deposit to bank account",
    "transactionSource": "BANK_COUNTER"
  }
  ```

3. **Withdraw Funds:** Withdraws funds from an account.
- **POST** `http://localhost:8080/api/v1/transactions/withdraw`
- Sample Request:
  ```json
  {
    "iban": "DE89370400440532013000",
    "amount": "2320",
    "currency": "EUR",
    "transactionType": "WITHDRAWAL",
    "transactionRemarks": "Withdrawal from bank account",
    "transactionSource": "ATM"
  }
  ```

4. **Transfer Funds:** Transfers funds between accounts.
- **POST** `http://localhost:8080/api/v1/transactions/transfer`
- Sample Request:
  ```json
  {
    "transactingAccountNumber": "GB29NWBK60161331926819",
    "transactingAccountBIC": "BUKBGB22",
    "customerAccountNumber": "DE89370400440532013000",
    "amount": "1000",
    "currencyType": "EUR",
    "transactionType": "CREDIT_TRANSFER"
  }
  ```
    ```json
  {
    "transactingAccountNumber": "GB29NWBK60161331926819",
    "transactingAccountBIC": "BUKBGB22",
    "customerAccountNumber": "DE89370400440532013000",
    "amount": "1000",
    "currencyType": "EUR",
    "transactionType": "DEBIT_TRANSFER"
  }
  ```

5. **Get Last N Transactions:** Retrieves the last N transactions for a specified IBAN.
- **GET** `http://localhost:8080/api/v1/transactions/history/{iban}/{n}`
- Example: `http://localhost:8080/api/v1/transactions/history/DE89370400440532013000/10`

6. **Get Transaction History (Date Range):** Retrieves the transaction history for a specified IBAN within a date range.
- **GET** `http://localhost:8080/api/v1/transactions/history/{iban}/{fromDate}/{toDate}`
- Example: `http://localhost:8080/api/v1/transactions/history/DE89370400440532013000/01-02-2024/30-01-2024`
