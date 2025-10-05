package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class PublicStationResponse {
    private Long id;
    private String name;
    private String code;
    private int stationOrder;
}