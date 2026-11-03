package com.example.demo.service;

import com.example.demo.client.CameraClient;
import com.example.demo.dto.CameraInfoDto;
import com.example.demo.dto.CameraItemDto;
import com.example.demo.dto.CameraListDto;
import com.example.demo.dto.SnapshotDto;
import com.example.demo.entity.CameraEntity;
import com.example.demo.repository.CameraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor // конструктор для final-полів: залежності передає Spring
@Slf4j
public class CameraService {

    private final CameraClient cameraClient;
    private final CameraRepository cameraRepository;

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

    /** Отримує список через Feign і зберігає його. Запис із наявним id оновлюється, дубля немає. */
    @Transactional
    public List<CameraEntity> importCameras() {
        CameraListDto response = cameraClient.getCameras();
        if (response == null || response.cameras() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid camera list response");
        }
        List<CameraEntity> entities = response.cameras().stream().map(this::toEntity).toList();
        log.info("Importing {} cameras", entities.size());
        return cameraRepository.saveAll(entities);
    }

    private CameraEntity toEntity(CameraItemDto dto) {
        if (dto == null || dto.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Camera ID is missing");
        }
        CameraEntity entity = new CameraEntity();
        entity.setId(dto.id());
        entity.setName(dto.name());
        entity.setDeviceType(dto.deviceType());
        entity.setStatus(dto.status());
        // location у таблиці зберігається двома окремими стовпцями
        if (dto.location() != null) {
            entity.setRoom(dto.location().room());
            entity.setFloor(dto.location().floor());
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public List<CameraEntity> getSavedCameras() {
        return cameraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CameraEntity getSavedCamera(Long id) {
        return cameraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Camera not found"));
    }

    @Transactional
    public void deleteSavedCamera(Long id) {
        cameraRepository.delete(getSavedCamera(id));
        log.info("Deleted camera {}", id);
    }
}
