package com.example.demo.repository;

import com.example.demo.entity.CameraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CameraRepository extends JpaRepository<CameraEntity, Long> {
}
