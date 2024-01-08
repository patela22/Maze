import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

// Represents the wall in the maze
class Walls {
  Path fromPath;
  Path toPath;
  int weight;

  Walls(Path fromPath, Path toPath, int weight) {
    this.fromPath = fromPath;
    this.toPath = toPath;
    this.weight = weight;
  }
}

// Path class (Edges)
class Path {

  int x;
  int y;
  Color curColor;

  Path(int x, int y) {
    this.x = x;
    this.y = y;
    this.curColor = Color.lightGray;
  }

  // Overrides the .equals method in Object class
  // Checks if two paths are equal
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Path)) {
      return false;
    }

    Path pathValue = (Path) o;
    return this.x == pathValue.x && this.y == pathValue.y;
  }

  // Overrides the .hashCode method in Object class
  // Creates a hashCode based on x and y values
  public int hashCode() {
    int result = 17;
    result = 31 * result + x;
    result = 31 * result + y;
    return result;
  }

  void changeColor(Color type) {
    this.curColor = type;
  }

}

// Function object to sort Walls by ascending order by weight
class WallSort implements Comparator<Walls> {
  public int compare(Walls o1, Walls o2) {
    if (o1.weight < o2.weight) {
      return 1;
    }
    else if (o1.weight > o2.weight) {
      return -1;
    }
    else {
      return 0;
    }
  }
}

// Union find data structure to find 
class UnionFindPath {
  Map<Path, Path> pointer;

  // Constructor
  UnionFindPath() {
    this.pointer = new HashMap<>();
  }

  // Finds the representative path for any given path
  Path find(Path key) {
    // Create a new hash if it doesn't exist
    if (!this.pointer.containsKey(key)) {
      this.pointer.put(key, key);
      return key;
    }

    // Finds the root of the key
    Path root = key;
    while (this.pointer.get(root) != root) {
      root = this.pointer.get(root);
    }
    return root;
  }

  // Returns boolean value if in same representative
  // EFFECT: sets representative of path2 to path1 if not already
  // under same representative
  boolean union(Path path1, Path path2) {
    Path root1 = this.find(path1);
    Path root2 = this.find(path2);

    // if they are already in the same set
    if (root1.equals(root2)) {
      return false;
    }
    else {
      this.pointer.put(root2, root1);
      return true;
    }
  }
}

// A simulation for a maze
class MazeWorld extends World {
  int yLength;
  int xLength;
  Random rand;
  ArrayList<Walls> board;
  ArrayList<Walls> wallBoard;
  ArrayList<Path> pathBoard;
  ICollection<Path> worklist;
  boolean search;
  boolean complete;
  boolean manual;
  boolean winScreen;
  Path first;
  Path last;
  Path curPath;
  Map<Path, Path> path;
  Color choice;

  MazeWorld(int xLength, int yLength) {
    this(xLength, yLength, new Random().nextInt());
  }

  MazeWorld(int xLength, int yLength, int seed) {
    this.xLength = xLength;
    this.yLength = yLength;
    this.board = new ArrayList<Walls>();
    this.wallBoard = new ArrayList<Walls>();
    this.pathBoard = new ArrayList<Path>();
    this.rand = new Random(seed);
    this.search = false;
    this.path = new HashMap<Path, Path>();
    this.initMaze();
  }

  // Initializes the maze
  void initMaze() {

    // Create the Paths
    this.createPath();

    // Create the Walls
    this.createWalls();

    // Run kruskals in order to choose the walls
    this.board.sort(new WallSort());
    UnionFindPath ufp = new UnionFindPath();
    ArrayList<Walls> tempWalls = new ArrayList<Walls>();

    for (Walls wall : this.board) {
      if (ufp.union(wall.fromPath, wall.toPath)) {
        tempWalls.add(wall);
      }
    }
    this.wallBoard = this.board;
    this.wallBoard.removeAll(tempWalls);
    this.board = tempWalls;

    // Change the colors for the start and end point
    this.first = this.pathBoard.get(0);
    this.first.changeColor(Color.green);
    this.last = this.pathBoard.get(this.xLength * this.yLength - 1);
    this.last.changeColor(Color.red);
    this.curPath = this.pathBoard.get(0);
  }

