package com.amazon.example.resolver.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class DBUtil {

    private Connection connection;
    private static final Logger logger = LogManager.getLogger(DBUtil.class);
    private static final String CREATE_COMMENTS_TBL = "CREATE TABLE IF NOT EXISTS comments (" +
                                                        "id        VARCHAR(64) NOT NULL," +
                                                        "author    VARCHAR(128) NOT NULL," +
                                                        "postId    VARCHAR(64) NOT NULL," +
                                                        "content   VARCHAR(255) NOT NULL," +
                                                        "upvotes   INT NOT NULL," +
                                                        "downvotes INT NOT NULL," +
                                                        "PRIMARY KEY(id)," +
                                                        "FOREIGN KEY(postId) REFERENCES posts(id))";

    private static final String CREATE_POSTS_TBL = "CREATE TABLE IF NOT EXISTS posts (" +
                                                        "id        VARCHAR(64) NOT NULL," +
                                                        "author    VARCHAR(128) NOT NULL," +
                                                        "content   VARCHAR(255) NOT NULL," +
                                                        "views   INT NOT NULL," +
                                                        "PRIMARY KEY(id))";


    public DBUtil(JsonConverter jsonConverter) {
        try {
            String secretString = System.getenv("RDS_SECRET");
            String secret = SecretUtil.getValue(secretString);
            Map<String, Object> secertMap = jsonConverter.fromJson(secret, Map.class);
            String connString = "jdbc:"+ secertMap.get("engine") + "://"
                    + secertMap.get("host") + ":" + ((Double)secertMap.get("port")).intValue()
                    + "/" + secertMap.get("dbname").toString();
            logger.info("Conn String :: " + connString);
            this.connection = DriverManager.getConnection(connString,
                    secertMap.get("username").toString(), secertMap.get("password").toString());
            logger.info("DB Connection Created Successfully");
        } catch (Exception e) {
            logger.error("Error initializing DB connection", e);
        }
        if(connection != null) {
            createTables();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    private void createTables() {
        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate(CREATE_POSTS_TBL);
            statement.executeUpdate(CREATE_COMMENTS_TBL);
            logger.info("After Create Comments Table");
        } catch (SQLException e) {
            logger.error("Cannot create DB tables ", e);
        }
    }

}
