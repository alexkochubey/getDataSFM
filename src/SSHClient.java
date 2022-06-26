import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import com.jcraft.jsch.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;


import javax.swing.*;


public class SSHClient {

    public static final String PATH_TO_PROPERTIES = "conf\\config.properties";
    private static Properties properties = new Properties();
    public static String commndSudoUser, commndSudoUserSPP , logsDirSPP , logsDirSFM = "";
    static {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(PATH_TO_PROPERTIES);
            properties.load(inputStream);
            commndSudoUser = properties.getProperty("techUser");
            commndSudoUserSPP = properties.getProperty("techUserSPP");
            logsDirSPP = properties.getProperty("logsDirSPP");
            logsDirSFM = properties.getProperty("logsDirSFM");
        }catch (IOException o){
            System.out.println("Ошибка : файл " + PATH_TO_PROPERTIES + " не обнаружен");
            o.printStackTrace();
        }
    }

    /**
     * Constant STRICT_KEY_CHECKING
     */
    public static final String STRICT_KEY_CHECKING = "StrictHostKeyChecking";


    /**
     * Name/ip of the remote machine/device
     **/
    private static String host;
    private static String userName;
    private static String password;
    private static String loginTechUser;
    private static String techUser;
    private JTextArea jTextArea;
    /**
     * This method used to initilze user and host
     *
     * @param userName
     * @param password
     * @param host
     */
    public SSHClient(String userName, String password, String host, JTextArea textArea) {
        super();
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.jTextArea = textArea;
    }

    public SSHClient(String userName, String password, String host) {
        super();
        this.userName = userName;
        this.password = password;
        this.host = host;
    }

    public SSHClient(String userName, String password, String host, String techUser) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.techUser =  techUser;
        this.loginTechUser = techUser.equalsIgnoreCase("SPP") ? commndSudoUserSPP : commndSudoUser;
    }

    /*
     * This method used to execute commands remotly by using SSHV2
     *
     * @param host
     * @param username
     * @param password
     * @param command
     * @return
     */
    public String executeCommand(String command) {
        StringBuilder log = new StringBuilder();
        String response = null;
        Channel channel = null;
        Session session = null;

        try {
            JSch jsch = new JSch();
            JSch.setConfig(STRICT_KEY_CHECKING, "no"); //Constants.NO

            session = jsch.getSession(this.userName, this.host, 22);
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

            session.setPassword(this.password);
            session.connect(30000);
            channel = session.openChannel("shell");
            OutputStream ops = channel.getOutputStream();
            PrintStream ps = new PrintStream(ops);
            jTextArea.append("Trhead " + Thread.currentThread().getName() + " Start ");

            channel.connect();
            ps.println(commndSudoUser);
            ps.println(command);
            ps.close();

            StringBuilder builder = new StringBuilder();
            InputStream commandOutput = channel.getInputStream();
            Thread.sleep(5000);
           byte[] bt = new byte[1024];
            String str = null;
            int tim =0;
           while(true) {
               tim++;
               if(tim==12){
                  jTextArea.append(Thread.currentThread().getName() + " превышена 1 минута ожидания. Поток прерван "+"\n");
                  Thread.currentThread().interrupt();
               }
                while (commandOutput.available() > 0) {
                    int i = commandOutput.read(bt, 0, 100);
                    //if (i < 0) break;
                    str = new String(bt, 0, i);
                    builder.append(str);
                    System.out.print(str);
                }

                if(str.contains("[tomcat@")){
                   break;
                } else{
                   Thread.sleep(5000);
                   jTextArea.append(Thread.currentThread().getName() + " sleep 5 sec "+"\n");
                   System.out.println(Thread.currentThread().getName() + " sleep 5 sec ");
                }
            }
            jTextArea.setText("");
            response = builder.toString();

        } catch (Exception ex) {
            //handle exception
            ex.printStackTrace();
        } finally {
            try {
                if (session != null) {
                    session.disconnect();
                }
            } catch (Exception ex) {
                //handle exception
            }
            try {
                if (channel != null) {
                    channel.disconnect();
                }
            } catch (Exception ex) {
                //handle exception
            }

        }
        System.out.println("Response received :" + response);
        return response;
    }

    public static Channel getChannelSHH() throws JSchException {
        Channel channel = null;
        Session session = null;

        JSch jsch = new JSch();
        JSch.setConfig(STRICT_KEY_CHECKING, "no"); //Constants.NO
        session = jsch.getSession(userName, host, 22);
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setConfig("max_input_buffer_size","500000");
        session.setPassword(password);
        session.connect(30000);
        channel =(ChannelShell) session.openChannel("shell");
        channel.connect();
        return channel;
    }

    public static Channel getChannelSFTP() throws JSchException, IOException, InterruptedException {
        ChannelSftp channel = null;
        Session session = null;

        JSch jsch = new JSch();
        JSch.setConfig(STRICT_KEY_CHECKING, "no"); //Constants.NO
        session = jsch.getSession(userName, host, 22);
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setPassword(password);
        session.connect(30000);
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }

    public void uploadFileToDiod(ArrayList<String> listPathLogs, String nodeNumber ) throws InterruptedException, JSchException, IOException, SftpException {

        String programmPath = System.getProperty("user.dir");
        Paths filePath = null;
        Path filedir = filePath.get(programmPath, "files");

        File dilesDir = new File(filedir.toString());
        if (!dilesDir.exists()){
            dilesDir.mkdir();
        }

            ChannelSftp channelSFTP = (ChannelSftp) getChannelSFTP();
            Channel channelShell =  getChannelSHH();
            OutputStream ops = channelShell.getOutputStream();
            PrintStream ps = new PrintStream(ops);
            ps.println(commndSudoUser);

            InputStream inputStrim = channelShell.getInputStream();

            for (String puthname: listPathLogs) {
                int lenght = puthname.split("/").length;
                String filename = puthname.split("/")[lenght-1];
                String fileNameShell = filename;
                String fileNameShellWithoutExt = FilenameUtils.removeExtension(fileNameShell);
                String fileNameShelltargz = fileNameShellWithoutExt + ".tar.gz";
                String fileNameShelltmp = fileNameShellWithoutExt +".tmp";
                String extension = filename.endsWith(".zip") ? "zip" : "log";
                String fileNamepath = filePath.get(programmPath, "files", fileNameShelltargz).toString();

                String cdTmpCommand = "cd /tmp/";
                String cpLogCommandToTmp = "cp " + puthname + " /tmp/";
                String command = extension.equals("zip") ? "zcat":"cat";
                String cmd = command + " /tmp/" + fileNameShell + "|sed  -re 's/([0-9]{13,19})/XXXXXXXX/g'> /tmp/" + fileNameShelltmp;
                String rmCommand = "rm -f /tmp/"+fileNameShell;
                String mvCommand = "mv /tmp/"+fileNameShelltmp + " /tmp/" + fileNameShellWithoutExt;
                String tarLogCommand = "tar -czf /tmp/" + fileNameShelltargz + " /tmp/" + fileNameShellWithoutExt + " --remove-files";
                String createLockFile = "touch /tmp/lock.tmp";
                String rmLockFile = "rm -f /tmp/lock.tmp";

                String commandShell = cdTmpCommand + " && " + cpLogCommandToTmp + " && " + cmd + " && " + rmCommand + " && " + mvCommand + " && " + tarLogCommand + " && " + rmLockFile;
                ps.println(createLockFile);
                ps.flush();
                ps.println(commandShell);
                ps.flush();

                int fileExist=0;
                int count=0;
                //wait file log
                while(fileExist==0){
                    // 0 найден лок файл. 1 не найден
                    fileExist = SSHClient.checkLockFile(userName, password, host);
                    System.out.println(fileExist);
                    Thread.sleep(100);
                    count++;
                    //Ждем 7 секунд этого времени должно хватить за глаза
                    if (count>70){
                        System.out.println("7 секунд прошло больше не ждем удаления tmp/lock.tmp");
                        break;
                    }
                }

                //Получить файлы. Будем забирать с tmp дирректории из-за ограничений
                channelSFTP.get("/tmp/" + fileNameShelltargz, fileNamepath);
                //Удаляем архив с сервера
                delOldLogFileInTmp(fileNameShelltargz);
                //Выгружаем в инфодиод
                uploadFilesToInfodiod(nodeNumber,fileNamepath,fileNameShelltargz );
            }
            System.out.println("Finish");

            channelSFTP.disconnect();
            inputStrim.close();
            ops.close();
            ps.close();
            channelShell.disconnect();
    }

    public void uploadFileToFTP(ArrayList<String> listPathLogs, String nodeNumber ) throws InterruptedException, JSchException, IOException, SftpException {

        String programmPath = System.getProperty("user.dir");
        Paths filePath = null;
        Path filedir = filePath.get(programmPath, "files");

        File dilesDir = new File(filedir.toString());
        if (!dilesDir.exists()){
            dilesDir.mkdir();
        }

        ChannelSftp channelSFTP = (ChannelSftp) getChannelSFTP();
        Channel channelShell =  getChannelSHH();
        OutputStream ops = channelShell.getOutputStream();
        PrintStream ps = new PrintStream(ops);
        ps.println(loginTechUser);

        InputStream inputStrim = channelShell.getInputStream();

        for (String puthname: listPathLogs) {
            int lenght = puthname.split("/").length;
            String filename = puthname.split("/")[lenght-1];
            String fileNameShell = filename;
            String fileNameShellWithoutExt = FilenameUtils.removeExtension(fileNameShell);
            String fileNameShelltargz = fileNameShellWithoutExt + ".tar.gz";
            String fileNameShelltmp = fileNameShellWithoutExt +".tmp";
            String extension = filename.endsWith(".gz") ? "gz" : "log";
            String fileNamepath = filePath.get(programmPath, "files", fileNameShelltargz).toString();

            String cdTmpCommand = "cd /tmp/";
            String cpLogCommandToTmp = "cp " + puthname + " /tmp/";
            String command = extension.equals("gz") ? "zcat":"cat";
            String cmd = command + " /tmp/" + fileNameShell + "|sed  -re 's/([0-9]{13,19})/XXXXXXXX/g'> /tmp/" + fileNameShelltmp;
            String rmCommand = "rm -f /tmp/"+fileNameShell;
            String mvCommand = "mv /tmp/"+fileNameShelltmp + " /tmp/" + fileNameShellWithoutExt;
            String tarLogCommand = "tar -czf /tmp/" + fileNameShelltargz + " /tmp/" + fileNameShellWithoutExt + " --remove-files";
            String createLockFile = "touch /tmp/lock.tmp";
            String rmLockFile = "rm -f /tmp/lock.tmp";

            String commandShell = cdTmpCommand + " && " + cpLogCommandToTmp + " && " + cmd + " && " + rmCommand + " && " + mvCommand + " && " + tarLogCommand + " && " + rmLockFile;
            ps.println(createLockFile);
            ps.flush();
            ps.println(commandShell);
            ps.flush();

            int fileExist=0;
            int count=0;
            //wait file log
            while(fileExist==0){
                // 0 найден лок файл. 1 не найден
                fileExist = SSHClient.checkLockFile(userName, password, host);
                System.out.println(fileExist);
                Thread.sleep(100);
                count++;
                //Ждем 7 секунд этого времени должно хватить за глаза
                if (count>70){
                    System.out.println("7 секунд прошло больше не ждем удаления tmp/lock.tmp");
                    break;
                }
            }
            //Получить файлы. Будем забирать с tmp дирректории из-за ограничений
            channelSFTP.get("/tmp/" + fileNameShelltargz, fileNamepath);
            //Удаляем архив с сервера
            delOldLogFileInTmp(fileNameShelltargz);
            //Выгружаем в инфодиод
            uploadFilesToFTP(nodeNumber,fileNamepath,fileNameShelltargz );
        }
        System.out.println("Finish");

        channelSFTP.disconnect();
        inputStrim.close();
        ops.close();
        ps.close();
        channelShell.disconnect();
    }
    public static int checkLockFile(String uName,String passWord, String hostName) throws JSchException, IOException {
        int result = 0;
        JSch jsch = new JSch();
        Channel channel;
        Session session;
        JSch.setConfig(STRICT_KEY_CHECKING, "no"); //Constants.NO
        session = jsch.getSession(uName, hostName, 22);
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
       // session.setConfig("max_input_buffer_size","500000");
        session.setPassword(passWord);
        session.connect(30000);
        channel =(ChannelShell) session.openChannel("shell");
        channel.connect();

        OutputStream ops = channel.getOutputStream();
        PrintStream ps = new PrintStream(ops);
        ps.println(loginTechUser);
        ps.flush();
        ps.println("cd /tmp/ && test -f /tmp/lock.tmp; echo $?_\"checkFile\"");
        ps.close();
        ops.close();
        InputStream inputStream = channel.getInputStream();

        int isLockFile = 0;
        byte[] tmp = new byte[1024];
        while (true)
        {
            while (inputStream.available() > 0)
            {
                int i = inputStream.read(tmp, 0, 1024);
                if (i < 0)
                    break;

                System.out.print(new String(tmp, 0, i));
                String respons = new String(tmp, 0, i);
                if (respons!=null){
                    for (String s : respons.split("\r\n")){
                        if (s.endsWith("_checkFile")){
                            isLockFile = Integer.valueOf(s.trim().substring(0,1));
                        }
                    }
                }
            }
            if (channel.isClosed())
            {
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception ee)
            {
            }
            channel.disconnect();
            session.disconnect();
        }
        inputStream.close();
        return isLockFile;
    }

    public static void delOldLogFileInTmp(String path) throws InterruptedException, JSchException, IOException {
        ChannelSftp channelSFTP = (ChannelSftp) getChannelSFTP();
        Channel channelShell =  getChannelSHH();
        OutputStream ops = channelShell.getOutputStream();
        PrintStream ps = new PrintStream(ops);
        ps.println(loginTechUser);
        ps.println("rm -f /tmp/"+path);
        ps.close();
        ops.close();
    }

    public StringBuilder getListLogFiles() throws IOException, JSchException, InterruptedException {
        Channel channel =  getChannelSHH();
        OutputStream ops = channel.getOutputStream();
        PrintStream ps = new PrintStream(ops);
        ps.println(loginTechUser);
        String logsdir = techUser.equalsIgnoreCase("SPP") ? logsDirSPP : logsDirSFM;
        ps.println("cd " + logsdir);
        ps.println("ls -1t */*");
        ps.close();

        StringBuilder builder = new StringBuilder();
        InputStream commandOutput = channel.getInputStream();
        Thread.sleep(3000);

        //byte[] bt = new byte[32768];
        byte[] bt = new byte[commandOutput.available()];
        String str = null;
        int tim =0;
        while(tim < 70 ) {
            tim++;
            while (commandOutput.available() > 0) {
                int i = commandOutput.read(bt, 0, bt.length);
                System.out.println("Размер буфера = " + i);
                str = new String(bt, 0, i);
                System.out.println(str
                        .replaceAll("(.*)31m{1,}","")
                        .replaceAll("\u001B\\[0m$","")
                        .replaceAll("\u001B\\[01;34m","")
                        .replaceAll("\u001B\\[0m",""));
                builder.append(str);
            }
            if (commandOutput.available()==0) tim=70; //Больше ожидать наполнения буфера не будем. После увеличения буфера до 500 000 стало все умещаться в одну итерацию
            Thread.sleep(100);
            System.out.println("Ожидание дополнений в буфер = " + ((tim==70) ? "Не будем ждать":tim ));
            }
       commandOutput.close();
       //ps.close();
       channel.disconnect();
    return builder;
    }

    private void uploadFilesToInfodiod(String nodeNumber,String fullPathFileName, String fileName ) throws IOException {
        String ftpServer = properties.getProperty("diodein");
        String workingDirektory = properties.getProperty("diodWorkingDirektory");
        String ftpUsername = properties.getProperty("diodUser");
        String ftpPassword = Encryptor.decrypt(Encryptor.key,Encryptor.initVector,properties.getProperty("diodPassword"));

        SSLSessionReuseFTPSClient ftpsClient = new SSLSessionReuseFTPSClient();
        ftpsClient.addProtocolCommandListener(new PrintCommandListener(System.out));
        try {
            ftpsClient.connect(ftpServer, 21);

            ftpsClient.login(ftpUsername, ftpPassword);
            ftpsClient.enterLocalPassiveMode(); // Run the passive mode command now  instead of after loggin in.
            ftpsClient.setControlEncoding("UTF-8");
            int code = ftpsClient.type(FTP.BINARY_FILE_TYPE);
            System.out.println("code = " + code);
            Boolean codeBoolean = ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println("code = " + codeBoolean);
            ftpsClient.execPBSZ(0);
            ftpsClient.execPROT("P");
            ftpsClient.changeWorkingDirectory(workingDirektory);

            InputStream inputStream;
            File file = new File(fullPathFileName);
            inputStream = new FileInputStream(file);

            Boolean append = ftpsClient.storeFile(nodeNumber + "-" + file.getName(), inputStream);
            System.out.println("append = " + append);

            inputStream.close();
            ftpsClient.logout();
            ftpsClient.disconnect();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    private void uploadFilesToFTP(String nodeNumber,String fullPathFileName, String fileName ) throws IOException {
        String ftpServer = properties.getProperty("ftpHost");
        String workingDirektory = properties.getProperty("ftpWorkingDirektory");
        String ftpUsername = properties.getProperty("ftpUser");
        String ftpPassword = Encryptor.decrypt(Encryptor.key,Encryptor.initVector,properties.getProperty("ftpPass"));

        //SSLSessionReuseFTPSClient ftpsClient = new SSLSessionReuseFTPSClient();
        FTPSClient ftpsClient = new FTPSClient();
        ftpsClient.addProtocolCommandListener(new PrintCommandListener(System.out));
        try {
            ftpsClient.connect(ftpServer, 21);
            ftpsClient.login(ftpUsername, ftpPassword);
            ftpsClient.enterLocalPassiveMode(); // Run the passive mode command now  instead of after loggin in.
            ftpsClient.setControlEncoding("UTF-8");
            int code = ftpsClient.type(FTP.BINARY_FILE_TYPE);
            System.out.println("code = " + code);
            Boolean codeBoolean = ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println("code = " + codeBoolean);
            ftpsClient.execPBSZ(0);
            ftpsClient.execPROT("P");
            ftpsClient.changeWorkingDirectory(workingDirektory);

            InputStream inputStream;
            File file = new File(fullPathFileName);
            inputStream = new FileInputStream(file);

            Boolean append = ftpsClient.storeFile(nodeNumber + "-" + file.getName(), inputStream);
            System.out.println("append = " + append);

            inputStream.close();
            ftpsClient.logout();
            ftpsClient.disconnect();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}