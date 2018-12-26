import java.util.LinkedList;
import java.util.List;

public class Snake implements Runnable{
    private Table table;
    private boolean isPlaying = true;
    private LinkedList<Cell> parts;
    private int id;
    private int snakeSize;
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
            isPlaying = table.ifAliveRandomMove(id, parts);
        }
    }
}
