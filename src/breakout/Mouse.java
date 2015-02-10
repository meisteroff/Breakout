package breakout;



import com.badlogic.gdx.Gdx;

public class Mouse {
	
	public static int x() {
		return Gdx.input.getX();
	}
	
	public static int y() {
		return 360 - Gdx.input.getY();
	}

}
