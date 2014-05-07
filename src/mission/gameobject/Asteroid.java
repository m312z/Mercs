package mission.gameobject;

import mission.Board;
import mission.effects.WordManager;
import phys.DefaultShapes;
import phys.Point2D;
import phys.Shape;

public class Asteroid extends SimpleMoveableObject {

	public static final float ASTEROID_RADIUS = 2f;
	
	int level;
	float angle;
	
	public Asteroid(Shape shape, Point2D pos, Point2D vel, float speed, int level) {
		super(shape,pos,vel,speed);
		this.level = level;
		angle = (float) (Math.random()*2-1);
	}

	@Override
	protected void concreteTick(float dt, Board board) {
		
		// rotate
		shape.rotate(angle*dt);
		
		// board destroy
		if((pos.x + getMaxX() < 0) || (pos.x + getMinX() > Board.BOARD_SIZE))
				dead = true;
		if((pos.y + getMinY() > Board.BOARD_SIZE))
				dead = true;
		
		// collision with player
		for(Player p: board.getPlayers()) {
			if(dead) break;
			for(Component pcomp: p.getComponents()) {
				if(pcomp.isDestroyed()) continue;
				collisionOffset.x = (p.getPos().x+pcomp.getPos().x) - pos.x;
				collisionOffset.y = (p.getPos().y+pcomp.getPos().y) - pos.y;
				Point2D mtv = Shape.collide(shape,pcomp.getShape(),collisionOffset);
				if(mtv!=Point2D.nullVector) {
					breakAsteroid(board,p.getId());
					p.damage(level,pcomp,board);
					break;
				}
			}
		}
	}

	
	public void breakAsteroid(Board board, int cause) {

		board.addScore(1);
		WordManager.addWord("+"+(board.getMap().getLevel()),new Point2D(pos.x,pos.y),30);
		if(cause>=0 && cause < 2)
			board.getCounter().addAsteroidsBroken(cause,1);
		
		dead = true;
		if(level>1) {
			for(int i=0;i<2;i++) {
				Asteroid child = new Asteroid(
						Shape.scale(DefaultShapes.basicHex, ASTEROID_RADIUS*(level)),
						new Point2D(pos.x,pos.y),
						new Point2D((float) (Math.random()-0.5),1f),
						(float) (Math.random()*0.2+0.2),level-1);
				board.addAsteroid(child);
			}
		}
	}
}
