/**
 * Source :
 * https://dev.to/codegym_cc/creating-a-telegram-bot-in-java-from-conception-to-deployment-3a8c
 */

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

public class Bot extends TelegramLongPollingBot {
    private HashMap<Long, Integer> addExerciseStatus = new HashMap<>();
    private HashMap<Long, List<List<String>>> addExerciseData = new HashMap<>();


    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {

    }

    public String getBotUsername() {
        return "BachelorHunterz";
    }

    public String getBotToken() {
        return "1527645320:AAH7Sh-jBaM4eAOp0TfU-nRK0xtDIXKEmvU";
    }


}
