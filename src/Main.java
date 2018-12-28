import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private final static int GSIZEMIN = 2;
    private final static int NSKNAKESMIN = 1;
    private final static int SNAKESIZEMIN = 1;
    private final static int NSTEPSMIN = 1;
    private static int gSize = 5;
    private static int nSnakes = 2;
    private static int snakeSize = 2;
    private static int nSteps = 3;
    public static void main(String[] args) {
        //setData();
        CyclicBarrier barrier = new CyclicBarrier(nSnakes + 1);
        Table table = new Table(gSize, barrier, nSnakes);
        Snake[] snakes = new Snake[nSnakes];
        Graficador graficador = new Graficador(table);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nSnakes+1); // nSakes + writer
        int id = 0;
        for (Snake snake : snakes) {
            snake = new Snake(id, snakeSize, table);
            id++;
            executor.execute(snake);
        }
        executor.execute(graficador);
        for(int i = 0; i < nSteps; i++){
            try {
                barrier.await();
                Thread.sleep(100);
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        executor.shutdownNow();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(table);
    }

    public static void setData(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introducir tamaño de la cuadrícula: ");
        gSize = scanner.nextInt();
        while(gSize < GSIZEMIN){
            System.out.println("Error. El minimo tamaño es " + GSIZEMIN);
            gSize = scanner.nextInt();
        }

        System.out.println("Introducir el numero de serpientes: : ");
        nSnakes = scanner.nextInt();// TODO: añadir comprobacion respecto tamaño table
        while(nSnakes < NSKNAKESMIN){
            System.out.println("Error. El minimo numero es " + NSKNAKESMIN);
            nSnakes = scanner.nextInt();
        }

        System.out.println("Introducir tamaño de las serpientes: ");
        snakeSize = scanner.nextInt();
        while(snakeSize < SNAKESIZEMIN){
            System.out.println("Error. El minimo tamaño es " + SNAKESIZEMIN);
            snakeSize = scanner.nextInt();
        }
        System.out.println("Introducir el numero de turnos: ");
        nSteps = scanner.nextInt();
        while(nSteps < NSTEPSMIN){
            System.out.println("Error. El minimo turnos es " + NSTEPSMIN);
            nSteps = scanner.nextInt();
        }
    }

}
