import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
    private char table[][];
    private ReentrantLock lock;
    private CyclicBarrier barrier;
    private Condition isWrited;
    private Condition isAllMoved;
    private Graficador graficador;
    private final int nSnakes;
    private boolean waitingForPrint = false;
    private int snakesThisTurn = 0;
    public Table(int gSize, CyclicBarrier barrier, int nSnakes){
        this.nSnakes = nSnakes;
        table = new char[gSize][gSize];
        Arrays.stream(table).forEach(e -> Arrays.fill(e, '*'));
        lock = new ReentrantLock();
        isWrited = lock.newCondition();
        isAllMoved = lock.newCondition();
        this.barrier = barrier;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < table[0].length; i++){
            for (char a: table[i]) {
                s.append(a).append(" ");
            }
            s.append('\n');
        }
        return s.toString();
    }

    private int getLength(){
        return table[0].length;
    }

    private boolean isFree(int x, int y){
        return table[y][x] == '*';
    }

    public void placeInitialPosition(int id, LinkedList<Cell> parts, int snakeSize) {
        lock.lock();
        List<Integer> possiblePlaces = new ArrayList<>();
        for(int i = 0; i < getLength(); i++){
            if(isFree(i, 1)){
                possiblePlaces.add(i);
            }
        }
        Random random = new Random();
        int c = possiblePlaces.get(random.nextInt(possiblePlaces.size()));
        System.out.println(Thread.currentThread().getName() + " escoge " + new Cell(c, 1));
        for(int i = 0; i < snakeSize; i++){
            parts.addLast(new Cell(c, 1 + i));
            table[i+1][c] = Character.forDigit(id, 10);
        }
        System.out.println(toString());
        lock.unlock();
    }

    public boolean ifAliveRandomMove(int id, LinkedList<Cell> parts) {
        boolean res = false;
        try {
            barrier.await();
            lock.lock();

            while(waitingForPrint){
                isWrited.await();
            }
            snakesThisTurn++;
            if(snakesThisTurn == nSnakes){
                isAllMoved.signalAll();
                waitingForPrint = true;
            }

            //Elegimos el siguiente movimiento
            List<Cell> possiblePlaces = new ArrayList<>();
            //Hacia la izquierda
            if (parts.getFirst().getX() > 0 && parts.get(1).getX() != (parts.getFirst().getX() - 1)) {
                possiblePlaces.add(new Cell(parts.getFirst().getX() - 1, parts.getFirst().getY()));
            }
            //Hacia la derecha
            if (parts.getFirst().getX() < getLength() - 1 && parts.get(1).getX() != (parts.getFirst().getX() + 1)) {
                possiblePlaces.add(new Cell(parts.getFirst().getX() + 1, parts.getFirst().getY()));
            }
            //Hacia abajo
            if (parts.getFirst().getY() < getLength() - 1 && parts.get(1).getY() != (parts.getFirst().getY() + 1)) {
                possiblePlaces.add(new Cell(parts.getFirst().getX(), parts.getFirst().getY() + 1));
            }
            //Hacia arriba
            if (parts.getFirst().getY() > 0 && parts.get(1).getY() != (parts.getFirst().getY() - 1)) {
                possiblePlaces.add(new Cell(parts.getFirst().getX(), parts.getFirst().getY() - 1));
            }

            Random random = new Random();
            Cell c = possiblePlaces.get(random.nextInt(possiblePlaces.size()));
            System.out.println(Thread.currentThread().getName() + " escoge " + c);
            //Se comprueba si hay otra serpiente
            if (isFree(c.getX(), c.getY())){
                System.out.println("No choca");
                // Ahora hay que mover la serpiente
                //En el tablero y en snake
                writeCellInTable(parts.removeLast(), '*');
                parts.addFirst(c);
                writeCellInTable(c, Character.forDigit(id, 10));
                res = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return res;
    }
    private void writeCellInTable(Cell cell, char a){
        table[cell.getY()][cell.getX()] = a;
    }

    public void syncToString() {
        lock.lock();
        try {
            while(snakesThisTurn < nSnakes){
                isAllMoved.await();
            }
            snakesThisTurn = 0;
            System.out.println(toString());
            waitingForPrint = false;
            isWrited.signalAll();
        }catch (InterruptedException e){
            graficador.setPlaying(false);
        }finally {
            lock.unlock();
        }
    }
}
