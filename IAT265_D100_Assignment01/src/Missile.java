import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import processing.core.PVector;

public class Missile extends Machine{
	    private Rectangle2D missileBody, missileWing, missileTail;  // rectangle tail for simplicity
	    private Path2D missileHead;       // keep head as Path2D
	    private PVector pos, speed;
	    private HunterBot target;
	    
	    
	    // Dimensions
	    private double mLength, mWidth, wingWidth, wingHeight, tailWidth,  tailHeight;
	    private int maxSpeed;
	    
	    Missile(double startX, double startY, Dimension dim, int id) {
	    	super(dim, id);
	        pos = new PVector((float) startX, (float) startY);
	        speed = new PVector(0, 0);
	        mLength = 30;
		    mWidth = 6;
		    wingWidth = 8;
		    wingHeight = 12;
		    tailWidth = 6;
		    tailHeight = 10;
		    target = null;
	        setShape();
	    }

	    @Override
	    public void setShape() {
	        // Body relative to (0,0)
	        missileBody = new Rectangle2D.Double(0, -mWidth/2, mLength, mWidth);

	        // Wing: rectangle for simplicity
	        missileWing = new Rectangle2D.Double(mLength/2, -wingHeight/2, wingWidth, wingHeight);

	        // Tail: rectangle for simplicity
	        missileTail = new Rectangle2D.Double(-tailWidth, -tailHeight/2, tailWidth, tailHeight);

	        // Head: triangle
	        missileHead = new Path2D.Double();
	        missileHead.moveTo(mLength, -mWidth/2);
	        missileHead.lineTo(mLength, mWidth/2);
	        missileHead.lineTo(mLength + 8, 0);
	        missileHead.closePath();
	    }

	    public void draw(Graphics2D g) {
	        AffineTransform af = g.getTransform();

	        // Translate to missile position
	        g.translate(pos.x, pos.y);
	        g.rotate(speed.heading());
	        
	        g.setColor(Color.ORANGE);
	        g.fill(missileTail);
	        g.fill(missileWing);
	        g.fill(missileBody);
	        g.fill(missileHead);

	        g.setTransform(af);
	    }
	    
	    public void autoTarget(HunterBot t) {
	    	target = t;
	    	PVector dir = PVector.sub(target.getPos(), pos); // direction to target
			speed = dir.normalize();
			//speed.setMag(maxSpeed);
			System.out.println(speed);

			pos.add(speed);
			//System.out.println(speed);
			//System.out.println(pos);

	    }
	    
	    public boolean collisionCheck() {
	    	
			return false;
	    	
	    }

		@Override
		public void move(Dimension panelSize, PVector f) {
			// TODO Auto-generated method stub
			pos.add(speed);
		}

		@Override
		protected boolean targetCollisionCheck(Object target) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean approach() {
			// TODO Auto-generated method stub
			return false;
		}
	    
	}