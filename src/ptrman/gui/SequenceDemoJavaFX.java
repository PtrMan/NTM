package gui;

import ntm.NeuralTuringMachine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ntm.run.RunSequenceLearner;
import ntm.run.SequenceLearner;
import ntm.run.TrainingSequence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by me on 7/5/15.
 */
public class SequenceDemoJavaFX extends Application implements Runnable {

    int vectorSize = 6;
    private SequenceLearner sl;
    int inputs, outputs;

    final int dataWindow = 50;
    int dataWidth;
    double[][] data = null;

    public static void main(String[] args) { launch(args);    }


    int sx, sy;
    float[][] heights;

    public final int updateMS = 75;
    //double t = 0;




    public void init(int sx, int sy) {
        this.sx = sx;
        this.sy = sy;
        heights = new float[sx][sy];
    }

    public void run() {

        final float[][] h = this.heights;

        for (int i = 0; i < sx; i++) {
            for (int j = 0; j < sy; j++) {
                //Transcedental Gradient
                //double xterm = (Math.cos(t + Math.PI * i / size) * Math.cos(Math.PI * i / size));
                //double yterm = (Math.cos(t + Math.PI * j / size) * Math.cos(Math.PI * j / size));
                //arrayY[i + size][j + size] = (float) (10 * ((xterm + yterm) * (xterm + yterm)));

                h[i][j] = (float) get(i, j);
            }
        }
        //histogram.setHeightData(heights, 1, 2, Color.SKYBLUE, false, true);

    }


    public final ExecutorService exe = Executors.newSingleThreadExecutor();


    public void queue(Runnable r) {
        exe.submit(r);
    }

    public void nextLoop() {
        queue(loop);
    }

    /** call when histogram is ready with changed data to display it */
    public void commit() {
        Platform.runLater(SequenceDemoJavaFX.this);


        try {
            Thread.sleep(updateMS);
        } catch (InterruptedException e) {

        }
    }

    Runnable loop = (() -> {
        try {

            cycle();

            commit();

            nextLoop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    });


    @Override
    public void start(Stage stage) throws Exception {

        PerspectiveCamera camera = new PerspectiveCamera(true);
        //CameraTransformer cameraTransform = new CameraTransformer();

        final double sceneWidth = 800;
        final double sceneHeight = 600;
        double cameraDistance = 5000;


        final Group sceneRoot = new Group();
        SubScene scene = new SubScene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.DISABLED);
        scene.setCacheHint(CacheHint.SPEED);
        scene.setFill(Color.BLACK);
        //setup camera transform for rotational support
        ///cameraTransform.setTranslate(0, 0, 0);
        //cameraTransform.getChildren().add(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-30);
//        cameraTransform.ry.setAngle(-45.0);
//        cameraTransform.rx.setAngle(-10.0);
        //add a Point Light for better viewing of the grid coordinate system
        PointLight light = new PointLight(Color.WHITE);
        //cameraTransform.getChildren().add(light);
        light.setTranslateX(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateY());
        light.setTranslateZ(10 * camera.getTranslateZ());
        scene.setCamera(camera);

        //sceneRoot.getChildren().addAll(histogram);

        nextLoop();

        exe.submit(() -> {
            try {
                exe.awaitTermination(10000, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

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

//        scene.setOnMousePressed((MouseEvent me) -> {
//            mousePosX = me.getSceneX();
//            mousePosY = me.getSceneY();
//            mouseOldX = me.getSceneX();
//            mouseOldY = me.getSceneY();
//        });
//        scene.setOnMouseDragged((MouseEvent me) -> {
//            mouseOldX = mousePosX;
//            mouseOldY = mousePosY;
//            mousePosX = me.getSceneX();
//            mousePosY = me.getSceneY();
//            mouseDeltaX = (mousePosX - mouseOldX);
//            mouseDeltaY = (mousePosY - mouseOldY);
//
//            double modifier = 10.0;
//            double modifierFactor = 0.1;
//
//            if (me.isControlDown()) {
//                modifier = 0.1;
//            }
//            if (me.isShiftDown()) {
//                modifier = 50.0;
//            }
//            if (me.isPrimaryButtonDown()) {
//                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // +
//                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // -
//            } else if (me.isSecondaryButtonDown()) {
//                double z = camera.getTranslateZ();
//                double newZ = z + mouseDeltaX * modifierFactor * modifier;
//                camera.setTranslateZ(newZ);
//            } else if (me.isMiddleButtonDown()) {
//                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
//                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
//            }
//        });

        StackPane sp = new StackPane();
        sp.setPrefSize(sceneWidth, sceneHeight);
        sp.setMaxSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setMinSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sp.setBackground(Background.EMPTY);
        sp.getChildren().add(scene);
        sp.setPickOnBounds(false);

        scene.widthProperty().bind(sp.widthProperty());
        scene.heightProperty().bind(sp.heightProperty());

        Scene S = new Scene(sceneRoot);
        stage.setScene(S);
        stage.show();

        //return sp;
    }

    public void cycle() {

        sl.run();

    }

    @Override
    public void init() {



        sl = new RunSequenceLearner(vectorSize) {

            @Override
            public void onTrained(int sequenceNum, TrainingSequence sequence, NeuralTuringMachine[] output, long trainTimeNS, double avgError) {

                double[][] inputs = sequence.input;
                double[][] ideals = sequence.ideal;
                int slen = ideals.length;

                /*for (int t = 0; t < slen; t++) {
                    double[] actual = output[t].getOutput();
                    System.out.println("\t" + sequenceNum + "#" + t + ":\t" + toNiceString(ideals[t]) + " =?= " + toNiceString(actual));
                }*/

                for (int t = 0; t < slen; t++) {
                    //pop(data);

                    final int tt = t;

                    queue(() -> {

                        double[] input = inputs[tt];
                        double[] ideal = ideals[tt];
                        double[] actual = output[tt].getOutput();

                        pushLast(input, 0);
                        pushLast(ideal, input.length + 1);
                        pushLast(actual, input.length+1+ideal.length+1);

                        commit();

                    });
                }


            }
        };

        inputs = sl.machine.inputSize();
        outputs = sl.machine.outputSize();

        dataWidth = inputs + 1 + outputs + 1 + outputs;

        data = new double[dataWindow][dataWidth];

        init(dataWindow, dataWidth);

    }

    int r = 0;
    public void pushLast(double[] p, int index) {
        for (double x : p) {
            data[r][index++] = x;
        }

        if (++r == data.length) r = 0; //wrap
    }

    public double get(int i, int j) {
        return data[i][j];
        //return -Math.random()*i;
    }
}
