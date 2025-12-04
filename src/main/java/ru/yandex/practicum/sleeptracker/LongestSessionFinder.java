package ru.yandex.practicum.sleeptracker;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class LongestSessionFinder implements Function<SessionsCollection, String> {

    @Override
    public String apply(SessionsCollection collection) {
        SleepSession minSession = collection.sessions.stream()
                .min(new Comparator<SleepSession>() {
                    @Override
                    public int compare(SleepSession o1, SleepSession o2) {
                        Duration durationO1 = Duration.between(o1.getStartSession(), o1.getEndSession());
                        Duration durationO2 = Duration.between(o2.getStartSession(), o2.getEndSession());
                        return durationO2.compareTo(durationO1);
                    }
                }).orElseThrow(NoSuchElementException::new);

        return "Самая длинная сессия сна была %s и длилась %d часов %d минут.".formatted(
                minSession.getStartSession().format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")),
                Duration.between(minSession.getStartSession(), minSession.getEndSession()).toHours(),
                Duration.between(minSession.getStartSession(), minSession.getEndSession()).toMinutesPart()
        )
                ;
    }
}