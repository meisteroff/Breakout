package breakout;


import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Particle {

	public TextureRegion region;
	public float rotation;
	public float scale;

	public float x, y;
	public float velX, velY;
	public float accX, accY;
	public float angVel;
	
	public int age, lifetime;

}
