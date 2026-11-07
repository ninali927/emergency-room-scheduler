# Emergency Room Scheduler

A Java implementation of an emergency-room patient system built on two hand-written data structures,
plus a browser-based visualizer that drives them live over WebSockets.

- **Patient registry: augmented AVL tree.** Arrival times are the keys, so lookups, inserts and deletes
  take O(log n). Each node also stores its subtree size, which supports time-range search, rank queries
  ("how many patients arrived before time t?") and k-th-by-arrival selection.
- **Treatment queue: binary max-heap.** Patients are ordered by severity (1–5). It supports adaptive
  priority updates (a patient's severity can change while they wait), removing any patient (for example,
  one who leaves before treatment), and k-th-most-severe queries.
- **`EmergencyRoom`** keeps both structures in sync, so one patient can be found by arrival time or by
  urgency.

The visualizer has two tabs:
- **ER Scheduler:** register, treat and re-prioritize patients, and run range, rank and k-th queries.
- **AVL Tree Visualization:** step through snapshots of the tree as patients are inserted and removed.

## Project layout

```
.
├── core/                         # Data structures and tests (no external dependencies)
│   └── src/
│       ├── main/java/io/github/ninali927/er/
│       │   ├── registry/         # PatientRegistry interface + AVLPatientRegistry
│       │   ├── queue/            # TreatmentQueue interface + HeapTreatmentQueue
│       │   ├── system/           # EmergencyRoom (combines registry + queue)
│       │   ├── models/           # Patient
│       │   ├── visualization/    # SnapshotAVLTree (records tree history)
│       │   ├── util/             # Tree / heap pretty-printers
│       │   └── ERSchedulerDemo.java
│       └── test/java/io/github/ninali927/er/   # JUnit 5 tests
├── app/
│   ├── backend/                  # Gradle project: Javalin server (port 7001), compiles core/ directly
│   └── frontend/                 # Static HTML/CSS/JS served by the backend
└── start-visualization.sh        # Starts the visualizer
```

## Running

Requires **JDK 25**. The Gradle wrapper downloads Gradle itself.

Run the visualizer, then open http://localhost:7001 (stop it with Ctrl+C):

```bash
./start-visualization.sh
```

Run the tests (writes a JaCoCo coverage report to `app/backend/build/reports/jacoco/`):

```bash
cd app/backend
./gradlew test
```

Run the console demo:

```bash
cd app/backend
./gradlew classes
java -cp build/classes/java/main io.github.ninali927.er.ERSchedulerDemo
```

## Tech stack

- Java 25, Gradle 9.1 (wrapper)
- Javalin 6 (HTTP + WebSockets), Gson
- JUnit 5, JaCoCo
- Alpine.js frontend (no build step)
