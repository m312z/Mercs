package mission.boss;

import static mission.Board.BOARD_SIZE;
import static mission.boss.LocustSpawnBoss.locustColor;
import gui.FontManager;
import gui.FontManager.FontType;
import gui.GameGUI;

import java.util.ArrayList;
import java.util.List;

import mission.Board;
import mission.EnemyFactory;
import mission.behaviour.MechAttack;
import mission.behaviour.MechBehaviour;
import mission.behaviour.StaticPositionBehaviour;
import mission.effects.WordManager;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.specialMechs.LocustGift;
import mission.weapon.Weapon;
import mission.weapon.Weapon.ShotType;
import mission.weapon.bullet.Bullet;
import mission.weapon.bullet.BulletMod;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class LocustGiftBoss extends Enemy {
	
	public LocustGiftBoss(List<Component> components) {
		super(components, new Point2D(), new Point2D(0, 1), 200, Mech.MECH_SPEED/2f);
	}
		
	public static Enemy makeBoss() {
		
		List<Component> components = new ArrayList<Component>();
		
		// head
		Component head = new Component(
				Shape.scale(DefaultShapes.basicSquare,Mech.MECH_RADIUS*1.5f),
				new Point2D(),-1);
		head.setColour(LocustSpawnBoss.locustColor);
		head.setIndestructable(true);
		components.add(head);
		
		LocustGiftBoss e = new LocustGiftBoss(components);
		e.setBase(true);
				
		// behaviour
		e.setAttackCycle(new MechAttack());
		e.setBehaviour(e.new LocustGiftBehaviour());
		
		// start position
		e.getPos().x = BOARD_SIZE/2f;
		e.getPos().y = -e.getMaxY();
		
		return e;
	}
		
	class LocustGiftBehaviour implements MechBehaviour
	{	
//		final float boxSize = BOARD_SIZE/11;
		final float WAIT_TIME = 250;
		
		float timer;
		int state = 0;
			
		Component head;
		Enemy gift;
		Mech clone;
		
		int text = 0;
		String conversation[] = {
				"X3[TO/GREET]",
				"[RECRIMINATION?]",
				"[GIFT]",
				"F2%908)33"
		};
		
		String conversationDestroyed[] = {
				"...",
				"[SORROW?]",
				"[EXTREMITY?]x3H",
				"..."
		};
		
		String conversationCloned[] = {
				"4D41494E2829",
				"x3AH[CONFUSION?]",
				"41524D",
				"[TO/MIRROR]",
				"46495245"
		};
		
		@Override
		public void tick(float dt, Board board, Mech mech) {
	
			timer += dt;
							
			switch(state) {
			case 0:
				// move into position
				mech.getDirection().x = 0;
				mech.getDirection().y = 1;
				mech.setSpeed(Mech.MECH_SPEED/2f);
				if(mech.getPos().y + mech.getMinY() >= BOARD_SIZE/3f) {
					mech.getPos().y = BOARD_SIZE/3f - mech.getMinY();
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
				// create gift
				createGift(board);
				state++;
			case 3:
				// move aside
				mech.getDirection().x = -1;
				mech.getDirection().y = 0;
				mech.setSpeed(Mech.MECH_SPEED/2f);
				if(mech.getPos().x + mech.getMaxX()
						<= BOARD_SIZE/2f + gift.getMinX() + Mech.MECH_RADIUS) {
					timer = 0;
					state++;
				}
				break;
				
			case 4:
				// stop
				mech.setSpeed(0);
				checkDestroyed();
				checkCloned(board);
				break;
				
			case 5:
				// DESTROYED
				if(timer > WAIT_TIME) {
					if(text>=conversationDestroyed.length) {
						state = 7;
					} else {
						WordManager.addWord(
							conversationDestroyed[text],
							new Point2D(
									mech.getPos().x - FontManager.getFont(FontType.FONT_24).getWidth(conversationDestroyed[text])/(2*GameGUI.scale),
									mech.getPos().y + mech.getMinY()),
							(int)(WAIT_TIME-20));
						text++;
						timer = 0;
					}
				}
				break;
				
			case 6:
				// CLONED
				if(timer > WAIT_TIME) {
					if(gift.isDead() || gift.getHealth()<100 || clone.isDead()) {
						state = 7;
					} else if(text>=conversationCloned.length) {
						state = 9;
					} else {
						Point2D textPos = new Point2D();
						if(text%2==0) {
							// clone
							textPos.x = clone.getPos().x - FontManager.getFont(FontType.FONT_24).getWidth(conversationCloned[text])/(2*GameGUI.scale);
							textPos.y = clone.getPos().y + clone.getMinY();
						} else {
							//gift
							textPos.x = gift.getPos().x - FontManager.getFont(FontType.FONT_24).getWidth(conversationCloned[text])/(2*GameGUI.scale);
							textPos.y = gift.getPos().y + gift.getMinY();
						}
						WordManager.addWord(conversationCloned[text],textPos,(int)(WAIT_TIME-20));
						text++;
						timer = 0;
					}
				}
				break;
				
			case 7:
				if(timer > WAIT_TIME/2f) {
					if(clone!=null)
						clone.destroy(board, false);
					if(!gift.isDead())
						gift.destroy(board, false);
					state++;
				}
				break;
			case 8:
				// move away
				mech.getDirection().x = 0;
				mech.getDirection().y = -1;
				mech.setSpeed(Mech.MECH_SPEED);
				break;
				
			case 9:
				// arm and retreat
				armClone();
				state = 8;
				break;
			}
		}

		private void armClone() {
			
			if(clone==null || clone.isDead())
				return;

			// central octopus
			Bullet b = new Bullet(clone, false, Shape.scale(DefaultShapes.basicHex, Bullet.BULLET_RADIUS), new Point2D(), new Point2D(0,-1));
			b.getMods().put(BulletMod.HOMING, 1);
			Weapon w = new Weapon(b, 6, ShotType.SPIRAL);
			clone.getComponents().get(0).setWeapon(w);
			clone.getAttackCycle().addVolley(clone.getAttackCycle().new Volley(
					new Component[] {clone.getComponents().get(0)},
					200, 200));
			
			// side guns
			b = new Bullet(clone, false, Shape.scale(DefaultShapes.basicHex, Bullet.BULLET_RADIUS), new Point2D(), new Point2D(0,-1));
			for(int i=0;i<6;i++) {
				w = new Weapon(b, 6, ShotType.TARGETEDBEAM);
				clone.getComponents().get(1+i).setWeapon(w);
			}
			for(int i=0;i<3;i++) {
				clone.getAttackCycle().addVolley(clone.getAttackCycle().new Volley(
						new Component[] {
								clone.getComponents().get(i+1),
								clone.getComponents().get(i+4)},
						0, 60));
			}
		}

		private void checkCloned(Board board) {
			// only the gift has 101 health
			for(Mech m: board.getMechs()) {
				if(m.getMaxHealth()==101) {
					timer = 0;
					text = 0;
					state = 6;
					clone = m;
				}
			}
		}

		private void checkDestroyed() {
			if(gift.isDead()) {
				timer = 0;
				text = 0;
				state = 5;
			}
		}

		private void createGift(Board board) {
			
			List<Component> list = new ArrayList<Component>();
			Component c = new Component(
					Shape.scale(DefaultShapes.basicHex, Mech.MECH_RADIUS),
					new Point2D(), -1);
			c.setColour(locustColor);
			list.add(c);
			for(int i=0;i<6;i++) {
				c = new Component(
						Shape.scale(DefaultShapes.basicHex, Mech.MECH_RADIUS),
						new Point2D(), 80);
				c.setColour(locustColor);
				c.setShowHealth(true);
				c.getPos().x = EnemyFactory.getXCoord(1, i);
				c.getPos().y = EnemyFactory.getYCoord(1, i);
				list.add(c);
			}
			Enemy e = new LocustGift(list, new Point2D());
			e.getPos().x = Board.BOARD_SIZE/2f;
			e.getPos().y = -e.getMaxY();
			
			Point2D pos = new Point2D(BOARD_SIZE/2f, BOARD_SIZE/2f);
			e.setAttackCycle(new MechAttack());
			e.setBehaviour(new StaticPositionBehaviour(pos, -1));
			
			gift = e;
			board.addEnemy(e);
		}
	}
}