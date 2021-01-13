/**
 * Sources :
 * https://neo4j.com/developer/java/
 * https://www.baeldung.com/java-neo4j
 */

import org.neo4j.driver.v1.*;

import java.util.List;

public class GraphDAO implements AutoCloseable{
    private final Driver driver;
    private static GraphDAO instance;

    private GraphDAO() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic( "cosmicdarine", "12345"));
    }

    static GraphDAO getInstance() {
        if(instance == null) {
            instance = new GraphDAO();
        }
        return instance;
    }

    @Override
    public void close() {
        driver.close();
    }

    void addNode(String id, List<String> collections) {
        StringBuilder args = new StringBuilder(id);
        for (String c : collections) {
            args.append(":").append(c);
        }
        runRequest("MERGE (" + args + "{name:'" + id + "'})");
    }

    private StatementResult runRequest(String request) {
        try (Session session = driver.session()) {
            return session.run(request);
        }
    }
}
