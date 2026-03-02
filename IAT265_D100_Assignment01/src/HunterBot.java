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
	private Shape panelArc;
	private Shape panelLineL;
	private Shape panelLineR;
	private Path2D face;
	private Shape eye;
	private Shape browL;
	private Shape browR;
	private Robot robotTarget;
	private enum State {
	    HUNTING,    // searching for and moving toward Robot
	    AVOIDING    // brief cooldown after interacting with another Hunter
	}
	private State currentState;

	
	public HunterBot(Dimension dim, int id) {
		super(dim, id);
		setShapes();
		color = RobotPane.amber;
		currentState = State.HUNTING;
	}

	
	@Override
	protected void setShapes() {
		float sCof = 0.15f;
		sight = width * maxSpeed/2 * sCof;
		robotArea = new Area();
		
		//body shapes (local coords)
		// outer circle
		outer = new Rectangle2D.Double(-dia / 2.0, -dia / 2.0, dia, dia);
		
		double d1 = dia * 4.0 / 5.0;
		inner = new Rectangle2D.Double(-d1 / 2.0, -d1 / 2.0, d1, d1);
		
		double d2 = dia * 1.0 / 10.0;
		button = new Ellipse2D.Double(-d2 / 2.0, d2 + 5, d2, d2);
		
		// power panel (arc + two lines)
		double d3 = dia / 4.0;
		panelArc = new Arc2D.Double(-d3 / 2.0, d3 - 10, d3, d3, 0, 180, Arc2D.OPEN);
		panelLineL = new Line2D.Double(-d3 / 2.0, d3, -d3 / 2.0, 33);
		panelLineR = new Line2D.Double(-d3 / 2.0 + d3, d3, -d3 / 2.0 + d3, 33);
		
		//face
		face = new Path2D.Double();
		face.moveTo(-15, -23);
		face.lineTo(-15, -15);
		face.quadTo(-15, -10, -10, -10);
		face.lineTo(10, -10);
		face.quadTo(15, -10, 15, -15);
		face.lineTo(15, -23);
		
		// eyes + brows
		eye = new RoundRectangle2D.Double(-3 / 2.0, -20, 3, 4, 2, 2);
		browL = new Line2D.Double(-10, -18, -7, -18);
		browR = new Line2D.Double(10, -18, 7, -18);
		
		
		fov = new Arc2D.Double(-sight, -sight, sight*2, sight*2, 45, 90, Arc2D.PIE);
		robotArea.add(new Area(outer));
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
		
		// outer circle
		g.setColor(Color.BLACK);
		g.fill(outer);
		g.setColor(color);
		g.draw(outer);
		
		//fov
		//g.draw(fov);
		
		// inner circle
		g.draw(inner);
		
		// button circle
		if (lightOn && timerLight > 24) {
			g.setColor(color);
		    g.fill(button);
		    timerLight = 0;
		} else {
		    g.setColor(Color.BLACK);
		    g.fill(button);
		    timerLight++;
		}
		
		//g.setColor(color);
		g.draw(button);
		
		// power panel (arc + two lines)
		g.setColor(color);
		g.draw(panelArc);
		g.draw(panelLineL);
		g.draw(panelLineR);
		
		// face
		g.draw(face);

		// eyes + brows
		g.draw(eye);
		g.draw(browL);
		g.draw(browR);
		
		//robot area outline
		g.setColor(RobotPane.amber);
		//robotArea.add(new Area(outer));
		if (displayInfo) g.draw(robotArea);
		g.setTransform(af);//reset for bounding box
		
		if (displayInfo) {
			g.draw(getBoundary().getBounds2D());
			g.draw(getFOV());
		}
		
	    if (displayInfo) {
	    	//display scale
			g.setColor(Color.WHITE);
		    g.setFont(new Font("Monospaced", Font.BOLD, 16));
		    String txtScale = "Scale " + String.format("%.2f", scale);
		    String txtSpeed = "Speed "+ String.format("%.2f", speed.mag());
		    String txtID = "ID " + id;
		    String txtHunt = "Hunt " + hunt;
		    String txtSeen = "Seen " + seen;
		    if (robotTarget != null) targetId = robotTarget.getId();
		    String txtTargetId = "target " + targetId;
		    String txtReTarget = "reTarget " + reTarget;
		    String txtCollect = "collect " + collectCount;
		    
	    	g.drawString(txtScale, pos.x, pos.y);
		    g.drawString(txtSpeed, pos.x, pos.y+15);
		    g.drawString(txtID, pos.x, pos.y+30);
		    g.drawString(txtHunt, pos.x, pos.y+45);
		    g.drawString(txtSeen, pos.x, pos.y+60);
		    g.drawString(txtTargetId, pos.x, pos.y+75);
		    g.drawString(txtReTarget, pos.x, pos.y+90);
		    g.drawString(txtCollect, pos.x, pos.y+105);
	    }
	    

   
	}
	
	@Override
	public void move(Dimension panelSize, PVector f) {
		//update State Timers (The missing part)
	    if (currentState == State.AVOIDING) {
	        timerAvoid++;
			color = Color.RED; // Visual feedback for being in AVOIDING state

	        if (timerAvoid > 40) { // Cooldown period
	            transitionTo(State.HUNTING);
	    		color = RobotPane.amber;

	        }
	    }

	    //physics logic
	    f.limit(0.3f); 
	    speed.add(f);
	    
	    //use the State to decide speed limits
	    if (currentState == State.AVOIDING) {
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
            if (currentState != State.AVOIDING) {
                transitionTo(State.AVOIDING);                
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
	private void transitionTo(State newState) {
	    currentState = newState;
	    timerAvoid = 0;
	
	    //synchronize legacy variables
	    this.hunt = (newState == State.HUNTING);
	    
	    //update visual feedback
	    if (newState == State.HUNTING) color = RobotPane.green;
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
