package ru.zinnurov.tgsharesbot.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.zinnurov.tgsharesbot.model.bot.BotCommands;

import java.util.ArrayList;
import java.util.List;

/**
 * Конструктор клавиатур с командами.
 */
public class KeyboardFactory {


    /**
     * Получение главной клавиатуры со всеми доступными командами.
     */
    public static InlineKeyboardMarkup getAllCommandButtons() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (BotCommands command : BotCommands.values()) {
            if(command.isEnabledInMainBoard()) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(command.getName());
                button.setCallbackData(command.getCallbackDataName());
                rowInline.add(button);
            }
        }
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    /**
     * Получение кнопки "Назад".
     */
    public static InlineKeyboardMarkup getBackButton() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(BotCommands.BACK_TO_MAIN_BOARD.getName());
        button.setCallbackData(BotCommands.BACK_TO_MAIN_BOARD.getCallbackDataName());
        rowInline.add(button);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
