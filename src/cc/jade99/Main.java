package cc.jade99;

//Imports
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;


//cc.jade99.Main class definition
public class Main extends Application {

    //Defining global FXComponents
    private final Pane rootPane = new Pane();
    private Stage primary;
    private Color[] colors;


    //Defining min and max window resolutions
    private final double minWidth = 352.0;
    private final double maxWidth = 3860.0;

    private final double minHeight = 240.0;
    private final double maxHeight = 2160.0;

    private double width = 1280.0;
    private double height = 720.0;


    //Defining variables required for calculation
    private int maxIterations = 1;
    private int iterations = 0;

    private final double realOffset = 0.0;
    private final double imaginaryOffset = 0.0;
    private final double zoom = 1.0;


    //Program entry-point
    public static void main(String[] args) {
        Main.launch(args);
    }

    @Override //JFX start-Function
    public void start(Stage primaryStage) {
        primary = primaryStage;


        //Setting rootPane max and min sizes
        getRootPane().setMinSize(getMaxWidth(), getMaxHeight());
        getRootPane().setMinSize(getMinWidth(), getMinHeight());


        //Defining main scene
        Scene scene = new Scene(getRootPane(), getWidth(), getHeight());


        //First initial draw
        draw();


        //listeners
        {

            //Width Listener
            scene.widthProperty().addListener((observable, oldValue, newValue) -> {
                setWidth(newValue.doubleValue());
                draw();
            });


            //Height Listener
            scene.heightProperty().addListener((observable, oldValue, newValue) -> {
                setHeight(newValue.doubleValue());
                draw();
            });


            //KeyBoard Listener
            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case PLUS:
                        incrementMaxIterations();
                        draw();
                        break;
                    case MINUS:
                        decrementMaxIterations();
                        draw();
                        break;
                    case MULTIPLY:
                        doubleMaxIterations();
                        draw();
                        break;
                    case DIVIDE:
                        halfMaxIterations();
                        draw();
                        break;
                }
            });
        }


        //Setting scene of stage and show it
        getPrimary().setScene(scene);
        getPrimary().show();

        //Fit stage size to scene
        getPrimary().sizeToScene();


        //Calculating window border differences
        double widthDiff = primaryStage.getWidth() - getWidth();
        double heightDiff = primaryStage.getHeight() - getHeight();


        //setting correct min and max sizes for stage
        getPrimary().setMinWidth(getMinWidth() + widthDiff);
        getPrimary().setMinHeight(getMinHeight() + heightDiff);
        getPrimary().setMaxWidth(getMaxWidth() + widthDiff);
        getPrimary().setMaxHeight(getMaxHeight() + heightDiff);
    }


    //draw a frame
    private void draw() {
        getPrimary().setTitle(generateTitle()); //setting Title
        generatePallet(); //generating color pallet


        //Creating JFX objects
        Canvas canvas = new Canvas(getWidth(), getHeight());
        GraphicsContext canvasGraphics = canvas.getGraphicsContext2D();
        PixelWriter pxWriter = canvasGraphics.getPixelWriter();


        //Calculating point map
        Map<Point2D, Integer> values = calc();


        //Drawing each point in point map
        for (Map.Entry<Point2D, Integer> pixel : values.entrySet()) {
            int xPixel = (int) pixel.getKey().getX();
            int yPixel = (int) pixel.getKey().getY();
            Color color = (pixel.getValue() <  getMaxIterations()) ? getColors()[pixel.getValue()] : Color.BLACK;

            pxWriter.setColor(xPixel, yPixel, color);
        }


        //Drawing center cross
        {
            canvasGraphics.setStroke(Color.BLACK);
            canvasGraphics.setLineWidth(1.0);
            canvasGraphics.strokeLine(0.0, getHeight() / 2.0, getWidth(), getHeight() / 2.0);
            canvasGraphics.strokeLine(getWidth() / 2.0, 0.0, getWidth() / 2.0, getHeight());
        }


        //adding canvas to rootPane
        getRootPane().getChildren().add(canvas);
    }


    //Calculating Mandelbrot Set
    private Map<Point2D, Integer> calc() {
        //casting dimensions to int
        final int intWidth = (int) getWidth();
        final int intHeight = (int) getHeight();


        //Declaring HashMap
        Map<Point2D, Integer> pixel = new HashMap<>();


        //Iterating through every pixel on canvas
        for (int x = 0; x < intWidth; x++) {
            for (int y = 0; y < intHeight; y++) {
                Point2D location = new Point2D(x, y); //Defining Point with canvas coordinates

                //getting min and max dimensions
                int minDim = Math.min(intWidth, intHeight);
                int maxDim = Math.max(intWidth, intHeight);

                //defining start points
                double startX = (intWidth == maxDim) ? (maxDim - minDim) / 2.0 : 0.0;
                double startY = (intHeight == maxDim) ? (maxDim - minDim) / 2.0 : 0.0;


                //declaring mandelbrot variables
                double realZ, imaginaryZ, tempZ;
                double realC, imaginaryC;
                realZ = imaginaryZ = 0.0;

                //mapping canvas coordinates to mandelbrot coordinates, applying zoom and offset
                realC = map(x, startX, startX + minDim, -2.0, 2.0) / zoom + realOffset;
                imaginaryC = map(y, startY, startY + minDim, 2.0, -2.0) / zoom + imaginaryOffset;

                nullIterations(); //nulling iterator

                //main calculations
                while (getIterations() < getMaxIterations() && (realZ * realZ + imaginaryZ * imaginaryZ < 4.0)) {
                    tempZ = realZ * realZ - imaginaryZ * imaginaryZ + realC;
                    imaginaryZ = 2 * realZ * imaginaryZ + imaginaryC;
                    realZ = tempZ;

                    incrementIterations();
                }

                //putting location and iterations in map
                pixel.put(location, getIterations());
            }
        }
        return pixel;
    }


    //method for generating window title
    private String generateTitle() {
        String title = "Mandelbrot Set -"; //main name
        title += String.format("Iterations: %d, ", getMaxIterations()); //iterations
        title += String.format("Location: %f + %fi, ", getRealOffset(), getImaginaryOffset()); //offset location
        title += String.format("Zoom: %.2f%%", getZoom() * 100.0); //zoom
        return title;
    }


    //method for generating color pallet
    private void generatePallet() {
        setColors(new Color[getMaxIterations()]); //initializing color pallet with current maxIterations

        //defining half and quarter iteration count for gradient
        final int halfIterations = getMaxIterations() / 2;
        final int quarterIterations = getMaxIterations() / 4;


        //gradient loop
        for (int i = 0; i < getColors().length; i++) {
            if (i < quarterIterations) {
                //blue to white
                getColors()[i] = Color.hsb(240.0, map(i, 0.0, quarterIterations - 1.0, 1.0, 0.0),1.0);
            } else if (i < halfIterations) {
                //white to yellow
                getColors()[i] = Color.hsb(60.0, map(i, (double) quarterIterations, halfIterations - 1.0, 0.0, 1.0), 1.0);
            } else {
                //yellow -> green -> cyan -> blue
                getColors()[i] = Color.hsb(map(i, halfIterations, getMaxIterations() - 1.0, 60.0, 240.0), 1.0, 1.0);
            }
        }
    }

    private double map(double val, double originalMin, double originalMax, double mappedMin, double mappedMax) {
        return mappedMin + (mappedMax - mappedMin) * ((val - originalMin) / (originalMax - originalMin));
    }

    //Getter

    private Pane getRootPane() {
        return rootPane;
    }

    private Stage getPrimary() {
        return primary;
    }

    private Color[] getColors() {
        return colors;
    }

    private double getMinWidth() {
        return minWidth;
    }

    private double getMaxWidth() {
        return maxWidth;
    }

    private double getMinHeight() {
        return minHeight;
    }

    private double getMaxHeight() {
        return maxHeight;
    }

    private double getWidth() {
        return width;
    }

    private double getHeight() {
        return height;
    }

    private int getMaxIterations() {
        return maxIterations;
    }

    private int getIterations() {
        return iterations;
    }

    private double getRealOffset() {
        return realOffset;
    }

    private double getImaginaryOffset() {
        return imaginaryOffset;
    }

    private double getZoom() {
        return zoom;
    }


    //Basic setter

    private void setColors(Color[] colors) {
        this.colors = colors;
    }

    private void setWidth(double width) {
        this.width = (width >= minWidth && width <= maxWidth) ? width : this.width;
    }

    private void setHeight(double height) {
        this.height = (height >= minHeight && height <= maxHeight) ? height : this.height;
    }

    //Special setter

    private void incrementMaxIterations() {
        maxIterations++;
    }

    private void decrementMaxIterations() {
        if (maxIterations > 1) maxIterations--;
        else maxIterations = 1;
    }

    private void doubleMaxIterations() {
        maxIterations *= 2.0;
    }

    private void halfMaxIterations() {
        if (maxIterations > 2) maxIterations /= 2;
        else maxIterations = 1;
    }

    private void nullIterations() {
        this.iterations = 0;
    }

    private void incrementIterations() {
        this.iterations++;
    }
}
