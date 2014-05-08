package mission.gameobject;

import mission.BaseFactory;
import mission.Board;
import phys.Point2D;
import phys.Shape;

public class Base extends SimpleMoveableObject {
	
	static float wrapY = (float)Math.ceil(Board.BOARD_SIZE/BaseFactory.hexOffsetY)*BaseFactory.hexOffsetY;
	
	boolean wrap = false;
	
	public Base(Shape shape, Point2D pos) {
		super(shape,pos,new Point2D(0,1),Board.BOARD_SPEED);
	}

	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}
	
	@Override
	protected void concreteTick(float dt, Board board) {
		
		if(!wrap && (pos.y + getMinY() > Board.BOARD_SIZE))
				dead = true;
		if(wrap && (pos.y + getMinY() > wrapY))
			pos.y -= (wrapY + BaseFactory.hexOffsetY);
		
		// collision with player
		for(Player p: board.getPlayers()) {
			if(dead) break;
			for(Component pcomp: p.getComponents()) {
				if(pcomp.isDestroyed()) continue;
				collisionOffset.x = (p.getPos().x+pcomp.getPos().x) - pos.x;
				collisionOffset.y = (p.getPos().y+pcomp.getPos().y) - pos.y;
				Point2D mtv = Shape.collide(shape,pcomp.getShape(),collisionOffset);
				if(mtv!=Point2D.nullVector) {
					p.damage(10,p.getComponents().get(0),board);
					break;
				}
			}
		}
	}
}
