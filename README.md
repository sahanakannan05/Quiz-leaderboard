# Quiz Leaderboard System

A Java solution for the Bajaj Finserv Health Internship Assignment — SRM Quiz Leaderboard.

## Problem Summary

Poll a quiz API 10 times, deduplicate event data using `roundId + participant` as a composite key, aggregate scores per participant, and submit a final sorted leaderboard.

## How It Works

1. **Poll** — Calls `GET /quiz/messages?regNo=<REG>&poll=<0-9>` ten times with a mandatory 5-second delay between each call.
2. **Deduplicate** — Tracks every `roundId::participant` pair in a `HashSet`. Any event already seen is skipped to avoid double-counting.
3. **Aggregate** — Accumulates scores per participant in a `Map<String, Integer>`.
4. **Sort** — Builds a leaderboard sorted by `totalScore` in descending order.
5. **Submit** — Posts the final leaderboard once to `POST /quiz/submit`.

## Project Structure

```
quiz-leaderboard/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── quiz/
                    └── QuizLeaderboard.java
```

## Prerequisites

- Java 11+
- Maven 3.6+

## Setup & Run

### 1. Clone the repo

```bash
git clone https://github.com/your-username/quiz-leaderboard.git
cd quiz-leaderboard
```

### 2. Set your registration number

Open `src/main/java/com/quiz/QuizLeaderboard.java` and replace:

```java
private static final String REG_NO = "YOUR_REG_NO_HERE";
```

with your actual registration number, e.g. `"2024CS101"`.

### 3. Build

```bash
mvn clean package
```

### 4. Run

```bash
java -jar target/quiz-leaderboard.jar
```

The program will print each poll response, flag duplicates, and finally print the leaderboard and the server's submission response.

## Key Design Decisions

| Challenge | Solution |
|---|---|
| Duplicate API responses | `HashSet<String>` keyed on `roundId::participant` |
| Score aggregation | `Map.merge(key, score, Integer::sum)` |
| Leaderboard ordering | `List.sort()` with descending comparator |
| Single submission | Submit called exactly once after all polls complete |

## Dependencies

- [org.json](https://github.com/stleary/JSON-java) — lightweight JSON parsing
