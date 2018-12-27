/*
 * Client.java
 *
 */
package jhelp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * Client class provides users's interface of the application.
 *
 * @author <strong >Y.D.Zakovryashin, 2009</strong>
 * @version 1.0
 */
public class Client extends JFrame implements JHelp {

    public static final long serialVersionUID = 1234;
    private final Properties prop;
    private Data dataResponse;
    private JButton findBtn;
    private JButton addBtn;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton previousBtn;
    private JButton nextBtn;
    private JButton exitBtn;
    private JLabel termLbl;
    private JLabel defLbl;
    private JLabel configLbl1;
    private JLabel configLbl2;
    private JLabel configLbl3;
    private JTextField termTextField;
    private JTextArea defTextArea;
    private JTextArea configTextArea1;
    private JTextArea configTextArea2;
    private JTextArea configTextArea3;
    private JMenuItem exitAction;
    private ArrayList<String> responses;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    static int count = 0;

    /**
     * Constructor with parameters.
     *
     */
    public Client() {
        prop = new Properties();
        System.out.println("Client: constructor");
        setBounds(150, 150, 700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        addMenu();
        mainPanel.add(addLeftPanel(), BorderLayout.EAST);
        mainPanel.add(addTopPanel(), BorderLayout.NORTH);
        mainPanel.add(addCenterPanel(), BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);

        getConfigFileForClient();

        ClientListener cl = new ClientListener(this);
        exitBtn.addActionListener(cl);
        exitAction.addActionListener(cl);
        nextBtn.addActionListener(cl);
        previousBtn.addActionListener(cl);
        deleteBtn.addActionListener(cl);
        addBtn.addActionListener(cl);
        editBtn.addActionListener(cl);
        findBtn.addActionListener(cl);

        readFiles();
    }

    /**
     * Method for application start
     *
     * @param args agrgument of command string
     */
    static public void main(String[] args) {

        Client client = new Client();
    }

    /**
     * Method define main job cycle
     *
     * @param str
     * @param data
     */
    public void run(String str, Data data) {
        if (str.equalsIgnoreCase("find")) {
            findTxt(data);
        }
        if (str.equalsIgnoreCase("delete")) {
            deleteTxt(data);
        }
        if (str.equalsIgnoreCase("add")) {
            addTxt(data);
        }
        if (str.equalsIgnoreCase("edit")) {
            editTxt(data);
        }
    }

    public void findTxt(Data data) {
        System.out.println("Client: run");
        responses = new ArrayList();
        try {
            connect();
            oos.writeObject(data);
            oos.flush();

            dataResponse = (Data) ois.readObject();
            for (int i = 0; i < dataResponse.getValues().length; i++) {
                responses.add(dataResponse.getValue(i).getItem());
            }
            count = 0;
            printText();

        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, e);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        disconnect();
    }

    public void deleteTxt(Data data) {
        try {
            connect();
            oos.writeObject(data);
            oos.flush();
            dataResponse = (Data) ois.readObject();

            for (int i = 0; i < dataResponse.getValues().length; i++) {
                System.out.println(dataResponse.getValue(i).getItem());
                responses.add(dataResponse.getValue(i).getItem());
            }
            count = 0;
            defTextArea.setText("");
            JOptionPane.showMessageDialog(null, "Значение удалено");

        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, e);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        disconnect();
    }

    public void addTxt(Data data) {
        try {
            connect();
            oos.writeObject(data);
            oos.flush();
            dataResponse = (Data) ois.readObject();

            if (dataResponse.getKey().getItem().equals("OK")) {
                JOptionPane.showMessageDialog(null, "Значение добавлено");
            }
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, e);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        disconnect();
    }

    private void editTxt(Data data) {
        try {
            connect();
            oos.writeObject(data);
            oos.flush();
            dataResponse = (Data) ois.readObject();
            JOptionPane.showMessageDialog(null, dataResponse.getKey().getItem());

        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null, e);
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        disconnect();
    }

    public void printText() {
        if (!responses.isEmpty()) {
            defTextArea.setText(responses.get(count));
        } else {
            JOptionPane.showMessageDialog(null, "Несуществующий термин");
        }
    }

    /**
     * Method set connection to default server with default parameters
     *
     * @return error code
     */
    @Override
    public int connect() {
        try {
            socket = new Socket(
                    InetAddress.getByName(prop.getProperty("host")),
                    Integer.parseInt(prop.getProperty("port")));
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
            return JHelp.ERROR;
        }
        return JHelp.OK;
    }

    /**
     * Method set connection to server with parameters defines by argument
     * <code>args</code>
     *
     * @return error code
     */
    @Override
    public int connect(String[] args) {
        System.out.println("Client: connect");
        return JHelp.OK;
    }

    /**
     * Method gets data from data source
     *
     * @param data initial object (template)
     * @return new object
     */
    @Override
    public Data getData(Data data) {
        System.out.println("Client: getData");
        return data;
    }

    /**
     * Method disconnects client and server
     *
     * @return error code
     */
    @Override
    public int disconnect() {
        System.out.println("Client: disconnect");
        try {
            if (socket != null) {
                socket.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return JHelp.OK;
    }

    private JPanel addLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        nextBtn = new JButton("Next");
        previousBtn = new JButton("Previous");
        exitBtn = new JButton("Exit");
        addBtn.setMaximumSize(new Dimension(100, 30));
        editBtn.setMaximumSize(new Dimension(100, 30));
        deleteBtn.setMaximumSize(new Dimension(100, 30));
        previousBtn.setMaximumSize(new Dimension(100, 30));
        nextBtn.setMaximumSize(new Dimension(100, 30));
        exitBtn.setMaximumSize(new Dimension(100, 30));

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(addBtn);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(editBtn);
        leftPanel.add(Box.createVerticalStrut(10));;
        leftPanel.add(deleteBtn);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(nextBtn);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(previousBtn);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(exitBtn);
        return leftPanel;
    }

    private void addMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu settingsMenu = new JMenu("Setting");
        JMenu helpMenu = new JMenu("Help");
        exitAction = new JMenuItem("Exit");

        fileMenu.add(exitAction);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        this.setJMenuBar(menuBar);
    }

    private Box addTopPanel() {
        Box topPanel = Box.createHorizontalBox();
        termLbl = new JLabel("Term : ");
        termTextField = new JTextField();
        findBtn = new JButton("Find");
        findBtn.setPreferredSize(new Dimension(85, 30));
        JRootPane rootPan = SwingUtilities.getRootPane(this);
        rootPan.setDefaultButton(findBtn);
        topPanel.add(termLbl);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(termTextField);
        topPanel.add(Box.createHorizontalStrut(50));
        topPanel.add(findBtn);
        return topPanel;

    }

    private JTabbedPane addCenterPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        defLbl = new JLabel("Definition : ");
        defTextArea = new JTextArea();
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(defLbl);
        centerPanel.add(defTextArea);
        tabbedPane.addTab("Main", centerPanel);
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configLbl1 = new JLabel("Configuration file of client : ");
        configLbl2 = new JLabel("Configuration file of client server : ");
        configLbl3 = new JLabel("Configuration file of server database : ");
        configTextArea1 = new JTextArea();
        configTextArea2 = new JTextArea();
        configTextArea3 = new JTextArea();
        configPanel.add(Box.createVerticalStrut(20));
        configPanel.add(configLbl1);
        configPanel.add(configTextArea1);
        configPanel.add(configLbl2);
        configPanel.add(configTextArea2);
        configPanel.add(configLbl3);
        configPanel.add(configTextArea3);
        tabbedPane.addTab("Settings", configPanel);
        return tabbedPane;
    }

