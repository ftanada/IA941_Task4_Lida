package main.org.xguzm.pathfinding.grid.heuristics;

import main.org.xguzm.pathfinding.Heuristic;
import main.org.xguzm.pathfinding.NavigationNode;
import main.org.xguzm.pathfinding.grid.GridCell;

public class ManhattanDistance implements Heuristic {

	@Override
	public float calculate(NavigationNode from, NavigationNode to) {
		GridCell c1 = (GridCell)from, c2 = (GridCell) to;
		
		return calculate(Math.abs(c2.x - c1.x), Math.abs(c2.y - c1.y));
	}
	
	
	public float calculate(float deltaX, float deltaY){
		return deltaX + deltaY;
	}

}
