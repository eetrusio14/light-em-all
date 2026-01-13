import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents a game of LightEmAll, where the player rotates tiles to connect
// all pieces to a power station and light up the entire board.
// The board can either be manually generated or generated recursively (fractal).
class LightEmAll extends World {

  ArrayList<ArrayList<GamePiece>> board; // 2D board
  ArrayList<GamePiece> nodes;// flat list of all tiles (used for win checks)
  ArrayList<GamePiece> poweredPieces; // tiles currently receiving power

  int width; // number of columns
  int height; // number of rows
  int powerRow; // current row of power station
  int powerCol; // current column of power station
  int radius; // max number of steps power can travel

  // Constructs a manual board with the given dimensions
  LightEmAll(int width, int height) {
    this.width = width;
    this.height = height;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.poweredPieces = new ArrayList<GamePiece>();

    // Build the fixed board layout
    this.initializeManualBoard();

    // Place power station at center of board
    this.powerCol = width / 2;
    this.powerRow = height / 2;
    this.board.get(this.powerCol).get(this.powerRow).powerStation = true;

    // Large radius so power can reach everything
    this.radius = width + height;

    // Randomize tile rotations to create the puzzle
    this.scrambleBoard();

    this.updatePower();
  }

  // Constructs a fractal board with the given dimensions
  LightEmAll(int width, int height, boolean fractal) {
    this.width = width;
    this.height = height;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.poweredPieces = new ArrayList<GamePiece>();

    if (fractal) {
      // Creates an empty board, then add wires recursively
      this.initializeFractalBoard();

      // Power station starts at the middle of the top row
      this.powerCol = width / 2;
      this.powerRow = 0;
      this.board.get(this.powerCol).get(this.powerRow).powerStation = true;

      // Radius depends on how long the wiring actually is
      this.radius = this.calculateRadius();
    }
    else {
      this.initializeManualBoard();

      this.powerCol = width / 2;
      this.powerRow = height / 2;
      this.board.get(this.powerCol).get(this.powerRow).powerStation = true;

      this.radius = width + height;
    }

    this.scrambleBoard();
    this.updatePower();
  }

  // Purpose: Builds the simple, non-fractal board
  void initializeManualBoard() {
    for (int col = 0; col < this.width; col++) {
      ArrayList<GamePiece> column = new ArrayList<GamePiece>();
      for (int row = 0; row < this.height; row++) {

        // Horizontal connections everywhere
        boolean left = col > 0;
        boolean right = col < this.width - 1;

        // Vertical connections only down the middle column
        boolean top = (col == this.width / 2) && row > 0;
        boolean bottom = (col == this.width / 2) && row < this.height - 1;

        GamePiece piece = new GamePiece(row, col, left, right, top, bottom);
        column.add(piece);
        this.nodes.add(piece);
      }
      this.board.add(column);
    }
  }

  // Purpose: Initializes an empty board, then fills it with fractal wiring
  void initializeFractalBoard() {
    for (int col = 0; col < this.width; col++) {
      ArrayList<GamePiece> column = new ArrayList<GamePiece>();
      for (int row = 0; row < this.height; row++) {

        // Start with no connections; recursion adds them later
        GamePiece piece = new GamePiece(row, col, false, false, false, false);
        column.add(piece);
        this.nodes.add(piece);
      }
      this.board.add(column);
    }

    this.generateFractalWiring(0, 0, this.width, this.height);
  }