  // Create the Paths for the board
  // EFFECT: Adds Paths to cellBoard
  void createPath() {
    for (int y = 0; y < this.yLength; y++) {
      for (int x = 0; x < this.xLength; x++) {
        this.pathBoard.add(new Path(x, y));
      }
    }
  }

  // Create the walls for the board
  // EFFECT: Adds walls to the board
  void createWalls() {
    for (int x = 0; x < this.xLength; x++) {
      for (int y = 0; y < this.yLength; y++) {
        Path cur = this.pathBoard.get(x + y * this.xLength);
        if (y < this.yLength - 1) {
          Path next = this.pathBoard.get(x + (y + 1) * this.xLength);
          this.board.add(new Walls(cur, next, this.rand.nextInt()));
        }
        if (x < this.xLength - 1) {
          Path next = this.pathBoard.get(x + 1 + y * this.xLength);
          this.board.add(new Walls(cur, next, this.rand.nextInt()));
        }

      }
    }
  }

  // Register different key events on the board
  // EFFECT: initiates breadth first and depth first as well
  // as manual key strokes
  public void onKeyEvent(String key) {
    // Starts breadth first stroke
    if (key.equals("b") && !this.search) {
      this.search = true;
      this.worklist = new Queue<Path>();
      this.worklist.add(this.pathBoard.get(0));
      this.choice = Color.cyan;
    }
    // Starts depth first search
    else if (key.equals("d") && !this.search) {
      this.search = true;
      this.worklist = new Stack<Path>();
      this.worklist.add(this.pathBoard.get(0));
      this.choice = Color.magenta;
    }
    // Resets the board and creates a new maze
    else if (key.equals("r")) {
      this.board = new ArrayList<Walls>();
      this.wallBoard = new ArrayList<Walls>();
      this.pathBoard = new ArrayList<Path>();
      this.search = false;
      this.choice = Color.lightGray;
      this.path = new HashMap<Path, Path>();
      this.manual = false;
      this.initMaze();
    }
    // Manual mode
    // Moves the block down
    else if (key.equals("down") && !this.winScreen && !this.search) {
      this.manual = true;
      move("down");
    }
    // Moves the block up
    else if (key.equals("up") && !this.winScreen && !this.search) {
      this.manual = true;
      move("up");
    }
    // Moves the block to the left
    else if (key.equals("left") && !this.winScreen && !this.search) {
      this.manual = true;
      move("left");
    }
    // Moves the block to the right
    else if (key.equals("right") && !this.winScreen && !this.search) {
      this.manual = true;
      move("right");
    }
  }

  // Checks the attempt path and sees if it is a possible movement
  boolean edgeExists(Path attempt) {
    boolean result = false;
    for (Walls w : this.board) {
      if (w.fromPath.equals(this.curPath) && w.toPath.equals(attempt)
          || w.toPath.equals(this.curPath) && w.fromPath.equals(attempt)) {
        result = true;
      }
    }
    return result;
  }

  // Changes the location of the block
  // EFFECT: Moves a the block a certain direction depending on the key stroke
  void move(String type) {
    Path attempt = null;
    int attemptNum = 0;
    if (type.equals("right")) {
      attemptNum = this.curPath.x + 1 + this.curPath.y * this.xLength;
    }
    else if (type.equals("left")) {
      attemptNum = this.curPath.x - 1 + this.curPath.y * this.xLength;
    }
    else if (type.equals("up")) {
      attemptNum = this.curPath.x + (this.curPath.y - 1) * this.xLength;
    }
    else if (type.equals("down")) {
      attemptNum = this.curPath.x + (1 + this.curPath.y) * this.xLength;
    }

    if (attemptNum > 0 && attemptNum < this.pathBoard.size()) {
      attempt = this.pathBoard.get(attemptNum);
    }

    if (edgeExists(attempt)) {
      this.curPath.changeColor(Color.lightGray);
      this.path.put(attempt, this.curPath);
      this.curPath = attempt;
      this.curPath.changeColor(Color.pink);
      if (attempt.equals(this.last)) {
        this.worklist = new Queue<Path>();
        this.worklist.add(this.first);
        this.choice = Color.lightGray;
        while (!this.complete) {
          this.searchHelp(this.worklist);
        }
        this.winScreen = true;
        this.manual = false;
      }
    }
  }

