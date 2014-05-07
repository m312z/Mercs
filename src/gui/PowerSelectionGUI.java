package gui;

import static core.Frame.SCREEN_SIZE;
import static gui.OpenGLDraw.fillRect;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import gui.FontManager.FontType;

import java.awt.Color;
import java.util.Iterator;

import mission.Board;
import mission.powers.Power;
import monitor.PowerShop;
import monitor.PowerShop.Challenge;

import org.lwjgl.opengl.GL11;

import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;
import core.Frame;
import core.menu.PowerSelectionScreen;

public class PowerSelectionGUI {
	
	/* size of the view window in terms of game size units */
	public static float VIEW_SIZE = Board.BOARD_SIZE;
	
	/* colour pallete */
	static final Color back = new Color(80,80,80);
	public static final Color[] playerDark = {new Color(0,60,255),new Color(6,183,0)};
	public static final Color energy = new Color(224,255,255);
	static final Color powercharging = new Color(55,55,55,155);
	
	/* ratio of SCREEN_SIZE/VIEW_SIZE */
	public static float scale;
	
	/* offset to center the screen when full-screen */
	public static float offX = 0;
	public static float offY = 0;
	public static float hudSize = 0;
	public static float panelWidth;
	public static float panelHeight;
	public static float slotSize;
	public static float indent;
	
	/* visual stuff */
	int timer=0;
	float[] pos = {0f,0f};
	
	public void draw(PowerSelectionScreen powerSelection) {
		
		hudSize = SCREEN_SIZE[0]/20f;
		panelWidth = SCREEN_SIZE[0]/3f - hudSize;
		indent = hudSize/20f;
		slotSize = (panelWidth-6*indent)/5f;
		panelHeight = 6*indent + slotSize * (float)Math.ceil(Power.values().length/5.0);
		
		// draw background
		fillRect(Color.BLACK, 0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);
		Texture tex = TextureLoader.getInstance().getTexture("stars");
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTexture(0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);
		OpenGLDraw.unbindTexture();
				
		FontManager.getFont(FontType.FONT_32).drawString(Color.WHITE, hudSize, hudSize,
				"CHOOSE: 4 ACTIVE POWERS / 6 TOTAL POWERS", Frame.FONT_SCALE, -Frame.FONT_SCALE);
		
		// draw challenges
		offX = SCREEN_SIZE[0]*(1/3f);
		offY = hudSize + panelHeight;
		drawChallengeSlots();
		
		// draw player sides
		for(int i=0;i<2;i++) {
			if(powerSelection.getControllers()[i]==null)
				continue;
			
			// power selection box
			offX = i*SCREEN_SIZE[0]*2/3f + (1-i)*hudSize;
			drawPowerSelectionBox(powerSelection, i);
			
			// power description box
			offX = SCREEN_SIZE[0]*(1/3f);
			drawPowerDescriptionBox(powerSelection, i);
			
			// currently selected powers boxes
			if(i==0) offX = hudSize - panelWidth + panelHeight;
			if(i==1) offX = SCREEN_SIZE[0]*2/3f;
			drawCurrentPowers(powerSelection, i);
		}
	}

