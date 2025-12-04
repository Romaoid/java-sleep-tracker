package ru.yandex.practicum.sleeptracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class UserTypeFinder implements Function<SessionsCollection, String> {

    @Override
    public String apply(SessionsCollection collection) {
        LocalDate startDay = collection.sessions.stream()
                .map(SleepSession::getStartSession)
                .min(LocalDateTime::compareTo)
                .map(localDateTime ->
                        (localDateTime.getHour() >= 12) ? localDateTime.plusDays(1) : localDateTime)
                .orElseThrow(() ->
                        new RuntimeException("Работа с сессиями возвращает null, возможно файл-ло пуст."))
                .toLocalDate();

        Period nightCount = Period.between(startDay, collection.sessions.stream()
                        .map(SleepSession::getEndSession)
                        .max(LocalDateTime::compareTo)
                        .orElseThrow(RuntimeException::new)
                        .toLocalDate()
                        .plusDays(1)
        );

        long owlNightCount = Stream.iterate(startDay, day -> day.plusDays(1))
                .limit(nightCount.getDays())
                .filter(day -> isHealthy(day, collection.sessions))
                .filter(day -> {
                    return collection.sessions.stream()
                            .anyMatch(session -> {
                                LocalDateTime startOwlNight = day.atTime(23, 0).minusDays(1);
                                LocalDateTime endOwlNight = day.atTime(9, 0);
                                boolean isOwlNight =
                                        session.getStartSession().isAfter(startOwlNight) &&
                                        session.getStartSession().isBefore(endOwlNight) &&
                                        session.getEndSession().isAfter(endOwlNight) &&
                                        session.getEndSession().isBefore(startOwlNight.plusDays(1));
                                return isOwlNight;
                            });

                })
                .count();

        long larkNightCount = Stream.iterate(startDay,day -> day.plusDays(1))
                .limit(nightCount.getDays())
                .filter(day -> isHealthy(day, collection.sessions))
                .filter(day -> {
                    return collection.sessions.stream()
                            .anyMatch(session -> {
                                LocalDateTime larkStartNight = day.atTime(22,0).minusDays(1);
                                LocalDateTime larkEndNight = day.atTime(7,0);
                                boolean isLarkNihgt =
                                        session.getStartSession().isBefore(larkStartNight) &&
                                        session.getStartSession().isAfter(larkEndNight.minusDays(1)) &&
                                        session.getEndSession().isAfter(day.atStartOfDay()) &&
                                        session.getEndSession().isBefore(larkEndNight);
                                return isLarkNihgt;
                            });
                })
                .count();

        long healthyNights = Stream.iterate(startDay,day -> day.plusDays(1))
                .limit(nightCount.getDays())
                .filter(day -> {
                    return collection.sessions.stream()
                            .anyMatch(session -> {
                                LocalDateTime startOfNight = day.atTime(0, 0);
                                LocalDateTime endOfNight = day.atTime(6, 0);
                                return session.getStartSession().isBefore(endOfNight) &&
                                        session.getEndSession().isAfter(startOfNight);
                            });
                })
                .count();

        long doveNightCount = healthyNights - owlNightCount - larkNightCount;

        if ((owlNightCount > larkNightCount) && (owlNightCount > doveNightCount)) {
            collection.setUserType(UserType.OWL);
        } else if ((larkNightCount > owlNightCount) && (larkNightCount > doveNightCount)) {
            collection.setUserType(UserType.LARK);
        } else {
            collection.setUserType(UserType.DOVE);
        }

        return "Тип пользователя определен как: " + collection.userType;
    }

    private boolean isHealthy(LocalDate day, List<SleepSession> sessions) {
        return sessions.stream()
                .anyMatch(session -> {
                    LocalDateTime startOfNight = day.atTime(0, 0);
                    LocalDateTime endOfNight = day.atTime(6, 0);
                    return session.getStartSession().isBefore(endOfNight) &&
                            session.getEndSession().isAfter(startOfNight);
                });
    }
}
