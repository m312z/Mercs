package mission.specialMechs;

import java.util.List;

import phys.Point2D;
import mission.gameobject.Component;
import mission.gameobject.Enemy;
import mission.gameobject.Mech;

public class LocustGift extends Enemy {

	public LocustGift(List<Component> components, Point2D pos) {
		super(components, pos, new Point2D(), 101, Mech.MECH_SPEED/2f);
	}

}
