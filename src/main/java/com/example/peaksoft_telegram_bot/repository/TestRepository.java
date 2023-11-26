package com.example.peaksoft_telegram_bot.repository;

import com.example.peaksoft_telegram_bot.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TestRepository extends JpaRepository<Test, Long> {
    Optional<Test> findByName (String name);
}
