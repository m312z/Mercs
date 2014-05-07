package mission.behaviour;

import mission.Board;
import mission.gameobject.Mech;

public interface MechBehaviour {
	public void tick(float dt, Board board, Mech mech);
}
