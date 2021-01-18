/**
 * Sources :
 * https://github.com/MonsterDeveloper/java-telegram-bot-tutorial/blob/master/Lesson%207/src/database.java
 * https://monsterdeveloper.gitbooks.io/writing-telegram-bots-on-java/content/lesson-7.-creating-users-database-with-mongodb.html
 * https://github.com/MonsterDeveloper/java-telegram-bot-tutorial/blob/master/lesson-7.-creating-users-database-with-mongodb.md
 * https://howtodoinjava.com/mongodb/mongodb-find-documents/
 * https://docs.mongodb.com/guides/server/insert/#what-you-ll-need
 * https://docs.mongodb.com/manual/reference/connection-string/
 * https://howtodoinjava.com/mongodb/java-mongodb-insert-documents-in-collection-examples/
 */

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;

import static com.mongodb.client.model.Filters.eq;

public class DocumentDAO {
    private static DocumentDAO documentDAO;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
    private MongoCollection<Document> collection;

    public static DocumentDAO getInstance() {
        if (documentDAO == null) {
            documentDAO = new DocumentDAO();
        }
        return documentDAO;
    }

    public MongoDatabase getDatabase() {
        mongoClient = new MongoClient(connectionString);
        return mongoClient.getDatabase("bachelorhunterz");
    }

    public boolean checkIfUserExists(String firstname, String lastname, Integer userID, String username, LinkedList<String> courses) {
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        database = getDatabase();
        collection = database.getCollection("user");

        //Si on a arrive à récupérer son ID, c'est qu'il existe.
        long subscribed = collection.count(Document.parse("{id : " + Integer.toString(userID) + "}"));
        if (subscribed == 0) {
            return false;
            //inscriptionUserDatabase(firstname, lastname, userID, username, courses);
        }
        else {
            return true;
        }
    }

    public void inscriptionUserDatabase(String firstname, String lastname, Integer userID, String username, LinkedList<String> courses) {
        Document doc = new Document("firstname", firstname)
                .append("lastname", lastname)
                .append("id", userID)
                .append("username", username)
                .append("courses", courses);
        collection.insertOne(doc);
        System.out.println(firstname + " a bien été inscrit !");
    }

    public Document getUser(String userID) {
        MongoDatabase database = getDatabase();
        MongoCollection<Document> collection = database.getCollection("user");
        return collection.find(eq("id", Integer.parseInt(userID))).first();
    }

    public ObjectId addExercise(String courseName, String teacherInitials, String statment, String correction) {
        MongoDatabase database = getDatabase();
        MongoCollection<Document> collection = database.getCollection("exercise");
        Document document = new Document("course", courseName)
                .append("teacher", teacherInitials)
                .append("statment", statment)
                .append("correction", correction);
        collection.insertOne(document);
        return (ObjectId) document.get("_id");
    }

    public Document getExercise(String exerciseID) {
        MongoDatabase database = getDatabase();
        collection = database.getCollection("exercise");
        return collection.find(new BasicDBObject("_id", new ObjectId(exerciseID))).first();
    }

    public FindIterable<Document> getExercisesByCourse(String course) {
        MongoDatabase database = getDatabase();
        collection = database.getCollection("exercise");
        return collection.find(Filters.eq("course", course));
    }

    public FindIterable<Document> getExercisesByTeacher(String teacherSigle) {
        MongoDatabase database = getDatabase();
        collection = database.getCollection("exercise");
        return collection.find(Filters.eq("teacher", teacherSigle));
    }

    public FindIterable<Document> getExercisesByTeacherAndCourse(String teacher, String course) {
        MongoDatabase database = getDatabase();
        collection = database.getCollection("exercise");
        return collection.find(Filters
                .and(Filters.eq("teacher", teacher), Filters.eq("course", course)));
    }

    public FindIterable<Document> getUserbyUsername(String username) {
        MongoDatabase database = getDatabase();
        collection = database.getCollection("user");
        return collection.find(Filters.eq("username", username));
    }

    public Document getRandomExercise() {
        MongoDatabase database = getDatabase();
        collection = database.getCollection("exercise");
        return collection.aggregate(Arrays.asList(Aggregates.sample(1))).first();

    }

}
