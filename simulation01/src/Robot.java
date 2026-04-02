

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


public class Robot {
	
	//properties fields
	
	private int width;
	private int height;
    private PVector pos, speed;
    private float maxSpeed;
    private int dia;
	private int id;
	private int targetId;
	private Color color;
	private Random dice = new Random();
	private double scale;
	//private double left;
    //private double right;
    //private double top;
    //private double bottom;
    private double theta;
    private double brushAngle = 0;
    private double brushSpeed = 0.25; // radians per frame
    private boolean lightOn;
    private boolean hunt;
    private boolean seen;
    private boolean reTarget;
    private boolean displayInfo;
    private int collectCount;
    private int timerHunt;
    private Area robotArea;
    private int timerLight;
    private DustPile currentTarget;
    private ArrayList<DustPile> targets;
	private Arc2D.Double fov; //field-of-view
	private float sight;
	private Shape outer;
	private Shape inner;
	private Shape button;
	private Shape panelArc;
	private Shape panelLineL;
	private Shape panelLineR;
	private Path2D face;
	private Shape eye;
	private Shape browL;
	private Shape browR;
	
	//constructor
	public Robot(Dimension dim, int id) {
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
	

	private void setShapes() {
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
	
	public void drawRobot(Graphics2D g) {

		//robotArea.reset();

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
	
	public void move(Dimension panelSize, PVector f) {
		
		if (lightOn) lightOn = false;
		else lightOn = true;
	

		f.limit(0.3f);//Steering force
		speed.add(f);//combined force
		speed.limit(maxSpeed);
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
	
	
	private Rectangle2D getBounds() {
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
	
	public PVector seen(Robot r) {
		
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
	
	
	//dust collision
	private boolean collides(DustPile pile) {
		
		boolean collision = (getBoundary().intersects(pile.getBoundary().getBounds2D()) &&
		        			pile.getBoundary().intersects(getBoundary().getBounds2D()));
		if (collision) System.out.println("Collision: " + collision);
		return collision;

	}
	
	//wall collision
	private void collisionValidate(Dimension panelSize) {
		
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
	
	boolean approach() {
		
		
		boolean reach = false;

		if (currentTarget!=null && hunt == true) {
			// calculate the path to target point
			PVector path = PVector.sub(currentTarget.getPos(), pos);


			path.limit(0.1f);                            // max steering strength
			speed.add(path);
			speed.limit(maxSpeed);
			
			// check if bug reaches target
			if (collides(currentTarget) && path.mag() - (scale * dia) / 2 <= 0 ) {
				reach = true;
				collectCount++;
				//System.out.println("Reached target at " + currentTarget.getPos());
				//speed.mult(0.5f);
			}
		}
		return reach;
	}
	
	private Shape getFOV() {
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
	
	
	private void reset(Dimension panelSize) {
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
	
	boolean isLightOn() {
		return lightOn;
	}
	
	public void displayInfo(boolean display) {
		this.displayInfo = display;
	}
	
	
	//getter, setter
	
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
	
	
	
	
	
	
}

