import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Main extends Application {
    private final Pane rootPane = new Pane();

    private final double minWidth = 352.0;
    private final double maxWidth = 3860.0;

    private final double minHeight = 240.0;
    private final double maxHeight = 2160.0;

    private final int maxIterations = 256;

    private final Color[] colors = new Color[maxIterations];

    private int iterations = 0;

    private double width = 1280.0;
    private double height = 720.0;

    public static void main(String[] args) {
        Main.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mandelbrot Set");

        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);

        primaryStage.setMaxWidth(maxWidth);
        primaryStage.setMaxHeight(maxHeight);

        Scene scene = new Scene(rootPane, width, height);

        final int halfIterations = maxIterations / 2;
        final int quarterIterations = maxIterations / 4;

        for (int i = 0; i < colors.length; i++) {
            if (i < quarterIterations) {
                colors[i] = Color.hsb(240.0, map(i, 0.0, quarterIterations - 1.0, 1.0, 0.0),1.0);
            } else if (i < halfIterations) {
                colors[i] = Color.hsb(60.0, map(i, (double) quarterIterations, halfIterations - 1.0, 0.0, 1.0), 1.0);
            } else {
                colors[i] = Color.hsb(map(i, halfIterations, maxIterations - 1.0, 60.0, 240.0), 1.0, 1.0);
            }
        }

        redraw();

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            setWidth(newValue.doubleValue());
            redraw();
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            setHeight(newValue.doubleValue());
            redraw();
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void redraw() {
        Canvas canvas = new Canvas(getWidth(), getHeight());

        getRootPane().getChildren().add(canvas);

        PixelWriter pxWriter = canvas.getGraphicsContext2D().getPixelWriter();
        Map<Point2D, Integer> values = calc();

        for (Map.Entry<Point2D, Integer> pixel : values.entrySet()) {
            pxWriter.setColor((int) pixel.getKey().getX(), (int) pixel.getKey().getY(), (pixel.getValue() <  maxIterations) ? colors[pixel.getValue()] : Color.BLACK);
        }
    }

    private Map<Point2D, Integer> calc() {
        final int intWidth = (int) getWidth();
        final int intHeight = (int) getHeight();
        Map<Point2D, Integer> pixel = new HashMap<>();

        for (int x = 0; x < intWidth; x++) {
            for (int y = 0; y < intHeight; y++) {
                Point2D location = new Point2D(x, y);

                int minDim = Math.min(intWidth, intHeight);
                int maxDim = Math.max(intWidth, intHeight);

                double startX, startY;
                startX = (intWidth == maxDim) ? (maxDim - minDim) / 2.0 : 0.0;
                startY = (intHeight == maxDim) ? (maxDim - minDim) / 2.0 : 0.0;

                double realZ, imaginaryZ, tempZ;
                double realC, imaginaryC;

                realZ = imaginaryZ = 0.0;

                realC = map(x, startX, startX + minDim, -2.0, 2.0);
                imaginaryC = map(y, startY, startY + minDim, 2.0, -2.0);

                nullIterations();
                while (getIterations() < getMaxIterations() && (realZ * realZ + imaginaryZ * imaginaryZ < 4.0)) {
                    tempZ = realZ * realZ - imaginaryZ * imaginaryZ + realC;
                    imaginaryZ = 2 * realZ * imaginaryZ + imaginaryC;
                    realZ = tempZ;

                    incrementIterations();
                }

                pixel.put(location, getIterations());
            }
        }
        return pixel;
    }

    private double map(double val, double originalMin, double originalMax, double mappedMin, double mappedMax) {
        return mappedMin + (mappedMax - mappedMin) * ((val - originalMin) / (originalMax - originalMin));
    }

    private Pane getRootPane() {
        return rootPane;
    }

    private double getWidth() {
        return width;
    }

    private void setWidth(double width) {
        this.width = (width >= minWidth && width <= maxWidth) ? width : this.width;
    }

    private double getHeight() {
        return height;
    }

    private void setHeight(double height) {
        this.height = (height >= minHeight && height <= maxHeight) ? height : this.height;
    }

    private int getMaxIterations() {
        return maxIterations;
    }

    private int getIterations() {
        return iterations;
    }

    private void nullIterations() {
        this.iterations = 0;
    }

    private void incrementIterations() {
        this.iterations++;
    }
}
