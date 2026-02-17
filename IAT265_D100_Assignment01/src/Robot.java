

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.Random;

import processing.core.PVector;


public class Robot {
	
	//properties fields
	
	private int width;
	private int height;
    private PVector pos, speed;
    private float speedLimit;
    private int dia;
	private int disp;
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
    private Area robotArea;
    private int timer;
    private DustPile target;
    
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
	public Robot(Dimension dim) {
		dia = 70;
		disp = 1;
		scale = dice.nextDouble(0.6, 1);
		speedLimit = 2;
		this.width = dim.width;
		this.height = dim.height;
		float x = (float)dice.nextDouble(RobotPane.margin + dia, dim.width - RobotPane.margin - dia);
		float y = (float)dice.nextDouble(RobotPane.margin + dia, dim.height - RobotPane.margin - dia);

		pos = new PVector(x , y);

		speed = new PVector(disp,0);
		speed.limit(speedLimit);
		
		theta = Math.toRadians(90);
		lightOn = false;
		robotArea = new Area();
		timer =0;
		color = RobotPane.green;
		target = null;
	}
	

	private void setShapes() {
		float sCof = 0.15f;
		sight = width * speedLimit * sCof;
		
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
	}
	
	public void drawRobot(Graphics2D g) {
		setShapes(); 
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
		
		// brushes
		drawBrushes(g, 1);
		drawBrushes(g, -1);

		// outer circle
		g.setColor(Color.BLACK);
		g.fill(outer);
		g.setColor(color);
		g.draw(outer);
		
		//fov
		g.draw(fov);
		
		// inner circle
		g.draw(inner);
		
		// button circle
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
		robotArea.add(new Area(outer));
		g.draw(robotArea);
		g.setTransform(af);//reset for bounding box
		g.draw(getBoundary().getBounds2D());
		
		g.draw(getFOV());
		
		// Display scale
		g.setColor(Color.WHITE);
	    g.setFont(new Font("Monospaced", Font.BOLD, 24));
	    String text = String.format("%.2f", scale);;
	    g.drawString(text, pos.x, pos.y);
		
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
	
	boolean approach(PVector f) {
		
		boolean reach = false;
		
		if (target!=null) {
			// calculate the path to target point
			PVector path = PVector.sub(target.getPos(), pos);

			// returns the direction as angle
			float angle = path.heading();

			// make a speed that points toward the target and then move
			speed = PVector.fromAngle(angle);

			// check if bug reaches target
			if (collides(target) && path.mag() - (scale * dia) / 2 <= 0 ) {
				reach = true;
				System.out.println("Reached target at " + target.getPos());
				//speed.mult(0.5f);
			}
		}
		
		//Steering along the wall
		PVector wallSteerAccel = f.div((float)scale);
		float speedValue = speed.mag();
		speed.add(wallSteerAccel);
		speed.normalize().mult(speedValue);
		pos.add(speed);
		
		return reach;
	}
	
	public Shape getFOV() {
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
	
	public void setTarget(DustPile target) {
		this.target = target;
	}
	
	public DustPile getTarget() {
		return target;
	}
	
	
	
	
	
	
}