  // On tick, search the maze using breadth first or depth first search
  public void onTick() {
    if (this.search && !this.manual) {
      searchHelp(this.worklist);
    }
    else if (this.complete) {
      remake(this.last);
    }
  }

  // Remakes the correct path to get to the end once the solution is found
  void remake(Path before) {
    Path onTrack = this.path.get(before);
    if (onTrack != null) {
      onTrack.changeColor(Color.orange);
      remake(onTrack);
    }
  }

  // The function to find the end path
  // EFFECT: changes searched blocks into a certain color, depending on the search
  // pattern. Also adds the path to ongoing list of paths gone through
  void searchHelp(ICollection<Path> worklist) {
    if (!worklist.isEmpty()) {
      Path next = worklist.remove();
      next.changeColor(this.choice);

      if (next.equals(this.last)) {
        this.last.changeColor(Color.orange);
        this.search = false;
        this.complete = true;
      }
      else {
        Iterator<Walls> wallsIterator = this.board.iterator();

        while (wallsIterator.hasNext()) {
          Walls w = wallsIterator.next();
          if (w.fromPath.equals(next)) {
            wallsIterator.remove();
            this.worklist.add(w.toPath);
            this.path.put(w.toPath, next);
          }
          else if (w.toPath.equals(next)) {
            wallsIterator.remove();
            this.worklist.add(w.fromPath);
            this.path.put(w.fromPath, next);
          }
        }
      }
    }
  }

  // Makes a visual representation of the maze
  public WorldScene makeScene() {
    WorldScene holder = new WorldScene(this.xLength * 10, this.yLength * 10);
    for (Path path : this.pathBoard) {
      holder.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, path.curColor),
          path.x * 10 + 5, path.y * 10 + 5);
    }
    for (Walls wall : this.wallBoard) {
      Path fromPath = wall.fromPath;
      Path toPath = wall.toPath;
      if ((fromPath.x - toPath.x) == 0) {
        holder.placeImageXY(new LineImage(new Posn(10, 0), Color.black), fromPath.x * 10 + 5,
            fromPath.y * 10 + 10);
      }
      else {
        holder.placeImageXY(new LineImage(new Posn(0, 10), Color.black), fromPath.x * 10 + 10,
            fromPath.y * 10 + 5);
      }
    }
    if (this.winScreen) {
      holder.placeImageXY(new TextImage("Congratulations :)", Color.black), this.xLength * 5,
          this.yLength * 10 + 50);
    }

    return holder;
  }
}

//Represents a mutable collection of items
interface ICollection<T> {
  // Is this collection empty?
  boolean isEmpty();

  // EFFECT: adds the item to the collection
  void add(T item);

  // Returns the first item of the collection
  // EFFECT: removes that first item
  T remove();
}

// a representation of a stack
class Stack<T> implements ICollection<T> {
  ArrayDeque<T> contents;

  Stack() {
    this.contents = new ArrayDeque<T>();
  }

  // Is this collection empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // Returns the first item of the collection
  // EFFECT: removes that first item
  public T remove() {
    return this.contents.removeFirst();
  }

  // EFFECT: adds the item to the collection
  public void add(T item) {
    this.contents.addFirst(item);
  }
}

class Queue<T> implements ICollection<T> {
  ArrayDeque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<T>();
  }

  // Is this collection empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // Returns the first item of the collection
  // EFFECT: removes that first item
  public T remove() {
    return this.contents.removeFirst();
  }

  // EFFECT: adds the item to the collection
  public void add(T item) {
    this.contents.addLast(item);
  }
}

// An examples class to test the maze
class ExampleMaze {

