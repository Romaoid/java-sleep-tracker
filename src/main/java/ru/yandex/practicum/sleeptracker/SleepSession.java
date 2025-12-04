package ru.yandex.practicum.sleeptracker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SleepSession {
    private final LocalDateTime startSession;
    private final LocalDateTime endSession;
    private final SleepQuality quality;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    public SleepSession(LocalDateTime startSession, LocalDateTime endSession, SleepQuality quality) {
        this.startSession = startSession;
        this.endSession = endSession;
        this.quality = quality;
    }

    @Override
    public String toString() {
        return startSession.format(formatter) + " - "
                + endSession.format(formatter)
                + " Оценка качества сна: " + quality;
    }

    public LocalDateTime getStartSession() {
        return startSession;
    }

    public LocalDateTime getEndSession() {
        return endSession;
    }

    public SleepQuality getQuality() {
        return quality;
    }
}
