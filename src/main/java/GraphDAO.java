/**
 * Sources :
 * https://neo4j.com/developer/java/
 * https://neo4j.com/docs/ogm-manual/current/tutorial/
 * https://neo4j.com/download-thanks-desktop/?edition=desktop&flavour=winstall64&release=1.3.11&offline=true#installation-guide
 */

import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.*;

import java.util.ArrayList;
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
        if (graphDAO == null) {
            graphDAO = new GraphDAO();
        }
        return graphDAO;
    }

    void addExercise(long userID, String exerciseID) {
        StringBuilder args;
        String id;
        List<String> collections = new LinkedList<>();
        collections.add("Exercise");
        id = "_" + exerciseID;
        args = new StringBuilder(id);
        for (String c : collections) {
            args.append(":").append(c);
        }
        performRequest("MERGE (" + args + "{exerciseID:'" + id + "'})");

        id = "_" + userID;
        args = new StringBuilder(id);
        collections = Collections.singletonList("User");

        for (String c : collections) {
            args.append(":").append(c);
        }
        performRequest("MERGE (" + args + "{userID:'" + id + "'})");

        //ajoute le lien entre les deux
        performRequest("MATCH (usr: User{userID:'" + "_" + userID + "' })\n" +
                "MATCH (xrc:Exercise{ exerciseID: '" + "_" + exerciseID + "'})\n" +
                "CREATE (usr)-[:PROPOSED{date:datetime()}]->(xrc)");
    }

    private Result performRequest(String request) {
        try (Session session = driver.session()) {
            return session.run(request);
        }
    }

    public List<String> getExercisesByUser(String userID) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                List<String> exercises = new ArrayList<>();
                Result result = tx.run("MATCH (u:User)-[:PROPOSED]->(e:Exercise) WHERE u.userID = '" + "_" + userID + "' RETURN e.exerciseID;");
                while (result.hasNext()) {
                    exercises.add(result.next().get(0).asString());
                }
                return exercises;
            });
        }
    }

    public List<String> getTopUsers() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                List<String> users = new ArrayList<>();
                Result result = tx.run("MATCH (e:Exercise)<-[:PROPOSED]-(u:User)\n"
                        + "RETURN u.userID, count(*) AS cnt\n"
                        + "ORDER BY cnt DESC LIMIT 10");
                while (result.hasNext()) {
                    users.add(result.next().get(0).asString());
                }
                return users;
            });
        }
    }

    void addLike(String currentUserID, String exerciseIDLiked) {
        String userID = "_" + exerciseIDLiked;
        performRequest("MATCH (u: User), (e:Exercise)" + "\n" +
                "WHERE u.userID = '" + currentUserID + "' AND e.exerciseID = '" + userID + "'" + "\n" +
                "CREATE (u)-[l:LIKE]->(e)");
    }

    public List<String> getUsersByExerciseID(String exerciseID) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                List<String> users = new ArrayList<>();
                Result result = tx.run("MATCH (u:User)--(e:Exercise{ exerciseID:':_" + exerciseID
                        + "'}) RETURN u.userID");
                while (result.hasNext()) {
                    users.add(result.next().get(0).asString());
                }
                return users;
            });
        }
    }

    public List<String> getExercisesLiked(String userID) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                List<String> exercises = new ArrayList<>();
                Result result = tx.run("MATCH (u:User)-[l:LIKE]->(e:Exercise)" + "\n" +
                        "WHERE u.userID = '_" + userID + "'" + "\n" +
                        "RETURN e.exerciseID");
                while (result.hasNext()) {
                    exercises.add(result.next().get(0).asString());
                }
                return exercises;
            });
        }
    }

    public List<String> getExercisesRecommandation(String userID) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                List<String> exercises = new ArrayList<>();
                Result result = tx.run(
                        "MATCH (u1:User{userID: '_" + userID + "'})-[l1:LIKE]->(e:Exercice)<-[:PROPOSED]-(u2:User)-[:PROPOSED]->(e2:Exercice)<-[l3:LIKE]-(u3:User)\n" +
                                "RETURN e2, COUNT(*)\n" +
                                "ORDER BY COUNT(*) DESC\n" +
                                "LIMIT 5");
                while (result.hasNext()) {
                    exercises.add(result.next().get(0).asString());
                }
                return exercises;
            });
        }
    }

    public List<String> getTopUsersWithALikedExercise(String userID) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                List<String> users = new ArrayList<>();
                Result result = tx.run("MATCH (u1:User{userID: '_" + userID + "'})-[l:LIKE]->(e:Exercise)<-[p:PROPOSED]-(u2:User)\n"
                        + "RETURN u2.userID, count(*) AS cnt\n"
                        + "ORDER BY cnt DESC LIMIT 10");
                while (result.hasNext()) {
                    users.add(result.next().get(0).asString());
                }
                return users;
            });
        }
    }

}
