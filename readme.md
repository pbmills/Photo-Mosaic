RxJava + Dagger2, very complex RxJava in image processing

Screen Record Link: https://goo.gl/8RBjMM 

Requirement:

1. A user selects a local image file.
2. The app must:
   * load that image;
   * divide the image into tiles;
   * find a representative color for each tile (average);
   * fetch a tile from the provided server (see below) for that color;
   * composite the results into a photomosaic of the original image;
3. The composited photomosaic should be displayed according to the following
   constraints:
   * tiles should be rendered a complete row at a time (a user should never
      see a row with some completed tiles and some incomplete)
   * the mosaic should be rendered from the top row to the bottom row.
4. The client app should make effective use of parallelism and asynchrony.

The nodejs_server.zip/android-challenge160621/servers directory contains a simple local mosaic tile server. See the
README file in that directory for more information.

IDE:
1. Android Studio : 2.2.2+
2. JDK 1.8
3. Android SDK Level 25
4. Android build tool 25.0.1

Building:
If build fails please rebuild again. This is due to dagger2 / jack compatibility issue

Unit Tests:
1. Please start nodejs server in your local machine before running unit tests
2. First time running unit tests may take some time to download robolectric dependency

App:
1. You can pinch to zoom image, pan to move it
2. Change server via toolbar button
3. To run app on real phone, phone and dev machine must be in the same subnet
4. Add nodejs server to your development machine's exception list, then change server in app
