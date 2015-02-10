package breakout;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Graphics {

	private final SpriteBatch batch;
	private final BitmapFont font;
	private final Sprite pixel;
	private float xoff;
	private float yoff;

	private Object lock;

	public Graphics(SpriteBatch batch, BitmapFont font) {
		this.batch = batch;
		this.font = font;

		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.drawPixel(0, 0, 0xFFFFFFFF);
		pixel = new Sprite(new Texture(pixmap));
	}

	public void clear(float r, float g, float b, float a) {
		Gdx.gl.glClearColor(r, g, b, a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	public void begin(Object lock) {
		if (lock == null) {
			throw new IllegalArgumentException("need a proper object as lock");
		}

		if (this.lock == null) {
			this.lock = lock;

			batch.begin();
		}
	}

	public void end(Object lock) {
		if (lock == null) {
			throw new IllegalArgumentException("need a proper object as lock");
		}

		if (this.lock == lock) {
			this.lock = null;

			batch.end();
		}
	}

	public void offset(float xoff, float yoff) {
		this.xoff = xoff;
		this.yoff = yoff;
	}

	public void draw(TextureRegion region, float x, float y) {
		batch.draw(region, x + xoff, y + yoff);
	}

	public void draw(TextureRegion region, float x, float y, float scale, float rotation) {
		batch.draw(region, x + xoff, y + yoff, region.getRegionWidth() / 2f, region.getRegionHeight() / 2f, region.getRegionWidth() * scale, region.getRegionHeight() * scale, 1, 1, rotation);
	}

	public void drawRotated(TextureRegion region, float x, float y, float rotation) {
		batch.draw(region, x + xoff, y + yoff, region.getRegionWidth() / 2f, region.getRegionHeight() / 2f, region.getRegionWidth(), region.getRegionHeight(), 1, 1, rotation);
	}

	public void drawScaled(TextureRegion region, float x, float y, float scale) {
		batch.draw(region, x + xoff, y + yoff, region.getRegionWidth() / 2f, region.getRegionHeight() / 2f, region.getRegionWidth(), region.getRegionHeight(), scale, scale, 0);
	}

	public void drawByCoordinate(TextureRegion region, int x, int y, int size) {
		draw(region, x * size, y * size);
	}

	public void draw(String text, float x, float y, Color color) {
		TextBounds bounds = font.getBounds(text);
		font.setColor(color);
		font.draw(batch, text, x + xoff, y + yoff + bounds.height);
	}

	public void drawCenterX(String text, float x, float y, Color color) {
		TextBounds bounds = font.getBounds(text);
		draw(text, x - bounds.width / 2f, y, color);
	}

	public void drawCenterXY(String text, float x, float y, Color color) {
		TextBounds bounds = font.getBounds(text);
		draw(text, x - bounds.width / 2f, y - bounds.height / 2f, color);
	}

	public void rectangle(float x, float y, float width, float height, Color color) {
		pixel.setSize(width, height);
		pixel.setColor(color);
		pixel.setPosition(x + xoff, y + yoff);
		pixel.draw(batch);
	}

	public void rectangleCenterX(float x, float y, float width, float height, Color color) {
		rectangle(x - width / 2f, y, width, height, color);
	}

	public void rectangleCenterXY(float x, float y, float width, float height, Color color) {
		rectangle(x - width / 2f, y - height / 2f, width, height, color);
	}

}
