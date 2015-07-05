/**
 * Histograms.java
 * <p>
 * Copyright (c) 2013-2015, F(X)yz
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the organization nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package Examples.viz;

import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.fxyz.samples.FXyzSample;
import org.fxyz.shapes.composites.Histogram;
import org.fxyz.utils.CameraTransformer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Sean
 */
abstract public class Histograms extends FXyzSample implements Runnable {
    int sx, sy;
    float[][] heights;
    Histogram histogram;
    public final int updateMS = 75;
    //double t = 0;

    public Histograms() {
        super();
        init();
    }

    abstract public void init();


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
        histogram.setHeightData(heights, 1, 2, Color.SKYBLUE, false, true);

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
        Platform.runLater(Histograms.this);


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


    public abstract void cycle();

    public abstract double get(int i, int j);

    @Override
    public Node getSample(Stage stage) {



        PerspectiveCamera camera = new PerspectiveCamera(true);
        CameraTransformer cameraTransform = new CameraTransformer();

        final double sceneWidth = 800;
        final double sceneHeight = 600;
        double cameraDistance = 5000;


        final Group sceneRoot = new Group();
        SubScene scene = new SubScene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.DISABLED);
        scene.setCacheHint(CacheHint.SPEED);
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
        PointLight light = new PointLight(Color.WHITE);
        cameraTransform.getChildren().add(light);
        light.setTranslateX(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateY());
        light.setTranslateZ(10 * camera.getTranslateZ());
        scene.setCamera(camera);

        histogram = new Histogram(1000, 1, true);
        sceneRoot.getChildren().addAll(histogram);

        nextLoop();

        getServiceExecutor().submit(() -> {
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
        sp.getChildren().add(scene);
        sp.setPickOnBounds(false);

        scene.widthProperty().bind(sp.widthProperty());
        scene.heightProperty().bind(sp.heightProperty());

        return sp;
    }

    @Override
    public Node getPanel(Stage stage) {
        return getSample(stage);
    }

    @Override
    public String getJavaDocURL() {
        return null;
    }

    @Override
    protected Node buildControlPanel() {
        return null;
    }


}
