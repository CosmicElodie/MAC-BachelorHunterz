/**
 * Sources :
 * https://www.youtube.com/watch?v=xv-FYOizUSY
 */

import org.bson.types.ObjectId;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

import static java.lang.Math.toIntExact;

public class BachelorHunterzBot extends TelegramLongPollingBot {

    //Permet de répertorier les différentes étapes de la création d'un exercice selon l'utilisateur.
    private LinkedList<String> exerciseDatas = new LinkedList<>();

    //Permet de passer les différentes étapes de création d'exercice
    private boolean isCourseNameCorrect = false;
    private boolean isTeacherNameCorrect = false;
    private boolean isStatementCorrect = false;
    private boolean isCorrectionCorrect = false;
    private boolean isCreatingExercise = false;

    //Liste des sigles des différents cours
    private LinkedList<String> courses = new LinkedList<>(
            Arrays.asList("INF1", "ARO1", "ARO2", "ANA", "MBT", "MAD", "ANG1", "ASD1", "INF2", "ISI",
                    "TIB", "EXP", "PLP", "POO1", "ASD2", "BDR", "GRE", "PST", "SIO", "PCO", "SYE",
                    "MCR", "POO2", "GEN", "SER", "RES", "SLO", "AMT", "MAC", "PRR", "IHM", "TWEB", "SYM"));

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
        int userID = toIntExact(update.getMessage().getChat().getId());
        String username = update.getMessage().getChat().getUserName();
        String firstname = update.getMessage().getChat().getFirstName();
        String lastname = update.getMessage().getChat().getLastName();

        //On check si une commande a bien été envoyée
        if(update.hasMessage() && update.getMessage().hasText()) {
            String userCommand = update.getMessage().getText();

            SendMessage messageToUser = new SendMessage();
            messageToUser.setChatId(update.getMessage().getChatId());

            System.out.println("Commande entrée : " + userCommand);

            //On check si l'utilisateur veut stoper une création d'exo qu'il a initiée.
            if(userCommand.startsWith("/abort")) {
                if(isCreatingExercise) {
                    //on enlève les données de création
                    exerciseDatas.clear();
                    isCourseNameCorrect = false;
                    isTeacherNameCorrect = false;
                    isStatementCorrect = false;
                    isCorrectionCorrect = false;
                    isCreatingExercise = false;

                    //On donne un feedback à l'user
                    messageToUser.setText("Création d'exercice stoppée.");
                }
                else {
                    messageToUser.setText("Cette commande est utile uniquement lors de la création d'un exercice.");
                }
            }

            //On check si l'utilisateur est en train de rentrer les données de création d'un exo qu'il a initiée.
            else if(!(isCourseNameCorrect && isTeacherNameCorrect && isStatementCorrect && isCorrectionCorrect) && isCreatingExercise)
            {
                if(!isTeacherNameCorrect) {
                    String teacherName = userCommand;
                    if(checkIfTeacherNameCorrect(teacherName.toUpperCase())) {
                        isTeacherNameCorrect = true;
                        exerciseDatas.add(teacherName);
                        messageToUser.setText("Bien ! Veuillez maintenant rentrer l'énoncé de votre exercice.\n");
                    } else {
                        messageToUser.setText("Le sigle du professeur doit faire exactement 3 lettres.");
                    }
                }
                else if(!isStatementCorrect) {
                    isStatementCorrect = true;
                    exerciseDatas.add(userCommand);
                    messageToUser.setText("Parfait ! Dernière étape : veuillez maintenant rentrer la correction de votre exercice.\n" +
                            "Si vous n'avez aucune correction à fournir, tapez : Aucune");
                }
                else {
                    isCreatingExercise = false;
                    isCorrectionCorrect = true;
                    exerciseDatas.add(userCommand);
                    addExerciseToDatabase((long) userID);
                    messageToUser.setText("Votre exercice a bien été ajouté à la base de données !");
                }
            }

            //On traite la commande de l'utilisateur
            else {
                switch (userCommand) {
                    case "/saymyname":
                        messageToUser.setText(firstname + " " + lastname + "... are you the one who knocks ?");
                        break;
                    case "/help":
                        messageToUser.setText(
                                "/help - Affiche la liste de commande disponibles par ordre alphabétique.\n" +
                                        "/saymyname - Affiche le nom complet de l'utilisateur.\n" +
                                        "/newexercise <sigleBranche> - initialise une création d'exercice\n" +
                                        "/abort - stop la création d'un exercice\n"
                        );
                        break;
                    default:
                        if (userCommand.startsWith("/newexercise ")) {
                            isCreatingExercise = true;
                            String courseName = userCommand.substring(13).toUpperCase();
                            if(checkIfCourseNameCorrect(courseName)) {
                                //on ajoute l'information
                                exerciseDatas.add(courseName);
                                isCourseNameCorrect = true;
                                messageToUser.setText("Veuillez spécifier le sigle du professeur.\n" + "Exemple : RRH");
                            }
                            else {
                                messageToUser.setText("Le cours spécifié n'existe pas ou est mal orthographié.\n" +
                                        "Exemple : Sigle du cours d'Informatique 1 -> INF1");
                            }
                        }
                        else {
                            messageToUser.setText("La commande rentrée est inexistante.");
                        }
                        break;
                }
            }
            try {
                DocumentDAO.getInstance().check(firstname, lastname, toIntExact(userID), username);
                execute(messageToUser);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check si le nom du cours rentré est correct.
     * @param courseName : le nom du cours rentré par l'user
     * @return true si dans la list de cours enregistrés, false sinon.
     */
    private boolean checkIfCourseNameCorrect(String courseName) {
        return courses.contains(courseName);
    }

    /**
     * Check si le sigle du prof fait bien 3 lettres.
     * @param teacherName : le sigle du professeur
     * @return true si correct, false sinon.
     */
    private boolean checkIfTeacherNameCorrect(String teacherName) {
        return teacherName.length() == 3;
    }

    private void addExerciseToDatabase(Long userID) {
        ObjectId exerciseId = DocumentDAO.getInstance().addExercise(exerciseDatas.get(0), exerciseDatas.get(1), exerciseDatas.get(2), exerciseDatas.get(3));
        exerciseDatas.clear();
    }

}
