import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.JSchException;
import com.tmax.tibero.jdbc.ext.TbDataSource;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.ArrayList;


public class SFMFormGui extends JFrame {
    private JButton runGetDataButton;
    private JPanel rootPanel;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextArea textArea3;
    private JTextField TextField1;
    private JTextField TextField2;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JTextArea notesArea;
    private JScrollPane notesScrollPane;
    private JScrollPane tiberoScrollPane;
    private JLabel SFMCORE_02;
    private JLabel SFMCORE_01;
    private JScrollPane SFMCORE02ScrollPane;
    private JScrollPane SFMCORE01ScrollPane;
    private JTabbedPane tabbedPane1;
    private JButton connectButton;
    private JTree treeSfmLogs;
    private JButton bTree;
    private JRadioButton rbSfm0101;
    private JRadioButton rbSfm0102;
    private JRadioButton rbSfm0103;
    private JRadioButton rbSfm0201;
    private JRadioButton rbSfm0202;
    private JRadioButton rbSfm0203;
    private JButton button4;
    private JButton refreshButtonSPP;
    private JButton btnUploadDownloadSPP;
    private JTree treeSPP;
    private JRadioButton rbSPP0101;
    private JRadioButton rbSPP0201;
    private JRadioButton rbSPP0101UAT;
    private JButton btnEncrPass;
    private JPasswordField passwordField1;
    private JTextField jtEncrypt;
    private JPanel jpSFM;
    private JPanel jpInfodiod;
    private JPanel jpSPP;
    private JPanel jpEncryptPass;
    private static SFMFormGui gui;
    public static final String PATH_TO_PROPERTIES = "conf\\config.properties";
    private static Properties  properties = new Properties();
    public static int count;
    public static int tiberothreadcount;
    public static int sshcount;

