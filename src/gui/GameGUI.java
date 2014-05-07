package gui;

import static core.Frame.SCREEN_SIZE;
import static gui.OpenGLDraw.drawPoly;
import static gui.OpenGLDraw.drawRect;
import static gui.OpenGLDraw.fillPoly;
import static gui.OpenGLDraw.fillRect;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import gui.FontManager.FontType;

import java.awt.Color;
import java.util.List;

import mission.Board;
import mission.ParticleManager;
import mission.ParticleManager.Particle;
import mission.SupportFactory;
import mission.boss.BuilderBaseBoss;
import mission.effects.ComponentImage;
import mission.effects.Joint;
import mission.effects.Laser;
import mission.effects.Word;
import mission.effects.WordManager;
import mission.gameobject.AllyMech;
import mission.gameobject.Asteroid;
import mission.gameobject.Base;
import mission.gameobject.Component;
import mission.gameobject.ComponentShapes;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;
import mission.gameobject.Player;
import mission.powers.Power;
import mission.powers.PowerEater.ActivePower;
import mission.weapon.bullet.Bullet;

import org.lwjgl.opengl.GL11;

import phys.DefaultShapes;
import phys.Point2D;
import core.controller.KeyboardController;
import core.controller.MouseController;
import core.controller.ShipController;
import core.controller.XboxController;

/**
 * Draws the game to the screen.
 * Feel free to replace with a prettier version.
 * @author Michael Cashmore
 */
public class GameGUI {
	
	/* size of the view window in terms of game size units */
	public static float VIEW_SIZE = Board.BOARD_SIZE;
	
	/* colour pallete */
	static final Color back = new Color(80,80,80);
	
	public static final Color enemyLight = new Color(200,30,30);
	public static final Color enemy = new Color(160,30,30);
	public static final Color enemyDark = new Color(120,30,30);
	public static final Color enemyDarkest = new Color(80,30,30);
	
	public static final Color wreckage = new Color(55,55,55);
	public static final Color asteroid = new Color(155,155,155);
	
	public static final Color energy = new Color(224,255,255);
	public static final Color[] playerLight = {new Color(0,191,255),new Color(6,250,0)};
	public static final Color[] player = {new Color(0,110,255),new Color(6,200,0)};
	public static final Color[] playerDark = {new Color(0,60,255),new Color(6,183,0)};
	
	public static final Color sky = new Color(135,206,250);
	
	static final Color powerdepleted = new Color(178,34,34);
	static final Color powercharging = new Color(55,55,55,155);
	static final Color powerready = new Color(50,205,50);
	
	/* ratio of SCREEN_SIZE/VIEW_SIZE */
	public static float scale;
	
	/* offset to center the screen when full-screen */
	public static float offX = 0;
	public static float offY = 0;
	public static float hudSize = 0;
	
	/* visual stuff */
	float timer=0;
	float backTimer = 0;
	
	/**
	 * Draw cursors on the screen
	 * @param powerSelection
	 */
	public void drawCursors(ShipController controller, int playerColor) {
		Texture tex = TextureLoader.getInstance().getTexture("p"+playerColor+"-cursor");
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTexture(
				controller.getCursorPosition().x,
				controller.getCursorPosition().y,
				32,48);
		OpenGLDraw.unbindTexture();
	}
	
