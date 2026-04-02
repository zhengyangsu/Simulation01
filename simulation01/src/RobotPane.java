/*
 * Turn off Debug mode to hide additional information
 * */


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;
import processing.core.PVector;


@SuppressWarnings("serial")
public class RobotPane extends JPanel implements ActionListener{
	
	//region
	public final static int paneWidth = 1200, paneHight = 800, margin = 50;
	public final static Color green = new Color(0, 255, 65), amber = new Color(255, 140, 0);
	public static float stroke = 2;
	private int width = paneWidth - 2* margin - (int)stroke, hight = paneHight - 2* margin - (int)stroke, fps = 24, timerHunter;
	private static int count = 0;
	private int machineCount, robotCount, pileCount;
	private ArrayList<Machine> machines;
	private ArrayList<DustPile> piles;
	private Hunter hunter;
	private Room room;
	private Timer t;
	private Shape infoButton;
	private boolean showInfo, space, hunterActive;
	//endregion
	
	//private int pileTimer; // custom timer used to generate a seed after 5 seconds

	
	public RobotPane() {
		super();
		this.setPreferredSize(new Dimension(width, hight));
		this.setBackground(Color.BLACK);
		this.addMouseListener(new MyMouseAdapter());
		this.addKeyListener(new MyKeyListener());
		setFocusable(true);
		
		machineCount = 18;
		robotCount = 12;
		pileCount = machineCount*2;
		hunterActive = false;
		timerHunter = 0;
				
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
	       
	    infoButton = new Ellipse2D.Double(margin/4, margin/4, margin/2, margin/2);
	    g2.setStroke(new BasicStroke(RobotPane.stroke * 1.5f));
	    
	    if(showInfo) g2.setColor(green);
	    else g2.setColor(Color.BLACK);
	    g2.fill(infoButton);
	    g2.setColor(green);
	    g2.draw(infoButton);
	    g.setColor(Color.WHITE);
	    g.setFont(new Font("Courier Prime", Font.BOLD, 16));
	    String txtShowInfo = "Debug " + showInfo;
	    g.drawString(txtShowInfo, margin/4 + margin, margin/4 + 20);
	    
	    if (room != null) room.drawRoom(g2, getSize());
	    
	    if (piles != null) for (DustPile dust : piles) {
		    dust.drawDustPile(g2);
		    //System.out.println("dust drawn");
	    }
	    
	    if (machines != null) {
	    	for (Machine robot : machines) { 
	    		robot.draw(g2);
	    	}
	    }
	    
	    if (hunterActive) hunter.draw(g2);
	    
	    //drawCounter(g2);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		hunterActication();
		
		//target acquisition
	    if (piles != null) {
	        targetAquisition();
	    }

	    //hunter action
	    if (hunterActive) {
		 	hunter.move(getSize(), null);
		    hunter.collisionValidate(getSize());
		    if(space ==true) hunter.fire();
	    }
	 	
	    //compute forces and move robots
	    for (Machine m : machines) {
	    	
	    	//hunterBot and Hunter collision check
	    	if (m instanceof HunterBot && ((HunterBot) m).hunterCollisionCheck(hunter)) {
	    		hunter.collisionDamage();
	    	}
	    	
	        PVector totalForce = room.wallPushForce(m).div((float) m.getScale());
	        if (m instanceof Robot) totalForce.add(hunter.hunterPushForce(m));//robot avoid hunter
	        
	        //adds room and others forces Robot and HunterBot if alive
	        for (Machine o : machines) {
	            if (o != m && !o.isDead()) {
	                totalForce.add(m.seen(o));
	            }
	        }
	 
	        m.move(getSize(), totalForce); //if not dead
	        
	    }
	    
	    //resolve hunterBot after destroyed
	    ArrayList<HunterBot> toRemoveHunterBot = new ArrayList<>();
	    for (Machine m : machines) {
	    	if (m instanceof HunterBot && m.isGone()) toRemoveHunterBot.add((HunterBot)m);
	    }
	    
	    //resolve robot hunting after movement
	    ArrayList<Robot> toRemoveRobot = new ArrayList<>();
	    for (Machine m : machines) {
	        if (!m.isDead() && m.approach() && m instanceof HunterBot) {
	        	HunterBot hunter = (HunterBot) m;
	            Robot target = (Robot) hunter.getTarget();
	            if (target != null) {
	                toRemoveRobot.add(target);
	            }
	        }else if (m instanceof Robot && m.isDead()) toRemoveRobot.add((Robot)m); 
	    }
	    
	    //resolve dust collection after movement
	    ArrayList<DustPile> toRemoveDust = new ArrayList<>();
	    for (Machine m : machines) {
	        if (m instanceof Robot && !toRemoveRobot.contains(m)) {
	        	Robot robot = (Robot) m;
	        	
	        	if (m.approach()) {
	        		DustPile target = (DustPile) robot.getTarget();
		            if (target != null) {
		                toRemoveDust.add(target);
		                robot.setTarget(null);
		                count++;
		            }
	        	}
	            
	        }
	    }
	    
	    //remove destroyed HunterBot
	    machines.removeAll(toRemoveHunterBot);
	    
	    //remove hunted robots
	    if (!toRemoveRobot.isEmpty()) {
	        for (Machine m : machines) {
	            if (m instanceof HunterBot) {
	                HunterBot h = (HunterBot) m;
	                //if this hunter was chasing one of the robots we just deleted
	                if (toRemoveRobot.contains(h.getTarget())) {
	                    h.setTarget(null); 
	                }
	            }
	        }
	        
	        /*
	        //repenish new robots
	        for(int i = 0; i < toRemoveRobot.size(); i++) {
	        	machines.add(new Robot(getSize(), machines.size() + i));
	        }
	        */
	        machines.removeAll(toRemoveRobot);
	    }
	    
	    //remove collected dust
	    if (!toRemoveDust.isEmpty()) {
	        for (Machine m : machines) {
	        	if (m instanceof Robot) {
	            	Robot r = (Robot) m;
	                if (toRemoveDust.contains(r.getTarget())) {
	                    r.setTarget(null); 
	                }
	            }
	        }
	        
	        //generate new dust piles to replace the removed ones
	        for(int i = 0; i < toRemoveDust.size(); i++) {
	        	piles.add(new DustPile(getSize()));
	        }
	        piles.removeAll(toRemoveDust);
	    }
	    
	    repaint();
	}

