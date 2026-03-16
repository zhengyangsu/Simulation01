import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
	
	private int health, missileCount;
	private Path2D body;
	private Shape turret, scanner, cannon;
	private double spinAngle, mid, point, cannonLength, cannonWidth, r;
	private ArrayList<Missile> missiles;
	private ArrayList<HunterBot> targets, lockedTargets;
	private HunterBot target;
	private float forceCoef;

	public Hunter(Dimension dim, int id) {
		
		super(dim, id);
		this.theta = 90;
		this.scale = 1;
		health = 100;
		missileCount = 6;
		point = 45;
		mid = 30;
		pos = new PVector(RobotPane.margin, RobotPane.margin);
		color = Color.red;
		spinAngle = 3 * Math.PI/2;
		vel = new PVector(0,4);
		cannonLength = 40;
		cannonWidth = 8;
		r = 10;//turret radius
		targets = new ArrayList<>();
		lockedTargets = new ArrayList<>();
		missiles = new ArrayList<>();
		forceCoef = 25.0f;
		setShape();
		loadMissiles();
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
		infoLines = new String[] {getClass().getName(), "Health: " + String.format("%d", health)};

		drawMissiles(g);
		
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
		
		//g.draw(getBoundary().getBounds2D());
		
		if (displayInfo) {
		    displayInfo(g);
		}
	}
	

	public void fire() {
		//System.out.println("fired");
		double minDist = Double.MAX_VALUE;
		
		for (HunterBot t : targets) {
			double tempDist = PVector.dist(t.getPos(), pos);
			if (tempDist < minDist) {
				minDist = tempDist;
				target = (HunterBot) t;
			}
		}
		
    	for (Missile m : missiles) {
    		
    		if (!lockedTargets.contains(target) && !m.getFired()) {
				m.setTarget(target);
				lockedTargets.add(target);
				//System.out.println("target set " + target);
    			m.setFired();
    			break;
    		}
    	}
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
	public void move(Dimension dim, PVector f) {
		// TODO Auto-generated method stub
		for (int i = missiles.size() - 1 ; i >= 0; i--) {
			Missile m = missiles.get(i);
			if (m.getFired() && m.approach()) {
				HunterBot hit = m.getTarget();
				targets.remove(hit);
				lockedTargets.remove(hit);
				missiles.remove(m);
				m = null;
				hit.kill();
			}
		}
		
		pos.add(vel);
		moveMissiles(dim, pos);
	}
	
	public void targetAquire(ArrayList<Machine> targets) {
		
		this.targets.clear();
		
		for (Machine m : targets) {
			if (m instanceof HunterBot && !m.isDead()) {
				this.targets.add((HunterBot) m);	
			}
		}
		//System.out.println("target aqquired " + targets.size());
	}
	
	private void loadMissiles() {
		
		for(int i = 0; i < missileCount; i++) {
			missiles.add(new Missile(pos.x , pos.y, dim, i));
		}
		
	}
	
	private void drawMissiles(Graphics2D g) {
		
		for(Missile m : missiles) {
			//m.drawWaves(g);
			m.draw(g);
		}
	}

	private void moveMissiles(Dimension dim, PVector pos) {
		for (Missile m : missiles) {
			
			m.move(dim, pos);
		}
	}
	
	public PVector hunterPushForce(Machine r) {
		
		PVector force = new PVector();
		float radius = (float)(r.getDia() * r.getScale() / 2f);
	    float x = r.getPos().x;
		int margin = RobotPane.margin;
	    float minDist = r.getDia() * 1.5f;      // how far robot senses hunter
	    float safe = 6f;         				// prevents infinite force


	    //left
	    float d = x - margin - radius;
	    if (d < minDist)
	        force.x -= forceCoef / Math.max(d, safe);

	    return force;
	}
	
	public void collisionDamage() {
		health -= 10;
	}
	
	public int getHealth() {
		return health;
	}

	public void setHealth(int h) {
		health = h;
	}
}
