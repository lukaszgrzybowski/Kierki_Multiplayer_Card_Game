import java.io.IOException;
import java.util.*;

public class Room {

    //pełna talia do gry w karty
    /**
     * pełna talia do gry w karty
     */
    public ArrayList<ArrayList<String>> playingCards = new ArrayList<>();

    //4 talie każdego gracza po 13 kart rozlosowane z playingCards
    /**
     * karty która posiada tylko i wyłącznie ten gracz
     */
    public ArrayList<ArrayList<ArrayList<String>>> playerCards = new ArrayList<>();
    //tablica na podstawie której jesteśmy w stanie wyznaczyć wyższość kart
    /**
     * kolejność kart według ich starszeńśtwa w talii
     */
    public ArrayList<String> cardsOrder = new ArrayList<>();
    //karty będące na stole
    /**
     * karty znajdujące się na stole
     */
    public  ArrayList<ArrayList<String>> cardsOnTable = new ArrayList<>();
    //która karta na stole jest kogo po indeksach
    /**
     * posiadacze kart znajdujących się na stole wedłuh Id
     */
    public ArrayList<Integer> cardsOnTableOwners = new ArrayList<>();
    //czyja kolej w rozgrywaniu rund
    /**
     * czyja kolej w danej rundzie
     */
    int whosTurnInRounds;
    //czyja kolej w pełnych rozdaniach
    /**
     * czyja kolej w danym rozdaniu kart
     */
    int whosTurnInCardDeal;

    //liczba punktów każdego z graczy
    /**
     * punkty gracza w grze
     */
    ArrayList<Integer> playersPoints = new ArrayList<>();
    //która tura rozgrywki
    /**
     * które ro rozdanie
     */
    int playingCardDeal =0;
    //która runda rozgrywki
    /**
     * która to runda
     */
    int playingRound=0;


    //lista clientHandler_id graczy
    /**
     * Id wszystkich klientów w grze
     */
    ArrayList<Integer> clientHandersIdInGame = new ArrayList<>();
    //nazwy graczy
    /**
     * nazwy graczy w grze
     */
    public ArrayList<String> playersNames = new ArrayList<>();

    /**
     * konstruktor pokoju inicjalizujący pola oraz ustawiąjący wszystko na początek rozgrywki
     */
    public Room(){
        setPlayingCards();
        setStartingPlayersPoints();
        setCardsOrder();
        playingCardDeal=1;
        playingRound=1;
        setPlayerCardsEmptySets();
        shuffleCardsToPlayers();
        randomChoiceOfTheDealer();
        setPlayerNamesStart();

    }

    //good
    //ustawiamy początkową wartość punktów dla wszystkich graczy

    /**
     * ustawienie punktów wszystkich graczy na 0
     */
    private void setStartingPlayersPoints(){
        for(int i=0;i<4;i++){
            playersPoints.add(0);
        }
    }

    //good
    //przygotowywujemy 4 tablice na karty dla graczy

    /**
     * ustawienie tali wszystkich graczy na puste
     */
    private void setPlayerCardsEmptySets(){
        for(int i=0;i<4;i++){
            playerCards.add(new ArrayList<ArrayList<String>>());
        }
    }

    //ustawiamy początkowe imiona graczy(puste)

    /**
     * ustawienie pustych nazw graczy w pokoju
     */
    public void setPlayerNamesStart(){
        for(int i=0;i<4;i++){
            playersNames.add("-1");
        }
    }


    //good
    //dodanie wszystkich kart do talii

    /**
     * ustawienie całej talii kart w pokoju
     */
    private void setPlayingCards(){
        String cardColor = null;
        for(int i=0;i<4;i++){
            if(i==0){
                cardColor="Kier";
            } else if(i==1){
                cardColor="Karo";
            }else if(i==2){
                cardColor="Trefl";
            }else if(i==3){
                cardColor="Pik";
            }

            for(int j=2;j<11;j++) {
                playingCards.add(new ArrayList<>(Arrays.asList(String.valueOf(j),cardColor)));
            }
            playingCards.add(new ArrayList<>(Arrays.asList("Walet",cardColor)));
            playingCards.add(new ArrayList<>(Arrays.asList("Dama",cardColor)));
            playingCards.add(new ArrayList<>(Arrays.asList("Król",cardColor)));
            playingCards.add(new ArrayList<>(Arrays.asList("As",cardColor)));

        }
        //pomoc
        //printAllCards();
    }

    //good
    //funkcja wyświetla wszystkie karty znajdujące się w talii

    /**
     * wyświetlenie gotowej talii kart
     */
    public void printAllCards(){
        for(ArrayList<String> card : playingCards ){
            System.out.println(card.get(0)+" "+card.get(1));
        }
    }

