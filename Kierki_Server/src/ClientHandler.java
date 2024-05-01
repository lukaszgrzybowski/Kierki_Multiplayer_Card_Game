

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 * Klasa ClientHandler obsługuje kontakty między klientem a serwerem oraz przeprowadza aktualizację danych na serwerze
 */
public class ClientHandler extends Thread{

    /**
     * socket Object do który otrzymujemy od serwera, na którym jest podłączony klient
     */
    private Socket socket;

    //tablica użytkowników
    /**
     *  lista wszystkich klientów krórą  dostajemy z serwera
     */
    private ArrayList<ClientHandler> clientsHandlers;
    //tablica pokoi do gry
    /**
     * lista wszystkich utworzonych pokoi otrzymywana z serwera
     */
    private  ArrayList<Room> rooms;
    /**
     * Objekt kalsy PrintWriter służący nam do wysyłania wiadomości do klientów
     */
    PrintWriter writer;
    /**
     * Objekt kalsy BufferedReader służący nam do odbierania wiadomości które klient wysyła nam przez socket
     */
    BufferedReader bufferedReader;

    //nazwa użytkownika
    /**
     * Nazwa naszego klienta który podłączył się do serwera
     */
    private String clientUsername;

    /**
     * Konstruktor ClientHandler inicjalizuje wszystkie pola oraz oczekuje na pierwszą
     * wiadomość od klienta z jego loginem, oraz informuje nas o jego połączeniu. W razie błądu natychmiast wszystko zamyka.
     * @param socket socket przekazany od Serwera dla ClientHandlera
     * @param clients Lista wszystkich klientów aktualnie będących podłączonymi do serwea
     * @param rooms Lista wszystkich pokoi aktualnie bedących na serwerze
     */
    public ClientHandler(Socket socket,ArrayList<ClientHandler> clients,ArrayList<Room> rooms){
        try{
            this.socket=socket;
            this.clientsHandlers = clients;
            this.rooms=rooms;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.clientUsername=bufferedReader.readLine();
            if(!rooms.isEmpty()){
                broadcastMessage(3,0);
            }
            System.out.println("Client: "+clientUsername+" has entered the menu!");
        }catch(IOException e){
            closeEverythingAndLeavingRoomsForClient(socket,bufferedReader, writer);
        }
    }


    //wiadomości z 1-gra, z 2-dodaj pokój, 3-dodaj użytkownika do pokoju,4 zaproś innego gracza
    /**
     *  funkcja run oczekuje na wiadomośc od klienta, a następnie przekazuje wiadomość do tłumaczenia
     *  Jeśli klient się odłączy od serwera również funkcja na to reaguje wymazując go z serwera.
     */
    @Override
    public void run() {
            String  messageFromClient;
            broadcastMessage(3,0);
            while(true){
                try {
                    if (!((messageFromClient = bufferedReader.readLine()) != null)) break;
                    } catch (IOException e) {
                        closeEverythingAndLeavingRoomsForClient(socket,bufferedReader, writer);
                        return;
                    }
                    if(messageFromClient.equalsIgnoreCase("exit")) {
                        break;
                    }
                    System.out.println("Odebrano: "+messageFromClient);
                    translateMessage(messageFromClient);
            }
    }

    /**
     * Funkcja tłumaczy komunikaty otrzymane od klienta we właściwy sposób,
     * a następnie odsyła właściwą odpowiedź o stanie menu, pokojku lub nakazującą zakończenie rozgrywki
     * @param messageFromClient wiadomość odebrana od klienta
     */
    public void translateMessage(String messageFromClient){
        String[] data = messageFromClient.split(":");
        if(data[0].equals("1")) {
            if(rooms.get(Integer.parseInt(data[1])).processMessageFromClient(messageFromClient)==2){
                closeEverythingAndLeavingRoomsForClient(socket,bufferedReader,writer);
            }
            broadcastMessage(1, Integer.parseInt(data[1]));
        }else if(data[0].equals("2")){
            for(ClientHandler clientHandler : clientsHandlers){
                if(!clientHandler.clientUsername.equals(data[2])) {
                    clientHandler.writer.println(messageFromClient);
                    System.out.println("Wysyłamy: "+messageFromClient);
                }
            }
        }else if(data[0].equals("3")){
            rooms.add(new Room());
            broadcastMessage(3,0);
        }else if(data[0].equals("4")){
            for(ClientHandler clientHandler : clientsHandlers){
                if(clientHandler.clientUsername.equals(data[1])){
                    clientHandler.writer.println("4:"+data[3]+":"+data[2]+":");
                    System.out.println("Wysyłamy: "+"4:"+data[3]+":"+data[2]+":");
                }
            }
        }else if(data[0].equals("5")){
            //jeśli już ten gracz jest w pokoju jest w pokoju
            if(rooms.get(Integer.parseInt(data[1])).playersNames.contains(data[2])) {
                broadcastMessage(1, Integer.parseInt(data[1]));
            }else{
                //jesli ten gracz nie jest w pokoju sprawdzamy czy możemy go dodać
                int counter1=0;
                for(String playerName:rooms.get(Integer.parseInt(data[1])).playersNames){
                    if(playerName.equals("-1")){
                        counter1++;
                    }
                }
                if(counter1>0){
                    int counter2=0;
                    for(String playerName:rooms.get(Integer.parseInt(data[1])).playersNames){
                        if(playerName.equals(data[2])){
                            counter2++;
                            break;
                        }
                    }
                    if(counter2==0) {
                        rooms.get(Integer.parseInt(data[1])).addClientToGameRoom(clientsHandlers.indexOf(this));
                        rooms.get(Integer.parseInt(data[1])).addClientNameToGameRoom(data[2]);
                        broadcastMessage(1, Integer.parseInt(data[1]));
                    }
                }
                if(counter1==0){
                    broadcastMessage(1,Integer.parseInt(data[1]));
                }
            }
        }
    }

