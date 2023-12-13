package com.example.peaksoft_telegram_bot.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    private Integer pin;
    private boolean emailActive;
    private LocalDateTime pinExpiration;
    @Email
    private String email;
    private int count;
    private String questionName;
    private int testResult;
    private int random;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user",fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Result> resultList;
}