    /**
     * wyswietlenie stanu pokoju
     */
    public void printRoomState(){
        System.out.println("Stan pokoju:");
        System.out.println("Rozdanie:"+playingCardDeal);
        System.out.println("Runda:"+playingRound);
        System.out.println("Cards on Table:");
        for (ArrayList<String> card: cardsOnTable){
            System.out.println(card.get(0)+" "+card.get(1));
        }
        System.out.println("Players Names:");
        for (String names: playersNames){
            System.out.println(names);
        }
        System.out.println("Players Points:");
        for (Integer points: playersPoints){
            System.out.println(points);
        }
    }

    //good
    //rozlosowanie kart z talii na talie graczy

    /**
     * potasowanie kart do graczy na początku rozgrywki
     */
    public void shuffleCardsToPlayers(){
        Random random = new Random();
        int[] arrayForPlayingCardsOrder = new int[52];
        for(int i=0;i<52;i++){
            arrayForPlayingCardsOrder[i]=i;
        }
        //losowa zamiana miejsc
        for(int i=0;i<arrayForPlayingCardsOrder.length;i++){
            int swap = random.nextInt(arrayForPlayingCardsOrder.length-i)+i;
            int temp = arrayForPlayingCardsOrder[swap];
            arrayForPlayingCardsOrder[swap] = arrayForPlayingCardsOrder[i];
            arrayForPlayingCardsOrder[i]=temp;
        }

        int counter=0;
        for(int i=0;i<4;i++){
            for(int j=0;j<13;j++){
                playerCards.get(i).add(playingCards.get(arrayForPlayingCardsOrder[counter]));
                counter++;
            }
        }
    }


    //do sprawdzenia
    //przejście do następnej rundy(dodać licznik rund, dodać znów karty do talii, użytkownik następny w kolejności zaczyna)

    /**
     * funkcja ustawiająca wszystkie pola tak aby przejść do następnej rundy
     */
    public void goToTheNextRound(){
        playingRound++;
            int index = checkWhoWonRound();
            int points = addPointsToTheRoundWinner(index);
            playersPoints.set(index, points);
            cardsOnTable.clear();
            cardsOnTableOwners.clear();
            whosTurnInRounds += 2;
            if (whosTurnInRounds > 3) {
                whosTurnInRounds -= 4;
            }
        if(playingRound==14) {
            goToTheNextCardDeal();
        }
    }

    /**
     * przejście do następnego rozdania kart
     */
    public void goToTheNextCardDeal(){
        shuffleCardsToPlayers();
        playingRound=1;
        playingCardDeal++;
        if(playingCardDeal==3){
            System.out.println("GAME OVER!!!");
        }
    }

    /**
     * losowe wybranie rozdającego
     */
    //funckja losująca rozdającego
    public void randomChoiceOfTheDealer(){
        Random random= new Random();
        this.whosTurnInCardDeal =random.nextInt(4);
        this.whosTurnInRounds=whosTurnInCardDeal;
    }



    //sprawdzenie kto wygrał rundę

    /**
     * sprawdzenie kto wygrał daną rundę
     * @return zwrócenie indeksu wygranego
     */
    public int checkWhoWonRound(){
        String mainColor;
        mainColor = cardsOnTable.get(0).get(1);
        ArrayList<String> winnerCard = cardsOnTable.get(0);
        int winnerIndex;
        for(ArrayList<String> playerCard :cardsOnTable){
           if(playerCard.get(1).equals(mainColor)){
               if(cardsOrder.indexOf(winnerCard.get(0))<cardsOrder.indexOf(playerCard.get(0))){
                   winnerCard=playerCard;
               }
           }
        }
        winnerIndex=cardsOnTableOwners.get(cardsOnTable.indexOf(winnerCard));
        return winnerIndex;
    }

    //dodanie punktów zwycięzcy po zakończonej rundzie