	/**
	 * Draw the game.
	 * @param board	the model to be drawn
	 * @param drawHUD	true if the size-bars are to be drawn
	 */
	public void draw(Board board, float dt) {

		scale = (SCREEN_SIZE[1]-hudSize)/VIEW_SIZE;
		if(SCREEN_SIZE[0]/VIEW_SIZE < scale) {
			scale = SCREEN_SIZE[0]/VIEW_SIZE;
			offY = (SCREEN_SIZE[1] - VIEW_SIZE*scale)/2f;
			offX = 0f;
		} else {
			offY = 0f;
			offX = (SCREEN_SIZE[0] - VIEW_SIZE*scale)/2f;
		}
		hudSize = (VIEW_SIZE*scale)/10;
		offY += hudSize;
		
		// draw background
		Texture tex;
		fillRect(Color.BLACK, 0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);
		
		timer = (timer+dt)%720;
		
		if(!board.getMap().isTimerPaused())
			backTimer = (backTimer+dt)%720;
		
		switch(board.getMap().getLevelType()) {
		case BUILDERCOLONY:
			tex = TextureLoader.getInstance().getTexture("orange_back");
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(offX, offY, VIEW_SIZE*scale, VIEW_SIZE*scale);
			OpenGLDraw.unbindTexture();
			break;
		case THEHIVE:
			tex = TextureLoader.getInstance().getTexture("star_back");
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(offX, offY+VIEW_SIZE*scale*backTimer/720f, VIEW_SIZE*scale, VIEW_SIZE*scale);
			OpenGLDraw.drawTexture(offX, offY-VIEW_SIZE*scale*(1-backTimer/720f), VIEW_SIZE*scale, VIEW_SIZE*scale);
			OpenGLDraw.unbindTexture();
			break;
		case LOCUSTSWARM:
			tex = TextureLoader.getInstance().getTexture("green_back");
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(offX, offY+VIEW_SIZE*scale*backTimer/720f, VIEW_SIZE*scale, VIEW_SIZE*scale);
			OpenGLDraw.drawTexture(offX, offY-VIEW_SIZE*scale*(1-backTimer/720f), VIEW_SIZE*scale, VIEW_SIZE*scale);
			OpenGLDraw.unbindTexture();
			break;
		default:
			tex = TextureLoader.getInstance().getTexture("purple_back");
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(offX, offY+VIEW_SIZE*scale*backTimer/720f, VIEW_SIZE*scale, VIEW_SIZE*scale);
			OpenGLDraw.drawTexture(offX, offY-VIEW_SIZE*scale*(1-backTimer/720f), VIEW_SIZE*scale, VIEW_SIZE*scale);
			OpenGLDraw.unbindTexture();
			break;
		}
		
		// images
		for(ComponentImage ci: board.getImages()) {
			drawImage(ci);
		}
		
		switch(board.getMap().getLevelType()) {
		case BUILDERCOLONY:
			for(int i=0;i<9;i++) {
				fillRect(BuilderBaseBoss.builderBossBackground,
						offX + (i*Board.BOARD_SIZE/8f - Mech.MECH_RADIUS/4f)*scale,
						offY, Mech.MECH_RADIUS/2f*scale,Board.BOARD_SIZE*scale);
				drawRect(Color.BLACK,
						offX + (i*Board.BOARD_SIZE/8f - Mech.MECH_RADIUS/4f)*scale,
						offY, Mech.MECH_RADIUS/2f*scale,Board.BOARD_SIZE*scale);
			}
			break;
		default:
			break;
		}
		
		// mechs
		for(Mech mech: board.getMechs()) 
			drawMech(board, mech, -1);
		for(Mech mech: board.getNonCompetingMechs()) 
			drawMech(board, mech, -1);
		if((Math.floor(timer/10))%2==0) {
			for(Mech mech: board.getMechRespawnQueue().keySet())
				drawMech(board, mech, -1);
		}
		
		// player backgrounds
		for(Player p: board.getPlayers()) {
			drawPlayerBackground(board, p);
		}
		
		for(Player p: board.getRespawnQueue().keySet()) {
			if((Math.floor(timer/10))%2==0)
				drawPlayerBackground(board, p);
		}
		
		// base
		for(Base b: board.getBase()) {
			drawBase(b);
		}
		
		// asteroids
		for(Asteroid a: board.getAsteroids()) {
			fillPoly(asteroid, (a.getPos().x)*scale+offX, (a.getPos().y)*scale+offY, a.getShape(), scale);
			drawPoly(Color.BLACK, (a.getPos().x)*scale+offX, (a.getPos().y)*scale+offY, a.getShape(), scale);
		}
				
		// enemy
		for(Mech mech: board.getEnemies()) {
			drawMech(board, mech, 1);
		}
		
		// particles
		for(Particle p: ParticleManager.getParticles()) {
			fillPoly(p.getColour(), p.getPos().x*scale+offX, p.getPos().y*scale+offY, p.getShape(), scale);
		}
		
		// friendly bullets
		for(Bullet b: board.getFriendlyBullets()) {
			drawBullet(b);
		}
				
		// bullets
		for(Bullet b: board.getEnemyBullets()) {
			drawBullet(b);
		}
		
		// players again
		for(Player p: board.getPlayers()) {
			drawPlayer(board, p);
		}
		
		for(Player p: board.getRespawnQueue().keySet()) {
			if((Math.floor(timer/10))%2==0)
				drawPlayer(board, p);
			FontManager.getFont(FontType.FONT_24).drawString(Color.WHITE,
					(int)(p.getPos().x*scale+offX),
					(int)(p.getPos().y*scale+offY),
					(int)(board.getRespawnQueue().get(p)/10)+"",
					1,-1);
		}
		
		// lasers
		for(Laser l: board.getLasers())
			drawLaser(l);
		for(Laser l: board.getEnemyLasers())
			drawLaser(l);
		
		// words
		WordManager.tick();
		for(Word w: WordManager.getWords()) {
			if(w.back)
				fillRect(Color.BLACK,
						(int)(offX + w.pos.x*scale),
						(int)(offY + w.pos.y*scale) - FontManager.getFont(FontType.FONT_24).getHeight(),
						FontManager.getFont(FontType.FONT_24).getWidth(w.word) + FontManager.getFont(FontType.FONT_24).getHeight(),
						FontManager.getFont(FontType.FONT_24).getHeight());
			FontManager.getFont(w.fontType).drawString(Color.WHITE,
					(int)(offX + w.pos.x*scale),
					(int)(offY + w.pos.y*scale),
					w.word, 1, -1);
		}
		
		// top bar
		fillRect(energy, 0, 0, SCREEN_SIZE[0], offY);
		fillRect(Color.BLACK, offX-3, offY-12, VIEW_SIZE*scale + 6, 12);
		fillRect(enemyLight, offX, offY-9,
				(VIEW_SIZE*scale)*(board.getMap().getTimer()/board.getMap().getFinalEventTime()),
				6);
		
		// side bars
		fillRect(wreckage, 0, 0, offX, SCREEN_SIZE[1]);
		fillRect(Color.BLACK, offX-3, 0, 3, SCREEN_SIZE[1]);
		fillRect(wreckage, SCREEN_SIZE[0]-offX, 0, offX, SCREEN_SIZE[1]);
		fillRect(Color.BLACK, SCREEN_SIZE[0]-offX, 0, 3, SCREEN_SIZE[1]);
	}

