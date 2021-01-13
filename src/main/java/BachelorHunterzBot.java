/**
 * Sources :
 * https://www.youtube.com/watch?v=xv-FYOizUSY
 */

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BachelorHunterzBot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "BachelorHunterz_bot";
    }

    @Override
    public String getBotToken() {
        return "1527645320:AAH7Sh-jBaM4eAOp0TfU-nRK0xtDIXKEmvU";
    }

    @Override
    public void onUpdateReceived(Update update) {
        String userCommand = update.getMessage().getText();
        SendMessage messageToUser = new SendMessage();
        System.out.println("Commande entrée : " + userCommand);
        switch (userCommand) {
            case "/saymyname":
                messageToUser.setText(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + "... are you the one who knocks ?");
                System.out.println(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName());
                break;
            case "/help":
                messageToUser.setText(
                        "/help - Affiche la liste de commande disponibles par ordre alphabétique.\n" +
                        "/saymyname - Affiche le nom complet de l'utilisateur.\n"
                );
                break;
            default:
                messageToUser.setText("Unknown command");
                break;
        }

        messageToUser.setChatId(update.getMessage().getChatId());
        try {
            execute(messageToUser);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
