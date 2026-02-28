import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;

import processing.core.PVector;

public class Room {

	private int height;
	private int width;
	private int margin;
	private float stroke;
	private Color color;
	private PVector pos;
	
	/*
	private Rectangle2D.Double rightWall;
	private Rectangle2D.Double leftWall;
	private Rectangle2D.Double topWall;
	private Rectangle2D.Double bottomWall;
	*/
	private Rectangle2D room;
	private Line2D.Double rightEdge;
	private Line2D.Double leftEdge;
	private Line2D.Double topEdge;
	private Line2D.Double bottomEdge;
	private float wallCoef;
	
	
	public Room(Dimension dim) {
		margin = RobotPane.margin;
		stroke = RobotPane.stroke;
		this.height = dim.height;
		this.width = dim.width;
		color = RobotPane.green;
		
		
		wallCoef = 25.0f;
		
	}
	
	//overloading
	public Room(int hight, int width, int margin){
		this.height = hight;
		this.width = width;
		this.margin = margin;

	}
	
	
	public void drawRoom(Graphics2D g, Dimension dim) {
		this.width = dim.width;
	    this.height = dim.height;
		pos = new PVector(width,  height);
		
		AffineTransform af = g.getTransform();
		g.setStroke(new BasicStroke(RobotPane.stroke));
		
		room = new Rectangle2D.Double(
				margin, 
				margin, 
				width - 2*margin, 
				height - 2*margin);
		
		rightEdge = new Line2D.Double(width-margin, margin, width-margin, height - margin);
		leftEdge = new Line2D.Double(margin, margin, margin, height - margin);
		topEdge = new Line2D.Double(margin, margin, width - margin, margin);
		bottomEdge = new Line2D.Double(margin, height - margin, width - margin, height - margin);
		
		//g.setColor(Color.BLACK);
		//g.fill(room);
		g.setColor(color);
		g.draw(room);
		g.draw(leftEdge);
		g.draw(rightEdge);
		g.draw(topEdge);
		g.draw(bottomEdge);
		
		//System.out.println("Room size: " + width + " x " + hight);
		g.setTransform(af);

		drawTable(g, pos);
		
		int cols = 3;
	    int rows = 2;

	    int chairW = 40;
	    int gapX = 10;      // space between chairs horizontally
	    int gapY = 100;      // space between rows vertically

	    float spacingX = chairW + gapX;
	    float spacingY = chairW + gapY;

	    // starting point top-left chair 
	    float startX = width / 2f - spacingX;   // centers 3 chairs
	    float startY = height / 2f - spacingY/2; // centers 2 rows

	    
	    for (int r = 0; r < rows; r++) {
	        for (int c = 0; c < cols; c++) {
	            float x = startX + c * spacingX;
	            float y = startY + r * spacingY;
	            
	            double rot = 0;
	            if (r ==1) rot = Math.PI;
	            drawChair(g, new PVector(x, y), rot);
	        }
	    }
	    
		

	}
	
	public Shape getBound() {
		return room.getBounds();
	}
	
	private void drawChair(Graphics2D g, PVector p, double rot) {
		
		int w = 40;
	    int iW = 30;

	    AffineTransform old = g.getTransform();

	   
	    g.translate(p.x, p.y);
	    g.rotate(rot);

	    g.setStroke(new BasicStroke(stroke));

	    //round corner square
	    RoundRectangle2D seat = new RoundRectangle2D.Double(
	            -w/2.0, -w/2.0, w, w,
	            w/5.0, w/5.0
	    );

	    g.setColor(Color.BLACK);
	    g.fill(seat);

	    g.setColor(color);
	    g.draw(seat);

	    //back arc
	    Shape back = new Arc2D.Double(
	            -iW/2.0, -iW/2.0, iW, iW,
	            0, 180, Arc2D.OPEN
	    );

	    g.draw(back);

	    //two lines
	    Shape left = new Line2D.Double(-w/2.0, 0, -iW/2.0, 0);
	    Shape right = new Line2D.Double(w/2.0, 0, iW/2.0, 0);

	    g.draw(left);
	    g.draw(right);

	    // Restore transform
	    g.setTransform(old);
		
	}
	
