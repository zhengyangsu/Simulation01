/*
hunter bot has 
unique method such as found(), setter and getter that is unique to hunter bot. 
state machine to handle the behavior of hunting and avoiding other hunter bots. 
draw(), move(), seen(), approach()
are overridden for hunter bot to handle its unique behavior and input(Robot) and unique LOOK.
unique fields enum State - HUNTING, AVOIDING, currentSate and robotTarget
*/


import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import processing.core.PVector;

public class HunterBot extends Machine{

	//properties fields
	Robot robotTarget;
	
	public HunterBot(Dimension dim, int id) {
		super(dim, id);
		scale = dice.nextDouble(0.7, 0.8);
		maxSpeed = 3;
		color = RobotPane.amber;
		engGainRatio = 20;
		setShapes();
	}

	@Override
	protected void setShapes() {
		super.setShapes();
		//body shapes (local coords)
		// outer circle
		super.dia = 60;
		super.outer = new Rectangle2D.Double(-dia / 2.0, -dia / 2.0, dia, dia);
		double d1 = dia * 4.0 / 5.0;
		super.inner = new Rectangle2D.Double(-d1 / 2.0, -d1 / 2.0, d1, d1);
		areaReset();
	}
	
	@Override
	public void move(Dimension panelSize, PVector f) {
		
		if(currentEnergyState != EnergyState.DEAD) {//else no movement
			
			//check energy level
			checkEnergyLvl();
			
		    switch(currentBehaviourState) {
		    
		    	case AVOIDING:
		    		timerAvoid++;
					//color = Color.RED; //visual feedback for being in AVOIDING state
					vel.limit(maxSpeed * 2f); 
			        if (timerAvoid > 40) { //cooldown period
			            transitionTo(BehaviourState.HUNTING);
			    		//color = RobotPane.amber;
			        }
		    		break;
		    		
		    	case HUNTING:
		    		if (robotTarget != null) {
		    	    	vel.normalize();
		    	        vel.mult(3f * maxSpeed);
		    		}
		    		break;
		    		
		    	case SEARCHING:
		    		vel.setMag(maxSpeed);
		    		break;
		    		
		    	case ESCAPING:
		    		break;
		    }
	 
		    vel.add(f); 
			energy -= engLossRatio;
		    pos.add(vel);	
		    
	    }else {
	    	vel.set(0,0);
	    	color = Color.GRAY;
	    	lightOn = false;
	    }

	    //collisionValidate(panelSize);
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
		
		boolean reach = false;
		
		if (currentEnergyState == EnergyState.DEAD) return false;
		
		if (robotTarget == null) {
			vel.setMag(maxSpeed);
			return false;		
		}
		
		PVector path = PVector.sub(robotTarget.getPos(), pos);
		vel.add(path);
		vel.normalize();
		//System.out.println("Approaching target at " + robotTarget.getPos() + " with speed " + speed);
		//check if bug reaches target
		if (targetCollisionCheck(robotTarget) && path.mag() - (scale * dia) / 2 <= 0) {
			reach = true;
			collectCount++;
			energy += robotTarget.getSize() * engGainRatio;

			//System.out.println("Reached target at " + currentTarget.getPos());
			//speed.mult(0.5f);
		}
		
		return reach;
	}
	
	public boolean found(Robot r) {
		if (r == null) return false;
		boolean intersect = getFOV().intersects(r.getBounds()) || getBoundary().intersects(r.getBounds());
		if (intersect) vel.mult(1.5f);
		return intersect;
	}

	public void setTarget(Robot target) {
		this.robotTarget = target;
	}
	
	public Robot getTarget() {
		return robotTarget;
	}

	public boolean hunterCollisionCheck(Hunter h) {
		
		boolean intersect = (getBoundary().intersects(h.getBoundary().getBounds2D()) && h.getBoundary().intersects(getBoundary().getBounds2D()));
		if (intersect) energy += 5;
		
		return intersect;
	}
	
}
