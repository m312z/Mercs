package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import mission.weapon.Weapon;
import phys.Point2D;

public class LaserStarBehaviour implements MechBehaviour {

	float dir;
	
	public LaserStarBehaviour(float dir) {
		this.dir = dir;
	}
	
	@Override
	public void tick(float dt, Board board, Mech mech) {
		// move
		mech.getDirection().x = 0;
		mech.getDirection().y = Board.BOARD_SPEED;
		mech.setSpeed(Board.BOARD_SPEED);
		
		// shoot
		Weapon w = mech.getComponents().get(0).getWeapon();
		if(w!=null)
		for(Point2D target: w.getLaserTargets())
			target.rotate(dt*dir);
		mech.getAttackCycle().tick(dt, board);
	}
	
}
