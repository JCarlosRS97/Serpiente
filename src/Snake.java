import java.util.LinkedList;

public class Snake implements Runnable{
    private Table table;
    private boolean isPlaying = true;
    private boolean isAlive = true;
    private LinkedList<Cell> parts;
    private int id;
    private int snakeSize;

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public LinkedList<Cell> getParts() {
        return parts;
    }

    public int getId() {
        return id;
    }

    public Snake(int id, int snakeSize, Table table){
        parts = new LinkedList<>(); // No cambia de tamaño
        this.table = table;
        this.id = id;
        this.snakeSize = snakeSize;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Serpiente_" + id);
        table.placeInitialPosition(id, parts, snakeSize);
        while(isPlaying){
            isAlive = table.ifAliveRandomMove(this, isAlive);
            if(!isAlive){

            }
        }
    }
}
