
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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;
import processing.core.PVector;

public class Robot extends Machine{
	
	//properties fields
	private int timerEscape;
	
	private double brushAngle = 0;
	private double brushSpeed = 0.25; // radians per frame
	private enum BehaviourState {
	    HUNTING,    //searching for and moving toward dust
	    ESCAPING,   //high-speed flight from a HunterBot
	    AVOIDING    //brief cooldown after interacting with another Robot
	}

	private BehaviourState currentBehaviourState = BehaviourState.HUNTING;
	//constructor
	public Robot(Dimension dim, int id) {
		super(dim, id);	
		timerEscape = 0;
		setShapes();
	}
	
	@Override
	protected void setShapes() {
		super.setShapes();
		areaReset();
	}
	
	@Override
	public void draw(Graphics2D g) {
		
		AffineTransform af = g.getTransform();
		
		g.setColor(color);
		g.setStroke(new BasicStroke(RobotPane.stroke * 1.5f));
		
		//transform stack
		g.translate(pos.x, pos.y);
		g.rotate(theta);
		g.rotate(speed.heading());
		g.scale(scale, scale);
		if (speed.x < 0) {
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
	    //update State Timers
	    switch (currentBehaviourState) {
	        case ESCAPING:
	            timerEscape++;
	            if (timerEscape > 48) {
	                transitionTo(BehaviourState.HUNTING);
	            }
	            break;
	
	        case AVOIDING:
	            timerAvoid++;
	            if (timerAvoid > 36) {
	                transitionTo(BehaviourState.HUNTING);
	                color = RobotPane.green;
	            }
	            break;
	            
	        case HUNTING:
	            // Standard hunting logic (no timer needed)
	            break;
	    }
	
	    //apply Physics based on State
	    if (currentBehaviourState == BehaviourState.ESCAPING) {
	        //apply raw force for flight
	        speed.add(f); 
	        speed.limit(maxSpeed * 2.5f);
	    } else if (currentBehaviourState == BehaviourState.AVOIDING) {
	        //when avoiding blend the steering force 'f' with momentum
	        f.limit(0.8f); 
	        speed.add(f);
	        speed.limit(maxSpeed * 1.5f); //speed boost to get around the obstacle
	    } else {
	        f.limit(0.15f); //smooth steering hunting
	        speed.add(f);
	        speed.limit(maxSpeed);
	    }
	
	    pos.add(speed);
	
	    //update animation regardless of state
	    updateAnimation(panelSize);
	}

	//Helper to handle transitions cleanly
	private void transitionTo(BehaviourState newState) {
	    currentBehaviourState = newState;
	    timerEscape = 0;
	    timerAvoid = 0;
	
	    //synchronize legacy variables
	    this.hunt = (newState == BehaviourState.HUNTING);
	    
	    //update visual feedback
	    if (newState == BehaviourState.HUNTING) color = RobotPane.green;
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
	        if (r instanceof HunterBot) {
	            transitionTo(BehaviourState.ESCAPING);
	            return escape((HunterBot)r); 
	        }

	        //robot avoidance
	        if (r instanceof Robot && scale <= r.getScale()) {
	            //only transition if we aren't already avoiding, to reset the timer
	            if (currentBehaviourState != BehaviourState.AVOIDING) {
	                transitionTo(BehaviourState.AVOIDING);
	                color = Color.RED;
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

		if (dustTarget!=null && hunt == true) {
			// calculate the path to target point
			PVector path = PVector.sub(dustTarget.getPos(), pos);
			path.limit(0.1f);                            // max steering strength
			speed.add(path);
			speed.limit(maxSpeed);
			
			//check if bug reaches target
			if (targetCollisionCheck(dustTarget) && path.mag() - (scale * dia) / 2 <= 0 ) {
				reach = true;
				collectCount++;
				//System.out.println("Reached target at " + currentTarget.getPos());
				//speed.mult(0.5f);
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

	private PVector escape(HunterBot h) {
		//System.out.println("Escape from HunterBot " + h.getId());
		hunt = false;
		PVector normal = PVector.sub(this.pos, h.pos);
		//float distance = normal.mag();
		//normal.normalize();//PVector (target, current position)
		//float strength = 1.0f / (distance + 1);
		//PVector escapeVector = normal.copy().mult(strength * maxSpeed); // scale by distance
		//return escapeVector;
		return normal;
	}
	
	
}

