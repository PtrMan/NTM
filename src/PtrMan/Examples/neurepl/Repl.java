package neurepl;

import Examples.SequenceLearner;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxyz.samples.FXyzSample;
import org.fxyz.shapes.composites.Histogram;
import org.fxyz.utils.CameraTransformer;

import java.util.ArrayDeque;

/**
 * Created by me on 7/5/15.
 */

// Display a rotating 3D box with a video projected onto its surface.
public class Repl extends FXyzSample {

    private final Node textArea;
    int vectorSize = 20;
    private SequenceLearner sl;
    int inputs, outputs;

    final int dataWindow = 200;
    int dataWidth;
    double[][] data = null;
    double mousePosX, mousePosY, mouseOldX, mouseOldY, mouseDeltaX, mouseDeltaY;


    public final ArrayDeque<Runnable> queue = new ArrayDeque<>(dataWindow * 2);
    private WritableImage textureImage;



    private static final int SCENE_W = 800;
    private static final int SCENE_H = 600;


    private static final double MEDIA_W = 540 * 2 / 3;
    private static final double MEDIA_H = 209 * 2 / 3;

    private static final Color INDIA_INK = Color.rgb(35, 39, 50);

    public Repl() {
        super();
        this.textArea = getOverGUI();
    }

    @Override
    public Node getSample(Stage stage) {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        CameraTransformer cameraTransform = new CameraTransformer();
        final double sceneWidth = 800;
        final double sceneHeight = 600;
        double cameraDistance = 5000;
        Histogram histogram;

        Group sceneRoot = new Group();
        SubScene scene = new SubScene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);
        //setup camera transform for rotational support
        cameraTransform.setTranslate(0, 0, 0);
        cameraTransform.getChildren().add(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-30);
//        cameraTransform.ry.setAngle(-45.0);
//        cameraTransform.rx.setAngle(-10.0);
        //add a Point Light for better viewing of the grid coordinate system
        PointLight light = new PointLight(Color.GRAY);
        cameraTransform.getChildren().add(light);
        light.setTranslateX(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateY());
        light.setTranslateZ(10 * camera.getTranslateZ());
        scene.setCamera(camera);

        histogram = new Histogram(1000, 1, true);
        sceneRoot.getChildren().addAll(histogram);

        int size = 30;
        float[][] arrayY = new float[2 * size][2 * size];
        for (int i = -size; i < size; i++) {
            for (int j = -size; j < size; j++) {
                //Transcedental Gradient
                double xterm = (Math.cos(Math.PI * i / size) * Math.cos(Math.PI * i / size));
                double yterm = (Math.cos(Math.PI * j / size) * Math.cos(Math.PI * j / size));
                arrayY[i + size][j + size] = (float) (10 * ((xterm + yterm) * (xterm + yterm)));
            }
        }
        histogram.setHeightData(arrayY, 1, 4, Color.SKYBLUE, false, true);

        //First person shooter keyboard movement
        scene.setOnKeyPressed(event -> {
            double change = 10.0;
            //Add shift modifier to simulate "Running Speed"
            if (event.isShiftDown()) {
                change = 50.0;
            }
            //What key did the user press?
            KeyCode keycode = event.getCode();
            //Step 2c: Add Zoom controls
            if (keycode == KeyCode.W) {
                camera.setTranslateZ(camera.getTranslateZ() + change);
            }
            if (keycode == KeyCode.S) {
                camera.setTranslateZ(camera.getTranslateZ() - change);
            }
            //Step 2d:  Add Strafe controls
            if (keycode == KeyCode.A) {
                camera.setTranslateX(camera.getTranslateX() - change);
            }
            if (keycode == KeyCode.D) {
                camera.setTranslateX(camera.getTranslateX() + change);
            }
        });

        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 10.0;
            double modifierFactor = 0.1;

            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isPrimaryButtonDown()) {
                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // +
                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // -
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
            }
        });

        StackPane sp = new StackPane();
        sp.setPrefSize(sceneWidth, sceneHeight);
        sp.setMaxSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setMinSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setBackground(Background.EMPTY);

        sp.getChildren().add(textArea);
        sp.getChildren().add(scene);

        scene.setBlendMode(BlendMode.ADD);

        scene.setPickOnBounds(false);

        scene.widthProperty().bind(sp.widthProperty());
        scene.heightProperty().bind(sp.heightProperty());

        //stage.initStyle(StageStyle.TRANSPARENT);

        return (sp);
    }

    @Override
    protected Node buildControlPanel() {
        return null;
    }



    private Node getOverGUI() {

        TextArea textArea = new TextArea("repl < >SDFSF>SDFSDF>ASDFDF\n\n\nMOREREOL");

        textArea.setStyle("-fx-background-color: black; -fx-base: rgba(255,255,255,0); -fx-font: 27px \"Monospace\"; -fx-text-fill: white;");


        //textPane.setBackground(new java.awt.Color(0,0,0,0)); // 0 opacity => 100% transparent!

        //textArea.setForeground(java.awt.Color.WHITE);

        //textArea.setCaretColor(java.awt.Color.WHITE);


        //textArea.setOpacity(0.5f);

        return textArea;
    }


    public static void main(String[] args) {
        launch(args);
    }
}