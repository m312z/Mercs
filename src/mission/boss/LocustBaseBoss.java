package mission.boss;

import static mission.Board.BOARD_SIZE;
import static mission.boss.LocustSpawnBoss.locustColor;
import gui.FontManager;
import gui.FontManager.FontType;
import gui.GameGUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import mission.Board;
import mission.behaviour.MechAttack;
import mission.behaviour.MechAttack.Volley;
import mission.behaviour.MechBehaviour;
import mission.effects.WordManager;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.weapon.Weapon;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import mission.weapon.bullet.BulletMod;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class LocustBaseBoss extends Enemy {
	
	public LocustBaseBoss(List<Component> components) {
		super(components, new Point2D(), new Point2D(0, 1), 200, Mech.MECH_SPEED/2f);
	}
		
	public static Enemy makeBoss() {
		
		List<Component> components = new ArrayList<Component>();
		
		// head
		Component head = new Component(
				Shape.scale(DefaultShapes.basicSquare,Mech.MECH_RADIUS*1.5f),
				new Point2D(),-1);
		head.setColour(GameGUI.wreckage);
		head.setIndestructable(true);
		components.add(head);
		
		LocustBaseBoss e = new LocustBaseBoss(components);
		e.setBase(true);
		
		// create weapon
		Bullet b = new Bullet(e, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
		b.getMods().put(BulletMod.GARGANTUA, 1);
		b.setSpeed(Bullet.BULLET_SPEED/3f);
		b.setRange(Bullet.BULLET_RANGE*3f);
		Weapon w = new Weapon(b,160,ShotType.STRAIGHTDUAL);
		w.setFireDelay(60);
		head.setWeapon(w);
		
		// behaviour
		e.setAttackCycle(new MechAttack());
		e.setBehaviour(e.new LocustBaseBehaviour());
		
		// start position
		e.getPos().x = BOARD_SIZE/2f;
		e.getPos().y = -e.getMaxY();
		
		return e;
	}
		
	class LocustBaseBehaviour implements MechBehaviour
	{	
		final float boxSize = BOARD_SIZE/11;
		final float WAIT_TIME = 250;
		
		float timer;
		int state = 0;
		
		Component head;
		List<Component> leftBase;
		List<Component> rightBase;
		
		List<Component> healthBoxes;
		Component[][] roomLasers;
			
		int text = 0;
		String conversation[] = {
		};
		
		@Override
		public void tick(float dt, Board board, Mech mech) {
	
			timer += dt;
			
			if(state>2) {
				checkDamage(mech, board);
			}
				
			switch(state) {
			case 0:
				// move into position
				mech.getDirection().x = 0;
				mech.getDirection().y = 1;
				mech.setSpeed(Mech.MECH_SPEED/2f);
				if(mech.getPos().y + mech.getMinY() >= 0) {
					mech.getPos().y = -mech.getMinY();
					timer = 0;
					state++;
				}
				break;
			case 1:
				head = mech.getComponents().get(0);
				// stop moving
				mech.getDirection().x = 0;
				mech.getDirection().y = -1;
				mech.setSpeed(0);
				// display text
				if(timer > WAIT_TIME) {
					if(text>=conversation.length) {
						state++;
					} else {
						WordManager.addWord(
							conversation[text],
							new Point2D(
									mech.getPos().x - FontManager.getFont(FontType.FONT_24).getWidth(conversation[text])/(2*GameGUI.scale),
									mech.getPos().y + mech.getMinY()),
							(int)(WAIT_TIME-20));
						text++;
						timer = 0;
					}
				}
				break;
			case 2:
				// create base
				createBase(mech);
				state++;
			case 3:
				// attack cycle
				makeVolleys(mech);
				state++;
				break;
			case 4:
				// move in
				for(Component c: leftBase)
					c.getPos().x += dt*Mech.MECH_SPEED*0.2f;
				for(Component c: rightBase)
					c.getPos().x -= dt*Mech.MECH_SPEED*0.2f;
				if(mech.getPos().x + mech.getMinX() + MECH_RADIUS/2f >= 0)
					state++;
				break;
			case 5:
				// wait
				if(timer > WAIT_TIME/2f)
					state++;
				break;
			case 6:
				// start shooting
				mech.getAttackCycle().tick(dt, board);
				if(healthBoxes.isEmpty()) state++;
				break;
			case 7:
				if(timer > WAIT_TIME/2f)
					state++;
				break;
			case 8:
				// move away
				mech.getDirection().x = 0;
				mech.getDirection().y = -1;
				mech.setSpeed(Mech.MECH_SPEED*0.3f);
				break;
			}
		}

		private void checkDamage(Mech mech, Board board) {
			
			Iterator<Component> cit = healthBoxes.iterator();
			while(cit.hasNext()) {
				Component c = cit.next();
				if(c.isDestroyed()) {
					
					// remove component from baseList
					cit.remove();
					
					// shower bullets
					Bullet b = new Bullet(mech, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
					b.getMods().put(BulletMod.HOMING, 1);
					b.setSpeed(Bullet.BULLET_SPEED/3f);
					b.getDirection().y = 1;
					b.getDirection().x = 0;
					b.getPos().x = mech.getPos().x + c.getPos().x;
					b.getPos().y = mech.getPos().y + c.getPos().y;
					for(int i=0;i<12;i++) {
						Bullet clone = b.clone();
						clone.getPos().x += ((float)Math.random()-1)*boxSize;
						clone.getPos().y += ((float)Math.random()-1)*boxSize;
						board.addBullet(clone);
					}
				}
			}
			
		}

		private void makeVolleys(Mech mech) {
			
			List<Volley> volleys = new ArrayList<Volley>();
			Component[][] volleyList = new Component[6][9];
						
			for(int i=0;i<4;i++) {
				volleyList[0][i] = roomLasers[0][i];
				volleyList[1][i] = roomLasers[0][i];
				volleyList[2][i] = roomLasers[0][i];
				volleyList[3][i] = roomLasers[1][i];
				volleyList[4][i] = roomLasers[1][i];
				volleyList[5][i] = roomLasers[2][i];
				volleyList[0][i+4] = roomLasers[1][i];
				volleyList[1][i+4] = roomLasers[2][i];
				volleyList[2][i+4] = roomLasers[3][i];
				volleyList[3][i+4] = roomLasers[2][i];
				volleyList[4][i+4] = roomLasers[3][i];
				volleyList[5][i+4] = roomLasers[3][i];
			}

			for(int i=0;i<6;i++) {
				volleyList[i][8] = head;
				volleys.add(mech.getAttackCycle().new Volley(volleyList[i],160,320));
			}
			
			Collections.shuffle(volleys);
			for(Volley v: volleys)
				mech.getAttackCycle().addVolley(v);
		}

		private void createBase(Mech mech) {
			
			rightBase = new ArrayList<Component>();
			leftBase = new ArrayList<Component>();
			healthBoxes = new ArrayList<Component>();
						
			roomLasers = new Component[4][4];
			
			for(int side=0;side<2;side++) {
				
				for(int h=0;h<11;h++) {
				for(int w=0;w<5;w++) {
					
					// doors and rooms
					if(side==0 && (h==4 || h==9)) continue;
					if(side==1 && (h==2 || h==7)) continue;
					if(w<4 && h%5>1) continue; 
							
					Component c = new Component(Shape.scale(DefaultShapes.basicSquare, boxSize/2f), new Point2D(), 30);
					
					// health boxes
					if(w>0 && w<4 && (h==6 || h==1)) {
						c.setColour(locustColor);
						c.setShowHealth(true);
						healthBoxes.add(c);
					} else {
						c.setColour(GameGUI.wreckage);
						c.setIndestructable(true);
					}
					
					// position
					c.getPos().x = 
							(1-side*2)*(w*boxSize - 5*boxSize)
							+ side*BOARD_SIZE
							- mech.getPos().x; 
					c.getPos().y = (1/2f+h)*boxSize - mech.getPos().y;
					
					// lasers
					int room = h/6 + 2*side;
					if(w<4 && (h==10 || h==5)) {
						Point2D target = new Point2D(0, -boxSize*5);
						Bullet b = new Bullet(mech, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
						c.setWeapon(new Weapon(b, 30f, ShotType.LASER));
						c.getWeapon().getLaserTargets().add(target);
						
						roomLasers[room][w] = c;
					}
					mech.getComponents().add(c);
					if(side==0) leftBase.add(c);
					else rightBase.add(c);					
				}};
			}
		}
	}
}