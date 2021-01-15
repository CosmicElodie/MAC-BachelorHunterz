/**
 * Sources :
 * https://neo4j.com/developer/java/
 * https://neo4j.com/docs/ogm-manual/current/tutorial/
 */

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

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
}
