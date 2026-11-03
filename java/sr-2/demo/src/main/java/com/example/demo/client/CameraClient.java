package com.example.demo.client;

import com.example.demo.dto.CameraInfoDto;
import com.example.demo.dto.CameraListDto;
import com.example.demo.dto.SnapshotDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Клієнт зовнішнього сервісу DummyJSON. Адресу можна перевизначити властивістю camera.client.url
 * (за замовчуванням адреса з методички).
 */
@FeignClient(name = "camera-client", url = "${camera.client.url:https://dummyjson.com/c}")
public interface CameraClient {

    /** Список камер: шлях береться з application.properties */
    @GetMapping("${camera.api.list-path}")
    CameraListDto getCameras();

    /** Інформація про камеру: GET {url}/0716-c5cb-4391-afc3 */
    @GetMapping("/0716-c5cb-4391-afc3")
    CameraInfoDto getCameraInfo();

    /** Знімок: PUT {url}/382a-be11-44ef-8b0c (у відповіді поле image з Base64) */
    @PutMapping("/382a-be11-44ef-8b0c")
    SnapshotDto captureSnapshot();
}
