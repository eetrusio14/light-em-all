import java.awt.Color;
import javalib.worldimages.*;

// Represents a single tile in the LightEmAll game board, with wires that can connect to adjacent
// tiles in any of the four cardinal directions
class GamePiece {
  int row; // y-coordinate of this piece on the board
  int col; // x-coordinate of this piece on the board
  boolean left; // whether this piece has a wire extending left
  boolean right; // whether this piece has a wire extending right
  boolean top; // whether this piece has a wire extending up
  boolean bottom; // whether this piece has a wire extending down
  boolean powerStation; // whether the power station is located on this piece

  // Initializes a GamePiece at the given position with the specified wire connections
  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = false;
  }

  // Purpose: Rotates this piece 90 degrees clockwise
  void rotate() {
    boolean oldLeft = this.left;
    boolean oldRight = this.right;
    boolean oldTop = this.top;
    boolean oldBottom = this.bottom;

    // Clockwise rotate: left -> top, top -> right, right -> bottom, bottom -> left
    this.top = oldLeft;
    this.right = oldTop;
    this.bottom = oldRight;
    this.left = oldBottom;
  }

  // Purpose: Draws this tile as a WorldImage with wires in the specified color and a power station marker if applicable
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    // Tile is gray to start
    WorldImage tile = new RectangleImage(size, size, "solid", Color.DARK_GRAY);

    // Draws wires in each direction if they exist
    if (this.left) {
      WorldImage leftWire = new RectangleImage(size / 2, wireWidth, "solid", wireColor);
      tile = new OverlayOffsetImage(leftWire, size / 4, 0, tile);
    }
    if (this.right) {
      WorldImage rightWire = new RectangleImage(size / 2, wireWidth, "solid", wireColor);
      tile = new OverlayOffsetImage(rightWire, -size / 4, 0, tile);
    }

    if (this.top) {
      WorldImage topWire = new RectangleImage(wireWidth, size / 2, "solid", wireColor);
      tile = new OverlayOffsetImage(topWire, 0, size / 4, tile);
    }
    if (this.bottom) {
      WorldImage bottomWire = new RectangleImage(wireWidth, size / 2, "solid", wireColor);
      tile = new OverlayOffsetImage(bottomWire, 0, -size / 4, tile);
    }

    // Draws power station if exists
    if (hasPowerStation) {
      WorldImage star = new StarImage(size / 3, 7, OutlineMode.SOLID, Color.CYAN);
      tile = new OverlayImage(star, tile);
    }
    return tile;
  }
}