package ru.yandex.practicum.sleeptracker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public class SleepTrackerApp {
    static SessionsCollection sessionsCollection;
    static List<Function<SessionsCollection, String>> functionList = new ArrayList<>();
    static String pathFromUser;

    public static void main(String[] args) {
        functionList.add(new SessionCounter());
        functionList.add(new ShortestSessionFinder());
        functionList.add(new LongestSessionFinder());
        functionList.add(new AveregeSleepDurationCalculation());
        functionList.add(new BadSessionCounter());
        functionList.add(new SleeplessNightFinder());
        functionList.add(new UserTypeFinder());

        Scanner scanner = new Scanner(System.in);
        System.out.println("Укажите абсолютный путь к файлу логов:");
        pathFromUser = scanner.nextLine();

        if (pathFromUser.isBlank()) {
            System.out.println("Путь к файлу указан неверно.");
            System.exit(-1);
        }

        try {
            if (pathFromUser.isBlank()) {
                System.out.println("Путь к файлу указан неверно.");
                throw new RuntimeException("Некорректный путь к файлу");
            }
            inputLog(pathFromUser);
        } catch (FileNotFoundException | RuntimeException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            throw new RuntimeException("Непредвидимая ошибка");
        }

        functionList.stream()
                .forEach(function ->
                        System.out.println(function.apply(sessionsCollection)));
    }

    protected static void inputLog(String path) throws IOException {
        Path fileLog = Paths.get(path);

        if (!(Files.exists(fileLog))) {
            throw new FileNotFoundException("Файл-лог не найден");
        }

        sessionsCollection = new SessionsCollection(path);

        if (sessionsCollection.sessions.isEmpty()) {
            throw new RuntimeException("Файл-лог пуст");
        }
    }
}