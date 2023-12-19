package com.example.peaksoft_telegram_bot.repository;

import com.example.peaksoft_telegram_bot.model.entity.Test;
import com.example.peaksoft_telegram_bot.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface TestRepository extends JpaRepository<Test, Long> {
    @Query("SELECT t FROM Test t WHERE t.name = :name")
    Test findByName(@Param("name") String name);
}
