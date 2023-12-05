package com.example.peaksoft_telegram_bot.repository;

import com.example.peaksoft_telegram_bot.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<Result, Long> {
}
