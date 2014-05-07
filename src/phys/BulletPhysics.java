package phys;

import java.util.List;

import mission.gameobject.Component;
import mission.gameobject.GameObject;
import mission.gameobject.Mech;
import mission.gameobject.SimpleMoveableObject;
import phys.Shape.Projection;

public class BulletPhysics {

	static SimpleMoveableObject lastSimpleMoveableObjectCollision;
	static Mech lastMechCollision;
	static Component lastComponentCollision;
	static int collisionEdge = 0;
		
	public static float getFirstRayTarget(Point2D pos, Point2D vel, List<? extends GameObject> objects) {
		
		float time = Float.MAX_VALUE;
		
		// vector orthogonal to the bullet's travel
		Point2D orth = Point2D.normalise(new Point2D(vel.y,-vel.x));		
		
		for(GameObject o: objects) {
			
			if(o instanceof SimpleMoveableObject) {
				SimpleMoveableObject sim = (SimpleMoveableObject)o;
				time = Math.min(time,doSimpleObjectCollision(pos, vel, orth, o, sim));
			} else {
				Mech mech = (Mech)o;
				for(Component c: mech.getComponents()) {
					time = Math.min(time,componentCollision(pos, vel, orth, mech, c));
				}
			}
		}
		return time;
	}

	private static float doSimpleObjectCollision(Point2D pos, Point2D vel, Point2D orth, GameObject o, SimpleMoveableObject sim) {
		
		 float time = Float.MAX_VALUE;
		
		// offset by block position
		Point2D offset = new Point2D(o.getPos().x-pos.x,o.getPos().y-pos.y);

		// project shadow onto orth
		Projection pOrth = sim.getShape().project(orth,offset);
		if(pOrth.min*pOrth.max>0){
			return time;
		}
		
		// for each edge, check collision time
		for(int i=0;i<sim.getShape().getPoints().length;i++) {

			float ux = sim.getShape().getPoints()[(i+1)%sim.getShape().getPoints().length].x - sim.getShape().getPoints()[i].x; 
			float uy = sim.getShape().getPoints()[(i+1)%sim.getShape().getPoints().length].y - sim.getShape().getPoints()[i].y;
			
			float wx = sim.getPos().x + sim.getShape().getPoints()[i].x - pos.x; 
			float wy = sim.getPos().y + sim.getShape().getPoints()[i].y - pos.y;
							
			// parallel check
			if(vel.x*uy - vel.y*ux == 0)
				continue;
			
			// Segment location: collision occurs if 0<=u<=1
			float s =  (vel.y*wx - vel.x*wy) / (vel.x*uy - vel.y*ux);
							
			if(s<=1 && s>=0) {
				// time until collision
				float t =  (ux*wy - uy*wx) / (vel.y*ux - vel.x*uy);
				if(t>=0 && t<time) {
					time = t;
					lastSimpleMoveableObjectCollision = sim;
					collisionEdge = i;
				}
			}
		}
		return time;
	}

	private static float componentCollision(Point2D pos, Point2D vel, Point2D orth, Mech mech, Component c) {
		
		float time = Float.MAX_VALUE;
		
		// offset by block position
		Point2D offset = new Point2D(mech.getPos().x +c.getPos().x-pos.x,mech.getPos().y +c.getPos().y-pos.y);
		
		// project shadow onto orth
		Projection pOrth = c.getShape().project(orth,offset);
		if(pOrth.min*pOrth.max>0){
			return time;
		}
		
		// for each edge, check collision time
		for(int i=0;i<c.getShape().getPoints().length;i++) {

			float ux = c.getShape().getPoints()[(i+1)%c.getShape().getPoints().length].x - c.getShape().getPoints()[i].x; 
			float uy = c.getShape().getPoints()[(i+1)%c.getShape().getPoints().length].y - c.getShape().getPoints()[i].y;
			
			float wx = mech.getPos().x + c.getPos().x + c.getShape().getPoints()[i].x - pos.x; 
			float wy = mech.getPos().y + c.getPos().y + c.getShape().getPoints()[i].y - pos.y;
							
			// parallel check
			if(vel.x*uy - vel.y*ux == 0)
				continue;
			
			// Segment location: collision occurs if 0<=u<=1
			float s =  (vel.y*wx - vel.x*wy) / (vel.x*uy - vel.y*ux);
							
			if(s<=1 && s>=0) {
				// time until collision
				float t =  (ux*wy - uy*wx) / (vel.y*ux - vel.x*uy);
				if(t>=0 && t<time) {
					time = t;
					lastMechCollision = mech;
					lastComponentCollision = c;
					collisionEdge = i;
				}
			}
		}
		return time;
	}
	
	public static int getCollisionEdge() {
		return collisionEdge;
	}
	
	public static Mech getLastMechCollision() {
		return lastMechCollision;
	}
	
	public static Component getLastComponentCollision() {
		return lastComponentCollision;
	}
}
