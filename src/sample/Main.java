package sample;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import javax.swing.*;
import java.util.Random;

public class Main extends Application {

    private boolean running = false;
    private Field enemyField, playerField;
    private int shipsToPlace = 10;
    private boolean enemyTurn = false;
    private Random random = new Random();
    private boolean ifHit = false, ifHitStart = false;
    private int xHit=0, yHit=0, xHitStart, yHitStart, counter=0;
    private Parent createContent() {
        BorderPane root = new BorderPane();
        root.setPrefSize(600, 400);

        enemyField = new Field(true, event -> {
            if (!running) //если мы все еще ставим корабли, то ничего не делаем
                return;

            Field.Cell cell = (Field.Cell) event.getSource();
            if (cell.wasShot) // Если в ячейку уже попали, то ничего не делаем
                return;

            enemyTurn = !cell.shoot(); // если не папали, то ход соперника (тру), наоборот фолс

            if (enemyField.ships == 0) { //Если кораблей уже нету, мы выиграли
                System.out.println("YOU WIN");
                int reply = JOptionPane.showConfirmDialog(null,
                        "You win, wanna play again?",
                        "SoftServe IT academy" ,
                         JOptionPane.YES_NO_OPTION);

                if (reply == JOptionPane.YES_OPTION) {
                    try {
                        JOptionPane.showMessageDialog(null,
                                "Sorry, but one game is enough, buy!"); // толстая шутка
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.exit(0);
                }
            }

            if (enemyTurn)
            enemyMove(); // повторный ход соперника, в зависимости от нашего предыдущего хода
        });

        playerField = new Field(false, event -> {
            if (running) // елси все еще ставим корабли
                return;

            Field.Cell cell = (Field.Cell) event.getSource();
            if (playerField.placeShip(new Ship(shipsToPlace, event.getButton() == MouseButton.PRIMARY),
                    cell.x, cell.y)) { // ставим корабль, в зависимости от того, какую конпку нажали
                if (--shipsToPlace == 0) { //если поставили корабль, уменьшаем количество всех кораблей.
                    // Если 0, начинаем игру
                    startGame();
                    JOptionPane.showMessageDialog(null,
                            "You have placed all your ships. Good luck!");
                }
            }
        });

        VBox vBox1 = new VBox(25, new Label("Your board"), playerField);
        VBox vBox2 = new VBox(25, new Label("Enemy's board"), enemyField);
        vBox1.setAlignment(Pos.CENTER);
        vBox2.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(30, vBox1, vBox2);
        hBox.setAlignment(Pos.CENTER);
        hBox.setStyle("-fx-background-image: url(\"1.jpg\")");
        root.setCenter(hBox);

        return root;
    }

    private void enemyMove() {
        /*
        Логика "нашего энеми"
        Сначала мы бьем рандомом. Если попадаем, то выбираем по очереди соседние клетки
        по 4-связности. И потом переопределяем ту ячейку, соседей которой будем искать.
        Если мы со средины пошли в одну сторону и дошли до конца, то с другой стороны
        осталась еще ячейка, поэтому в этой ситуации мы опять возвращаемся на самую первую точку,
        по которой мы попали.
         */
        while (enemyTurn) {
            int x=0, y=0;

            if(!ifHit) {
                 x = random.nextInt(10); //"ии" выбирает рандомную точку
                 y = random.nextInt(10);
                xHitStart= x ;
                yHitStart= y ;
            }else
            {
                if(xHit+1<10 && !playerField.getCell(xHit+1,yHit).wasShot) {
                    x = xHit + 1;
                    y = yHit;
                }
                else if(xHit-1>=0 && !playerField.getCell(xHit-1,yHit).wasShot){
                    x = xHit - 1;
                    y = yHit;
                }
                else if(yHit+1<10 && !playerField.getCell(xHit,yHit+1).wasShot){
                    x = xHit;
                    y = yHit+1;
                }
                else if(yHit-1>=0 && !playerField.getCell(xHit,yHit-1).wasShot){
                    x = xHit;
                    y = yHit-1;
                }
                else {
                    if(counter>2) {
                        ifHit=false;
                        counter=0;
                    }else {
                        x = xHit = xHitStart;
                        y = yHit = yHitStart;
                        counter++;
                    }
                }

            }



            System.out.println("x "+x+" y "+y);

            Field.Cell cell = playerField.getCell(x, y); //выбираем на нашем борде выбранную рандомом точку
            if (cell.wasShot)
                continue; //если точка уже была выбрана, идем дальше

            counter=0;
            enemyTurn = cell.shoot();//продолжаем, если попали
            if(enemyTurn) {

                if(!playerField.getCell(xHit,yHit).partOfDeadShip) {
                    xHit = x;
                    yHit = y;
                    ifHit = true;
                }
                else
                {
                    xHit = 0;
                    yHit = 0;
                    ifHit=false;
                }
            }

            if (playerField.ships == 0) {
                running=false;
                System.out.println("YOU LOSE");
                int reply = JOptionPane.showConfirmDialog(null,
                        "You lost, wanna play again?", "SoftServe task1" , JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    try {
                        JOptionPane.showMessageDialog(null,
                                "Sorry, but one game is enough, buy!");//опять эти шутки
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {

                    System.exit(0);
                }
            }
        }
    }

    private void startGame() {
        /*
        ставим корабли нашего енеми, тоже рандомом
         */
        int type = 10;

        while (type > 0) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            if (enemyField.placeShip(new Ship(type, Math.random() < 0.5), x, y)) {
                type--;
            }
        }

        running = true;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("Battleship");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        JOptionPane.showMessageDialog(null,"Welcome! Place your ships.");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
