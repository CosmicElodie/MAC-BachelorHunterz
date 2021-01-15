/**
 * Sources :
 * https://neo4j.com/developer/java/
 * https://neo4j.com/docs/ogm-manual/current/tutorial/
 * https://neo4j.com/download-thanks-desktop/?edition=desktop&flavour=winstall64&release=1.3.11&offline=true#installation-guide
 */

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.fabric.stream.StatementResult;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GraphDAO implements AutoCloseable {
    private GraphDAO graphDAO;
    private final Driver driver;

    @Override
    public void close() throws Exception {
        driver.close();
    }

    private GraphDAO() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic( "cosmicdarine", "bot123"));
    }

    GraphDAO getInstance() {
        if(graphDAO == null) {
            graphDAO = new GraphDAO();
        }
        return graphDAO;
    }

    private void addExercise(long userID, String exerciseID) {
        List<String> collections = new LinkedList<>();
        collections.add("Exercise");
        addNode("_" + exerciseID, collections);

        addNode("_" + userID, Collections.singletonList("User"));
        runRequest("MATCH (usr: User{name:'_" + userID + "' })," +
                "(xrc:Exercise{ statment: '_" + exerciseID + "'})\n" +
                "CREATE (usr)-[:PROPOSED{date:datetime()}]->(xrc)");
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
            return (StatementResult) session.run(request);
        }
    }

    public StatementResult getExerciseByUser(String user) {
        return runRequest("MATCH (u:User)-[:PROPOSED]->(e:Exercise) WHERE u.name = '_" + user + "' RETURN e.statment;");
    }
}
