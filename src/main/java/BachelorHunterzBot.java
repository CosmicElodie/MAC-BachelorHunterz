/**
 * Sources :
 * https://www.youtube.com/watch?v=xv-FYOizUSY
 */

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
        long chatID = update.getMessage().getChatId();

        //On check si une commande a bien été envoyée
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userCommand = update.getMessage().getText();

            SendMessage messageToUser = new SendMessage();
            messageToUser.setChatId(update.getMessage().getChatId());

            System.out.println("Commande entrée : " + userCommand);

            //On check si l'utilisateur veut stoper une création d'exo qu'il a initiée.
            if (userCommand.startsWith("/abort")) {
                if (isCreatingExercise) {
                    //on enlève les données de création
                    exerciseDatas.clear();
                    isCourseNameCorrect = false;
                    isTeacherNameCorrect = false;
                    isStatementCorrect = false;
                    isCorrectionCorrect = false;
                    isCreatingExercise = false;

                    //On donne un feedback à l'user
                    messageToUser.setText("Création d'exercice stoppée.");
                } else {
                    messageToUser.setText("Cette commande est utile uniquement lors de la création d'un exercice.");
                }
            }

            //On check si l'utilisateur est en train de rentrer les données de création d'un exo qu'il a initiée.
            else if (!(isCourseNameCorrect && isTeacherNameCorrect && isStatementCorrect && isCorrectionCorrect) && isCreatingExercise) {
                if (!isTeacherNameCorrect) {
                    String teacherName = userCommand;
                    if (checkIfTeacherNameCorrect(teacherName.toUpperCase())) {
                        isTeacherNameCorrect = true;
                        exerciseDatas.add(teacherName);
                        messageToUser.setText("Bien ! Veuillez maintenant rentrer l'énoncé de votre exercice.\n");
                    } else {
                        messageToUser.setText("Le sigle du professeur doit faire exactement 3 lettres.");
                    }
                } else if (!isStatementCorrect) {
                    isStatementCorrect = true;
                    exerciseDatas.add(userCommand);
                    messageToUser.setText("Parfait ! Dernière étape : veuillez maintenant rentrer la correction de votre exercice.\n" +
                            "Si vous n'avez aucune correction à fournir, tapez : Aucune");
                } else {
                    exerciseDatas.add(userCommand);
                    addExerciseToDatabases((long) userID);
                    isCourseNameCorrect = false;
                    isTeacherNameCorrect = false;
                    isStatementCorrect = false;
                    isCorrectionCorrect = false;
                    isCreatingExercise = false;
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
                                "/help - affiche la liste de commande disponibles par ordre alphabétique.\n" +
                                        "/saymyname - affiche le nom complet de l'utilisateur.\n" +
                                        "/newexercise <sigleBranche> - initialise une création d'exercice.\n" +
                                        "/abort - stop la création d'un exercice.\n" +
                                        "/exercisesbyuser <username> - affiche les exercices rentrés par l'utilisateur spécifié.\n" +
                                        "/exercisesbyteacher <SIGLE_TEACHER> - affiche les exercices créés par le professeur spécifié.\n" +
                                        "/exercisesbycourse <SIGLE_COURSE> - affiche les exercices créés pour le cours spécifié.\n" +
                                        "/exercisesbyteacherandcourse <SIGLE_PROFESSEUR> <SIGLE_COURS> - affiche les exercices créés par un professeur pour un cours spécifié.\n"
                        );
                        break;
                    default:
                        if (userCommand.startsWith("/newexercise ")) {
                            isCreatingExercise = true;
                            String courseName = userCommand.substring(13).toUpperCase();
                            if (checkIfCourseNameCorrect(courseName)) {
                                //on ajoute l'information
                                exerciseDatas.add(courseName);
                                isCourseNameCorrect = true;
                                messageToUser.setText("Veuillez spécifier le sigle du professeur.\n" + "Exemple : RRH");
                            } else {
                                messageToUser.setText("Le cours spécifié n'existe pas ou est mal orthographié.\n" +
                                        "Exemple : Sigle du cours d'Informatique 1 -> INF1");
                            }
                        } else if (userCommand.startsWith("/exercisesbyuser ")) {
                            String specifiedUser = userCommand.substring(17);
                            if (specifiedUser.length() > 0) {
                                messageToUser.setText(getExercisesByUser(specifiedUser));


                                //On permet de liker cet user
                                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                                List<List<InlineKeyboardButton>> rowsInline = new LinkedList<>();
                                List<InlineKeyboardButton> rowInline = new LinkedList<>();
                                rowInline.add(new InlineKeyboardButton().setText("Suivre cet utilisateur").setCallbackData("Suivre" + userID + " _" + specifiedUser));
                                rowsInline.add(rowInline);
                                markup.setKeyboard(rowsInline);
                                messageToUser.setReplyMarkup(markup);

                            } else {
                                messageToUser.setText("Veuillez spécifier le pseudo d'un utilisateur");
                            }
                        } else if (userCommand.startsWith("/exercisesbyteacher ")) {
                            String specifiedTeacher = userCommand.substring(20);
                            if (specifiedTeacher.length() == 3) {
                                messageToUser.setChatId(chatID).setText("Exercices donnés par professeur " + specifiedTeacher + ":\n\n" +
                                        getExercisesByTeacher(specifiedTeacher));
                            } else {
                                messageToUser.setText("Erreur : le sigle du professeur ne doit pas excéder 3 lettres.\n" +
                                        "Exemple : RRH");
                            }
                        } else if (userCommand.startsWith("/exercisesbycourse ")) {
                            String specifiedCourse = userCommand.substring(19);
                            if (courses.contains(specifiedCourse)) {
                                messageToUser.setChatId(chatID).setText("Exercices pour le cours de " + specifiedCourse + ":\n\n"
                                        + getExercisesByCourse(specifiedCourse));
                            } else {
                                messageToUser.setText("Le cours entré est inexistant.");
                            }
                        } else if (userCommand.startsWith("/exercisesbyteacherandcourse ")) {
                            String speciefiedTeacher = userCommand.substring(29, 32);
                            String specifiedCourse = userCommand.substring(33);
                            System.out.println("DEBUG : " + speciefiedTeacher + "-" + specifiedCourse + "-");
                            if (courses.contains(specifiedCourse)) {
                                messageToUser.setChatId(chatID).setText("Exercices données par le professeur " + speciefiedTeacher + " pour le cours de " + specifiedCourse + ":\n\n"
                                        + getExercisesByTeacherAndCourse(speciefiedTeacher, specifiedCourse));
                            } else {
                                messageToUser.setText("Le cours entré est inexistant.");
                            }
                        } else {
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
     *
     * @param courseName : le nom du cours rentré par l'user
     * @return true si dans la list de cours enregistrés, false sinon.
     */
    private boolean checkIfCourseNameCorrect(String courseName) {
        return courses.contains(courseName);
    }

    /**
     * Check si le sigle du prof fait bien 3 lettres.
     *
     * @param teacherName : le sigle du professeur
     * @return true si correct, false sinon.
     */
    private boolean checkIfTeacherNameCorrect(String teacherName) {
        return teacherName.length() == 3;
    }

    /**
     * Permet d'ajouter les exercises créés dans MongoDB et Neo4j
     *
     * @param userID : l'utilisateur qui a rentré l'exercice.
     */
    private void addExerciseToDatabases(Long userID) {
        //On ajoute l'exercice dans MongoDB
        ObjectId exerciseId = DocumentDAO.getInstance().addExercise(exerciseDatas.get(0), exerciseDatas.get(1),
                exerciseDatas.get(2), exerciseDatas.get(3));

        //On ajoute l'exercice dans Neo4J
        GraphDAO.getInstance().addExercise(userID, exerciseId.toString());

        //On nettoie la variable locale au bot
        exerciseDatas.clear();
    }

    /**
     * Permet de récupérer les exercices rentrés par l'utilisateur
     *
     * @param userID : l'id de l'utilisateur
     * @return les exercices rentrés par l'utilisateur via telegram.
     */
    /*
    private String getExerciseByUser(String userID) {
        StringBuilder exercises = new StringBuilder();
        Result statementResult = GraphDAO.getInstance().getExerciseByUser(userID);

        while(statementResult.hasNext()) {
            Record record = statementResult.next();
            String exerciseID = record.get(0).asString().substring(1);
            Document exercise = DocumentDAO.getInstance().getExercise(exerciseID);
            exercises.append(exerciseID).append("\t\t").append(exercise.get("statment")).append("\n");
        }
        if(exercises.toString().isEmpty()) {
            exercises.append("Aucun exercice trouvé");
        }
        return exercises.toString();
    }
    */
    private String getExercisesByUser(String userID) {
        StringBuilder exercises = new StringBuilder();
        Result result = GraphDAO.getInstance().getExercisesByUser(userID);

        while (result.hasNext()) {
            Record record = result.next();
            String exerciseID = record.get(0).asString().substring(1);
            Document exercise = DocumentDAO.getInstance().getExercise(exerciseID);
            exercises.append(exerciseID).append("\t\t").append(exercise.get("name")).append("\n");
        }
        if (exercises.toString().equals("")) { //isEmpty
            exercises.append("Aucun exercice trouvé pour cet utilisateur");
        } else {
            exercises.insert(0, "id \t\t\t\t\t\t\t\t\t nom\n");
        }
        return exercises.toString();
    }

    //OK
    private String getExercisesByTeacher(String teacherName) {
        StringBuilder exercisesFound = new StringBuilder();
        FindIterable<Document> exercisesPointer = DocumentDAO.getInstance().getExercisesByTeacher(teacherName);
        for (Document exercise : exercisesPointer) {
            exercisesFound
                    .append("COURS : ").append(exercise.get("course")).append("\n\n")
                    .append("- ÉNONCÉ -\n").append(exercise.get("statment")).append("\n\n")
                    .append("- CORRECTION -\n").append(exercise.get("correction")).append("\n\n")
                    .append("- - - - - - - - -\n\n");
        }
        if (exercisesFound.toString().equals("")) {
            exercisesFound.append("Aucun exercice trouvé pour ce professeur.");
        }
        return exercisesFound.toString();
    }

    private String getExercisesByCourse(String courseName) {
        StringBuilder exercisesFound = new StringBuilder();
        FindIterable<Document> exercisesPointer = DocumentDAO.getInstance().getExercisesByCourse(courseName);
        for (Document exercise : exercisesPointer) {
            exercisesFound
                    .append("PROFESSEUR : ").append(exercise.get("teacher")).append("\n\n")
                    .append("- ÉNONCÉ -\n").append(exercise.get("statment")).append("\n\n")
                    .append("- CORRECTION -\n").append(exercise.get("correction")).append("\n\n")
                    .append("- - - - - - - - -\n\n");
        }
        if (exercisesFound.toString().equals("")) {
            exercisesFound.append("Aucun exercice trouvé pour le cours de " + courseName + ".\n" +
                    "N'hésitez pas à rajouter un exercie avec la commande :\n /newexercise <sigle_cours>\n !!!");
        }
        return exercisesFound.toString();
    }

    private String getExercisesByTeacherAndCourse(String teacher, String courseName) {
        StringBuilder exercisesFound = new StringBuilder();
        FindIterable<Document> exercisesPointer = DocumentDAO.getInstance().getExercisesByTeacherAndCourse(teacher, courseName);
        for (Document exercise : exercisesPointer) {
            exercisesFound
                    .append("- ÉNONCÉ -\n").append(exercise.get("statment")).append("\n\n")
                    .append("- CORRECTION -\n").append(exercise.get("correction")).append("\n\n")
                    .append("- - - - - - - - -\n\n");
        }
        if (exercisesFound.toString().equals("")) {
            exercisesFound.append("Aucun exercice trouvé pour le cours de " + courseName + ".\n" +
                    "N'hésitez pas à rajouter un exercie avec la commande :\n /newexercise <sigle_cours>\n !!!");
        }
        return exercisesFound.toString();
    }

}
