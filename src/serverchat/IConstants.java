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
interface IConstants {
    final String DRIVER_NAME = "jdbc:mysql";
    final String SQLITE_DB = "users";
    final String PROPERTISE = "?useSSL=no";
    final String SERVER_ADDR = "localhost"; // server net name or "127.0.0.1"
    final int SERVER_PORT = 2048; // servet port
    final int BAZE_PORT = 3306; // servet port
    final String USERNAME = "root"; //пользователь БД
    final String PASSWORD = "123456"; //пароль БД
    final String SERVER_START = "Server is started...";
    final String SERVER_STOP = "Server stopped.";
    final String CLIENT_JOINED = " client joined.";
    final String CLIENT_DISCONNECTED = " disconnected.";
    final String CLIENT_PROMPT = "$ "; // client prompt
    final String LOGIN_PROMPT = "Login: ";
    final String PASSWD_PROMPT = "Passwd: ";
    final String AUTH_SIGN = "auth";
    final String AUTH_FAIL = "Authentication failture.";
    final String SQL_SELECT = "SELECT * FROM users WHERE login = '?'";
    final String PASSWD_COL = "passwd";
    final String CONNECT_TO_SERVER = "Connection to server established.";
    final String CONNECT_CLOSED = "Connection closed.";
    final String EXIT_COMMAND = "exit"; // command for exit
}
