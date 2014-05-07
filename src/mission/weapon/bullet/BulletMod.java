package mission.weapon.bullet;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.gameobject.Component;
import mission.gameobject.GameObject;
import mission.gameobject.Mech;
import phys.Point2D;

public enum BulletMod {

	CLUSTER(0) {
		@Override
		public void bulletTick(float dt, Board board, Bullet b) {
			if(b.getFireTime() > Bullet.BULLET_RANGE/2f) {
				for(int i=0;i<b.getMods().get(CLUSTER);i++) {
					Bullet clone = b.clone();
					clone.getDirection().x = (float) Math.cos(Math.toRadians(i*(360.0/b.getMods().get(CLUSTER))));
					clone.getDirection().y = (float) Math.sin(Math.toRadians(i*(360.0/b.getMods().get(CLUSTER))));
					clone.getMods().remove(CLUSTER);
					clone.setSpeed(Bullet.BULLET_SPEED);
					board.addBullet(clone);
				}
				b.setDead(true);
			}
		}
		@Override
		public void bulletHit(Board board, Bullet b, GameObject target) {}
		@Override
		public void bulletHit(Board board, Mech shooter, Bullet b,Mech target, Component component) {}
	},
	MEGA(1) {
		@Override
		public void bulletTick(float dt, Board board, Bullet b) {
			float d  = (float) (Math.sin(Math.toRadians(b.getFireTime()*20))+2)*2;
			Point2D[] p = b.getShape().getPoints();
			for(int i=0;i<p.length;i++) {
				Point2D.normalise(p[i]);
				p[i].x = p[i].x*d*Bullet.BULLET_RADIUS;
				p[i].y = p[i].y*d*Bullet.BULLET_RADIUS;
			}
		}
		@Override
		public void bulletHit(Board board, Bullet b, GameObject target) {}
		@Override
		public void bulletHit(Board board, Mech shooter, Bullet b,Mech target, Component component) {
			target.damage(20, component, board);
		}
	},
	GARGANTUA(2) {
		@Override
		public void bulletTick(float dt, Board board, Bullet b) {
			float scale = 1 + dt/20f;
			Point2D[] p = b.getShape().getPoints();
			float mag = 1;
			for(int i=0;i<p.length;i++) {
				mag = Point2D.magnitude(p[i]);
				if(mag>0 && mag < Bullet.BULLET_RADIUS*10) {
					p[i].x = p[i].x*scale;
					p[i].y = p[i].y*scale;
				}
			}
		}
		
		@Override
		public void bulletHit(Board board, Bullet b, GameObject target) {}
		@Override
		public void bulletHit(Board board, Mech shooter, Bullet b, Mech target, Component component) {
			float mag = Point2D.magnitude(b.getShape().getPoints()[0])/Bullet.BULLET_RADIUS;
			target.damage(mag/2, component, board);
		}
	},
	HOMING(3) {
		@Override
		public void bulletTick(float dt, Board board, Bullet b) {
			// find closest enemy
			float min = Float.MAX_VALUE;
			Mech target = null;
			Point2D p = new Point2D();
			List<Mech> targetList = new ArrayList<Mech>();
			if(b.isEnemyBullet()) {
				targetList.addAll(board.getPlayers());
				targetList.addAll(board.getMechs());
			} else {
				targetList.addAll(board.getEnemies());
			}
			for(Mech e: targetList) {
				p.x = (b.getPos().x-e.getPos().x);
				p.y = (b.getPos().y-e.getPos().y);
				if(Point2D.magnitude(p) < min) {
					min = Point2D.magnitude(p);
					target = e;
				}
			}
			// move towards closest enemy
			if(target!=null) {
				p.x = (b.getPos().x-target.getPos().x);
				p.y = (b.getPos().y-target.getPos().y);
				Point2D.normalise(p);
				Point2D.normalise(b.getDirection());
				b.getDirection().x *= 15;
				b.getDirection().y *= 15;
				b.getDirection().x = b.getDirection().x - p.x;
				b.getDirection().y = b.getDirection().y - p.y;
			}
		}
		@Override
		public void bulletHit(Board board, Bullet b, GameObject target) {}
		@Override
		public void bulletHit(Board board, Mech shooter, Bullet b,Mech target, Component component) {}
	},
	MINE(4) {
		@Override
		public void bulletTick(float dt, Board board, Bullet b) {
			// slow down
			b.setSpeed(b.getSpeed() - dt*0.02f);
			if(b.getSpeed() < 0.01f)
				b.setSpeed(0);
		}
		@Override
		public void bulletHit(Board board, Bullet b, GameObject target) {}
		@Override
		public void bulletHit(Board board, Mech shooter, Bullet b, Mech target, Component component) {}
	},
	FIRE(5) {
		@Override
		public void bulletTick(float dt, Board board, Bullet b) {}
		@Override
		public void bulletHit(Board board, Bullet b, GameObject target) {}
		@Override
		public void bulletHit(Board board, Mech shooter, Bullet b,Mech target, Component component) {
			component.setBurnTime(1800);
		}
	},
	SHIELDCHARGE(6) {
		@Override
		public void bulletTick(float dt, Board board, Bullet b) {
			// add smoke trail
		}
		@Override
		public void bulletHit(Board board, Bullet b, GameObject target) {}
		@Override
		public void bulletHit(Board board, Mech shooter, Bullet b,Mech target, Component component) {
			// recharge some shield
			if(shooter.getShield()!=null)
				shooter.getShield().setCurrentCapacity(shooter.getShield().getCurrentCapacity()+1/2f);
		}
	};
	
	public int id;
	
	private BulletMod(int id) {
		this.id = id;
	}
	
	public abstract void bulletTick(float dt, Board board, Bullet b);
	public abstract void bulletHit(Board board, Bullet b, GameObject target);
	public abstract void bulletHit(Board board, Mech shooter, Bullet b, Mech target, Component component);
}
