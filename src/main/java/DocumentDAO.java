/**
 * Sources :
 * https://github.com/MonsterDeveloper/java-telegram-bot-tutorial/blob/master/Lesson%207/src/database.java
 * https://howtodoinjava.com/mongodb/mongodb-find-documents/
 */

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.logging.Level;

import static com.mongodb.client.model.Filters.eq;

public class DocumentDAO {
    private static DocumentDAO documentDAO;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
    private MongoCollection<Document> collection;

    private DocumentDAO(){}
    public static DocumentDAO getInstance() {
        if(documentDAO == null) {
            documentDAO = new DocumentDAO();
        }
        return documentDAO;
    }

    public MongoDatabase getDatabase() {
        mongoClient = new MongoClient(connectionString);
        return mongoClient.getDatabase("bachelorhunterz");
    }

    public void check(String firstname, String lastname, Integer userID, String username) {
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        database = getDatabase();
        collection = database.getCollection("user");

        long found = collection.count(Document.parse("{id : " + Integer.toString(userID) + "}"));
        if (found == 0) {
            Document doc = new Document("first_name", firstname)
                    .append("last_name", lastname)
                    .append("id", userID)
                    .append("username", username);
            collection.insertOne(doc);
            //mongoClient.close();
            System.out.println("User not exists in database. Written.");
            //return "user : no_exists";
        } else {
            System.out.println("User exists in database.");
            //mongoClient.close();
            //return "user : exists";
        }
    }

    public Document getUser(String userID) {
        MongoDatabase database = getDatabase( );
        MongoCollection<Document> collection = database.getCollection("user");
        return collection.find(eq("id", Integer.parseInt(userID))).first();
    }

    public ObjectId addExercise(String courseName, String teacherInitials, String statment, String correction){
        MongoDatabase database = getDatabase( );
        MongoCollection<Document> collection = database.getCollection("exercise");
        Document document = new Document("course", courseName)
                .append("teacher", teacherInitials)
                .append("statment", statment)
                .append("correction", correction);
        collection.insertOne(document);
        return (ObjectId) document.get("_id");
    }


    public Document getExercise(String exerciseID) {
        return collection.find(Filters.eq("_id", exerciseID)).first();
    }

    public Document getExerciseByCourse(String course) {
        return collection.find(Filters.eq("course", course)).first();
    }

    public Document getExerciseByTeacher(String teacherInitials) {
        return collection.find(Filters.eq("teacher", teacherInitials)).first();
    }

    public Document getExerciseByTeacherAndCourse (String teacher, String course) {
        return collection.find(Filters
                .and(Filters.eq("teacher", teacher), Filters.eq("course", course))).first();
    }

}
