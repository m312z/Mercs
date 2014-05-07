package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

public class FollowBehaviour implements MechBehaviour {

	float distance;
	float speed;
	Mech target;
	
	public FollowBehaviour(Mech target, float speed, float distance) {
		this.target = target;
		this.speed = speed;
		this.distance = distance;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		// shoot cycle
		mech.getAttackCycle().tick(dt, board);
		
		mech.getDirection().x = target.getPos().x - mech.getPos().x;
		mech.getDirection().y = target.getPos().y - mech.getPos().y;
		
		if(Point2D.magnitude(mech.getDirection()) < distance)
			mech.setSpeed(0);
		else
			mech.setSpeed(speed);
		
		if((mech.getMaxX()+mech.getPos().x > Board.BOARD_SIZE && mech.getDirection().x > 0)
			|| (mech.getMinX()+mech.getPos().x <0 && mech.getDirection().x < 0))
			mech.getDirection().x = mech.getDirection().x*-1;
		if((mech.getMaxY()+mech.getPos().y > Board.BOARD_SIZE && mech.getDirection().y > 0)
			|| (mech.getMinY()+mech.getPos().y < 0 && mech.getDirection().y < 0))
			mech.getDirection().y = mech.getDirection().y*-1;
	}
}