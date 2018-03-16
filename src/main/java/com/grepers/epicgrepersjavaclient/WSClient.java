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
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Actor mainActor = null;
    private Scene scene = null;
    private static final double M_TO_PX = 10;

    public double toScreenX(double m) {
        return (m - mainActor.getPos().x) * M_TO_PX + scene.getWidth() / 2d;
    }
    public double toScreenY(double m) {
        return - (m - mainActor.getPos().y) * M_TO_PX + scene.getHeight() / 2d;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = URI.create("ws://localhost:8080/client");
        container.connectToServer(this, uri);
        scene = primaryStage.getScene();
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("open");
        MessageFromClient messageFromClient = new MessageFromClient();
        messageFromClient.setFiring(true);
        messageFromClient.setRot(Math.random() % Math.PI);
        messageFromClient.setVel(new Point2D(Math.random() % 1, Math.random() % 1));
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(messageFromClient));
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
                Image image = new Image(getClass().getResource("/"+actor.getType()+".png").toExternalForm());
                ImageView view = new ImageView(image);
                view.setRotate(-Math.toDegrees(actor.getRot()));
                view.setX(toScreenX(actor.getPos().x) - image.getWidth() / 2d);
                view.setY(toScreenY(actor.getPos().y)  - image.getHeight() / 2d);
                root.getChildren().add(view);
            });
        });
    }

    @OnClose
    public void onClose() {
        System.out.println("close");
    }

}

