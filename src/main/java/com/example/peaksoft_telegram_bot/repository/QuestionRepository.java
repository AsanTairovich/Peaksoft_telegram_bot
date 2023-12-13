package com.example.peaksoft_telegram_bot.repository;

import com.example.peaksoft_telegram_bot.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
}
