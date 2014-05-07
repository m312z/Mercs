package mission.behaviour;

import java.util.List;

import mission.Board;
import mission.gameobject.AllyMech;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import phys.Point2D;

public class BeeBehaviour implements MechBehaviour {
	
	static BeeBehaviour singleton;
	
	public static BeeBehaviour getInstance() {
		if(singleton == null)
			singleton = new BeeBehaviour();
		return singleton;
	}

	@Override
	public void tick(float dt, Board board, Mech mech) {

		// find closest target
		float min = Float.MAX_VALUE;
		Mech target = null;
		Point2D p = new Point2D();
		
		if(mech.getId()>=0) {
			// find enemy
			for(Enemy enemy: board.getEnemies()) {
				if(!enemy.isOnScreen()) continue;
				p.x = (mech.getPos().x-enemy.getPos().x);
				p.y = (mech.getPos().y-enemy.getPos().y);
				if(Point2D.magnitude(p) < min) {
					min = Point2D.magnitude(p);
					target = enemy;
				}
			}
			// no enemy, return to player
			if(target==null) target = ((AllyMech)mech).getMaster();
		} else {
			// find player
			for(Player player: board.getPlayers()) {
				p.x = (mech.getPos().x-player.getPos().x);
				p.y = (mech.getPos().y-player.getPos().y);
				if(Point2D.magnitude(p) < min) {
					min = Point2D.magnitude(p);
					target = player;
				}
			}
		}
		
		// move towards closest enemy
		if(target!=null) {
			p.x = (target.getPos().x - mech.getPos().x);
			p.y = (target.getPos().y - mech.getPos().y);
			
			float mag = Point2D.magnitude(p);
			Point2D.normalise(p);
			
			if(mag < Mech.MECH_RADIUS) {
				mech.setSpeed(0);
			} else {
				Point2D.normalise(mech.getDirection());
				mech.getDirection().x *= 15;
				mech.getDirection().y *= 15;
				mech.getDirection().x = mech.getDirection().x + p.x;
				mech.getDirection().y = mech.getDirection().y + p.y;
				mech.setSpeed(Mech.MECH_SPEED);	
			}
			
			// target laser
			List<Point2D> laserTargets = mech.getComponents().get(0).getWeapon().getLaserTargets();
			if(laserTargets.size()!=1) {
				laserTargets.clear();
				laserTargets.add(new Point2D());
			}
			Point2D l = laserTargets.get(0);
			l.x = p.x * Mech.MECH_RADIUS*2;
			l.y = p.y * Mech.MECH_RADIUS*2;
			mech.getComponents().get(0).getWeapon().getAim().x = p.x;
			mech.getComponents().get(0).getWeapon().getAim().y = p.y;
		}
		
		// shoot cycle
		mech.getAttackCycle().tick(dt, board);
	}

}
