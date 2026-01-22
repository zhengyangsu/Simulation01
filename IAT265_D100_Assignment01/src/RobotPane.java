

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
	
	public final static int width = 800;
	public final static int hight = 600;
	private Robot robot = new Robot();
	private int spawn = 2;
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
	    
	    robot.drawPacman(g2);
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