    /**
     * funkcja kończąca rundę i wysyłającą losową liczbe punktów dla graczy
     * jako reakcję na błąd lub wyjście gracza z rozgrywki
     * @param roomid numer pokoju dla którego funkcja ma zadziałąć
     */
    public void endGameAndSendRandomPoints(int roomid){
        rooms.get(roomid).playingRound=13;
        rooms.get(roomid).playingCardDeal=3;
        Random random = new Random();
        rooms.get(roomid).playersPoints.set(0,(random.nextInt(20)+1)*-10);
        rooms.get(roomid).playersPoints.set(1,(random.nextInt(20)+1)*-10);
        rooms.get(roomid).playersPoints.set(2,(random.nextInt(20)+1)*-10);
        rooms.get(roomid).playersPoints.set(3,(random.nextInt(20)+1)*-10);
        broadcastMessage(1,roomid);
    }


    /**
     * Funckja wysyłąjąca wiadomości do klientów o stanie pokoju lubo stanie menu
     * @param typeOfMessage rodzaj wiadomości jaka ma zostać wysłana 1-pokój,3-menu
     * @param roomIdIfNeeded numer pokoju jakiego stan mamy wysłać
     */
    //rozesłanie wiadomości do graczy
    public void broadcastMessage(int typeOfMessage,int roomIdIfNeeded){
        for(ClientHandler clientHandler : clientsHandlers){
            //przesłanie stanu pokoju
            if(typeOfMessage==1) {
                if(rooms.get(roomIdIfNeeded).clientHandersIdInGame.contains(clientsHandlers.indexOf(clientHandler))) {
                    String messageToBeSend = rooms.get(roomIdIfNeeded).createMessageOfRoomState(clientsHandlers.indexOf(clientHandler), roomIdIfNeeded);
                    clientHandler.writer.println(messageToBeSend);
                    System.out.println("Wysyłamy: "+messageToBeSend);
                }
            //przesłanie stanu menu
            }else if(typeOfMessage==3) {
                String messageToBeSend = String.valueOf(prepareMessageOfMenuStateForClient(3));
                clientHandler.writer.println(messageToBeSend);
                System.out.println("Wysyłamy: "+messageToBeSend);
            }
        }
        if(typeOfMessage==1){
            rooms.get(roomIdIfNeeded).printRoomState();
        }
    }

    //opuszczenie przez gracza servera

    /**
     * usunięcie klienta z listy wszystkich klientów na serwerze
     */
    public void removeClientHandler(){
        clientsHandlers.remove(this);
        System.out.println("Client: "+clientUsername+" has left the game!");
        broadcastMessage(3,0);
    }

    /**
     * Funkcja zamykająca wszystko w przypadku błędu lub wyjścia klienta z rozgrywki
     * @param socket socket na jakim jest podłączony klient
     * @param bufferedReader służący do odbierania wiadomości
     * @param bufferedWriter służący do wysyłania wiadomości
     */
    public void closeEverythingAndLeavingRoomsForClient(Socket socket, BufferedReader bufferedReader, PrintWriter bufferedWriter){
        for(Room room:rooms){
            if(room.clientHandersIdInGame.contains(clientsHandlers.indexOf(this))){
                endGameAndSendRandomPoints(rooms.indexOf(room));
            }
        }
        removeClientHandler();
        try{
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //funkcje dotyczące obsługi menu

    //wysyłanie wiadomości o stanie menu 3- wysłanie ilości pokoi oraz graczy

    /**
     * Funkcja przygotowywująca stan menui do wysłania klientowi
     * @param typeOfMessage rodzaj wiadomości
     * @return brak wiadomości jeśli błąd przekazanej wiadomości
     */
    public StringBuilder prepareMessageOfMenuStateForClient(int typeOfMessage){
        if(typeOfMessage==3){
            StringBuilder message=new StringBuilder(new String());
            message = new StringBuilder("3:");
            message.append(clientsHandlers.size());
            message.append(":");
            for(ClientHandler client:clientsHandlers){
                message.append(client.clientUsername);
                message.append(":");
            }
            message.append(rooms.size());
            message.append(":");
            return message;
        }
        return null;
    }

}