    /**
     * przygotowanie punktów  do dodania wygranemu w rozgrywce
     * @param winnerIndex indeks osoby która wygrała rundę
     * @return zwrócenie ilości punktów do dodania
     */
    public int addPointsToTheRoundWinner(int winnerIndex){
        int points=playersPoints.get(winnerIndex);
        if(playingCardDeal==1) {
            points -= 20;
        }else if(playingCardDeal==2){
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(1).equals("Kier")){
                    points-=20;
                }
            }
        }else if(playingCardDeal==3){
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(0).equals("Dama")){
                    points-=60;
                }
            }
        }else if(playingCardDeal==4){
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(0).equals("Król") || card.get(0).equals("Walet")){
                    points-=30;
                }
            }
        }else if(playingCardDeal==5){
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(0).equals("Król") && card.get(1).equals("Kier")){
                    points-=150;
                }
            }
        }else if(playingCardDeal==6){
            if(playingRound!=7 && playingRound!=13){
                points-=75;
            }
        }else if(playingCardDeal==7){
            if(playingRound!=7 && playingRound!=13){
                points-=75;
            }
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(0).equals("Król") && card.get(1).equals("Kier")){
                    points-=150;
                }
            }
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(0).equals("Król") || card.get(0).equals("Walet")){
                    points-=30;
                }
            }
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(0).equals("Dama")){
                    points-=60;
                }
            }
            for(ArrayList<String> card: cardsOnTable){
                if(card.get(1).equals("Kier")){
                    points-=20;
                }
            }
            points -= 20;

        }

        return points;
    }

    //good
    //ustawiamy tablicę wyższości kart

    /**
     * ustawienie talii ważności/starszeństwa kart
     */
    private void setCardsOrder(){
        for(int j=1;j<11;j++) {
            cardsOrder.add(String.valueOf(j));
        }
        cardsOrder.add("Walet");
        cardsOrder.add("Dama");
        cardsOrder.add("Król");
        cardsOrder.add("As");
    }

    //dodanie clientHandlerId do pokoju
    //zwraca 0 jeśli się udało 1 jeśli w pokoju jest już 4 graczy

    /**
     * dodanie klienta do pokoju
     * @param clientIndex indeks klienta
     */
    public void addClientToGameRoom(int clientIndex){
        if(clientHandersIdInGame.size()<4) {
            clientHandersIdInGame.add(clientIndex);
        }
    }

    /**
     * dodanie loginu klienta do pokoju
     * @param clientName nazwa klienta
     */
    public void addClientNameToGameRoom(String clientName){
        int numberOfClients=clientHandersIdInGame.size();
        playersNames.set(numberOfClients-1,clientName);
    }



    //przetworezenie wiadomości dostanej od gracza

    /**
     * Analiza wiadomości od klienta
     * @param messageFromClient wiadomość od klienta
     * @return zwrot jeśli błąd
     */
    public int processMessageFromClient(String messageFromClient) {
        try {
            String[] data = messageFromClient.split(":");
            cardsOnTable.add(new ArrayList<>(Arrays.asList(data[2], data[3])));
            playerCards.get(whosTurnInRounds).remove(new ArrayList<>(Arrays.asList(data[2], data[3])));
            cardsOnTableOwners.add(whosTurnInRounds);
            if (cardsOnTable.size() == 4) {
                goToTheNextRound();
                if (playingCardDeal == 8) {
                    return 1;
                }
            } else {
                if (whosTurnInRounds == 3) {
                    whosTurnInRounds = 0;
                } else {
                    whosTurnInRounds++;
                }
            }
            return 0;
        }catch(RuntimeException e){
            return 2;
        }
    }


    //przygotowanie wiadomości dla graczy o stanie pokoju

    /**
     * stworzenie wiadomości oddzielnie dla każdego gracza o jego kartach oraz stanie pokoju
     * @param clientId id klienta
     * @param roomNumber numer pokoju
     * @return wiadomość do wysłania
     */
    public String createMessageOfRoomState(int clientId,int roomNumber){
        StringBuilder message = new StringBuilder(new String());
        //typ komunikatu 1 - zagranie, 2 - czat
        message = new StringBuilder("1:");
        //numer pokoju
        message.append(roomNumber);
        message.append(":");
        //nazwy wszystkich graczy
        message.append(playersNames.get(0)).append(":").append(playersNames.get(1)).append(":").append(playersNames.get(2)).append(":").append(playersNames.get(3)).append(":");
        //punkty wszystkich graczy
        for (Integer playersPoint : playersPoints) {
            message.append(playersPoint);
            message.append(":");
        }
        //która runda
        message.append(playingRound);
        message.append(":");
        //które rozdanie
        message.append(playingCardDeal);
        message.append(":");
        //czyja kolej w rundzie (gracz dostaje 1 lub 0 czy jest czy nie jest jego kolej)
        int actualIdInTheRoom=clientHandersIdInGame.indexOf(clientId);
        if(actualIdInTheRoom==whosTurnInRounds) {
            message.append("1");
        }else{
            message.append("0");
        }
        message.append(":");
        //ilość kjart na stole
        message.append(cardsOnTable.size());
        message.append(":");
        //wszystkie karty na sole oraz do kogo one należą
        for(int i=0;i<cardsOnTable.size();i++){
            message.append(cardsOnTable.get(i).get(0));
            message.append(":");
            message.append(cardsOnTable.get(i).get(1));
            message.append(":");
        }
        // ilość kart gracza
        message.append(playerCards.get(actualIdInTheRoom).size());
        message.append(":");
        //karty gracza
        for(int i=0;i<playerCards.get(actualIdInTheRoom).size();i++){
            message.append(playerCards.get(actualIdInTheRoom).get(i).get(0));
            message.append(":");
            message.append(playerCards.get(actualIdInTheRoom).get(i).get(1));
            message.append(":");
        }

        return message.toString();
    }




}
