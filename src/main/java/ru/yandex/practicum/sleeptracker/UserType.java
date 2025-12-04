package ru.yandex.practicum.sleeptracker;

public enum UserType {
    OWL("Сова"),
    DOVE("Голубь"),
    LARK("Жаворонок");

    private final String description;

    UserType(String description) {
        this.description = description;
    }
    @Override
    public String toString() {
        return description;
    }
}
