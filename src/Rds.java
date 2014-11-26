import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;


public class Rds {
    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String DB_URL = "jdbc:mysql://tweetmap.crsarl5br9bw.us-east-1.rds.amazonaws.com:3306/tweet";
    public Connection conn = null;
    public String table = null;

    public Rds(String password){
        String pass = password;

    	try {
            Class.forName("com.mysql.jdbc.Driver");

            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, "xiaojing", pass);
            System.out.println("connection finished");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void createTable(String name) {
    	this.table = name;
        System.out.println("Creating table in given database...");
        Statement stmt;
        try {
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS " +name+ " "+
                    "(id_str VARCHAR(255) not NULL, " +
                    " keyword VARCHAR(20), " +
                    " user VARCHAR(255), " +
                    " text VARCHAR(2000), " +
                    " latitude VARCHAR(255), " +
                    " longitude VARCHAR(255), " +
                    " created_at VARCHAR(255), " +
                    " sentiment_exist TINYINT(1), " +
                    " sentiment DOUBLE(4,3), " +
                    " PRIMARY KEY ( id_str ))";
            stmt.executeUpdate(sql);
            stmt.close();
            System.out.println("Finished creating table");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void deleteTable(String name) {
        System.out.println("Deleting table in given database...");
        Statement stmt;
        try {
            stmt = conn.createStatement();
            String sql = "DROP TABLE " + name;
            stmt.executeUpdate(sql);
            stmt.close();
            System.out.println("Finished deleting table");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    static HashMap<String, String> map = new HashMap<String, String>();

    private static void createMap() {
        map.put("Jan", "01");
        map.put("Feb", "02");
        map.put("Mar", "03");
        map.put("Apr", "04");
        map.put("May", "05");
        map.put("Jun", "06");
        map.put("Jul", "07");
        map.put("Aug", "08");
        map.put("Sep", "09");
        map.put("Oct", "10");
        map.put("Nov", "11");
        map.put("Dec", "12");
    }

    private static String convertTime(String date) {
        String processed = null;

        if(map.size()==0){
            createMap();
        }

        // hard coded according to tweet format
        String[] s = date.split(" ");
        String year = s[5];
        String month = s[1];
        String day = s[2];
        String time = s[3];
        processed = year+"-"+map.get(month)+"-"+day+" "+time;

        Timestamp timestamp = Timestamp.valueOf(processed);
        return String.valueOf(timestamp.getTime());
    }

    public String select() {
        String sql = "SELECT * FROM "+table;
//    	String selectExpression = "select * from " + table + " where created_at > '"+start+"' and created_at < '"+end+"'";
        StringBuilder sb = new StringBuilder();
        Statement stmt;
        int count = 0;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
            	String id = rs.getString("id_str");

                String text = rs.getString("text");
                String keyword = rs.getString("keyword");
                String user = rs.getString("user");
                String c1 = rs.getString("latitude");
                String c2 = rs.getString("longitude");
                String time = rs.getString("created_at");
                Boolean sen = rs.getBoolean("sentiment_exist");

                System.out.println("Keyword:" + keyword + "User:" + user + " Text:" + text+ " Created_at:"+ time + " id:"+id+ " sen:" + sen + " C1:" + c1);
                count++;
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println(count);
        return sb.toString();

    }

    
    public String selectTimeRange(String table, String start, String end) {
        String sql = "SELECT * FROM "+table+" WHERE created_at < '"+end+"' AND created_at > '"+start+"'";
//    	String selectExpression = "select * from " + table + " where created_at > '"+start+"' and created_at < '"+end+"'";
        StringBuilder sb = new StringBuilder();
        Statement stmt;
        int count = 0;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
            	String id = rs.getString("id_str");

            	String text = rs.getString("text");
            	String keyword = rs.getString("keyword");
            	String user = rs.getString("user");
            	String c1 = rs.getString("latitude");
            	String c2 = rs.getString("longitude");
            	String time = rs.getString("created_at");
            	Boolean sen = rs.getBoolean("sentiment_exist");

            	System.out.println("Keyword:" + keyword + "User:" + user + " Text:" + text+ " Created_at:"+ time + " id:"+id+ " sen:" + sen + " C1:" + c1);

                count++;
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println(count);
        return sb.toString();

    }

    public void insert(String id_str, String keyword, String user, String text, String latitude, String longitude, String created_at) {
        System.out.println("Inserting into table " +table );
        String sql = "INSERT INTO " +table + " VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps;

        try {
            ps = conn.prepareStatement(sql);
            String timestamp = convertTime(created_at);
   
            ps.setString(1, id_str);
            ps.setString(2, keyword);
            ps.setString(3, user);
            ps.setString(4, text);
            ps.setString(5, latitude);
            ps.setString(6, longitude);
            ps.setString(7, timestamp);
            ps.setBoolean(8, false);
            ps.setDouble(9, 0.0);

            ps.executeUpdate();

            ps.close();

            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
}
