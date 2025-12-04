package ru.yandex.practicum.sleeptracker;

import java.util.function.Function;

public class BadSessionCounter implements Function<SessionsCollection, String> {

    @Override
    public String apply(SessionsCollection collection) {
        long counter = collection.sessions.stream()
                .filter(sleepSession -> sleepSession.getQuality().equals(SleepQuality.BAD))
                .count();
        return "Сессий плохого сна: " + counter;
    }
}
