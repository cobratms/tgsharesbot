package ru.zinnurov.tgsharesbot.handler;

import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.zinnurov.tgsharesbot.bot.KeyboardFactory;
import ru.zinnurov.tgsharesbot.model.bot.BotCommands;
import ru.zinnurov.tgsharesbot.model.bot.UserBotState;
import ru.zinnurov.tgsharesbot.service.ApiSharesService;
import ru.zinnurov.tgsharesbot.utils.MessageTemplates;

import java.util.Arrays;
import java.util.Map;

/**
 * Обработчик ответов пользователя.
 */
public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserBotState> chatStates;
    private final Map<Long, BotCommands> lastCommand;
    private final ApiSharesService apiSharesService;

    public ResponseHandler(SilentSender sender, ApiSharesService apiSharesService, DBContext db) {
        this.sender = sender;
        this.apiSharesService = apiSharesService;
        this.chatStates = db.getMap("chatStates");
        this.lastCommand = db.getMap("lastCommand");
    }

    /**
     * Обработка команды /start отправленная пользователем.
     *
     * Отправляет привественное сообщение пользователю
     * и переводит
     * @param chatId
     * @param userName
     * @param allCommandButtons
     */
    public void replyToStart(long chatId, String userName, InlineKeyboardMarkup allCommandButtons) {
        this.sendMessage(chatId, allCommandButtons, String.format(MessageTemplates.START_TEXT, userName));
        chatStates.put(chatId, UserBotState.AWAITING_COMMAND);
    }

    public void replyToStop(long chatId) {
        this.sendMessage(chatId, MessageTemplates.STOP_BOT);
        chatStates.clear();
    }

    public void sendBaseCommandMessage(long chatId, InlineKeyboardMarkup allCommandButtons, Update update) {
        this.sendMessage(chatId, allCommandButtons, MessageTemplates.BASE_COMMAND_TEXT);
    }

    private void sendMessage(long chatId, InlineKeyboardMarkup commandButtons, String textMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textMessage);
        message.setReplyMarkup(commandButtons);
        sender.execute(message);
    }

    private void sendMessage(long chatId, String textMessage) {
        this.sendMessage(chatId, null, textMessage);
    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }

    public boolean userInState(Long chatId, UserBotState ... userState) {
        return Arrays.stream(userState).anyMatch(userBotState -> userBotState == chatStates.get(chatId));
    }

    public void startCommand(Update update) {
        String callData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (BotCommands.SHARE_PRICE.getCallbackDataName().equals(callData)) {
            chatStates.put(chatId, UserBotState.AWAITING_QUERY);
            lastCommand.put(chatId, BotCommands.SHARE_PRICE);
            this.sendMessage(chatId, KeyboardFactory.getBackButton(), MessageTemplates.SHARES_NAME_QUERY);
        }
    }

    public void startCommandProcess(Long chatId, String userQuery) {
        if (BotCommands.SHARE_PRICE == lastCommand.get(chatId)) {
            String response = this.apiSharesService.getSharePriceByName(userQuery);
            if (StringUtils.hasText(response)) {
                response += " USD";
            } else {
                response = MessageTemplates.WRONG_SHARE_NAME;
            }
            this.sendMessage(chatId, null, response);
            chatStates.put(chatId, UserBotState.AWAITING_COMMAND);

        }
    }

    public void backToAwaitingCommand(Long chatId, Update update) {
        this.sendBaseCommandMessage(chatId, KeyboardFactory.getAllCommandButtons(), update);
        chatStates.put(chatId, UserBotState.AWAITING_COMMAND);
    }
}
