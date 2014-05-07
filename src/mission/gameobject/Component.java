package mission.gameobject;

import java.awt.Color;

import mission.weapon.Weapon;
import phys.Point2D;
import phys.Shape;

public class Component {
	
	protected Shape shape;
	protected Color colour;
	protected Point2D pos;
	protected Weapon weapon;
	protected float health;
	protected float maxHealth;
	protected float burnTime;
	protected boolean destroyed;
	protected boolean indestructable = false;
	private boolean showHealth;
	
	public Component(Shape shape, Point2D pos, float health) {
		this.shape = shape;
		this.pos = pos;
		this.weapon = Weapon.spongeGun;
		this.health = health;
		this.maxHealth = health;
		this.destroyed = false;
		this.colour = Color.GRAY; 
	}

	public Shape getShape() {
		return shape;
	}
	
	public Point2D getPos() {
		return pos;
	}
	
	public Weapon getWeapon() {
		return weapon;
	}
	
	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	};
	
	public void setHealth(float health) {
		this.health = health;
	}
	
	public float getHealth() {
		return health;
	}
	
	public float getMaxHealth() {
		return maxHealth;
	}
	
	public float getBurnTime() {
		return burnTime;
	}
	
	public void setBurnTime(float burnTime) {
		this.burnTime = burnTime;
	}
	
	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}
	
	public boolean isDestroyed() {
		return destroyed;
	}

	public Color getColour() {
		return colour;
	}
	
	public void setColour(Color colour) {
		this.colour = colour;
	}
	
	public void setIndestructable(boolean indestructable) {
		this.indestructable = indestructable;
	}
	
	public boolean isIndestructable() {
		return indestructable;
	}

	public void setShowHealth(boolean showHealth) {
		this.showHealth = showHealth;
	}
	
	public boolean showHealth() {
		return showHealth;
	}
}
