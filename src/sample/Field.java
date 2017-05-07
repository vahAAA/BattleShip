package sample;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;



public class Field extends Parent {
    private VBox rows = new VBox(); // для нашей сетки
    private boolean enemy = false; // для определения кому пренадлежит поле
    public int ships = 10; // общее количество кораблей
    Image image = new Image("dot.png"); // бекграунд для промаха
    ImagePattern imagePattern = new ImagePattern(image);

    public Field(boolean enemy, EventHandler<? super MouseEvent> handler) { // ивент на нажатие кнопки мышкой
        this.enemy = enemy;
        for (int y = 0; y < 10; y++) {
            HBox row = new HBox(); // в каждую ячейку ВИбокса будет помещен целый ЕйчБокс
            for (int x = 0; x < 10; x++) {
                Cell c = new Cell(x, y, this);
                c.setOnMouseClicked(handler);
                row.getChildren().add(c);
            }

            rows.getChildren().add(row);
        }

        getChildren().add(rows);
    }

    public boolean placeShip(Ship ship, int x, int y) {
        if (canPlaceShip(ship, x, y)) { // проверяем, можно ли поставить
            int length = ship.type;

            if (ship.vertical) { // если вертикальный заполняем по у вперед
                for (int i = y; i < y + length; i++) {
                    Cell cell = getCell(x, i);
                    cell.ship = ship;
                    if (!enemy) {
                        cell.setFill(Color.GREEN);
                        cell.setStroke(Color.BLACK);
                    }
                }
            }
            else {
                for (int i = x; i < x + length; i++) {
                    Cell cell = getCell(i, y);
                    cell.ship = ship;
                    if (!enemy) {
                        cell.setFill(Color.GREEN);
                        cell.setStroke(Color.BLACK);
                    }
                }
            }

            return true;
        }

        return false;
    }

    public Cell getCell(int x, int y) {
        return (Cell)((HBox)rows.getChildren().get(y)).getChildren().get(x);
    }

    private Cell[] getNeighbors(int x, int y) { // определяем соседей, чтобы нельзя было ставить впритык (8-связность)
        Point2D[] points = new Point2D[] {
                new Point2D(x - 1, y),
                new Point2D(x + 1, y),
                new Point2D(x, y - 1),
                new Point2D(x, y + 1),
                new Point2D(x + 1, y + 1),
                new Point2D(x - 1, y - 1),
                new Point2D(x - 1, y + 1),
                new Point2D(x + 1, y - 1)
        };

        List<Cell> neighbors = new ArrayList<Cell>();

        for (Point2D p : points) {
            if (isValidPoint(p)) {
                neighbors.add(getCell((int)p.getX(), (int)p.getY()));
            }
        }

        return neighbors.toArray(new Cell[0]);
    }


    private Cell[] getShipCells(int x, int y) { // определяем все ячейки корабля, для автозаполения его соседей при убийстве
        /*
        Так как мы не знаем, какая ячейка будет нажата последней, мы идем от нее в две стороны по у для вертикального
        корабля и по х для горизонтального, пока не найдем пустую ячейку
         */
        List<Cell> isShip = new ArrayList<Cell>();
        int i = 0;
        if (getCell(x,y).ship.vertical){
           // for(int i=-y;i<getCell(x,y).ship.type+y;i++){
            while (y+i<=9 && getCell(x,y+i).ship!=null )
            {

                isShip.add(getCell(x,y+i));
                i++;
            }
            i=0;

            while (y-i>=0 && getCell(x,y-i).ship!=null )
            {

                isShip.add(getCell(x,y-i));
                i++;
            }
        }
        else
        {
            i=0;
            while ( x+i <= 9 && getCell(x+i,y).ship!=null )
            {

                isShip.add(getCell(x+i,y));
                i++;
            }
            i=0;
            while (x-i >= 0 && getCell(x-i,y).ship!=null  )
            {

                isShip.add(getCell(x-i,y));
                i++;
            }
        }


        return isShip.toArray(new Cell[0]);
    }

    private boolean canPlaceShip(Ship ship, int x, int y) {
        /*
        Определение, можно ли ставить корабль в зависимости от куда тыкнули + чек соседей
         */
        int length = ship.type;

        if (ship.vertical) {
            for (int i = y; i < y + length; i++) {
                if (!isValidPoint(x, i))
                    return false;

                Cell cell = getCell(x, i);
                if (cell.ship != null)
                    return false;

                for (Cell neighbor : getNeighbors(x, i)) {
                    if (!isValidPoint(x, i))
                        return false;

                    if (neighbor.ship != null)
                        return false;
                }
            }
        }
        else {
            for (int i = x; i < x + length; i++) {
                if (!isValidPoint(i, y))
                    return false;

                Cell cell = getCell(i, y);
                if (cell.ship != null)
                    return false;

                for (Cell neighbor : getNeighbors(i, y)) {
                    if (!isValidPoint(i, y))
                        return false;

                    if (neighbor.ship != null)
                        return false;
                }
            }
        }

        return true;
    }

    private boolean isValidPoint(Point2D point) {
        return isValidPoint(point.getX(), point.getY());
    }

    private boolean isValidPoint(double x, double y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

    public class Cell extends Rectangle {
        /*
        Класс ячейка
         */
        public int x, y; //координаты
        public Ship ship = null;//какому кораблю принадлежит
        public boolean wasShot = false;

        private Field field;

        public Cell(int x, int y, Field field) {
            super(30, 30);
            this.x = x;
            this.y = y;
            this.field = field;
            setFill(Color.BLACK);
            setStroke(Color.BLUE);
        }


        public void markAsShot (Cell [] cells) {
            /*
            Если весь кораблю убили, помечаем всех соседей тоже отмечеными
             */
            for(int i=0; i<cells.length;i++)
            {
                if(!cells[i].wasShot)
                cells[i].shoot();
            }
        }


        public boolean shoot() {
            /*
            Если выстрелили, то помечаем как промах, потом смотрим попали ли. Если ранили, то помечаем фиолетовым, если
            убили - то красным весь корабль.
             */
            wasShot = true;
            setFill(imagePattern);

            if (ship != null) {
                ship.hit();
                setFill(Color.PURPLE);
                if (!ship.isAlive()) {

                    for(int j = 0; j< field.getShipCells(x,y).length; j++) {
                        field.getShipCells(x,y)[j].setFill(Color.RED);
                        markAsShot(field.getNeighbors(field.getShipCells(x,y)[j].x, field.getShipCells(x,y)[j].y));
                    }
                    field.ships--;
                }
                return true;
            }

            return false;
        }
    }
}