  // Purpose: Recursively subdivides the board and connects sub-regions together
  void generateFractalWiring(int startCol, int startRow, int regionWidth, int regionHeight) {
    // Nothing to do for invalid regions
    if (regionWidth <= 0 || regionHeight <= 0) {
      return;
    }

    // Base cases: region is too small to split further
    if (regionWidth == 1 && regionHeight == 1) {
      return;
    }
    if (regionWidth == 1) {
      this.createBasePattern(startCol, startRow, 1, regionHeight);
      return;
    }
    if (regionHeight == 1) {
      this.createBasePattern(startCol, startRow, regionWidth, 1);
      return;
    }
    if (regionWidth == 2 && regionHeight == 2) {
      this.createBasePattern(startCol, startRow, 2, 2);
      return;
    }

    // Split the region roughly in half
    int leftWidth = regionWidth / 2;
    int topHeight = regionHeight / 2;
    if (leftWidth == 0) leftWidth = 1;
    if (topHeight == 0) topHeight = 1;

    int rightWidth = regionWidth - leftWidth;
    int bottomHeight = regionHeight - topHeight;

    int midCol = startCol + leftWidth;
    int midRow = startRow + topHeight;

    // Recurse into the four quadrants
    this.generateFractalWiring(startCol, startRow, leftWidth, topHeight);
    this.generateFractalWiring(midCol, startRow, rightWidth, topHeight);
    this.generateFractalWiring(startCol, midRow, leftWidth, bottomHeight);
    this.generateFractalWiring(midCol, midRow, rightWidth, bottomHeight);

    // Connect the quadrants together along the outer edges

    // Left edge: connect top-left to bottom-left
    int leftEdgeCol = startCol;
    if (midRow - 1 >= 0 && midRow < this.height) {
      GamePiece a = this.board.get(leftEdgeCol).get(midRow - 1);
      GamePiece b = this.board.get(leftEdgeCol).get(midRow);
      a.bottom = true;
      b.top = true;
    }

    // Right edge: connect top-right to bottom-right
    int rightEdgeCol = startCol + regionWidth - 1;
    if (rightEdgeCol >= 0 && rightEdgeCol < this.width
      && midRow - 1 >= 0 && midRow < this.height) {
      GamePiece a = this.board.get(rightEdgeCol).get(midRow - 1);
      GamePiece b = this.board.get(rightEdgeCol).get(midRow);
      a.bottom = true;
      b.top = true;
    }

    // Bottom edge: connect bottom-left to bottom-right
    int bottomEdgeRow = startRow + regionHeight - 1;
    if (bottomEdgeRow >= 0 && bottomEdgeRow < this.height
      && midCol - 1 >= 0 && midCol < this.width) {
      GamePiece a = this.board.get(midCol - 1).get(bottomEdgeRow);
      GamePiece b = this.board.get(midCol).get(bottomEdgeRow);
      a.right = true;
      b.left = true;
    }
  }

  // Purpose: Creates the smallest wiring patterns used by the fractal algorithm
  void createBasePattern(int startCol, int startRow, int width, int height) {

    // 2x2 case: make a squared U-shape
    if (width == 2 && height == 2) {
      GamePiece topLeft = this.board.get(startCol).get(startRow);
      GamePiece topRight = this.board.get(startCol + 1).get(startRow);
      GamePiece bottomLeft = this.board.get(startCol).get(startRow + 1);
      GamePiece bottomRight = this.board.get(startCol + 1).get(startRow + 1);

      // Left side vertical
      topLeft.bottom = true;
      bottomLeft.top = true;

      // Bottom horizontal
      bottomLeft.right = true;
      bottomRight.left = true;

      // Right side vertical
      topRight.bottom = true;
      bottomRight.top = true;
    }

    // Single column: connect straight down
    else if (width == 1 && height >= 2) {
      for (int row = startRow; row < startRow + height - 1; row++) {
        this.board.get(startCol).get(row).bottom = true;
        this.board.get(startCol).get(row + 1).top = true;
      }
    }

    // Single row: connect straight across
    else if (height == 1 && width >= 2) {
      for (int col = startCol; col < startCol + width - 1; col++) {
        this.board.get(col).get(startRow).right = true;
        this.board.get(col + 1).get(startRow).left = true;
      }
    }

    // 1x1 does nothing
  }

  // Purpose: Computes a radius based on the longest path in the wiring
  int calculateRadius() {
    GamePiece start = this.board.get(this.powerCol).get(this.powerRow);
    GamePiece farthest = this.findFarthestNode(start);
    int diameter = this.findFarthestDistance(farthest);
    return (diameter / 2) + 1;
  }

