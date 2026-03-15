import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import processing.core.PVector;

public class Missile extends Machine{
	    private Rectangle2D missileBody, missileWing, missileTail;  // rectangle tail for simplicity
	    private Path2D missileHead;       // keep head as Path2D
	    private PVector pos, speed;
	    private HunterBot target;
	    private boolean fired;
	    
	    
	    // Dimensions
	    private double mLength, mWidth, wingWidth, wingHeight, tailWidth,  tailHeight;
	    private int maxSpeed;
	    
	    Missile(double startX, double startY, Dimension dim, int id) {
	    	super(dim, id);
	        pos = new PVector((float) startX, (float) startY);
	        speed = new PVector(0, 0);
	        maxSpeed = 10;
	        mLength = 30;
		    mWidth = 6;
		    wingWidth = 8;
		    wingHeight = 12;
		    tailWidth = 6;
		    tailHeight = 10;
		    target = null;
		    fired = false;
	        setShape();
	    }

	    @Override
	    public void setShape() {
		    robotArea = new Area();

	        //body 
	        missileBody = new Rectangle2D.Double(0, -mWidth/2, mLength, mWidth);

	        //wing 
	        missileWing = new Rectangle2D.Double(mLength/2, -wingHeight/2, wingWidth, wingHeight);

	        //tail 
	        missileTail = new Rectangle2D.Double(-tailWidth, -tailHeight/2, tailWidth, tailHeight);

	        //head 
	        missileHead = new Path2D.Double();
	        missileHead.moveTo(mLength, -mWidth/2);
	        missileHead.lineTo(mLength, mWidth/2);
	        missileHead.lineTo(mLength + 8, 0);
	        missileHead.closePath();
	        
	        robotArea.add(new Area(missileBody));
	        robotArea.add(new Area(missileWing));
	        robotArea.add(new Area(missileTail));
	        robotArea.add(new Area(missileHead));

	    }

	    public void draw(Graphics2D g) {
	        AffineTransform af = g.getTransform();

	        //translate to missile position
	        g.translate(pos.x, pos.y);
	        g.rotate(speed.heading());
	        
	        g.setColor(Color.ORANGE);
	        g.fill(missileTail);
	        g.fill(missileWing);
	        g.fill(missileBody);
	        g.fill(missileHead);

	        g.setTransform(af);
	    }
	    
	    public void setTarget(HunterBot t) {
	    	target = t;
	    }
	    
	    public void setFired() {
	    	fired = true;
	    }
	    
	    public boolean getFired() {
	    	return fired;
	    }
	    


		@Override
		public void move(Dimension panelSize, PVector f) {
			// TODO Auto-generated method stub
			 if (!fired) {
			        // stick to hunter until fired
			        pos.x = f.x;
			        pos.y = f.y;
			    } 
			
			pos.add(speed);
		}

		@Override
		protected boolean targetCollisionCheck(Object target) {
			// TODO Auto-generated method stub
			boolean collision = false;
			
			collision = (getBoundary().intersects(this.target.getBoundary().getBounds2D()) && this.target.getBoundary().intersects(getBoundary().getBounds2D()));

			return collision;
		}

		@Override
		public boolean approach() {
			// TODO Auto-generated method stub
			boolean reach = false;
	    	PVector path = PVector.sub(this.target.getPos(), pos); // direction to target
			speed = path.copy().normalize();
			speed.setMag(maxSpeed);
			
			if (targetCollisionCheck(target) && path.mag() - mLength / 2 <= 0 ) {
				reach = true;
			}
			
			return reach;
		}
	    
		public HunterBot getTarget() {
			return target;
		}
		
		@Override
		public Shape getBoundary() {
			AffineTransform at = new AffineTransform();
			at.translate(pos.x, pos.y);
	        at.rotate(speed.heading());
			return at.createTransformedShape(robotArea);
		}
	}