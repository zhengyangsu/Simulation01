

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
//import java.util.Random;

import processing.core.PVector;


public class Robot {
	
	//properties fields
	
	
    private PVector pos, speed;
    private int dia;
	private int disp;
	private Color color;
	//private Random dice = new Random();
	private double scale;
	//private double left;
    //private double right;
    //private double top;
    //private double bottom;
    private double theta;
    private double brushAngle = 0;
    private double brushSpeed = 0.25; // radians per frame
    private boolean lightOn;
    private Area robotArea;
    private int timer;
    
	
	
	//constructor
	public Robot(Dimension dim) {
		dia = 70;
		disp = 1;
		scale = 0.7;
		pos = new PVector(dim.width - 3 * RobotPane.margin,  3 * RobotPane.margin);
		speed = new PVector(disp,0);
		theta = Math.toRadians(90);
		lightOn = false;
		robotArea = new Area();
		timer =0;
		color = RobotPane.green;
	}
	
	//overloading
	/*public Robot(int x, int y, int size, Color c, int xSpeed, int ySpeed) {
		this.pos = new PVector(x, y);
		this.speed = new PVector(xSpeed, ySpeed);
		this.dia = size;
		color = c;
		
	}*/

	
	public void drawRobot(Graphics2D g) {
		 
		robotArea.reset();

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
		
		drawBrushes(g, 1);
		drawBrushes(g, -1);

		//body shapes (local coords)
		
		// outer circle
		Shape outer = new Ellipse2D.Double(-dia / 2.0, -dia / 2.0, dia, dia);
		g.setColor(Color.BLACK);
		g.fill(outer);
		g.setColor(color);
		g.draw(outer);
		

		// inner circle
		double d = dia * 4.0 / 5.0;
		Shape inner = new Ellipse2D.Double(-d / 2.0, -d / 2.0, d, d);
		g.draw(inner);
		
		// button circle
		d = dia * 1.0 / 10.0;
		Shape button = new Ellipse2D.Double(-d / 2.0, d + 5, d, d);
		if (lightOn && timer > 24) {
			g.setColor(color);
		    g.fill(button);
		    timer = 0;
		} else {
		    g.setColor(Color.BLACK);
		    g.fill(button);
		    timer++;
		}
		
		//g.setColor(color);
		g.draw(button);
		
		// power panel (arc + two lines)
		g.setColor(color);
		d = dia / 4.0;
		Shape panelArc = new Arc2D.Double(-d / 2.0, d - 10, d, d, 0, 180, Arc2D.OPEN);
		Shape panelLineL = new Line2D.Double(-d / 2.0, d, -d / 2.0, 33);
		Shape panelLineR = new Line2D.Double(-d / 2.0 + d, d, -d / 2.0 + d, 33);
		
		g.draw(panelArc);
		g.draw(panelLineL);
		g.draw(panelLineR);
		
		//face
		Path2D face = new Path2D.Double();
		face.moveTo(-15, -23);
		face.lineTo(-15, -15);
		face.quadTo(-15, -10, -10, -10);
		face.lineTo(10, -10);
		face.quadTo(15, -10, 15, -15);
		face.lineTo(15, -23);
		g.draw(face);


		// eyes + brows
		Shape eye = new RoundRectangle2D.Double(-3 / 2.0, -20, 3, 4, 2, 2);
		Shape browL = new Line2D.Double(-10, -18, -7, -18);
		Shape browR = new Line2D.Double(10, -18, 7, -18);
		
		g.draw(eye);
		g.draw(browL);
		g.draw(browR);
		
		g.setColor(RobotPane.amber);
		robotArea.add(new Area(outer));
		 
		
		g.draw(robotArea);
		g.setTransform(af);
		
		g.draw(getBoundary().getBounds2D());
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
	    robotArea.add(new Area(transformedOutline));
	    
	    g.setTransform(old);;
	    
	    
	   
	    
	}
	
	
	public void move(Dimension panelSize) {
		
		if (lightOn) lightOn = false;
		else lightOn = true;
		
		pos.add(speed);
		collisionValidate(panelSize);
		//advanceCollision(panelSize);
		
		
		brushAngle += brushSpeed;

		// keep angle from growing forever 
		if (brushAngle > Math.PI * 2) {
		    brushAngle -= Math.PI * 2;
		}
		reset(panelSize);//if out of bounds, reset to center
		
		//System.out.println(pos);
	}
	
	private Rectangle2D getBounds() {
		
		
		double brushMargin = scale * 16;
		double r = scale * (dia / 2.0);
		double x   = pos.x - r - brushMargin;
	    double y    = pos.y - r - brushMargin;
	    double size   = 2 * (r + brushMargin);
	    

		return new Rectangle2D.Double(x, y, size, size);

	}
	
	private Shape getBoundary() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
	    at.rotate(theta);
		at.rotate(speed.heading());
		at.scale(scale, scale);
		if (speed.x < 0) at.scale(-1, 1);
		return at.createTransformedShape(robotArea);
	}
	
	private boolean collides(DustPile pile) {
		
		boolean collision = (getBoundary().intersects(pile.getBoundary().getBounds2D()) &&
		        			pile.getBoundary().intersects(getBoundary().getBounds2D()));
		
		if (collision) System.out.println("Collision: " + collision);
		return collision;

	}
	
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
	
	
	public PVector getPos() {
		return pos;
	}
	
	public double getRadius() {
		return scale * (dia / 2.0);
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
	
	boolean approach(DustPile targ) {
		
		boolean reach = false;
		
		// calculate the path to target point
		PVector path = PVector.sub(targ.getPos(), pos);

		// returns the direction as angle
		float angle = path.heading();

		// make a speed that points toward the target and then move
		speed = PVector.fromAngle(angle);
		//speed.mult(2);

		// check if bug reaches target
		if (collides(targ) && path.mag() - (scale * dia) / 2 <= 0 ) {
			reach = true;
			System.out.println("Reached target at " + targ.getPos());
			//speed.mult(0.5f);
		}

		return reach;
	}
	
	
	
}