    static {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(PATH_TO_PROPERTIES);
            properties.load(inputStream);
        }catch (IOException o){
            System.out.println("Ошибка : файл " + PATH_TO_PROPERTIES + " не обнаружен");
            o.printStackTrace();
        }
    }

    private static String decryptPass(String pass){
        return Encryptor.decrypt(Encryptor.key, Encryptor.initVector,pass);
    }

    public void appendTextArea2(String appen) {
        this.textArea2.append(appen);
    }

    public void appendTextArea3(String appen) {
        this.textArea3.append(appen);
    }

    public String getTextField1Text() {
        return this.TextField1.getText();
    }

    public String getTextField2Text() {
        return this.TextField2.getText();
    }
        private Ssh ssh;
        private Ssh ssh1;
        private Tibero tibero;
    public SFMFormGui() {
        setContentPane(rootPanel);
        pack();
        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        runGetDataButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String dat1 = getTextField1Text();
                String dat2 = getTextField2Text();

                String com = "zcat `find /home/tomcat/ufm_home/logs/out/*.zip -type f -newermt '" + dat1 + "' ! -newermt '" + dat2 + "'`|strings|grep -E '[0-9][0-9]:[0-9][0-9]:[0-9][0-9]'|awk '{print $2}'|awk -F '.' '{print $1}'|sort|uniq -c ";
                System.out.println("count Threads = " + count);
                System.out.println(com);
                if (count==0) {
                    ssh = new Ssh(com, properties.getProperty("sfmco1User"), decryptPass(properties.getProperty("sfmco1Pass")), properties.getProperty("sfmco1Host"), textArea2);
                    ssh1 = new Ssh(com, properties.getProperty("sfmco2User"), decryptPass(properties.getProperty("sfmco2Pass")), properties.getProperty("sfmco2Host"), textArea3);
                    tibero = new Tibero(properties.getProperty("databaseURL"), properties.getProperty("tib.user"), decryptPass(properties.getProperty("tib.password")), TextField1.getText(), TextField2.getText(), textArea1);
                }

                if ( tibero.isAlive()==false && ssh.isAlive()==false && ssh1.isAlive()==false ) {
                        tibero.start();
                        ssh.start();
                        ssh1.start();
                }
            }
        });
        button3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (tiberothreadcount==0) {
                    tibero = new Tibero(properties.getProperty("databaseURL"), properties.getProperty("tib.user"), decryptPass(properties.getProperty("tib.password")), TextField1.getText(), TextField2.getText(), textArea1);
                }

                if ( tibero.isAlive()==false ) {
                    tibero.start();
                }

            }
        });
        button2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String dat1 = getTextField1Text();
                String dat2 = getTextField2Text();

                String com = "zcat `find /home/tomcat/ufm_home/logs/out/*.zip -type f -newermt '" + dat1 + "' ! -newermt '" + dat2 + "'`|strings|grep -E '[0-9][0-9]:[0-9][0-9]:[0-9][0-9]'|awk '{print $2}'|awk -F '.' '{print $1}'|sort|uniq -c ";
                System.out.println(com);
                if (sshcount==0) {
                    ssh = new Ssh(com, properties.getProperty("sfmco1User"), decryptPass(properties.getProperty("sfmco1Pass")), properties.getProperty("sfmco1Host"), textArea2);
                }

                if ( ssh.isAlive()==false) {
                    ssh.start();
                }
            }
        });
        button1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String dat1 = getTextField1Text();
                String dat2 = getTextField2Text();

                String com = "zcat `find /home/tomcat/ufm_home/logs/out/*.zip -type f -newermt '" + dat1 + "' ! -newermt '" + dat2 + "'`|strings|grep -E '[0-9][0-9]:[0-9][0-9]:[0-9][0-9]'|awk '{print $2}'|awk -F '.' '{print $1}'|sort|uniq -c ";
                System.out.println(com);
                if (sshcount==0) {
                    ssh1 = new Ssh(com, properties.getProperty("sfmco2User"), decryptPass(properties.getProperty("sfmco2Pass")), properties.getProperty("sfmco2Host"), textArea3);
                }

                if ( ssh1.isAlive()==false) {
                    ssh1.start();
                }
            }
        });



        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            String homeDirectoryLogs =   properties.getProperty("homeDirectoryLogs");
            String pathNameLogs="";
                String[] sshdata = getSshDataByRbSFM();
                String hostSFM=sshdata[0];
                String userSFM=sshdata[1];
                String passSFM=sshdata[2];
                String nodeNumber=sshdata[3];

            ArrayList<String> listPathLogs = new ArrayList<>();
                for (TreePath treePath : treeSfmLogs.getSelectionPaths()){
                    for (int i =0; i<treePath.getPathCount(); i++) {
                        pathNameLogs=pathNameLogs + "/" + treePath.getPath()[i];
                    }
                    listPathLogs.add(homeDirectoryLogs + pathNameLogs);
                    pathNameLogs="";
                }

                SSHClient sshClient = new SSHClient(userSFM,decryptPass(passSFM),hostSFM);

                try {
                    sshClient.uploadFileToDiod(listPathLogs,nodeNumber);

                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (JSchException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (SftpException e1) {
                    e1.printStackTrace();
                }
            }
        });


        bTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                DefaultTreeModel model = (DefaultTreeModel) treeSfmLogs.getModel();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                model.reload();
                String[] sshdata = getSshDataByRbSFM();
                String hostSFM=sshdata[0];
                String userSFM=sshdata[1];
                String passSFM=sshdata[2];


          SSHClient ssh = new SSHClient(userSFM, decryptPass(passSFM), hostSFM, "SFM");

                try {
                    StringBuilder builder = ssh.getListLogFiles();

                   // String tmp = builder.toString();
                        for (String line : builder.toString().split("\r\n")){
                            String ll = line;
                            if (line.matches("(.*)\\w{1,}[/]\\w{1,}-\\d{0,}(.*)") ){
                                String name = line.split("/")[0];
                                String nameLogs = line.split("/")[1];
                                addTreeNodes(name
//                                                .replaceAll("(.*)31m{1,}","")
//                                                .replaceAll("\u001B\\[0m$",""),
                                                 .replaceAll("(.*)31m{1,}","")
                                                 .replaceAll("\u001B\\[0m$","")
                                                 .replaceAll("\u001B\\[01;34m","")
                                                 .replaceAll("\u001B\\[0m",""),
                                             nameLogs
//                                                     .replaceAll("(.*)31m{1,}","")
//                                                     .replaceAll("\u001B\\[0m$","")
                                                     .replaceAll("(.*)31m{1,}","")
                                                     .replaceAll("\u001B\\[0m$","")
                                                     .replaceAll("\u001B\\[01;34m","")
                                                     .replaceAll("\u001B\\[0m",""),
                                             model,
                                             root);
                                }
                        }

                    } catch (InterruptedException e2) {
                    e2.printStackTrace();
                    } catch (IOException e2) {
                    e2.printStackTrace();
                    } catch (JSchException e2) {
                      e2.printStackTrace();   }
            }
        });
        button4.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                String ftpServer = properties.getProperty("diodein");
