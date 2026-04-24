# Quiz Leaderboard System

A Java solution for the Bajaj Finserv Health Internship Assignment — SRM Quiz Leaderboard.

## Problem Summary

Polls a quiz API 10 times, removes duplicate entries, aggregates each participant's score, and submits a sorted leaderboard.

## Prerequisites

- Java 11+
- Maven 3.6+

## Setup & Run

### 1. Clone the repo

1. Clone the repo
2. Open `QuizLeaderboard.java` and set your registration number on line 14
replace:

private static final String REG_NO = "YOUR_REG_NO_HERE";

with your actual registration number, e.g. `"RA2311053010140"`.
3. Build:

```bash
mvn clean package
```

4. Run

```bash
java -jar target/quiz-leaderboard.jar
```

The program will print each poll response, flag duplicates, and finally print the leaderboard and the server's submission response.

## Logic used
- Called the API 10 times with poll values 0 to 9
- Used a HashSet to track roundId + participant combinations already seen
- If the same combination appeared again in a later poll, it was skipped
- Scores were added up per participant using a HashMap
- Final leaderboard sorted by total score in descending order
- Submitted once after all polls completed

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
## How It Works

1. Poll — Calls with ten times with a mandatory 5-second delay between each call.
2. Deduplicate — Tracks every roundId::participant pair in a HashSet. Any event already seen is skipped to avoid double counting.
3. Aggregate — Accumulates scores per participant 
4. Sort — Builds a leaderboard sorted by totalScore in descending order.
5. Submit — Posts the final leaderboard once to POST /quiz/submit.

## Key Design Decisions

| Duplicate API responses - HashSet<String> keyed on roundId::participant |
| Score aggregation |
| Leaderboard ordering - List.sort() with descending comparator |
| Single submission - Submit called exactly once after all polls complete |

## Dependencies

- [org.json](https://github.com/stleary/JSON-java) — lightweight JSON parsing
