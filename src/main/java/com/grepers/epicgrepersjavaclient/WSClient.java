package com.grepers.epicgrepersjavaclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grepers.epicgrepersjavaclient.dto.MessageToClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.MessageHandler;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = URI.create("ws://localhost:8080/client");
        container.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("open");
        session.getBasicRemote().sendText("chachinga");
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
                Image image = new Image(getClass().getResource("/"+actor.getType()+".png").toExternalForm());
                ImageView view = new ImageView(image);
                view.setRotate(Math.toDegrees(actor.getRot()));
                view.setX(actor.getPos().x);
                view.setY(actor.getPos().y);
                root.getChildren().add(view);
            });
        });
    }

    @OnClose
    public void onClose() {
        System.out.println("close");
    }

}

