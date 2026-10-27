package skelemarket;

import skelemarket.core.Logger;
import skelemarket.core.Simulation;

import javafx.stage.Stage;

public class Application extends javafx.application.Application {
	private Simulation mSimulation;

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
}
