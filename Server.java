/*
 * Server.java
 *
 */
package jhelp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * This class sets a network connection between end client's objects of
 * {@link jhelp.Client} type and single {@link jhelp.ServerDb} object.
 *
 * @author <strong >Y.D.Zakovryashin, 2009</strong>
 * @version 1.0
 * @see jhelp.Client
 * @see jhelp.ClientThread
 * @see jhelp.ServerDb
 */
public class Server extends Thread implements JHelp {

    private Socket clientSocket;
    private static FileInputStream fis;
    private static Properties prop = new Properties();

    /**
     * Creates a new instance of Server
     */
    public Server() {
        this(DEFAULT_SERVER_PORT, DEFAULT_DATABASE_PORT);
        System.out.println("SERVER: Default Server Constructed");
    }

    public Server(int port, int dbPort) {
        System.out.println("SERVER: Server Constructed");
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("SERVER: main");
        getConfigFileForServer();

        Server server = new Server(
                Integer.parseInt(prop.getProperty("port")),
                Integer.parseInt(prop.getProperty("dbPort")));
        try (
                ServerSocket serverSocket = new ServerSocket(
                        Integer.parseInt(prop.getProperty("port")),
                        0,
                        InetAddress.getByName(prop.getProperty("host")));) {
            while (true) {
                server.clientSocket = serverSocket.accept();
                System.err.println("Client accepted");
                new Thread(new ClientThread(server, server.clientSocket)).start();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void run(Data data) {
        System.out.println("SERVER: run");
//        try (Socket clientSocket2 = new Socket(InetAddress.getByName(prop.getProperty("host")), Integer.parseInt(prop.getProperty("dbPort")));
//                ObjectOutputStream output = new ObjectOutputStream(clientSocket2.getOutputStream());
//                ObjectInputStream input = new ObjectInputStream(clientSocket2.getInputStream());) {
//            dataResponse = null;
//            output.writeObject(data);
//            output.flush();
//            dataResponse = (Data) input.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            JOptionPane.showMessageDialog(null, e);
//        }
    }

    /**
     * The method sets connection to database ({@link jhelp.ServerDb} object)
     * and create {@link java.net.ServerSocket} object for waiting of client's
     * connection requests. This method uses default parameters for connection.
     *
     * @return error code. The method returns {@link JHelp#OK} if streams are
     * successfully opened, otherwise the method returns {@link JHelp#ERROR}.
     */
    @Override
    public int connect() {
        System.out.println("SERVER: connect");
        return OK;
    }

    /**
     * The method sets connection to database ({@link jhelp.ServerDb} object)
     * and create {@link java.net.ServerSocket} object for waiting of client's
     * connection requests.
     *
     * @param args specifies properties of connection.
     * @return error code. The method returns {@link JHelp#OK} if connection are
     * openeds uccessfully, otherwise the method returns {@link JHelp#ERROR}.
     */
    @Override
    public int connect(String[] args) {
        System.out.println("SERVER: connect");
        return OK;
    }

    /**
     * Transports initial {@link Data} object from {@link ClientThread} object
     * to {@link ServerDb} object and returns modified {@link Data} object to
     * {@link ClientThread} object.
     *
     * @param data Initial {@link Data} object which was obtained from client
     * application.
     * @return modified {@link Data} object
     */
    @Override
    public Data getData(Data data) {
        System.out.println("SERVER:getData");

        return null;
    }

    /**
     * The method closes connection with database.
     *
     * @return error code. The method returns {@link JHelp#OK} if a connection
     * with database ({@link ServerDb} object) closed successfully, otherwise
     * the method returns {@link JHelp#ERROR} or any error code.
     */
    @Override
    public int disconnect() {
        System.out.println("SERVER: disconnect");
        return OK;
    }

    static void getConfigFileForServer() {
        fis = null;
        try {
            fis = new FileInputStream("D:\\Others\\Programming\\JavaIDE\\JHelp\\src\\jhelp\\configServerClient");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        try {
            prop.load(fis);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
