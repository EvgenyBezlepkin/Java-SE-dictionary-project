/*
 * Class ClientThread.
 */
package jhelp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 * This class provides a network connection between end client of
 * {@link jhelp.Client} type and {@link jhelp.Server} object. Every object of
 * this class may work in separate thread.
 *
 * @author <strong >Y.D.Zakovryashin, 2009</strong>
 * @version 1.0
 * @see jhelp.Client
 * @see jhelp.Server
 */
public class ClientThread implements JHelp, Runnable {

    private final Server server;
    private final Socket clientSocket;
    private Data data, dataResponse;

    /**
     * Creates a new instance of Client
     *
     * @param server reference to {@link Server} object.
     * @param socket reference to {@link java.net.Socket} object for connection
     * with client application.
     */
    public ClientThread(Server server, Socket socket) {
        System.out.println("MClient: constructor");
        this.clientSocket = socket;
        this.server = server;
    }

    /**
     * The method defines main job cycle for the object.
     */
    public void run() {
        System.out.println("MClient: run");
        connect();
        disconnect();
    }

    /**
     * Opens input and output streams for data interchanging with client
     * application. The method uses default parameters.
     *
     * @return error code. The method returns {@link JHelp#OK} if streams are
     * successfully opened, otherwise the method returns {@link JHelp#ERROR}.
     */
    @Override
    public int connect() {
        System.out.println("MClient: connect");
        try (
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());) {
            data = (Data) input.readObject();
            System.out.println(data.toString());

            connectDB(data);

            output.writeObject(dataResponse);
            output.flush();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
            return JHelp.ERROR;
        }
        return JHelp.OK;
    }

    /**
     * Opens input and output streams for data interchanging with client
     * application. This method uses parameters specified by parameter
     * <code>args</code>.
     *
     * @param args defines properties for input and output streams.
     * @return error code. The method returns {@link JHelp#OK} if streams are
     * successfully opened, otherwise the method returns {@link JHelp#ERROR}.
     */
    public int connect(String[] args) {
        System.out.println("MClient: connect");
        return JHelp.OK;
    }

    /**
     * Transports {@link Data} object from client application to {@link Server}
     * and returns modified {@link Data} object to client application.
     *
     * @param data {@link Data} object which was obtained from client
     * application.
     * @return modified {@link Data} object
     */
    public Data getData(Data data) {
        System.out.println("Client: getData");
        return null;
    }

    /**
     * The method closes connection with client application.
     *
     * @return error code. The method returns {@link JHelp#OK} if input/output
     * streams and connection with client application was closed successfully,
     * otherwise the method returns {@link JHelp#ERROR}.
     */
    public int disconnect() {
        System.out.println("Client: disconnect");
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
            return JHelp.ERROR;
        }
        return JHelp.OK;
    }

    public void connectDB(Data data) {
        try (Socket clientSocket2 = new Socket(InetAddress.getByName("localhost"), 8081);
                ObjectOutputStream output = new ObjectOutputStream(clientSocket2.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(clientSocket2.getInputStream());) {
            System.out.println("Ушло на сервер" + data);
            output.writeObject(data);
            output.flush();
            dataResponse = (Data) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

}
