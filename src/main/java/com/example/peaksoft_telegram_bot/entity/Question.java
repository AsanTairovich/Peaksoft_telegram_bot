package com.example.peaksoft_telegram_bot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "questions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String questionName;
    private String questionTest;
    private String correctAnswer;
    private String incorrectAnswerOne;
    private String incorrectAnswerTwo;
    private String incorrectAnswerThree;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "test_id")
    @JsonIgnore
    private Test test;

}
