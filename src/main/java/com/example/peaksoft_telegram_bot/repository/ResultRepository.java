package com.example.peaksoft_telegram_bot.repository;

import com.example.peaksoft_telegram_bot.model.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {
    @Query("SELECT r FROM Result r WHERE r.resultQuestion > :result")
    List<Result> findByResult(@Param("result") int result);
}
