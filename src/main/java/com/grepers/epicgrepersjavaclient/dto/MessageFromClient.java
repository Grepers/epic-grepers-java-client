package com.grepers.epicgrepersjavaclient.dto;

import javafx.geometry.Point2D;
import lombok.Data;

/**
 * Message from client.
 */
@Data
public class MessageFromClient {
    private Point2D vel;
    private double rot;
    private boolean firing;
}
