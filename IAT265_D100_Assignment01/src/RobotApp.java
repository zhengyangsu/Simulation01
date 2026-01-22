
import javax.swing.JFrame;


@SuppressWarnings("serial")
public class RobotApp extends JFrame{
	
	
	public RobotApp(String title) {
		super(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		RobotPane pacmanPane = new RobotPane();
		this.add(pacmanPane);
		this.pack(); 	// window's size is rather determined by 
						// packing to BallPanel's size
		this.setLocationRelativeTo(null); // center JFrame window
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new RobotApp("RobotApp");
	}

}

