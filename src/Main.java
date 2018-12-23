import java.util.Scanner;

public class Main {
    private final static int GSIZEMIN = 2;
    private final static int NSKNAKESMIN = 1;
    private final static int SNAKESIZEMIN = 1;
    private final static int NSTEPSMIN = 1;
    private static int gSize;
    private static int nSnakes;
    private static int snakeSize;
    private static int nSteps;
    public static void main(String[] args) {
        pedirDatos();

    }
    public static void pedirDatos(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introducir tamaño de la cuadrícula: ");
        gSize = scanner.nextInt();
        while(gSize < GSIZEMIN){
            System.out.println("Error. El minimo tamaño es " + GSIZEMIN);
            gSize = scanner.nextInt();
        }

        System.out.println("Introducir el numero de serpientes: : ");
        nSnakes = scanner.nextInt();
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