  Path path1;
  Path path2;
  Path path3;
  Path path4;
  Path path5;
  Walls wall1;
  Walls wall2;
  Walls wall3;
  Walls wall4;
  WallSort sort;
  UnionFindPath ufp1;
  MazeWorld mw1;
  MazeWorld mw2;
  MazeWorld mw3;
  MazeWorld mw4;
  ICollection<Path> queue;
  ICollection<Path> stack;

  // Initializes the examples
  void initExample() {
    this.path1 = new Path(0, 0);
    this.path2 = new Path(0, 0);
    this.path3 = new Path(0, 1);
    this.path4 = new Path(1, 0);
    this.path5 = new Path(1, 1);
    this.wall1 = new Walls(this.path1, this.path3, 10);
    this.wall2 = new Walls(this.path1, this.path4, 5);
    this.wall3 = new Walls(this.path4, this.path5, 3);
    this.wall4 = new Walls(this.path3, this.path5, 8);
    this.sort = new WallSort();
    this.ufp1 = new UnionFindPath();
    this.mw1 = new MazeWorld(5, 5, 1);
    this.mw2 = new MazeWorld(4, 6, 2);
    this.mw3 = new MazeWorld(6, 4, 3);
    this.mw4 = new MazeWorld(2, 3, 4);
    this.stack = new Stack<Path>();
    this.queue = new Queue<Path>();
  }

  // tests the .equals Override
  void testEquals(Tester t) {
    this.initExample();
    t.checkExpect(this.path1.equals(this.path2), true);
    t.checkExpect(this.path1.equals(this.path3), false);
  }

  // Tests compare
  void testCompare(Tester t) {
    this.initExample();
    t.checkExpect(this.sort.compare(this.wall1, this.wall2), -1);
    t.checkExpect(this.sort.compare(this.wall3, this.wall4), 1);
    t.checkExpect(this.sort.compare(this.wall2, this.wall4), 1);
    t.checkExpect(this.sort.compare(this.wall1, this.wall3), -1);
  }

  // A combination of find and union in order to test both to the fullest
  // Tests the methods of UnionFind class
  void testUnionFind(Tester t) {
    this.initExample();
    t.checkExpect(this.ufp1.find(this.path1), this.path1);
    t.checkExpect(this.ufp1.union(this.path1, this.path1), false);
    t.checkExpect(this.ufp1.find(this.path2), this.path2);
    t.checkExpect(this.ufp1.union(this.path1, this.path3), true);
    t.checkExpect(this.ufp1.find(this.path1), this.path1);
    t.checkExpect(this.ufp1.find(this.path3), this.path1);
    t.checkExpect(this.ufp1.union(this.path1, this.path3), false);
    t.checkExpect(this.ufp1.union(this.path1, this.path1), false);
  }

  // Tests createPath method
  void testCreatePath(Tester t) {
    this.initExample();
    this.mw1.pathBoard.clear();
    this.mw2.pathBoard.clear();
    this.mw3.pathBoard.clear();
    t.checkExpect(this.mw1.pathBoard.size(), 0);
    t.checkExpect(this.mw2.pathBoard.size(), 0);
    t.checkExpect(this.mw3.pathBoard.size(), 0);
    this.mw1.createPath();
    this.mw2.createPath();
    this.mw3.createPath();
    t.checkExpect(this.mw1.pathBoard.size(), 25);
    t.checkExpect(this.mw2.pathBoard.size(), 24);
    t.checkExpect(this.mw3.pathBoard.size(), 24);
    t.checkExpect(this.mw1.pathBoard.get(0), this.path1);
    t.checkExpect(this.mw1.pathBoard.get(1), this.path4);
  }

