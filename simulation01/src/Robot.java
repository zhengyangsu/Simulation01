
/*
Robot has 
unique method such as escape(), drawBrushes(), setter and getter that is unique to Robot. 
state machine to handle the behavior of hunting and avoiding other Robot. 
move(), seen(), approach() targetCollisionCheck() and draw() 
are overridden for robot to handle its unique behavior and input(DustPile) and unique LOOK.
unique field enum State - HUNTING, ESCAPING, AVOIDING, currentSate and robotTarget
*/
	
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;
import processing.core.PVector;

public class Robot extends Machine{
	
	//region
	//properties fields
	private double brushAngle = 0;
	private double brushSpeed = 0.25; // radians per frame
	//endregion
	
	//constructor
	public Robot(Dimension dim, int id) {
		super(dim, id);	
		scale = dice.nextDouble(0.55, 0.65);
		engGainRatio = 50;
		setShapes();
	}
	
	@Override
	protected void setShapes() {
		super.setShapes();
		areaReset();
	}
	
	@Override
	public void draw(Graphics2D g) {
		infoLines = new String[] {getClass().getName(), currentBehaviourState.name(), currentEnergyState.name(), "Energy " + energy};
		AffineTransform af = g.getTransform();
		g.setColor(color);
		g.setStroke(new BasicStroke(RobotPane.stroke * 1.5f));
		
		//flicker on weak
		if (currentEnergyState == EnergyState.WEAK) {
		    //blink every 12 frames: visible 6 frames, invisible 6 frames
		    if (timerLight % 12 < 6) {
		        timerLight++;
		        return; //skips drawing and exit
		    }
		    timerLight++;
		}
		
		//transform stack
		g.translate(pos.x, pos.y);
		g.rotate(theta);
		g.rotate(vel.heading());
		g.scale(scale, scale);
		if (vel.x < 0) {
		    g.scale(-1, 1); // flip
		}
		
		//brushes
		drawBrushes(g, 1);
		drawBrushes(g, -1);

		
		g.setTransform(af);//reset for bounding box
		super.draw(g);

   
	}
	
	//draw moving brushes
	private void drawBrushes(Graphics2D g, int side) {
		// TODO Auto-generated method stub
		
		
		int pivotX = 27 * side;
	    int pivotY = -22;

	    double length = 18;                
	    double spread = Math.toRadians(20);
	    double baseAngle = Math.toRadians(-135);
	    
		AffineTransform at = new AffineTransform();
	    AffineTransform old = g.getTransform();

	    // move pivot to origin
	    //g.translate(pivotX, pivotY);
	    at.translate(pivotX, pivotY);
	    // rotate brush
	    //g.rotate(brushAngle * side);
	    at.rotate(brushAngle * side);
	    g.transform(at);
	    int count = 3;
	    for (int i = 0; i < count; i++) {
	        double t = (i - (count - 1) / 2.0);
	        double a = baseAngle + t * spread;

	        double endX = Math.cos(a) * length;
	        double endY = Math.sin(a) * length;

	        Shape bristle = new Line2D.Double(0, 0, endX, endY);
	        g.draw(bristle);
	       

	    }
	    Shape bristleOutline = new Ellipse2D.Double(0 - length, 0 - length, 2 * length, 2 * length);
	    Shape transformedOutline = at.createTransformedShape(bristleOutline);
	    
	    g.draw(bristleOutline);
	    robotArea.add(new Area(transformedOutline));
	    
	    g.setTransform(old);;
   
	}
	
	@Override
	public void move(Dimension panelSize, PVector f) {
		
		checkEnergyLvl();
		
	    //update State Timers
		if (currentEnergyState != EnergyState.DEAD) {
		    switch (currentBehaviourState) {
		        case ESCAPING:
		            timerEscape++;
		            vel.add(f); 
			        vel.limit(maxSpeed * 2.5f);
		            if (timerEscape > 48) {
		                transitionTo(BehaviourState.SEARCHING);
		            }
		            break;
		
		        case AVOIDING:
		            timerAvoid++;
		            f.limit(0.8f); 
			        vel.add(f);
			        vel.limit(maxSpeed * 1.5f);
		            if (timerAvoid > 36) {
		                transitionTo(BehaviourState.SEARCHING);
		                color = RobotPane.green;
		            }
		            break;
		            
		        case SEARCHING:
		            // Standard hunting logic (no timer needed)
		        	f.limit(0.15f); //smooth steering hunting
			        vel.add(f);
			        vel.limit(maxSpeed);
		            break;
		            
		        case HUNTING:
		        	break;
		    }
		    
		    updateAnimation(panelSize);
		    
		}else vel.mult(0);

	    pos.add(vel);
		energy -= engLossRatio;
	    
	}

