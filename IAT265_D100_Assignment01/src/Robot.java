

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

import processing.core.PVector;


public class Robot extends Machine{
	
	//properties fields
	private int timerEscape;
	private Shape panelArc;
	private Shape panelLineL;
	private Shape panelLineR;
	private Path2D face;
	private Shape eye;
	private Shape browL;
	private Shape browR;
	private double brushAngle = 0;
	private double brushSpeed = 0.25; // radians per frame
	private boolean escape;
	
	//constructor
	public Robot(Dimension dim, int id) {
		super(dim, id);	
		timerEscape = 0;
		escape = !hunt;
		setShapes();
	}
	
	@Override
	protected void setShapes() {
		float sCof = 0.15f;
		sight = width * maxSpeed/2 * sCof;
		robotArea = new Area();
		
		//body shapes (local coords)
		// outer circle
		outer = new Ellipse2D.Double(-dia / 2.0, -dia / 2.0, dia, dia);
		
		double d1 = dia * 4.0 / 5.0;
		inner = new Ellipse2D.Double(-d1 / 2.0, -d1 / 2.0, d1, d1);
		
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
		
		// brushes
		drawBrushes(g, 1);
		drawBrushes(g, -1);

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
	    	// Display scale
			g.setColor(Color.WHITE);
		    g.setFont(new Font("Monospaced", Font.BOLD, 16));
		    String txtScale = "Scale " + String.format("%.2f", scale);
		    String txtSpeed = "Speed "+ String.format("%.2f", speed.mag());
		    String txtID = "ID " + id;
		    String txtHunt = "Hunt " + hunt;
		    String txtSeen = "Seen " + seen;
		    if (currentTarget != null) targetId = currentTarget.getId();
		    String txtTargetId = "target " + targetId;
		    String txtReTarget = "reTarget " + reTarget;
		    String txtCollect = "collect " + collectCount;
		    String txtEscape = "escape " + escape;
		    
	    	g.drawString(txtScale, pos.x, pos.y);
		    g.drawString(txtSpeed, pos.x, pos.y+15);
		    g.drawString(txtID, pos.x, pos.y+30);
		    g.drawString(txtHunt, pos.x, pos.y+45);
		    g.drawString(txtSeen, pos.x, pos.y+60);
		    g.drawString(txtTargetId, pos.x, pos.y+75);
		    g.drawString(txtReTarget, pos.x, pos.y+90);
		    g.drawString(txtCollect, pos.x, pos.y+105);
		    g.drawString(txtEscape, pos.x, pos.y+120);
	    }
	    

   
	}
	
	//Draw moving brushes
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
	    
	    //g.draw(bristleOutline);
	    //robotArea.add(new Area(transformedOutline));
	    
	    g.setTransform(old);;
	    
	    
	   
	    
	}
	
	@Override
	public void move(Dimension panelSize, PVector f) {
		
		if (lightOn) lightOn = false;
		else lightOn = true;
	
		if(!escape) f.limit(0.3f);//Steering force
		speed.add(f);//combined force
		if(!escape)speed.limit(maxSpeed);
		else speed.limit(maxSpeed * 3.5f);
		pos.add(speed);
		
		//if (hunt) pos.setMag(maxSpeed);
		collisionValidate(panelSize);

		brushAngle += brushSpeed;

		// keep angle from growing forever 
		if (brushAngle > Math.PI * 2) {
		    brushAngle -= Math.PI * 2;
		}
		reset(panelSize);//if out of bounds, reset to center
		
		//System.out.println(pos);
	}
	
	@Override
	public PVector seen(Machine r) {
		
		Rectangle2D robotBound = r.getBounds();
		Rectangle2D myBound = getBoundary().getBounds2D();
		Shape FOVOutline = getFOV();
		Shape robotOutline = r.getBoundary();
		Shape myOutline = getBoundary();
		PVector forceVector = new PVector(0, 0); 
		Boolean intersect = false;
		
		if (FOVOutline.intersects(robotBound) || myOutline.intersects(robotBound) || robotOutline.intersects(myBound)) intersect = true;
		
		if (r instanceof HunterBot && intersect) {
			return escape((HunterBot) r); 
		}
		
		if (scale > r.getScale()) return new PVector(0, 0);
			
		//intersects with another robot
		if (r instanceof Robot && intersect) {
			forceVector = PVector.sub(this.pos, r.pos);
			color = new Color(255,0,0);
			seen = true;
			hunt = false;
			if (targets != null && targets.size() > 1) {
				targets.remove(0);
				currentTarget = targets.get(0);
				reTarget = true;
			}

		}
		
		else {
			color = RobotPane.green;
			seen = false;
			timerHunt ++;
			if (timerHunt > 96) {
				hunt = true;
				timerHunt = 0;
			}
		}			
		return forceVector;
	
	}
	
	@Override
	protected boolean targetCollisionCheck(Object target) {
		boolean collision = false;
		
		if (target instanceof DustPile) {
			DustPile pile = (DustPile) target;
			collision = (getBoundary().intersects(pile.getBoundary().getBounds2D()) && pile.getBoundary().intersects(getBoundary().getBounds2D()));
			if (collision) System.out.println("Collision: " + collision);
		}
		
		return collision;

	}
	
	@Override
	public boolean approach() {
				
		boolean reach = false;

		if (currentTarget!=null && hunt == true) {
			// calculate the path to target point
			PVector path = PVector.sub(currentTarget.getPos(), pos);
			path.limit(0.1f);                            // max steering strength
			speed.add(path);
			speed.limit(maxSpeed);
			
			// check if bug reaches target
			if (targetCollisionCheck(currentTarget) && path.mag() - (scale * dia) / 2 <= 0 ) {
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
		if (this.targets != null && currentTarget == null) {
			this.currentTarget = this.targets.get(0);
			reTarget = false;
		}
		else currentTarget = null;
	}
	
	public DustPile getTarget() {
		return currentTarget;
	}

	private PVector escape(HunterBot h) {
		System.out.println("Escape from HunterBot " + h.getId());
		hunt = false;
		escape = true;
		PVector normal = PVector.sub(this.pos, h.pos);
		float distance = normal.mag();
		normal.normalize();//PVector (target, current position)
		if (timerEscape < 240) {
			timerEscape++;
		} else {
			hunt = true;
			escape = false;
			timerEscape = 0;
			return new PVector(0, 0);
		}
		
		float strength = 1.0f / (distance + 1);
		PVector escapeVector = normal.copy().mult(strength * maxSpeed); // scale by distance
		return escapeVector;
	}
	
	
}

