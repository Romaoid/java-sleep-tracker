package ru.yandex.practicum.sleeptracker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SessionsCollection {
    List<SleepSession> sessions;
    UserType userType;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    public SessionsCollection(String sleepSessionsLog) throws IOException {
        sessions = imputList(sleepSessionsLog);
    }

    private List<SleepSession> imputList(String sleepSessionsLog) throws IOException {
        List<SleepSession> list;
        try (BufferedReader buffer =
                     new BufferedReader(new FileReader(sleepSessionsLog, StandardCharsets.UTF_8))) {
            list = buffer.lines()
                    .map(line -> {
                        String[] lines = line.split(";");
                        SleepQuality sq = lines[2].equalsIgnoreCase("good") ? SleepQuality.GOOD : (
                                lines[2].equalsIgnoreCase("bad") ? SleepQuality.BAD : SleepQuality.NORMAL);
                        return new SleepSession(
                                LocalDateTime.parse(lines[0],formatter),
                                LocalDateTime.parse(lines[1], formatter),
                                sq
                        );
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IOException();
        }
        return list;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
