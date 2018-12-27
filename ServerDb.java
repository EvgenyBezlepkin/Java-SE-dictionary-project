package jhelp;

import com.mysql.jdbc.Connection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JOptionPane;

public class ServerDb implements JHelp {

    private Socket clientSocket;
    private static Properties prop = new Properties();
    private static FileInputStream fis;

    public ServerDb() throws IOException {
        this(DEFAULT_DATABASE_PORT);
        System.out.println("SERVERDb: default constructor");
    }

    public ServerDb(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
    }

    public ServerDb(int port) {
        System.out.println("SERVERDb: constructor");
    }

    public ServerDb(String[] args) {
        System.out.println("SERVERDb: constructor");
    }

    public static void main(String[] args) {
        System.out.println("SERVER: main");
        getConfigFileForServerDB();
        ServerDb serverDb = new ServerDb(Integer.parseInt(prop.getProperty("port")));
        try (
                ServerSocket serverSocket = new ServerSocket(
                        Integer.parseInt(prop.getProperty("port")),
                        0,
                        InetAddress.getByName(prop.getProperty("host")))) {
            while (true) {
                serverDb.clientSocket = serverSocket.accept();
                new ServerDb(serverDb.clientSocket).run();
            }
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, e);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    /**
     * Method defines job cycle for client request processing.
     */
    private void run() throws IOException, ClassNotFoundException {
        System.out.println("SERVERDb: run");

        try (
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());) {
            Data data = null;
            data = (Data) input.readObject();
            Data dataResponse = null;
            switch (data.getOperation()) {
                case JHelp.SELECT:
                    dataResponse = selectDB(data);
                    break;
                case JHelp.INSERT:
                    dataResponse = insertDB(data);
                    break;
                case JHelp.UPDATE:
                    dataResponse = updateDB(data);
                    break;
                case JHelp.DELETE:
                    dataResponse = deleteDB(data);
                    break;
            }
            output.writeObject(dataResponse);
            output.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    /**
     *
     * @return error code. The method returns {@link JHelp#OK} if streams are
     * opened successfully, otherwise the method returns {@link JHelp#ERROR}.
     */
    @Override
    public int connect() {

        return JHelp.READY;
    }

    /**
     * Method sets connection to database and create
     * {@link java.net.ServerSocket} object for waiting of client's connection
     * requests.
     *
     * @return error code. Method returns {@link jhelp.JHelp#READY} in success
     * case. Otherwise method return {@link jhelp.JHelp#ERROR} or error code.
     */
    @Override
    public int connect(String[] args) {
        return JHelp.READY;
    }

    /**
     * Method returns result of client request to a database.
     *
     * @param data object of {@link jhelp.Data} type with request to database.
     * @return object of {@link jhelp.Data} type with results of request to a
     * database.
     * @see Data
     * @since 1.0
     */
    @Override
    public Data getData(Data data) {
        System.out.println("SERVERDb: getData");
        return null;
    }

    /**
     * Method disconnects <code>ServerDb</code> object from a database and
     * closes {@link java.net.ServerSocket} object.
     *
     * @return disconnect result. Method returns {@link #DISCONNECT} value, if
     * the process ends successfully. Othewise the method returns error code,
     * for example {@link #ERROR}.
     * @see jhelp.JHelp#DISCONNECT
     * @since 1.0
     */
    @Override
    public int disconnect() {
        System.out.println("SERVERDb: disconnect");
        return JHelp.DISCONNECT;
    }

    public static void getConfigFileForServerDB() {
        fis = null;
        try {
            fis = new FileInputStream("D:\\Others\\Programming\\JavaIDE\\JHelp\\src\\jhelp\\configServerDB");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        try {
            prop.load(fis);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private Data selectDB(Data data) {
        System.out.println("SERVERDb: connect");
        getConfigFileForServerDB();
        ArrayList<String> response = new ArrayList<>();
        Data dataResponse2 = null;
        try (
                Connection connection = (Connection) DriverManager.getConnection(
                        prop.getProperty("dbName"),
                        prop.getProperty("user"),
                        prop.getProperty("password")
                )) {
                    String findQuery = prop.getProperty("find").replaceAll("&&", data.getKey().getItem());
                    ResultSet rs = connection.createStatement().executeQuery(findQuery);

                    rs.last();
                    int rsRow = rs.getRow();
                    rs.beforeFirst();

                    Item[] items = new Item[rsRow];
                    int i = 0;
                    while (rs.next()) {
                        response.add(rs.getString("definition"));
                        items[i] = new Item(rs.getInt("id"), rs.getString("definition"), JHelp.ORIGIN);
                        i++;
                    }
                    dataResponse2 = new Data(JHelp.SELECT, new Item(data.getKey().getItem()), items);
                    System.out.println();

                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }
                return dataResponse2;
    }

    private Data deleteDB(Data data) {
        System.out.println("SERVERDb: connect");
        getConfigFileForServerDB();
        try (
                Connection connection = (Connection) DriverManager.getConnection(
                        prop.getProperty("dbName"),
                        prop.getProperty("user"),
                        prop.getProperty("password")
                )) {
                    String deleteQuery = prop.getProperty("delete").replaceAll("&&", data.getKey().getItem());
                    System.out.println(deleteQuery);
                    connection.createStatement().executeUpdate(deleteQuery);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }
                return new Data(JHelp.DELETE, new Item(""), new Item[0]);
    }

    private Data insertDB(Data data) {
        System.out.println("SERVERDb: connect");
        getConfigFileForServerDB();
        try (
                Connection connection = (Connection) DriverManager.getConnection(
                        prop.getProperty("dbName"),
                        prop.getProperty("user"),
                        prop.getProperty("password")
                )) {

                    boolean isExists = false;
                    String findQuery = prop.getProperty("find").replaceAll("&&", data.getKey().getItem());
                    ResultSet rs = connection.createStatement().executeQuery(findQuery);
                    if (rs.next()) {
                        isExists = true;
                    }
                    //значение существует
                    if (isExists) {
                        String addQuery2 = prop.getProperty("insert-def").replace("&&", data.getValue(0).getItem()).replace("$$", data.getKey().getItem());
                        connection.createStatement().executeUpdate(addQuery2);
                        return new Data(JHelp.INSERT, new Item("OK"), new Item[0]);
                    } else {
                        String addQuery1 = prop.getProperty("insert-term").replaceAll("&&", data.getKey().getItem());
                        connection.createStatement().executeUpdate(addQuery1);

                        String addQuery2 = prop.getProperty("insert-def").replace("&&", data.getValue(0).getItem()).replace("$$", data.getKey().getItem());
                        connection.createStatement().executeUpdate(addQuery2);
                        return new Data(JHelp.INSERT, new Item("OK"), new Item[0]);
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                    return new Data(JHelp.INSERT, new Item("Error"), new Item[0]);
                }
    }

    private Data updateDB(Data data) {

        System.out.println("SERVERDb: connect");
        getConfigFileForServerDB();
        try (
                Connection connection = (Connection) DriverManager.getConnection(
                        prop.getProperty("dbName"),
                        prop.getProperty("user"),
                        prop.getProperty("password")
                )) {

                    String deleteQuery = prop.getProperty("delete").replaceAll("&&", String.valueOf(data.getKey().getId()));
                    System.out.println(deleteQuery);
                    connection.createStatement().executeUpdate(deleteQuery);

                    String addQuery2 = prop.getProperty("insert-def")
                            .replace("&&", data.getValue(0).getItem()).replace("$$", data.getKey().getItem());
                    connection.createStatement().executeUpdate(addQuery2);

                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, e);
                }
                return new Data(JHelp.INSERT, new Item("OK"), new Item[0]);
    }
}
