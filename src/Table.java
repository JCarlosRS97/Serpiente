import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
    private char table[][];
    private ReentrantLock lock;
    private CyclicBarrier barrier;
    private Condition isWritten;
    private Condition isAllMoved;
    private int nSnakes;
    private boolean waitingForPrint = false;
    private int snakesThisTurn = 0;
    private FileWriter logFile;
    public Table(int gSize, CyclicBarrier barrier, int nSnakes, FileWriter fileWriter){
        this.nSnakes = nSnakes;
        table = new char[gSize][gSize];
        Arrays.stream(table).forEach(e -> Arrays.fill(e, '*'));
        lock = new ReentrantLock();
        isWritten = lock.newCondition();
        isAllMoved = lock.newCondition();
        this.barrier = barrier;
        logFile = fileWriter;
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

    public boolean ifAliveRandomMove(Snake snake, boolean isAlive) {
        boolean res = isAlive;
        try {
            barrier.await();
            lock.lock();

            while(waitingForPrint){
                isWritten.await();
            }
            snakesThisTurn++;
            if(snakesThisTurn == nSnakes){
                isAllMoved.signalAll();
                waitingForPrint = true;
            }
            LinkedList<Cell> parts = snake.getParts();
            if(isAlive) {
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
                if (isFree(c.getX(), c.getY())) {
                    System.out.println("No choca");
                    // Ahora hay que mover la serpiente
                    //En el tablero y en snake
                    logFile.write("Snake " + snake.getId() + " moved from : " + parts.getLast() + " to " + c + " at " + System.currentTimeMillis() + '\n');
                    writeCellInTable(parts.removeLast(), '*');
                    parts.addFirst(c);
                    writeCellInTable(c, Character.forDigit(snake.getId(), 10));
                }else {
                    System.out.println("Han chocado");
                    logFile.write("Snake " + snake.getId() + " died at : " + c + " at " + System.currentTimeMillis() + '\n');
                    res = false;
                }
            }else {
                System.out.println(Thread.currentThread().getName() + " está muerto.");
            }
        }catch(Exception e){
            snake.setPlaying(false);
        }finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return res;
    }
    private void writeCellInTable(Cell cell, char a){
        table[cell.getY()][cell.getX()] = a;
    }

    public void syncToString(Graficador g) {
        lock.lock();
        try {
            while(snakesThisTurn < nSnakes){
                isAllMoved.await();
            }
            snakesThisTurn = 0;
            System.out.println(toString());
            waitingForPrint = false;
            isWritten.signalAll();
        }catch (InterruptedException e){
            g.setPlaying(false);
        }finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
