package skelemarket;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    private double customerX = 50;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext g = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);

        stage.setScene(new Scene(root));
        stage.setTitle("Supermarket Simulation");
        stage.show();

        new AnimationTimer() {
            private long previousTime = 0;

            @Override
            public void handle(long now) {
                if (previousTime == 0) {
                    previousTime = now;
                    return;
                }

                double dt = (now - previousTime) / 1_000_000_000.0;
                previousTime = now;

                update(dt);
                draw(g);
            }
        }.start();
    }

    private void update(double dt) {
        customerX += 50 * dt;
    }

    private void draw(GraphicsContext g) {

        // Clear screen
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, 800, 600);

        // Shelf
        g.setFill(Color.DARKGRAY);
        g.fillRect(400, 100, 300, 80);

        // Customer
        g.setFill(Color.BLUE);
        g.fillOval(customerX, 300, 32, 32);

        // Draw image (from resources)
        Image img = new Image(getClass().getResource("/puzzled-skeleton.png").toExternalForm());
        g.drawImage(img, 100, 100);

    }

    public static void main(String[] args) {
        launch();
    }
}
