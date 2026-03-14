import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import processing.core.PVector;

public class Hunter extends Machine{
	
	private int health;
	private Path2D body;
	private Shape turret, scanner, cannon;
	private double spinAngle, mid, point, cannonLength, cannonWidth, r;
	private ArrayList<Missile> missiles;
	private ArrayList<HunterBot> targets;
	private HunterBot target;
	private Missile m;
	
	public Hunter(Dimension dim, int id) {
		super(dim, id);
		this.theta = 90;
		this.scale = 1;
		health = 100;
		point = 45;
		mid = 30;
		pos = new PVector(RobotPane.margin, RobotPane.margin);
		color = Color.red;
		spinAngle = 3 * Math.PI/2;
		speed = new PVector(0,4);
		cannonLength = 40;
		cannonWidth = 8;
		r = 10;//turret radius
		
		targets = new ArrayList<>();
		m = new Missile(pos.x - 45, pos.y, dim, id);
		setShape();
		
		
		
	}

	
	public void setShape() {
	    robotArea = new Area();

	    //body
	    body = new Path2D.Float();
	    body.moveTo(-point/2, -mid);
	    body.lineTo(point/2, 0);
	    body.lineTo(-point/2, mid);
	    body.closePath();

	    //turret
	    turret = new Ellipse2D.Double(-r, -r, 2*r, 2*r);

	    //cannon
	    cannon = new Rectangle2D.Double(0, -cannonWidth/2, cannonLength, cannonWidth);

	    //scanner
	    scanner = new Arc2D.Double(-r, -r, 2*r, 2*r, 0, 180, Arc2D.PIE);

	    robotArea.add(new Area(body));
	}

	
	@Override
	public void draw(Graphics2D g) {
		AffineTransform af = g.getTransform();
		g.translate(pos.x + 45/2, pos.y); // move robot origin to pos

		//draw body
		g.setColor(Color.BLACK);
		g.fill(body);
		g.setColor(color);
		g.draw(body);

		//turret & Cannon 
	    //pivot at body center
	    //Save transform
	    AffineTransform afTurret = g.getTransform();
	    double turretOffsetX = -point/6;
	    double turretOffsetY = 0;
	    g.translate(turretOffsetX, turretOffsetY);

	    //rotate turret toward target
	    if (target != null) {
	        PVector dir = PVector.sub(target.getPos(), pos); // direction from body center to target
	        g.rotate(dir.heading());
	    }

		//draw cannon and turret
		g.setColor(Color.BLACK);
		g.fill(cannon);
		g.setColor(color);
		g.draw(cannon);

		g.setColor(Color.BLACK);
		g.fill(turret);
		g.setColor(color);
		g.draw(turret);

		g.setTransform(afTurret);

		//draw scanner
		AffineTransform old = g.getTransform();
		g.translate(turretOffsetX, turretOffsetY);

		g.rotate(spinAngle);
		g.setColor(Color.ORANGE);
		g.fill(scanner);
		g.setTransform(old);
		g.setTransform(af);

		spinAngle += 2 * Math.PI / 33;
		spinAngle %= 2 * Math.PI;
		
		m.draw(g);
		g.draw(getBoundary().getBounds2D());
		
	}
	
	public void autoMove() {
		pos.add(speed);
	}
	
	public void fire() {
		//System.out.println("fired");
    	m.autoTarget(target);
    }
	
	@Override
	public Shape getBoundary() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x + 45/2, pos.y);
		return at.createTransformedShape(robotArea);
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
		m.autoTarget(target);
		m.move(panelSize, f);
	}
	
	public void targetAquire(ArrayList<Machine> targets) {
		
		for (Machine m : targets) {
			if (m instanceof HunterBot) {
				this.targets.add((HunterBot) m);
			}
		}
		
		double minDist = Double.MAX_VALUE;
		for (HunterBot h : this.targets) {
			double tempDist = PVector.dist(h.getPos(), pos);
			if (tempDist < minDist) {
				minDist = tempDist;
				target = h;
			}
		}
		
	}
	

}
