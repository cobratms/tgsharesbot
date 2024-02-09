package ru.zinnurov.tgsharesbot.utils;

/**
 * Шаблоны информационных сообщений.
 */
public class MessageTemplates {
    public static final String START_TEXT = """
                Здравствуй, %s!
                Пользуйся командами:
                """;
    public static final String BASE_COMMAND_TEXT = "Пользуйся командами:";
    public static final String STOP_BOT = "До свидания!";
    public static final String SHARES_NAME_QUERY = "Введите название акции:";
    public static final String WRONG_SHARE_NAME = "Не найдено акции с таким названием!";
}
