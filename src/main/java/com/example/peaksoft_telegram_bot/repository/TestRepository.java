package com.example.peaksoft_telegram_bot.repository;

import com.example.peaksoft_telegram_bot.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TestRepository extends JpaRepository<Test, Long> {
    @Query("select test from Test test where upper(test.name) like concat('%',:text,'%')")
    Test findByName(@Param("text") String text);
}
