package mission.behaviour;

import static gui.GameGUI.enemy;
import static mission.boss.BuilderBaseBoss.builderBossColor;

import java.awt.Color;

import phys.Point2D;

import mission.Board;
import mission.EnemyFactory;
import mission.EnemyFactory.EnemyType;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;

public class DroneMakerBehaviour implements MechBehaviour {
	
	final float buildTime = 180f;
	float timer;

	@Override
	public void tick(float dt, Board board, Mech mech) {
		
		timer += dt;
		
		if(timer > buildTime) {
			timer = 0;
			for(int i=1;i<7;i++) {
				if(!mech.getComponents().get(i).isDestroyed()) {
					Enemy e = EnemyFactory.makeBasicEnemy(EnemyType.DIRECTEDDRONE);
					e.getPos().x = mech.getPos().x + mech.getComponents().get(i).getPos().x;
					e.getPos().y = mech.getPos().y + mech.getComponents().get(i).getPos().y;
					Point2D p = new Point2D(
							(float)((Math.random() + 1/8f)*6*Board.BOARD_SIZE/8f),
							(float)((Math.random() + 1/8f)*6*Board.BOARD_SIZE/8f));
					e.setBehaviour(new StaticPositionBehaviour(p,Float.MAX_VALUE));
					board.addEnemy(e);
					mech.getComponents().get(i).setColour(builderBossColor);
				}
			}
		} else {
			for(int i=1;i<7;i++) {
				if(!mech.getComponents().get(i).isDestroyed())
					mech.getComponents().get(i).setColour(new Color(
							(int)(builderBossColor.getRed() + (enemy.getRed() - builderBossColor.getRed())*(timer/buildTime)),
							(int)(builderBossColor.getGreen() + (enemy.getGreen() - builderBossColor.getGreen())*(timer/buildTime)),
							(int)(builderBossColor.getBlue() + (enemy.getBlue() - builderBossColor.getBlue())*(timer/buildTime))));
			}	
		}
		
		// map move
		mech.getDirection().x = 0;
		mech.getDirection().y = Board.BOARD_SPEED;
		mech.setSpeed(Board.BOARD_SPEED);
		
		
	}
}
