import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import java.util.Random;
import static java.lang.StrictMath.*;

public class timeTables extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    /*Variables required for the program*/
    //times table multiple number
    int times_number = 2;
    //times table point number
    int num_circlePoints = 360;
    //arrays to store generated points around the circle
    double[] points_X = new double[num_circlePoints];
    double[] points_Y = new double[num_circlePoints];

    /*Here we draw line animation*/
    AnchorPane animationSpace = new AnchorPane();

    /*randomly generates the color for line*/
    public Color get_line_Color(){
        Color[] line_Color = {Color.BLACK,Color.RED,Color.BLUE,Color.GREEN,Color.GOLD,Color.PURPLE,
                Color.DARKGRAY,Color.ORANGE,Color.BROWN,Color.CYAN};
        Random rand = new Random();
        int rand_Num = rand.nextInt(9) + 1;
        return line_Color[rand_Num];
    }

    /*generates uniform points around the circle*/
    public void generate_Points(int points){
        for (int i = 0; i < points; i++) {
            points_X[i] = cos(toRadians(360 * i / points)) * 250 + 600f;
            points_Y[i] = sin(toRadians(360 * i / points)) * 250 + 400f;
        }
    }

    /*draw line with starting and end points with given random color*/
    public void draw_line(){
        animationSpace.getChildren().clear();
        Color line_color = get_line_Color();
        for (int j = 0; j < num_circlePoints; j++) {
            int connecting_point = times_number * j;
            while (connecting_point > num_circlePoints -1){
                connecting_point = connecting_point - num_circlePoints;
            }
            Line line = new Line();
            line.setStartX(points_X[j]);
            line.setStartY(points_Y[j]);
            line.setEndX(points_X[connecting_point]);
            line.setEndY(points_Y[connecting_point]);
            line.setStroke(line_color);
            animationSpace.getChildren().addAll(line);
        }
    }

    @Override
    public void start(Stage Stage) throws Exception {
        VBox box = new VBox(20);
        HBox box1 = new HBox(20);
        HBox box2 = new HBox(20);
        HBox box3 = new HBox(20);
        HBox box4 = new HBox(20);
        HBox box5 = new HBox(20);
        HBox box6 = new HBox(20);
        HBox box7 = new HBox(20);
        HBox box8 = new HBox(20);
        HBox box9 = new HBox(20);

        TextField text1 = new TextField();
        TextField text2 = new TextField();

        Button button1 = new Button("Start");
        Button button2 = new Button("Pause");
        Button button3 = new Button("Jump to");
        Button button4 = new Button("Jump to favorite");

        ChoiceBox<String> favorite = new ChoiceBox<>();
        favorite.getItems().add("25 = 360");
        favorite.getItems().add("170 = 360");
        favorite.getItems().add("358 = 360");
        favorite.getItems().add("177 = 360");
        favorite.getItems().add("359 = 360");

        Slider slider1 = new Slider(0,5,0);
        slider1.setShowTickMarks(true);
        slider1.setShowTickLabels(true);
        slider1.setMajorTickUnit(1);

        Slider slider2 = new Slider(0,5,1);
        slider2.setShowTickMarks(true);
        slider2.setShowTickLabels(true);
        slider2.setMajorTickUnit(1);

        Label text_label1 = new Label("Increment by:");
        Label text_label2 = new Label("Delay by(seconds):");
        Label text_label3 = new Label("Times Table Number:");
        Label text_label4 = new Label("Number of Points:");

        box1.getChildren().addAll(button1);
        box2.getChildren().addAll(button2);
        box3.getChildren().addAll(text_label1,slider1);
        box4.getChildren().addAll(text_label2,slider2);
        box5.getChildren().addAll(text_label3,text1);
        box6.getChildren().addAll(text_label4,text2);
        box7.getChildren().addAll(button3);
        box8.getChildren().addAll(favorite);
        box9.getChildren().addAll(button4);

        box.getChildren().addAll(box1,box2,box3,box4,box5,box6,box7,box8,box9);
        box.setLayoutY(100);
        box.setLayoutX(20);

        final Circle circle = new Circle(600,400,250);
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.TRANSPARENT);
        AnchorPane showCircle = new AnchorPane(circle);
        showCircle.setMouseTransparent(true);

        animationSpace.setMouseTransparent(true);

        AnchorPane screen = new AnchorPane(box,showCircle,animationSpace);
        Scene scene = new Scene(screen,1200,800, Color.BLUE);
        Stage.setScene(scene);
        Stage.setTitle("Modulo Times Tables Visualization");
        Stage.show();

        AnimationTimer timer = new AnimationTimer() {
            private long startTime = -1;
            @Override
            public void handle(long now) {
                if ((now - startTime) >= slider2.getValue() * 1_000_000_000) {
                    //takes value from slier one if any
                    times_number+=slider1.getValue();
                    animationSpace.getChildren().clear();
                    //generating points for the circle
                    generate_Points(num_circlePoints);
                    //drawing lines with the points
                    draw_line();
                    startTime = now;
                    times_number++;
                }
            }
        };
        button1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                timer.start();
            }
        });
        button2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                timer.stop();
            }
        });
        button3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                times_number = Integer.parseInt(text1.getText());
                num_circlePoints = Integer.parseInt(text2.getText());
                draw_line();;
                timer.stop();
            }
        });
        button4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                favorite.getValue();
                switch (favorite.getValue()) {
                    case "25 = 360":
                        times_number=25;
                        num_circlePoints=360;
                        break;
                    case "170 = 360":
                        times_number=170;
                        num_circlePoints=360;
                        break;
                    case "358 = 360":
                        times_number=358;
                        num_circlePoints=360;
                        break;
                    case "177 = 360":
                        times_number=177;
                        num_circlePoints=360;
                        break;
                    case "359 = 360":
                        times_number=359;
                        num_circlePoints=360;
                }
                draw_line();
                timer.stop();
            }
        });
    }
}