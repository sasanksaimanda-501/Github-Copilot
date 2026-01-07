#Project 1 Basic Counter (Java, Web)

This is a minimal Java HTTP server + static web UI counter. It serves a simple page on http://localhost:8080 where you can increment, decrement and reset the counter.

Files added:
- src/main/java/com/githubcopilot/counter/CounterServer.java
- static/index.html

Requirements:
- JDK 11+ (com.sun.net.httpserver is available in standard JDKs; tested with JDK 11+).

Build & run (no Maven/Gradle required):
1. From the repository root:
   ```bash
   mkdir -p out
   javac -d out src/main/java/com/githubcopilot/counter/CounterServer.java
   cp -r static out/static
   java -cp out com.githubcopilot.counter.CounterServer
   ```
2. Open your browser at: http://localhost:8080

What the server exposes:
- GET  /api/count    -> returns {"count":<n>}
- POST /api/inc      -> increments and returns {"count":<n>}
- POST /api/dec      -> decrements and returns {"count":<n>}
- POST /api/reset    -> sets to 0 and returns {"count":0}

Development notes:
- The static UI is in `static/index.html`.
- If you'd like a Maven or Gradle project, or for me to commit & push the files and open a PR, tell me the branch name to use.
<img width="1481" height="816" alt="image" src="https://github.com/user-attachments/assets/31527655-5c72-49f8-8025-1348aeaa54a5" />

https://github.com/sasanksaimanda-501/Github-Copilot/tree/main




