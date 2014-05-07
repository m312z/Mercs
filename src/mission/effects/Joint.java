package mission.effects;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import phys.Point2D;
import phys.Shape;
import mission.gameobject.Mech;

public class Joint {

	private List<Joint> children;
	private Shape shape;
	private Color colour; 
	private Point2D pos, relativePos;
	private float rotation, orgRotation;
	private float rotationTarget;
	private float rotationSpeed;
	private Joint parent;
	
	/**
	 * Create a joint that can rotate around <code>pos</code>. 
	 * @param parent The parent joint, if this node rotates than this node changes position accordingly.
	 * @param pos The position relative to the root joint.
	 * @param mech The mech this joint is part of.
	 * @param shape The shape of the joint.
	 * @param colour The colour of the joint.
	 * @param rotation The current rotation of the joint.
	 * @param rotationSpeed The speed at which the joint can rotate.
	 */
	public Joint(Joint parent, Point2D pos, Mech mech, Shape shape, Color colour, float rotation, float rotationSpeed) {
		this.parent = parent;
		this.pos = pos;
		this.shape = shape;
		this.colour = colour;
		this.rotation = rotation;
		orgRotation = rotation;
		rotationTarget = rotation;
		this.rotationSpeed = rotationSpeed;
		children = new ArrayList<Joint>();
		
		if (parent != null) {
			parent.addChild(this);
			relativePos = new Point2D(pos.x - parent.pos.x, pos.y - parent.pos.y);
		} else {
			relativePos = new Point2D(pos.x,pos.y);
		}
	}
	
	private void addChild(Joint joint) {
		children.add(joint);
	}
	
	public Joint getParent() {
		return parent;
	}
	
	public void setRotation(float degree) {
		rotationTarget = degree % 360;
	}
	
	public float getRotation() {
		return rotation;
	}
	
	public float getOrgRotation() {
		return orgRotation;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public Point2D getPos() {
		return pos;
	}
	
	public Color getColour() {
		return colour;
	}
	
	public void setColour(Color colour) {
		this.colour = colour;
	}
	
	/**
	 * If a joint has rotated than we need to change the position and rotation of all the children as well.
	 * @param source
	 * @param rotation
	 */
	public void parentRotated(Joint source, float rotation) {
		// Rotate the shape with the rotation done by the source.
		Point2D tmp = new Point2D(pos.x - source.pos.x, pos.y - source.pos.y);
		tmp.rotate(rotation);
		pos.x = tmp.x + source.pos.x;
		pos.y = tmp.y + source.pos.y;
		
		relativePos.x = getPos().x - parent.getPos().x;
		relativePos.y = getPos().y - parent.getPos().y;
		
		// Rotate the shape such that the angle between this joint and the parent stays the same.
		shape.rotate(rotation);
		
		// Update the children accordingly.
		for (Joint j : children) {
			j.parentRotated(source, rotation);
		}
	}
	
	public void tick(float dt) {

		// Determine which direction to go.
		float currentRotation = rotation;
		if (rotation >= 0 || rotation <= 180) {
			// Turn left.
			if (rotationTarget >= rotation && rotationTarget <= rotation + 180) {
				rotation += rotationSpeed * dt;
				if (rotation > rotationTarget) {
					rotation = rotationTarget;
				}
			}
			// Turn right.
			else {
				rotation -= rotationSpeed * dt;
				if (rotation < rotationTarget || rotation < 0) {
					rotation = rotationTarget;
				}
			}
		} else {
			// Turn left.
			if (rotationTarget >= rotation && (rotationTarget <= 360 || rotationTarget <= (rotation + 180) % 360)) {
				rotation += rotationSpeed * dt;
				if (rotation > rotationTarget) {
					rotation = rotationTarget;
				}
			}
			// Turn right.
			else {
				rotation -= rotationSpeed * dt;
				if (rotation < rotationTarget || (rotationTarget < 180 && rotation < 0)) {
					rotation = rotationTarget;
				}
			}
		}
		
		// Check how much we rotated in the end.
		float toRotate = rotation - currentRotation;
		if (toRotate > 180) {
			toRotate = -360 + toRotate;
		} else {
			toRotate = 360 - toRotate;
		}
		
		// Rotate the shape such that the angle between this joint and the parent stays the same.
		shape.rotate(toRotate);//, relativePos);
		
		// Rotate all the children too.
		for (Joint j : children) {
			j.parentRotated(this, toRotate);
		}
	}
}
