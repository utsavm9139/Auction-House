import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import java.util.Random;

import static java.lang.Math.cos;
import static java.lang.Math.toRadians;
import static java.lang.StrictMath.sin;

public class main extends Application {

    //width of final circle
    private static final double WIDTH = 1200;
    //height of final circle
    private static final double HEIGHT = 800;
    private double initialTimesTableNum = 2;
    private double numPoints = 360;
    //stores the x-coordinates of all points
    private double[] xPos =new double[(int)numPoints];
    //stores the y-coordinatees of all points
    private double[] yPos =new double[(int)numPoints];
    private Random random = new Random();

    Color[] colors = {Color.RED, Color.BLACK, Color.BLUE, Color.ORANGE,
            Color.PURPLE, Color.GREEN, Color.YELLOW, Color.VIOLET,
            Color.INDIGO, Color.LIME, Color.NAVY};

    /**
     * returns the x coordinates of generated points
     * @param num number of points to be generated
     * @return array of x coordinates
     */
    private double[] generateX(double num){
        double xPositions[] = new double[(int)num];
        int i=0;
        double distanceBetPoints = 360.0/num;
        for(double angle = 180; angle < 540; angle += distanceBetPoints) {
            double x = cos(toRadians(angle)) * 250+600f;
            xPositions[i] = x;
            i++;
        }
        return xPositions;
    }

    /**
     * returns the y coordinates of generated points
     * @param num number of points to be generated
     * @return array of y coordinates
     */
    private double[] generateY(double num){
        double yPositions[] = new double[(int)num];
        int i=0;
        double distanceBetPoints = 360.0/num;
        for(double angle = 180; angle < 540; angle += distanceBetPoints) {
            double y = sin(toRadians(angle)) * 250+400f;
            yPositions[i] = y;
            i++;
        }
        return yPositions;
    }




    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Times Table Visualization");


