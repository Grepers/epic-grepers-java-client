package com.grepers.epicgrepersjavaclient.dto;

import com.sun.javafx.geom.Vec2d;
import javafx.geometry.Point2D;
import lombok.Data;

import java.util.UUID;

@Data
public class Actor {
    private String type;
    private UUID id;
    private Vec2d pos; // meters
    private Vec2d vel; // meters / seconds
    private Double rot; // radians
}