    public void getConfigFileForClient() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("D:\\Others\\Programming\\JavaIDE\\JHelp\\src\\jhelp\\configClient");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        try {
            prop.load(fis);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
        }

    }

    public void readFiles() {
        try (Stream<String> stream = Files.lines(Paths.get("D:\\Others\\Programming\\JavaIDE\\JHelp\\src\\jhelp\\configClient"))) {
            stream.forEach(configTextArea1::append);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JButton getFindBtn() {
        return findBtn;
    }

    public JButton getAddBtn() {
        return addBtn;
    }

    public JButton getEditBtn() {
        return editBtn;
    }

    public JButton getDeleteBtn() {
        return deleteBtn;
    }

    public JButton getPreviousBtn() {
        return previousBtn;
    }

    public JButton getExitBtn() {
        return exitBtn;
    }

    public JTextField getTermTextField() {
        return termTextField;
    }

    public JTextArea getDefTextArea() {
        return defTextArea;
    }

    public JTextArea getConfigTextArea1() {
        return configTextArea1;
    }

    public JTextArea getConfigTextArea2() {
        return configTextArea2;
    }

    public JTextArea getConfigTextArea3() {
        return configTextArea3;
    }

    public JMenuItem getExitAction() {
        return exitAction;
    }

    public int getCount() {
        return count;
    }

    public JButton getNextBtn() {
        return nextBtn;
    }

    public ArrayList<String> getResponses() {
        return responses;
    }

    public Data getDataResponse() {
        return dataResponse;
    }

}
