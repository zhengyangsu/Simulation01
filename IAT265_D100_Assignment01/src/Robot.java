

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
		//dir = 1;
		pos = new PVector(dim.width/2,  dim.height/2);
		//speed = new PVector(dice.nextInt(1, disp), dice.nextInt(1, disp));
		speed = new PVector(disp,0);
		theta = Math.toRadians(90);
		lightOn = false;
		robotArea = new Area();
		timer =0;
		//left   = pos.x - scale*(dia/2);
	    //right  = (int)pos.x + scale*(dia/2);
	    //top    = (int)pos.y - scale*(dia/2);
	    //bottom = (int)pos.y + scale*(dia/2);
	    
	   
		
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
		 
		
		/*AffineTransform af = g.getTransform();
		g.setColor(color);
		g.setStroke(new BasicStroke(RobotPane.stroke * 1.5f));
		g.translate((int)pos.x, (int)pos.y);
		g.rotate(theta);
		g.rotate(speed.heading());

		
		g.scale(scale, scale);
		if (speed.x < 0) { 
			g.scale(-1, 1); // axis rotated so flipping x
			//System.out.println("Flipped");
		}

		
		drawBrushes(g, 1);  // right brush
		drawBrushes(g, -1); // left brush
		 
		g.setColor(Color.BLACK);
		
		//outer circle
		g.fillOval(-dia/2, -dia/2, dia, dia);
		g.setColor(color);
		g.drawOval(-dia/2, -dia/2, dia, dia);
		
		//inner circle
		int d = dia * 4/5;
		g.drawOval(-d/2, -d/2, d, d);
		
		//button circle
		d = dia * 1/10;
		g.drawOval(-d/2, d + 5, d, d);
		
		//power panel
		d = dia / 4;
		g.drawArc(-d/2, d - 10, dia/4, dia/4, 0, 180);
		g.drawLine(-d/2, d , - d/2, 33);
		g.drawLine(-d/2 + dia/4, d , -d/2 + dia/4, 33);

		
		//robot "face"
		Path2D path = new Path2D.Double();
		path.moveTo(-15, -23);          // start
		path.lineTo(-15, -15);          // approach corner

		// rounded corner
		path.quadTo(-15, -10, -10, -10);

		// continue horizontally
		path.lineTo(10, -10);
		path.quadTo(15, -10, 15, -15);
		path.lineTo(15, -23);

		g.draw(path); 
		//robot "eyes"
		g.drawRoundRect(-3/2, -20, 3, 4, 2, 2);//g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
		g.drawLine(-10, -18, -7, -18);
		g.drawLine(10, -18, 7, -18);

		

		
		g.setTransform(af);*/
		
		
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
		robotArea.add(new Area(outer));

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
		
		g.setTransform(af);
		
	}
	

	private void drawBrushes(Graphics2D g, int side) {
		// TODO Auto-generated method stub
		
		/*int pivotX = 27 * side;
	    int pivotY = -22;

	    int length = 18;
	    double spread = Math.toRadians(20);
	    double baseAngle = Math.toRadians(-135);

	    AffineTransform brushAt = g.getTransform();

	    // move pivot to origin
	    g.translate(pivotX, pivotY);

	    // rotate brush
	    g.rotate(brushAngle * side); // opposite spin 

	    // draw brush relative to (0,0)
	    int count = 3;
	    for (int i = 0; i < count; i++) {
	        double t = (i - (count - 1) / 2.0);
	        double a = baseAngle + t * spread;

	        int endX = (int)(Math.cos(a) * length);
	        int endY = (int)(Math.sin(a) * length);

	        g.drawLine(0, 0, endX, endY);
	    }

	    g.setTransform(brushAt);*/
		
		int pivotX = 27 * side;
	    int pivotY = -22;

	    double length = 18;                
	    double spread = Math.toRadians(20);
	    double baseAngle = Math.toRadians(-135);

	    AffineTransform old = g.getTransform();

	    // move pivot to origin
	    g.translate(pivotX, pivotY);

	    // rotate brush
	    g.rotate(brushAngle * side);

	    int count = 3;
	    for (int i = 0; i < count; i++) {
	        double t = (i - (count - 1) / 2.0);
	        double a = baseAngle + t * spread;

	        double endX = Math.cos(a) * length;
	        double endY = Math.sin(a) * length;

	        Shape bristle = new Line2D.Double(0, 0, endX, endY);
	        robotArea.add(new Area(bristle));
	        g.draw(bristle);
	    }

	    g.setTransform(old);

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
		
		
		/*double radius = dia / 2.0;
		double marginX = maxX + radius;
		double marginY = maxY + radius;
		
		
		
		double r = scale * (dia / 2.0);
	    left   = pos.x - r;
	    right  = pos.x + r;
	    top    = pos.y - r;
	    bottom = pos.y + r;*/
		
		double brushMargin = scale * 16;
		double r = scale * (dia / 2.0);
		double x   = pos.x - r - brushMargin;
	    double y    = pos.y - r - brushMargin;
	    double size   = 2 * (r + brushMargin);
	    
		
	    //System.out.println("Robot bounds left: " + left + ", top: " + top + ", width: " + (scale * 2 * (radius + marginX)) + ", height: " + (scale * 2 * radius + marginY));
	    //System.out.println("Robot position: " + pos);
	    //System.out.println("maxX: " + maxX + ", maxY: " + maxY + ", minX: " + minX + ", minY: " + minY);
		//return new Rectangle2D.Double(left, top, scale * 2 * (radius + marginX), scale * 2 * radius + marginY);
		return new Rectangle2D.Double(x, y, size, size);

	}
	
	private void collisionValidate(Dimension panelSize) {
		
	    /*System.out.println(
	    	    "runtime right = " + (panelSize.width - RobotPane.margin) +
	    	    " | static rB = " + RobotPane.rB
	    	);*/
		
		
		
		Rectangle2D bounds = getBounds();
		
	    
	    if (bounds.getMinX() <= RobotPane.margin || bounds.getMaxX() >= panelSize.width - RobotPane.margin) {
	        speed.x *= -1;
	        //System.out.println("pos.x " + pos.x);

	    }

	    if (bounds.getMinY() <= RobotPane.margin || bounds.getMaxY() >= panelSize.height - RobotPane.margin) {
	        speed.y *= -1;
	        //System.out.println("pos.y " + pos.y);

	    }
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
	
	boolean approach(PVector targ) {
		boolean reach = false;

		// calculate the path to target point
		PVector path = PVector.sub(targ, pos);

		// returns the direction as angle
		float angle = path.heading();

		// make a speed that points toward the target and then move
		speed = PVector.fromAngle(angle);
		//speed.mult(2);

		// check if bug reaches target
		if (path.mag() - (scale * dia) / 2 <= 0 ) {
			reach = true;
			//speed.mult(0.5f);
		}

		return reach;
	}
	
	
	
}