  // tests the createWalls method
  void testCreateWalls(Tester t) {
    this.initExample();
    this.mw1.board.clear();
    this.mw2.board.clear();
    this.mw3.board.clear();
    this.mw1.pathBoard.clear();
    this.mw2.pathBoard.clear();
    this.mw3.pathBoard.clear();
    t.checkExpect(this.mw1.pathBoard.size(), 0);
    t.checkExpect(this.mw2.pathBoard.size(), 0);
    t.checkExpect(this.mw3.pathBoard.size(), 0);
    t.checkExpect(this.mw1.board.size(), 0);
    t.checkExpect(this.mw2.board.size(), 0);
    t.checkExpect(this.mw3.board.size(), 0);
    this.mw1.createPath();
    this.mw2.createPath();
    this.mw3.createPath();
    this.mw1.createWalls();
    this.mw2.createWalls();
    this.mw3.createWalls();
    t.checkExpect(this.mw1.board.size(), 40);
    t.checkExpect(this.mw2.board.size(), 38);
    t.checkExpect(this.mw3.board.size(), 38);
    t.checkExpect(this.mw1.board.get(0).fromPath, this.path1);
    t.checkExpect(this.mw1.board.get(0).toPath, this.path3);
    t.checkExpect(this.mw2.board.get(1).fromPath, this.path1);
    t.checkExpect(this.mw2.board.get(1).toPath, this.path4);
    t.checkExpect(this.mw3.board.get(7).fromPath, this.path4);
    t.checkExpect(this.mw3.board.get(7).toPath, this.path5);
  }

