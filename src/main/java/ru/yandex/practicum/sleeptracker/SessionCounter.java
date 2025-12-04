package ru.yandex.practicum.sleeptracker;

import java.util.function.Function;

public class SessionCounter implements Function<SessionsCollection, String> {

    @Override
    public String apply(SessionsCollection collection) {
        return "Всего загружено %d сессий".formatted(collection.sessions.size());
    }
}
