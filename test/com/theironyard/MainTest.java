package com.theironyard;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by zach on 10/17/16.
 */
public class MainTest {
    Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice");
        User user = Main.selectUser(conn, "Alice");
        conn.close();
        assertTrue(user != null);
        assertTrue(user.name.equals("Alice"));
    }

    @Test
    public void testMessages() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice");
        User user = Main.selectUser(conn, "Alice");
        Main.insertMessage(conn, "Hello, world!", user.id);
        Main.insertMessage(conn, "Hello, world again!", user.id);
        ArrayList<Message> msgs = Main.selectMessages(conn);
        conn.close();
        assertTrue(msgs.size() == 2);
    }
}