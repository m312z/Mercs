package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;

public class BaseBehaviour implements MechBehaviour{

	static BaseBehaviour singleton;
	
	public static BaseBehaviour getInstance() {
		if(singleton==null)
			singleton = new BaseBehaviour();
		return singleton;
	}
	
	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		// map move
		mech.getDirection().x = 0;
		mech.getDirection().y = Board.BOARD_SPEED;
		mech.setSpeed(Board.BOARD_SPEED);
		
		// shoot cycle
		mech.getAttackCycle().tick(dt, board);
	}
}
