/**
 * Sources :
 * https://github.com/MonsterDeveloper/java-telegram-bot-tutorial/blob/master/Lesson%207/src/database.java
 * https://howtodoinjava.com/mongodb/mongodb-find-documents/
 */

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class DocumentDAO {
    private DocumentDAO documentDAO;
    private MongoClient mongoClient;
    MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("bachelorhunterz");
    MongoCollection<Document> collection = database.getCollection("exercise");

    public DocumentDAO getInstance() {
        if(documentDAO == null) {
            documentDAO = new DocumentDAO();
        }
        return documentDAO;
    }

    public MongoDatabase getDatabase(String databaseName) {
        mongoClient = new MongoClient(connectionString);
        return mongoClient.getDatabase(databaseName);
    }

    public void check(String firstname, String lastname, int userID, String username) {
        collection = database.getCollection("user");

        long found = collection.count(Document.parse("{id : " + Integer.toString(userID) + "}"));
        if (found == 0) {
            Document doc = new Document("firstname", firstname)
                    .append("lastname", lastname)
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
        MongoCollection<Document> collection = database.getCollection("user");
        return collection.find(eq("id", Integer.parseInt(userID))).first();
    }

    public ObjectId addExercise(String courseName, String teacherInitials, String statment, String correction){
        Document document = new Document("course", courseName)
                .append("teacher", teacherInitials)
                .append("statment", statment)
                .append("correction", correction);
        collection.insertOne(document);
        return (ObjectId) document.get("_id");
    }

    public FindIterable<Document> getExercise(String exerciseID) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", exerciseID);
        FindIterable<Document> cursor = collection.find(query);
        return cursor;
    }

    public FindIterable<Document> getExerciseByCourse(String course) {
        BasicDBObject query = new BasicDBObject();
        query.put("course", course);
        FindIterable<Document> cursor = collection.find(query);
        return cursor;
    }

    public FindIterable<Document> getExerciseByTeacher(String teacherInitials) {
        BasicDBObject query = new BasicDBObject();
        query.put("teacher", teacherInitials);
        FindIterable<Document> cursor = collection.find(query);
        return cursor;
    }

}
