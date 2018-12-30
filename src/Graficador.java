
public class Graficador implements Runnable {
    private final Table table;
    private boolean isPlaying = true;

    public Graficador(Table table) {
        this.table = table;
    }


    @Override
    public void run() {
        Thread.currentThread().setName("Graficador");
        while(isPlaying){
            try {
                table.syncToString();
            } catch (InterruptedException e) {
                isPlaying = false;
            }
        }
    }
}
