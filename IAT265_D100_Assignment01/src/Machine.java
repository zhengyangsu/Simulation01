/*Abstract class Machine
 * 
*/

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
	//region
	protected PVector pos, vel;
	protected float maxSpeed, sight;
	protected int width, height, dia, id, targetId, collectCount, timerAvoid, timerLight, timerEscape;
	protected Color color;
	protected Random dice = new Random();
	protected double scale, theta;
	protected boolean lightOn, hunt, seen, reTarget, displayInfo, dead;
	protected DustPile dustTarget;
	protected ArrayList<DustPile> targets;
	protected Shape outer, inner, button, panelArc, panelLineL, panelLineR, eye, browL, browR, fov;
	protected Path2D face;
	protected Area robotArea;
	protected Dimension dim;
	
	protected enum BehaviourState {
	    HUNTING,    // searching for and moving toward Robot
	    ESCAPING,   //high-speed flight from a HunterBot
	    AVOIDING,    // brief cooldown after interacting with another Hunter
	    SEARCHING	    
	}
	protected BehaviourState currentBehaviourState;
	
	protected enum EnergyState{
		NORMAL,
		WEAK,
		DEAD
	}
	protected EnergyState currentEnergyState;
	protected int energy, fullEnergy;
	protected float engGainRatio = 100;                   //Energy gained per food size unit 
	protected float engLossRatio = fullEnergy/(30*15);    //Energy loss per frame
	//endregion
	
	//constructor
	public Machine(Dimension dim, int id) {
		this.id = id;
		this.dim = dim;
		dia = 70;
		scale = dice.nextDouble(0.6, 1);
		maxSpeed = 2;
		width = dim.width;
		height = dim.height;
		float x = (float)dice.nextDouble(RobotPane.margin + dia, dim.width - RobotPane.margin - dia);
		float y = (float)dice.nextDouble(RobotPane.margin + dia, dim.height - RobotPane.margin - dia);
		pos = new PVector(x , y);
		vel = new PVector(dice.nextInt(1, 10), dice.nextInt(1, 10));
		vel.limit(maxSpeed);
		theta = Math.toRadians(90);
		lightOn = false;
		robotArea = new Area();
		timerLight = 0;
		timerEscape = 0;
		color = RobotPane.green;
		dustTarget = null;
		hunt = true;
		reTarget = false;
		collectCount = 0;
		currentBehaviourState = BehaviourState.SEARCHING;
		currentEnergyState = EnergyState.NORMAL;
		energy = 1000;
		fullEnergy = 1000;
		engGainRatio = 100;                   //Energy gained per food size unit 
		engLossRatio = fullEnergy/(30*15);
		dead = false;
		setShapes();
	}
	
	protected void setShapes() {
		float sCof = 0.15f;
		sight = width * maxSpeed/2 * sCof;
		robotArea = new Area();
		
		//body shapes (local coords)
		//outer circle
		outer = new Ellipse2D.Double(-dia / 2.0, -dia / 2.0, dia, dia);
		
		double d1 = dia * 4.0 / 5.0;
		inner = new Ellipse2D.Double(-d1 / 2.0, -d1 / 2.0, d1, d1);
		
		double d2 = dia * 1.0 / 10.0;
		button = new Ellipse2D.Double(-d2 / 2.0, d2 + 5, d2, d2);
		
		//power panel (arc + two lines)
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
		
		//eyes + brows
		eye = new RoundRectangle2D.Double(-3 / 2.0, -20, 3, 4, 2, 2);
		browL = new Line2D.Double(-10, -18, -7, -18);
		browR = new Line2D.Double(10, -18, 7, -18);
		
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
		g.rotate(vel.heading());
		g.scale(scale, scale);
		if (vel.x < 0) {
		    g.scale(-1, 1); // flip
		}

		
		
		// outer circle
		g.setColor(Color.BLACK);
		g.fill(outer);
		g.setColor(color);
		g.draw(outer);
		
		// inner circle
		g.draw(inner);
		
		// button circle
		if (timerLight > 24) {
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

		if (currentEnergyState == EnergyState.DEAD) {
	  		drawWaves(g);
			//g.draw(new Ellipse2D.Double(-dia/2, -dia/2, dia, dia));

	    }
		
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
		    String txtSpeed = "Speed "+ String.format("%.2f", vel.mag());
		    String txtID = "ID " + id;
		    String txtHunt = "Hunt " + hunt;
		    String txtSeen = "Seen " + seen;
		    if (dustTarget != null) targetId = dustTarget.getId();
		    String txtTargetId = "target " + targetId;
		    String txtReTarget = "reTarget " + reTarget;
		    String txtCollect = "collect " + collectCount;
		    String txtEnergy = "energy " + energy;
		    
	    	g.drawString(txtScale, pos.x, pos.y);
		    g.drawString(txtSpeed, pos.x, pos.y+15);
		    g.drawString(txtID, pos.x, pos.y+30);
		    g.drawString(txtHunt, pos.x, pos.y+45);
		    g.drawString(txtSeen, pos.x, pos.y+60);
		    g.drawString(txtTargetId, pos.x, pos.y+75);
		    g.drawString(txtReTarget, pos.x, pos.y+90);
		    g.drawString(txtCollect, pos.x, pos.y+105);
		    g.drawString(txtEnergy, pos.x, pos.y+120);

	    }
   
	    
	    
	}
	
	public abstract void move(Dimension panelSize, PVector f);
	
	protected Rectangle2D getBounds() {
		return getBoundary().getBounds2D();
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
				dustTarget = targets.get(0);
				reTarget = true;
			}

		}else {
			color = RobotPane.green;
			seen = false;
			timerAvoid ++;
			if (timerAvoid > 96) {
				hunt = true;
				timerAvoid = 0;
			}

		}
		return forceVector;
	}
	
	//Abstract methods
	//target collision
	protected abstract boolean targetCollisionCheck(Object target);
	//approach targets
	public abstract boolean approach();
	
	//wall collision
	protected void collisionValidate(Dimension panelSize) {
		
		Shape bnd = getBoundary();
	    Rectangle2D.Double top = new Rectangle2D.Double(0, 0, panelSize.width, RobotPane.margin);
	    Rectangle2D.Double bottom = new Rectangle2D.Double(0, panelSize.height - RobotPane.margin, panelSize.width, RobotPane.margin);
	    Rectangle2D.Double left = new Rectangle2D.Double(0, 0, RobotPane.margin, panelSize.height);
	    Rectangle2D.Double right = new Rectangle2D.Double(panelSize.width - RobotPane.margin, 0, RobotPane.margin, panelSize.height);

	    if (bnd.intersects(left) && vel.x < 0) vel.x *= -1;
	    if (bnd.intersects(right) && vel.x > 0) vel.x *= -1;
	    if (bnd.intersects(top) && vel.y < 0) vel.y *= -1;
	    if (bnd.intersects(bottom) && vel.y > 0) vel.y *= -1;
	    
	}

	//Helper to handle transitions cleanly
	protected void transitionTo(BehaviourState newState) {
	    currentBehaviourState = newState;
	    timerEscape = 0;
	    timerAvoid = 0;
	
	    //synchronize legacy variables
	    this.hunt = (newState == BehaviourState.HUNTING);
	    
	    //update visual feedback
	    if (newState == BehaviourState.HUNTING) color = RobotPane.green;
	}
	
	protected void reset(Dimension panelSize) {
		Rectangle2D panelBounds = new Rectangle2D.Double(0, 0, panelSize.width, panelSize.height);

			if (!panelBounds.contains(getBounds())) {
			    pos.set(panelSize.width / 2f, panelSize.height / 2f);
			}
	}
	
	public void displayInfo(boolean display) {
		this.displayInfo = display;
	}
	
	protected void areaReset(){
		robotArea.reset();
		robotArea.add(new Area(outer));
	}

	// method for drawing traces after avatar was killed
	protected void drawWaves(Graphics2D g) {
		
		for (int i = 1; i <= 3; i++) {
			g.scale(i, i);
			g.draw(new Ellipse2D.Double( -dia/2, -dia/2, dia, dia));
		}
		
	}
	
	//getter, setter
	//returns outline
	public Shape getBoundary() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
	    at.rotate(theta);
		at.rotate(vel.heading());
		at.scale(scale, scale);
		if (vel.x < 0) at.scale(-1, 1);
		return at.createTransformedShape(robotArea);
	}
	
	protected Shape getFOV() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.rotate(theta);
		at.rotate(vel.heading());
		at.scale(scale, scale);
		if (vel.x < 0) {
		    at.scale(-1, 1); // flip
		}
		return at.createTransformedShape(fov);
	}
	
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
	
	public double getSize() {
		return scale * dia;
	}
	
	public int getDia() {
		return dia;
	}

	public void setShape() {
		// TODO Auto-generated method stub
		
	}

	public void kill() {
		dead = true;
		currentEnergyState = EnergyState.DEAD;
	}
	
	public boolean isDead() {
		return dead;
	}
}

