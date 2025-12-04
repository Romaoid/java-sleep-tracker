package ru.yandex.practicum.sleeptracker;

import java.time.Duration;
import java.util.function.Function;

public class AveregeSleepDurationCalculation implements Function<SessionsCollection, String> {

    @Override
    public String apply(SessionsCollection collection) {
        Duration avgDuration = collection.sessions.stream()
                .map(sleepSession -> Duration.between(
                        sleepSession.getStartSession(),
                        sleepSession.getEndSession())
                )
                .reduce((averageDuration,duration) -> averageDuration.plus(duration))
                .orElseThrow(() -> new RuntimeException("Работа с сессиями возвращает null, возможно файл-ло пуст."));
        avgDuration = avgDuration.dividedBy(collection.sessions.size());

        return "Средняя продолжительность сна %d часов %d минут".formatted(
                avgDuration.toHours(),
                avgDuration.toMinutesPart()
        );
    }
}
