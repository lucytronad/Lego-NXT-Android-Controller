package com.example.lo52_project;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Lois Aubree & Lucie Boutou
 *
 */
public class PiecePathFinder extends AStar<Piece> {

	public Piece[][] mWorld;
	
	/**
	 * @constructor
	 * @param world map
	 */
	public PiecePathFinder(Piece[][] map)
	{
		mWorld = map;
	}
	@Override
	protected boolean isGoal(Piece node) {
		return node.isGoal();
	}

	/**
	 * Cost for the operation to go to <code>to</code> from
	 * <code>from</from>.
	 *
	 * @param from The piece we are leaving.
	 * @param to The piece we are reaching.
	 * @return The cost of the operation.
	 */
	@Override
	protected Double g(Piece from, Piece to) {
		if(from.wall_config[1] == Piece.MUR_OFF && to.wall_config[3] == Piece.MUR_OFF)
			return 1.0;
		if(from.wall_config[2] == Piece.MUR_OFF && to.wall_config[4] == Piece.MUR_OFF)
			return 1.0;
		if(from.wall_config[3] == Piece.MUR_OFF && to.wall_config[1] == Piece.MUR_OFF)
			return 1.0;
		if(from.wall_config[4] == Piece.MUR_OFF && to.wall_config[2] == Piece.MUR_OFF)
			return 1.0;
		if(from.wall_config[1] == Piece.MUR_ON || to.wall_config[3] == Piece.MUR_ON)
			return 0.0;
		if(from.wall_config[2] == Piece.MUR_ON || to.wall_config[4] == Piece.MUR_ON)
			return 0.0;
		if(from.wall_config[3] == Piece.MUR_ON || to.wall_config[1] == Piece.MUR_ON)
			return 0.0;
		if(from.wall_config[5] == Piece.MUR_ON || to.wall_config[3] == Piece.MUR_ON)
			return 0.0;
		return Double.MAX_VALUE;
	}

	/**
	 * Estimated cost to reach a goal piece using the Manhattan distance .
	 *
	 * @param from The node we are leaving.
	 * @param to The node we are reaching.
	 * @return The estimated cost to reach an object.
	 */
	@Override
	protected Double h(Piece from, Piece to) {
		return new Double(Math.abs(mWorld[0].length - 1 - to.getIndX()) + Math.abs(mWorld.length - 1 - to.getIndY()));
	}

	/**
	 * Generate the successors for a given piece.
	 *
	 * @param piece The piece we want to expand.
	 * @return A list of possible next steps.
	 */
	@Override
	protected List<Piece> generateSuccessors(Piece node) {
		List<Piece> ret = new LinkedList<Piece>();
		int x = node.getIndX();
		int y = node.getIndY();
		if(x < mWorld.length - 1 && mWorld[x+1][y].wall_config[4] == Piece.MUR_OFF  && node.wall_config[2] == Piece.MUR_OFF )
				ret.add(mWorld[x+1][y]);

		if(y < mWorld[0].length - 1 && mWorld[x][y+1].wall_config[1] == Piece.MUR_OFF  && node.wall_config[3] == Piece.MUR_OFF )
				ret.add(mWorld[x][y+1]);
		
		if(x > 0  && mWorld[x-1][y].wall_config[2] == Piece.MUR_OFF  && node.wall_config[4] == Piece.MUR_OFF )
			ret.add(mWorld[x-1][y]);
		
		if(y > 0 && mWorld[x][y-1].wall_config[3] == Piece.MUR_OFF  && node.wall_config[1] == Piece.MUR_OFF )
			ret.add(mWorld[x][y-1]);

		return ret;
	}

}
