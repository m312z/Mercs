package mission.behaviour;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.behaviour.MechAttack.Volley;
import mission.boss.BuilderBaseBoss;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.weapon.Weapon;
import mission.weapon.Weapon.ShotType;
import mission.weapon.WeaponFactory;
import mission.weapon.bullet.Bullet;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class BuilderBehaviour implements MechBehaviour {

	Mech target;
	Component c;
	boolean hasWeaponComponent;
	Point2D hardpoint;
	boolean placed;
	boolean rotatingTarget;
	float angle;
	int placeOffset;

	static List<Mech> collisionList = new ArrayList<Mech>(1);
		
	public BuilderBehaviour(boolean weapon) {
		placeOffset = (int) (Math.random()*6);
		hardpoint = new Point2D();
		placed = false;
		angle = 0;
		hasWeaponComponent = weapon;
	}
	
	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		// dead target
		if(target!=null && target.isDead())
			target = null;
		
		// choose target and weapon
		if(!placed && target==null)
			chooseTarget(mech, board);
		if(!placed && c==null)
			makeComponent(mech);

		// rotate component
		if(rotatingTarget && !placed && c!=null) {
			angle+=dt;
		}
		
		if(placed) {
			
			// retreat
			mech.getDirection().x = 0;
			mech.getDirection().y = -1;
			mech.setSpeed(Mech.MECH_SPEED);
			if(!((Enemy)mech).isOnScreen())
				mech.setDead(true);
			
		} else if(target!=null) {
			
			// move toward target
			setHardPoint(target);
			mech.getDirection().x = (hardpoint.x) - mech.getPos().x;
			mech.getDirection().y = (hardpoint.y + (float)(Math.sin(Math.toRadians(60)))*Mech.MECH_RADIUS*(5/3f)) - mech.getPos().y;
			float mag = Point2D.magnitude(mech.getDirection());
			mech.setSpeed(Mech.MECH_SPEED);
			
			if(mag < Mech.MECH_SPEED) {
				
				// place new component
				mech.getComponents().remove(c);
				target.getComponents().add(c);
				
				if(rotatingTarget)
					c.getShape().rotate(angle);
				c.getPos().x = hardpoint.x - target.getPos().x;
				c.getPos().y = hardpoint.y - target.getPos().y;
				
				if(c.getWeapon()!=Weapon.spongeGun) {
					if(target.getId()<0) {
						boolean volleyFound = false;
						for(Volley v: target.getAttackCycle().getShootCycle()) {
							boolean laser = false;
							if(target.getComponents().get(0).getWeapon()!=null)
								laser = (target.getComponents().get(0).getWeapon().getShotType() == ShotType.LASER);
							if((laser || v.components.size()<2) && v.components.size()>0) {
								volleyFound = true;
								v.components.add(c);
								break;
							}
						}
						if(!volleyFound) {
							target.getAttackCycle().addVolley(target.getAttackCycle().new Volley(
									new Component[] {c}, 30, 240));
						}
					} else {
						((Player)target).getCurrentComponents().add(c);
					}
				}
				placed = true;
			}
		} else {
			
			// retreat
			mech.getDirection().x = 0;
			mech.getDirection().y = -1;
			mech.setSpeed(Mech.MECH_SPEED);
			if(!((Enemy)mech).isOnScreen())
				mech.setDead(true);
		}
	}

	private void makeComponent(Mech mech) {
		c = new Component(
				Shape.scale(DefaultShapes.basicHex,Mech.MECH_RADIUS),
				new Point2D(0,0), 15);
		
		if(hasWeaponComponent) {
			Weapon weapon;
			if(mech.getId()<0) {
				weapon = makeBuilderWeapon((Mech)mech);
				c.setColour(BuilderBaseBoss.builderBossColor);
			} else {
				weapon = WeaponFactory.makeWeapon(mech,ShotType.SPREAD);
				c.setHealth(60);
			}
			c.setWeapon(weapon);
		} else {
			c.setColour(BuilderBaseBoss.builderBossColorDark[0]);
		}
		
		c.getPos().x = 0;
		c.getPos().y = -(float)(Math.sin(Math.toRadians(60)))*Mech.MECH_RADIUS*(5/3f);
		
		mech.getComponents().add(c);
	}

	private void chooseTarget(Mech mech, Board board) {
		if(mech.getId()>=0) {
			for(Player p: board.getPlayers()) {
				if(p.getId() == mech.getId() && p.getComponents().size()<2) {
					target = p;
					break;
				}
			}
		} else {
			for(int i=board.getEnemies().size()-1; i>=0; i--) {
				Enemy m = board.getEnemies().get(i);
				if(m.getComponents().size()>2) {
					target = m;
					if(m.getBehaviour() instanceof BounceDriftBehaviour
							|| m.getBehaviour() instanceof BuilderBounceDriftBehaviour) {
						rotatingTarget = true;
						angle = ((BounceDriftBehaviour)m.getBehaviour()).getTotalAngle();
					} else {
						rotatingTarget = false;
						angle = 0;
					}
					break;
				}
			}
		}
	}

	private void setHardPoint(Mech mech) {
		
		for(int rad=1; rad<4; rad++) {
			int offset = placeOffset*rad+1;
			// for each layer, work around hex
			for(int j=offset;j<offset+rad*6;j++) {
				
				int i = j%(rad*6);
				
				// x coord
				float x = Math.abs(((i+(rad*3)/2f)%(rad*3))-(rad*3)/2f);
				if(x>rad) x=rad;
				if(i>rad*3) x = -x;
				x = x*3*Mech.MECH_RADIUS/2f;
				
				// y coord
				float y = rad*3 - Math.abs(i-rad*3);
				if(y<rad) y = y - 2*rad;
				else if(y > 2*rad) y = y - rad;
				else y = 2*(y-rad) - rad;
				y = (float) (y*Mech.MECH_RADIUS*Math.sin(Math.toRadians(60)));
								
				// check if point is empty
				boolean empty = true;
				hardpoint.x = x;
				hardpoint.y = y;
				hardpoint.rotate(angle);
				for(Component c: mech.getComponents()) {
					if(Shape.shapeContainsPoint(hardpoint,c.getShape(),c.getPos())!=Point2D.nullVector)
						empty = false;
				}
				
				// we have found a point, exit
				if(empty) {
					hardpoint.x += mech.getPos().x;
					hardpoint.y += mech.getPos().y;
					return;
				}
			}
		}
	}
	
	public static Weapon makeBuilderWeapon(Mech e) {
		
		ShotType st = ShotType.values()[(int) (Math.random()*ShotType.values().length)];
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		float fireDelay = 3f;
		switch(st) {						
		case ASTEROID:
			st = ShotType.TARGETEDBEAM;
			fireDelay = 3f;
			break;
		case CLONERAY:
			st = ShotType.SPREAD;
			fireDelay = 2;
			break;
		case IONBEAM:
			st = ShotType.STRAIGHTDUAL;
			fireDelay = 3f;
			break;
		case LASER:
			st = ShotType.FASTSPIRAL;
		case FASTSPIRAL:
			fireDelay = 10;
			b.setSpeed(Bullet.BULLET_SPEED/3);
			b.setRange(Bullet.BULLET_RANGE*3);
			break;
		default:
			st = ShotType.STRAIGHT;
			fireDelay = 3f;
			break;
		}
		Weapon bossweapon = new Weapon(b, fireDelay, st);
		return bossweapon;
	}
}
