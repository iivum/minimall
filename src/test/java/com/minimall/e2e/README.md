# E2E Tests for Minimall Customer Service

## Overview

This package contains end-to-end tests for the Minimall customer service feature, covering:
- Customer message reception
- Auto-reply scenarios  
- Status transitions
- Human transfer workflows

## Running Tests

### Prerequisites
- Docker and Docker Compose installed
- Application running on `http://localhost:8080` (or set `base.url` system property)

### Run All E2E Tests
```bash
mvn test -Dtest=com.minimall.e2e.* -Dbase.url=http://localhost:8080
```

### Run Individual Test Classes
```bash
mvn test -Dtest=CustomerServiceMessageE2ETest -Dbase.url=http://localhost:8080
mvn test -Dtest=AutoReplyScenarioE2ETest -Dbase.url=http://localhost:8080
mvn test -Dtest=StatusTransitionE2ETest -Dbase.url=http://localhost:8080
mvn test -Dtest=HumanTransferE2ETest -Dbase.url=http://localhost:8080
```

### Docker Environment
```bash
./docker-test.sh
```

## Test Scenarios

| Test Class | Scenario | Description |
|------------|----------|-------------|
| `CustomerServiceMessageE2ETest` | 客服消息接收 | Verifies messages are received and stored correctly |
| `AutoReplyScenarioE2ETest` | 自动回复场景 | Tests auto-reply for first-time visitors |
| `StatusTransitionE2ETest` | 状态流转 | Tests PENDING → PROCESSING → COMPLETED flow |
| `HumanTransferE2ETest` | 人工转接 | Tests transfer to human handler functionality |
