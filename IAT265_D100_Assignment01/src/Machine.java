

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


public abstract class Machine {
	
	//properties fields
	protected int width;
	protected int height;
	protected PVector pos, speed;
	protected float maxSpeed;
	protected int dia;
	protected int id;
	protected int targetId;
	protected Color color;
	protected Random dice = new Random();
	protected double scale;
	protected double theta;
	protected boolean lightOn;
	protected boolean hunt;
	protected boolean seen;
	protected boolean reTarget;
	protected boolean displayInfo;
	protected int collectCount;
	protected int timerHunt;
	protected Area robotArea;
	protected int timerLight;
	protected DustPile currentTarget;
	protected ArrayList<DustPile> targets;
	protected Arc2D.Double fov; //field-of-view
	protected float sight;
	protected Shape outer;
	protected Shape inner;
	protected Shape button;

	
	//constructor
	public Machine(Dimension dim, int id) {
		this.id = id;
		dia = 70;
		scale = dice.nextDouble(0.6, 1);
		maxSpeed = 2;
		this.width = dim.width;
		this.height = dim.height;
		float x = (float)dice.nextDouble(RobotPane.margin + dia, dim.width - RobotPane.margin - dia);
		float y = (float)dice.nextDouble(RobotPane.margin + dia, dim.height - RobotPane.margin - dia);
		pos = new PVector(x , y);
		speed = new PVector(dice.nextInt(1, 10), dice.nextInt(1, 10));
		speed.limit(maxSpeed);
		theta = Math.toRadians(90);
		lightOn = false;
		robotArea = new Area();
		timerLight =0;
		color = RobotPane.green;
		currentTarget = null;
		hunt = true;
		reTarget = false;
		collectCount = 0;
		
		setShapes();
	}
	

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
		
		fov = new Arc2D.Double(-sight, -sight, sight*2, sight*2, 45, 90, Arc2D.PIE);
		robotArea.add(new Area(outer));
	}
	
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
	
	
	public void move(Dimension panelSize, PVector f) {
		
		if (lightOn) lightOn = false;
		else lightOn = true;
	

		f.limit(0.3f);//Steering force
		speed.add(f);//combined force
		speed.limit(maxSpeed);
		pos.add(speed);
		
		//if (hunt) pos.setMag(maxSpeed);
		
		collisionValidate(panelSize);

		reset(panelSize);//if out of bounds, reset to center
		
		//System.out.println(pos);
	}
	
	
	protected Rectangle2D getBounds() {
		return getBoundary().getBounds2D();
	  
	}
	
	//returns outline
	public Shape getBoundary() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
	    at.rotate(theta);
		at.rotate(speed.heading());
		at.scale(scale, scale);
		if (speed.x < 0) at.scale(-1, 1);
		return at.createTransformedShape(robotArea);
	}
	
	public PVector seen(Machine r) {
		
		if (scale > r.getScale()) return new PVector(0, 0);
		
		Rectangle2D robotBound = r.getBounds();
		Rectangle2D myBound = getBoundary().getBounds2D();
		Shape FOVOutline = getFOV();
		Shape robotOutline = r.getBoundary();
		Shape myOutline = getBoundary();
		
		
		PVector forceVector = new PVector(0, 0);
		
		
		//intersects
		if (FOVOutline.intersects(robotBound) || myOutline.intersects(robotBound) || robotOutline.intersects(myBound)) {
			forceVector = PVector.sub(this.pos, r.pos);
			color = new Color(255,0,0);
			seen = true;
			hunt = false;
			if (targets != null && targets.size() > 1) {
				targets.remove(0);
				currentTarget = targets.get(0);
				reTarget = true;
			}

		}else {
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
	
	//target collision
	protected abstract boolean targetCollisionCheck(Object target);
	
	//wall collision
	protected void collisionValidate(Dimension panelSize) {
		
		Shape bnd = getBoundary();
	    Rectangle2D.Double top = new Rectangle2D.Double(0, 0, panelSize.width, RobotPane.margin);
	    Rectangle2D.Double bottom = new Rectangle2D.Double(0, panelSize.height - RobotPane.margin, panelSize.width, RobotPane.margin);
	    Rectangle2D.Double left = new Rectangle2D.Double(0, 0, RobotPane.margin, panelSize.height);
	    Rectangle2D.Double right = new Rectangle2D.Double(panelSize.width - RobotPane.margin, 0, RobotPane.margin, panelSize.height);

	    if (bnd.intersects(left) && speed.x < 0) speed.x *= -1;
	    if (bnd.intersects(right) && speed.x > 0) speed.x *= -1;
	    if (bnd.intersects(top) && speed.y < 0) speed.y *= -1;
	    if (bnd.intersects(bottom) && speed.y > 0) speed.y *= -1;
	    
	}
	
	public abstract boolean approach();
	
	protected Shape getFOV() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.rotate(theta);
		at.rotate(speed.heading());
		at.scale(scale, scale);
		if (speed.x < 0) {
		    at.scale(-1, 1); // flip
		}
		return at.createTransformedShape(fov);
	}
	
	protected void reset(Dimension panelSize) {
		Rectangle2D panelBounds =
			    new Rectangle2D.Double(
			        0,
			        0,
			        panelSize.width,
			        panelSize.height
			    );

			if (!panelBounds.contains(getBounds())) {
			    pos.set(panelSize.width / 2f, panelSize.height / 2f);
			}
	}
	
	public void displayInfo(boolean display) {
		this.displayInfo = display;
	}
	
	
	//getter, setter
	
	protected int getId() {
		return id;
	}
	
	public PVector getPos() {
		return pos;
	}
	
	public double getRadius() {
		return scale * (dia / 2.0);
	}
	
	public double getScale() {
		return scale;
	}
	
	public int getDia() {
		return dia;
	}

}