	private void updateAnimation(Dimension panelSize) {
	    // Toggle light based on a slower timer, not every frame
	    if (timerLight % 15 == 0) lightOn = !lightOn;
	    timerLight++;

	    brushAngle += brushSpeed;
	    if (brushAngle > Math.PI * 2) brushAngle -= Math.PI * 2;

	    collisionValidate(panelSize);
	    reset(panelSize);
	}
	
	@Override
	public PVector seen(Machine r) {
		//if already escaping stays escaping
	    if (currentBehaviourState == BehaviourState.ESCAPING) return new PVector(0, 0);

	    //calculate distance and intersection
	    double dist = PVector.dist(this.pos, r.pos);
	    boolean intersect = getFOV().intersects(r.getBounds()) || 
	                        getBoundary().intersects(r.getBounds());

	    if (intersect) {
	    	
	    	//hunterBot flee immediately
	        if (r instanceof Hunter) {
	            transitionTo(BehaviourState.ESCAPING);
	            return escape((Hunter)r); 
	        }
	    	
	        //hunterBot flee immediately
	        if (r instanceof HunterBot) {
	            transitionTo(BehaviourState.ESCAPING);
	            return escape((HunterBot)r); 
	        }

	        //robot avoidance
	        if (r instanceof Robot && scale <= r.getScale()) {
	            //only transition if we aren't already avoiding, to reset the timer
	            if (currentBehaviourState != BehaviourState.AVOIDING) {
	                transitionTo(BehaviourState.AVOIDING);
	                //color = Color.RED;
	            }

	            //calculate a weighted repulsion force
	            PVector repulsion = PVector.sub(this.pos, r.pos);
	            repulsion.normalize();
	            
	            //closer it is, the stronger the push (Inverse Square Law logic)
	            float strength = (float) (sight / (dist + 1));
	            repulsion.mult(strength * 2.0f); 
	            
	            return repulsion; 
	        }
	    }
	    
	    return new PVector(0, 0);
	}
	
	@Override
	protected boolean targetCollisionCheck(Object target) {
		boolean collision = false;
		
		if (target instanceof DustPile) {
			DustPile pile = (DustPile) target;
			collision = (getBoundary().intersects(pile.getBoundary().getBounds2D()) && pile.getBoundary().intersects(getBoundary().getBounds2D()));
			//if (collision) System.out.println("Collision: " + collision);
		}
		
		return collision;

	}
	
	@Override
	public boolean approach() {
				
		boolean reach = false;

		if (dustTarget!=null && currentBehaviourState == BehaviourState.SEARCHING) {
			// calculate the path to target point
			PVector path = PVector.sub(dustTarget.getPos(), pos);
			path.limit(0.1f);                            // max steering strength
			vel.add(path);
			vel.limit(maxSpeed);
			
			//check if bug reaches target
			if (targetCollisionCheck(dustTarget) && path.mag() - (scale * dia) / 2 <= 0 ) {
				reach = true;
				collectCount++;
				energy += dustTarget.getSize() * engGainRatio;
			}
		}
		return reach;
	}
	
	//getter, setter
	public void setTarget(ArrayList <DustPile> targets) {
		this.targets = targets;
		if (this.targets != null && dustTarget == null) {
			this.dustTarget = this.targets.get(0);
			reTarget = false;
		}
		else dustTarget = null;
	}
	
	public DustPile getTarget() {
		return dustTarget;
	}

	private PVector escape(Machine h) {
		//System.out.println("Escape from HunterBot " + h.getId());
		hunt = false;
		PVector normal = PVector.sub(this.pos, h.pos);
		return normal;
	}

}

