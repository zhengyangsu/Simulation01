import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import processing.core.PVector;

public class Room {

	private int hight;
	private int width;
	private int margin;
	private float stroke;
	private Color color;
	private PVector pos;
	
	public Room(Dimension dim) {
		margin = RobotPane.margin;
		stroke = RobotPane.stroke;
		this.hight = dim.height;
		this.width = dim.width;
		color = RobotPane.green;
	}
	
	public Room(int hight, int width, int margin){
		this.hight = hight;
		this.width = width;
		this.margin = margin;

	}
	
	public void drawRoom(Graphics2D g, Dimension dim) {
		this.width = dim.width;
	    this.hight = dim.height;
		pos = new PVector(width,  hight);
		
		AffineTransform af = g.getTransform();
		g.setColor(color);
		g.setStroke(new BasicStroke(RobotPane.stroke));
		g.drawRect(margin, margin, width - 2*margin, hight - 2*margin);
		//System.out.println("Room size: " + width + " x " + hight);
		g.setTransform(af);

		drawSofa(g, pos);
		
		int cols = 3;
	    int rows = 2;

	    int chairW = 40;
	    int gapX = 10;      // space between chairs horizontally
	    int gapY = 100;      // space between rows vertically

	    float spacingX = chairW + gapX;
	    float spacingY = chairW + gapY;

	    // starting point top-left chair 
	    float startX = width / 2f - spacingX;   // centers 3 chairs
	    float startY = hight / 2f - spacingY/2; // centers 2 rows

	    for (int r = 0; r < rows; r++) {
	        for (int c = 0; c < cols; c++) {
	            float x = startX + c * spacingX;
	            float y = startY + r * spacingY;
	            
	            double rot = 0;
	            if (r ==1) rot = Math.PI;
	            drawChair(g, new PVector(x, y), rot);
	        }
	    }
		
		//drawChair(g, pos);

	}
	
	private void drawChair(Graphics2D g, PVector p, double rot) {
		int w = 40;
		int iW = 30;
		
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(stroke));
		
		AffineTransform af = g.getTransform();
		g.translate(p.x, p.y);
		g.rotate(rot);
		g.fillRoundRect(-w/2, -w/2, w, w, w/5, w/5);
		g.setColor(color);
		g.drawRoundRect(-w/2, -w/2, w, w, w/5, w/5);
		g.drawArc(-iW/2, -iW/2, iW, iW, 0, 180);
		g.drawLine(-w/2, 0, -iW/2, 0);
		g.drawLine(w/2, 0, iW/2, 0);
		g.setTransform(af);
		
	}
	
	private void drawSofa(Graphics2D g, PVector p) {
		

		int w = 230, h = 80;
		AffineTransform af = g.getTransform();

		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(stroke));
		g.translate(pos.x/2, pos.y/2);
		g.fillRoundRect(-w/2, -h/2, w, h, h, h);
		g.setColor(color);
		g.drawRoundRect(-w/2, -h/2, w, h, h, h);
		g.setTransform(af);

	}
	
	
}
