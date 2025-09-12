package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<Collection<Mpa>> getAllMpaRatings() {
        log.info("Получение списка всех MPA рейтингов");
        return ResponseEntity.ok(mpaService.getAllMpaRatings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getMpaRating(@PathVariable Long id) {
        log.info("Получение MPA рейтинга с ID: {}", id);
        return ResponseEntity.ok(mpaService.getMpaRatingById(id));
    }
}