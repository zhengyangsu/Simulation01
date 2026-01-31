

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;
import processing.core.PVector;

public class DustPile {
	
	private PVector pos;
    private int r;
    private double scale;
	private Color color;
	private Random dice = new Random();
	private double mScale = 1.5;
	private Blob[] blobs;
	private ArrayList<Shape> shapes; 
	
    
    public DustPile(Dimension dim) {
    	color = RobotPane.green;
    	r = 10;
    	scale = 1;
    	shapes = new ArrayList<Shape>();
		pos = new PVector(dice.nextInt((int)(RobotPane.margin*mScale), 
				dim.width - (int)(RobotPane.margin*mScale)),
				dice.nextInt((int)(RobotPane.margin*mScale), 
				dim.height - (int)(RobotPane.margin*mScale)));
		//System.out.println(pos);
		generateCluster();
    }
    
    public DustPile(PVector pos) {
    	color = RobotPane.green;
    	r = 10;
    	scale = 1;
    	shapes = new ArrayList<Shape>();
    	this.pos = pos;
    	generateCluster();
    }
    
    //Overload
    public DustPile(PVector pos, int r, double scale, Color c) {
    	this.r = r;
    	this.scale = scale;
    	this.color = c;
		this.pos = pos;
    	shapes = new ArrayList<Shape>();
		generateCluster();
    }
    
    private static class Blob {
        float ox, oy;   // offset from pile center
        float w, h;     // oval width/height
        double angle; // rotation angle
        Blob(float ox, float oy, float w, float h, double angle) {
            this.ox = ox; this.oy = oy; this.w = w; this.h = h; this.angle = angle;
        }
    }
    
    private void generateCluster() {
    	
        int n = dice.nextInt(7, 13);
        blobs = new Blob[n];
        
        // Spread controls radius the pile is around center
        float spread = r * 2.2f;

        // First blob
        blobs[0] = new Blob(0f, 0f, r * 2.1f, r * 1.8f, dice.nextInt());

        // Build the rest around the core
        for (int i = 1; i < n; i++) {
            // random polar offset 
            float angle = (float) (dice.nextDouble() * Math.PI * 2);//angle
            float radius = (float) (Math.pow(dice.nextDouble(), 1.7) * spread); // distance from center
            float ox = (float) (Math.cos(angle) * radius); //Cartesian x=r* cosθ
            float oy = (float) (Math.sin(angle) * radius); //Cartesian y=r* sinθ

            // sizes vary
            float w = (float) ((0.4 + dice.nextDouble() * 1.8) * r);
            float h = (float) ((0.6 + dice.nextDouble() * 1.9) * r);


            blobs[i] = new Blob(ox, oy, w, h, angle);
        
        }
    }
    
    public void drawDustPile(Graphics2D g) {

        AffineTransform old = g.getTransform();

        g.setColor(color);

        //transform 
        AffineTransform tf = new AffineTransform();
        
        tf.translate(pos.x, pos.y);
        tf.scale(scale, scale);

        //hit-testing, clear it each draw
        shapes.clear();

        g.transform(tf);

        for (Blob b : blobs) {
            double x = b.ox - b.w / 2.0;
            double y = b.oy - b.h / 2.0;
            double w = b.w;
            double h = b.h;
            double angle = b.angle;

            //AffineTransform perBlob = new AffineTransform(tf);
            // rotate around the blob center
            g.rotate(angle, b.ox, b.oy);
            //g.setTransform(perBlob);
            Ellipse2D local = new Ellipse2D.Double(x, y, w, h);
            g.fill(local);
            
            // translate local coord to world coord for hit-testing
            Shape world = tf.createTransformedShape(local);
            shapes.add(world);
            g.rotate(-b.angle, b.ox, b.oy); //undo rotation
        }

        g.setTransform(old);
    }

    
    public boolean checkMouseHit(MouseEvent e) {
    	//System.out.println("Mouse at " + e.getX() + ", " + e.getY());
    	//System.out.println(getBound().contains(e.getX(), e.getY()));
	    return  getBound().contains(e.getX(), e.getY());
	}
    
    private Shape getBound() {
    	Area area = new Area();
    	for (Shape s : shapes) area.add(new Area(s));
    	//System.out.println("Area " + area.getBounds2D());
    	return area.getBounds2D();

    }
    
    public PVector getPos() {
		return pos;
    	
    }
    
    public void enlarge() {
    	if (scale < 1.5) scale += 0.1;
    }
    
}
