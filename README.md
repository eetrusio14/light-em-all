# HW8 — LightEmAll (Java)

A grid-based puzzle game where you rotate tiles to connect all wires to a power station and light the entire board. The board can be generated in two ways:
- **Manual layout**: a fixed “cross + horizontal” wiring layout (power station starts in the center).
- **Fractal layout**: wiring generated recursively (power station starts at the middle of the top row).

## Project Structure

- `src/LightEmAll.java`  
  Game world logic (board generation, scrambling, power propagation, input handlers, rendering, win check).
- `src/GamePiece.java`  
  Tile model (wire directions), rotation, and drawing (`tileImage`).
- `ExamplesLightEmAll` (inside `LightEmAll.java`)  
  Tester-based unit tests and a visual `testGame` launcher.

## Requirements

This project uses the “Fundies” libraries:
- `javalib.impworld.*`
- `javalib.worldimages.*`
- `tester.*`

You must have the corresponding JARs available (`javalib.jar` and `tester.jar`) and on your classpath.

## How to Play

### Controls
- **Mouse click**: rotate the clicked tile 90° clockwise.
- **Arrow keys**: move the power station **only if** there is a valid wire connection between the current tile and the destination tile.
- When all tiles are powered, the game displays **YOU WIN!** and ignores further input.

### Visuals
- Powered wires: **yellow**
- Unpowered wires: **gray**
- Power station: **cyan star**

## Running (IntelliJ IDEA)

1. Open the project folder (the `HW8/` directory) in IntelliJ.
2. Add the `javalib` and `tester` JARs to the project:
   - **File → Project Structure -> Modules -> Dependencies -> + -> JARs or directories**
3. Run the visual test:
   - Open `src/LightEmAll.java`
   - Find `class ExamplesLightEmAll`
   - Run `testGame(Tester t)` using testing setup (Tester).

To switch which game you launch, edit `testGame`:
- Manual board:
  ```java
  LightEmAll game = new LightEmAll(int, int);
- Fractal board:
  ```java
  LightEmAll game = new LightEmAll(int, int, boolean);

## Notes

- Game boards must be square, therefore, the int value on game board creation must be the same values.
- The power propagating function only works in the confines of a set radius and is recomputed each each rotation / move.
