package mission.effects;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.gameobject.Component;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import phys.BulletPhysics;
import phys.Point2D;

public class EnemyLaser extends Laser {

	Mech parent;
	Component weaponcomponent;
	Point2D target;
	
	static List<Mech> collisionList = new ArrayList<Mech>();
	float damage;
	
	public EnemyLaser(Mech parent, Component component, Point2D target, float duration, float width) {
		super(new Point2D(), new Point2D(), duration, width);
		this.target = target;
		this.weaponcomponent = component;
		this.parent = parent;
		
		start.x = weaponcomponent.getPos().x + parent.getPos().x;
		start.y = weaponcomponent.getPos().y + parent.getPos().y;
		end.x = start.x + target.x;
		end.y = start.y + target.y;
		
		damage = 2;
	}
	
	public void collide(float dt, Board board) {
		
		if(parent.isDead())
			timer = duration;
		
		start.x = weaponcomponent.getPos().x + parent.getPos().x;
		start.y = weaponcomponent.getPos().y + parent.getPos().y;
		end.x = start.x + target.x;
		end.y = start.y + target.y;
		Point2D vel = new Point2D(end.x - start.x,end.y - start.y);

		if(weaponcomponent.getWeapon().getBullet().isEnemyBullet()) {
			for(Player p: board.getPlayers()) {
				collisionList.clear();
				collisionList.add(p);
				float tuc = BulletPhysics.getFirstRayTarget(start,vel,collisionList);
				if(tuc <= 1) {
					p.damage(dt*damage,p.getComponents().get(0),board);
				}
			}
		} else {
			for(Mech m: board.getEnemies()) {
				collisionList.clear();
				collisionList.add(m);
				float tuc = BulletPhysics.getFirstRayTarget(start,vel,collisionList);
				if(tuc <= 1) {
					m.damage(dt*damage,m.getComponents().get(0),board);
				}
			}
		}
	}
}
