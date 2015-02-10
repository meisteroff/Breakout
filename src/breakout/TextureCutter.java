package breakout;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureCutter {

	public static TextureRegion byCoordinate(Texture texture, int x, int y, int size) {
		return new TextureRegion(texture, x * size, y * size, size, size);
	}

	public static TextureRegion byBounds(Texture texture, int x, int y, int width, int height) {
		return new TextureRegion(texture, x, y, width, height);
	}

}
