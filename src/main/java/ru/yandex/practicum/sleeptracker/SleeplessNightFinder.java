package ru.yandex.practicum.sleeptracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;


public class SleeplessNightFinder implements Function<SessionsCollection, String> {

    @Override
    public String apply(SessionsCollection collection) {
        LocalDate startDay = collection.sessions.stream()
                .map(SleepSession::getStartSession)
                .min(LocalDateTime::compareTo)
                .map(localDateTime ->
                        (localDateTime.getHour() >= 12) ? localDateTime.plusDays(1) : localDateTime)
                .orElseThrow(() -> new RuntimeException("Работа с сессиями возвращает null, возможно файл-ло пуст."))
                .toLocalDate();

        LocalDate endDay = collection.sessions.stream()
                .map(SleepSession::getEndSession)
                .max(LocalDateTime::compareTo)
                .orElseThrow(RuntimeException::new)
                .toLocalDate();

        long sleeplessCounter = startDay.datesUntil(endDay.plusDays(1))
                .filter(day -> {
                    boolean isHealthy = collection.sessions.stream()
                            .anyMatch(session -> {
                                LocalDateTime startOfNight = day.atTime(0, 0);
                                LocalDateTime EndOfNight = day.atTime(6, 0);
                                return session.getStartSession().isBefore(EndOfNight) &&
                                        session.getEndSession().isAfter(startOfNight);
                            });
                    return !isHealthy;
                })
                .count();
        return "Всего бессонных ночей: " + sleeplessCounter;
    }

}
