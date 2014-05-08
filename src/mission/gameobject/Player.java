package mission.gameobject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mission.Board;
import mission.SupportFactory;
import mission.powers.Power;
import mission.powers.PowerEater;
import mission.powers.PowerEater.ActivePower;
import mission.weapon.Weapon;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import phys.Point2D;
import sound.SoundManager;
import sound.SoundManager.SoundEffect;

public class Player extends Mech {

	private boolean mouseControlled = false;
	private int mouseSelection = 0;
	
	protected String playerID;
	protected PowerEater powerEater;
	
	/* for deactivating powers */
	protected ShotType originalShotType;
	protected float originalFireDelay;
	
	public Player(String id, int playerNumber, List<Component> components, Point2D pos, Point2D vel, float speed) {
		super(components, pos, vel, 1, speed);
		this.id = playerNumber;
		this.playerID = id;
		this.bounded = true;
		
		this.currentComponents = new HashSet<Component>();
		this.currentComponents.addAll(components);
		this.powerEater = new PowerEater();
	}
	
	public void setPlayerWeapon(Weapon weapon) {
		this.components.get(0).setWeapon(weapon);
		this.originalShotType = weapon.getShotType();
		this.originalFireDelay = weapon.getFireDelay();
	}
	
	@Override
	protected void makeDecisions(float dt, Board board) {
				
		// powerups
		powerEater.tick(dt, board, this);
		if(shield==null) powerEater.activatePower(Power.EXPLOSIVECHARGE, board, this);
		
		if(isShooting()) {
			SoundManager.playSound(SoundEffect.PROJECTILE);
		}
		
		Iterator<Component> cit = components.iterator();
		while(cit.hasNext()) {
			Component c = cit.next();
			if(c.isDestroyed()) {
				if(currentComponents.contains(c))
					currentComponents.remove(c);
				cit.remove();
			}
		}
	}

	public void reset(Board board) {
		
		// powers
		for(ActivePower ap:powerEater.getActivePowers()) {
			ap.remainingActive = 0;
			powerEater.removePower(ap,board,this);
			ap.remainingCooldown = 0;
		}
		
		//weapons
		components.get(0).getWeapon().setFireDelay(originalFireDelay);
		components.get(0).getWeapon().setShotType(originalShotType);
		
		// added components
		Iterator<Component> cit = components.iterator();
		while(cit.hasNext()) {
			Component c = cit.next();
			if(c!=components.get(0)) {
				if(currentComponents.contains(c))
					currentComponents.remove(c);
				cit.remove();
			}
		}
		
		// support satellites
		if(powerEater.hasPower(Power.SUPPORT_GADGETS)) {
			for(AllyMech g: powerEater.getGadgets())
				g.destroy(board, false);
			powerEater.getGadgets().clear();
			for(int i=0;i<6;i++) {
				AllyMech g = SupportFactory.makeGadget(board, this, i*60);
				powerEater.addGadget(g);
			}
		}
		
		// shields and health
		if(shield!=null)
			shield.setCurrentCapacity(shield.getMaxCapacity());
		health = maxHealth;
		
		// position and such
		pos.x = Board.BOARD_SIZE/2;
		pos.y = 4*Board.BOARD_SIZE/5;
		direction.x = 0;
		direction.y = 0;
		shooting = false;
		dead = false;
	}
	
	@Override
	public void damage(float amount, Component component,  Board board) {
		
		board.getCounter().addDamageTaken(id,amount);
		amount = damageShield(amount, component, board);
		if(amount==0)
			return;
		damageHull(amount, component, board);
	}
	
	@Override
	protected float damageShield(float amount, Component component,  Board board) {

		if(shield==null)
			return amount;
		
		// reflection
		if(powerEater.hasPower(Power.BULLETREFLECT) && shield.getCurrentCapacity() > 0) {
			Bullet bullet = components.get(0).getWeapon().getBullet();
			for(int i=0;i<12;i++) {
				Bullet b = bullet.clone();
				b.getPos().x = pos.x;
				b.getPos().y = pos.y;
				b.getDirection().x = (float) Math.cos(Math.toRadians(i*30 + board.getTime()));
				b.getDirection().y = (float) Math.sin(Math.toRadians(i*30 + board.getTime()));
				board.addBullet(b);
			}
		}
		
		// check for shield depletion
		boolean explode = (shield.getCurrentCapacity() > 0 && powerEater.hasPower(Power.EXPLOSIVECHARGE));
		
		// absorb damage with shields
		amount = shield.damage(board, amount);
		
		if(shield.getCurrentCapacity() <= 0 && explode) {
			powerEater.activatePower(Power.EXPLOSIVECHARGE, board, this);
		}
				
		return amount;
	}
	
	public void destroy(Board board, boolean score) {
		
		super.destroy(board, score);
			
		//powers
		if(powerEater.hasPower(Power.SUPPORT_GADGETS)) {
			for(AllyMech g: powerEater.getGadgets())
				g.destroy(board, false);
		}
	}
	
	/*---------------------*/
	/* Setters and Getters */
	/*---------------------*/
	
	public Set<Component> getCurrentComponents() {
		return this.currentComponents;
	}
	
	public int getPlayerNumber() {
		return id;
	}
	
	public String getPlayerID() {
		return playerID;
	}
	
	public Weapon getWeapon() {
		return components.get(0).getWeapon();
	}
	
	public Bullet getBullet() {
		return components.get(0).getWeapon().getBullet();
	}
	
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}
	
	public PowerEater getPowerEater() {
		return powerEater;
	}
	
	public ShotType getOriginalShotType() {
		return originalShotType;
	}

	public float getOriginalFireDelay() {
		return originalFireDelay;
	}
	
	public boolean isMouseControlled() {
		return mouseControlled;
	}
	
	public void setMouseControlled(boolean mouseControlled) {
		this.mouseControlled = mouseControlled;
	}
	
	public void setMouseSelection(int mouseSelection) {
		this.mouseSelection = mouseSelection;
	}
	
	public int getMouseSelection() {
		return mouseSelection;
	}
}
