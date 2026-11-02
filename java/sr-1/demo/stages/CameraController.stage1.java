package com.example.demo.controller;

import com.example.demo.dto.CameraInfoDto;
import com.example.demo.dto.SnapshotDto;
import com.example.demo.service.CameraService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/camera")
@RequiredArgsConstructor
@Tag(name = "Camera")
public class CameraController {

    private final CameraService cameraService;

    /** GET /api/v1/camera/info: CameraService, далі Feign GET, відповідь JSON у CameraInfoDto */
    @GetMapping("/info")
    public CameraInfoDto getCameraInfo() {
        return cameraService.getCameraInfo();
    }

    /** Етап 1: POST /api/v1/camera/snapshot повертає SnapshotDto як JSON (Base64 у полі image). */
    @PostMapping("/snapshot")
    public SnapshotDto snapshot() {
        return cameraService.captureSnapshot();
    }
}
