package ru.zinnurov.tgsharesbot.model.bot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BotCommands {
    START("Запуск бота", "/start", "start", false),
    STOP("Остановить бота", "/stop", "stop", false),
    SHARE_PRICE("Цена акции", "/share_price", "share_price", true),
    BACK_TO_MAIN_BOARD("Назад", "/back", "back_to_awaiting_command", false);

    private String name;
    private String command;
    private String callbackDataName;
    private boolean enabledInMainBoard;
}
