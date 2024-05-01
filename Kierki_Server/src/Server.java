
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Klasa servera która zawiera wszelkie funkcjonalności potrzebne do połączenia się użytkowników
 * oraz przechowuje liste uzytkowników oraz pokoi
 */
public class Server {
    /**
     * Array lista użytkowników którzy będą łączyli się przez socket z Serverem
     */
    private static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    /**
     * Array lista pokoi w których toczy się rozgrywka do której ma dostęp każdy z graczy
     */
    private static ArrayList<Room> rooms = new ArrayList<Room>();


    /**
     * klasa main serwera w której inicjalizujemy Socket oraz Server socket oraz oczekujemy
     * na połączenie się użytkowników którymi będzie dalej zajmował się ClientHandler dla których rozpoczynamy
     * osobne wątki
     * @param args argumenty wywołania programu
     */
    public static void main(String[] args){
        ServerSocket serverSocket;
        Socket socket;
        try {
            serverSocket = new ServerSocket(1234);
            while(true) {
                System.out.println("Waiting for clients...");
                socket = serverSocket.accept();
                System.out.println("A new client has connected.\n");
                ClientHandler clientThread = new ClientHandler(socket, clients,rooms);
                clients.add(clientThread);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
