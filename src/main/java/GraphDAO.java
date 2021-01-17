/**
 * Sources :
 * https://neo4j.com/developer/java/
 * https://neo4j.com/docs/ogm-manual/current/tutorial/
 * https://neo4j.com/download-thanks-desktop/?edition=desktop&flavour=winstall64&release=1.3.11&offline=true#installation-guide
 */

import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GraphDAO implements AutoCloseable {
    private static GraphDAO graphDAO;
    private final Driver driver;

    @Override
    public void close() throws Exception {
        driver.close();
    }

    private GraphDAO() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "bot123"));
    }

    static GraphDAO getInstance() {
        if(graphDAO == null) {
            graphDAO = new GraphDAO();
        }
        return graphDAO;
    }

    void addExercise(long userID, String exerciseID) {
        List<String> collections = new LinkedList<>();
        collections.add("Exercise");
        addNode("_" + exerciseID, collections);

        addNode("_" + userID, Collections.singletonList("User"));
        runRequest("MATCH (usr: User{name:'" + userID + "' })," +
                "(xrc:Exercise{ statment: '" + exerciseID + "'})\n" +
                "CREATE (usr)-[:PROPOSED{date:datetime()}]->(xrc)");
    }

    void addNode(String id, List<String> collections) {
        StringBuilder args = new StringBuilder(id);
        for (String c : collections) {
            args.append(":").append(c);
        }
        runRequest("MERGE (" + args + "{name:'" + id + "'})");
    }

    private Result runRequest(String request) {
        try (Session session = driver.session()) {
            return session.run(request);
        }
    }

    public Result getExercisesByUser(String user) {
        try (Session session = driver.session()) {
            return session.run("MATCH (u:User)-[:PROPOSED]->(e:Exercise) WHERE u.name = '" + user + "' RETURN e.statment;");
        }
      }
}
