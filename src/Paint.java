import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import static javafx.scene.input.MouseEvent.*;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

public class Paint extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Integer screenWidth = 800;
        Integer screenHeight = 600;

        Canvas canvas = new Canvas(screenHeight, screenWidth);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Group root = new Group();
        root.getChildren().add(canvas);

        Observable<Boolean> leftButtonUp = leftButtonUp(canvas);
        Observable<Boolean>  leftButtonDown = leftButtonDown(canvas);

        leftButtonUp.subscribe(up -> System.out.println("up: "+up));
        leftButtonDown.subscribe(down -> System.out.println("down: "+down));


        Observable<MouseEvent> mouseMoves = mouseMoves(canvas);
        Observable<MouseEvent>  mouseDrags = mouseDrags(canvas);

        Observable<MouseEvent>  mouse = Observable.merge(mouseMoves, mouseDrags);

        Observable<Point2D[]> mouseDiffs =
                mouse
                .buffer(2, 1)
                .map(buffer -> new Point2D[]{
                    new Point2D(buffer.get(0).getX(), buffer.get(0).getY()),
                    new Point2D(buffer.get(1).getX(), buffer.get(1).getY())
                });

        Observable<Point2D[]> paint =
                mouseDiffs
                .window(leftButtonDown, (Boolean b) -> leftButtonUp)
                .flatMap(x -> x);

        paint.subscribe(diff -> {
            gc.strokeLine(diff[0].getX(), diff[0].getY(),diff[1].getX(), diff[1].getY());
        });

        stage.setTitle("Rx Paint");
        stage.setScene(new Scene(root));
        stage.show();
    }

    Observable<Boolean> leftButtonDown(Canvas canvas) {
        return Observable.create((Subscriber<? super Boolean> subscriber) -> {
            EventHandler<MouseEvent> handler = mouseEvent -> subscriber.onNext(true);
            canvas.addEventHandler(MOUSE_PRESSED, handler);
            subscriber.add(Subscriptions.create(() -> canvas.removeEventHandler(MOUSE_PRESSED, handler)));
        });
    }

    Observable<Boolean> leftButtonUp(Canvas canvas) {
        return Observable.create((Subscriber<? super Boolean> subscriber) -> {
            EventHandler<MouseEvent> handler = mouseEvent -> subscriber.onNext(false);
            canvas.addEventHandler(MOUSE_RELEASED, handler);
            subscriber.add(Subscriptions.create(() -> canvas.removeEventHandler(MOUSE_RELEASED, handler)));
        });
    }

    Observable<MouseEvent> mouseMoves(Canvas canvas) {
        return Observable.create((Subscriber<? super MouseEvent> subscriber) -> {
            EventHandler<MouseEvent> handler = subscriber::onNext;
            canvas.addEventHandler(MOUSE_MOVED, handler);
            subscriber.add(Subscriptions.create(() -> canvas.removeEventHandler(MOUSE_MOVED, handler)));
        });
    }

    Observable<MouseEvent> mouseDrags(Canvas canvas) {
        return Observable.create((Subscriber<? super MouseEvent> subscriber) -> {
            EventHandler<MouseEvent> handler = subscriber::onNext;
            canvas.addEventHandler(MOUSE_DRAGGED, handler);
            subscriber.add(Subscriptions.create(() -> canvas.removeEventHandler(MOUSE_DRAGGED, handler)));
        });
    }
}
