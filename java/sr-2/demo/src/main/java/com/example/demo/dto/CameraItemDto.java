package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CameraItemDto(
        Long id,
        String name,
        @JsonProperty("device_type") String deviceType,
        String status,
        CameraInfoDto.Location location
) {
}
