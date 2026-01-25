

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;


@SuppressWarnings("serial")
public class RobotPane extends JPanel implements ActionListener{
	
	public final static int paneWidth = 800;
	public final static int paneHight = 600;
	public final static int margin = 40;
	


	
	public final static Color green = new Color(0, 255, 65);
	public final static Color amber = new Color(255, 140, 0);
	public final static float stroke = 2;
	
	public final static int width = paneWidth - 2* margin - (int)stroke;
	public final static int hight = paneHight - 2* margin - (int)stroke;
	
	

	private Robot robot;
	private DustPile pile;
	private Room room;
	private Timer t;
	private int pileTimer; // custom timer used to generate a seed after 5 seconds

	
	public RobotPane() {
		super();
		this.setPreferredSize(new Dimension(width, hight));
		
		this.setBackground(Color.BLACK);
		/*robot = new Robot();
		pile = new DustPile(width, hight);
		room = new Room(width, hight);
		pileTimer = 0;
		t = new Timer(33, this);
		t.start();*/
		
		//printStat();
		
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
	    
	    if (pile != null) pile.drawDustPile(g2, getSize());
	    if (robot != null) robot.drawRobot(g2);
	    if (room != null) room.drawRoom(g2, getSize());

	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		if (robot != null) robot.move(getSize());
		//Rectangle2D rBound = robot.getBounds();
        //robot.collisionValidate(getSize());
		
        //System.out.println(pileTimer);
        
		if (pileTimer < 300) // increase only when it's less than 10 seconds
			pileTimer++;
		else {
			pileTimer = 0;
			if (pile == null) pile = new DustPile(getSize()); // produce a seed when it's 10 seconds since program launches
		}

		if (pile != null && robot.approach(pile.getPos())) { // Only when seed is NOT NULL, it makes sense to approach it
			pile = null; // If bug catches seed eat it by setting it to null so that it will be garbage
							// collected by system
		}
        

		repaint();
	}
	
	public void simulationBegin() {
		robot = new Robot(getSize());
		pile = new DustPile(getSize());
		room = new Room(getSize());
		pileTimer = 0;
		t = new Timer(33, this);
		t.start();
	}
	
	//helper
	private void printStat() {
		System.out.println("panelSize " + getSize());
		

	}
}
