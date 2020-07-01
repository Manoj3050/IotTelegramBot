/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.dbAccess;

import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.leotech.telegrambot.dbAccess.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.sql.DataSource;
import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 *
 * @author Manoj
 */
public class sqlConnection {

    String host = DBvars.HOST;
    String username = DBvars.USERNAME;
    String password = DBvars.PASSWORD;
    String dbName = DBvars.DBNAME;
    String dbport = DBvars.PORT;
    /*String host = "localhost";
    String username = "root";
    String password = "";
    String dbName = "arzaman_db";
    String dbport = "3308";*/

    static boolean isConnected = false;
    private static GenericObjectPool gPool = null;
    private static DataSource dataSource = null;

    public sqlConnection() {

        try {
            /*try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + dbName, username, password);
                Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, "***************" + conn.getCatalog());
                } catch (SQLException ex) {
                Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
                }*/
            dataSource = setUpPool();

        } catch (Exception ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @SuppressWarnings("unused")
    public DataSource setUpPool() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
        gPool = new GenericObjectPool();
        gPool.setMaxActive(5);

        // Creates a ConnectionFactory Object Which Will Be Use by the Pool to Create the Connection Object!
        ConnectionFactory cf = new DriverManagerConnectionFactory("jdbc:mysql://" + host + ":" + dbport + "/" + dbName, username, password);

        // Creates a PoolableConnectionFactory That Will Wraps the Connection Object Created by the ConnectionFactory to Add Object Pooling Functionality!
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, gPool, null, null, false, true);
        return new PoolingDataSource(gPool);
    }

    public GenericObjectPool getConnectionPool() {
        return gPool;
    }

    public static String isUserExists(String userID) {
        String uniqueID = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT unique_id FROM smartpid_users WHERE email = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                uniqueID = rs.getString("unique_id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return uniqueID;
        }
    }

    public static boolean isUserExistsByUID(String uniqueID) {
        boolean rsl = false;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT unique_id FROM smartpid_users WHERE unique_id = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uniqueID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rsl = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }

    }

    public static boolean isPasswordCorrect(String uniqueID, String password) {
        boolean rsl = false;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT encrypted_password,salt FROM smartpid_users WHERE unique_id = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uniqueID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                /*
                php function for decryption
                 public function checkhashSSHA($salt, $password) {
 
                    $hash = base64_encode(sha1($password . $salt, true) . $salt);
 
                    return $hash;
                    }
                 */
                String hash = rs.getString("encrypted_password");
                String salt = rs.getString("salt");

                if (checkHash(password, hash, salt)) {
                    rsl = true;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // work on telegram_bot table
    public static boolean setChatID(String uniqueID, long chatID, boolean active) {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO telegram_bot(unique_id, chat_id, active) VALUES (?, ?, ?)");
            stmt.setString(1, uniqueID);
            stmt.setLong(2, chatID);
            stmt.setBoolean(3, active);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return true;
        }
    }

    public static boolean isUniqueIDexists(String uniqueID) {
        boolean rsl = false;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT unique_id FROM telegram_bot WHERE unique_id = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uniqueID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rsl = true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }
    }

    public static String isChatIDexists(Long chatID) {
        String uniqueID = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT unique_id FROM telegram_bot WHERE chat_id = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, chatID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                uniqueID = rs.getString("unique_id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return uniqueID;
        }

    }

    public static boolean isChatIDActive(Long chatID) {
        boolean rsl = false;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT active FROM telegram_bot WHERE chat_id = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, chatID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean("active") == true) {
                    rsl = true;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }
    }

    public static void activateChatID(Long chatID) {
        //update users set num_points = ? where first_name = ?
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "UPDATE telegram_bot SET active = ? WHERE chat_id = ?";

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, true);
            pstmt.setLong(2, chatID);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
        }

    }

    public static String getUserIDbyChatID(Long chatID) {
        String userID = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT email FROM smartpid_users WHERE unique_id = (SELECT unique_id FROM telegram_bot WHERE chat_id = ?)";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, chatID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                userID = rs.getString("email");
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return userID;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //work on device_table
    public static boolean isSerialExists(String serialID) {
        boolean rsl = false;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT id_device FROM device_table WHERE id_device = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, serialID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rsl = true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }

    }

    public static Long getChatID(String serialID) {
        long rsl = 0;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT chat_id FROM device_table WHERE device_hash = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, serialID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rsl = rs.getLong("chat_id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }
    }

    public static int setChatIDForSerial(String serialID, long chatID, String userID) {
        int rsl = 0;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE device_table SET chat_id = ? WHERE id_device = ? AND mail = ?");
            stmt.setLong(1, chatID);
            stmt.setString(2, serialID);
            stmt.setString(3, userID);
            rsl = stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }
    }

    public static int setAlarmForSerialandChatID(String serialID, long chatID, boolean alarm, String deviceHash) {
        int rsl = 0;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE device_table SET alarm_on = ? , device_hash = ? WHERE id_device = ? AND chat_id = ?");
            stmt.setBoolean(1, alarm);
            stmt.setString(2, deviceHash);
            stmt.setString(3, serialID);
            stmt.setLong(4, chatID);
            rsl = stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
            return rsl;
        }
    }

    public static List<String> getAllDevicesWithAlarm() {
        List<String> devices = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT id_device FROM device_table WHERE alarm_on = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, true);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                devices.add(rs.getString("id_device"));
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }

        }
        return devices;
    }

    public static List<String> getDevicesWithChatID(Long chatID) {
        List<String> devices = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = "SELECT id_device FROM device_table WHERE chat_id = ?";
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, chatID);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                devices.add(rs.getString("id_device"));
            }

        } catch (SQLException ex) {
            Logger.getLogger(sqlConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Closing ResultSet Object
                if (rs != null) {
                    rs.close();
                }
                // Closing PreparedStatement Object
                if (pstmt != null) {
                    pstmt.close();
                }
                // Closing Connection Object
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }

        }
        return devices;
    }

    private static boolean checkHash(String password, String encHash, String salt) {
        byte[] hashSha1 = DigestUtils.sha1(password + salt);
        byte[] b = salt.getBytes(StandardCharsets.US_ASCII);
        byte[] destination = new byte[hashSha1.length + b.length];
        System.arraycopy(hashSha1, 0, destination, 0, hashSha1.length);

        System.arraycopy(b, 0, destination, hashSha1.length, b.length);
        String newHash = Base64.getEncoder().encodeToString(destination);
        return newHash.equals(encHash);
    }

}