	private void drawCurrentPowers(PowerSelectionScreen powerSelection, int i) {
		
		Texture tex;
		
		// border
		fillRect(Color.BLACK,
				offX,
				panelHeight + hudSize,
				slotSize*4f + indent*6,
				slotSize + indent*6);
		
		fillRect(playerDark[i],
				offX + indent,
				panelHeight + hudSize + indent,
				slotSize*4f + indent*4,
				slotSize + indent*4);
		
		fillRect(Color.BLACK,
				offX + 2*indent,
				panelHeight + hudSize + 2*indent,
				slotSize*4f + indent*2,
				slotSize + indent*2);
		
		int j=0;
		Iterator<Integer> pit = powerSelection.getPowers(i).iterator();
		while(pit.hasNext()) {
			int p = pit.next();
			if(Power.values()[p].active) {
				tex = TextureLoader.getInstance().getTexture("icon_"+Power.values()[p].image);
				if(tex==null)
					tex = TextureLoader.getInstance().getTexture("icon_temppower");
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						offX + 5*indent + j*slotSize,
						panelHeight + hudSize + 5*indent,
						slotSize-indent*4,slotSize-indent*4);
				OpenGLDraw.unbindTexture();
				
				tex = TextureLoader.getInstance().getTexture("icon_front");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						offX + 5*indent + j*slotSize,
						panelHeight + hudSize + 5*indent,
						slotSize-indent*4,slotSize-indent*4);
				OpenGLDraw.unbindTexture();
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				
				j++;
			}
		}
	}

	private void drawPowerDescriptionBox(PowerSelectionScreen powerSelection, int i) {
		
		Texture tex;
		// border
		fillRect(Color.BLACK,
				offX,
				hudSize + i*panelHeight/2f,
				SCREEN_SIZE[0]/3f,
				panelHeight/2f);
		
		fillRect(playerDark[i],
				offX + indent,
				hudSize + i*panelHeight/2f + indent,
				SCREEN_SIZE[0]/3f - 2*indent,
				panelHeight/2f - 2*indent);
		
		fillRect(Color.BLACK,
				offX + 2*indent,
				hudSize + i*panelHeight/2f + 2*indent,
				SCREEN_SIZE[0]/3f - 4*indent,
				panelHeight/2f - 4*indent);

		// challenge description
		int slot = getChallengeSlot(i,powerSelection.getCursorPosition()[i]);
		if(slot>=0) {
			Challenge c = Challenge.values()[slot];
			tex = TextureLoader.getInstance().getTexture(c.image);
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(
					offX + 2*indent,
					hudSize + i*panelHeight/2f + 2*indent,
					SCREEN_SIZE[0]/3f - 4*indent,
					(250/512f)*(SCREEN_SIZE[0]/3f - 4*indent));
			OpenGLDraw.unbindTexture();
		}
		
		// power description
		slot = getSlot(i,powerSelection.getCursorPosition()[i]);
		if(slot>=0) {
			Power p = Power.values()[slot];
			tex = TextureLoader.getInstance().getTexture(p.image+"_unlock");
			if(PowerShop.isPowerUnlocked(slot))
				tex = TextureLoader.getInstance().getTexture(p.image);
			OpenGLDraw.bindTexture(tex);
			OpenGLDraw.drawTexture(
					offX + 2*indent,
					hudSize + i*panelHeight/2f + 2*indent,
					SCREEN_SIZE[0]/3f - 4*indent,
					(250/512f)*(SCREEN_SIZE[0]/3f - 4*indent));
			OpenGLDraw.unbindTexture();
		}
	}

	private void drawPowerSelectionBox(PowerSelectionScreen powerSelection, int i) {
		
		Texture tex;
		
		// border
		fillRect(Color.BLACK,
				offX,
				hudSize,
				panelWidth,
				panelHeight);
		
		fillRect(playerDark[i],
				offX + indent,
				hudSize + indent,
				panelWidth - 2*indent,
				panelHeight - 2*indent);
		
		fillRect(Color.BLACK,
				offX + 2*indent,
				hudSize + 2*indent,
				panelWidth - 4*indent,
				panelHeight - 4*indent);
		
		// panels
		for(int p=0;p<Power.values().length;p++) {

			if(Power.values()[p].active)
				fillRect(GameGUI.enemy,
						offX + 4*indent + (p%5)*slotSize,
						hudSize + 4*indent + (p/5)*slotSize,
						slotSize-indent*2,slotSize-indent*2);
			else
				fillRect(Color.BLACK,
					offX + 4*indent + (p%5)*slotSize,
					hudSize + 4*indent + (p/5)*slotSize,
					slotSize-indent*2,slotSize-indent*2);
			
			if(!PowerShop.isPowerUnlocked(p)) {
				
				Color col = Color.BLACK;
				for(Challenge c: Challenge.values())
				for(Power cp: c.powers)
					if(cp.id==p) col = c.color;
				
				fillRect(col,
						offX + 6*indent + (p%5)*slotSize,
						hudSize + 6*indent + (p/5)*slotSize,
						slotSize-indent*6,slotSize-indent*6);
				
				tex = TextureLoader.getInstance().getTexture("powerBlank");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						offX + 5*indent + (p%5)*slotSize,
						hudSize + 5*indent + (p/5)*slotSize,
						slotSize-indent*4,slotSize-indent*4);
				OpenGLDraw.unbindTexture();
			} else {
				tex = TextureLoader.getInstance().getTexture("icon_"+Power.values()[p].image);
				if(tex==null)
					tex = TextureLoader.getInstance().getTexture("icon_temppower");
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
				if(!powerSelection.getPowers(i).contains(p)) {
					OpenGLDraw.bindTexture(tex);
					OpenGLDraw.drawTexture(back,
							offX + 5*indent + (p%5)*slotSize,
							hudSize + 5*indent + (p/5)*slotSize,
							slotSize-indent*4,slotSize-indent*4);
					OpenGLDraw.unbindTexture();
				} else {
					OpenGLDraw.bindTexture(tex);
					OpenGLDraw.drawTexture(
							offX + 5*indent + (p%5)*slotSize,
							hudSize + 5*indent + (p/5)*slotSize,
							slotSize-indent*4,slotSize-indent*4);
					OpenGLDraw.unbindTexture();
					
					tex = TextureLoader.getInstance().getTexture("icon_front");
					OpenGLDraw.bindTexture(tex);
					OpenGLDraw.drawTexture(
							offX + 5*indent + (p%5)*slotSize,
							hudSize + 5*indent + (p/5)*slotSize,
							slotSize-indent*4,slotSize-indent*4);
					OpenGLDraw.unbindTexture();
				}
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			}
		}
	}

	private void drawChallengeSlots() {
				
		float size = (SCREEN_SIZE[0]/3f - 6*indent)/20f - indent;
		Shape challengeHexShape = Shape.scale(DefaultShapes.basicHex,size);
		Shape challengeHexShapeInsert = Shape.scale(challengeHexShape,0.8f);
		
		// border
		fillRect(Color.BLACK,
				offX, offY,
				SCREEN_SIZE[0]/3f,
				hudSize);

		fillRect(GameGUI.energy,
				offX + indent, offY + indent,
				SCREEN_SIZE[0]/3f - 2*indent,
				hudSize - 2*indent);
		
		fillRect(Color.BLACK,
				offX + 2*indent, offY + 2*indent,
				SCREEN_SIZE[0]/3f - 4*indent,
				hudSize - 4*indent);
		
		for(int i=0;i<Challenge.values().length;i++) {
			
			OpenGLDraw.fillPoly(Challenge.values()[i].color,
					offX + 3*indent + size + (i/10.0f)*(SCREEN_SIZE[0]/3f - 6*indent),
					offY + hudSize/2f,
					challengeHexShape,
					1f);
			if(!PowerShop.isPowerUnlocked(Challenge.values()[i].powers[0].id))
				OpenGLDraw.fillPoly(Color.BLACK,
						offX + 3*indent + size + (i/10.0f)*(SCREEN_SIZE[0]/3f - 6*indent),
						offY + hudSize/2f,
						challengeHexShapeInsert,
						1f);
		}
	}
	
	public int getSlot(int player, Point2D cursorPosition) {
		int x = (int) ((cursorPosition.x - (player*SCREEN_SIZE[0]*2/3f + (1-player)*hudSize + 3*indent)) / slotSize);
		int y = (int) ((cursorPosition.y - (hudSize + 3*indent)) / slotSize);
		
		if(x<0 || x>4) return -1;
		if(y<0 || y+1>Power.values().length/5) return -1;
		
		return x + y*5;
	}
	
	public int getChallengeSlot(int player, Point2D cursorPosition) {
	
		float size = (SCREEN_SIZE[0]/3f - 6*indent)/20f;
		if(cursorPosition.y < (hudSize + panelHeight + 3*indent)) return -1;		
		if(cursorPosition.y > (hudSize + panelHeight + 3*indent + size*2)) return -1;
		
		int x = (int) ((cursorPosition.x - (SCREEN_SIZE[0]*(1/3f) + 3*indent)) / (size*2));
		if(x<0 || x>=Challenge.values().length) return -1;
		
		return x;
	}

	public void drawCursors(PowerSelectionScreen powerSelection) {
		for(int i=0;i<2;i++) {
			if(powerSelection.getControllers()[i]!=null) {
				Texture tex = TextureLoader.getInstance().getTexture("p"+i+"-cursor");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						powerSelection.getCursorPosition()[i].x,
						powerSelection.getCursorPosition()[i].y,
						32,48);
				OpenGLDraw.unbindTexture();
			}
		}
	}
}
