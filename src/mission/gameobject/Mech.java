package mission.gameobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mission.Board;
import mission.ParticleManager;
import mission.behaviour.MechAttack;
import mission.effects.Joint;
import mission.effects.WordManager;
import mission.shield.Shield;
import phys.Point2D;
import sound.SoundManager;
import sound.SoundManager.SoundEffect;

public abstract class Mech extends MoveableObject {

	/* default settings */
	public static final int MECH_HEALTH = 10;
	public static final float MECH_SPEED = 0.5f;
	public static final float MECH_RADIUS = 4f;
	
	// component parts
	protected boolean base = false;
	protected List<Component> components;
	protected Shield shield;
	protected List<Joint> joints;
	
	/* weapons */
	MechAttack attackCycle;
	boolean shooting;
	Point2D shootPosition = new Point2D();
	Set<Component> currentComponents;
	
	/* health and armor */
	protected float maxHealth;
	protected float health;
	
	/* ID for achievements (-1: enemy; 0,1: players) */
	protected int id = -1;
	
	/**
	 * Constructor; does not set weapon or aim.
	 * @param shape
	 * @param pos
	 * @param vel
	 * @param health
	 * @param speed
	 */
	public Mech(List<Component> components, Point2D pos, Point2D vel, float health, float speed) {
		super(pos, vel, speed);
		this.components = components;
		this.joints = new ArrayList<Joint>();
		this.maxHealth = this.health = health;
	}
	
	/**
	 * Damage the Mech
	 * @param amount	the amount of damage
	 */
	public void damage(float amount, Component component,  Board board) {
		amount = damageShield(amount, component, board);
		if(amount==0)
			return;
		if(!component.isIndestructable())
			damageHull(amount, component, board);
	}

	protected void damageHull(float amount, Component component, Board board) {
		// damage components
		if(component.getHealth()<=0) {
			// core component, damage mech
			health -= amount;
			if(health<=0) {
				destroy(board, this instanceof Enemy);
			}
		} else {
			// sub-component, damage separately
			component.setHealth(component.getHealth()-amount);
			if(component.getHealth()<=0) {
				SoundManager.playSound(SoundEffect.EXPLOSION);
				ParticleManager.makeExplosion(new Point2D(component.getPos().x+pos.x,component.getPos().y+pos.y));
				component.setDestroyed(true);
				boolean alive = false;
				for(Component c: components)
					if(!c.isDestroyed())
						alive = true;
				if(!alive) {
					destroy(board, this instanceof Enemy);
				}
			}
		}
	}
	
	protected float damageShield(float amount, Component component,  Board board) {
		// absorb damage with shields
		if(shield!=null)
			amount = shield.damage(board, amount);
		return amount;
	}

	public void destroy(Board board, boolean score) {
		SoundManager.playSound(SoundEffect.EXPLOSION);
		dead = true;
		if(score) {
			board.getCounter().addEnemiesDestroyed(1);
			board.addScore(components.size()*6);
			WordManager.addWord("+"+(components.size()*6*board.getMap().getLevel()),new Point2D(pos.x,pos.y),30);
		}
		for(Component c: components)
			ParticleManager.makeExplosion(new Point2D(c.getPos().x+pos.x,c.getPos().y+pos.y));
	}
	
	@Override
	protected void concreteTick(float dt, Board board) {
		
		makeDecisions(dt,board);
		
		// shield
		if(shield!=null)
			shield.tick(dt, board);
		
		// smoke and burn
		for(Component c: components) {
			if(c.isDestroyed()) {
				if(board.isParticleTick())
					ParticleManager.makeSmoke(new Point2D(c.getPos().x+pos.x,c.getPos().y+pos.y), Mech.MECH_RADIUS/2f);
			} else {
				if(c.getBurnTime()>dt) {
					c.setBurnTime(c.getBurnTime()-dt);
					damage(3/60f,c,board);
					board.getCounter().addFireDamage(3/60f);
					if(board.isParticleTick())
						ParticleManager.makeFire(new Point2D(c.getPos().x+pos.x,c.getPos().y+pos.y), Mech.MECH_RADIUS/2f);
				} else if(c.getBurnTime()>0) {
					c.setBurnTime(0);
				}
			}
		}
		
		// move joints
		for (Joint j : joints) {
			j.tick(dt);
		}
		
		// shoot
		for(Component c: currentComponents) {
			if(!c.isDestroyed() && c.getWeapon()!=null) {
				shootPosition.x = pos.x + c.getPos().x;
				shootPosition.y = pos.y + c.getPos().y;
				c.getWeapon().setHolding(shooting);
				c.getWeapon().tick(dt, this, shootPosition, board);
				if(shooting && c.getWeapon().isReadytoShoot()) {
					c.getWeapon().shoot(this, c, shootPosition, board);
				}
			}
		}
	}
	
	protected abstract void makeDecisions(float dt, Board board);

	/*---------------------*/
	/* Setters and Getters */
	/*---------------------*/

	public MechAttack getAttackCycle() {
		return attackCycle;
	}
	
	public void setAttackCycle(MechAttack attackCycle) {
		this.attackCycle = attackCycle;
	}
	
	public boolean isShooting() {
		return shooting;
	}

	public void setShooting(boolean shooting) {
		this.shooting = shooting;
	}
	
	public void setShield(Shield shield) {
		this.shield = shield;
	}
	
	public Shield getShield() {
		return shield;
	}
	
	public float getHealth() {
		return health;
	}
	
	public float getMaxHealth() {
		return maxHealth;
	}
	
	public void setHealth(float health) {
		this.health = health;
		if(health>maxHealth)
			health=maxHealth;
	}
	
	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}
		
	public List<Component> getComponents() {
		return components;
	}
	
	public List<Joint> getJoints() {
		return joints;
	}
	
	public float getMaxX() {
		float max = components.get(0).getShape().getMaxX()+components.get(0).getPos().x;
		for(int i=1;i<components.size();i++) {
			max = Math.max(max, components.get(i).getShape().getMaxX()+components.get(i).getPos().x);
		}
		return max;
	}
	
	public float getMinX() {
		float min = components.get(0).getShape().getMinX();
		for(int i=1;i<components.size();i++) {
			min = Math.min(min, components.get(i).getShape().getMinX()+components.get(i).getPos().x);
		}
		return min;
	}

	public float getMaxY() {
		float max = components.get(0).getShape().getMaxY()+components.get(0).getPos().y;
		for(int i=1;i<components.size();i++) {
			max = Math.max(max, components.get(i).getShape().getMaxY()+components.get(i).getPos().y);
		}
		return max;
	}
	
	public float getMinY() {
		float min = components.get(0).getShape().getMinY()+components.get(0).getPos().y;
		for(int i=1;i<components.size();i++) {
			min = Math.min(min, components.get(i).getShape().getMinY()+components.get(i).getPos().y);
		}
		return min;
	}
	
	public int getId() {
		return id;
	}

	public boolean isBase() {
		return base;
	}

	public void setBase(boolean base) {
		this.base = base;
	}
}