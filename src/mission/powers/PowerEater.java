package mission.powers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mission.Board;
import mission.SupportFactory;
import mission.gameobject.AllyMech;
import mission.gameobject.Component;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.weapon.SelfDefenceLaser;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import mission.weapon.bullet.BulletMod;
import phys.Point2D;

public class PowerEater {
	
	List<ActivePower> activePowers;
	
	/* stuff for specific powers */
	AllyMech clone;
	List<AllyMech> gadgets;
	List<AllyMech> support;
	List<AllyMech> drones;
	AllyMech port;
	AllyMech decoy;
	float explodeFireTimer;
	
	public PowerEater() {
		activePowers = new ArrayList<PowerEater.ActivePower>();
		support = new ArrayList<AllyMech>();
		gadgets = new ArrayList<AllyMech>();
		drones =  new ArrayList<AllyMech>();
	}

	public void tick(float dt, Board board, Player player) {
		for(ActivePower ap: activePowers) {
			
			if(ap.remainingCooldown > dt)
				ap.remainingCooldown -= dt;
			else if(ap.remainingCooldown > 0)
				ap.remainingCooldown = 0;
			
			tickPower(dt, board, player, ap);
			
			if(ap.remainingActive > dt) {
				ap.remainingActive -= dt;
			} else if(ap.remainingActive > 0) {
				ap.remainingActive = 0;
			}
			
			if(ap.active && ap.remainingActive<=0) {
				removePower(ap,board,player);
				ap.active = false;
			}
			
			Iterator<AllyMech> mit = support.iterator();
			while(mit.hasNext()) {
				if(mit.next().isDead())
					mit.remove();
			}
			
			mit = drones.iterator();
			while(mit.hasNext()) {
				if(mit.next().isDead())
					mit.remove();
			}
			
			mit = gadgets.iterator();
			while(mit.hasNext()) {
				if(mit.next().isDead())
					mit.remove();
			}
			
			if(decoy!=null && decoy.isDead()) {
				if(hasPower(Power.PORT_EXPLOSION)) {
					Bullet bullet = player.getComponents().get(0).getWeapon().getBullet();
					for(int i=0;i<36;i++) {
						Bullet b = bullet.clone();
						b.getPos().x = decoy.getPos().x;
						b.getPos().y = decoy.getPos().y;
						b.getDirection().x = (float) Math.cos(Math.toRadians(i*360.0/36.0));
						b.getDirection().y = (float) Math.sin(Math.toRadians(i*360.0/36.0));
						board.addBullet(b);
					}
				}
				decoy = null;
			}
		}
	}

	public void activatePower(Power p, Board board, Player player) {
		for(int i=0;i<activePowers.size();i++) {
			if(activePowers.get(i).type == p) {
				activatePower(i, board, player);
			}
		}
	}
	
