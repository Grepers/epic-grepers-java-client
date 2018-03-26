package com.grepers.epicgrepersjavaclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grepers.epicgrepersjavaclient.dto.Actor;
import com.grepers.epicgrepersjavaclient.dto.MessageFromClient;
import com.grepers.epicgrepersjavaclient.dto.MessageToClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

@RequiredArgsConstructor
@ClientEndpoint
public class WSClient extends Application {

    private final Group root;
    private final ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Actor mainActor = null;
    private Scene scene = null;
    private static final double M_TO_PX = 50;

    private Session session;

    private boolean firing = false;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private Point2D vel = Point2D.ZERO;
    private double rot = 0d;

    double toScreenX(double m) {
        return (m - mainActor.getPos().x) * M_TO_PX + scene.getWidth() / 2d;
    }

    double toScreenY(double m) {
        return -(m - mainActor.getPos().y) * M_TO_PX + scene.getHeight() / 2d;
    }

    void sendActionUpdate() throws IOException {
        MessageFromClient messageFromClient = new MessageFromClient();
        messageFromClient.setFiring(firing);
        messageFromClient.setRot(rot);
        messageFromClient.setVel(vel);
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(messageFromClient));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = URI.create("ws://localhost:8080/client");
        container.connectToServer(this, uri);
        scene = primaryStage.getScene();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.W)
                upPressed = true;
            else if (event.getCode() == KeyCode.S)
                downPressed = true;
            else if (event.getCode() == KeyCode.D)
                rightPressed = true;
            else if (event.getCode() == KeyCode.A)
                leftPressed = true;
            else if (event.getCode() == KeyCode.SPACE)
                firing = true;
            vel = new Point2D(rightPressed ? 1d : leftPressed ? -1d : 0d, upPressed ? 1d : downPressed ? -1d : 0d);
        });
        scene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.W)
                upPressed = false;
            else if (event.getCode() == KeyCode.S)
                downPressed = false;
            else if (event.getCode() == KeyCode.D)
                rightPressed = false;
            else if (event.getCode() == KeyCode.A)
                leftPressed = false;
            else if (event.getCode() == KeyCode.SPACE)
                firing = false;
            vel = new Point2D(rightPressed ? 1d : leftPressed ? -1d : 0d, upPressed ? 1d : downPressed ? -1d : 0d);
        });
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            double x = event.getX() - scene.getWidth() / 2;
            double y = event.getY() - scene.getHeight() / 2;
            rot = -Math.atan2(y, x);
        });
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            firing = true;
        });
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            firing = false;
        });    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("open");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String input) {
        MessageToClient messageToClient;
        try {
            messageToClient = objectMapper.readValue(input, MessageToClient.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Platform.runLater(() -> {
            root.getChildren().clear();
            messageToClient.getActors().forEach(actor -> {
                if (mainActor == null || mainActor.getId().equals(actor.getId()))
                    mainActor = actor;
                Image image = new Image(getClass().getResource("/" + actor.getType() + ".png").toExternalForm());
                ImageView view = new ImageView(image);
                if(actor.getRadius() != null) {
                    view.setFitWidth(actor.getRadius() * 2d * M_TO_PX);
                    view.setFitHeight(actor.getRadius() * 2d * M_TO_PX);
                } else if(actor.getWidth() != null && actor.getDepth() != null) {
                    view.setFitWidth(actor.getWidth() * M_TO_PX);
                    view.setFitHeight(actor.getDepth() * M_TO_PX);
                }
                view.setRotate(-Math.toDegrees(actor.getRot()));
                view.setX(toScreenX(actor.getPos().x) - view.getFitWidth() / 2d);
                view.setY(toScreenY(actor.getPos().y) - view.getFitHeight() / 2d);
                root.getChildren().add(view);
            });
        });
        try {
            sendActionUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OnClose
    public void onClose() {
        System.out.println("close");
    }

}

