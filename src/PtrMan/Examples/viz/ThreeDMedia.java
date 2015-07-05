package Examples.viz;

import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;


// Display a rotating 3D box with a video projected onto its surface.
public class ThreeDMedia extends Application {

    private static final String MEDIA_URL =
            "http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv";

    private static final int SCENE_W = 640;
    private static final int SCENE_H = 400;


    private static final double MEDIA_W = 540 * 2/3;
    private static final double MEDIA_H = 209 * 2/3;

    private static final Color INDIA_INK = Color.rgb(35, 39, 50);

    @Override
    public void start(Stage stage) {
        // create a 3D box shape on which to project the video.
        Box box = new Box(MEDIA_W, MEDIA_H, MEDIA_W);
        box.setTranslateX(SCENE_W / 2);
        box.setTranslateY(SCENE_H / 2);

        // create a media player for the video which loops the video forever.
        MediaPlayer player = new MediaPlayer(new Media(MEDIA_URL));
        player.setCycleCount(MediaPlayer.INDEFINITE);

        // create a media view for the video, sized to our specifications.
        MediaView mediaView = new MediaView(player);
        mediaView.setPreserveRatio(false);
        mediaView.setFitWidth(MEDIA_W);
        mediaView.setFitHeight(MEDIA_H);

        // project the video on to the 3D box.
        //showMediaOnShape3D(box, mediaView);

        // rotate the box.
        rotateAroundYAxis(box);

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
        stage.setResizable(false);

        // start playing the media, showing the scene once the media is ready to play.
        //player.setOnReady(stage::show);
        //player.setOnError(Platform::exit);
        //player.play();

        stage.show();
    }

       // Project video on to 3D shape.
    private void showMediaOnShape3D(Shape3D shape3D, final MediaView mediaView) {
        PhongMaterial material = new PhongMaterial();
        shape3D.setMaterial(material);

        Scene mediaScene = new Scene(
                new Group(mediaView),
                MEDIA_W, MEDIA_H
        );
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setViewport(
                new Rectangle2D(
                        0, 0, MEDIA_W, MEDIA_H
                )
        );
        WritableImage textureImage = mediaView.snapshot(
                snapshotParameters,
                null
        );
        material.setDiffuseMap(textureImage);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                mediaView.snapshot(
                        snapshotParameters,
                        textureImage
                );
            }
        };
        timer.start();
    }

    // Rotates a shape around the y axis indefinitely.
    private void rotateAroundYAxis(Shape3D shape3D) {
        RotateTransition rotateY = new RotateTransition(
                Duration.seconds(10),
                shape3D
        );

        rotateY.setAxis(Rotate.Y_AXIS);
        rotateY.setFromAngle(360);
        rotateY.setToAngle(0);
        rotateY.setCycleCount(RotateTransition.INDEFINITE);
        rotateY.setInterpolator(Interpolator.LINEAR);

        rotateY.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}