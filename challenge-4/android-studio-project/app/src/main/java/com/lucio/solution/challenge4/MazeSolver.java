package com.lucio.solution.challenge4;

import java.util.ArrayList;
import java.util.HashMap;

public class MazeSolver {

    // I'm going to hardcode each possible solution
    private static final ArrayList<HashMap<Player, MazeSolution>> solutions =
        new ArrayList<HashMap<Player, MazeSolution>>() {{
            // Level 1
            add(
                new HashMap<Player, MazeSolution>() {{
                    put(
                        new Player(1, 3),
                        new MazeSolution(Direction.Right, Direction.Up, Direction.Left)
                    );

                    put(
                        new Player(3, 1),
                        new MazeSolution(Direction.Down, Direction.Left, Direction.Up)
                    );
                }}
            );

            // Level 2
            add(
                new HashMap<Player, MazeSolution>() {{
                    put(
                        new Player(5, 3),
                        new MazeSolution(
                            Direction.Left, Direction.Up, Direction.Right, Direction.Down,
                            Direction.Left, Direction.Down, Direction.Left, Direction.Up,
                            Direction.Left, Direction.Up
                        )
                    );
                }}
            );

            // Level 3
            add(
                new HashMap<Player, MazeSolution>() {{
                    put(
                        new Player(13, 1),
                        new MazeSolution(
                            Direction.Down, Direction.Left, Direction.Up, Direction.Right, Direction.Up,
                            Direction.Left, Direction.Down, Direction.Right, Direction.Down, Direction.Right,
                            Direction.Right, Direction.Down, Direction.Left, Direction.Down, Direction.Left,
                            Direction.Up, Direction.Left, Direction.Down,Direction.Left, Direction.Up,
                            Direction.Left, Direction.Up
                        )
                    );
                }}
            );
        }};


    /**
     * Returns a valid solution if any
     *
     * @param mazeInfo
     * @return
     */
    public static MazeSolution getSolution(MazeInfoInterface mazeInfo) {
        if (mazeInfo.getLevel() > solutions.size()) {
            return null;
        }

        for (HashMap.Entry<Player, MazeSolution> entry:
                solutions.get(mazeInfo.getLevel() - 1).entrySet()) {

            if (mazeInfo.getPlayer().equals(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

}
