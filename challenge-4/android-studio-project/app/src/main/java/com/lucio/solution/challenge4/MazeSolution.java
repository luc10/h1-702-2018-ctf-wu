package com.lucio.solution.challenge4;

import java.util.LinkedList;

public class MazeSolution extends LinkedList<Direction> {

    public MazeSolution(Direction ...directions) {
        super();

        for (Direction direction: directions) {
            add(direction);
        }
    }

}
