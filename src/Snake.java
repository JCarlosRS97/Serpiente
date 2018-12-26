
public class Snake implements Runnable{
    private Table table;
    private boolean isPlaying = true;
    private Cell parts[];
    private int id;
    public Snake(int id, int snakeSize, Table table){
        parts = new Cell[snakeSize]; // No cambia de tamaño
        this.table = table;
        this.id = id;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Serpiente_" + id);
        table.placeInitialPosition(id, parts);
        while(isPlaying){
            isPlaying = table.ifAliveRandomMove(id, parts);
        }
    }
}