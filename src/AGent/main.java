package AGent;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;
import static java.lang.StrictMath.*;
import static java.lang.StrictMath.toRadians;

public class main extends Application {
    boolean counter =false;
    int flag=0;
    int circlepoints=360;
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(firstScreen(), 200, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Group imageCode() throws FileNotFoundException {
        // image import from favouriteiamges.
        Image image1 = new Image(new FileInputStream("src\\sample\\FavouriteImages\\1.jpg"));
        Image image2 = new Image(new FileInputStream("src\\sample\\FavouriteImages\\2.jpg"));
        Image image3 = new Image(new FileInputStream("src\\sample\\FavouriteImages\\3.jpg"));
        Image image4 = new Image(new FileInputStream("src\\sample\\FavouriteImages\\4.jpg"));
        Image image5 = new Image(new FileInputStream("src\\sample\\FavouriteImages\\5.jpg"));
        Image image6 = new Image(new FileInputStream("src\\sample\\FavouriteImages\\6.jpg"));

        // ImageView to add image.
        ImageView imageView1= new ImageView(image1);
        ImageView imageView2 = new ImageView(image2);
        ImageView imageView3 = new ImageView(image3);
        ImageView imageView4  = new ImageView(image4);
        ImageView imageView5 = new ImageView(image5);
        ImageView imageView6 = new ImageView(image6);

        // to set the coordinate of the imageview
        imageView2.setX(250);
        imageView3.setY(102);
        imageView4.setX(250);
        imageView4.setY(102);
        imageView5.setY(202);
        imageView6.setX(250);
        imageView6.setY(202);

        // created a Group  to add  all thge imageview.
        Group root = new Group(imageView1,  imageView2, imageView3, imageView4, imageView5, imageView6);

        return root;
    }

    public AnchorPane firstScreen()
    {
        // VBox to add button label and textfield.
        VBox Space = new VBox(15);
        HBox row1 = new HBox(15);
        HBox row2 = new HBox(15);
        HBox row3 = new HBox(15);
        HBox row4 = new HBox(15);
        HBox row5= new HBox(15);

        //Created a Button.
        Button startBtn = new Button("Start");
        Button stopBtn = new Button("Stop");
        Button enterBtn= new Button("Enter");
        Button restaBtn = new Button("Restart");
        final Button favBtn = new Button("Favourite Images");

        //Created a label  for text.
        Label text1 = new Label("TimesCircle:");
        Label text2= new Label("Points:");
        Label text3 = new Label("SlideToIncrease:");
        Label text4= new Label("SlidetoIncreasePoint");

        // created a textfield to take input of the text.
        TextField box1 = new TextField();
        TextField box2 = new TextField();

        // two slider for the time and point control.
        Slider slide1 = new Slider(0,100,10);
        Slider slide2= new Slider(0,360,5);

        // Hbox( row) ahh the textfield , button and the slider added.
        row1.getChildren().addAll( startBtn,text1,box1,text2,box2);
        row2.getChildren().addAll(stopBtn,text3,slide1);
        row3.getChildren().addAll(enterBtn,text4,slide2);
        row4.getChildren().addAll(restaBtn);
        row5.getChildren().addAll(favBtn);

        Space.getChildren().addAll(row1,row2,row3,row4,row5);
        // created a Anchorpane.
        AnchorPane root = new AnchorPane();
        AnchorPane root1 = new AnchorPane();
        AnchorPane rootContainer = new AnchorPane(Space,root,root1);
        root.setMouseTransparent(true);
        root1.setMouseTransparent(true);

        // created a timer for  runtime.
        AnimationTimer timer = new AnimationTimer() {
            private long startTime = -1;
            int circleMulty =2;
            @Override
            public void handle(long now) {
                // start button action.
                startBtn.setOnAction(value -> counter = true);
                // stop button action.
                stopBtn.setOnAction(value -> counter = false);
                // restart button function.
                restaBtn.setOnAction(e -> {
                    circleMulty = 2;
                    counter = true;
                    circlepoints = 360;
                });
                // enterBtn wotking. settign the cirlepoint and
                // cirlceMultiply to textfield and setting  the slider to text.
                enterBtn.setOnAction(e -> {
                    counter = true;
                    circlepoints = 360;
                    circleMulty = Integer.parseInt(box1.getText());
                    circlepoints = Integer.parseInt(box2.getText());
                    slide2.setValue(Integer.parseInt(box2.getText()));
                });

                // working of the slidder ( slide2) to circle point.
                int l=5;
                if(l!=(int)slide2.getValue()){
                    circlepoints=(int) slide2.getValue();
                }
                //  favbtn wokting
                // loading screenTwo on action
                favBtn.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent arg0) {
                        try {
                            favBtn.getScene().setRoot(screenTwo());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
                // timer slider condition.

                if (counter ) {
                    if ((now - startTime) + slide1.getValue() * 10_000_000 > 1_000_000_000) {

                        // color array.
                        Color[] ballColors = {Color.BLUE, Color.RED, Color.GREEN, Color.PURPLE, Color.BLACK,
                                Color.GOLD, Color.GREEN, Color.DARKBLUE, Color.ANTIQUEWHITE, Color.ORANGE, Color.YELLOW};
                        Random rd = new Random();
                        int rd_int = rd.nextInt(10) + 1;
                        // Clearing the root and root1.
                        root.getChildren().clear();
                        root1.getChildren().clear();
                        // creating array for x and y points on circle.
                        double[] xPointArray = new double[circlepoints];
                        double[] yPointArray = new double[circlepoints];
                        Circle pt = null;
                        // finding the distance of the point in circle.
                        for (int a = 0; a < circlepoints; a++) {
                            xPointArray[a]=cos(toRadians(360*a/circlepoints)) * 300 +600f;
                            yPointArray[a] =sin(toRadians(360*a/circlepoints)) * 300+350f;
                            pt = new Circle(xPointArray[a],
                                    yPointArray[a], 4.0f);
                            pt.setFill(Color.BLUE);
                            root1.getChildren().add(pt);
                        }
                        //finding the other half of the line in circle.
                        int EndCord = 0;
                        for (int i = 0; i < circlepoints; i++) {
                            EndCord = circleMulty * i;
                            while (EndCord > circlepoints - 1) {
                                EndCord = EndCord - circlepoints ;
                            }
                            // setting the line points for begining and end.

                            Line line = new Line();
                            line.setStartX(xPointArray[i]);
                            line.setStartY(yPointArray[i]);
                            line.setEndX(xPointArray[EndCord]);
                            line.setEndY(yPointArray[EndCord]);
                            line.setStroke(ballColors[rd_int]);
                            root.getChildren().addAll(line);
                        }

                        startTime = now;
                        circleMulty++;
                    }

                }
            }
        };timer.start();
        return rootContainer;
    }
    // method screenTwo for the back button to load screenOne.
    public VBox screenTwo() throws FileNotFoundException {
        VBox vBox = new VBox();
        final Button button = new Button("Back");
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                button.getScene().setRoot(firstScreen());
            }

        });

        vBox.getChildren().addAll( button,imageCode());
        return vBox;
    }

}
