package mission.gameobject;

import static mission.gameobject.Mech.MECH_RADIUS;
import phys.DefaultShapes;
import phys.Shape;

public class ComponentShapes {

	public static final Shape[] ship1 = new Shape[]
			{
		Shape.scale(DefaultShapes.basicSquare,MECH_RADIUS/6f),
	};
	
	public static final Shape playerBase = Shape.scale(DefaultShapes.basicHex, Mech.MECH_RADIUS);
}
