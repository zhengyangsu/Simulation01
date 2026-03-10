/*
hunter bot has 
unique method such as found(), setter and getter that is unique to hunter bot. 
state machine to handle the behavior of hunting and avoiding other hunter bots. 
draw(), move(), seen(), approach()
are overridden for hunter bot to handle its unique behavior and input(Robot) and unique LOOK.
unique fields enum State - HUNTING, AVOIDING, currentSate and robotTarget
*/


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import processing.core.PVector;

public class HunterBot extends Machine{

	//properties fields
	Robot robotTarget;
	
	public HunterBot(Dimension dim, int id) {
		super(dim, id);
		color = RobotPane.amber;
		setShapes();
	}

	
	@Override
	protected void setShapes() {
		super.setShapes();
		//body shapes (local coords)
		// outer circle
		super.outer = new Rectangle2D.Double(-dia / 2.0, -dia / 2.0, dia, dia);
		double d1 = dia * 4.0 / 5.0;
		super.inner = new Rectangle2D.Double(-d1 / 2.0, -d1 / 2.0, d1, d1);
		areaReset();
	}
	
	
	@Override
	public void move(Dimension panelSize, PVector f) {
		//update State Timers (The missing part)
	    if (currentBehaviourState == BehaviourState.AVOIDING) {
	        timerAvoid++;
			color = Color.RED; // Visual feedback for being in AVOIDING state

	        if (timerAvoid > 40) { // Cooldown period
	            transitionTo(BehaviourState.HUNTING);
	    		color = RobotPane.amber;

	        }
	    }

	    //physics logic
	    f.limit(0.3f); 
	    speed.add(f);
	    
	    //use the State to decide speed limits
	    if (currentBehaviourState == BehaviourState.AVOIDING) {
	        speed.limit(maxSpeed * 2f); // Move slower while careful
	    } else if (robotTarget != null) {
	    	speed.normalize();
	        speed.mult(3f * maxSpeed);
	    }else {
	    	speed.limit(maxSpeed);
	    }
	    
	    pos.add(speed);
	    collisionValidate(panelSize);
	    reset(panelSize);
	}
	
	@Override
	public PVector seen(Machine r) {

	    //calculate distance and intersection
	    double dist = PVector.dist(this.pos, r.pos);
	    
	    boolean intersect = getFOV().intersects(r.getBounds()) || 
	                        getBoundary().intersects(r.getBounds());

        //robot avoidance
        if (r instanceof HunterBot && scale <= r.getScale() && intersect) {
            // Only transition if we aren't already avoiding, to reset the timer
            if (currentBehaviourState != BehaviourState.AVOIDING) {
                transitionTo(BehaviourState.AVOIDING);                
            }

            //calculate a weighted repulsion force
            PVector repulsion = PVector.sub(this.pos, r.pos);
            repulsion.normalize();
            
            //closer it is, the stronger the push (Inverse Square Law logic)
            float strength = (float) (sight / (dist + 1));
            repulsion.mult(strength * 2.0f); 
            
            return repulsion; 
        }
	    
	    
	    return new PVector(0, 0);
	}
	
	//helper to handle transitions cleanly
	private void transitionTo(BehaviourState newState) {
	    currentBehaviourState = newState;
	    timerAvoid = 0;
	
	    //synchronize legacy variables
	    this.hunt = (newState == BehaviourState.HUNTING);
	    
	    //update visual feedback
	    if (newState == BehaviourState.HUNTING) color = RobotPane.green;
	}
	
	@Override
	protected boolean targetCollisionCheck(Object target) {
		boolean collision = false;
		
		if (target instanceof Robot) {
			Robot robot = (Robot) target;
			collision = (getBoundary().intersects(robot.getBoundary().getBounds2D()) && robot.getBoundary().intersects(getBoundary().getBounds2D()));
			//if (collision) System.out.println("Collision: " + collision);
		}
		
		return collision;
	}

	@Override
	public boolean approach() {
		
		if (robotTarget == null) {
			speed.setMag(maxSpeed);
			return false;		
		}
		
		boolean reach = false;

		PVector path = PVector.sub(robotTarget.getPos(), pos);
		speed.add(path);
		speed.normalize();
		System.out.println("Approaching target at " + robotTarget.getPos() + " with speed " + speed);
		// check if bug reaches target (targetCollisionCheck(robotTarget) && path.mag() - (scale * dia) / 2 <= 0 )
		if (targetCollisionCheck(robotTarget) && path.mag() - (scale * dia) / 2 <= 0) {
			reach = true;
			collectCount++;
			//System.out.println("Reached target at " + currentTarget.getPos());
			//speed.mult(0.5f);
		}
		
		return reach;
	}
	
	public boolean found(Robot r) {
		if (r == null) return false;
		boolean intersect = getFOV().intersects(r.getBounds()) || 
                getBoundary().intersects(r.getBounds());

		if (intersect) {
			return true;
		}
		return false;
	}

	public void setTarget(Robot target) {
		this.robotTarget = target;
	}
	
	public Robot getTarget() {
		return robotTarget;
	}
}
