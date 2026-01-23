import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Random;

import processing.core.PVector;

public class DustPile {
	
	private PVector pos;
    private int r;
    private double scale;
    private int left;
    private int right;
    private int top;
    private int bottom;
	private Color color;
	private Random dice = new Random();
	Blob[] blobs;
	
    
    public DustPile() {
    	color = RobotPane.green;
    	r = 10;
		pos = new PVector(dice.nextInt(RobotPane.lB, RobotPane.rB),
				dice.nextInt(RobotPane.tB, RobotPane.bB));
		System.out.println(pos);
		generateCluster();
    }
    
    //Overload
    public DustPile(PVector pos, int r, double scale, Color c) {
    	this.r = r;
    	this.scale = scale;
    	this.color = c;
		this.pos = pos;
    }
    
    private static class Blob {
        float ox, oy;   // offset from pile center
        float w, h;     // oval width/height
        Blob(float ox, float oy, float w, float h) {
            this.ox = ox; this.oy = oy; this.w = w; this.h = h;
        }
    }
    
    private void generateCluster() {
    	
        int n = dice.nextInt(7, 13);
        blobs = new Blob[n];

        // Spread controls radius the pile is around center
        float spread = r * 2.2f;

        // First blob
        blobs[0] = new Blob(0f, 0f, r * 2.1f, r * 1.8f);

        // Build the rest around the core
        for (int i = 1; i < n; i++) {
            // random polar offset (clustered near center)
            float angle = (float) (dice.nextDouble() * Math.PI * 2);//angle
            float radius = (float) (Math.pow(dice.nextDouble(), 1.7) * spread); // distance from center
            float ox = (float) (Math.cos(angle) * radius); //Cartesian x=r* cosθ
            float oy = (float) (Math.sin(angle) * radius); //Cartesian y=r* sinθ

            // sizes vary a lot 
            float w = (float) ((0.4 + dice.nextDouble() * 1.8) * r);
            float h = (float) ((0.6 + dice.nextDouble() * 1.9) * r);

            blobs[i] = new Blob(ox, oy, w, h);
        
        }
    }
    
    public void drawDustPile(Graphics2D g) {
		 AffineTransform old = g.getTransform();
		 g.translate(pos.x, pos.y);
		 for (Blob b : blobs) {
	            int x = Math.round(b.ox - b.w / 2f);
	            int y = Math.round(b.oy - b.h / 2f);
	            int w = Math.round(b.w);
	            int h = Math.round(b.h);
	            g.fillOval(x, y, w, h);
	        }
	     g.setTransform(old);
	     

    }
    
    public void reset() {
		scale = 1;
		r = 50;
		pos.set(RobotPane.width/2, RobotPane.hight/2);
		
	}
}
