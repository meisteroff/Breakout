package breakout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Game extends GameAdapter {

	SpriteBatch batch;
	BitmapFont font;
	Graphics g;
	Preferences savegame;
	int clock;
	int previousMouseX;

	final int screenWidth = 360, screenHeight = 640;
	final int brickWidth = 30, brickHeight = 20;
	final int paddleWidth = 60, paddleHeight = 20;
	final int ballWidth = 8, ballHeight = 8;
	final int eventClockTiming = 10;

	Texture sheet;
	TextureRegion regionParticleRed1;
	TextureRegion regionParticleRed2;
	TextureRegion regionParticleRed3;
	TextureRegion regionParticleGreen1;
	TextureRegion regionParticleGreen2;
	TextureRegion regionParticleGreen3;
	TextureRegion regionParticleYellow1;
	TextureRegion regionParticleYellow2;
	TextureRegion regionParticleYellow3;

	final Color colorBrickRed = new Color(Functions.toIntBitsRGBA(127, 0, 0, 255));
	final Color colorBrickYellow = new Color(Functions.toIntBitsRGBA(255, 216, 0, 255));
	final Color colorBrickGreen = new Color(Functions.toIntBitsRGBA(38, 127, 0, 255));
	final Color colorBallNormal = new Color(Functions.toIntBitsRGBA(0, 148, 255, 255));
	final Color colorBallPenetrating = new Color(Functions.toIntBitsRGBA(161, 127, 255, 255));
	final Color colorBallFast = new Color(Functions.toIntBitsRGBA(38, 127, 0, 255));
	final Color colorBallSmall = new Color(Functions.toIntBitsRGBA(127, 51, 0, 255));
	final Color colorPaddleLarge = new Color(Functions.toIntBitsRGBA(128, 128, 128, 255));

	int eventClock;
	int level;
	int highscore;
	int score;
	Rectangle arena;
	Paddle paddle;
	List<Ball> balls;
	List<Brick> bricks;
	List<Event> events;
	List<Upgrade> upgrades;
	List<Particle> particles;

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		g = new Graphics(batch, font);
		savegame = Gdx.app.getPreferences("savegame");
		previousMouseX = Mouse.x();

		loadHighscore();

		sheet = new Texture("sheet.png");
		regionParticleRed1 = TextureCutter.byBounds(sheet, 0, 0, 10, 7);
		regionParticleRed2 = TextureCutter.byBounds(sheet, 0, 7, 10, 7);
		regionParticleRed3 = TextureCutter.byBounds(sheet, 0, 14, 10, 7);
		regionParticleGreen1 = TextureCutter.byBounds(sheet, 10, 0, 10, 7);
		regionParticleGreen2 = TextureCutter.byBounds(sheet, 10, 7, 10, 7);
		regionParticleGreen3 = TextureCutter.byBounds(sheet, 10, 14, 10, 7);
		regionParticleYellow1 = TextureCutter.byBounds(sheet, 20, 0, 10, 7);
		regionParticleYellow2 = TextureCutter.byBounds(sheet, 20, 7, 10, 7);
		regionParticleYellow3 = TextureCutter.byBounds(sheet, 20, 14, 10, 7);

		reset();

		Gdx.input.setCursorCatched(true);
		Gdx.input.setInputProcessor(this);
	}

	private void reset() {
		arena = new Rectangle(25, 25, screenWidth - 50, screenHeight - 50);
		paddle = new Paddle();
		paddle.bounds = new Rectangle(centerX(arena) - paddleWidth / 2f, arena.y + paddleHeight, paddleWidth, paddleHeight / 2f);

		balls = new ArrayList<Ball>();
		bricks = new ArrayList<Brick>();
		upgrades = new ArrayList<Upgrade>();
		events = new ArrayList<Event>();
		particles = new ArrayList<Particle>();

		makeLevel();
		spawnBallNormal();
	}

	@Override
	public void render() {
		clock += 1;

		update();
		draw();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			Gdx.app.exit();
		}

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		float delta = Mouse.x() - previousMouseX;
		movePaddleTo(paddle.bounds.x + delta);

		previousMouseX = Mouse.x();

		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		float delta = Mouse.x() - previousMouseX;
		movePaddleTo(paddle.bounds.x + delta);

		previousMouseX = Mouse.x();

		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT) {
			shootBalls();
		}

		return true;
	}

	private void update() {
		updateEvents();
		updateBalls();
		updateBricks();
		updateUpgrades();
		updateParticles();

		updateLevel();
	}

	private void updateLevel() {
		if (bricks.isEmpty()) {
			if (score > highscore) {
				highscore = score;
				saveHighscore(score);
			}

			level += 1;
			reset();
		} else if (balls.isEmpty() && upgrades.isEmpty()) {
			level = 1;
			score = 0;
			reset();
		}
	}

	private void updateEvents() {
		if (clock % eventClockTiming == 0) {
			eventClock += 1;
		}

		for (Iterator<Event> it = events.iterator(); it.hasNext();) {
			Event event = it.next();

			if (event.timing == eventClock) {
				it.remove();

				event.applier.consume();
			}
		}
	}

	private void draw() {
		g.clear(.2f, .3f, .84f, 1);
		g.begin(this);
		g.rectangle(arena.x, 0, arena.width, arena.y + arena.height, Color.BLACK);
		for (Ball ball : balls) {
			g.rectangle(ball.bounds.x, ball.bounds.y, ball.bounds.width, ball.bounds.height, getBallColor(ball));
		}
		g.rectangle(paddle.bounds.x, paddle.bounds.y, paddle.bounds.width, paddle.bounds.height, Color.ORANGE);
		for (Brick brick : bricks) {
			g.rectangle(brick.bounds.x, brick.bounds.y, brick.bounds.width, brick.bounds.height, getBrickColor(brick));
		}
		for (Upgrade upgrade : upgrades) {
			g.rectangle(upgrade.bounds.x, upgrade.bounds.y, upgrade.bounds.width, upgrade.bounds.height, Color.WHITE);
			g.rectangle(upgrade.bounds.x + 1, upgrade.bounds.y + 1, upgrade.bounds.width - 2, upgrade.bounds.height - 2, upgrade.color);
			g.drawCenterXY(upgrade.sign, centerX(upgrade.bounds), centerY(upgrade.bounds), Color.BLACK);
		}
		for (Particle particle : particles) {
			g.draw(particle.region, particle.x, particle.y, particle.scale, particle.rotation);
		}
		g.draw("Level " + level, 40, 10, Color.WHITE);
		g.drawCenterX("Score " + score, centerX(arena), 10, Color.WHITE);

		String text = "High " + highscore;
		g.draw(text, arena.x + arena.width - font.getBounds(text).width - 10, 10, Color.WHITE);
		g.end(this);
	}

	private void updateBalls() {
		for (Iterator<Ball> it = balls.iterator(); it.hasNext();) {
			Ball ball = it.next();

			updateBall(ball);
			collideBall(ball);

			if (ball.dead) {
				it.remove();
			}
		}
	}

	private void updateBall(Ball ball) {
		if (ball.lockedToPaddle) {
			ball.bounds.x = centerX(paddle.bounds) - ball.bounds.width / 2f;
			ball.bounds.y = paddle.bounds.y + paddle.bounds.height + 4;
		} else {
			ball.velocity.nor().scl(ball.speed).scl(ball.speedMultiplier);
			move(ball.bounds, ball.velocity);
		}

		if (ball.bounds.y < 0) {
			ball.dead = true;
		}
	}

	private void updateUpgrades() {
		for (Iterator<Upgrade> it = upgrades.iterator(); it.hasNext();) {
			Upgrade upgrade = it.next();

			updateUpgrade(upgrade);

			if (upgrade.dead) {
				it.remove();
			}
		}
	}

	private void updateUpgrade(Upgrade upgrade) {
		if (upgrade.bounds.overlaps(paddle.bounds)) {
			upgrade.dead = true;

			upgrade.applier.consume();
		}

		move(upgrade.bounds, upgrade.velocity);

		if (upgrade.bounds.y < 0) {
			upgrade.dead = true;
		}
	}

	private void updateBricks() {
		for (Iterator<Brick> it = bricks.iterator(); it.hasNext();) {
			Brick brick = it.next();

			if (brick.health.isMin()) {
				it.remove();
			}
		}
	}

	private void updateParticles() {
		for (Iterator<Particle> it = particles.iterator(); it.hasNext();) {
			Particle particle = it.next();

			updateParticle(particle);

			if (particle.age >= particle.lifetime) {
				it.remove();
			}
		}
	}

	private void updateParticle(Particle particle) {
		particle.age += 1;

		particle.velX += particle.accX;
		particle.velY += particle.accY;

		particle.x += particle.velX;
		particle.y += particle.velY;

		particle.rotation += particle.angVel;
	}

	private void spawnUpgradeByChance(float x, float y) {
		int chance = 10 + upgrades.size() * 10;

		if (MathUtils.random(1, chance) == 1) {
			spawnUpgradeBallPenetrating(x, y);
		}
		if (MathUtils.random(1, chance) == 1) {
			spawnUpgradeBallFast(x, y);
		}
		if (MathUtils.random(1, chance) == 1) {
			spawnUpgradeBallSmall(x, y);
		}
		if (MathUtils.random(1, chance) == 1) {
			spawnUpgradePaddleLarge(x, y);
		}
	}

	private void spawnBrickParticles(int amount, Brick brick) {
		for (int i = 0; i < amount; i++) {
			Particle particle = new Particle();
			particle.region = getBrickParticleRegion(brick);
			particle.scale = 1;
			particle.lifetime = MathUtils.random(60, 90);
			particle.velX = Functions.negateByChance(MathUtils.random(1.2f));
			particle.velY = MathUtils.random(.2f, 1.3f);
			particle.accX = particle.velX * .01f * -1;
			particle.accY = .05f * -1;
			particle.angVel = Functions.negateByChance(MathUtils.random(5f));
			float bcx = centerX(brick.bounds);
			float bcy = centerY(brick.bounds);
			float rangeX = brick.bounds.width;
			float rangeY = brick.bounds.height;
			float rx = -rangeX / 2f + MathUtils.random(rangeX);
			float ry = -rangeY / 2f + MathUtils.random(rangeY);
			particle.x = bcx - particle.region.getRegionWidth() / 2f + rx;
			particle.y = bcy - particle.region.getRegionHeight() / 2f + ry;
			particles.add(particle);
		}
	}

	private void spawnBrick(float x, float y, int health) {
		Brick brick = new Brick();
		brick.bounds = new Rectangle(x, y, brickWidth, brickHeight);
		brick.health = new BoundedInt(health, 0, health);
		brick.damageTakenFrom = new ArrayList<Ball>();

		bricks.add(brick);
	}

	private Ball spawnBallNormal() {
		Ball ball = new Ball();
		ball.bounds = new Rectangle(centerX(paddle.bounds), paddle.bounds.y + paddle.bounds.height + 4, ballWidth, ballHeight);
		ball.velocity = new Vector2();
		ball.speed = 5;
		ball.speedMultiplier = 1;
		ball.lockedToPaddle = true;
		balls.add(ball);

		return ball;
	}

	private void spawnBallFast() {
		Ball ball = spawnBallNormal();
		ball.speedMultiplier = 1.2f;
	}

	private void spawnBallPenetrating(int amount) {
		Ball ball = spawnBallNormal();
		ball.penetratingRemaining = amount;
	}

	private void spawnBallSmall() {
		Ball ball = spawnBallNormal();
		ball.bounds.width *= .5f;
		ball.bounds.height *= .5f;
	}

	private void spawnUpgradeBallPenetrating(float x, float y) {
		Upgrade upgrade = new Upgrade();
		upgrade.applier = new Consumer() {
			@Override
			public void consume() {
				spawnBallPenetrating(8);
			}
		};
		upgrade.sign = "P";
		float width = 20, height = 20;
		upgrade.bounds = new Rectangle(x - width / 2f, y - height / 2f, width, height);
		upgrade.velocity = new Vector2(0, -2);
		upgrade.color = colorBallPenetrating;

		upgrades.add(upgrade);
	}

	private void spawnUpgradeBallFast(float x, float y) {
		Upgrade upgrade = new Upgrade();
		upgrade.applier = new Consumer() {
			@Override
			public void consume() {
				spawnBallFast();
			}
		};
		upgrade.sign = "F";
		float width = 20, height = 20;
		upgrade.bounds = new Rectangle(x - width / 2f, y - height / 2f, width, height);
		upgrade.velocity = new Vector2(0, -3);
		upgrade.color = colorBallSmall;

		upgrades.add(upgrade);
	}

	private void spawnUpgradeBallSmall(float x, float y) {
		Upgrade upgrade = new Upgrade();
		upgrade.applier = new Consumer() {
			@Override
			public void consume() {
				spawnBallSmall();
			}
		};
		upgrade.sign = "S";
		float width = 15, height = 15;
		upgrade.bounds = new Rectangle(x - width / 2f, y - height / 2f, width, height);
		upgrade.velocity = new Vector2(0, -2);
		upgrade.color = colorBallNormal;

		upgrades.add(upgrade);
	}

	private void spawnUpgradePaddleLarge(float x, float y) {
		Upgrade upgrade = new Upgrade();
		upgrade.applier = new Consumer() {
			@Override
			public void consume() {
				if (!paddle.large) {
					makePaddleSizeLarge();
					spawnEventPaddleSizeNormal(eventClockTiming * 5);
				}
			}
		};
		upgrade.sign = "L";
		float width = 25, height = 25;
		upgrade.bounds = new Rectangle(x - width / 2f, y - height / 2f, width, height);
		upgrade.velocity = new Vector2(0, -2);
		upgrade.color = colorPaddleLarge;

		upgrades.add(upgrade);
	}

	private void spawnEventPaddleSizeNormal(int delay) {
		final Event event = new Event();
		event.timing = eventClock + delay;
		event.applier = new Consumer() {
			@Override
			public void consume() {
				if (paddle.large) {
					makePaddleSizeNormal();
				}
			}
		};
		events.add(event);
	}

	private void makePaddleSizeLarge() {
		paddle.large = true;

		float scale = 1.6f;
		paddle.bounds.width = paddleWidth * scale;

		float delta = paddleWidth * scale - paddleWidth;
		movePaddleTo(paddle.bounds.x - delta / 2f);
	}

	private void makePaddleSizeNormal() {
		paddle.large = false;

		float delta = paddle.bounds.width - paddleWidth;
		paddle.bounds.width = paddleWidth;

		movePaddleTo(paddle.bounds.x + delta / 2f);
	}

	private void makeLevel() {
		if (level == 1) {
			makeBricksLevel1();
		} else if (level == 2) {
			makeBricksLevel2();
		} else if (level == 3) {
			makeBricksLevel3();
		} else {
			makeBricksLevelBeyond();
		}
	}

	private void makeBricksLevel1() {
		int countX = 5, countY = 3;
		int spacingX = 20, spacingY = 20;
		for (int x = 0; x < countX; x++) {
			for (int y = 0; y < countY; y++) {
				float ax = centerX(arena);
				float ay = arena.y + arena.height - arena.height / 3f;
				float bw = countX * brickWidth + (countX - 1) * spacingX;
				float bh = countY * brickHeight + (countY - 1) * spacingY;
				float boffx = x * brickWidth + x * spacingX;
				float boffy = y * brickHeight + y * spacingY;

				float xoff = ax - bw / 2f + boffx;
				float yoff = ay - bh / 2f + boffy;
				spawnBrick(xoff, yoff, 1);
			}
		}
	}

	private void makeBricksLevel2() {
		int countX = 4, countY = 5;
		int spacingX = 8, spacingY = 8;
		for (int x = 0; x < countX; x++) {
			for (int y = 0; y < countY; y++) {
				float ax = centerX(arena);
				float ay = arena.y + arena.height - arena.height / 3f;
				float bw = countX * brickWidth + (countX - 1) * spacingX;
				float bh = countY * brickHeight + (countY - 1) * spacingY;
				float boffx = x * brickWidth + x * spacingX;
				float boffy = y * brickHeight + y * spacingY;

				float xoff = ax - bw / 2f + boffx;
				float yoff = ay - bh / 2f + boffy;
				spawnBrick(xoff, yoff, 2);
			}
		}
	}

	private void makeBricksLevel3() {
		int countX = 6, countY = 8;
		int spacingX = 8, spacingY = 8;
		for (int x = 0; x < countX; x++) {
			for (int y = 0; y < countY; y++) {
				float ax = centerX(arena);
				float ay = arena.y + arena.height - arena.height / 3f;
				float bw = countX * brickWidth + (countX - 1) * spacingX;
				float bh = countY * brickHeight + (countY - 1) * spacingY;
				float boffx = x * brickWidth + x * spacingX;
				float boffy = y * brickHeight + y * spacingY;

				float xoff = ax - bw / 2f + boffx;
				float yoff = ay - bh / 2f + boffy;
				spawnBrick(xoff, yoff, 3);
			}
		}
	}

	private void makeBricksLevelBeyond() {
		int countX = 7, countY = 12;
		int spacingX = 12, spacingY = 12;
		for (int x = 0; x < countX; x++) {
			for (int y = 0; y < countY; y++) {
				float ax = centerX(arena);
				float ay = arena.y + arena.height - arena.height * 2f / 5f;
				float bw = countX * brickWidth + (countX - 1) * spacingX;
				float bh = countY * brickHeight + (countY - 1) * spacingY;
				float boffx = x * brickWidth + x * spacingX;
				float boffy = y * brickHeight + y * spacingY;

				float xoff = ax - bw / 2f + boffx;
				float yoff = ay - bh / 2f + boffy;
				spawnBrick(xoff, yoff, level);
			}
		}
	}

	private void collideBall(Ball ball) {
		if (ball.bounds.overlaps(paddle.bounds)) {
			CollisionResolve resolve = resolveBallAgainst(ball, paddle.bounds);

			if (resolve.side == Side.TOP) {
				float percent = Functions.percentage(resolve.offsetCenterX, paddle.bounds.width / 2f);
				float rotation = 10 + 20 * percent;

				if (resolve.offsetCenterX > 0) {
					ball.velocity.rotate(-rotation);
				} else {
					ball.velocity.rotate(rotation);
				}

				float minimumAngle = 30;
				if (ball.velocity.x > 0) {
					if (ball.velocity.angle() < minimumAngle) {
						ball.velocity.setAngle(minimumAngle);
					}
				} else {
					if (ball.velocity.angle() > 180 - minimumAngle) {
						ball.velocity.setAngle(180 - minimumAngle);
					}
				}
			}
		}

		for (Brick brick : bricks) {
			if (brick.bounds.overlaps(ball.bounds)) {
				damageBrick(brick, ball);

				if (ball.penetratingRemaining == 0) {
					resolveBallAgainst(ball, brick.bounds);
				} else {
					ball.penetratingRemaining -= 1;
				}
			}
		}

		if (ball.bounds.x <= arena.x) {
			ball.bounds.x = arena.x;

			ball.velocity.x = -ball.velocity.x;
		}
		if (ball.bounds.x >= arena.x + arena.width - ball.bounds.width) {
			ball.bounds.x = arena.x + arena.width - ball.bounds.width;

			ball.velocity.x = -ball.velocity.x;
		}
		if (ball.bounds.y >= arena.y + arena.height - ball.bounds.height) {
			ball.bounds.y = arena.y + arena.height - ball.bounds.height;

			ball.velocity.y = -ball.velocity.y;
		}
	}

	private CollisionResolve resolveBallAgainst(Ball ball, Rectangle bounds) {
		CollisionResolve resolve = new CollisionResolve();

		boolean left = false, right = false, top = false, bottom = false;
		float dx, dy;
		if (ball.velocity.x > 0) {
			dx = (ball.bounds.x + ball.bounds.width) - bounds.x;
			left = Math.abs(dx) < ball.bounds.width;
		} else {
			dx = ball.bounds.x - (bounds.x + bounds.width);
			right = Math.abs(dx) < ball.bounds.width;
		}
		if (ball.velocity.y > 0) {
			dy = (ball.bounds.y + ball.bounds.height) - bounds.y;
			bottom = Math.abs(dy) < ball.bounds.height;
		} else {
			dy = ball.bounds.y - (bounds.y + bounds.height);
			top = Math.abs(dy) < ball.bounds.height;
		}

		if (left && Math.abs(dx) < Math.abs(dy)) {
			ball.bounds.x = bounds.x - ball.bounds.width;
			ball.velocity.x = -ball.velocity.x;
			resolve.side = Side.LEFT;
		} else if (right && Math.abs(dx) < Math.abs(dy)) {
			ball.bounds.x = bounds.x + bounds.width;
			ball.velocity.x = -ball.velocity.x;
			resolve.side = Side.RIGHT;
		} else if (top && Math.abs(dx) > Math.abs(dy)) {
			ball.bounds.y = bounds.y + bounds.height;
			ball.velocity.y = -ball.velocity.y;
			resolve.side = Side.TOP;
		} else if (bottom && Math.abs(dx) > Math.abs(dy)) {
			ball.bounds.y = bounds.y - ball.bounds.height;
			ball.velocity.y = -ball.velocity.y;
			resolve.side = Side.BOTTOM;
		} else {
			resolve.side = Side.NONE;
		}

		resolve.offsetCenterX = centerX(ball.bounds) - (centerX(bounds));

		return resolve;
	}

	private void shootBalls() {
		for (Ball ball : balls) {
			shootBall(ball);
		}
	}

	private void shootBall(Ball ball) {
		if (ball.lockedToPaddle) {
			ball.lockedToPaddle = false;

			float rotation = Functions.negateByChance(MathUtils.random(45));
			ball.velocity.set(0, 1).scl(ball.speed).rotate(rotation);
		}
	}

	private void movePaddleTo(float x) {
		paddle.bounds.x = MathUtils.clamp(x, arena.x, arena.x + arena.width - paddle.bounds.width);
	}

	private void damageBrick(Brick brick, Ball ball) {
		boolean canDamageBrick = true;

		if (ball.penetratingRemaining > 0 && brick.damageTakenFrom.contains(ball)) {
			canDamageBrick = false;
		}

		if (canDamageBrick) {
			int amount = MathUtils.random(3, 4);
			if (brick.health.isMin()) {
				amount *= 3;
			}
			spawnBrickParticles(amount, brick);

			brick.health.subtract(1);
			brick.damageTakenFrom.add(ball);

			spawnUpgradeByChance(centerX(brick.bounds), centerY(brick.bounds));
			gainScore(brick, ball);
		}
	}

	private void gainScore(Brick brick, Ball ball) {
		int low = 1 * level / 2 + 1;
		int high = 1 * level * level / 8 + 1 + low;
		int points = MathUtils.random(low, high);

		score += points;
	}

	private TextureRegion getBrickParticleRegion(Brick brick) {
		float percent = Functions.percentage(brick.health.value(), brick.health.max());

		if (percent >= .5f) {
			return Functions.randomElement(regionParticleGreen1, regionParticleGreen2, regionParticleGreen3);
		}
		if (percent >= .25f) {
			return Functions.randomElement(regionParticleYellow1, regionParticleYellow2, regionParticleYellow3);
		}

		return Functions.randomElement(regionParticleRed1, regionParticleRed2, regionParticleRed3);
	}

	private Color getBrickColor(Brick brick) {
		float percent = Functions.percentage(brick.health.value(), brick.health.max());

		if (percent >= .5f) {
			return colorBrickGreen;
		}
		if (percent >= .25f) {
			return colorBrickYellow;
		}

		return colorBrickRed;
	}

	private Color getBallColor(Ball ball) {
		if (ball.penetratingRemaining > 0) {
			return colorBallPenetrating;
		}
		if (ball.speedMultiplier > 1) {
			return colorBallFast;
		}

		return colorBallNormal;
	}

	private void move(Rectangle bounds, Vector2 velocity) {
		bounds.x += velocity.x;
		bounds.y += velocity.y;
	}

	private float centerX(Rectangle bounds) {
		return bounds.x + bounds.width / 2f;
	}

	private float centerY(Rectangle bounds) {
		return bounds.y + bounds.height / 2f;
	}

	private void loadHighscore() {
		highscore = savegame.getInteger("highscore", 0);
	}

	private void saveHighscore(int score) {
		savegame.putInteger("highscore", score);
		savegame.flush();
	}

	class CollisionResolve {
		float offsetCenterX;
		Side side;
	}

	enum Side {
		NONE,
		LEFT,
		RIGHT,
		TOP,
		BOTTOM;
	}

	class Paddle {
		Rectangle bounds;
		boolean large;
	}

	class Brick {
		Rectangle bounds;
		BoundedInt health;
		List<Ball> damageTakenFrom;
	}

	class Ball {
		Rectangle bounds;
		Vector2 velocity;
		float speed;
		float speedMultiplier;
		int penetratingRemaining;
		boolean lockedToPaddle;
		boolean dead;
	}

	class Upgrade {
		String sign;
		Rectangle bounds;
		Vector2 velocity;
		Color color;
		boolean dead;
		Consumer applier;
	}

	class Event {
		int timing;
		Consumer applier;
	}

	interface Consumer {
		void consume();
	}

}
