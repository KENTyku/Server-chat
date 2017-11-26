/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverchat;

/**
 *
 * @author Yuri Tveritin
 */
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServerChat implements IConstants {// основной класс сервера
    ServerSocket server;//серверный сокет
    Socket socket;//клиентский сокет
    ArrayList<ClientHandler> clients;// список подключенных клиентов

    public static void main(String[] args) { //создание сервера
        new ServerChat();
    }

    //Конструктор сервера
    ServerChat() {
        System.out.println(SERVER_START);
        
        // поток для управления выключенем сервера по команде с консоли
        new Thread(new CommandHandler()).start();
        
        //создаем список клиентов
        clients=new ArrayList<>();
        
        try {
            //создаем сокет сервера
            server = new ServerSocket(SERVER_PORT);
            
            //запускаем бесконечный цикл работы сервера
            while (true) {
                socket = server.accept();//когда клиентский сокет подключится к серверу
                System.out.println("#" + (clients.size()+1) + CLIENT_JOINED);//вывод информации о подключившемся клиенте
                ClientHandler client=new ClientHandler(socket);//создание объекта обработки клиентского сокета 
                clients.add(client);//добавление в список
                new Thread(client).start();//стартуем поток объекта 
				//взаимодествия с клиентским сокетом
            }
        } catch (Exception ex) {
            System.out.println("Сокет сервера был закрыт:"+ex.getMessage());
        }
        System.out.println(SERVER_STOP);
    }

    /**
     *  метод checkAuthentication: проверка логина и пароля
     */
    private boolean checkAuthentication(String login, String passwd) {
        Connection connect;// переменная подключения
        String url=DRIVER_NAME+"://"+SERVER_ADDR+":"+BAZE_PORT+"/"+SQLITE_DB+PROPERTISE;        
        boolean result = false;
        try {
            // connect db            
            connect = DriverManager.getConnection(url,USERNAME,PASSWORD);//создание объекта подключения к базе
            // looking for login && passwd in db
            Statement stmt = connect.createStatement();//объект для выполнения запросов к базе
            ResultSet rs = stmt.executeQuery(SQL_SELECT.replace("?", login));//результат запроса к базе
            while (rs.next())//перевод курсора на следующую строку для считывания результата запроса
                result = rs.getString(PASSWD_COL).equals(passwd);//сравнение результата запроса
				//с поданным на вход метода значением пароля
            // close all
            rs.close();
            stmt.close();
            connect.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * Выключение сервера.
     * Класс описывает работу самого сервера
	который отвечает на запросы с консоли
        * (обработчик команд на сервере)
     */
    class CommandHandler implements Runnable {
        Scanner scanner = new Scanner(System.in);        
        @Override // т.к. расширяет интерфейс Runnable то необходимо переопределить метод run
        public void run() {
            String command;            
            do
                command = scanner.nextLine();//считывать с клавиатуры 
            while (!command.equals(EXIT_COMMAND));//пока пользователь не напишет exit
             try {
                server.close();
                System.exit(0);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    /*
    Многоадрессное сообщение всем
    */
    void broadcasMsg(String msg){
        for (ClientHandler client: clients){
            client.sendMsg(msg);
        }
    }
    /**
     * Класс обработки запросов клиентов (Обработчик клиентов) 
     */
    class ClientHandler implements Runnable {   //класс отвечающих клиентов
        BufferedReader reader;
        PrintWriter writer;//класс вывода в поток (в нашем случае поток-сокет клиента)
        Socket socket;
        String name;
        
        //конструктор обработчика

        ClientHandler(Socket clientSocket) {
            try {
                socket = clientSocket;
                reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())); //сообщение пришедшее от  сокета клиента
                writer = new PrintWriter(socket.getOutputStream());  //сообщение отправляемое в сокет клиента
                name = "Client #" + (clients.size()+1);
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        
        //метод отправки сообщения клиенту
        
        void sendMsg(String msg){
            try{
                writer.println(msg);//отправка сообщения msg в сокет клиента
                writer.flush();
            }
            catch(Exception ex){                
                System.out.println(ex.getMessage());        
            }
        }
       
        
        @Override // переопределяем метод для потока обработки подключившегося клиента
        public void run() {
            String message;
            try {
                //отправка сообщения клиенту который подключился
                sendMsg("Connecting to server...");
                
                //циклическая обработка пришедших от клиента сообщений
                do {                    
                    message = reader.readLine();// присвоение переменной сообщения, пришедшего от сокета клиента 
                    if (message != null) {
                        //вывод в консоль сервера непустого сообщения
                        System.out.println(name + ": " + message);
                        /*
                        проверка на получение сообщения авторизации 
                        */
                        if (message.startsWith(AUTH_SIGN)) {// если начало сообщения совпадает с авторизационным полем
                            String[] wds = message.split(" ");// то режем через пробелы на куски и помещаем
                            //в строковый массив (три куска)
                            if (checkAuthentication(wds[1], wds[2])) {//2ой и 3ий кусок оправляем на проверку и если она 
                                name = wds[1];// успешна, выводим приветствие в сокет клиента
                                sendMsg("Hello, " + name);
                                sendMsg("\0");//вспомогательное сообщение для клиента(чтобы отловить прилгашение на стороне клиента)
                            } else {
                                System.out.println(name + ": " + AUTH_FAIL);
                                sendMsg(AUTH_FAIL);// иначе выводим отказ в сокет клиента
                                message = EXIT_COMMAND;
                            }
                        } else if (!message.equalsIgnoreCase(EXIT_COMMAND)) {//пока massage не равно exit
                            broadcasMsg(name+": " + message);   //пишем в сокеты клиентов, то что получлили от него
                            broadcasMsg("\0");
                        }
                        writer.flush();//иначе очищаем буфер вывода (для последующего закрытия сокета клиента) 
                    }
                } while (!message.equalsIgnoreCase(EXIT_COMMAND));//пока massage не равно exit выполняется код  do 
                clients.remove(this);
                socket.close();
                System.out.println(name + CLIENT_DISCONNECTED);
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}