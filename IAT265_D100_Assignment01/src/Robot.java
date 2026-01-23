

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import processing.core.PVector;


public class Robot {
	
	//properties fields
	
	
    private PVector pos, speed;
    private int dia;
	private int disp;
	private Color color;
	private Random dice = new Random();
	private double scale;
	private double left;
    private double right;
    private double top;
    private double bottom;
    private double theta;
	
	//constructor
	public Robot() {
		dia = 70;
		disp = 20;
		scale = dice.nextInt(1, 2);
		pos = new PVector(RobotPane.width/2,  RobotPane.hight/2);
		speed = new PVector(dice.nextInt(1, disp), dice.nextInt(1, disp));
		theta = Math.toRadians(-90);
		
		left   = pos.x - scale*(dia/2);
	    right  = (int)pos.x + scale*(dia/2);
	    top    = (int)pos.y - scale*(dia/2);
	    bottom = (int)pos.y + scale*(dia/2);
		
		color = RobotPane.green;
	}
	
	//overloading
	public Robot(int x, int y, int size, Color c, int xSpeed, int ySpeed) {
		this.pos = new PVector(x, y);
		this.speed = new PVector(xSpeed, ySpeed);
		this.dia = size;
		color = c;
		
	}

	
	public void drawRobot(Graphics2D g) {
		 
		AffineTransform af = g.getTransform();
		
		g.setStroke(new BasicStroke(RobotPane.stroke));
		g.translate((int)pos.x, (int)pos.y);
		g.rotate(theta);
		
		g.scale(scale, scale);
		if (speed.x < 0) g.scale(1, -1);
		 
		 
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

		//brushes
		g.drawLine(-27, -22, -32, -33);
		g.drawLine(-27, -22, -37, -31);
		g.drawLine(-27, -22, -38, -25);
		g.drawLine(27, -22, 32, -33);
		g.drawLine(27, -22, 37, -31);
		g.drawLine(27, -22, 38, -25);

		g.draw(path);
		g.setTransform(af);
		
		//g.rotate(speed.heading());
	}
	

	
	public void move() {
		//pos.add(speed);
		//System.out.println(pos);
	}
	
	public void collisionValidate(Dimension panelSize) {
		
		
		double nDia = scale * (dia / 2.0);
	    left   = pos.x - nDia;
	    right  = pos.x + nDia;
	    top    = pos.y - nDia;
	    bottom = pos.y + nDia;
		
	    if (left <= 0 || right >= panelSize.width) {
	        speed.x *= -1;
	        //System.out.println("x reverse");
	        
	    }

	    if (top <= 0 || bottom >= panelSize.height) {
	        speed.y *= -1;
	        //System.out.println("y reverse");
	        
	    }
	}
	
	public Rectangle2D getBounds() {
		double nDia = scale * (dia / 2.0);
		double x = pos.x - nDia;
		double y = pos.y - nDia;
		return new Rectangle2D.Double(x, y,nDia*2, nDia*2);
	}
	
	public double getRadius() {
		return scale * (dia / 2.0);
	}
	
	
	public void reset() {
		scale = 1;
		dia = 50;
		pos.set(RobotPane.width/2, RobotPane.hight/2);
		
	}
}

