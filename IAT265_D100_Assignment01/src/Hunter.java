import java.awt.Dimension;

public class Hunter extends Machine{

	public Hunter(Dimension dim, int id) {
		super(dim, id);
		// TODO Auto-generated constructor stub
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

}
