import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;

public class Snake implements Runnable{
    private final Table table;
    private boolean isPlaying = true;
    private boolean isAlive = true;
    private final LinkedList<Cell> parts;
    private final int id;
    private final int snakeSize;


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
            try {
                isAlive = table.ifAliveRandomMove(this, isAlive);
            } catch (InterruptedException | BrokenBarrierException e) {
                isPlaying = false;
            }
        }
    }
}
