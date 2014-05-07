import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import static javafx.scene.input.MouseEvent.*;

import rx.Observable;
import rx.Subscriber;
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

        Observable<Boolean> merge = Observable.merge(leftButtonDown(canvas), leftButtonUp(canvas));

        merge.subscribe(System.out::println);

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

    Observable<javafx.scene.input.MouseEvent> mouseMoves(Canvas canvas) {
        return Observable.create((Subscriber<? super MouseEvent> subscriber) -> {
            EventHandler<MouseEvent> handler = subscriber::onNext;
            canvas.addEventHandler(MOUSE_MOVED, handler);
            subscriber.add(Subscriptions.create(() -> canvas.removeEventHandler(MOUSE_MOVED, handler)));
        });
    }

    Observable<javafx.scene.input.MouseEvent> mouseDrags(Canvas canvas) {
        return Observable.create((Subscriber<? super MouseEvent> subscriber) -> {
            EventHandler<MouseEvent> handler = subscriber::onNext;
            canvas.addEventHandler(MOUSE_DRAGGED, handler);
            subscriber.add(Subscriptions.create(() -> canvas.removeEventHandler(MOUSE_DRAGGED, handler)));
        });
    }
}
