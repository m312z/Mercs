package mission.weapon;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.effects.Laser;
import mission.gameobject.Asteroid;
import mission.gameobject.Enemy;
import mission.gameobject.GameObject;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.powers.Power;
import mission.powers.PowerEater.ActivePower;
import mission.weapon.bullet.Bullet;
import phys.BulletPhysics;
import phys.Point2D;

public class SelfDefenceLaser {
	
	static List<GameObject> hitObjects = new ArrayList<GameObject>();
	
	public static void fire(float dt, Board board, Point2D origin, Player player) {
		
		Enemy e = null;

		float min = Float.MAX_VALUE;
		Point2D p = new Point2D();
		for(Enemy target: board.getEnemies()) {
			if(hitObjects.contains(target))
				continue;
			p.x = (origin.x-target.getPos().x);
			p.y = (origin.y-target.getPos().y);
			if(Point2D.magnitude(p) < min && Point2D.magnitude(p) < Mech.MECH_RADIUS*5) {
				min = Point2D.magnitude(p);
				e = target;
			}
		}
		
		boolean targetbullets = false;
		for(ActivePower ap: player.getPowerEater().getActivePowers())
			if(ap.type==Power.LASER_MICROTARGETING)
				targetbullets = true;
		
		Bullet b = null;
		if(targetbullets) {
			for(Bullet target: board.getEnemyBullets()) {
				if(hitObjects.contains(target))
					continue;
				p.x = (origin.x-target.getPos().x);
				p.y = (origin.y-target.getPos().y);
				if(Point2D.magnitude(p) < min && Point2D.magnitude(p) < Mech.MECH_RADIUS*5) {
					min = Point2D.magnitude(p);
					b = target;
				}
			}
		}
		
		boolean targetAsteroids = false;
		for(ActivePower ap: player.getPowerEater().getActivePowers())
			if(ap.type==Power.LASER_ASTEROIDTARGETING)
				targetAsteroids = true;
		
		Asteroid a = null;
		if(targetAsteroids) {
			for(Asteroid target: board.getAsteroids()) {
				if(hitObjects.contains(target))
					continue;
				p.x = (origin.x-target.getPos().x);
				p.y = (origin.y-target.getPos().y);
				if(Point2D.magnitude(p) < min && Point2D.magnitude(p) < Mech.MECH_RADIUS*5) {
					min = Point2D.magnitude(p);
					a = target;
				}
			}
		}
		
		if(a!=null) {
			// direction towards enemy
			p.x = (a.getPos().x-origin.x);
			p.y = (a.getPos().y-origin.y);
			Point2D.normalise(p);
			
			// find closest component, and ray distance
			List<Asteroid> list = new ArrayList<Asteroid>(1);
			list.add(a);
			float mag = BulletPhysics.getFirstRayTarget(player.getPos(),p,list);
			
			// damage
			a.breakAsteroid(board,player.getPlayerNumber());
			p.x = origin.x + p.x*mag;
			p.y = origin.y + p.y*mag;
			board.addLaser(new Laser(origin,p,2f,Laser.LASER_WIDTH));
			hitObjects.add(a);
		} else if(b!=null) {
			b.setDead(true);
			board.addLaser(new Laser(origin,b.getPos(),2f,Laser.LASER_WIDTH));
			hitObjects.add(b);
		} else if (e!=null) {
			// direction towards enemy
			p.x = (e.getPos().x-origin.x);
			p.y = (e.getPos().y-origin.y);
			Point2D.normalise(p);
			
			// find closest component, and ray distance
			List<Enemy> list = new ArrayList<Enemy>(1);
			list.add(e);
			float mag = BulletPhysics.getFirstRayTarget(origin,p,list);
						
			// damage
			e.damage(1,BulletPhysics.getLastComponentCollision(), board);
			board.getCounter().addDamageDealt(player.getId(),1);
			p.x = origin.x + p.x*mag;
			p.y = origin.y + p.y*mag;
			board.addLaser(new Laser(player.getPos(),p,2f,Laser.LASER_WIDTH));
			
			hitObjects.add(e);
		}
	}
	
	public static List<GameObject> getHitObjects() {
		return hitObjects;
	}
}
