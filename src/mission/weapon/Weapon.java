package mission.weapon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mission.Board;
import mission.SupportFactory;
import mission.effects.EnemyLaser;
import mission.effects.Laser;
import mission.gameobject.Asteroid;
import mission.gameobject.Component;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.weapon.bullet.Bullet;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class Weapon {
	
	public static final Weapon spongeGun = new Weapon(null,1f,ShotType.PAUSE);
	
	public enum ShotType {
		PAUSE,
		STRAIGHTDUAL,
		SPREAD,
		SPIRAL,
		FASTSPIRAL,
		STRAIGHT,
		
		// enemy only
		TARGETEDBEAM,
		LASER,
		ASTEROID,
		DIRECTEDBEAM,
		
		// player only
		IONBEAM,
		CLONERAY;
	}
	
	/* progenitor bullet */
	protected Bullet bullet;
	protected ShotType shotType;
	
	/* gun attributes */
	protected float fireDelay;
	protected float fireTimer = 0;
	protected float volleyTimer = 0;
	protected Point2D aim;
	protected int offset;
	
	/* specific gun things */
	boolean hasTarget = false;
	boolean holding = false;
	Set<Mech> beamTargets;
	List<Point2D> laserTargets;
	
	public Weapon(Bullet bullet, float fireDelay, ShotType shotType) {
		beamTargets = new HashSet<Mech>(0);
		laserTargets = new ArrayList<Point2D>(0);
		this.bullet = bullet;
		this.fireDelay = fireDelay;
		this.shotType = shotType;
		aim = new Point2D(0,-1);
		offset = 1;
	}
	
	public boolean isReadytoShoot() {
		return fireTimer >= fireDelay;
	}

	public void shoot(Mech shooter, Component component, Point2D pos, Board board) {
		
		fireTimer = 0;
		
		Bullet b;

		switch(shotType) {
		case STRAIGHT:
			b = bullet.clone();
			b.getDirection().x = 0;
			b.getPos().x = pos.x;
			b.getDirection().y = -1;
			if(b.isEnemyBullet())
				b.getDirection().y = 1;
			b.getPos().y = pos.y + b.getDirection().y*(6/8f)*Mech.MECH_RADIUS;
			board.addBullet(b);
			break;
		case STRAIGHTDUAL:
			for(int i=0;i<2;i++) {
				b = bullet.clone();
				
				b.getDirection().x = 0;
				if(b.isEnemyBullet())
					b.getDirection().y = 1;
				else b.getDirection().y = -1;
				
				b.getPos().x = pos.x + ((float)(Math.cos(Math.toRadians(60)))*Mech.MECH_RADIUS - Bullet.BULLET_RADIUS)*(i*2-1);
				b.getPos().y = pos.y + b.getDirection().y*(float)(Math.sin(Math.toRadians(60))*Mech.MECH_RADIUS);
				
				board.addBullet(b);
			}
			break;
		case SPREAD:
			for(int i=0;i<4;i++) {
				b = bullet.clone();
				
				b.getPos().x = pos.x + (6/8f)*Mech.MECH_RADIUS*(float)Math.cos(Math.toRadians(i*30-135));
				b.getDirection().x = (float) Math.cos(Math.toRadians(i*30-135));
				
				if(b.isEnemyBullet()) {
					b.getDirection().y = (float) -Math.sin(Math.toRadians(i*30-135));
					b.getPos().y = pos.y - (6/8f)*Mech.MECH_RADIUS*(float) Math.sin(Math.toRadians(i*30-135));
				} else {
					b.getDirection().y = (float) Math.sin(Math.toRadians(i*30-135));
					b.getPos().y = pos.y + (6/8f)*Mech.MECH_RADIUS*(float) Math.sin(Math.toRadians(i*30-135));
				}
				
				board.addBullet(b);
			}
			break;
		case SPIRAL:
			for(int i=0;i<8;i++) {
				b = bullet.clone();
				b.getPos().x = pos.x + (5/8f)*Mech.MECH_RADIUS*(float)(Math.cos(Math.toRadians(i*360.0/8+volleyTimer*(3/8f))));
				b.getPos().y = pos.y + (5/8f)*Mech.MECH_RADIUS*(float)(Math.sin(Math.toRadians(i*360.0/8+volleyTimer*(3/8f))));
				b.getDirection().x = (float) Math.cos(Math.toRadians(i*360.0/8+volleyTimer*(3/8f)));
				b.getDirection().y = (float) Math.sin(Math.toRadians(i*360.0/8+volleyTimer*(3/8f)));
				board.addBullet(b);
			}
			break;
		case FASTSPIRAL:
			for(int i=0;i<8;i++) {
				b = bullet.clone();
				b.getPos().x = pos.x + (5/8f)*Mech.MECH_RADIUS*(float) Math.cos(Math.toRadians(i*360.0/8+volleyTimer*1.0));
				b.getPos().y = pos.y + (5/8f)*Mech.MECH_RADIUS*(float) Math.sin(Math.toRadians(i*360.0/8+volleyTimer*1.0));
				b.getDirection().x = (float) Math.cos(Math.toRadians(i*360.0/8+volleyTimer*1.0));
				b.getDirection().y = (float) Math.sin(Math.toRadians(i*360.0/8+volleyTimer*1.0));
				board.addBullet(b);
			}
			break;
		case ASTEROID:
			if(getBullet()!=null && bullet.isEnemyBullet())
				for(int i=0;i<10;i++) {
					Asteroid a = new Asteroid(Shape.scale(DefaultShapes.basicHex, Asteroid.ASTEROID_RADIUS*5),
							new Point2D(i*(Board.BOARD_SIZE-Asteroid.ASTEROID_RADIUS*10)/9f + Asteroid.ASTEROID_RADIUS*5,
									-Asteroid.ASTEROID_RADIUS*5),
							new Point2D(0,1f), (float) (Math.random()*0.2+0.2), 4);
					board.addAsteroid(a);
				}
			break;
		case CLONERAY:
			if(shooter instanceof Player) {
				Player p = (Player)shooter;
				if(SupportFactory.cloneEnemy(p, p.getPos(), board))
					p.getComponents().get(0).getWeapon().setShotType(p.getOriginalShotType());
			}
			break;
		case TARGETEDBEAM:
			// fire at closest enemy
			if(hasTarget) {
				b = bullet.clone();
				b.getDirection().x = aim.x;
				b.getDirection().y = aim.y;
				b.getPos().x = pos.x + aim.x*(6/8f)*Mech.MECH_RADIUS;
				b.getPos().y = pos.y + aim.y*(6/8f)*Mech.MECH_RADIUS;				
				board.addBullet(b);
			}
			break;
		case DIRECTEDBEAM:
			// fire at target
			b = bullet.clone();
			b.getDirection().x = aim.x;
			b.getDirection().y = aim.y;
			b.getPos().x = pos.x + aim.x*(6/8f)*Mech.MECH_RADIUS;
			b.getPos().y = pos.y + aim.y*(6/8f)*Mech.MECH_RADIUS;				
			board.addBullet(b);
			break;
		case LASER:
			// fire the lasers!
			for(Point2D laserTarget: laserTargets) {
				EnemyLaser el = new EnemyLaser(
						shooter,
						component, 
						laserTarget,
						fireDelay, 
						Laser.LASER_WIDTH);
				el.doubleEnded = ((Mech)shooter).isBase();
				if(!bullet.isEnemyBullet()) {
					el.r = 0;
					if(shooter.getId()==0)
						el.b = 1;
					else el.g = 1;
				}
				board.getEnemyLasers().add(el);
			}
			break;
		case IONBEAM:
		case PAUSE:
		default:
			break;
		}
	}

	public void tick(float dt, Mech shooter, Point2D pos, Board board) {
		
		if(fireTimer<fireDelay)
			fireTimer += dt;
		volleyTimer += dt;
		if(volleyTimer >= 720)
			volleyTimer = 0;
		
		switch(shotType) {
		case TARGETEDBEAM:
			// find closest enemy
			float min = Float.MAX_VALUE;
			Mech target = null;
			Point2D p = new Point2D();
			List<Mech> targetList = new ArrayList<Mech>();
			if(bullet.isEnemyBullet()) {
				targetList.addAll(board.getPlayers());
				targetList.addAll(board.getMechs());
			} else {
				targetList.addAll(board.getEnemies());
			}
			for(Mech e: targetList) {
				if(!bullet.isEnemyBullet() && ((Mech)e).isBase())
					continue;
				p.x = (pos.x-e.getPos().x);
				p.y = (pos.x-e.getPos().y);
				if(Point2D.magnitude(p) < min) {
					min = Point2D.magnitude(p);
					target = e;
				}
			}
			// target closest enemy
			if(target!=null) {
				hasTarget = true;
				aim.x = target.getPos().x - pos.x;
				aim.y = target.getPos().y - pos.y;
				Point2D.normalise(aim);
			} else {
				hasTarget = false;
			}
			break;
		case IONBEAM:
			if(holding && beamTargets.size()<12 && (volleyTimer%6)<1) {
				// find targets
				target = null;
				p = new Point2D();
				min = Float.MAX_VALUE;
				for(Mech m: board.getEnemies()) {
					if(beamTargets.contains(m))
						continue;
					p.x = m.getPos().x - shooter.getPos().x;
					p.y = m.getPos().y - shooter.getPos().y;
					float d = Point2D.magnitude(p);
					if(d<min) {
						min = d;
						target = m;
					}
				}
				if(target!=null)
					beamTargets.add(target);
			} else if(!holding && beamTargets.size()>0) {
				// release
				for(Mech m: beamTargets) {
					if(!m.isDead() && m.getComponents().size()>0) {
						Laser l = new Laser(pos,m.getPos(),20,Laser.LASER_WIDTH);
						board.addLaser(l);
						m.damage(10f,m.getComponents().get(0), board);
						board.getCounter().addDamageDealt(shooter.getId(),10);
					}
				}
				beamTargets.clear();
				((Player)shooter).getComponents().get(0).getWeapon().setShotType(((Player)shooter).getOriginalShotType());
			}
			break;
		default:
			break;
		}
	}
	
	public List<Point2D> getLaserTargets() {
		return laserTargets;
	}
	
	public void setHolding(boolean holding) {
		this.holding = holding;
	}
	
	public boolean isHolding() {
		return holding;
	}
	
	public Set<Mech> getBeamTargets() {
		return beamTargets;
	}
	
	public void setFireDelay(float fireDelay) {
		this.fireDelay = fireDelay;
	}
	
	public float getFireDelay() {
		return fireDelay;
	}
	
	public void setFireTimer(float fireTimer) {
		this.fireTimer = fireTimer;
	}
	
	public float getVolleyTimer() {
		return volleyTimer;
	}
	
	public void setVolleyTimer(float volleyTimer) {
		this.volleyTimer = volleyTimer;
	}
	
	public ShotType getShotType() {
		return shotType;
	}
	
	public void setShotType(ShotType shotType) {
		this.shotType = shotType;
	}
	
	public Point2D getAim() {
		return aim;
	}
	
	public Bullet getBullet() {
		return bullet;
	}
}
