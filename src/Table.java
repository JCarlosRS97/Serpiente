import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
    private final int table[][];
    private final ReentrantLock lock;
    private final CyclicBarrier barrier;
    private final Condition isWritten;
    private final Condition isAllMoved;
    private final int nSnakes;
    private int turnsForPrint = 3;
    private final static int NUM_TURN_FOR_PRINT = 3;
    private boolean waitingForPrint = false;
    private boolean isFirstPrint = true;
    private int snakesThisTurn = 0;
    private final FileWriter logFile;
    public Table(int gSize, CyclicBarrier barrier, int nSnakes, FileWriter fileWriter){
        this.nSnakes = nSnakes;
        table = new int[gSize][gSize];
        Arrays.stream(table).forEach(e -> Arrays.fill(e, -1));
        lock = new ReentrantLock();
        isWritten = lock.newCondition();
        isAllMoved = lock.newCondition();
        this.barrier = barrier;
        logFile = fileWriter;
    }

    @Override
    public String toString() {
        int digits = (int) (Math.log10(table[0].length) + 1);
        StringBuilder s = new StringBuilder();
        Formatter f = new Formatter(s);
        for(int i = 0; i < table[0].length; i++){
            for (int a: table[i]) {
                if(a == -1){
                    //s.append('*').append(" ");
                    f.format("%" + digits + "c ", '*');
                }else{
                    //s.append(a).append(" ");
                    f.format("%" + digits + "d ", a);
                }
            }
            s.append('\n');
        }
        return s.toString();
    }

    private int getLength(){
        return table[0].length;
    }

    private boolean isFree(int x, int y){
        return table[y][x] == -1;
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
            table[i+1][c] = id;
        }
        snakesThisTurn++;
        lock.unlock();
    }

    public boolean ifAliveRandomMove(Snake snake, boolean isAlive) throws InterruptedException, BrokenBarrierException {
        boolean res = isAlive;

        barrier.await();
        try {
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
                    writeInLogFile("Snake " + snake.getId() + " moved from : " + parts.getFirst() + " to " + c + " at " + System.currentTimeMillis() + '\n');
                    writeCellInTable(parts.removeLast(), -1);
                    parts.addFirst(c);
                    writeCellInTable(c, snake.getId());
                }else {
                    System.out.println("Han chocado");
                    writeInLogFile("Snake " + snake.getId() + " died at : " + c + " at " + System.currentTimeMillis() + '\n');
                    res = false;
                }
            }else {
                System.out.println(Thread.currentThread().getName() + " está muerto");
            }
        }catch (InterruptedException e){
            throw new InterruptedException("Fin del juego"); // De esta forma se asegura que se libera el lock
        }finally {
            lock.unlock();
        }
        return res;
    }
    private void writeCellInTable(Cell cell, int a){
        table[cell.getY()][cell.getX()] = a;
    }

    public void syncToString(Graficador g) {
        lock.lock();
        try {
            while(snakesThisTurn < nSnakes){
                isAllMoved.await();
            }
            snakesThisTurn = 0;
            if(turnsForPrint == NUM_TURN_FOR_PRINT){
                turnsForPrint = 1;
                if(isFirstPrint){
                    isFirstPrint = false;
                    System.out.println("Posición inicial:");
                }
                System.out.println(toString());
            }else{
                turnsForPrint++;
            }
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
    private void writeInLogFile(String str){
        try {
            logFile.write(str);
        }catch (Exception e){
            try {
                System.out.println("No se puede escribir en el log.");
                if(logFile != null){
                    logFile.close();
                }
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
    }
}
