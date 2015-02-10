package breakout;



import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {

	public static void main(String[] args) {
		LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
		configuration.title = "";
		configuration.width = 360;
		configuration.height = 640;
		configuration.foregroundFPS = 60;
		configuration.resizable = false;
		configuration.vSyncEnabled = false;
		configuration.fullscreen = false;

		new LwjglApplication(new Game(), configuration);
	}
	
}
