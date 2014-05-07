package mission.boss;

import gui.FontManager;
import gui.FontManager.FontType;
import gui.GameGUI;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mission.Board;
import mission.ParticleManager;
import mission.behaviour.MechAttack;
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

public class LocustSpawnBoss extends Enemy {
	
	public static final Color locustColor = new Color(105,139,34);
	
	public LocustSpawnBoss(List<Component> components) {
		super(components, new Point2D(
					Board.BOARD_SIZE/2f,
					(float) -(9*Mech.MECH_RADIUS*Math.sin(Math.toRadians(60)))),
				new Point2D(0, 1), 200, Mech.MECH_SPEED/2f);
	}
		
	public static Enemy makeBoss() {
		
		List<Component> components = new ArrayList<Component>();
		
		// head
		Component head = new Component(
				Shape.scale(DefaultShapes.basicSquare,Mech.MECH_RADIUS*1.5f),
				new Point2D(),-1);
		head.setColour(locustColor);
		head.setIndestructable(true);
		components.add(head);
		
		LocustSpawnBoss e = new LocustSpawnBoss(components);
		e.setBase(true);
				
		// behaviour
		e.setAttackCycle(new MechAttack());
		e.setBehaviour(e.new LocustSpawnBehaviour());
		
		// start position
		e.getPos().y = -e.getMaxY();
		
		return e;
	}
	
	@Override
	public void damage(float amount, Component component, Board board) {
		super.damage(amount, component, board);
		((LocustSpawnBehaviour)getBehaviour()).damage();
	}
	
	class LocustSpawnBehaviour implements MechBehaviour
	{	
		
		final float WAIT_TIME = 250;
		float timer;
		float totalAngle = 0;
		int state = 0;
		int dir = 1;
		
		Component head;
		
		int text = 0;
		String conversation[] = {
				"RT:5%GA",
				"...",
				"X3H[TO/GREET?]X4",
				"...",
				"5g%[TO/CONSUME?]",
				"[POSSESSIVE][YOUTH?]",
				"[TO/REGRET]/[TO/PREPARE?]",
				"[SILENCE?]",
				"[TO/PLEAD]"
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
				if(mech.getPos().y >= Board.BOARD_SIZE/2f) {
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
						state = 7;
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
				// start splitting
				head.setColour(GameGUI.wreckage);
				for(int l=0;l<6;l++) {
				for(int i=0;i<4;i++) {
					Component c = new Component(Shape.scale(DefaultShapes.basicSquare, 3*Mech.MECH_RADIUS/4f), new Point2D(), 8);
					c.setColour(locustColor);
					c.setShowHealth(true);
					c.getPos().x = (i%2)*3*Mech.MECH_RADIUS/4f - 3*MECH_RADIUS/8f;
					c.getPos().y = (i/2)*3*Mech.MECH_RADIUS/4f - 3*MECH_RADIUS/8f;
					mech.getComponents().add(c);
				}};
				state++;
			case 3:
				// create weapon
				Bullet b = new Bullet(mech, true, Shape.scale(DefaultShapes.basicHex,Bullet.BULLET_RADIUS), new Point2D(), new Point2D());
				b.getMods().put(BulletMod.GARGANTUA, 1);
				b.setSpeed(Bullet.BULLET_SPEED/3f);
				b.setRange(Bullet.BULLET_RANGE*3f);
				Weapon w = new Weapon(b,30,ShotType.TARGETEDBEAM);
				w.setFireDelay(60);
				head.setWeapon(w);
				// attack cycle
				Component[] wc = new Component[1];
				wc[0] = head;
				mech.getAttackCycle().addVolley(mech.getAttackCycle().new Volley(wc,0,10));
				state++;
				break;
			case 4:
				// move apart
				for(int i=1;i<mech.getComponents().size();i++) {
					Component c = mech.getComponents().get(i);
					c.getPos().x += (1 + (i-1)/4)*dt*Mech.MECH_SPEED*Math.signum(c.getPos().x)/4f;
					c.getPos().y += (1 + (i-1)/4)*dt*Mech.MECH_SPEED*Math.signum(c.getPos().y)/4f;
				}
				if(Point2D.magnitude(mech.getComponents().get(1).getPos())>MECH_RADIUS*3)
					state++;
				break;
			case 5:
				// shoot and rotate 360
				mech.getAttackCycle().tick(dt, board);
				float rt = dt*0.5f;
				if(totalAngle+dt>360) {
					rt = rt - (totalAngle+rt)%360;
					timer = 0;
					state++;
				}
				totalAngle = (totalAngle+rt)%360;
				for(Component c: mech.getComponents()) {
					c.getShape().rotate(dir*rt);
					c.getPos().rotate(dir*rt);
				}
				checkDestruction(mech);
				break;
			case 6:
				// check destruction
				checkDestruction(mech);
				// shoot
				mech.getAttackCycle().tick(dt, board);
				if(timer > WAIT_TIME) {
					dir = dir*-1;
					state--;
				}
				break;
			case 7:
				if(timer > WAIT_TIME/2f)
					state++;
				break;
			case 8:
				// move away
				mech.getDirection().x = 0;
				mech.getDirection().y = -1;
				mech.setSpeed(Mech.MECH_SPEED);
				// text
				WordManager.addWord(
						"[TO/REGRET]",
						mech.getPos(),
						(int)(WAIT_TIME-20));
				state++;
				break;
			}
		}

		public void damage() {
			if(state==1 && text>0)
				state++;
		}
		
		private void checkDestruction(Mech mech) {
			boolean alive = false;
			for(Component c: mech.getComponents()) {
				if(c==head) continue;
				if(!c.isDestroyed()) {
					alive = true;
					break;
				}
			}
			if(!alive) {
				Iterator<Component> cit = mech.getComponents().iterator();
				while(cit.hasNext()) {
					Component c = cit.next();
					if(c.isDestroyed()) {
						cit.remove();
						Point2D p = new Point2D(
								mech.getPos().x + c.getPos().x,
								mech.getPos().y + c.getPos().y);
						ParticleManager.makeExplosion(p);
					}
				}
				timer = 0;
				state = 7;
			}
		}	
	}
}