        final Circle circle = new Circle(WIDTH/2,
                HEIGHT / 2, 250);
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.TRANSPARENT);

        //organize labels, buttons, and choiceBox with uniform spacing
        VBox allControls = new VBox(12);
        allControls.setLayoutX(15);
        allControls.setLayoutY(230);

        //buttons in the gui
        Button start = new Button("Start");
        Button pause = new Button("Pause");
        Button jumpToButton = new Button("Jump To");
        Button jumpToFavButton = new Button("Jump To Favorite");


        //"Increase by:" label and corresponding slider to it
        HBox incerementBy = new HBox(12);
        Label increment = new Label("Increase by:");
        Slider sliderForIncrement = new Slider(0, 6, 1);
        sliderForIncrement.setShowTickLabels(true);
        sliderForIncrement.setMajorTickUnit(0.25f);
        incerementBy.getChildren().addAll(increment, sliderForIncrement);

        //"Delay by: " label and corresponding slider to it
        HBox delayBy = new HBox(12);
        Label delay = new Label("Delay by:");
        Slider sliderForDelay = new Slider(0, 5, 1);
        sliderForDelay.setShowTickLabels(true);
        sliderForDelay.setMajorTickUnit(0.25f);
        delayBy.getChildren().addAll(delay, sliderForDelay);

        Label jumpLabel = new Label("Jump To Parameters Section");

        //"Times Table Number:" label and corresponding text field
        HBox timesNum = new HBox(12);
        Label timeNumLabel = new Label("Times Table Number:");
        TextField timesNumTextField = new TextField("2");
        timesNum.getChildren().addAll(timeNumLabel, timesNumTextField);

        //"Number of Points:" label and corresponding text field
        HBox numPointsBox = new HBox(12);
        Label numPointsLabel = new Label("Number Of Points:");
        TextField numPointsText = new TextField("360");
        numPointsBox.getChildren().addAll(numPointsLabel, numPointsText);

        //choicebox for favorite visual tour
        ChoiceBox choiceBox = new ChoiceBox();
        choiceBox.getItems().add("Favorite 1");
        choiceBox.getItems().add("Favorite 2");
        choiceBox.getItems().add("Favorite 3");
        choiceBox.getItems().add("Favorite 4");
        choiceBox.getItems().add("Favorite 5");


        //adding all controls to the assigned vbox
        allControls.getChildren().addAll(
                start, pause, incerementBy, delayBy,
                jumpLabel, timesNum, numPointsBox,
                jumpToButton, choiceBox, jumpToFavButton);

        //pane within the main screen where animation occurs
        Pane root2 = new Pane();
        root2.setMouseTransparent(true);
        //main screen
        Pane root = new Pane(allControls,circle,root2);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.show();


        AnimationTimer at = new AnimationTimer() {
            private long update = 0;

            /**
             * function to update the screen
             * @param now current time in nanoseconds
             */
            @Override
            public void handle(long now) {
                if (now - update >= sliderForDelay.getValue()
                        * 1_000_000_000) {
                    //checks the number of points in text field
                    if (!numPointsText.getText().equals("")) {
                        initialTimesTableNum+=sliderForIncrement.getValue();
                        xPos = generateX(numPoints);
                        yPos = generateY(numPoints);
                        //clear the lines
                        root2.getChildren().clear();
                        //generate random index value for color array
                        int randomNum =
                                random.nextInt(colors.length-1)+1;
                            /*
                            generates the corresponding point
                            for a given point
                             */
                        for (int j = 0; j < numPoints; j++) {
                            int endPoints = (int)initialTimesTableNum * j;
                            while (endPoints > numPoints -1){
                                endPoints = endPoints - (int)numPoints;
                            }

                            Line line = new Line(xPos[j],yPos[j],
                                    xPos[endPoints],yPos[endPoints]);

                            //sets the color
                            line.setStroke(colors[randomNum]);
                            //adds the line to screen
                            root2.getChildren().addAll(line);
                        }

                        update = now;

                    }
                }


            }
        };

        //starts the animation
        start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                at.start();
            }
        });

        //pauses the animation
        pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                at.stop();
            }
        });


        /*
        jumps the animation to the desired times table number and number
        of points
         */
        jumpToButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                initialTimesTableNum =
                        Double.parseDouble(timesNumTextField.getText());
                numPoints = Double.parseDouble(numPointsText.getText());
                //checks the number of points in text field
                if (!numPointsText.getText().equals("")) {
                    //clears the screen
                    root2.getChildren().clear();
                    //generates the random number
                    int randomNum = random.nextInt(colors.length-1);
                    /*
                      generates the corresponding point
                            for a given point
                      */
                    for (int j = 0; j < numPoints; j++) {
                        int endPoints1 = (int)initialTimesTableNum * j;
                        while (endPoints1 > numPoints){
                            endPoints1 = endPoints1 - (int)numPoints;
                        }
                        Line line = new Line(xPos[j],yPos[j],
                                xPos[endPoints1],yPos[endPoints1]);
                        line.setStroke(colors[randomNum]);
                        root2.getChildren().addAll(line);
                    }

                }
                at.stop();

            }
        });

        //takes to the favorite visual
        jumpToFavButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(choiceBox.getValue() == "Favorite 1"){
                    initialTimesTableNum = 20;
                    numPoints = 359;
                }
                else if(choiceBox.getValue() == "Favorite 2"){
                    initialTimesTableNum = 40;
                    numPoints = 359;
                }

                else if(choiceBox.getValue() == "Favorite 3"){
                    initialTimesTableNum = 100;
                    numPoints = 359;
                }
                else if(choiceBox.getValue() == "Favorite 4"){
                    initialTimesTableNum = 280;
                    numPoints = 359;
                }
                else{
                    initialTimesTableNum = 330;
                    numPoints = 359;
                }

                if (!numPointsText.getText().equals("")) {
                    root2.getChildren().clear();
                    int randomNum = random.nextInt(colors.length - 1) +1;
                    /*
                      generates the corresponding point
                            for a given point
                      */
                    for (int j = 0; j < numPoints; j++) {
                        int endPoints1 = (int) initialTimesTableNum * j;
                        while (endPoints1 > numPoints) {
                            endPoints1 = endPoints1 - (int) numPoints;
                        }
                        Line line = new Line(xPos[j], yPos[j],
                                xPos[endPoints1], yPos[endPoints1]);
                        line.setStroke(colors[randomNum]);
                        root2.getChildren().addAll(line);
                    }

                }
                at.stop();
            }

        });

    }

    public static void main(String[] args) {
        launch(args);
    }


}
