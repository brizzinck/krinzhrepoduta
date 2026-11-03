package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cameras")
@Getter
@Setter
public class CameraEntity {

    // id приходить із зовнішнього сервісу, тому @GeneratedValue не потрібна
    @Id
    private Long id;
    private String name;
    private String deviceType;
    private String status;
    private String room;
    private Integer floor;
}
