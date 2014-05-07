package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

public class PathBehaviour implements MechBehaviour {
	
	Point2D[] direction;
	float turn = 0;
	float speed;
	
	public PathBehaviour(Point2D[] direction) {
		speed = Mech.MECH_SPEED;
		this.direction = direction;
	}
	
	public Point2D[] getDirection() {
		return direction;
	}
	
	public void setDirection(Point2D[] direction) {
		this.direction = direction;
	}
	
	public float getTurn() {
		return turn;
	}
	
	public void setTurn(float turn) {
		this.turn = turn;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		turn += dt/240f;
		
		if(turn <= 0) {
			mech.getDirection().x = direction[0].x;
			mech.getDirection().y = direction[0].y;
		} else if(turn >= direction.length-1) {
			mech.getDirection().x = direction[direction.length-1].x;
			mech.getDirection().y = direction[direction.length-1].y;
		} else {
			mech.getDirection().x = direction[(int)turn].x*(1-turn%1) + direction[(int)turn+1].x*(turn%1);
			mech.getDirection().y = direction[(int)turn].y*(1-turn%1) + direction[(int)turn+1].y*(turn%1);
		}
		
		mech.setSpeed(speed);
		
		// shoot cycle
		mech.getAttackCycle().tick(dt, board);
	}
}
