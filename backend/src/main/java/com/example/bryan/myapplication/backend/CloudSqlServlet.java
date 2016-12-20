package com.example.bryan.myapplication.backend;

/**
 * Created by bryan on 12/4/16.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class CloudSqlServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
            ServletException {
        String path = req.getRequestURI();

        String accX = req.getParameter("name");

        if (path.startsWith("/favicon.ico")) {
            return; // ignore the request for favicon.ico
        }
        // store only the first two octets of a users ip address
        String userIp = req.getRemoteAddr();
        InetAddress address = InetAddress.getByName(userIp);
        if (address instanceof Inet6Address) {
            // nest indexOf calls to find the second occurrence of a character in a string
            // an alternative is to use Apache Commons Lang: StringUtils.ordinalIndexOf()
            userIp = userIp.substring(0, userIp.indexOf(":", userIp.indexOf(":") + 1)) + ":*:*:*:*:*:*";
        } else if (address instanceof Inet4Address) {
            userIp = userIp.substring(0, userIp.indexOf(".", userIp.indexOf(".") + 1)) + ".*.*";
        }

        final String createTableSql = "CREATE TABLE IF NOT EXISTS accvisits3 ( visit_id INT NOT NULL "
                + "AUTO_INCREMENT, user_ip VARCHAR(46) NOT NULL, accX VARCHAR(46), accY VARCHAR(46), " +
                "timestamp DATETIME NOT NULL, PRIMARY KEY (visit_id) )";
        final String createVisitSql = "INSERT INTO accvisits2 (user_ip, accX, accY, timestamp) VALUES (?, ?, ?, ?)";
        final String selectSql = "SELECT user_ip, accX, accy, timestamp FROM accvisits2 ORDER BY timestamp DESC "
                + "LIMIT 10";
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/plain");
        String url;
        if (System
                .getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
            // Check the System properties to determine if we are running on appengine or not
            // Google App Engine sets a few system properties that will reliably be present on a remote
            // instance.
            url = System.getProperty("ae-cloudsql.cloudsql-database-url");
            try {
                // Load the class that provides the new "jdbc:google:mysql://" prefix.
                Class.forName("com.mysql.jdbc.GoogleDriver");
            } catch (ClassNotFoundException e) {
                throw new ServletException("Error loading Google JDBC Driver", e);
            }
        } else {
            // Set the url with the local MySQL database connection url when running locally
            url = System.getProperty("ae-cloudsql.local-database-url");
        }
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement statementCreateVisit = conn.prepareStatement(createVisitSql)) {
            conn.createStatement().executeUpdate(createTableSql);
            statementCreateVisit.setString(1, userIp);
            statementCreateVisit.setString(2, accX);
            statementCreateVisit.setTimestamp(3, new Timestamp(new Date().getTime()));
            statementCreateVisit.executeUpdate();

            try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
                out.print("Last 10 visits:\n");
                while (rs.next()) {
                    String savedIp = rs.getString("user_ip");
                    //String accX = rs.getString("accX");
                    String timeStamp = rs.getString("timestamp");
                    out.print("Time: " + timeStamp + " Addr: " + savedIp + " AccX= "+accX+"\n");
                }
            }
        } catch (SQLException e) {
            throw new ServletException("SQL error", e);
        }

    }
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException  {

        String name = req.getParameter("name");

        String accX = req.getParameter("accX");

        String accY = req.getParameter("accY");

        String accZ = req.getParameter("accZ");

        String time = req.getParameter("time");



        String userIp = req.getRemoteAddr();
        InetAddress address = InetAddress.getByName(userIp);
        if (address instanceof Inet6Address) {
            // nest indexOf calls to find the second occurrence of a character in a string
            // an alternative is to use Apache Commons Lang: StringUtils.ordinalIndexOf()
            userIp = userIp.substring(0, userIp.indexOf(":", userIp.indexOf(":") + 1)) + ":*:*:*:*:*:*";
        } else if (address instanceof Inet4Address) {
            userIp = userIp.substring(0, userIp.indexOf(".", userIp.indexOf(".") + 1)) + ".*.*";
        }

        final String createTableSql = "CREATE TABLE IF NOT EXISTS accTable ( visit_id INT NOT NULL "
                + "AUTO_INCREMENT, user VARCHAR(46) NOT NULL, accX VARCHAR(46), accY VARCHAR(46), " +
                "accZ VARCHAR(46), timestamp LONG NOT NULL, "
                + "PRIMARY KEY (visit_id) )";
        final String createVisitSql = "INSERT INTO accTable (user, accX, accY, accZ, timestamp) VALUES (?, ?, ?, ?, ?)";
        final String selectSql = "SELECT user, accX, accY, accZ, timestamp FROM accTable ORDER BY timestamp DESC "
                + "LIMIT 10";
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/plain");
        String url;
        if (System
                .getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
            // Check the System properties to determine if we are running on appengine or not
            // Google App Engine sets a few system properties that will reliably be present on a remote
            // instance.
            url = System.getProperty("ae-cloudsql.cloudsql-database-url");
            try {
                // Load the class that provides the new "jdbc:google:mysql://" prefix.
                Class.forName("com.mysql.jdbc.GoogleDriver");
            } catch (ClassNotFoundException e) {
                throw new ServletException("Error loading Google JDBC Driver", e);
            }
        } else {
            // Set the url with the local MySQL database connection url when running locally
            url = System.getProperty("ae-cloudsql.local-database-url");
        }
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement statementCreateVisit = conn.prepareStatement(createVisitSql)) {
            conn.createStatement().executeUpdate(createTableSql);
            statementCreateVisit.setString(1, name);
            statementCreateVisit.setString(2, accX);
            statementCreateVisit.setString(3, accY);
            statementCreateVisit.setString(4, accZ);
            statementCreateVisit.setString(5, time);
            statementCreateVisit.executeUpdate();

            try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
                out.print("Last 10 visits:\n");
                while (rs.next()) {
                    String savedIp = rs.getString("user");
                    //String accX = rs.getString("accX");
                    String timeStamp = rs.getString("timestamp");
                    String accYFinal = rs.getString("accY");
                    out.print("Time: " + timeStamp + " Addr: " + savedIp + " AccX= "+ name+ " accY= "+accYFinal+"\n");
                }
            }
        } catch (SQLException e) {
            throw new ServletException("SQL error", e);
        }
    }
}