	private void drawPlayerBackground(Board board, Player p) {

		// base image
		fillPoly(playerDark[p.getId()], (p.getPos().x)*scale+offX, (p.getPos().y)*scale+offY, ComponentShapes.playerBase, scale);
		drawPoly(Color.BLACK, (p.getPos().x)*scale+offX, (p.getPos().y)*scale+offY, ComponentShapes.playerBase, scale);
		
	}

	private void drawImage(ComponentImage ci) {
		for(Component c: ci.getImage()) {
			fillPoly(c.getColour(),
					offX + scale*(ci.getPos().x + c.getPos().x),
					offY + scale*(ci.getPos().y + c.getPos().y),
					c.getShape(), scale);
			drawPoly(Color.BLACK,
					offX + scale*(ci.getPos().x + c.getPos().x),
					offY + scale*(ci.getPos().y + c.getPos().y),
					c.getShape(), scale);
		}
	}

	private void drawBase(Base b) {
		fillPoly(wreckage,
				offX + scale*b.getPos().x,
				offY + scale*b.getPos().y,
				b.getShape(), scale);
		drawPoly(Color.BLACK,
				offX + scale*b.getPos().x,
				offY + scale*b.getPos().y,
				b.getShape(), scale);
	}

	private void drawBullet(Bullet b) {
		
		Color c = player[0];
		if(b.isEnemyBullet())
			c = Color.RED;
		else if(b.getParent() instanceof Player)
			c = player[((Player)(b.getParent())).getPlayerNumber()];
		else if(b.getParent() instanceof AllyMech)
			c = player[((AllyMech)(b.getParent())).getMaster().getPlayerNumber()];
		
		// smoother
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		Texture tex = TextureLoader.getInstance().getTexture("bullet_back");
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTexture(c,
				(b.getPos().x + b.getMinX()*2f)*scale+offX,
				(b.getPos().y + b.getMinY()*2f)*scale+offY,
				(b.getMaxX() - b.getMinX())*scale*2f,
				(b.getMaxY() - b.getMinY())*scale*2f);
		OpenGLDraw.unbindTexture();
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		fillPoly(energy, (b.getPos().x)*scale+offX, (b.getPos().y)*scale+offY, b.getShape(), scale);
	}

