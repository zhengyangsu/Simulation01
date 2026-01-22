import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Room {

	private int hight;
	private int width;
	private int margin;
	private float stroke;
	private Color color;
	
	public Room() {
		margin = RobotPane.margin;
		stroke = RobotPane.stroke;
		hight = RobotPane.hight;
		width = RobotPane.width;
		color = RobotPane.green;
	}
	
	public Room(int hight, int width, int margin){
		this.hight = hight;
		this.width = width;
		this.margin = margin;
	}
	
	public void drawRoom(Graphics2D g) {
		g.setColor(color);
		g.setStroke(new BasicStroke(RobotPane.stroke));
		g.drawRect(margin, margin, width - 2*margin, hight - 2*margin);
		
	}
}