	//class methods
	public void simulationBegin() {
		
		showInfo = true;
		
		piles = new ArrayList<DustPile>();
		hunter = new Hunter(getSize(), 1000);
		
		requestFocusInWindow();
		
		for (int i = 0; i < pileCount; i++) piles.add(new DustPile(getSize()));
		
		machines = new ArrayList<Machine>();
		for (int i = 0; i < machineCount; i++) {
			if (i < robotCount)machines.add(new Robot(getSize(), i));
			else machines.add(new HunterBot(getSize(), i));
		}
			
		room = new Room(getSize());
		
		displayRobotInfo();
		displayPileInfo();
		
		t = new Timer(1000/fps, this);
		t.start();
		
	}
	
	//pass the designated pile to the corresponding robot
	private void targetAquisition() {
		//find the closest dust pile
		
		dustTargetAquisition();
		robotTargetAquisition();
	}
	
	//helpers
	private void dustTargetAquisition() {
		for (Machine r: machines) {
			if (r instanceof Robot) {
				Robot robot = (Robot) r;
				if (robot.getTarget() != null) continue;//skip if already has target
				ArrayList <DustPile> targets = new ArrayList<DustPile>();
				DustPile mainTarget = null;
				DustPile secTarget = null;
				double minDist = Double.MAX_VALUE;
				
				for (DustPile p : piles) {
					
					if (p.getScale() <= r.getScale()) {
						
						double dist = PVector.dist(r.getPos(), p.getPos());
						
						if (dist < minDist) {
							minDist = dist;
							secTarget = mainTarget;
							mainTarget = p;						
						}
					//System.out.println("target: " + targets);
					}
				}
				
				if (mainTarget != null) {
				    targets.add(mainTarget);
				    if (secTarget != null) targets.add(secTarget);
				    robot.setTarget(targets);
				} 
				else {
				    robot.setTarget(null);
				}
			}
		}
	}
	
	private void robotTargetAquisition() {
		Robot mainTarget = null;

		for (Machine m: machines) {
			if (m instanceof HunterBot) {
				HunterBot hunter = (HunterBot) m;
				if(hunter.found(hunter.getTarget())) continue;//skip if already has target
				for (Machine o : machines) {
					if (o instanceof Robot && hunter.found((Robot) o)) {
						Robot robot = (Robot) o;
							mainTarget = robot;
							hunter.setTarget(mainTarget);
					}
				}
			}
		}
	}
	
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
	
	private void displayRobotInfo() {
		for (Machine r : machines) {
			r.displayInfo(showInfo);
		}
		
		hunter.displayInfo(showInfo);
	}
	
	private void displayPileInfo() {
		for (DustPile d : piles) {
			d.displayInfo(showInfo);
		}
	}
	
	private void reset() {
		
		machines.clear();
		machines = new ArrayList<Machine>();
		
		for (int i = 0; i < machineCount; i++) {
			if (i < robotCount)machines.add(new Robot(getSize(), i));
			else machines.add(new HunterBot(getSize(), i));
		}
		
		hunterActive = false;
		hunter = new Hunter(getSize(), 1000);
	}
	
	private void hunterActication() {
		
		hunter.targetAquire(machines);

		//resolve hunter
		if (hunter.getHealth() <= 0) {
		    hunterActive = false;
		    
		    //increment timer while hunter is "dead"
		    timerHunter++;

		    //respawn after 3 seconds (fps * 3)
		    if (timerHunter >= fps * 3) {
		        hunter.setHealth(100);
		        hunterActive = true;
		        timerHunter = 0;
		    }
		    
		    return;
		}
	    
		int currentPrey = 0;
		int currentPredator = 0;		
		
		for (Machine m : machines) {
			if (m instanceof Robot) currentPrey ++;
			if (m instanceof HunterBot) currentPredator ++;
		}
		
		if (currentPrey == 0 && currentPredator == 0) {
			reset();
			return;
		}
		
		if (currentPrey < Math.max(1, (float)currentPredator)/2) hunterActive = true;
		else if (currentPredator == 0) {
			hunterActive = false;
			timerHunter++;
			if (timerHunter >= fps * 3) {
				reset();
				timerHunter = 0;
			}
		}
		
		
		
	}
	
	private class MyMouseAdapter extends MouseAdapter {
		
	    public void mouseClicked(MouseEvent e) {
	    	
	        //System.out.println(e.toString());
	    	
	    	if (e.getClickCount() == 1 && infoButton.contains(getMousePosition())) {
	    		showInfo = !showInfo;
	    		displayRobotInfo();
	    		displayPileInfo();
	    	} 
	    	
	    	if (e.getClickCount() == 2 && room.getBound().contains(getMousePosition())) {
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
	
	public class MyKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				space = true;
				//System.out.println("pressed space");
			}
				
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				space = false;
				//System.out.println("released space");
			}
		}
	}
}
