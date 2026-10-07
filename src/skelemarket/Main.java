package skelemarket;

import javafx.application.Application;
import javafx.stage.Stage;

import skelemarket.core.Simulation;
import skelemarket.core.Logger;

public class Main extends Application {
	private Simulation mSimulation = null;

	@Override
	public void start(Stage stage) {
		try {
			mSimulation = new Simulation(stage);
			mSimulation.run();
		} catch (Exception ex) {
			Logger.error("Failed to start JavaFX application due to error: %s.", ex.toString());
			stage.close();
		}
	}

	public static void main(String[] args) {
		launch();
	}
}