//                String workingDirektory =  properties.getProperty("diodWorkingDirektory");
//                String ftpUsername = properties.getProperty("diodUser");
//                String ftpPassword = properties.getProperty("diodPassword");
//
//                SSLSessionReuseFTPSClient ftpsClient = new SSLSessionReuseFTPSClient();
//                ftpsClient.addProtocolCommandListener(new PrintCommandListener(System.out));
//                try {
//                    ftpsClient.connect(ftpServer, 21);
//
//                    ftpsClient.login(ftpUsername, ftpPassword);
//                    ftpsClient.enterLocalPassiveMode();// Run the passive mode command now  instead of after loggin in.
//                    ftpsClient.setControlEncoding("UTF-8");
//                    int code = ftpsClient.type(FTP.BINARY_FILE_TYPE);
//                    System.out.println("code = " + code);
//                    Boolean codeBoolean =  ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);
//                    System.out.println("code = " + codeBoolean);
//                    ftpsClient.execPBSZ(0);
//                    ftpsClient.execPROT("P");
//                    //ftpsClient.type(FTP.BINARY_FILE_TYPE);
//                    ftpsClient.changeWorkingDirectory(workingDirektory);
//
//                    InputStream inputStream;
//                    //  Научился класть файлы на infodiod
//                    String fullPathFileName = "D:\\Sources\\GetDataSFM\\files\\out-2018-11-23.75.tar.gz";
//                    File file = new File(fullPathFileName);
//                    inputStream = new FileInputStream(file);
//                    Boolean append = ftpsClient.storeFile(file.getName(), inputStream);
//                    System.out.println("append = " + append);
//
//                    inputStream.close();
//                    ftpsClient.logout();
//                    ftpsClient.disconnect();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }



            }
        });
        refreshButtonSPP.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                DefaultTreeModel model = (DefaultTreeModel) treeSPP.getModel();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                model.reload();
                String[] sshdata = getSshDataByRbSPP();
                String hostSPP=sshdata[0];
                String userSPP=sshdata[1];
                String passSPP=sshdata[2];

                SSHClient ssh = new SSHClient(userSPP, decryptPass(passSPP), hostSPP, "SPP");

                try {
                    StringBuilder builder = ssh.getListLogFiles();

                    // String tmp = builder.toString();
                    for (String line : builder.toString().split("\r\n")){
                        String ll = line;
                        if (line.matches("(.*)\\w{1,}[/]\\w{1,}-\\d{0,}(.*)") ){
                            String name = line.split("/")[0];
                            String nameLogs = line.split("/")[1];
                            addTreeNodes(name
                                            .replaceAll("(.*)31m{1,}","")
                                            .replaceAll("\u001B\\[0m$","")
                                            .replaceAll("\u001B\\[01;34m","")
                                            .replaceAll("\u001B\\[0m",""),
                                    nameLogs
                                            .replaceAll("(.*)31m{1,}","")
                                            .replaceAll("\u001B\\[0m$","")
                                            .replaceAll("\u001B\\[01;34m","")
                                            .replaceAll("\u001B\\[0m",""),
                                    model,
                                    root);
                        }
                    }

                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (JSchException e2) {
                    e2.printStackTrace();   }
            }
        });
        btnUploadDownloadSPP.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String homeDirectoryLogs = properties.getProperty("homeDirectoryLogsSPP");
                String pathNameLogs="";
                String[] sshdata = getSshDataByRbSPP();
                String hostSPP=sshdata[0];
                String userSPP=sshdata[1];
                String passSPP=sshdata[2];
                String nodeNumber=sshdata[3];

                ArrayList<String> listPathLogs = new ArrayList<>();
                for (TreePath treePath : treeSPP.getSelectionPaths()){
                    for (int i =0; i<treePath.getPathCount(); i++) {
                        pathNameLogs=pathNameLogs + "/" + treePath.getPath()[i];
                    }
                    listPathLogs.add(homeDirectoryLogs + pathNameLogs);
                    pathNameLogs="";
                }

                SSHClient sshClient = new SSHClient(userSPP,decryptPass(passSPP),hostSPP,"SPP");

                try {
                    sshClient.uploadFileToFTP(listPathLogs,nodeNumber);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (JSchException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (SftpException e1) {
                    e1.printStackTrace();
                }
            }
        });
        btnEncrPass.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                //String key = "Bar12345Bar12345"; // 128 bit key
                //String initVector = "RandomInitVector"; // 16 bytes IV
               String password = new String(passwordField1.getPassword());
               if (password.length() == 0) {
                   jtEncrypt.setText("Пароль не может быть < 0");
               }else {
                   String newpass = Encryptor.encrypt(Encryptor.key, Encryptor.initVector, password);
                   jtEncrypt.setText(newpass);
               }
               //System.out.println(Encryptor.decrypt(Encryptor.key, Encryptor.initVector,newpass));
            }
        });
    }

    private String[] getSshDataByRbSPP(){
        String hostSPP="";
        String userSPP="";
        String passSPP="";
        String nodeNumber="";
        if (rbSPP0101.isSelected()){
            nodeNumber="0101";
            hostSPP = properties.getProperty("spp1Host");
            userSPP = properties.getProperty("spp1User");
            passSPP = properties.getProperty("spp1Pass");
        }else if (rbSPP0201.isSelected()){
            nodeNumber="0201";
            hostSPP = properties.getProperty("spp2Host");
            userSPP = properties.getProperty("spp2User");
            passSPP = properties.getProperty("spp2Pass");
        }else if (rbSPP0101UAT.isSelected()){
            nodeNumber="0101_UAT";
            hostSPP = properties.getProperty("spp3Host");
            userSPP = properties.getProperty("spp3User");
            passSPP = properties.getProperty("spp3Pass");
        }else {
            nodeNumber="0101";
            hostSPP = properties.getProperty("spp1Host");
            userSPP = properties.getProperty("spp1User");
            passSPP = properties.getProperty("spp1Pass");
        }
        return new String[]{ hostSPP, userSPP, passSPP, nodeNumber };
    }
    private String[] getSshDataByRbSFM(){
        String hostSFM="";
        String userSFM="";
        String passSFM="";
        String nodeNumber="";
        if (rbSfm0101.isSelected()){
            nodeNumber="0101";
            hostSFM = properties.getProperty("sfmco1Host");
            userSFM = properties.getProperty("sfmco1User");
            passSFM = properties.getProperty("sfmco1Pass");
        }else if (rbSfm0102.isSelected()){
            nodeNumber="0102";
            hostSFM = properties.getProperty("sfmco2Host");
            userSFM = properties.getProperty("sfmco2User");
            passSFM = properties.getProperty("sfmco2Pass");
        }else if (rbSfm0103.isSelected()){
            nodeNumber="0103";
            hostSFM = properties.getProperty("sfmco3Host");
            userSFM = properties.getProperty("sfmco3User");
            passSFM = properties.getProperty("sfmco3Pass");
        }else if (rbSfm0201.isSelected()){
            nodeNumber="0201";
            hostSFM = properties.getProperty("sfmco4Host");
            userSFM = properties.getProperty("sfmco4User");
            passSFM = properties.getProperty("sfmco4Pass");
        }else if (rbSfm0202.isSelected()){
            nodeNumber="0202";
            hostSFM = properties.getProperty("sfmco5Host");
            userSFM = properties.getProperty("sfmco5User");
            passSFM = properties.getProperty("sfmco5Pass");
        }else if (rbSfm0203.isSelected()){
            nodeNumber="0203";
            hostSFM = properties.getProperty("sfmco6Host");
            userSFM = properties.getProperty("sfmco6User");
            passSFM = properties.getProperty("sfmco6Pass");
        }else {
            nodeNumber="0101";
            hostSFM = properties.getProperty("sfmco1Host");
            userSFM = properties.getProperty("sfmco1User");
            passSFM = properties.getProperty("sfmco1Pass");
        }
        return new String[]{hostSFM,userSFM,passSFM,nodeNumber};
    }



    private void addTreeNodes(String parent, String child,DefaultTreeModel model, DefaultMutableTreeNode root){
        Boolean needCreateParent=true;
        DefaultMutableTreeNode node = null;
        for (int i =0; i<root.getChildCount(); i++){
            if (root.getChildAt(i).toString().equals(parent)){
                node = (DefaultMutableTreeNode) root.getChildAt(i);
                needCreateParent=false;
            }
        }

        if (needCreateParent){
            root.add(new DefaultMutableTreeNode(parent));
            for (int i =0; i<root.getChildCount(); i++){
                if (root.getChildAt(i).toString().equals(parent)){
                    node = (DefaultMutableTreeNode) root.getChildAt(i);
                }
            }
        }

        DefaultMutableTreeNode logs = new DefaultMutableTreeNode(child);
        model.insertNodeInto(logs, node, node.getChildCount());
        model.reload();
    }

    private class Ssh extends Thread{
        public String command;
        public String user, pass, host;
        public String respons;
        private JTextArea jTextArea;


        public Ssh(String command, String user, String pass, String host, JTextArea textArea) {
            this.command = command;
            this.user = user;
            this.pass = pass;
            this.host = host;
            this.jTextArea = textArea;
        }

        @Override
        public void run() {
            count++;
            sshcount++;
           try {
               jTextArea.setText("");
               System.out.println(Thread.currentThread().getName());
               SSHClient sshClient = new SSHClient(user, pass, host, jTextArea);
               this.respons = sshClient.executeCommand(command);
               this.jTextArea.append(this.respons);
               jTextArea.append( Thread.currentThread().getName() + " Finish ");
               System.out.println( Thread.currentThread().getName() + " SSH is Finish" );
           }catch (Exception e){
               jTextArea.append(e.getMessage());
           }
           finally {
               count--;
               sshcount--;
           }


        }

        public String getRespons(){
            return this.respons;
        }

    }

    private static class Tibero extends Thread {
        private static String databaseURL;
        private static String user;
        private static String password;
        private static String dat1, dat2;
        private static ArrayList<String> strings = new ArrayList<>();
        private static JTextArea jTextArea;

        public Tibero(String dbURL, String userName, String pass, String date1, String date2,JTextArea textArea) {
            databaseURL = dbURL;
            user = userName;
            password = pass;
            dat1 =date1;
            dat2 = date2;
            jTextArea = textArea;
        }

        @Override
        public void run() {
            count++;
            tiberothreadcount++;
            try {
                jTextArea.setText("");
                strings = getDataFromTibero();
                for (String s : strings){
                    jTextArea.append(s+"\n");
                }
                jTextArea.append( Thread.currentThread().getName() + " Finish ");
                System.out.println("Thread Tibero is Finish" );
            }catch (Exception e){
                jTextArea.append(e.getMessage());
            }
            finally {
                count--;
                tiberothreadcount--;
            }

        }

        public ArrayList<String> getStrings() {
            return strings;
        }

        private static ArrayList<String> getDataFromTibero() {
            ArrayList<String> list = new ArrayList<>();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;

            try {
                jTextArea.append( Thread.currentThread().getName() + " Start ");
                Class.forName("com.tmax.tibero.jdbc.TbDriver");
                TbDataSource tds = new TbDataSource();
                tds.setURL(databaseURL);
                tds.setUser(user);
                tds.setPassword(password);
                conn = tds.getConnection();

                if (conn != null) {
                    System.out.println("Connected to the database");
                }

                stmt = conn.createStatement();

                String partiotion = "p_"+dat1.substring(0,4)+dat1.substring(5,7)+dat1.substring(8,10);

                String sql = "select count(*) as cn,to_char(ttime, 'hh:mi:ss') as tt "
                        + " from fraud.t_fraud_trans partition (" + partiotion + ") "
                        + " where ttime between to_date('" + dat1 + "','YYYY-MM-DD hh24:mi:ss') and to_date('" + dat2 + "','YYYY-MM-DD hh24:mi:ss') " +
                        " and msgtype = 0100 "
                        + " group by to_char(ttime, 'hh:mi:ss') "
                        + " order by 2 desc";

                System.out.println(sql);

                rs = stmt.executeQuery(sql);

            int columns = rs.getMetaData().getColumnCount();
            StringBuilder builder = new StringBuilder();
                while (rs.next()) {
                    builder.setLength(0);
                    for (int i = 1; i <= columns; i++) {
                        builder.append(rs.getString(i) + "\t");
                        System.out.print(rs.getString(i) + "  ");
                    }
                    list.add(builder.toString() );
                    System.out.println();
                }


            } catch (ClassNotFoundException ex) {
                System.out.println("Could not find database driver class");
                ex.printStackTrace();
                System.out.println();

                jTextArea.append(ex.getMessage());

            } catch (SQLException ex) {
                System.out.println("An error occurred. Maybe user/password is invalid");
                ex.printStackTrace();
                jTextArea.append(ex.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        rs.close();
                        stmt.close();
                        conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        return list;
        }
    }

    public void changeNameinJtreeSFM(String name){
        DefaultTreeModel model = (DefaultTreeModel) treeSfmLogs.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.setUserObject(name);
        root.removeAllChildren();
        model.reload();
    }

    public void changeNameinJtreeSPP(String name){
        DefaultTreeModel modelSPP = (DefaultTreeModel) treeSPP.getModel();
        DefaultMutableTreeNode rootSPP = (DefaultMutableTreeNode) modelSPP.getRoot();
        rootSPP.setUserObject(name);
        rootSPP.removeAllChildren();
        modelSPP.reload();
    }

    public static void main(String[] args)  {
        gui=new SFMFormGui();
        Dimension dimension = new Dimension();
        dimension.width =816;
        dimension.height=568;
        gui.setSize(dimension);
        gui.changeNameinJtreeSFM("logs");
        gui.changeNameinJtreeSPP("log");
        //System.setProperty("jdk.tls.useExtendedMasterSecret","false");
        //gui.pack();

    }



    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

}