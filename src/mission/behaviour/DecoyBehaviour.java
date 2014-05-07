package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;
import phys.Point2D;

/** @Deprectated -- caused bugs and has been replaced by bullet reflection */
public class DecoyBehaviour extends StaticPositionBehaviour {

	Point2D p;
	
	public DecoyBehaviour(Point2D position, float waitTime) {
		super(position, waitTime);
		p = new Point2D();
	}
	
	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		super.tick(dt, board, mech);
		
		for(Mech e: board.getEnemies()) {
			
			if(e.isBase()) continue;
			
			p.x = position.x - e.getPos().x;
			p.y = position.y - e.getPos().y;
			
			if(Point2D.magnitude(p) > Mech.MECH_RADIUS*2) {
				e.getDirection().x = p.x;
				e.getDirection().y = p.y;
				e.setSpeed(0.1f);
			} else {
				e.setSpeed(0f);
			}
		}
	}
}