  // tests the makeScene method
  void testMakeScene(Tester t) {
    this.initExample();
    WorldScene holder = new WorldScene(20, 30);
    holder.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, Color.green), 5, 5);
    holder.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, Color.lightGray), 15, 5);
    holder.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, Color.lightGray), 5, 15);
    holder.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, Color.lightGray), 15, 15);
    holder.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, Color.lightGray), 5, 25);
    holder.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, Color.red), 15, 25);
    holder.placeImageXY(new LineImage(new Posn(10, 0), Color.black), 5, 10);
    holder.placeImageXY(new LineImage(new Posn(10, 0), Color.black), 15, 20);
    t.checkExpect(this.mw4.makeScene(), holder);
    this.mw4.winScreen = true;
    holder.placeImageXY(new TextImage("Congratulations :)", Color.black), 10, 80);
  }

  // tests the initMaze method
  void testInitMaze(Tester t) {
    this.initExample();
    this.mw1.board.clear();
    this.mw1.pathBoard.clear();
    this.mw1.wallBoard.clear();
    this.mw2.board.clear();
    this.mw2.pathBoard.clear();
    this.mw2.wallBoard.clear();
    this.mw3.board.clear();
    this.mw3.pathBoard.clear();
    this.mw3.wallBoard.clear();
    this.mw1.initMaze();
    this.mw2.initMaze();
    this.mw3.initMaze();
    t.checkExpect(this.mw1.board.size(), 24);
    t.checkExpect(this.mw2.board.size(), 23);
    t.checkExpect(this.mw3.board.size(), 23);
    t.checkExpect(this.mw1.pathBoard.size(), 25);
    t.checkExpect(this.mw2.pathBoard.size(), 24);
    t.checkExpect(this.mw3.pathBoard.size(), 24);
    t.checkExpect(this.mw1.pathBoard.get(0).curColor, Color.green);
    t.checkExpect(this.mw2.pathBoard.get(0).curColor, Color.green);
    t.checkExpect(this.mw3.pathBoard.get(0).curColor, Color.green);
    t.checkExpect(this.mw1.pathBoard.get(24).curColor, Color.red);
    t.checkExpect(this.mw2.pathBoard.get(23).curColor, Color.red);
    t.checkExpect(this.mw3.pathBoard.get(23).curColor, Color.red);
    t.checkExpect(this.mw1.board.removeAll(this.mw1.wallBoard), false);
    t.checkExpect(this.mw2.board.removeAll(this.mw2.wallBoard), false);
    t.checkExpect(this.mw3.board.removeAll(this.mw3.wallBoard), false);
  }

  // Creates a test for hashCode
  void testHashCode(Tester t) {
    this.initExample();
    t.checkExpect(this.path1.hashCode(), 16337);
    t.checkExpect(this.path2.hashCode(), 16337);
    t.checkExpect(this.path3.hashCode(), 16338);
    t.checkExpect(this.path4.hashCode(), 16368);
    t.checkExpect(this.path5.hashCode(), 16369);
  }

  // Creates tests for onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.initExample();
    t.checkExpect(this.mw1.search, false);
    t.checkExpect(this.mw2.search, false);
    t.checkExpect(this.mw1.manual, false);
    t.checkExpect(this.mw1.choice, null);
    t.checkExpect(this.mw2.choice, null);
    this.mw1.onKeyEvent("b"); // breadth
    this.mw2.onKeyEvent("d"); // depth
    t.checkExpect(this.mw1.choice, Color.cyan);
    t.checkExpect(this.mw2.choice, Color.magenta);
    t.checkExpect(this.mw1.manual, false);
    t.checkExpect(this.mw1.search, true);
    t.checkExpect(this.mw2.search, true);
    this.mw2.onKeyEvent("b"); // Supposed to check if there would be any changes
    t.checkExpect(this.mw2.choice, Color.magenta); // Shouldn't be any
    this.mw1.onKeyEvent("r"); // resets
    this.mw2.onKeyEvent("r");
    t.checkExpect(this.mw1.search, false);
    t.checkExpect(this.mw2.search, false);
    t.checkExpect(this.mw1.manual, false);
    t.checkExpect(this.mw1.choice, Color.lightGray);
    t.checkExpect(this.mw2.choice, Color.lightGray);
    this.mw1.onKeyEvent("up");
    t.checkExpect(this.mw1.manual, true);
    t.checkExpect(this.mw1.curPath.x, 0);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.onKeyEvent("down"); // Shouldn't work
    t.checkExpect(this.mw1.curPath.x, 0);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.onKeyEvent("right"); // should work
    t.checkExpect(this.mw1.curPath.x, 1);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.onKeyEvent("right");
    t.checkExpect(this.mw1.curPath.x, 2);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.onKeyEvent("down");
    t.checkExpect(this.mw1.curPath.x, 2);
    t.checkExpect(this.mw1.curPath.y, 1);
    this.mw1.onKeyEvent("up");
    t.checkExpect(this.mw1.curPath.x, 2);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.onKeyEvent("right");
    this.mw1.onKeyEvent("down");
    this.mw1.onKeyEvent("down");
    this.mw1.onKeyEvent("down");
    this.mw1.onKeyEvent("down");
    this.mw1.onKeyEvent("right");
    this.mw1.onKeyEvent("right");
    this.mw1.onKeyEvent("down");
    t.checkExpect(this.mw1.winScreen, true); // end
    t.checkExpect(this.mw1.manual, false);
    this.mw1.onKeyEvent("down");
    t.checkExpect(this.mw1.manual, false); // Shouldn't change after done
  }

  // Creates tests for edgeExists
  void testEdgeExists(Tester t) {
    this.initExample();
    t.checkExpect(this.mw1.edgeExists(this.path3), false);
    t.checkExpect(this.mw1.edgeExists(this.path4), true);
    t.checkExpect(this.mw1.edgeExists(this.path5), false);
    t.checkExpect(this.mw2.edgeExists(this.path3), false);
    t.checkExpect(this.mw2.edgeExists(this.path4), true);
    t.checkExpect(this.mw2.edgeExists(this.path5), false);
  }

  // Creates a test for remake
  void testRemake(Tester t) {
    this.initExample();
    t.checkExpect(this.mw1.first.curColor, Color.green);
    t.checkExpect(this.mw1.pathBoard.get(7).curColor, Color.lightGray);
    t.checkExpect(this.mw1.last.curColor, Color.red);
    this.mw1.onKeyEvent("d");
    while (this.mw1.search) {
      this.mw1.searchHelp(this.mw1.worklist);
    } // Needed to make the path for the function to be called
    this.mw1.remake(this.mw1.last);
    t.checkExpect(this.mw1.pathBoard.get(2).curColor, Color.orange);
    t.checkExpect(this.mw1.pathBoard.get(7).curColor, Color.orange);
    t.checkExpect(this.mw1.last.curColor, Color.orange);
  }

  // Creates a test for isEmpty
  void testIsEmpty(Tester t) {
    this.initExample();
    t.checkExpect(this.queue.isEmpty(), true);
    t.checkExpect(this.stack.isEmpty(), true);
    this.queue.add(this.path1);
    this.stack.add(this.path1);
    t.checkExpect(this.stack.isEmpty(), false);
    t.checkExpect(this.queue.isEmpty(), false);
  }

  // Creates a test for remove
  void testRemove(Tester t) {
    this.initExample();
    this.queue.add(this.path1);
    this.stack.add(this.path1);
    t.checkExpect(this.queue.remove(), this.path1);
    t.checkExpect(this.stack.remove(), this.path1);
    t.checkException(new NoSuchElementException(), this.queue, "remove");
    t.checkException(new NoSuchElementException(), this.stack, "remove");
  }

  // Creates a test for add
  void testAdd(Tester t) {
    this.initExample();
    t.checkExpect(this.queue.isEmpty(), true);
    t.checkExpect(this.stack.isEmpty(), true);
    this.queue.add(this.path1);
    this.stack.add(this.path1);
    t.checkExpect(this.queue.remove(), this.path1);
    t.checkExpect(this.stack.remove(), this.path1);
  }

  // Creates a test for searchHelp
  void testSearchHelp(Tester t) {
    this.initExample();
    t.checkExpect(this.mw1.first.curColor, Color.green);
    t.checkExpect(this.mw2.first.curColor, Color.green);
    this.mw1.onKeyEvent("b");
    this.mw2.onKeyEvent("d");
    this.mw1.searchHelp(this.mw1.worklist);
    this.mw2.searchHelp(this.mw2.worklist);
    t.checkExpect(this.mw1.worklist.isEmpty(), false);
    t.checkExpect(this.mw2.worklist.isEmpty(), false);
    t.checkExpect(this.mw1.first.curColor, Color.cyan);
    t.checkExpect(this.mw2.first.curColor, Color.magenta);
  }

  // Creates a test for onTick
  void testOnTick(Tester t) {
    this.initExample();
    t.checkExpect(this.mw1.search, false);
    t.checkExpect(this.mw2.search, false);
    t.checkExpect(this.mw1.first.curColor, Color.green);
    t.checkExpect(this.mw2.first.curColor, Color.green);
    this.mw1.onTick(); // No Change
    this.mw2.onTick();
    t.checkExpect(this.mw1.search, false);
    t.checkExpect(this.mw2.search, false);
    t.checkExpect(this.mw1.first.curColor, Color.green);
    t.checkExpect(this.mw2.first.curColor, Color.green);
    this.mw1.onKeyEvent("b");
    this.mw2.onKeyEvent("d");
    t.checkExpect(this.mw1.worklist.isEmpty(), false);
    t.checkExpect(this.mw2.worklist.isEmpty(), false);
    this.mw1.onTick();
    this.mw2.onTick();
    t.checkExpect(this.mw1.search, true);
    t.checkExpect(this.mw2.search, true);
    t.checkExpect(this.mw1.first.curColor, Color.cyan);
    t.checkExpect(this.mw2.first.curColor, Color.magenta);
  }

  // Creates a test for Move
  void testMove(Tester t) {
    this.initExample();
    this.mw1.move("up");
    t.checkExpect(this.mw1.curPath.x, 0);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.move("down"); // Shouldn't work
    t.checkExpect(this.mw1.curPath.x, 0);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.move("right"); // should work
    t.checkExpect(this.mw1.curPath.x, 1);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.move("right");
    t.checkExpect(this.mw1.curPath.x, 2);
    t.checkExpect(this.mw1.curPath.y, 0);
    this.mw1.move("down");
    t.checkExpect(this.mw1.curPath.x, 2);
    t.checkExpect(this.mw1.curPath.y, 1);
    this.mw1.move("up");
    t.checkExpect(this.mw1.curPath.x, 2);
    t.checkExpect(this.mw1.curPath.y, 0);
  }

  // Creates a maze bigBang
  void testMaze(Tester t) {
    MazeWorld starterWorld = new MazeWorld(100, 60);
    int sceneSize = 1000;
    starterWorld.bigBang(sceneSize, sceneSize, 0.005);
  }
}
