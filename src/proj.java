/**
 * Created by jaydhanota on 11/25/16.
 */


import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class proj {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/project";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "Tm4tD8q9";


    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.println("Connecting to database...");
        int swVal = 0;
        while (swVal != -1) {
            System.out.println("=====================================================");
            System.out.println("           MENU             ");
            System.out.println("=====================================================");
            System.out.println(" Options:                    ");
            System.out.println("        -1. EXIT");
            System.out.println("        1. List all team information");
            System.out.println("        2. List all players from a given team");
            System.out.println("        3. List remaining matches");
            System.out.println("        4. Add a match result");
            System.out.println("        5. Trade player");
            System.out.println("        6. List team results for a particular month");
            System.out.println("        7. List current result standings");
            System.out.println("=====================================================");
            System.out.println("Make your choice: ");
            swVal = scan.nextInt();

            switch (swVal) {
                case 1:
                    System.out.println("Listing all teams...");
                    listTeamInfo();
                    break;
                case 2:
                    listPlayers();
                    break;
                case 3:
                    listRemainingMatches();
                    break;
                case 4:
                    addMatchResults();
                    break;
                case 5:
                    tradePlayer();
                    break;
                case 6:
                    listNMonthResults();
                    break;
                case 7:
                    getStandings();
                    break;
                case -1:
                    System.out.println("Exit selected");
                    break;
                default:
                    System.out.println("Invalid selection");
                    break; // This break is not really necessary
            }
        }

        System.out.println("Goodbye!");
    }//end main

    // Handles loggging all SQL queries
    public static void sqlLog(String query) {
        Logger logger = Logger.getLogger("MyLog");
        FileHandler fh;
        logger.setUseParentHandlers(false);
        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler("sqlLog.txt", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            logger.info(query);
            fh.close();

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // List current league standings
    public static void getStandings(){
        Connection conn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        Map<String,Integer> teams = new HashMap<String,Integer>();
        //Execute a query
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();

            String sqlTeam;
            sqlTeam = "SELECT T_name FROM team ";
            sqlLog(sqlTeam);
            ResultSet rsTeam = stmt2.executeQuery(sqlTeam);

            while (rsTeam.next()) { // grab all teams and store into map, initialize to 0
                teams.put(rsTeam.getString("T_name"), 0);
            }

            String sql;
            sql = "SELECT home_team_name, visiting_team_name, home_team_goals, visiting_team_goals FROM game " +
                    "WHERE home_team_goals IS NOT NULL AND visiting_team_goals IS NOT NULL";
            sqlLog(sql);
            ResultSet rs = stmt.executeQuery(sql);


            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String hTeam = rs.getString("home_team_name");
                String vTeam = rs.getString("visiting_team_name");
                int hScore = rs.getInt("home_team_goals");
                int vScore = rs.getInt("visiting_team_goals");

                if(hScore > vScore){ // HOME TEAM WINS
                    teams.put(hTeam, teams.get(hTeam) + 3);
                }
                else if(hScore < vScore){ // VISITING TEAM WINS
                    teams.put(vTeam, teams.get(vTeam) + 3);
                }
                else { // TIE
                    teams.put(hTeam, teams.get(hTeam) + 1);
                    teams.put(vTeam, teams.get(vTeam) + 1);
                }
            }

            Map <String,Integer> sortedTeams = sortByValues(teams);
            int pos = 1;
            for (Map.Entry<String, Integer> entry : sortedTeams.entrySet()) {
                System.out.println(pos + ". "+entry.getKey()+" : "+entry.getValue());
                pos++;
            }
            //Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
    }

    // Responsible for returning a map that is sorted. Used to return a list of teams with points in descending order
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =  new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    // List results for a particular month for a particular team
    public static void listNMonthResults(){
        Connection conn = null;
        Statement stmt = null;
        Scanner scan = new Scanner(System.in);
        String month;
        String tName;
        //Execute a query
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            System.out.println("Please enter which team you would like results for: ");
            tName = scan.nextLine();

            System.out.println("Please enter any month as integer: ");
            month = scan.nextLine();

            String sql;
            sql = "SELECT home_team_name AS tName, home_team_goals AS tScore, visiting_team_name AS oName, visiting_team_goals AS oScore , mdate " +
                    "FROM game " +
                    "WHERE home_team_name = '" + tName + "' AND mdate LIKE '2016-"+month+"-%'" +
                    " UNION ALL " +
                    "SELECT visiting_team_name AS tName, visiting_team_goals AS tScore, home_team_name AS oName, home_team_goals AS oScore, mdate " +
                    "FROM game " +
                    "WHERE visiting_team_name = '" + tName + "' AND mdate LIKE '2016-"+month+"-%'";
            sqlLog(sql);
            ResultSet rs = stmt.executeQuery(sql);


            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                //String hTeam = rs.getString("home_team_name");
                String oTeam = rs.getString("oName");
                String mDate = rs.getString("mdate");
                int tScore = rs.getInt("tScore");
                int oScore = rs.getInt("oScore");

                //Display values
                System.out.print("Team: " + tName);
                System.out.print(", Opponent: " + oTeam);
                System.out.print(", Team score: " + tScore);
                System.out.print(", Opponent score: " + oScore);
                System.out.println(", Date: " + mDate);

            }
            //Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try

    }

    // Trade a player to a different team
    public static void tradePlayer(){
        Connection conn = null;
        Statement stmt = null;
        Scanner scan = new Scanner(System.in);
        String fName;
        String lName;
        String newTeam;


        System.out.println("Please enter the player's fist name: ");
        fName = scan.nextLine();

        System.out.println("Please enter the player's last name: ");
        lName = scan.nextLine();

        System.out.println("Please enter the new team name: ");
        newTeam = scan.nextLine();

        //Execute a query
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql;
            sql = "UPDATE player SET T_name = '" + newTeam + "' WHERE fname = + '" + fName + "' AND lname = '" + lName + "'" ;
            sqlLog(sql);

            if(stmt.executeUpdate(sql) == 0) {
                System.out.println("Sorry, player or team information not found.");
                return;
            }

            System.out.println("Player has been successfully traded!");

            //Clean-up environment
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
    }

    // List last N matches played in the league
    public static void listRemainingMatches(){
        Connection conn = null;
        Statement stmt = null;
        //Execute a query
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String sql;
            sql = "SELECT * FROM game WHERE home_team_goals IS NULL AND visiting_team_goals IS NULL ";
            sqlLog(sql);
            ResultSet rs = stmt.executeQuery(sql);


            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String hTeam = rs.getString("home_team_name");
                String vTeam = rs.getString("visiting_team_name");
                String mDate = rs.getString("mdate");

                //Display values
                System.out.print("Home Team: " + hTeam);
                System.out.print(", Visiting Team: " + vTeam);
                System.out.println(", Date: " + mDate);

            }
            //Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try

    }
    
    // ADD A MATCH RESULT TO THE DB - MODIFICATION
    public static void addMatchResults() {
        Connection conn = null;
        Statement stmt = null;
        Scanner scan = new Scanner(System.in);
        String hTeam;
        String vTeam;
        String date;
        int hGoal;
        int vGoal;

        System.out.println("Please enter the date of the match: ");
        date = scan.nextLine();

        System.out.println("Please enter the home team name: ");
        hTeam = scan.nextLine();

        System.out.println("Please enter the visiting team name: ");
        vTeam = scan.nextLine();

        System.out.println("How many goals did " + hTeam + " score? : ");
        hGoal = scan.nextInt();

        System.out.println("How many goals did " + vTeam + " score? : ");
        vGoal = scan.nextInt();

        if(hGoal < 0 || vGoal < 0){
            System.out.println("Sorry, invalid scores");
            return;
        }

        //Execute a query
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql;
            sql = "UPDATE game SET home_team_goals = '" + hGoal + "', visiting_team_goals = '" + vGoal + "' WHERE home_team_goals IS NULL AND" +
                    " home_team_name = '" + hTeam + "' AND mdate = '" + date + "'";
            sqlLog(sql);


            if(stmt.executeUpdate(sql) == 0) {
                System.out.println("Sorry, no match found.");
            }

            //Clean-up environment
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
    }

    // list all employees from a team given by the user
    public static void listPlayers() {
        Connection conn = null;
        Statement stmt = null;
        Scanner scan = new Scanner(System.in);
        String teamName;

        System.out.println("Please enter team name: ");
        teamName = scan.nextLine();

        //Execute a query
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT fname,lname, player.T_name " +
                    "FROM player INNER JOIN team ON player.T_name = team.T_name WHERE player.T_name ='" + teamName + "'";
            sqlLog(sql);

            ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                System.out.println("Sorry, no matching results...");
            }
            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String tname = rs.getString("T_name");

                //Display values
                System.out.print("Team Name: " + tname);
                System.out.println(", NAME: " + fname + " " + lname);

            }
            //Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try

    }

    // list all teams and their information. Also list their manager
    public static void listTeamInfo() {
        Connection conn = null;
        Statement stmt = null;
        //Execute a query
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT fname,lname, team.T_name, stadium_name, city " +
                    "FROM team JOIN manager ON team.manager_id = manager.manager_id";
            sqlLog(sql);
            ResultSet rs = stmt.executeQuery(sql);


            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String T_name = rs.getString("T_name");
                String stadiumName = rs.getString("stadium_name");
                String city = rs.getString("city");
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");

                //Display values
                System.out.print("Team Name: " + T_name);
                System.out.print(", Stadium: " + stadiumName);
                System.out.print(", City: " + city);
                System.out.println(", Manager: " + fname + " " + lname);

            }
            //Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
    }
}//end proj