	private void drawLaser(Laser l) {
		
		Texture tex;
		
		// smoother lasers
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		
		float length = l.length()-2*l.width;
		float rotation = (float)-Math.toDegrees(Math.atan2((l.end.x - l.start.x),(l.end.y - l.start.y)));
		float fade = 1f - ((l.duration-2f*l.timer)*(l.duration-2f*l.timer))/(l.duration*l.duration);
		Color overlay = new Color(1f,1f,1f,fade);
		Color laserback = new Color(l.r,l.g,l.b,fade);
			
		// middle
		tex = TextureLoader.getInstance().getTexture("laser_middle_back");
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTextureWithRotation(laserback,
				offX + l.start.x*scale,
				offY + l.start.y*scale,
				2*l.width*scale,
				length*scale,
				-l.width*scale,
				l.width*scale,
				rotation);
		OpenGLDraw.unbindTexture();
		
		if(l.bright) {
			tex = TextureLoader.getInstance().getTexture("laser_middle_overlay");
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTextureWithRotation(overlay,
					offX + l.start.x*scale,
					offY + l.start.y*scale,
					2*l.width*scale,
					length*scale,
					- l.width*scale,
					l.width*scale,
					rotation);
			OpenGLDraw.unbindTexture();
		}
		
		// start
		tex = TextureLoader.getInstance().getTexture("laser_start_back");
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTextureWithRotation(laserback,
				offX + l.start.x*scale,
				offY + l.start.y*scale,
				2*l.width*scale,
				2*l.width*scale,
				- l.width*scale,
				- l.width*scale,
				rotation);
		OpenGLDraw.unbindTexture();
		
		if(l.bright) {
			tex = TextureLoader.getInstance().getTexture("laser_start_overlay");
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTextureWithRotation(overlay,
					offX + l.start.x*scale,
					offY + l.start.y*scale,
					2*l.width*scale,
					2*l.width*scale,
					-l.width*scale,
					-l.width*scale,
					rotation);
			OpenGLDraw.unbindTexture();
		}
		
		// end
		rotation += 180;
		if(l.doubleEnded)
			tex = TextureLoader.getInstance().getTexture("laser_start_back");
		else 
			tex = TextureLoader.getInstance().getTexture("laser_end_back");
		
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTextureWithRotation(laserback,
				offX + (l.end.x)*scale,
				offY + (l.end.y)*scale,
				2*l.width*scale,
				2*l.width*scale,
				-l.width*scale,
				-l.width*scale,
				rotation);
		OpenGLDraw.unbindTexture();
		
		if(l.bright) {
			if(l.doubleEnded)
				tex = TextureLoader.getInstance().getTexture("laser_start_overlay");
			else 
				tex = TextureLoader.getInstance().getTexture("laser_end_overlay");
			
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTextureWithRotation(overlay,
					offX + (l.end.x)*scale,
					offY + (l.end.y)*scale,
					2*l.width*scale,
					2*l.width*scale,
					-l.width*scale,
					-l.width*scale,
					rotation);
			OpenGLDraw.unbindTexture();
		}
		
		
		// back to normal
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public void drawHud(ShipController sc, Player player, Board board) {
		
		float sx = Math.max(0,offX-hudSize) + (hudSize + VIEW_SIZE*scale)*player.getPlayerNumber();
		float indent = hudSize/20f;
		int i=0;
		
		for(;i<4 && i<player.getPowerEater().getActivePowers().size();i++) {
			
			ActivePower ap = player.getPowerEater().getActivePowers().get(i);
			if(!ap.type.active) break;

			if(sc instanceof MouseController) {
				
				if(player.getMouseSelection()==i) {
					Texture tex = TextureLoader.getInstance().getTexture("mouse_"+player.getPlayerNumber());
					OpenGLDraw.bindTexture(tex);
					OpenGLDraw.drawTexture(sx+hudSize*(player.getPlayerNumber()*2-1), SCREEN_SIZE[1]-hudSize*(i+1), hudSize, hudSize);
					OpenGLDraw.unbindTexture();
				}
				
			} else {
				String prefix = "";
				if(sc instanceof XboxController)
					prefix = "controller_"+player.getPlayerNumber()+"_";
				if(sc instanceof KeyboardController)
					prefix = "keyboard_";
				
				Texture tex = TextureLoader.getInstance().getTexture(prefix+i);
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(sx+hudSize*(player.getPlayerNumber()*2-1), SCREEN_SIZE[1]-hudSize*(i+1), hudSize, hudSize);
				OpenGLDraw.unbindTexture();
			}
			
			// image background
			fillRect(Color.BLACK,
					sx,
					SCREEN_SIZE[1]-hudSize*(i+1),
					hudSize,hudSize);
			fillRect(GameGUI.player[player.getPlayerNumber()],
					sx + indent,
					SCREEN_SIZE[1]-hudSize*(i+1) + indent,
					hudSize - indent*2,
					hudSize - indent*2);
			fillRect(Color.BLACK,
					sx + 2*indent,
					SCREEN_SIZE[1]-hudSize*(i+1) + 2*indent,
					hudSize - indent*4,
					hudSize - indent*4);
			
			drawPowerIcon(ap.type.id,
					sx + 3*indent,
					SCREEN_SIZE[1]-hudSize*(i+1) + 3*indent,
					hudSize - indent*6,
					hudSize - indent*6);
			
			// cool-down			
			if(ap.remainingCooldown>0) {
				fillRect(powercharging,
						sx + 3*indent,
						SCREEN_SIZE[1]-hudSize*(i+1) + 3*indent,
						hudSize - indent*6,
						(hudSize - indent*6)*(ap.remainingCooldown/ap.type.coolDown));
				fillRect(energy,
						sx + 3*indent,
						SCREEN_SIZE[1]-hudSize*(i+1) + 3*indent + (hudSize - indent*6)*(ap.remainingCooldown/ap.type.coolDown),
						hudSize - indent*6,
						indent);
			}
		}
		
		if(player.getShield()!=null) {
			float height = (hudSize*5);
			fillRect(Color.BLACK,
					sx,
					SCREEN_SIZE[1]-hudSize*(i)- height,
					hudSize,
					hudSize*5);
			fillRect(GameGUI.player[player.getPlayerNumber()],
					sx + indent,
					SCREEN_SIZE[1]-hudSize*(i) + indent - height,
					hudSize - indent*2,
					hudSize*5 - indent*2);
			fillRect(Color.BLACK,
					sx + indent*2,
					SCREEN_SIZE[1]-hudSize*(i) + indent*2 - height,
					hudSize - indent*4,
					hudSize*5 - indent*4);
			height = (hudSize*5 - indent*6)*(player.getShield().getCurrentCapacity()/player.getShield().getMaxCapacity());
			fillRect(energy,
					sx + indent*3,
					(SCREEN_SIZE[1]-hudSize*(i) - height - indent*3),
					hudSize - indent*5,
					height);
		}
	}

	private void drawPowerIcon(int p, float x, float y, float w, float h) {

		Texture tex = TextureLoader.getInstance().getTexture("icon_"+Power.values()[p].image);
		if(tex==null)
			tex = TextureLoader.getInstance().getTexture("icon_temppower");
		if(tex==null) {
			tex = TextureLoader.getInstance().getTexture("powerBlank");
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(x, y, w, h);
			OpenGLDraw.unbindTexture();
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(x, y, w, h);
			OpenGLDraw.unbindTexture();
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}
		
	}

	private void drawPlayer(Board board, Player p) {
		
		drawMechShield(p);
		
		// little square
		drawWeapon(board, p, p.getComponents().get(0), -1);
		drawMechComponent(p, p.getComponents().get(0), false, false);
		
		// rest of body
		for(int i=1;i<p.getComponents().size();i++) {
			drawMechComponent(p, p.getComponents().get(i), false, false);
			drawWeapon(board, p, p.getComponents().get(i), -1);
		}
		
		// special HUD
		for(ActivePower ap: p.getPowerEater().getActivePowers()) {
			// defence laser range
			if(ap.type==Power.DEFENCELASER && ap.remainingActive>0)
				OpenGLDraw.drawArc(energy,
						(p.getPos().x)*scale+offX,
						(p.getPos().y)*scale+offY,
						 scale*Mech.MECH_RADIUS*5,0,360);
		}
	}

	private void drawMech(Board board, Mech mech, int facing) {

		drawMechShield(mech);
		
		// joints
		for (Joint j : mech.getJoints()) {
			fillPoly(j.getColour(), (j.getPos().x+mech.getPos().x)*scale+offX, (j.getPos().y+mech.getPos().y)*scale+offY, j.getShape(), scale);
			drawPoly(Color.BLACK, (j.getPos().x+mech.getPos().x)*scale+offX, (j.getPos().y+mech.getPos().y)*scale+offY, j.getShape(), scale);
		}
		
		// body
		for(int i=0;i<mech.getComponents().size();i++) {
			drawMechComponent(mech, mech.getComponents().get(i), (i==0), mech.getComponents().get(i).showHealth());
		}
		
		for(Component c: mech.getComponents()) {
			if(!c.isDestroyed())
				drawWeapon(board,mech,c,facing);
		}
	}

	private void drawMechShield(Mech mech) {
		// shield
		if(mech.getShield()!=null) {
			for(int i=0;i<mech.getShield().getCurrentCapacity();i++) {
				drawPoly(energy,
						mech.getPos().x*scale+offX,
						mech.getPos().y*scale+offY,
						DefaultShapes.basicHex, Mech.MECH_RADIUS*(1+i/8f)*scale);
			}
		}
	}

	private void drawMechComponent(Mech mech, Component c, boolean heart, boolean showComponentHealth) {
		if(c.isDestroyed()) {
			fillPoly(wreckage, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
			drawPoly(Color.BLACK, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
		} else {
			if(heart && c.getHealth()<0) {
				fillPoly(wreckage, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
				drawPoly(Color.BLACK, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
				fillPoly(c.getColour(), (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(),
						scale*(9/10f)*(mech.getHealth()/(float)mech.getMaxHealth()));	
				drawPoly(Color.BLACK, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(),
						scale*(9/10f)*(mech.getHealth()/(float)mech.getMaxHealth()));
			} else if(showComponentHealth) {
				fillPoly(wreckage, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
				drawPoly(Color.BLACK, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
				fillPoly(c.getColour(), (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(),
						scale*(c.getHealth()/(float)c.getMaxHealth()));	
				drawPoly(Color.BLACK, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(),
						scale*(c.getHealth()/(float)c.getMaxHealth()));
			} else {
				fillPoly(c.getColour(), (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
				drawPoly(Color.BLACK, (c.getPos().x+mech.getPos().x)*scale+offX, (c.getPos().y+mech.getPos().y)*scale+offY, c.getShape(), scale);
			}
		}
	}

	private void drawWeapon(Board board, Mech mech, Component c, int dir) {
		Texture tex;
		Point2D p;
		if(c.getWeapon()!=null) {
			switch(c.getWeapon().getShotType()) {
			case STRAIGHTDUAL:
				tex = TextureLoader.getInstance().getTexture("weap_straightdual");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						(c.getPos().x+mech.getPos().x-Mech.MECH_RADIUS)*scale+offX,
						(c.getPos().y+mech.getPos().y-Mech.MECH_RADIUS*(-dir*5/4f))*scale+offY,
						2*Mech.MECH_RADIUS*scale, 2*Mech.MECH_RADIUS*scale*(-dir));
				OpenGLDraw.unbindTexture();
				break;
			case SPIRAL:
				tex = TextureLoader.getInstance().getTexture("weap_octo");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTextureWithRotation(Color.WHITE,
						(c.getPos().x+mech.getPos().x)*scale+offX,
						(c.getPos().y+mech.getPos().y)*scale+offY,
						2*Mech.MECH_RADIUS*scale, 2*Mech.MECH_RADIUS*scale,
						-Mech.MECH_RADIUS*scale, -Mech.MECH_RADIUS*scale,
						c.getWeapon().getVolleyTimer()*(3/8f));
				OpenGLDraw.unbindTexture();
				break;
			case FASTSPIRAL:
				tex = TextureLoader.getInstance().getTexture("weap_octo");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTextureWithRotation(Color.WHITE,
						(c.getPos().x+mech.getPos().x)*scale+offX,
						(c.getPos().y+mech.getPos().y)*scale+offY,
						2*Mech.MECH_RADIUS*scale, 2*Mech.MECH_RADIUS*scale,
						-Mech.MECH_RADIUS*scale, -Mech.MECH_RADIUS*scale,
						c.getWeapon().getVolleyTimer());
				OpenGLDraw.unbindTexture();
				break;
			case SPREAD:
				tex = TextureLoader.getInstance().getTexture("weap_spread");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						(c.getPos().x+mech.getPos().x-Mech.MECH_RADIUS)*scale+offX,
						(c.getPos().y+mech.getPos().y-Mech.MECH_RADIUS*(-dir*5/4f))*scale+offY,
						2*Mech.MECH_RADIUS*scale, 2*Mech.MECH_RADIUS*scale*(-dir));
				OpenGLDraw.unbindTexture();
				break;
			case TARGETEDBEAM:
			case DIRECTEDBEAM:
				float angle = (float) Math.toDegrees(Math.atan2(c.getWeapon().getAim().x,c.getWeapon().getAim().y));
				tex = TextureLoader.getInstance().getTexture("weap_directed");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTextureWithRotation(Color.WHITE,
						(c.getPos().x+mech.getPos().x)*scale+offX,
						(c.getPos().y+mech.getPos().y)*scale+offY,
						2*Mech.MECH_RADIUS*scale, -2*Mech.MECH_RADIUS*scale,
						-Mech.MECH_RADIUS*scale, Mech.MECH_RADIUS*scale,
						-angle);
				OpenGLDraw.unbindTexture();
				break;
			case STRAIGHT:
				tex = TextureLoader.getInstance().getTexture("weap_directed");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						(c.getPos().x+mech.getPos().x-Mech.MECH_RADIUS)*scale+offX,
						(c.getPos().y+mech.getPos().y-Mech.MECH_RADIUS*(-dir))*scale+offY,
						2*Mech.MECH_RADIUS*scale, 2*Mech.MECH_RADIUS*scale*(-dir));
				OpenGLDraw.unbindTexture();
				break;
			case ASTEROID:
				tex = TextureLoader.getInstance().getTexture("weap_asteroid");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						(c.getPos().x+mech.getPos().x-Mech.MECH_RADIUS)*scale+offX,
						(c.getPos().y+mech.getPos().y-Mech.MECH_RADIUS)*scale+offY,
						2*Mech.MECH_RADIUS*scale, 2*Mech.MECH_RADIUS*scale);
				OpenGLDraw.unbindTexture();
				if(mech.getAttackCycle().getCurrentComponents().contains(c) && mech.getAttackCycle().isPoweringUp()) {
					OpenGLDraw.drawArc(Color.LIGHT_GRAY,
							(c.getPos().x+mech.getPos().x)*scale+offX,
							(c.getPos().y+mech.getPos().y)*scale+offY,
							scale*Mech.MECH_RADIUS*
								(mech.getAttackCycle().getRemainingPowerupTime()/mech.getAttackCycle().getPowerupTime()),
							0,360);
				}
				break;
			case LASER:
				if(!mech.isBase()) {
					angle = (float) Math.toDegrees(Math.atan2(c.getWeapon().getAim().x,c.getWeapon().getAim().y));
					tex = TextureLoader.getInstance().getTexture("weap_laser");
					OpenGLDraw.bindTexture(tex);
					OpenGLDraw.drawTextureWithRotation(Color.WHITE,
							(c.getPos().x+mech.getPos().x)*scale+offX,
							(c.getPos().y+mech.getPos().y)*scale+offY,
							2*Mech.MECH_RADIUS*scale, 2*Mech.MECH_RADIUS*scale*(-dir),
							-Mech.MECH_RADIUS*scale, -Mech.MECH_RADIUS*scale*(-dir),
							-angle);
					OpenGLDraw.unbindTexture();
				}
				if(mech.getAttackCycle().getCurrentComponents().contains(c) && mech.getAttackCycle().isPoweringUp()) {
					for(Point2D target: c.getWeapon().getLaserTargets()) {
						
						float length = Point2D.magnitude(target);
						float rotation = (float)-Math.toDegrees(Math.atan2(target.x,target.y));
						Color laserback;
						switch(mech.getId()) {
						case 0: laserback = new Color(0f,0f,1f,.75f); break;
						case 1: laserback = new Color(0f,1f,0f,.75f); break;
						default: laserback = new Color(1f,0f,0f,.75f); break;
						}						
						// middle
						glBlendFunc(GL_SRC_ALPHA, GL_ONE);
						tex = TextureLoader.getInstance().getTexture("laser_middle_overlay");
						OpenGLDraw.bindTexture(tex);
						OpenGLDraw.drawTextureWithRotation(laserback,
								(c.getPos().x+mech.getPos().x)*scale+offX,
								(c.getPos().y+mech.getPos().y)*scale+offY,
								Laser.LASER_WIDTH*scale, length*scale,
								-Laser.LASER_WIDTH/2f*scale, 0,
								rotation);
						OpenGLDraw.unbindTexture();
						glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
					}	
				}
				break;
			case CLONERAY:
				if(mech instanceof Player) {
					// find mech target
					Mech e = null;					
					List<Enemy> targetList = board.getEnemies();
					float min = Float.MAX_VALUE;
					p = new Point2D();
					for(Mech target: targetList) {
						if(target.isBase())
							continue;
						p.x = (mech.getPos().x-target.getPos().x);
						p.y = (mech.getPos().y-target.getPos().y);
						if(Point2D.magnitude(p) < min && Point2D.magnitude(p) < Mech.MECH_RADIUS*10) {
							min = Point2D.magnitude(p);
							e = target;
						}
					}
					
					if(e!=null) {
						Color highlight = energy;
						if(!SupportFactory.isCloneable((Player)mech,e))
							highlight = enemyLight;
						
						OpenGLDraw.drawLine(highlight,
							(c.getPos().x+mech.getPos().x)*scale+offX,
							(c.getPos().y+mech.getPos().y)*scale+offY,
							(e.getPos().x)*scale+offX,
							(e.getPos().y)*scale+offY);
						for(Component cr: e.getComponents())
							drawPoly(highlight, (cr.getPos().x+e.getPos().x)*scale+offX, (cr.getPos().y+e.getPos().y)*scale+offY, cr.getShape(), scale);
					} else {
						OpenGLDraw.drawArc(energy,
								(c.getPos().x+mech.getPos().x)*scale+offX,
								(c.getPos().y+mech.getPos().y)*scale+offY,
								 scale*Mech.MECH_RADIUS*10,0,360);
					}
				}
				break;
			case IONBEAM:
				// find mech target
				for(Mech target: c.getWeapon().getBeamTargets()) {
					p = new Point2D((target.getPos().x - mech.getPos().x),(target.getPos().y - mech.getPos().y));
					float length = Point2D.magnitude(p);
					float rotation = (float)-Math.toDegrees(Math.atan2(p.x,p.y));
					Color laserback = new Color(1f,0f,0f,1f);
					
					// middle
					glBlendFunc(GL_SRC_ALPHA, GL_ONE);
					tex = TextureLoader.getInstance().getTexture("laser_middle_overlay");
					OpenGLDraw.bindTexture(tex);
					OpenGLDraw.drawTextureWithRotation(laserback,
							(c.getPos().x+mech.getPos().x)*scale+offX,
							(c.getPos().y+mech.getPos().y)*scale+offY,
							Laser.LASER_WIDTH*scale, length*scale,
							-Laser.LASER_WIDTH/2f*scale, 0,
							rotation);
					OpenGLDraw.unbindTexture();
					glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				}
				break;
			default:
				break;
			}
		}
	}
}
