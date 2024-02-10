package ru.zinnurov.tgsharesbot.model.bot;

/**
 * Состояние пользователя.
 */
public enum UserBotState {
    AWAITING_COMMAND,
    AWAITING_QUERY,
    COMMAND_PROCESS
}
