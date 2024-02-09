package ru.zinnurov.tgsharesbot.bot;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.zinnurov.tgsharesbot.handler.ResponseHandler;
import ru.zinnurov.tgsharesbot.model.bot.BotCommands;
import ru.zinnurov.tgsharesbot.model.bot.UserBotState;
import ru.zinnurov.tgsharesbot.service.ApiSharesService;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class TgSharesBot extends AbilityBot {

    private final static String BOT_NAME = "TgSharesBot";

    private ResponseHandler responseHandler;

    public TgSharesBot(Environment environment, ApiSharesService apiSharesService) {
        super(environment.getProperty("bot.token"), BOT_NAME);
        this.responseHandler = new ResponseHandler(silent, apiSharesService, db);
    }

    public ReplyFlow startBot() {
        BiConsumer<BaseAbilityBot, Update> greetings
                = (abilityBot, upd) -> this.responseHandler.replyToStart(getChatId(upd),
                AbilityUtils.getUser(upd).getUserName(),
                KeyboardFactory.getAllCommandButtons());

        return ReplyFlow.builder(db)
                .onlyIf(userIsActive().negate())
                .onlyIf(hasMessageWith(BotCommands.START.getCommand()))
                .action(greetings)
                .build();
    }

    public ReplyFlow baseCommandMessage() {
        BiConsumer<BaseAbilityBot, Update> baseCommandMessage
                = (abilityBot, upd) -> this.responseHandler.sendBaseCommandMessage(getChatId(upd),
                KeyboardFactory.getAllCommandButtons(), upd);

        return ReplyFlow.builder(db)
                .onlyIf(userInState(UserBotState.AWAITING_COMMAND))
                .onlyIf(hasCommandsName(BotCommands.values()).negate())
                .action(baseCommandMessage)
                .build();
    }

    public ReplyFlow stopBot() {
        BiConsumer<BaseAbilityBot, Update> stopBot
                = (abilityBot, upd) -> this.responseHandler.replyToStop(getChatId(upd));

        return ReplyFlow.builder(db)
                .onlyIf(userIsActive())
                .onlyIf(hasMessageWith(BotCommands.STOP.getCommand()))
                .action(stopBot)
                .build();
    }

    public ReplyFlow runCommand() {
        BiConsumer<BaseAbilityBot, Update> runCommand
                = (bot, upd) -> this.responseHandler.startCommand(upd);

        return ReplyFlow.builder(db)
                .onlyIf(userInState(UserBotState.AWAITING_COMMAND))
                .onlyIf(Update::hasCallbackQuery)
                .action(runCommand)
                .build();
    }

    public ReplyFlow startCommandProcess() {
        BiConsumer<BaseAbilityBot, Update> commandProcess
                = (bot, upd) -> this.responseHandler.startCommandProcess(getChatId(upd), upd.getMessage().getText());

        return ReplyFlow.builder(db)
                .onlyIf(userInState(UserBotState.AWAITING_QUERY))
                .action(commandProcess)
                .build();
    }

    public ReplyFlow backToTheMainMenu() {
        BiConsumer<BaseAbilityBot, Update> backToTheMainMenuAction
                = (bot, upd) -> this.responseHandler.backToAwaitingCommand(getChatId(upd), upd);

        return ReplyFlow.builder(db)
                .onlyIf(Update::hasCallbackQuery)
                .onlyIf(isThisCallBack(BotCommands.BACK_TO_MAIN_BOARD.getCallbackDataName()))
                .action(backToTheMainMenuAction)
                .build();
    }

    @NotNull
    private Predicate<Update> isThisCallBack(String callbackDataName) {
        return update -> callbackDataName.equals(update.getCallbackQuery().getData());
    }

    @NotNull
    private Predicate<Update> userIsActive() {
        return update -> this.responseHandler.userIsActive(getChatId(update));
    }

    @NotNull
    private Predicate<Update> userInState(UserBotState userState) {
        return update -> this.responseHandler.userInState(getChatId(update), userState);
    }

    @NotNull
    private Predicate<Update> hasCommandsName(BotCommands[] commands) {
        return upd -> Arrays.stream(commands).anyMatch(command -> upd.getMessage().getText().equalsIgnoreCase(command.getCommand()));
    }

    @NotNull
    private Predicate<Update> hasMessageWith(String msg) {
        return upd -> upd.getMessage().getText().equalsIgnoreCase(msg);
    }

    @Override
    public long creatorId() {
        return 402195552L;
    }
}
