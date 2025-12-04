package ru.yandex.practicum.sleeptracker;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.sleeptracker.SleepTrackerApp.inputLog;

public class SleepTrackerAppTest {
    static SessionsCollection sessionsCollection;
    static Path fileLog = Paths.get(System.getProperty("user.dir"), "src","main", "resources","test_log.txt");
    static FileWriter writer;

    @AfterAll
    public static void deleteLog() throws IOException {
        writer.close();
        Files.deleteIfExists(fileLog);
    }

    @BeforeEach
    public void createCollection() throws IOException {
        try {
            if (!Files.exists(fileLog)) Files.createFile(fileLog);
            writer = new FileWriter(String.valueOf(fileLog));
            writer.write("10.10.25 21:59;11.10.25 06:59;NORMAL\n" +
                    "11.10.25 23:10;12.10.25 09:10;BAD\n" +
                    "12.10.25 23:00;13.10.25 10:00;GOOD\n" +
                    "13.10.25 21:00;14.10.25 06:30;GOOD");
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));
    }

    //inputLog tests
    @Test
    public void shouldInputLogByUserBeCorrect() throws IOException {
        writer = new FileWriter(String.valueOf(fileLog));
        writer.close();
        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));
        assertTrue(sessionsCollection.sessions.isEmpty());

        assertThrows(RuntimeException.class, () -> inputLog(String.valueOf(fileLog)));

        Files.deleteIfExists(fileLog);
        assertThrows(FileNotFoundException.class, () -> inputLog(String.valueOf(fileLog.toAbsolutePath())));
    }

    //SessionCollection test
    @Test
    public void shouldCreateCorrectSessionCollection() {
        assertEquals(4, sessionsCollection.sessions.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
        ArrayList<SleepSession> testList = new ArrayList<>();
        testList.add(new SleepSession(
                LocalDateTime.parse("10.10.25 21:59",formatter),
                LocalDateTime.of(2025, 10, 11, 6, 59),
                SleepQuality.NORMAL));
        testList.add(new SleepSession(
                LocalDateTime.of(2025, 10, 11, 23, 10),
                LocalDateTime.of(25, 10, 12, 9,10),
                SleepQuality.BAD));
        testList.add(new SleepSession(
                LocalDateTime.of(25, 10, 12, 23, 0),
                LocalDateTime.of(25, 10, 13, 10, 0),
                SleepQuality.GOOD));
        testList.add(new SleepSession(
                LocalDateTime.of(25, 10, 13, 21, 0),
                LocalDateTime.of(25, 10, 14, 6, 30),
                SleepQuality.GOOD));
        for (int i = 0; i < testList.size(); i++) {
            assertEquals(testList.get(i).toString(),sessionsCollection.sessions.get(i).toString());
        }
    }

    //SessionCounter tests
    @Test
    public void shouldSessionCounterGiveCorrectString() {
        SessionCounter sessionCounter = new SessionCounter();
        assertEquals("Всего загружено 4 сессий", sessionCounter.apply(sessionsCollection));
    }

    @Test
    public void shouldSessionCounterGiveZeroAndThree() throws IOException {
        writer = new FileWriter(String.valueOf(fileLog));
        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));
        SessionCounter sessionCounter = new SessionCounter();
        assertEquals("Всего загружено 0 сессий", sessionCounter.apply(sessionsCollection));

        writer.write("10.10.25 21:59;11.10.25 06:59;NORMAL\n" +
                "11.10.25 23:10;12.10.25 09:10;BAD\n" +
                "12.10.25 23:00;13.10.25 10:00;GOOD\n");
        writer.close();
        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));
        assertEquals("Всего загружено 3 сессий", sessionCounter.apply(sessionsCollection));
    }

    //ShortestSessionFinder tests
    @Test
    public void shouldShortestSessionFinderGiveNine() {
        ShortestSessionFinder shortestSessionFinder = new ShortestSessionFinder();
        assertEquals("Самая короткая сессия сна была 10.10.25 21:59 и длилась 9 часов 0 минут.",
                shortestSessionFinder.apply(sessionsCollection));
    }

    @Test
    public void shouldShortestSessionFinderBeCorrectInBoundary() {
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 14, 10, 0),
                LocalDateTime.of(2025, 10, 14, 11, 59),
                SleepQuality.NORMAL));
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 15, 10, 0),
                LocalDateTime.of(2025, 10, 15, 12, 0),
                SleepQuality.NORMAL));
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 16, 10, 0),
                LocalDateTime.of(2025, 10, 16, 11, 58),
                SleepQuality.NORMAL));
        ShortestSessionFinder shortestSessionFinder = new ShortestSessionFinder();

        assertEquals("Самая короткая сессия сна была 16.10.25 10:00 и длилась 1 часов 58 минут.",
                shortestSessionFinder.apply(sessionsCollection));

        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 16, 16, 0),
                LocalDateTime.of(2025, 10, 16, 16, 59),
                SleepQuality.NORMAL));

        assertEquals("Самая короткая сессия сна была 16.10.25 16:00 и длилась 0 часов 59 минут.",
                shortestSessionFinder.apply(sessionsCollection));
    }

    //LongestSessionFinder tests
    @Test
    public void shouldLongestSessionFinderGiveEleven() {
        LongestSessionFinder sessionFinder = new LongestSessionFinder();
        assertEquals("Самая длинная сессия сна была 12.10.25 23:00 и длилась 11 часов 0 минут.",
                sessionFinder.apply(sessionsCollection));
    }

    @Test
    public void shouldLongestSessionFinderBeCorrectInBoundary() {
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 14, 10, 0),
                LocalDateTime.of(2025, 10, 14, 21, 59),
                SleepQuality.NORMAL));
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 15, 10, 0),
                LocalDateTime.of(2025, 10, 15, 22, 1),
                SleepQuality.NORMAL));
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 16, 10, 0),
                LocalDateTime.of(2025, 10, 16, 22, 0),
                SleepQuality.NORMAL));

        LongestSessionFinder sessionFinder = new LongestSessionFinder();
        assertEquals("Самая длинная сессия сна была 15.10.25 10:00 и длилась 12 часов 1 минут.",
                sessionFinder.apply(sessionsCollection));

        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 17, 10, 0),
                LocalDateTime.of(2025, 10, 17, 23, 0),
                SleepQuality.NORMAL));

        assertEquals("Самая длинная сессия сна была 17.10.25 10:00 и длилась 13 часов 0 минут.",
                sessionFinder.apply(sessionsCollection));
    }

    //AverageSleepDurationCalculation tests
    @Test
    public void shouldAveregeSleepDurationCalculationBeNineFiftyFive() {
        Duration dur1 = Duration.between(
                LocalDateTime.of(2025, 10, 10, 21, 59),
                LocalDateTime.of(2025, 10, 11, 6, 59));
        Duration dur2Plus1 = Duration.between(
                        LocalDateTime.of(2025, 10, 11, 23, 10),
                        LocalDateTime.of(2025, 10, 12, 9,10))
                .plus(dur1);
        Duration dur3Plus2 = Duration.between(
                LocalDateTime.of(2025, 10, 12, 23, 0),
                LocalDateTime.of(2025, 10, 13, 10, 0)
                        .plus(dur2Plus1));
        Duration avgDur = Duration.between(
                        LocalDateTime.of(2025, 10, 13, 21, 0),
                        LocalDateTime.of(2025, 10, 14, 6, 30))
                .plus(dur3Plus2)
                .dividedBy(4);

        AveregeSleepDurationCalculation avg = new AveregeSleepDurationCalculation();
        assertEquals(
                "Средняя продолжительность сна " + avgDur.toHours() + " часов " + avgDur.toMinutesPart() + " минут",
                avg.apply(sessionsCollection));
    }

    @Test
    public void shouldAveregeSleepDurationCalculationDoCalculate() {
        Duration dur4 = Duration.ofMinutes(2370);

        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 14, 10, 0),
                LocalDateTime.of(2025, 10, 14, 15, 0),
                SleepQuality.NORMAL));
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 14, 22, 0),
                LocalDateTime.of(2025, 10, 15, 6, 0),
                SleepQuality.NORMAL));

        Duration avgDur = dur4.plus(Duration.ofHours(13)).dividedBy(6);

        AveregeSleepDurationCalculation avg = new AveregeSleepDurationCalculation();
        assertEquals(
                "Средняя продолжительность сна " + avgDur.toHours() + " часов " + avgDur.toMinutesPart() + " минут",
                avg.apply(sessionsCollection));

        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 15, 7, 0),
                LocalDateTime.of(2025, 10, 15, 7, 0),
                SleepQuality.NORMAL));

        avgDur = dur4.plus(Duration.ofHours(13)).dividedBy(7);

        assertEquals(
                "Средняя продолжительность сна " + avgDur.toHours() + " часов " + avgDur.toMinutesPart() + " минут",
                avg.apply(sessionsCollection));
    }


    //BadSessionCounter tests
    @Test
    public void shouldBadSessionCounterGiveOne() {
        BadSessionCounter sessionCounter = new BadSessionCounter();

        assertEquals("Сессий плохого сна: 1", sessionCounter.apply(sessionsCollection));
    }

    @Test
    public void shouldBadSessionCounterBeCorrect() {
        BadSessionCounter sessionCounter = new BadSessionCounter();

        for (int i = 0; i < 5; i++) {
            sessionsCollection.sessions.add(new SleepSession(
                    LocalDateTime.of(2025, 10, 14 + i, 22, 0),
                    LocalDateTime.of(2025, 10, 15 + i, 7, 0),
                    SleepQuality.BAD));
            assertEquals("Сессий плохого сна: " + (2 + i), sessionCounter.apply(sessionsCollection));
        }

        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 19, 22, 0),
                LocalDateTime.of(2025, 10, 20, 7, 0),
                SleepQuality.BAD));
        sessionsCollection.sessions.add(new SleepSession(
                LocalDateTime.of(2025, 10, 20, 22, 0),
                LocalDateTime.of(2025, 10, 21, 7, 0),
                SleepQuality.BAD));

        assertEquals("Сессий плохого сна: 8", sessionCounter.apply(sessionsCollection));
    }

    //SleeplessNightFinder tests
    @Test
    public void shouldSleeplessNightFinderGiveZero() {
        SleeplessNightFinder sleeplessCount = new SleeplessNightFinder();
        assertEquals("Всего бессонных ночей: 0", sleeplessCount.apply(sessionsCollection));
    }

    @Test
    public void shouldSleeplessNightFinderCheckOneMoreIfFirstSessionStartBeforeTwelveHours() throws IOException {
        SleeplessNightFinder sleeplessCount = new SleeplessNightFinder();
        //First day is sleepness
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("10.10.25 11:59;10.10.25 16:59;NORMAL\n" +
                "11.10.25 13:10;11.10.25 19:10;BAD\n" +
                "12.10.25 13:00;12.10.25 15:00;GOOD\n");
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Всего бессонных ночей: 3", sleeplessCount.apply(sessionsCollection));
        //First day isn'n sleepness
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("10.10.25 05:59;10.10.25 16:59;NORMAL\n" +
                "11.10.25 13:10;11.10.25 19:10;BAD\n" +
                "12.10.25 13:00;12.10.25 15:00;GOOD\n");
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Всего бессонных ночей: 2", sleeplessCount.apply(sessionsCollection));
        //First day isn't before 12:00
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("10.10.25 12:00;10.10.25 16:59;NORMAL\n" +
                "11.10.25 13:10;11.10.25 19:10;BAD\n" +
                "12.10.25 13:00;12.10.25 15:00;GOOD\n");
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Всего бессонных ночей: 2", sleeplessCount.apply(sessionsCollection));
    }

    @Test
    public void shouldSleeplessNightBeCorrectOnBoundary() throws IOException {
        SleeplessNightFinder sleeplessCount = new SleeplessNightFinder();
        //SleeplessNight is only at 06:00-24:00
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("10.10.25 23:58;10.10.25 23:59;NORMAL\n" + //не учитывается
                "11.10.25 23:58;12.10.25 00:00;BAD\n" + //бессонная
                "13.10.25 00:00;13.10.25 03:00;GOOD\n" + //не бессонная
                "14.10.25 06:00;14.10.25 06:01;GOOD\n" + //бессонная
                "13.10.25 00:00;13.10.25 06:00;GOOD\n" + //не бессонная
                "14.10.25 23:58;14.10.25 23:59;GOOD\n" + //бессонная
                "15.10.25 03:00;15.10.25 09:59;GOOD\n"); //не бессонная
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Всего бессонных ночей: 3", sleeplessCount.apply(sessionsCollection));
    }

    @Test
    public void shouldSleeplessNightBeCorrectWithLongDream() throws IOException {
        SleeplessNightFinder sleeplessCount = new SleeplessNightFinder();
        //Is not sleepless night if you sleep all the time
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("""
                10.10.25 23:58;15.10.25 01:59;NORMAL
                15.10.25 03:00;17.10.25 09:59;GOOD
                """);
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Всего бессонных ночей: 0", sleeplessCount.apply(sessionsCollection));
    }

    //UserTypeFinder tests
    @Test
    public void shouldUserTypeFinderBeLarkIfLarksNightsIsMore() {
        UserTypeFinder userTypeFinder = new UserTypeFinder();
        //Корректный сценарий, жаворонков больше
//        10.10.25 21:59 - 11.10.25 06:59 - Жаворонок, время засыпания было до 22:00, а время пробуждения до — 7:00
//        11.10.25 23:10 - 12.10.25 09:10 - Сова, время засыпания было после 23:00, а время пробуждения — после 9:00
//        12.10.25 23:00 - 13.10.25 10:00 - Голубь во всех остальных случаях
//        13.10.25 21:00 - 14.10.25 06:30 - Жаворонок

        assertEquals(
                "Тип пользователя определен как: " + UserType.LARK,
                userTypeFinder.apply(sessionsCollection)
        );
    }

    @Test
    public void shouldUserTypeFinderBeDoveIfDovesNightIsMore() throws IOException {
        UserTypeFinder userTypeFinder = new UserTypeFinder();
        //Корректный сценарий, голубей больше
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("""
                10.10.25 22:58;11.10.25 01:59;GOOD
                12.10.25 23:00;13.10.25 09:59;GOOD
                13.10.25 23:10;14.10.25 08:59;GOOD
                14.10.25 23:10;15.10.25 09:00;GOOD
                15.10.25 23:10;16.10.25 10:59;NORMAL
                16.10.25 23:30;17.10.25 11:59;NORMAL
                18.10.25 00:30;18.10.25 09:59;NORMAL
                18.10.25 21:30;19.10.25 06:59;BAD
                19.10.25 20:30;20.10.25 03:59;BAD
                """);
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Тип пользователя определен как: " + UserType.DOVE,
                userTypeFinder.apply(sessionsCollection));
    }

    @Test
    public void shouldUserTypeFinderBeOwlIfOwlsNightIsMore() throws IOException {
        UserTypeFinder userTypeFinder = new UserTypeFinder();
        //Корректный сценарий, сов больше
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("""
                13.10.25 23:10;14.10.25 08:59;GOOD
                14.10.25 23:10;15.10.25 09:00;GOOD
                15.10.25 23:10;16.10.25 10:59;NORMAL
                16.10.25 23:30;17.10.25 11:59;NORMAL
                18.10.25 00:30;18.10.25 09:59;NORMAL
                18.10.25 21:30;19.10.25 06:59;BAD
                19.10.25 20:30;20.10.25 03:59;BAD
                """);
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Тип пользователя определен как: " + UserType.OWL,
                userTypeFinder.apply(sessionsCollection));
    }

    @Test
    public void shouldUserTypeFinderBeDoveIfLarkCountIsEqualOwl() throws IOException {
        UserTypeFinder userTypeFinder = new UserTypeFinder();
        //Корректный сценарий, сов == жаворонков
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("""
                16.10.25 23:30;17.10.25 11:59;NORMAL
                18.10.25 00:30;18.10.25 09:59;NORMAL
                18.10.25 21:30;19.10.25 06:59;BAD
                19.10.25 20:30;20.10.25 03:59;BAD
                """);
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Тип пользователя определен как: " + UserType.DOVE,
                userTypeFinder.apply(sessionsCollection));
    }

    @Test
    public void shouldUserTypeFinderNotCalculateSleepnessNights() throws IOException {
        UserTypeFinder userTypeFinder = new UserTypeFinder();
        //Корректный сценарий, Dove:2, owl:1, lark:0, sleepless:3
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("10.10.25 23:58;10.10.25 23:59;NORMAL\n" + //не учитывается
                "11.10.25 23:58;12.10.25 00:00;BAD\n" + //бессонная
                "12.10.25 00:00;12.10.25 10:00;GOOD\n" + //не бессонная + сов
                "13.10.25 06:00;13.10.25 06:01;GOOD\n" + //бессонная
                "14.10.25 00:00;14.10.25 06:00;GOOD\n" + //не бессонная + голуб
                "14.10.25 23:58;14.10.25 23:59;GOOD\n" + //бессонная
                "15.10.25 03:00;15.10.25 06:59;GOOD\n"); //не бессонная + голуб
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Тип пользователя определен как: " + UserType.DOVE,
                userTypeFinder.apply(sessionsCollection));
    }

    @Test
    public void shouldUserTypeFinderNotCalculateLongDreams() throws IOException {
        UserTypeFinder userTypeFinder = new UserTypeFinder();
        //Корректный сценарий, 4 беспорядочных ночи - голубиные
        writer = new FileWriter(String.valueOf(fileLog));
        writer.write("10.10.25 23:58;13.10.25 15:59;NORMAL\n" + //10 не учитывается
                "13.10.25 23:58;15.10.25 00:00;BAD\n"); // 15 - бессонная
        writer.close();

        sessionsCollection = new SessionsCollection(String.valueOf(fileLog));

        assertEquals("Тип пользователя определен как: " + UserType.DOVE,
                userTypeFinder.apply(sessionsCollection));
    }
}