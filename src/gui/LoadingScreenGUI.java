package gui;

import static core.Frame.SCREEN_SIZE;
import static gui.GameGUI.VIEW_SIZE;
import static gui.GameGUI.hudSize;
import static gui.GameGUI.scale;
import static gui.OpenGLDraw.fillPoly;
import static gui.OpenGLDraw.fillRect;

import java.awt.Color;

import mission.EnemyFactory;
import mission.gameobject.Mech;
import phys.DefaultShapes;

public class LoadingScreenGUI {

	public void draw(float completion) {
	
		hudSize = (VIEW_SIZE*scale)/10;
		scale = (SCREEN_SIZE[1]-hudSize)/VIEW_SIZE;
		if(SCREEN_SIZE[0]/VIEW_SIZE < scale) {
			scale = SCREEN_SIZE[0]/VIEW_SIZE;
		}
		
		// draw background
		fillRect(Color.BLACK, 0, 0, SCREEN_SIZE[0], SCREEN_SIZE[1]);

		// draw hexes
		fillPoly(GameGUI.enemyDarkest,
				SCREEN_SIZE[0]/2f,SCREEN_SIZE[1]/2f,
				DefaultShapes.basicHex, Mech.MECH_RADIUS*scale);
		
		float totalHexes = 6*(3+2+1) * completion;
		Color[] colors = new Color[] {
			GameGUI.enemyDarkest,
			GameGUI.enemyDark,
			GameGUI.enemy
		};
		
		for(int r=1;r<4;r++) {
		for(int i=0;i<r*6;i++) {
			totalHexes -=1;
			fillPoly(colors[r-1],
					SCREEN_SIZE[0]/2f + scale*(1.1f)*EnemyFactory.getXCoord(r, i),
					SCREEN_SIZE[1]/2f + scale*(1.1f)*EnemyFactory.getYCoord(r, i),
					DefaultShapes.basicHex, Mech.MECH_RADIUS*scale);
			if(totalHexes<=0) return;
		}};
	}
}
