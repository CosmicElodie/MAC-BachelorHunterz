/**
 * Sources :
 * https://www.youtube.com/watch?v=xv-FYOizUSY
 */

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static java.lang.StrictMath.toIntExact;

public class BachelorHunterzBot extends TelegramLongPollingBot {

    private DocumentDAO documentDAO = new DocumentDAO();
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

        //On récupère les infos utilisateur
        int userID = Math.toIntExact(update.getMessage().getChat().getId());
        String username = update.getMessage().getChat().getUserName();
        String firstname = update.getMessage().getChat().getFirstName();
        String lastname = update.getMessage().getChat().getLastName();

        //On check si une commande a bien été envoyée
        if(update.hasMessage() && update.getMessage().hasText()) {
            String userCommand = update.getMessage().getText();
            SendMessage messageToUser = new SendMessage();
            System.out.println("Commande entrée : " + userCommand);

            documentDAO.getInstance().check(firstname, lastname, userID, username);
            switch (userCommand) {
                case "/saymyname":
                    messageToUser.setText(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + "... are you the one who knocks ?");
                    System.out.println(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName());
                    break;
                case "/help":
                    messageToUser.setText(
                            "/help - Affiche la liste de commande disponibles par ordre alphabétique.\n" +
                            "/saymyname - Affiche le nom complet de l'utilisateur.\n" +
                            "/newexercise <branche> - initialise une création d'exercice\n"
                    );
                    break;
                default:
                    if(userCommand.startsWith("/newexercise ")) {
                        messageToUser.setChatId(update.getMessage().getChatId()).setText("TO_COMPLETE");
                    }
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

    private void newExercise(int userID, String className) {

    }
}
