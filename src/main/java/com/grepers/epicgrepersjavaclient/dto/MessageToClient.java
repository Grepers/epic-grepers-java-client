package com.grepers.epicgrepersjavaclient.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageToClient {
    private EventType eventType;
    private List<Actor> actors = new ArrayList<>();
}