	public void activatePower(int powerIndex, Board board, Player player) {
			
		if(powerIndex<0 || powerIndex >= activePowers.size())
			return;
		ActivePower ap = activePowers.get(powerIndex);
		if(ap.remainingCooldown > 0)
			return;
		ap.remainingCooldown = ap.type.coolDown;
		ap.remainingActive = ap.type.duration;
		ap.active = true;
		
		switch(ap.type) {
		case SLOW:
			board.setTimeScale(0.2f);
			break;
		case CLUSTERPOWER:
			addBulletMod(player, BulletMod.CLUSTER,8);
			break;
		case GARGANTUA:
			addBulletMod(player, BulletMod.GARGANTUA,8);
			break;
		case SHIELD_CHARGING:
			addBulletMod(player, BulletMod.SHIELDCHARGE,8);
			break;
		case FIREPOWER:
			addBulletMod(player, BulletMod.FIRE,8);
			break;
		case HOMINGPOWER:
			addBulletMod(player, BulletMod.HOMING,1);
			break;
		case MEGAPOWER:
			addBulletMod(player, BulletMod.MEGA,6);
			break;
		case MINEPOWER:
			if(!player.getComponents().get(0).getWeapon().getBullet().getMods().containsKey(BulletMod.MINE))
				addBulletMod(player, BulletMod.MINE,1);
			else
				removeBulletMod(player, BulletMod.MINE);
			break;
		case TELEPORT:
			if(port==null) {
				port = SupportFactory.makePort(board, player);
				if(hasPower(Power.SUPPORT_DRONEMASTER))
					addAllBulletModsToSupport(player, port);
				support.add(port);
				board.addNonCompetingMech(port);
			} else {
				if(hasPower(Power.PORT_EXPLOSION)) {
					Bullet bullet = player.getComponents().get(0).getWeapon().getBullet();
					for(int i=0;i<36;i++) {
						Bullet b = bullet.clone();
						b.getPos().x = port.getPos().x;
						b.getPos().y = port.getPos().y;
						b.getDirection().x = (float) Math.cos(Math.toRadians(i*360.0/36.0));
						b.getDirection().y = (float) Math.sin(Math.toRadians(i*360.0/36.0));
						board.addBullet(b);
					}
				}
				player.getPos().x = port.getPos().x;
				player.getPos().y = port.getPos().y;
				port.destroy(board, false);
				port = null;
			}
			break;
		case SUPPORT_KILLSTAR:
			AllyMech k = SupportFactory.makeKillstar(board, player);
			if(hasPower(Power.SUPPORT_DRONEMASTER))
				addAllBulletModsToSupport(player, k);
			support.add(k);
			board.addMech(k, 120);
			break;
		case SUPPORT_DRONE:
			if(drones.size()<4) {
				AllyMech d = SupportFactory.makeDrone(board, player);
				if(hasPower(Power.SUPPORT_DRONEMASTER))
					addAllBulletModsToSupport(player, d);
				support.add(d);
				drones.add(d);
				board.addMech(d, 120);
			} else {
				ap.remainingCooldown = 0;
			}
			break;
		case SUPPORT_DECOY:
			if(decoy!=null && !decoy.isDead()) {
				if(hasPower(Power.PORT_EXPLOSION)) {
					Bullet bullet = player.getComponents().get(0).getWeapon().getBullet();
					for(int i=0;i<36;i++) {
						Bullet b = bullet.clone();
						b.getPos().x = decoy.getPos().x;
						b.getPos().y = decoy.getPos().y;
						b.getDirection().x = (float) Math.cos(Math.toRadians(i*360.0/36.0));
						b.getDirection().y = (float) Math.sin(Math.toRadians(i*360.0/36.0));
						board.addBullet(b);
					}
				}
				decoy.destroy(board, false);
			}
			decoy = SupportFactory.makeDecoy(board, player);
			if(hasPower(Power.SUPPORT_DRONEMASTER))
				addAllBulletModsToSupport(player, decoy);
			support.add(decoy);
			board.addMech(decoy);
			break;
		case SPIRALPOWER:
			player.getComponents().get(0).getWeapon().setShotType(ShotType.SPIRAL);
			break;
		case RAPIDFIRE:
			player.getComponents().get(0).getWeapon().setFireDelay(player.getOriginalFireDelay()/2f);
			break;
		case CLONE:
			if(player.getComponents().get(0).getWeapon().getShotType()==ShotType.CLONERAY) {
				player.getComponents().get(0).getWeapon().setShotType(player.getOriginalShotType());
			} else player.getComponents().get(0).getWeapon().setShotType(ShotType.CLONERAY);
			break;
		case CLONE_SELFDESTRUCT:
			if(clone!=null && !clone.isDead()) {
				clone.destroy(board, false);
				Point2D dir = new Point2D();
				for(Bullet b: board.getEnemyBullets()) {
					dir.x = clone.getPos().x - b.getPos().x;
					dir.y = clone.getPos().y - b.getPos().y;
					if(Point2D.magnitude(dir) < Mech.MECH_RADIUS*10)
						b.setSpeed(0);
				}
				for(Component c: clone.getComponents()) {
					Bullet bullet = player.getComponents().get(0).getWeapon().getBullet();
					for(int i=0;i<20;i++) {
						Bullet b = bullet.clone();
						b.getPos().x = c.getPos().x + clone.getPos().x;
						b.getPos().y = c.getPos().y + clone.getPos().y;
						b.getDirection().x = (float) Math.cos(Math.toRadians(i*360.0/20.0));
						b.getDirection().y = (float) Math.sin(Math.toRadians(i*360.0/20.0));
						board.addBullet(b);
					}
				}
			}
			break;
		case IONBEAMS:
			if(player.getComponents().get(0).getWeapon().getShotType()==ShotType.IONBEAM) {
				player.getComponents().get(0).getWeapon().setShotType(player.getOriginalShotType());
			} else
				player.getComponents().get(0).getWeapon().setShotType(ShotType.IONBEAM);
			break;
		case EXPLOSIVECHARGE:
			explodeFireTimer = 0;
			break;
		default:
			break;
		}
	}
	
