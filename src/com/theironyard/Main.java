package com.theironyard;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS messages (id IDENTITY, text VARCHAR, time TIMESTAMP, user_id INT)");
    }

    static void insertMessage(Connection conn, String text, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO messages VALUES (NULL, ?, CURRENT_TIMESTAMP(), ?)");
        stmt.setString(1, text);
        stmt.setInt(2, userId);
        stmt.execute();
    }

    static ArrayList<Message> selectMessages(Connection conn) throws SQLException {
        ArrayList<Message> messages = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM messages INNER JOIN users ON messages.user_id = users.id");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("messages.id");
            String text = results.getString("messages.text");
            Timestamp time = results.getTimestamp("messages.time");
            String author = results.getString("users.name");
            Message msg = new Message(id, author, text, time);
            messages.add(msg);
        }
        return messages;
    }

    public static void insertUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?)");
        stmt.setString(1, name);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            return new User(id, name);
        }
        return null;
    }

    public static int count(Connection conn, String table) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
        if (results.next()) {
            return results.getInt(1);
        }
        return 0;
    }

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();

        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);
        Spark.externalStaticFileLocation("public");
        Spark.init();

        if (count(conn, "users") == 0) {
            insertUser(conn, "Alice");
            insertUser(conn, "Bob");
        }

        if (count(conn, "messages") == 0) {
            User alice = selectUser(conn, "Alice");
            User bob = selectUser(conn, "Bob");
            insertMessage(conn, "Hello guys!", alice.id);
            insertMessage(conn, "Hey Alice!", bob.id);
        }

        Spark.get(
                "/messages",
                (request, response) -> {
                    ArrayList<Message> messages = selectMessages(conn);
                    JsonSerializer serializer = new JsonSerializer();
                    MessagesWrapper wrapper = new MessagesWrapper(messages);
                    return serializer.deep(true).serialize(wrapper);
                }
        );

        Spark.post(
                "/messages",
                (request, response) -> {
                    String body = request.body();
                    JsonParser parser = new JsonParser();
                    HashMap<String, String> msg = parser.parse(body);

                    User user = selectUser(conn, msg.get("author"));
                    if (user == null){
                        insertUser(conn, msg.get("author"));
                        user = selectUser(conn, msg.get("author"));
                    }

                    insertMessage(conn, msg.get("text"), user.id);
                    return "";
                }
        );
    }
}
