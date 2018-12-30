
public class Graficador implements Runnable {
    private final Table table;

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private boolean isPlaying = true;
    public Graficador(Table table) {
        this.table = table;
    }


    @Override
    public void run() {
        Thread.currentThread().setName("Graficador");
        while(isPlaying){
            table.syncToString(this);
        }
    }
}
