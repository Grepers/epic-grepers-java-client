package com.grepers.epicgrepersjavaclient;

import javafx.application.Application;
import javafx.stage.Stage;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WSClient extends Application {

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
        System.out.println("message: " + input);
    }

    @OnClose
    public void onClose() {
        System.out.println("close");
    }

}

