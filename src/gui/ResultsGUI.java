package gui;

import static core.Frame.SCREEN_SIZE;
import static gui.OpenGLDraw.fillRect;
import gui.FontManager.FontType;
import gui.TrueTypeFont.FontAlign;
import gui.ui.HudOverlay;
import gui.ui.TextElement;

import java.awt.Color;

import monitor.StatCounter;
import phys.DefaultShapes;
import phys.Point2D;
import core.FileManager.LevelScore;
import core.Frame;
import core.controller.ShipController;

public class ResultsGUI {

	Frame frame;
	HudOverlay overlay;
		
	/* colour pallete */
	static final Color back = new Color(80,80,80);
	public static final Color energy = new Color(224,255,255);
	public static final Color[] player = {new Color(0,110,255),new Color(6,200,0)};
	public static final Color enemy = new Color(160,30,30);
	
	/* offset to center the screen when full-screen */
	float offX = 0;
	float textOffX;
	float offY = 0;
	float panelWidth;
	float panelHeight;
	float indent;
	
	int timer = 0;
	float distance = 0;
	int score = 0;
	int[] damageDealt = new int[2];
	int enemiesDestroyed = 0;
	int[] asteroidsBroken = new int[2];
	int[] bulletsFired = new int[2];
	int[] damageTaken = new int[2];
	int fireDamage = 0;
	
