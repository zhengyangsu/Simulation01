import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

import processing.core.PVector;

public class Hunter extends Machine{
	
	private int health;
	private Path2D body;
	private Shape turret;
	private Shape cannon;
	private double mid;
	private double point;
	
	public Hunter(Dimension dim, int id) {
		super(dim, id);
		pos = new PVector(RobotPane.margin, RobotPane.margin);
		health = 100;
		point = 80;
		mid = 30;
		setShape();
	}

	
	public void setShape() {
		robotArea = new Area();
		
		//body shapes
		body = new Path2D.Float();
		body.moveTo(RobotPane.margin, RobotPane.margin);
		body.lineTo(point, RobotPane.margin + mid);
		body.lineTo(RobotPane.margin, RobotPane.margin + 2* mid);
		body.closePath();
		robotArea.add(new Area(outer));
	}
	
	@Override
	public void draw(Graphics2D g) {
		AffineTransform af = g.getTransform();
		g.setColor(Color.RED);		
		g.setStroke(new BasicStroke(RobotPane.stroke * 1.5f));
		
		//transform stack
		g.translate(0, pos.y - mid/2);
		g.draw(body);
		g.setTransform(af);
		
		
	}
	
	@Override
	public void move(PVector f) {
		speed.mult(0);
		speed.add(f);
		//System.out.println(speed);
		pos.add(speed);
	}
	
	@Override
	protected boolean targetCollisionCheck(Object target) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean approach() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void move(Dimension panelSize, PVector f) {
		// TODO Auto-generated method stub
		
	}

}
