package com.example.peaksoft_telegram_bot.repository;

import com.example.peaksoft_telegram_bot.model.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TestRepository extends JpaRepository<Test, Long> {
    Optional<Test> findByName (String name);
}
