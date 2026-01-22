

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
	public final static int margin = 20;
	public final static int width = paneWidth - 2* margin - 6;
	public final static int hight = paneHight - 2* margin - 6;
	public final static int lB = margin;
	public final static int rB = margin + width;
	public final static int tB = margin;
	public final static int bB = margin + hight;

	
	public final static Color green = new Color(0, 255, 65);
	public final static Color amber = new Color(255, 140, 0);
	public final static float stroke = 6;

	private Robot robot = new Robot();
	private DustPile pile = new DustPile();
	private Room room = new Room();
	private Timer t;
	
	public RobotPane() {
		super();
		this.setPreferredSize(new Dimension(width, hight));
		
		this.setBackground(Color.BLACK);
		t = new Timer(33, this);
		t.start();
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
	    
	    room.drawRoom(g2);
	    //robot.drawPacman(g2);
	    
	    pile.drawDustPile(g2);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
		robot.move();
		Rectangle2D rBound = robot.getBounds();
        robot.collisionValidate(getSize());
		

		repaint();
	}

	//helper
	
}
