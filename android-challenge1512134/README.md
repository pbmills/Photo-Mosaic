Photo Mosaic
------------

The goal of this task is to implement the following flow in an android app.
The app must make effective use of parallelism and asynchrony via RxJava.

_This task should take around 3-5 hours, focus on getting the best results in this time._

1. A user selects a local image file.
2. The app must:
   * load that image;
   * find the average color for each contiguous 32x32 tile;
   * fetch a tile from the provided server (see below) for that color;
   * composite the results into a photomosaic of the original image;
3. The composited photomosaic must be displayed according to the following
   constraints:
   * tiles must be rendered a complete row at a time (a user should never
      see a row with some completed tiles and some incomplete)
   * the mosaic must be rendered from the top row to the bottom row.

The server directory contains a simple local mosaic tile server. See the
README file in that directory for more information.

## Marking Criteria

Your code must be clear and easy to understand:

 * Avoids unnecessary complexity / over-engineering
 * Brief comments where appropriate
 * Logically broken up into parts
 * Correct and idiomatic usage of RxJava

Your code must be performant:

 * Gives feedback to the user as soon as possible (perceived performance)
 * Intelligently coordinates dependent asynchronous tasks
 * UI remains responsive
 * Memory Leaks are avoided
 * Non-deterministic behavior is avoided

Your code must be testable (but writing tests isn't necessary), for example:

 * Use dependency injection (the design pattern, not a framework or library)
 * Separate presentation logic from business logic.
 
 ## Constraints

You must:

 * Make the UI work on different screen sizes;
 * Use an API level for which source code is available.
 * Not use third-party libraries that directly solve the task,
   this will not tell us anything about you,
   and will not produce adaptable code;

You may:

 * use proven libraries (e.g. Guava) if you are familiar with them
   (be prepared to refactor and extend your code in an interview);

Have fun!
