package gui;

import static core.Frame.SCREEN_SIZE;
import static gui.OpenGLDraw.fillRect;
import gui.FontManager.FontType;

import java.awt.Color;

import mission.Board;
import mission.boss.BuilderBaseBoss;
import mission.boss.HoneycombBoss;
import mission.boss.LocustSpawnBoss;
import mission.map.LevelMap.LevelType;
import phys.Point2D;
import core.Frame;
import core.menu.LevelSelectionScreen;

public class LevelSelectionGUI {
	
	/* size of the view window in terms of game size units */
	public static float VIEW_SIZE = Board.BOARD_SIZE;
	
	/* colour pallete */
	static final Color back = new Color(80,80,80);
	public static final Color energy = new Color(224,255,255);
	
	public static final Color[] levelColor = new Color[] {
		HoneycombBoss.honey,
		BuilderBaseBoss.builderBossColor,
		LocustSpawnBoss.locustColor,
		GameGUI.wreckage
	};
		
	/* offset to center the screen when full-screen */
	public static float hudSize = 0;
	public static float panelWidth;
	public static float panelHeight;
	public static float indent;
	
	/* visual stuff */
	int timer=0;
	float[] pos = {0f,0f};
	
	public void draw(LevelSelectionScreen levelSelection) {
		
		hudSize = SCREEN_SIZE[0]/20f;
		indent = hudSize/20f;
		panelWidth = SCREEN_SIZE[0]/3f;
		panelHeight = 6*indent + hudSize;
		
		// draw background
		fillRect(Color.BLACK, 0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);
		Texture tex = TextureLoader.getInstance().getTexture("stars");
		OpenGLDraw.bindTexture(tex);
		OpenGLDraw.drawTexture(0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);
		OpenGLDraw.unbindTexture();
		
		float offX = SCREEN_SIZE[0]/3f;
		float offY = hudSize;
			
		FontManager.getFont(FontType.FONT_32).drawString(Color.WHITE,
				offX + 2*indent,
				offY,
				"CHOOSE MISSION",Frame.FONT_SCALE,-Frame.FONT_SCALE);
		
		for(int i=0;i<LevelType.values().length;i++) {
			// border
			fillRect(Color.BLACK,
					offX,
					offY,
					panelWidth,
					panelHeight);
			
			if(levelSelection.getLevelType() == LevelType.values()[i]) {
				fillRect(energy,
						offX + indent,
						offY + indent,
						panelWidth - 2*indent,
						panelHeight - 2*indent);
			} else {
				fillRect(GameGUI.wreckage,
						offX + indent,
						offY + indent,
						panelWidth - 2*indent,
						panelHeight - 2*indent);
			}
			
			fillRect(levelColor[i],
					offX + 2*indent,
					offY + 2*indent,
					panelWidth - 4*indent,
					panelHeight - 4*indent);
			
			FontManager.getFont(FontType.FONT_32).drawString(Color.BLACK,
					offX + 2*indent,
					offY + 2*indent + FontManager.getFont(FontType.FONT_32).getHeight()*Frame.FONT_SCALE,
					LevelType.values()[i].displayName,Frame.FONT_SCALE,-Frame.FONT_SCALE);
			
			offY += panelHeight + indent;
		}
	}
	


	public int getSlot(int player, Point2D cursorPosition) {
		
		if(cursorPosition.x < SCREEN_SIZE[0]/3f || cursorPosition.x > SCREEN_SIZE[0]*2/3f)
			return -1;
		
		int y = (int) ((cursorPosition.y-hudSize) / (panelHeight+indent));
		if(y >= LevelType.values().length)
			return -1;
		
		return y;
	}

	public void drawCursors(LevelSelectionScreen levelSelection) {
		for(int i=0;i<2;i++) {
			if(levelSelection.getControllers()[i]!=null) {
				Texture tex = TextureLoader.getInstance().getTexture("p"+i+"-cursor");
				OpenGLDraw.bindTexture(tex);
				OpenGLDraw.drawTexture(
						levelSelection.getCursorPosition()[i].x,
						levelSelection.getCursorPosition()[i].y,
						tex.getImageWidth(),tex.getImageHeight());
				OpenGLDraw.unbindTexture();
			}
		}
	}
}