	public ResultsGUI(Frame frame, HudOverlay overlay) {
		
		this.frame = frame;
		this.overlay = overlay;
		
		panelHeight = SCREEN_SIZE[1]*3/4f;
		panelWidth = SCREEN_SIZE[1];
		indent = SCREEN_SIZE[0]/400f;

		offX = SCREEN_SIZE[0]/2f - panelWidth/2f;
		offY = SCREEN_SIZE[1]/10f;
		textOffX = FontManager.getFont(FontType.FONT_32).getWidth("ASTEROIDS DESTROYED ")*Frame.FONT_SCALE + offX + indent*4;
		float textOffX2 = textOffX + indent*8;
		float textOffX3 = FontManager.getFont(FontType.FONT_32).getWidth("000000000 ")*Frame.FONT_SCALE + textOffX2;
		float textOffX4 = offX + panelWidth - indent*4;
		
		// text panels
		float baseline = offY + 4*indent;
		TextElement text = new TextElement("score", DefaultShapes.pointShape, new Point2D(textOffX4,baseline), "HIGH SCORE ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		
		baseline += FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE + indent;
		text = new TextElement("score", DefaultShapes.pointShape, new Point2D(textOffX,baseline), "SCORE ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		text = new TextElement("score0", DefaultShapes.pointShape, new Point2D(textOffX2,baseline), " ", enemy, FontType.FONT_32);
		overlay.addElement(text);
		
		text = new TextElement("high_score", DefaultShapes.pointShape, new Point2D(textOffX4,baseline), " ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		
		baseline += FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE + indent;
		text = new TextElement("enemy", DefaultShapes.pointShape, new Point2D(textOffX,baseline), "ENEMIES DESTROYED ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		text = new TextElement("enemy0", DefaultShapes.pointShape, new Point2D(textOffX2,baseline), " ", enemy, FontType.FONT_32);
		overlay.addElement(text);
		
		text = new TextElement("high_enemies", DefaultShapes.pointShape, new Point2D(textOffX4,baseline), " ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		
		baseline += FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE + indent;
		text = new TextElement("dealt", DefaultShapes.pointShape, new Point2D(textOffX,baseline), "DAMAGE ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		text = new TextElement("dealt0", DefaultShapes.pointShape,new Point2D(textOffX2,baseline), " ", player[0], FontType.FONT_32);
		overlay.addElement(text);

		text = new TextElement("dealt1", DefaultShapes.pointShape,new Point2D(textOffX3,baseline), " ", player[1], FontType.FONT_32);
		overlay.addElement(text);
		
		baseline += FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE + indent;
		text = new TextElement("asteroids", DefaultShapes.pointShape, new Point2D(textOffX,baseline), "ASTEROIDS DESTROYED ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		text = new TextElement("asteroids0", DefaultShapes.pointShape, new Point2D(textOffX2,baseline), " ", player[0], FontType.FONT_32);
		overlay.addElement(text);

		text = new TextElement("asteroids1", DefaultShapes.pointShape, new Point2D(textOffX3,baseline), " ", player[1], FontType.FONT_32);
		overlay.addElement(text);
		
		baseline += FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE + indent;
		text = new TextElement("projectile", DefaultShapes.pointShape, new Point2D(textOffX,baseline), "PROJECTILES FIRED ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		text = new TextElement("projectile0", DefaultShapes.pointShape, new Point2D(textOffX2,baseline), " ", player[0], FontType.FONT_32);
		overlay.addElement(text);

		text = new TextElement("projectile1", DefaultShapes.pointShape, new Point2D(textOffX3,baseline), " ", player[1], FontType.FONT_32);
		overlay.addElement(text);
		
		baseline += FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE + indent;
		text = new TextElement("taken", DefaultShapes.pointShape, new Point2D(textOffX,baseline), "DAMAGE TAKEN ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		text = new TextElement("taken0", DefaultShapes.pointShape, new Point2D(textOffX2,baseline), " ", player[0], FontType.FONT_32);
		overlay.addElement(text);

		text = new TextElement("taken1", DefaultShapes.pointShape, new Point2D(textOffX3,baseline), " ", player[1], FontType.FONT_32);
		overlay.addElement(text);
		
		baseline += FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE + indent;
		text = new TextElement("fire", DefaultShapes.pointShape, new Point2D(textOffX,baseline), "FIRE DAMAGE ", energy, FontType.FONT_32, FontAlign.ALIGN_RIGHT);
		overlay.addElement(text);
		text = new TextElement("fire0", DefaultShapes.pointShape, new Point2D(textOffX2,baseline), " ", enemy, FontType.FONT_32);
		overlay.addElement(text);
	}
	
	public void draw(StatCounter lastResults, LevelScore levelScore) {
				
		if(timer<120)
			timer++;
		
		distance = lastResults.getDistance()*timer/120f;
		score = (int) (lastResults.getScore()*timer/120f);
		enemiesDestroyed = (int) (lastResults.getEnemiesDestroyed()*timer/120f);
		damageDealt[0] = (int) (lastResults.getDamageDealt()[0]*timer/120f);
		damageDealt[1] = (int) (lastResults.getDamageDealt()[1]*timer/120f);
		asteroidsBroken[0] = (int) (lastResults.getAsteroidsBroken()[0]*timer/120f);
		asteroidsBroken[1] = (int) (lastResults.getAsteroidsBroken()[1]*timer/120f);
		bulletsFired[0] = (int) (lastResults.getBulletsFired()[0]*timer/120f);
		bulletsFired[1] = (int) (lastResults.getBulletsFired()[1]*timer/120f);
		damageTaken[0] = (int) (lastResults.getDamageTaken()[0]*timer/120f);
		damageTaken[1] = (int) (lastResults.getDamageTaken()[1]*timer/120f);
		fireDamage = (int) (lastResults.getFireDamage()*timer/120f);
				
		fillRect(Color.BLACK,
				offX,
				offY,
				panelWidth,
				panelHeight);
		
		fillRect(energy,
				offX + indent,
				offY + indent,
				panelWidth - 2*indent,
				panelHeight - 2*indent);
		
		fillRect(Color.BLACK,
				offX + 2*indent,
				offY + 2*indent,
				panelWidth - 4*indent,
				panelHeight - 4*indent);
		
		OpenGLDraw.drawLine(energy,
				textOffX + indent*4, offY + indent,
				textOffX + indent*4, offY + panelHeight - indent);
		
		// text panels
		((TextElement)overlay.getElement("score0")).setText(""+score);
		((TextElement)overlay.getElement("enemy0")).setText(""+enemiesDestroyed);
		((TextElement)overlay.getElement("dealt0")).setText(""+damageDealt[0]);
		((TextElement)overlay.getElement("dealt1")).setText(""+damageDealt[1]);
		((TextElement)overlay.getElement("asteroids0")).setText(""+asteroidsBroken[0]);
		((TextElement)overlay.getElement("asteroids1")).setText(""+asteroidsBroken[1]);
		((TextElement)overlay.getElement("projectile0")).setText(""+bulletsFired[0]);
		((TextElement)overlay.getElement("projectile1")).setText(""+bulletsFired[1]);
		((TextElement)overlay.getElement("taken0")).setText(""+damageTaken[0]);
		((TextElement)overlay.getElement("taken1")).setText(""+damageTaken[1]);
		((TextElement)overlay.getElement("fire0")).setText(""+fireDamage);
		
		if(levelScore.score == lastResults.getScore())
			((TextElement)overlay.getElement("high_score")).setColour(enemy);
		((TextElement)overlay.getElement("high_score")).setText(""+levelScore.score+" ");
		
		if(levelScore.enemiesDestroyed == lastResults.getEnemiesDestroyed())
			((TextElement)overlay.getElement("high_enemies")).setColour(enemy);
		((TextElement)overlay.getElement("high_enemies")).setText(""+levelScore.enemiesDestroyed+" ");
	}
	
	public void drawCursors(ShipController[] controllers, Point2D[] positions) {
		for(int i=0;i<2;i++) {
			if(controllers[i]!=null) {
				Texture tex = TextureLoader.getInstance().getTexture("p"+i+"-cursor");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						positions[i].x,
						positions[i].y,
						32,48);
				OpenGLDraw.unbindTexture();
			}
		}
	}
}
