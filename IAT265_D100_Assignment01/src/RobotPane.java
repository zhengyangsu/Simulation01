

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

import processing.core.PVector;


@SuppressWarnings("serial")
public class RobotPane extends JPanel implements ActionListener{
	
	public final static int paneWidth = 800;
	public final static int paneHight = 600;
	public final static int margin = 40;
	


	
	public final static Color green = new Color(0, 255, 65);
	public final static Color amber = new Color(255, 140, 0);
	public static float stroke = 2;
	
	private int width = paneWidth - 2* margin - (int)stroke;
	private int hight = paneHight - 2* margin - (int)stroke;
	private static int count = 0;
	
	
	private int robotCount;
	private int pileCount;
	private ArrayList<Robot> robots;
	private ArrayList<DustPile> piles;
	private Room room;
	private int fps = 24;
	private Timer t;
	//private int pileTimer; // custom timer used to generate a seed after 5 seconds

	
	public RobotPane() {
		super();
		this.setPreferredSize(new Dimension(width, hight));
		this.setBackground(Color.BLACK);
		this.addMouseListener(new MyMouseAdapter());
		
		pileCount = 0;
		robotCount = 0;
		
				
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		// Enable anti-aliasing
	    g2.setRenderingHint(
	        RenderingHints.KEY_ANTIALIASING,
	        RenderingHints.VALUE_ANTIALIAS_ON
	    );
	    
	    if (piles != null) for (DustPile dust : piles) {
		    dust.drawDustPile(g2);
		    System.out.println("dust drawn");
	    }
	    
	    if (robots != null) {
	    	for (Robot robot : robots) { 
	    		robot.drawRobot(g2);
	    	}
	    }
	    
	    if (room != null) room.drawRoom(g2, getSize());
	    drawCounter(g2);
	    
	    
	    
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		if (robots != null) {
			for (Robot robot : robots) {
				robot.move(getSize());
				//System.out.println("robot moved");
			}
			
		}
		//Rectangle2D rBound = robot.getBounds();
        //robot.collisionValidate(getSize());
		
        //System.out.println(pileTimer);
        
		/*if (pileTimer < fps*5) // increase only when it's less than 5 seconds. given 24 FPS
			pileTimer++;
		else {
			pileTimer = 0;
			if (piles == null) piles = new DustPile(getSize()); // produce a pile every 5 seconds
		}*/
		
		if (!piles.isEmpty()) {
			DustPile targ = targetAquisition();
			for (Robot robot : robots) {
				if (robot.approach(targ.getPos())) {
					piles.remove(targ);
					count++;
				}
		
				
			}
		}
		

        

		repaint();
	}
	
	
	
	private class MyMouseAdapter extends MouseAdapter {
		
	    public void mouseClicked(MouseEvent e) {
	    	
	        //System.out.println(e.toString());
	    	if (e.getClickCount() == 2) {
	    		PVector pos = new PVector(e.getX(), e.getY());
		        piles.add(new DustPile(pos));
	    	}
	        for (DustPile pile: piles) {
		    	if (e.isControlDown() && pile.checkMouseHit(e)) {
		    		System.out.println("enlarged");
		    		pile.enlarge();
	        }
	    		
	    	}
	        //System.out.println("Pile added in " + pos);
	        repaint();//have to repaint to show new pile modification
	
	    }
	}
	
	
	//class methods
	public void simulationBegin() {
		
		
		piles = new ArrayList<DustPile>();
		robots = new ArrayList<Robot>();
		robots.add(new Robot(getSize()));
		room = new Room(getSize());
		//pileTimer = 0;
		t = new Timer(1000/fps, this);
		t.start();
		
	}
	
	
	//helper
	private void drawCounter(Graphics2D g2) {
		String text = "" + count;

	    g2.setColor(green);
	    //g2.setFont(g2.getFont().deriveFont(48f));
	    g2.setFont(new Font("Monospaced", Font.PLAIN, 32));
	    FontMetrics fm = g2.getFontMetrics();

	    int textW = fm.stringWidth(text);
	    int ascent = fm.getAscent();
	    int descent = fm.getDescent();

	    int cx = getWidth() / 2;
	    int cy = getHeight() / 2;

	    int x = cx - textW / 2;
	    int y = cy + (ascent - descent) / 2;//center text vertically

	    g2.drawString(text, x, y);
	}
	
	private DustPile targetAquisition() {
		//find the closest dust pile
		DustPile target = null;
		double minDist = Double.MAX_VALUE;
		for (DustPile pile : piles) {
			double dist = PVector.dist(robots.get(0).getPos(), pile.getPos());
			if (dist < minDist) {
				minDist = dist;
				target = pile;
			}
		}
		return target;
	}
	
}
