package mission.boss;

import java.awt.Color;
import java.util.List;

import phys.Point2D;
import mission.gameobject.Component;
import mission.gameobject.Enemy;

public class BigSpiderBoss extends Enemy {

	public static final Color minion_body_colour = new Color(22,19,225);
	
	public BigSpiderBoss(List<Component> components, Point2D pos, Point2D vel, float health, float speed) {
		super(components, pos, vel, health, speed);
		// TODO Auto-generated constructor stub
	}

}
