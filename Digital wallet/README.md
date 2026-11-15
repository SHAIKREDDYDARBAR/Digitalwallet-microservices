# Digital Wallet & Money Transfer System (Microservices)

A Java 17 / Spring Boot microservices project that lets users create wallets, view balances, transfer funds, and view transfer history.

## Services
- **Discovery Server** (8761): Eureka registry.
- **API Gateway** (8080): routes `/api/wallets/**` and `/api/transfers/**` to services.
- **Wallet Service** (8081): wallet creation, balance lookup, internal debit/credit; MySQL database `wallet_db`.
- **Transfer Service** (8082): transfer orchestration and history; MySQL database `transfer_db`; calls Wallet Service through OpenFeign.

## Requirements
Java 17, Maven 3.8+, MySQL 8+, Postman.

## Configure MySQL password (PowerShell)
Set your local password in an environment variable before starting the services:
```powershell
$env:DB_PASSWORD="your-mysql-password"
```
Both services use MySQL root and create their databases automatically. Do not commit passwords to GitHub.

## Run order
Open a terminal in the project root for each command (use separate terminals):
```bash
mvn -pl discovery-server spring-boot:run
mvn -pl wallet-service spring-boot:run
mvn -pl transfer-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```
Open Eureka at `http://localhost:8761`. Wait for both services to register before testing through the gateway.

## API examples (through Gateway)
Create wallet:
```http
POST http://localhost:8080/api/wallets
Content-Type: application/json
{"userId":"user101","openingBalance":5000.00}
```
Create a second wallet:
```json
{"userId":"user202","openingBalance":1000.00}
```
Get balance:
```http
GET http://localhost:8080/api/wallets/user101
```
Transfer money:
```http
POST http://localhost:8080/api/transfers
Content-Type: application/json
{"senderUserId":"user101","receiverUserId":"user202","amount":750.00}
```
Transfer history:
```http
GET http://localhost:8080/api/transfers/history/user101
```
Get transfer by reference:
```http
GET http://localhost:8080/api/transfers/{referenceId}
```

## Important design notes
- Money uses `BigDecimal`, not floating-point types.
- Wallet debit/credit operations are transactional within the Wallet Service.
- Transfer Service records a transfer reference and uses a compensating credit if the receiver credit fails. This is a learning/demo-level saga-style compensation, not a production-grade payment system.
- For production, add authentication/authorization, idempotency keys, immutable double-entry ledger entries, outbox/event messaging, stronger concurrency controls, retries/timeouts, audit logging, and reconciliation. Do not expose internal debit/credit endpoints publicly.

## Suggested GitHub description
Microservices-based digital wallet and money transfer application built with Java, Spring Boot, Spring Cloud Gateway, Eureka, OpenFeign, Spring Data JPA and MySQL.
