package sample;

import javafx.scene.Parent;

public class Ship extends Parent {
    public int type;
    public boolean vertical = true;
    private int health;

    public Ship(int type, boolean vertical) {
        /*
        Так как корбли ставяться по очереди, начиная с самого большего,
        то по счетчику определяем его длину.
         */
        switch(type){
            case 1:
                this.type = type;
                break;
            case 2:
                this.type = type-1;
                break;
            case 3:
                this.type = type-2;
                break;
            case 4:
                this.type = type-3;
                break;
            case 5:
                this.type = type-3;
                break;
            case 6:
                this.type = type-4;
                break;
            case 7:
                this.type = type-5;
                break;
            case 8:
                this.type = type-5;
                break;
            case 9:
                this.type = type-6;
                break;
            case 10:
                this.type = type-6;
                break;
        }

        health = this.type;
        this.vertical = vertical;

    }

    public void hit() {
        health--;
    }

    public boolean isAlive() {
        return health > 0;
    }
}