	private void drawTable(Graphics2D g, PVector p) {

		int w = 230;
	    int h = 80;

	    AffineTransform old = g.getTransform();

	    g.setColor(Color.BLACK);
	    g.setStroke(new BasicStroke(stroke));

	  
	    g.translate(p.x / 2, p.y / 2);

	    Shape sofa = new RoundRectangle2D.Double(
	            -w / 2.0, -h / 2.0,
	            w, h,
	            h, h
	    );

	    g.fill(sofa);

	    g.setColor(color);
	    g.draw(sofa);

	    g.setTransform(old);
		
	}
	
	public PVector wallPushForce(Machine r) {
		
		
		PVector force = new PVector();
		float radius = (float)(r.getDia() * r.getScale() / 2f);
	    float x = r.getPos().x;
	    float y = r.getPos().y;
		
	    float minDist = r.getDia() * 1.5f;      // how far robot senses wall
	    float safe = 6f;         // prevents infinite force


	    //force = wallCoef / d
	    // RIGHT
	    float d = (width - margin) - x - radius;
	    if (d < minDist)
	        force.x -= wallCoef / Math.max(d, safe);

	    // LEFT
	    d = x - margin - radius;
	    if (d < minDist)
	        force.x += wallCoef / Math.max(d, safe);

	    // TOP
	    d = y - margin - radius;
	    if (d < minDist)
	        force.y += wallCoef / Math.max(d, safe);

	    // BOTTOM
	    d = (height - margin) - y - radius;
	    if (d < minDist)
	        force.y -= wallCoef / Math.max(d, safe);
	    
	    /*
	     * Double distance = 0.0;
	    float d = (width - margin) - x - radius;
		distance = rightEdge.ptLineDist(r.getPos().x, r.getPos().y) - r.getDia() * r.getScale(); 
		force.add(new PVector((float)(-wallCoef / Math.pow(distance, 2)), 0.0f));
		
	    d = x - margin - radius;
		distance = leftEdge.ptLineDist(r.getPos().x, r.getPos().y) - r.getDia() * r.getScale(); 
		force.add(new PVector((float)(+wallCoef / Math.pow(distance, 2)), 0.0f));
		
		d = y - margin - radius;
		distance = topEdge.ptLineDist(r.getPos().x, r.getPos().y) - r.getDia() * r.getScale(); 
		force.add(new PVector((float)(+wallCoef / Math.pow(distance, 2)), 0.0f));
		
		d = y - margin - radius;
		distance = bottomEdge.ptLineDist(r.getPos().x, r.getPos().y) - r.getDia() * r.getScale(); 
		force.add(new PVector((float)(-wallCoef / Math.pow(distance, 2)), 0.0f));
		
	    
	    float minDist = r.getDia() * 1.5f;      // how far robot senses wall
	    float safe = 6f;         // prevents infinite force
	    float maxForce = 0.4f;   // caps acceleration

	    //force = wallCoef / d
	    // RIGHT
	    float d = (width - margin) - x - radius;
	    if (d < minDist)
	        force.x -= Math.min(maxForce, wallCoef / Math.max(d, safe));

	    // LEFT
	    d = x - margin - radius;
	    if (d < minDist)
	        force.x += Math.min(maxForce, wallCoef / Math.max(d, safe));

	    // TOP
	    d = y - margin - radius;
	    if (d < minDist)
	        force.y += Math.min(maxForce, wallCoef / Math.max(d, safe));

	    // BOTTOM
	    d = (height - margin) - y - radius;
	    if (d < minDist)
	        force.y -= Math.min(maxForce, wallCoef / Math.max(d, safe));
		*/
	    return force;
	}
}