  // Finds the tile that is the most steps away from start
  GamePiece findFarthestNode(GamePiece start) {
    ArrayList<GamePiece> queue = new ArrayList<GamePiece>();
    ArrayList<GamePiece> visited = new ArrayList<GamePiece>();

    queue.add(start);
    visited.add(start);

    GamePiece farthest = start;
    int index = 0;

    while (index < queue.size()) {
      GamePiece current = queue.get(index);

      // In BFS, the last thing reached is always one of the farthest
      farthest = current;
      index++;

      // Only follow neighbors when both sides have a matching wire
      if (current.left && current.col > 0) {
        GamePiece n = this.board.get(current.col - 1).get(current.row);
        if (n.right && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
        }
      }

      if (current.right && current.col < this.width - 1) {
        GamePiece n = this.board.get(current.col + 1).get(current.row);
        if (n.left && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
        }
      }

      if (current.top && current.row > 0) {
        GamePiece n = this.board.get(current.col).get(current.row - 1);
        if (n.bottom && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
        }
      }

      if (current.bottom && current.row < this.height - 1) {
        GamePiece n = this.board.get(current.col).get(current.row + 1);
        if (n.top && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
        }
      }
    }
    return farthest;
  }

  // Purpose: Finds how many steps away the farthest tile is
  int findFarthestDistance(GamePiece start) {
    ArrayList<GamePiece> queue = new ArrayList<GamePiece>();
    ArrayList<GamePiece> visited = new ArrayList<GamePiece>();
    ArrayList<Integer> depths = new ArrayList<Integer>();

    queue.add(start);
    visited.add(start);
    depths.add(0); // starting tile is distance 0

    int maxDepth = 0;
    int index = 0;

    while (index < queue.size()) {
      GamePiece current = queue.get(index);
      int depth = depths.get(index);
      maxDepth = Math.max(maxDepth, depth);
      index++;

      // Each neighbor added is exactly one step farther away
      if (current.left && current.col > 0) {
        GamePiece n = this.board.get(current.col - 1).get(current.row);
        if (n.right && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }

      if (current.right && current.col < this.width - 1) {
        GamePiece n = this.board.get(current.col + 1).get(current.row);
        if (n.left && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }

      if (current.top && current.row > 0) {
        GamePiece n = this.board.get(current.col).get(current.row - 1);
        if (n.bottom && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }

      if (current.bottom && current.row < this.height - 1) {
        GamePiece n = this.board.get(current.col).get(current.row + 1);
        if (n.top && !visited.contains(n)) {
          visited.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }
    }

    return maxDepth;
  }

  // Purpose: Randomly rotates each tile
  void scrambleBoard() {
    for (GamePiece piece : this.nodes) {
      int rotations = (int) (Math.random() * 4);
      for (int i = 0; i < rotations; i++) {
        piece.rotate();
      }
    }
  }

  // Purpose: Rotates a tile when clicked, unless the game is already won
  public void onMouseClicked(Posn pos) {
    if (this.allPowered()) {
      return;
    }
    int tileSize = 50;
    int col = pos.x / tileSize;
    int row = pos.y / tileSize;

    if (col >= 0 && col < this.width && row >= 0 && row < this.height) {
      this.board.get(col).get(row).rotate();
      this.updatePower();
    }
  }

  // Purpose: Moves the power station with arrow keys if wires allow it
  public void onKeyEvent(String key) {
    if (this.allPowered()) {
      return;
    }

    GamePiece currentPiece = this.board.get(this.powerCol).get(this.powerRow);
    int newCol = this.powerCol;
    int newRow = this.powerRow;

    if (key.equals("up")) {
      newRow--;
      if (newRow >= 0 && currentPiece.top
        && this.board.get(newCol).get(newRow).bottom) {
        this.movePowerStation(newCol, newRow);
        this.updatePower();
      }
    }
    else if (key.equals("down")) {
      newRow++;
      if (newRow < this.height && currentPiece.bottom
        && this.board.get(newCol).get(newRow).top) {
        this.movePowerStation(newCol, newRow);
        this.updatePower();
      }
    }
    else if (key.equals("left")) {
      newCol--;
      if (newCol >= 0 && currentPiece.left
        && this.board.get(newCol).get(newRow).right) {
        this.movePowerStation(newCol, newRow);
        this.updatePower();
      }
    }
    else if (key.equals("right")) {
      newCol++;
      if (newCol < this.width && currentPiece.right
        && this.board.get(newCol).get(newRow).left) {
        this.movePowerStation(newCol, newRow);
        this.updatePower();
      }
    }
  }

  // Purpose: Updates the power station's location
  void movePowerStation(int newCol, int newRow) {
    this.board.get(this.powerCol).get(this.powerRow).powerStation = false;
    this.powerCol = newCol;
    this.powerRow = newRow;
    this.board.get(this.powerCol).get(this.powerRow).powerStation = true;
  }

  // Purpose: Recomputes which tiles have power based on current wiring and radius
  void updatePower() {
    ArrayList<GamePiece> poweredPieces = new ArrayList<GamePiece>();
    ArrayList<GamePiece> queue = new ArrayList<GamePiece>();
    ArrayList<Integer> depths = new ArrayList<Integer>();

    GamePiece start = this.board.get(this.powerCol).get(this.powerRow);
    queue.add(start);
    poweredPieces.add(start);
    depths.add(0);

    int index = 0;
    while (index < queue.size()) {
      GamePiece current = queue.get(index);
      int depth = depths.get(index);
      index++;

      // Do not go farther than the allowed radius
      if (depth >= this.radius) {
        continue;
      }

      // Add neighboring tiles if wires match and tile isn't already powered
      if (current.left && current.col > 0) {
        GamePiece n = this.board.get(current.col - 1).get(current.row);
        if (n.right && !poweredPieces.contains(n)) {
          poweredPieces.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }

      if (current.right && current.col < this.width - 1) {
        GamePiece n = this.board.get(current.col + 1).get(current.row);
        if (n.left && !poweredPieces.contains(n)) {
          poweredPieces.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }

      if (current.top && current.row > 0) {
        GamePiece n = this.board.get(current.col).get(current.row - 1);
        if (n.bottom && !poweredPieces.contains(n)) {
          poweredPieces.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }

      if (current.bottom && current.row < this.height - 1) {
        GamePiece n = this.board.get(current.col).get(current.row + 1);
        if (n.top && !poweredPieces.contains(n)) {
          poweredPieces.add(n);
          queue.add(n);
          depths.add(depth + 1);
        }
      }
    }

    this.poweredPieces = poweredPieces;
  }

  // Purpose: Checks whether a specific tile currently has power
  boolean isPoweredAt(int col, int row) {
    return this.poweredPieces.contains(this.board.get(col).get(row));
  }

  // Purpose: Checks whether every tile on the board has power
  boolean allPowered() {
    for (GamePiece piece : this.nodes) {
      if (!this.poweredPieces.contains(piece)) {
        return false;
      }
    }
    return true;
  }

  // Purpose: Draws the board and win message (during endgame)
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width * 50, this.height * 50);

    for (int col = 0; col < this.width; col++) {
      for (int row = 0; row < this.height; row++) {
        GamePiece piece = this.board.get(col).get(row);
        Color wireColor =
          this.isPoweredAt(col, row) ? Color.YELLOW : Color.GRAY;
        WorldImage tileImg =
          piece.tileImage(50, 5, wireColor, piece.powerStation);
        scene.placeImageXY(tileImg, col * 50 + 25, row * 50 + 25);
      }
    }

    if (this.allPowered()) {
      scene.placeImageXY(new TextImage("YOU WIN!", 30, Color.GREEN),
        this.width * 25, this.height * 25);
    }

    return scene;
  }
}


class ExamplesLightEmAll {
// Test that updatePower recomputes powered tiles after a rotation
void testUpdatePower(Tester t) {
  LightEmAll game = new LightEmAll(3, 3);

  int pc = game.powerCol;
  int pr = game.powerRow;

  // Pick a tile directly above the power station
  GamePiece neighbor = game.board.get(pc).get(pr - 1);

  // Force a known connection
  neighbor.bottom = true;
  game.board.get(pc).get(pr).top = true;

  game.updatePower();
  t.checkExpect(game.isPoweredAt(pc, pr - 1), true);

  // Break the connection
  neighbor.bottom = false;
  game.board.get(pc).get(pr).top = false;

  game.updatePower();
  t.checkExpect(game.isPoweredAt(pc, pr - 1), false);
}

  // Test that the power station starts in the correct place
  void testPowerStationStart(Tester t) {
    LightEmAll game = new LightEmAll(4, 4);
    t.checkExpect(game.powerCol, 2);
    t.checkExpect(game.powerRow, 2);
  }

  // Visual test
  void testGame(Tester t) {
    // Testing for Normal Game (uncomment for play, comment out fractal board version)
    // LightEmAll game = new LightEmAll(4, 4);
    // Testing for Fractal Game
    LightEmAll game = new LightEmAll(4, 4, true);
    game.bigBang(game.width * 50, game.height * 50, 0.2);
  }
}