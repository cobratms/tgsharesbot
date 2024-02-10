package ru.zinnurov.tgsharesbot.bot;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.zinnurov.tgsharesbot.handler.ResponseHandler;
import ru.zinnurov.tgsharesbot.model.bot.BotCommands;
import ru.zinnurov.tgsharesbot.model.bot.UserBotState;
import ru.zinnurov.tgsharesbot.service.ApiSharesService;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

/**
 * Основной класс телеграмм-бота.
 */
@Component
public class TgSharesBot extends AbilityBot {

    private final static String BOT_NAME = "TgSharesBot";

    private final ResponseHandler responseHandler;

    public TgSharesBot(Environment environment, ApiSharesService apiSharesService) {
        super(environment.getProperty("bot.token"), BOT_NAME);
        this.responseHandler = new ResponseHandler(silent, apiSharesService);
    }

    /**
     * Обработка команды /start<br/>
     * <br/>
     * Условие обработки:<br/>
     * 1. Пользователь еще не запустил робота (отсутствие состояния - {@link  ru.zinnurov.tgsharesbot.model.bot.UserBotState  UserBotState}.<br/>
     * 2. Сообщение от пользователя должно содержать только команду - {@link  ru.zinnurov.tgsharesbot.model.bot.BotCommands#START  START}.
     */
    public ReplyFlow startBot() {
        BiConsumer<BaseAbilityBot, Update> greetings
                = (abilityBot, upd) -> this.responseHandler.replyToStart(getChatId(upd),
                AbilityUtils.getUser(upd).getUserName(),
                KeyboardFactory.getAllCommandButtons());

        return ReplyFlow.builder(db)
                .onlyIf(userIsActive().negate())
                .onlyIf(hasMessageEquals(BotCommands.START.getCommand()))
                .action(greetings)
                .build();
    }

    /**
     * Обработка любого сообщения не равное имени команды от пользователя<br/>
     * в состоянии ожидания выбора команды.<br/>
     *<br/>
     * Условия обработки:<br/>
     * 1. Пользователь в состоянии ожидания выбора команды - {@link  ru.zinnurov.tgsharesbot.model.bot.UserBotState#AWAITING_COMMAND  AWAITING_COMMAND}<br/>
     * 2. Сообщение не равно существующим командам - {@link  ru.zinnurov.tgsharesbot.model.bot.BotCommands  BotCommands}.<br/>
     */
    public ReplyFlow baseCommandMessage() {
        BiConsumer<BaseAbilityBot, Update> baseCommandMessage
                = (abilityBot, upd) -> this.responseHandler.sendBaseCommandMessage(getChatId(upd),
                KeyboardFactory.getAllCommandButtons());

        return ReplyFlow.builder(db)
                .onlyIf(userInState(UserBotState.AWAITING_COMMAND))
                .onlyIf(hasCommandsName(BotCommands.values()).negate())
                .action(baseCommandMessage)
                .build();
    }

    /**
     * Обработка команды /stop<br/>
     * <br/>
     * Условие обработки:<br/>
     * 1. Пользователь еще не запустил робота (наличие состояния - {@link  ru.zinnurov.tgsharesbot.model.bot.UserBotState  UserBotState}.<br/>
     * 2. Сообщение от пользователя должно содержать только команду {@link  ru.zinnurov.tgsharesbot.model.bot.BotCommands#STOP  STOP}.
     */
    public ReplyFlow stopBot() {
        BiConsumer<BaseAbilityBot, Update> stopBot
                = (abilityBot, upd) -> this.responseHandler.replyToStop(getChatId(upd));

        return ReplyFlow.builder(db)
                .onlyIf(userIsActive())
                .onlyIf(hasMessageEquals(BotCommands.STOP.getCommand()))
                .action(stopBot)
                .build();
    }

    /**
     * Обработка нажатия пользователем на одну из доступных кнопок на главном меню.<br/>
     *<br/>
     * Условие обработки:<br/>
     * 1. Пользователь должен быть в состоянии ожидания команды - {@link  ru.zinnurov.tgsharesbot.model.bot.UserBotState#AWAITING_COMMAND  AWAITING_COMMAND}<br/>
     * 2. Наличие CallbackQuery.<br/>
     *<br/>
     * @see KeyboardFactory#getAllCommandButtons()
     */
    public ReplyFlow runCommand() {
        BiConsumer<BaseAbilityBot, Update> runCommand
                = (bot, upd) -> this.responseHandler.startCommand(upd);

        return ReplyFlow.builder(db)
                .onlyIf(userInState(UserBotState.AWAITING_COMMAND))
                .onlyIf(Update::hasCallbackQuery)
                .action(runCommand)
                .build();
    }

    /**
     * Обработка сообщения полученного после запуска команды. <br/>
     *<br/>
     * Условия обработки:<br/>
     * 1. Пользователь в состоянии ожидания выбора команды - {@link  ru.zinnurov.tgsharesbot.model.bot.UserBotState#AWAITING_QUERY  AWAITING_QUERY}<br/>
     */
    public ReplyFlow startCommandProcess() {
        BiConsumer<BaseAbilityBot, Update> commandProcess
                = (bot, upd) -> this.responseHandler.startCommandProcess(getChatId(upd), upd.getMessage().getText());

        return ReplyFlow.builder(db)
                .onlyIf(userInState(UserBotState.AWAITING_QUERY))
                .action(commandProcess)
                .build();
    }

    /**
     * Обработка нажатия пользователем на кнопку "Назад".<br/>
     *<br/>
     * Условие обработки:<br/>
     * Наличие CallbackQuery равное - {@link  ru.zinnurov.tgsharesbot.model.bot.BotCommands#BACK_TO_MAIN_BOARD  back_to_main_command}<br/>
     * <br/>
     * @see KeyboardFactory#getBackButton()
     */
    public ReplyFlow backToTheMainMenu() {
        BiConsumer<BaseAbilityBot, Update> backToTheMainMenuAction
                = (bot, upd) -> this.responseHandler.backToAwaitingCommand(getChatId(upd));

        return ReplyFlow.builder(db)
                .onlyIf(Update::hasCallbackQuery)
                .onlyIf(isThisCallBack(BotCommands.BACK_TO_MAIN_BOARD.getCallbackDataName()))
                .action(backToTheMainMenuAction)
                .build();
    }

    /**
     * Проверка на соотвествие имени callBack.
     */
    @NotNull
    private Predicate<Update> isThisCallBack(String callbackDataName) {
        return update -> callbackDataName.equals(update.getCallbackQuery().getData());
    }

    /**
     * Проверка состояния пользователя к боту.
     */
    @NotNull
    private Predicate<Update> userIsActive() {
        return update -> this.responseHandler.userIsActive(getChatId(update));
    }

    /**
     * Проверка нахождения пользователя в конкретном состоянии.
     */
    @NotNull
    private Predicate<Update> userInState(UserBotState userState) {
        return update -> this.responseHandler.userInState(getChatId(update), userState);
    }

    /**
     * Проверка сообщения на соответствие с командой.
     */
    @NotNull
    private Predicate<Update> hasCommandsName(BotCommands[] commands) {
        return upd -> Arrays.stream(commands).anyMatch(command -> upd.getMessage().getText().equalsIgnoreCase(command.getCommand()));
    }

    /**
     * Проверка сообщения на соотвествие с текстом.
     */
    @NotNull
    private Predicate<Update> hasMessageEquals(String msg) {
        return upd -> upd.getMessage().getText().equalsIgnoreCase(msg);
    }

    @Override
    public long creatorId() {
        return 402195552L;
    }
}
