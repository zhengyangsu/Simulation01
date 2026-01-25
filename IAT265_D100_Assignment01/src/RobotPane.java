

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
	public static float stroke = 2;
	
	private int width = paneWidth - 2* margin - (int)stroke;
	private int hight = paneHight - 2* margin - (int)stroke;
	private static int count = 0;
	

	private Robot robot;
	private DustPile pile;
	private Room room;
	private int fps = 24;
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
	    drawCounter(g2);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		if (robot != null) robot.move(getSize());
		//Rectangle2D rBound = robot.getBounds();
        //robot.collisionValidate(getSize());
		
        //System.out.println(pileTimer);
        
		if (pileTimer < fps*5) // increase only when it's less than 5 seconds. given 60 FPS
			pileTimer++;
		else {
			pileTimer = 0;
			if (pile == null) pile = new DustPile(getSize()); // produce a seed when it's 10 seconds since program launches
		}

		if (pile != null && robot.approach(pile.getPos())) { // Only when seed is NOT NULL, it makes sense to approach it
			pile = null; // If bug catches seed eat it by setting it to null so that it will be garbage
			count++;				// collected by system
		}
        

		repaint();
	}
	
	public void simulationBegin() {
		robot = new Robot(getSize());
		pile = new DustPile(getSize());
		room = new Room(getSize());
		pileTimer = 0;
		t = new Timer(1000/fps, this);
		t.start();
	}
	
	//helper
	private void drawCounter(Graphics2D g2) {
		String text = "" + count;

	    g2.setColor(green);
	    //g2.setFont(g2.getFont().deriveFont(48f));
	    g2.setFont(new Font("Monospaced", Font.BOLD, 32));
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
}
