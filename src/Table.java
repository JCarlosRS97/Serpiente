import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
    private char table[][];
    private ReentrantLock lock;
    private CyclicBarrier barrier;
    public Table(int gSize, CyclicBarrier barrier){
        table = new char[gSize][gSize];
        Arrays.stream(table).forEach(e -> Arrays.fill(e, '*'));
        lock = new ReentrantLock();
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

    public int getLength(){
        return table[0].length;
    }

    public boolean isFree(int x, int y){
        return table[x][y] == '*';
    }

    public void placeInitialPosition(int id, Cell[] parts) {
        lock.lock();
        List<Integer> possiblePlaces = new ArrayList<>();
        for(int i = 0; i < getLength(); i++){
            if(isFree(1, i)){
                possiblePlaces.add(i);
            }
        }
        Random random = new Random();
        int c = possiblePlaces.get(random.nextInt(possiblePlaces.size()));
        for(int i = 0; i < parts.length; i++){
            parts[i] = new Cell(c, 1 + i);
            table[i+1][c] = Character.forDigit(id, 10);
        }
        lock.unlock();
    }

    public boolean randomMove(int id, Cell[] parts) {
        lock.lock();
        boolean res = false;
        try {
            barrier.await();
            //Elegimos el siguiente movimiento
            List<Cell> possiblePlaces = new ArrayList<>();
            //Hacia la izquierda
            if (parts[0].getX() > 0 && parts[1].getX() != (parts[0].getX() - 1)) {
                possiblePlaces.add(new Cell(parts[0].getX() - 1, parts[0].getY()));
            }
            //Hacia la derecha
            if (parts[0].getX() < getLength() - 1 && parts[1].getX() != (parts[0].getX() + 1)) {
                possiblePlaces.add(new Cell(parts[0].getX() + 1, parts[0].getY()));
            }
            //Hacia abajo
            if (parts[0].getY() < getLength() - 1 && parts[1].getY() != (parts[0].getY() + 1)) {
                possiblePlaces.add(new Cell(parts[0].getX(), parts[0].getY() + 1));
            }
            //Hacia arriba
            if (parts[0].getY() > 0 && parts[1].getY() != (parts[0].getY() - 1)) {
                possiblePlaces.add(new Cell(parts[0].getX(), parts[0].getY() - 1));
            }
            Random random = new Random();
            Cell c = possiblePlaces.get(random.nextInt(possiblePlaces.size()));
            //Se comprueba si hay otra serpiente
            if (isFree(c.getX(), c.getY())){
                // Ahora hay que mover la serpiente
                //En el tablero:
                writeCellInTable(parts[parts.length - 1], '*');
                writeCellInTable(c, Character.forDigit(id, 10));
                // En la serpiente
                for (int i = parts.length; i > 0; i--) {
                    parts[i] = parts[i + 1];
                }
                parts[0] = c;
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
        table[cell.getX()][cell.getY()] = a;
    }
}
