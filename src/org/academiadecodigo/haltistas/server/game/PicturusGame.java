package org.academiadecodigo.haltistas.server.game;

import org.academiadecodigo.haltistas.server.Server;

import java.util.*;

public class PicturusGame implements Runnable {


    private Server server;
    private List<String> playerList;
    private LinkedList<String> waitingQueue;
    private String gameWord;
    private int minPlayers = 3;


    public PicturusGame(Server server) {

        this.server = server;
        this.playerList = new ArrayList<>();
        this.waitingQueue = new LinkedList<>();
    }


    @Override
    public void run() {

        while (true) {

            synchronized (this) {

                if (waitingQueue.isEmpty()) {
                    continue;
                }

                while (waitingQueue.size() < minPlayers) {
                    try {

                        server.whisper(
                                waitingQueue.get(
                                        waitingQueue.size() - 1), Encoder.info("waiting..."));
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while (!waitingQueue.isEmpty()) {
                    String name = waitingQueue.poll();
                    server.whisper(name, Encoder.info("The wait is over..."));
                    playerList.add(name);
                }

                server.broadcast(Encoder.info("Starting game!"), playerList); //just to start the game

                notifyAll();
            }
            startingGame();
        }
    }


    private void startingGame() {
        wordToDraw();
    }


    public void drawMessage(String message) {
        server.broadcast(Encoder.draw(message), playerList);
    }


    public void chatMessage(String message) {

        //wordCheck(message);

        // server.broadcast(Encoder.chat(message), playerList);
    }

    /**
     * adds the players to the playerList
     *
     * @param playerName
     */
    public void addPlayer(String playerName) {
        waitingQueue.offer(playerName);
    }

    /**
     * gets the word from the list and sends it to the Drawing Player
     * sends it through server whisper
     */
    public void wordToDraw() {
        gameWord = GameWords.getWord();
        drawingPlayer();
    }


    public void drawingPlayer() {

        Collections.shuffle(playerList);

        String toSend = Encoder.activePlayer(gameWord);
        server.whisper(playerList.get(0), toSend);
    }


    /**
     * compares the gameWord with the words sent by the chat with /CHAT/
     */
    public void wordCheck(String wordGuess) {

        if (wordGuess.equals(gameWord)) {
            startingGame();
        }
    }
}