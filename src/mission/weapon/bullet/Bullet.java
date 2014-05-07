package mission.weapon.bullet;

import java.util.Map;
import java.util.TreeMap;

import mission.Board;
import mission.ParticleManager;
import mission.gameobject.Asteroid;
import mission.gameobject.Base;
import mission.gameobject.Component;
import mission.gameobject.Mech;
import mission.gameobject.SimpleMoveableObject;
import phys.Point2D;
import phys.Shape;

public class Bullet extends SimpleMoveableObject {

	/* default settings */
	public static final float BULLET_SPEED = 1.0f;
	public static final float BULLET_RANGE = 100f;
	public static final float BULLET_RADIUS = 0.5f;
	
	Map<BulletMod,Integer> mods = new TreeMap<BulletMod,Integer>(); 
	
	Mech parent;
	boolean enemyBullet;
	float fireTime;
	float range = BULLET_RANGE;

	public Bullet(Mech parent, boolean enemy, Shape shape, Point2D pos, Point2D vel) {
		super(shape, pos, vel, BULLET_SPEED);
		this.parent = parent;
		enemyBullet = enemy;
	}
	
	@Override
	protected void concreteTick(float dt, Board board) {
		
		fireTime += dt;
		
		if((fireTime > range && !mods.containsKey(BulletMod.MINE)) || fireTime > range*6)
			dead = true;
		
		for(BulletMod mod: mods.keySet()) {
			mod.bulletTick(dt, board, this);
		}
		
		if(enemyBullet) {
			for(Base b: board.getBase()) {
				doBaseCollision(b,board);
			}
			for(Mech e: board.getPlayers()) {
				if(e.isDead() || isDead()) continue;
				doMechCollision(e, board);
			}
			for(Mech e: board.getMechs()) {
				if(e.isDead() || isDead()) continue;
				doMechCollision(e, board);
			}
		} else {
			for(Base b: board.getBase()) {
				doBaseCollision(b,board);
			}
			if(!dead)
			for(Mech e: board.getEnemies()) {
				if(e.isDead() || isDead()) continue;
				doMechCollision(e, board);
			}
			if(!dead)
			for(Asteroid a: board.getAsteroids()) {
				if(a.isDead() || isDead()) continue;
				doAsteroidCollision(a, board);
				if(dead) break;
			}
		}
		
		// board destroy
		if((pos.x + getMaxX() < 0) || (pos.x + getMinX() > Board.BOARD_SIZE))
				dead = true;
		if((pos.y + getMinY() > Board.BOARD_SIZE) || (pos.y + getMaxY() < 0))
				dead = true;
	}

	private void doBaseCollision(Base b, Board board) {
		if(b.getPos().y + b.getMaxY() < 0)
			return;
		collisionOffset.x = b.getPos().x - pos.x;
		collisionOffset.y = b.getPos().y - pos.y;
		Point2D mtv = Shape.collide(shape,b.getShape(),collisionOffset);
		if(mtv!=Point2D.nullVector) {
			dead = true;
			ParticleManager.makeTinyExplosion(pos);
		}
	}

	private void doAsteroidCollision(Asteroid a, Board board) {
		collisionOffset.x = a.getPos().x - pos.x;
		collisionOffset.y = a.getPos().y - pos.y;
		Point2D mtv = Shape.collide(shape,a.getShape(),collisionOffset);
		if(mtv!=Point2D.nullVector) {
			dead = (!mods.containsKey(BulletMod.MEGA));
			a.setDead(true);
			a.breakAsteroid(board,parent.getId());
			for(BulletMod mod: mods.keySet()) {
				mod.bulletHit(board, this, a);
			}
			ParticleManager.makeTinyExplosion(pos);
		}
	}

	private void doMechCollision(Mech o, Board board) {
		for(Component acomp: o.getComponents()) {
			if(acomp.isDestroyed()) continue;
			collisionOffset.x = (o.getPos().x+acomp.getPos().x) - pos.x;
			collisionOffset.y = (o.getPos().y+acomp.getPos().y) - pos.y;
			Point2D mtv = Shape.collide(shape,acomp.getShape(),collisionOffset);
			if(mtv!=Point2D.nullVector) {
				dead = (acomp.isIndestructable() || !mods.containsKey(BulletMod.MEGA));
				o.damage(1,acomp,board);
				if(!enemyBullet)
					board.getCounter().addDamageDealt(parent.getId(),1);
				for(BulletMod mod: mods.keySet()) {
					mod.bulletHit(board, parent, this, o, acomp);
				}
				ParticleManager.makeTinyExplosion(pos);
			}
		};
	}

	public Mech getParent() {
		return parent;
	}
	
	public void setParent(Mech parent) {
		this.parent = parent;
	}
	
	public void setEnemyBullet(boolean enemyBullet) {
		this.enemyBullet = enemyBullet;
	}
	
	public boolean isEnemyBullet() {
		return enemyBullet;
	}
	
	public float getFireTime() {
		return fireTime;
	}
	
	public Map<BulletMod,Integer> getMods() {
		return mods;
	}
	
	public void setRange(float range) {
		this.range = range;
	}
	
	public float getRange() {
		return range;
	}
	
	public Bullet clone() {
		Bullet clone = new Bullet(parent,
				enemyBullet,
				shape.clone(),
				new Point2D(pos.x,pos.y),
				new Point2D(direction.x,direction.y));
		clone.setRange(range);
		clone.setSpeed(speed);
		for(BulletMod mod: mods.keySet())
			clone.getMods().put(mod,mods.get(mod));
		return clone;
	}
}
