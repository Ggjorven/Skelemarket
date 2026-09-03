package skelemarket;

import javafx.application.Application;
import javafx.stage.Stage;

import skelemarket.core.Simulation;

public class Main extends Application {
    private Simulation mSimulation = null;

    @Override
    public void start(Stage stage) {
		mSimulation = new Simulation(stage);
		mSimulation.run();
    }

    public static void main(String[] args) {
        launch();
    }
}
