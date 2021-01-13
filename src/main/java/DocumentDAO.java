import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;


public class DocumentDAO {

    //
    private static DocumentDAO instance;

    //
    private MongoClient mongoclient;


    private MongoDatabase getDatabase() {
        MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
        mongoclient = new MongoClient(connectionString);
        return mongoclient.getDatabase("bachelorhunterz");
    }

    private DocumentDAO() {
    }

    public static DocumentDAO getInstance() {
        if (instance == null) {
            instance = new DocumentDAO();
        }
        return instance;
    }
}
