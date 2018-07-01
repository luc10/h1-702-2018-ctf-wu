


# Beware of serialization, you who code!

Java, as well as other languages, allows to serialize classes (generally objects) and save its content somewhere (file, database, paper - by hand -). Obviously, it's possible to do the inverse. It's like taking a picture of current state of (each property of) a particular object and restore it when necessary.

Challenge 4 relies on serialization.

## The :door:\_|\\/|\_:runner:aze

This challenge comes with a little maze game:

![](https://i.imgur.com/fdWPJ7x.png)

The red dot must reach the green one to complete the level and pass to next one.

## Jaxd, how you do with riddles?

This time there're more classes to inspect:

- BroadcastAnnouncer;
- ~~BuildConfig~~;
- ~~Dot~~;
- ~~Drawable~~ (interface);
- ~~Exit~~;
- **GameManager**;
- **GameState**;
- ~~InfoActivity~~;
- **MainActivity**;
- **Maze**;
- **MazeMover**;
- ~~MazeView~~;
- **MenuActivity**;
- ~~Player~~;
- ~~R~~;
- **StateController**;
- **StateLoader**.

For a total of 8 classes to be analyzed. Since the first view is the MenuActivity one we'll start from that.

### MenuActivity

```java
public class MenuActivity extends AppCompatActivity implements OnClickListener {
    protected void onCreate(Bundle bundle) {
        ...
        ((Button) findViewById(R.id.StartGame)).setOnClickListener(this);
        ((Button) findViewById(R.id.info)).setOnClickListener(this);
        ...
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("start_game")) {
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            }
        }, new IntentFilter("com.hackerone.mobile.challenge4.menu"));
    }

    public void onClick(View view) {
        ...
    }
}
```

Two buttons, a callback (`onView`) and an interesting thing: a `BroadcastReceiver`. A [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver) allows different apps to communicate and pass data to each other by using an internal/native android broadcast service. The flow is pretty simple:

![](https://i.imgur.com/DT4vqxi.png)

Data is passed through **Intent** class (with a specified **action**)  by **sendBroadcast** method from **App A**. The Android service receives the message and dispatches it to any app that has a **BroadcastReceiver** registered for that specific **action**.

This means that we can start the game with few lines of Java code:

```java
    public void start() {  
        context  
            .sendBroadcast(  
                 new Intent(MenuAction)  
                    .putExtra("start_game", true)  
        );  
    }
```
Which ends in:

![](https://i.imgur.com/vzxJ0kL.gif)

Amazing! We're controlling the app flow.

### MainActivity

We can now inspect the **MainActivity** class:

```java
public class MainActivity extends AppCompatActivity {
    ...

    protected void onCreate(Bundle bundle) {
        ...
        bundle = new GameManager();
        ...
        context = getApplicationContext();
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                MazeMover.onReceive(context, intent);
            }
        }, new IntentFilter("com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER"));
    }

    ...
    public static MazeView getMazeView() {
        return view;
    }
}
```

A new class instance of type **GameManager** is assigned to **bundle**  and another **BroadcastReceiver** is registered with *com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER* **filter**. Its handler is located under another class however.

### MazeMover

MazeMover class, which handles broadcasted intents I was talking above, contains juicy code:

```java
public class MazeMover {
    public static void onReceive(Context context, Intent intent) {
        ...
        GameManager gameManager = MainActivity.getMazeView().getGameManager();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (intent.hasExtra("get_maze")) {
                intent = new Intent();
                intent.putExtra("walls", gameManager.getMaze().getWalls());
                Serializable arrayList = new ArrayList();
                arrayList.add(Integer.valueOf(gameManager.getPlayer().getX()));
                arrayList.add(Integer.valueOf(gameManager.getPlayer().getY()));
                arrayList.add(Integer.valueOf(gameManager.getExit().getX()));
                arrayList.add(Integer.valueOf(gameManager.getExit().getY()));
                intent.putExtra("positions", arrayList);
                intent.setAction("com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER");
                context.sendBroadcast(intent);
            } else if (intent.hasExtra("move")) {
                intent = extras.getChar("move");
                int i = -1;
                int i2 = 0;
                switch (intent) {
                    case 104:
                        i2 = -1;
                        i = 0;
                        break;
                    case 106:
                        i = 1;
                        break;
                    case 107:
                        break;
                    case 108:
                        i = 0;
                        i2 = 1;
                        break;
                    default:
                        i = 0;
                        break;
                }
                intent = new Point(i2, i);
                Intent intent2 = new Intent();
                if (gameManager.movePlayer(intent) != null) {
                    intent2.putExtra("move_result", "good");
                } else {
                    intent2.putExtra("move_result", "bad");
                }
                intent2.setAction("com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER");
                context.sendBroadcast(intent2);
            } else if (intent.hasExtra("cereal")) {
                ((GameState) intent.getSerializableExtra("cereal")).initialize(context);
            }
        }
    }
}
```

The **GameManager** object previously initialized from MainActivity class is retrieved and few checks occur. Depending on **extra** present into the **Intent** object, the code performs actions and/or sends back useful data using the broadcast service. Give a look at the following table:

| Has extra named| Action performed | Data sent back |
|--|--|--|
| get_maze || Walls (maze), player and exit positions
|move| move player | good/bad move
|cereal|**deserialization**||

Again, we can type down few lines of Java code:

```Java
    ...

    public static String MoveAction = "com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER";

    private void move(Direction direction) {  
        char c = '-';  

        switch (direction) {  
            case Up:    c = 'k'; break;  
            case Right: c = 'l'; break;  
            case Down:  c = 'j'; break;  
            case Left:  c = 'h'; break;  
        }  

        context  
          .sendBroadcast(  
              new Intent(MoveAction)  
                 .putExtra("move", c)  
        );  
    }  

    private void getMazeInfo() {  
       context  
          .sendBroadcast(  
              new Intent(MoveAction)  
                 .putExtra("get_maze", true)  
        );  
    }

    ...
```

And register a **BroadcastReceiver** using the filter *com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER*:

```java
    ...

    context  
      .registerReceiver(  
          this,  
          new IntentFilter(MoveAction)  
    );

    ...

    @Override  
    public void onReceive(Context context, Intent intent) {
        ...
    }

    ...
```

Starting from the point that we need to exploit a serialization bug I'm going to inspect the **GameState** class which is type-cast of the deserialized object.

### GameState

Keep this in your mind: GameState is a **Serializable** class:

```java
public class GameState implements Serializable {
    private static final long serialVersionUID = 1;
    public String cleanupTag;
    private Context context;
    public int levelsCompleted;
    public int playerX;
    public int playerY;
    public long seed;
    public StateController stateController;

    public GameState(int i, int i2, long j, int i3) {
        this.playerX = i;
        this.playerY = i2;
        this.seed = j;
        this.levelsCompleted = i3;
    }

    public GameState(String str, StateController stateController) {
        this.cleanupTag = str;
        this.stateController = stateController;
    }

    public void initialize(Context context) {
        this.context = context;
        GameState gameState = (GameState) this.stateController.load(context);
        if (gameState != null) {
            this.playerX = gameState.playerX;
            this.playerY = gameState.playerY;
            this.seed = gameState.seed;
            this.levelsCompleted = gameState.levelsCompleted;
        }
    }

    public void finalize() {
        if (GameManager.levelsCompleted > 2 && this.context != null) {
            this.stateController.save(this.context, this);
        }
    }
}
```

So the **initialize** method  calls the **load** one of its own **stateController** property.

### Java 101

Have you ever heard about abstract class and inheritance? If yes you can skip this section.

An abstract class can't be initialized, just inherited. Inheritance is commonly used (as well as interfaces) in [OOP](https://en.wikipedia.org/wiki/Object-oriented_programming). This allows to extend objects and/or change the behavior of overridden methods.

I attended a Java course last year and I still remember the example my prof did:

```Java
public abstract class Shape {

    protected int base, height;

    public Shape(int base, int height) {
        this.base = base;
        this.height = height;
    }

    public int area() { return -1; }
    public int perimeter() { return -1; }

}

public class Square extends Shape {

    public Square(int side) {
        super(side, side);
    }

    public int area() {
        // or Math.Pow(...)
        return base * height;
    }

    public int perimeter() {
        return base * 4;
    }

    public double diagonal() {  
        return base * 1.41;  
    }

}

public class Rectangle extends Shape {

    public Rectangle(int base, int height) {
        super(base, height);
    }

    public int area() {
        // or Math.Pow(...)
        return base * height;
    }

    public int perimeter() {
        return (base + height) * 2;
    }

    public double diagonal() {
        return Math.sqrt(base * base + height * height);
    }

}
```

```java
    ...
    Square square  = new Square(1337);
    Rectangle rect = new Rectangle(13, 37);

    r.diagonal(); // returns 39.21
    r.area(); // returns 481

    Shape shape = (Shape)rect;

    // shape.diagonal() is not accessible
    shape.area() // returns 481 - the rectangle area - and not -1

    ...
```

What happens here?! We have a **Shape** class which can't be initialized but their children can be (**Square**, **Rectangle**).  **Square** and **Rectangle** extends their parent with a method named **diagonal**. This method is accessible only when we're calling it from a **Square** or **Rectangle** type var. The following line:

```java
    Shape shape = (Shape)rect;
```

is called **Upcasting**. We lose **Rectangle** extended methods but we can still call **Shape** ones (area and perimeter).

### StateController, StateLoader and BroadcastAnnouncer

StateController is inherited by **StateLoader** and **BroadcastAnnouncer**. Since both classes are **Serializable**, we can forge a **GameState** object and pass it through **cereal** extra. The crafted **GameState** can have both, a **StateLoader** or a **BroadcastAnnouncer**, as **stateLoader** property. If you carefully read the above section you should know that it doesn't matter. Both classes implement (and override) **load** method.

**StateLoader** is useless since it just reads and writes locally. We need a class that reads a file locally and sends its content to a remote location.

### BroadcastAnnouncer

BroadcastAnnouncer has everything we need:

```java
public class BroadcastAnnouncer extends StateController implements Serializable {
    private static final long serialVersionUID = 1;
    private String destUrl;
    private String stringRef;
    private String stringVal;

    public BroadcastAnnouncer(String str, String str2, String str3) {
        super(str);
        this.stringRef = str2;
        this.destUrl = str3;
    }

    public void save(Context context, Object obj) {
        new Thread() {
            public void run() {
                HttpURLConnection httpURLConnection;
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(BroadcastAnnouncer.this.destUrl);
                    stringBuilder.append("/announce?val=");
                    stringBuilder.append(BroadcastAnnouncer.this.stringVal);
                    httpURLConnection = (HttpURLConnection) new URL(stringBuilder.toString()).openConnection();
                    new BufferedInputStream(httpURLConnection.getInputStream()).read();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    httpURLConnection.disconnect();
                }
            }
        }.start();
    }

    public Object load(Context context) {
        this.stringVal = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(this.stringRef)));
            while (true) {
                context = bufferedReader.readLine();
                if (context == null) {
                    break;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(this.stringVal);
                stringBuilder.append(context);
                this.stringVal = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return null;
    }

    public void setStringRef(String str) {
        this.stringRef = str;
    }

    public String getStringRef() {
        return this.stringRef;
    }
}
```

If we forge a **BroadcastAnnouncer** object with **stringRef** property set to */data/local/tmp/challenge4* (our flag path) and **destUrl** set to a controlled domain or remote http listener the **load** method will read its content and keep it into **stringVal** property while the **save** one will send it to **destUrl/announce?val=**.

As said, the method that sends **stringVal** over the network is the **save** one. We need to spot the code that fires it.

### A step back

Let's check again the **GameState** class:

```java
    ...

    public void finalize() {
        if (GameManager.levelsCompleted > 2 && this.context != null) {
            this.stateController.save(this.context, this);
        }
    }

    ...
```

The **finalize** method is called by [Garbage Collector](https://en.wikipedia.org/wiki/Garbage_collection_%28computer_science%29) when an object is no more referenced from another one. Since the **initialize** method is called on the deserialized **cereal** object but the resulting **GameState**   object is not referenced by (or saved in) any other var/property the GC should call the **finalize** method honoring its own task.

As said **it should** (and it will) but you don't know when. From the [java doc](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#finalize%28%29) :

> The general contract of `finalize` is that it is invoked if and when the JavaTM virtual machine has determined that there is no longer any means by which this object can be accessed by any thread that has not yet died, except as a result of an action taken by the finalization of some other object or class which is ready to be finalized.

Not a big deal nevertheless. We can send the crafted serialized object multiple times forcing the JVM to run the GC - sooner or later the garbage will invoke that method -.

### Mission accomplished... not yet!

To allow **save** method to be called at least 3 levels (`GameManager.levelsCompleted > 2`) must be completed.

To note how completely wrong you'll be by thinking that you've control of **levelsCompleted** property too:

```java
    public void initialize(Context context) {
        this.context = context;
        GameState gameState = (GameState) this.stateController.load(context);
        if (gameState != null) {
            this.playerX = gameState.playerX;
            this.playerY = gameState.playerY;
            this.seed = gameState.seed;
            this.levelsCompleted = gameState.levelsCompleted;
        }
    }
```

Even if **gameState** has **levelsCompleted** property, its value is assigned to **this.levelsCompleted** property instance an not to **GameManager** one.

We have to automate this step by using broadcast messages that give us info about the maze and let us move the player. We need a maze solver.

Googling a bit I ended [here](http://www.baeldung.com/java-solve-maze). It seems that most used (and simple) algorithms to solve mazes are:

- depth-first search (DFS);
- breadth-first search (BFS).

I was going to code a basic maze solver using one of the above algorithms when I noticed an interesting thing.

### A logical issue

Every time a level is completed, the next one is randomly generated using a **seed**.

```java
public class GameManager extends SimpleOnGestureListener {

    ...

    private long seed;

    private void create(long j) {
        int i = (levelsCompleted + 1) * 5;
        System.gc();
        this.drawables.clear();
        this.maze = new Maze(i, j);
        this.drawables.add(this.maze);
        this.exit = new Exit(i, this.maze.getEnd());
        this.drawables.add(this.exit);
        this.player = new Player(this.maze.getStart(), i);
        this.drawables.add(this.player);
    }

    public boolean movePlayer(Point point) {
        ...

        if (this.exit.getPoint().equals(this.player.getPoint()) != null) {
            levelsCompleted += 1;
            create(this.seed);
            this.broadcastAnnouncer.save(view.getContext(), this);
            this.loader.save(view.getContext(), new GameState(this.player.getX(), this.player.getY(), this.seed, levelsCompleted));
        }
        view.invalidate();
        return z;
    }

}
```

Probably, while coding, someone forgot to assign/reassign **seed** value once the level has been completed. Thanks to this little error mazes don't change (except for 1st level).

![](https://i.imgur.com/l1eq6Hh.png)

### Hardcode is the way

To save time I've hardcoded each possible solution (totally 4) into the final app that you can find in my repo.

https://github.com/luc10/h1-702-2018-ctf-wu/tree/master/challenge-4/

## 500 Internal Server Error

Unfortunately, setting up a web server to validate solutions is not so easy as solving this challenge. **I must thank [Christopher Thompson](https://github.com/breadchris) (the dev behind all challenges) for his patience**. After different attempts to make the remote server more reliable and setting up a local one to try my exploit forgot to set the right permissions to the flag file (all I got were empty requests from his IP) and was finally forced to give me the flag which is:

`flag{my_favorite_cereal_and_mazes}`
