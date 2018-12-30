import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    private final static int GSIZE_MIN = 2;
    private final static int NSKNAKES_MIN = 1;
    private final static int SNAKESIZE_MIN = 2;
    private final static int NSTEPS_MIN = 1;
    private static int gSize = 5;
    private static int nSnakes = 2;
    private static int snakeSize = 2;
    private static int nSteps = 10;
    public static void main(String[] args) {
        setData();
        FileWriter logFile;
        try {
            logFile = new FileWriter("Log.txt");
        } catch (IOException e) {
            System.out.println("No es posible almacenar el log en este directorio.");
            logFile = null;
        }
        //logFile = null; //Esta sentencia muestra la robustez del programa frente a fallos de escritura en el fichero
        CyclicBarrier barrier = new CyclicBarrier(nSnakes + 1);
        Table table = new Table(gSize, barrier, nSnakes, logFile);
        Snake[] snakes = new Snake[nSnakes];
        Graficador graficador = new Graficador(table);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nSnakes+1); // nSakes + main

        for (int i = 0; i < snakes.length; i++) {
            snakes[i] = new Snake(i, snakeSize, table);
            executor.execute(snakes[i]);
        }
        executor.execute(graficador);

        for(int i = 1; i < nSteps +1 ; i++){
            try {
                if (i > 1)
                    System.out.println("Turno " + i + ":");
                barrier.await();
                Thread.sleep(100);
            } catch (InterruptedException | BrokenBarrierException e) {
                executor.shutdownNow(); // Si el main es interrumpido, se interrumpe el resto de hilos
            }
        }
        executor.shutdownNow();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS); // Se impone un tiempo en la practica infinito de espera
                                                                 // para la finalizacion de todos los hilos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Resultado final : ");
        System.out.println(table);

        try {
            if(logFile != null){
                logFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void setData(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introducir tamaño de la cuadrícula: ");
        gSize = scanner.nextInt();
        while(gSize < GSIZE_MIN){
            System.out.println("Error. El minimo tamaño es " + GSIZE_MIN);
            gSize = scanner.nextInt();
        }

        System.out.println("Introducir el numero de serpientes: ");
        nSnakes = scanner.nextInt();
        while(nSnakes < NSKNAKES_MIN && nSnakes > gSize){
            System.out.println("Error. El minimo numero es " + NSKNAKES_MIN + " y el máximo es " + gSize);
            nSnakes = scanner.nextInt();
        }

        System.out.println("Introducir tamaño de las serpientes: ");
        snakeSize = scanner.nextInt();
        while(snakeSize < SNAKESIZE_MIN){
            System.out.println("Error. El minimo tamaño es " + SNAKESIZE_MIN);
            snakeSize = scanner.nextInt();
        }
        System.out.println("Introducir el numero de turnos: ");
        nSteps = scanner.nextInt();
        while(nSteps < NSTEPS_MIN){
            System.out.println("Error. El minimo turnos es " + NSTEPS_MIN);
            nSteps = scanner.nextInt();
        }
    }

}
