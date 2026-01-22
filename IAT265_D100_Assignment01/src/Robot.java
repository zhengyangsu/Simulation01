

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import processing.core.PVector;


public class Robot {
	
	//properties fields
	
	
    private PVector pos, speed;
    private int size;
	private int disp;
	private Color color;
	private Random dice = new Random();
	private double scale;
	private double left;
    private double right;
    private double top;
    private double bottom;
	
	//constructor
	public Robot() {
		size = 50;
		disp = 20;
		scale = dice.nextInt(1, 2);
		int limit = (int) (size * scale);
		this.pos = new PVector(dice.nextInt(limit/2, RobotPane.width/2), dice.nextInt(limit/2, RobotPane.hight/2));
		this.speed = new PVector(dice.nextInt(1, disp), dice.nextInt(1, disp));

		left   = pos.x - scale*(size/2);
	    right  = (int)pos.x + scale*(size/2);
	    top    = (int)pos.y - scale*(size/2);
	    bottom = (int)pos.y + scale*(size/2);
		
		color = Color.GRAY;
	}
	
	//overloading
	public Robot(int x, int y, int size, Color c, int xSpeed, int ySpeed) {
		this.pos = new PVector(x, y);
		this.speed = new PVector(xSpeed, ySpeed);
		this.size = size;
		color = c;
		
	}

	
	public void drawPacman(Graphics2D g) {
		 g.setColor(color);

		 AffineTransform af = g.getTransform();
		 g.translate((int)pos.x, (int)pos.y); //new
		 g.rotate(speed.heading());
		 g.scale(scale, scale);
		 if (speed.x < 0) g.scale(1, -1);
	     
	     g.fillArc(-size/2, -size/2, size, size, 30, 300); 
	        
	     //the eye
	     g.setColor(Color.black);
	     g.fillOval(0, -size/4, size/10, size/10);
	     
	     
	     g.setTransform(af);
	}
	

	
	public void move() {
		pos.add(speed);
		//System.out.println(pos);
	}
	
	public void collisionValidate(Dimension panelSize) {
		Color nColor = new Color(128 + dice.nextInt(128), 128 + dice.nextInt(128), 128 + dice.nextInt(128));
		
		double r = scale * (size / 2.0);
	    left   = pos.x - r;
	    right  = pos.x + r;
	    top    = pos.y - r;
	    bottom = pos.y + r;
		
	    if (left <= 0 || right >= panelSize.width) {
	        speed.x *= -1;
	        color = nColor;
	        //System.out.println("x reverse");
	        
	    }

	    if (top <= 0 || bottom >= panelSize.height) {
	        speed.y *= -1;
	        color = nColor;
	        //System.out.println("y reverse");
	        
	    }
	}
	
	public Rectangle2D getBounds() {
		double r = scale * (size / 2.0);
		double x = pos.x - r;
		double y = pos.y - r;
		return new Rectangle2D.Double(x, y,r*2, r*2);
	}
	
	public double getRadius() {
		return scale * (size / 2.0);
	}
	
	public void enlarge() {
		
		System.out.println(scale);
		if (size*scale <= RobotPane.width/2) {
			scale += 0.1;
			//size = (int) (size * scale);
		}
		
	}
	
	public void reset() {
		scale = 1;
		size = 50;
		pos.set(RobotPane.width/2, RobotPane.hight/2);
		
	}
}