	private void tickPower(float dt, Board board, Player player, ActivePower ap) {
		switch(ap.type) {
		case EXPLOSIVECHARGE:
			if(ap.active) {
				explodeFireTimer += dt;
				if(explodeFireTimer > 3) {
					explodeFireTimer = explodeFireTimer%3;
					Bullet bullet = player.getComponents().get(0).getWeapon().getBullet();
					Bullet b = bullet.clone();
					b.getPos().x = player.getPos().x;
					b.getPos().y = player.getPos().y;
					b.getDirection().x = (float) Math.cos(Math.toRadians(ap.remainingCooldown*8));
					b.getDirection().y = (float) Math.sin(Math.toRadians(ap.remainingCooldown*8));
					board.addBullet(b);
				}
			}
			break;
		case DEFENCELASER:
			if(ap.active) {
				Point2D pos = player.getPos();
				SelfDefenceLaser.fire(dt, board, pos, player);
				SelfDefenceLaser.getHitObjects().clear();
			}
			break;
		case DEFLECTORSHIELD:
			if(player.getShield()!=null && player.getShield().getCurrentCapacity()>0) {
				// defelct bullets
				Point2D p = new Point2D();
				for(Bullet b: board.getEnemyBullets()) {
					p.x = (b.getPos().x-player.getPos().x);
					p.y = (b.getPos().y-player.getPos().y);
					if(Point2D.magnitude(p)<Mech.MECH_RADIUS*5) {
						Point2D.normalise(p);
						Point2D.normalise(b.getDirection());
						b.getDirection().x *= 25;
						b.getDirection().y *= 25;
						p.x *= player.getShield().getCurrentCapacity()/player.getShield().getMaxCapacity();
						p.y *= player.getShield().getCurrentCapacity()/player.getShield().getMaxCapacity();
						b.getDirection().x = b.getDirection().x + p.x;
						b.getDirection().y = b.getDirection().y + p.y;
					}
				}
			}
			break;
		default:
			break;
		}
	}

	public void removePower(ActivePower ap, Board board, Player player) {
		switch(ap.type) {
		case SLOW:
			board.setTimeScale(1f);
			break;
		case CLUSTERPOWER:
			removeBulletMod(player, BulletMod.CLUSTER);
			break;
		case GARGANTUA:
			removeBulletMod(player, BulletMod.GARGANTUA);
			break;
		case SHIELD_CHARGING:
			removeBulletMod(player, BulletMod.SHIELDCHARGE);
			break;
		case FIREPOWER:
			removeBulletMod(player, BulletMod.FIRE);
			break;
		case HOMINGPOWER:
			removeBulletMod(player, BulletMod.HOMING);
			break;
		case MEGAPOWER:
			removeBulletMod(player, BulletMod.MEGA);
			break;
		case SPIRALPOWER:
			player.getComponents().get(0).getWeapon().setShotType(player.getOriginalShotType());
			break;
		case RAPIDFIRE:
			player.getComponents().get(0).getWeapon().setFireDelay(player.getOriginalFireDelay());
			break;
		default:
			break;
		}
	}
	
	private void addBulletMod(Player player, BulletMod mod, int amnt) {
		
		player.getComponents().get(0).getWeapon().getBullet().getMods().put(mod, amnt);
		
		// share bullet mods
		if(hasPower(Power.SUPPORT_DRONEMASTER)) {
			for(AllyMech m: support) {
				for(Component c: m.getComponents()) {
					if(c.getWeapon()!=null && c.getWeapon().getBullet()!=null)
						c.getWeapon().getBullet().getMods().put(mod, amnt);
				}
			}
		}
	}
	
	private void removeBulletMod(Player player, BulletMod mod) {
		
		player.getComponents().get(0).getWeapon().getBullet().getMods().remove(mod);
		
		// remove mods
		if(hasPower(Power.SUPPORT_DRONEMASTER)) {
			for(AllyMech m: support) {
				for(Component c: m.getComponents()) {
					if(c.getWeapon()!=null && c.getWeapon().getBullet()!=null)
						c.getWeapon().getBullet().getMods().remove(mod);
				}
			}
		}
	}
	
	public boolean hasPower(Power power) {
		for(ActivePower ap: getActivePowers())
			if(ap.type==power)
				return true;
		return false;
	}
	
	public List<ActivePower> getActivePowers() {
		return activePowers;
	}
	
	public void addActivePower(Power power) {
		ActivePower ap = new ActivePower();
		ap.type = power;
		ap.remainingActive = 0;
		ap.remainingCooldown = 0;
		activePowers.add(ap);
	}
	
	public void addAllBulletModsToSupport(Player player, Mech support) {
		// share bullet mods
		for(BulletMod mod: player.getBullet().getMods().keySet()) {
			for(Component c: support.getComponents()) {
				if(c.getWeapon()!=null && c.getWeapon().getBullet()!=null)
					c.getWeapon().getBullet().getMods().put(mod, player.getBullet().getMods().get(mod));
			}
		}
	}
	
	public Mech getClone() {
		return clone;
	}
	
	public void setClone(AllyMech clone) {
		support.add(clone);
		this.clone = clone;
	}
	
	public List<AllyMech> getGadgets() {
		return gadgets;
	}

	public void addGadget(AllyMech g) {
		gadgets.add(g);
		support.add(g);
	}
		
	public class ActivePower
	{
		public Power type;
		public float remainingCooldown;
		public float remainingActive;
		public boolean active;
	}
}
