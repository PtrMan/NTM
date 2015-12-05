package gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.stage.Stage;
import ntm.NeuralTuringMachine;
import ntm.memory.address.Head;
import ntm.run.SequenceLearner;
import ntm.run.TrainingSequence;

import java.util.ArrayDeque;

/**
 * Created by me on 7/5/15.
 */

// Display a rotating 3D box with a video projected onto its surface.
public class SequenceDemoTextureJavaFX extends Application {

    int vectorSize = 16;
    private SequenceLearner sl;
    int inputs, outputs;

    final int dataWindow = 200;
    int dataWidth;
    double[][] data = null;
    double mousePosX, mousePosY, mouseOldX, mouseOldY, mouseDeltaX, mouseDeltaY;


    public final ArrayDeque<Runnable> queue = new ArrayDeque<>(dataWindow * 2);
    private WritableImage textureImage;


    public void cycle() {

        try {
            if (queue.isEmpty())
                sl.run();
            else {
                Runnable r = queue.pollFirst();
                r.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    int margin = 1;

    final ColorMatrix cm = new ColorMatrix(16,16,(x,y) -> {
        return Color.hsb(180 * x, 0.85f, 0.85f * y);
    });

    public void commit() {
        PixelWriter wr = textureImage.getPixelWriter();

        int m = margin;
        double[][] d = this.data;

        for (int i = 0; i < dataWidth; i++) {

            double[] di = d[i];
            for (int j = 0; j < dataWindow; j++) {
                double v = di[j];

                Color c = cm.get((float)i/dataWindow, v);
                //Color c = Color.gray(v);
                wr.setColor(j + m, i + m, c);

            }
        }


    }

    @Override
    public void init() {
        // must be the same as for NTM
        int memoryWith = 8;

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


                    double[] input = inputs[tt];
                    double[] ideal = ideals[tt];
                    double[] actual = output[tt].getOutput();
                    Head head = output[tt].getHeads()[0];

                    pushLast(input, 0);
                    pushLast(ideal, input.length + 1);
                    pushLast(actual, input.length + 1 + ideal.length + 1);

                    for( int headDataIndex = 0; headDataIndex < memoryWith*2; headDataIndex++ ) {
                        double headValue = head.get(headDataIndex).getValue() * 0.5f/*normalisation, arbitarly */;
                        pushLast(new double[]{headValue}, input.length + 1 + ideal.length + 1 + actual.length + headDataIndex);
                    }

                }

                queue.add(() ->
                                commit()
                );


            }
        };

        inputs = sl.machine.inputSize();
        outputs = sl.machine.outputSize();

        // for now we are just interested in the two upper head regions with the size of memoryWidth
        int headsSum = memoryWith*2;
        dataWidth = inputs + 1 + outputs + 1 + outputs + headsSum;

        data = new double[dataWidth][dataWindow];


        textureImage = new WritableImage(dataWindow + margin * 2, dataWidth + margin * 2);

    }



    public void pushLast(double[] p, int index) {

        for (double x : p) {
            double[] d = data[index++];
            System.arraycopy(d, 0, d, 1, d.length-1);
            d[0] = x;
        }

    }


    private static final int SCENE_W = 800;
    private static final int SCENE_H = 600;


    private static final double MEDIA_W = 540 * 2 / 3;
    private static final double MEDIA_H = 209 * 2 / 3;

    private static final Color INDIA_INK = Color.rgb(35, 39, 50);

    @Override
    public void start(Stage stage) {

        init();

        float scale = 16f;
        // create a 3D box shape on which to project the video.
        Shape3D box = new Box(dataWindow * scale, dataWidth * scale, 1);
        box.setTranslateX(SCENE_W / 2);
        box.setTranslateY(SCENE_H / 2);

        // project the video on to the 3D box.
        PhongMaterial material = new PhongMaterial();
        box.setMaterial(material);
        //box.setCullFace(CullFace.BACK);
        material.setDiffuseMap(textureImage);


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                cycle();

            }
        };
        timer.start();


        // create a point light source a fair way away so lighting is reasonably even.
        PointLight pointLight = new PointLight(
                Color.WHITE
        );
        pointLight.setTranslateX(SCENE_W / 2);
        pointLight.setTranslateY(SCENE_H / 2);
        pointLight.setTranslateZ(-SCENE_W * 5);

        // add a bit of ambient light to make the lighting more natural.
        AmbientLight ambientLight = new AmbientLight(
                Color.rgb(15, 15, 15)
        );

        // place the shape and associated lights in a group.
        Group group = new Group(
                box,
                pointLight,
                ambientLight
        );

        // create a 3D scene with a default perspective camera.
        Scene scene = new Scene(
                group,
                SCENE_W, SCENE_H, true, SceneAntialiasing.BALANCED
        );
        scene.setFill(INDIA_INK);
        PerspectiveCamera camera = new PerspectiveCamera();

        scene.setCamera(camera);

        stage.setScene(scene);

        // start playing the media, showing the scene once the media is ready to play.
        //player.setOnReady(stage::show);
        //player.setOnError(Platform::exit);
        //player.play();


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
            double modifierFactor = 0.5;

            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isMiddleButtonDown()) {
                double r = camera.getRotate();
                r = (((r + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // +
                camera.setRotate(r);
                //cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // -
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isPrimaryButtonDown()) {
                camera.setTranslateX(camera.getTranslateX() + mouseDeltaX * modifierFactor * modifier * -1);
                camera.setTranslateY(camera.getTranslateY() + mouseDeltaY * modifierFactor * modifier * -1);
                //cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                //cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
            }
        });

        stage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}