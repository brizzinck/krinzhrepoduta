package com.example.demo.service;

import com.example.demo.client.CameraClient;
import com.example.demo.dto.CameraInfoDto;
import com.example.demo.dto.SnapshotDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // конструктор для final-поля: залежність передає Spring
@Slf4j
public class CameraService {

    private final CameraClient cameraClient;

    public CameraInfoDto getCameraInfo() {
        log.info("Getting camera info");
        CameraInfoDto response = cameraClient.getCameraInfo();
        log.info("Camera info received: {}", response);
        return response;
    }

    public SnapshotDto captureSnapshot() {
        log.info("Capturing snapshot");
        SnapshotDto response = cameraClient.captureSnapshot();
        log.info("Snapshot received");
        return response;
    }
}
