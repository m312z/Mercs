package mission.gameobject;

import phys.Point2D;
import phys.Shape;

public abstract class SimpleMoveableObject extends MoveableObject {
	
	protected Shape shape;
	
	public SimpleMoveableObject(Shape shape, Point2D pos, Point2D vel, float speed) {
		super(pos, vel, speed);
		this.shape = shape;
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public float getMaxX() {
		return shape.getMaxX();
	}
	
	public float getMinX() {
		return shape.getMinX();
	}

	public float getMaxY() {
		return shape.getMaxY();
	}
	
	public float getMinY() {
		return shape.getMinX();
	}
}
