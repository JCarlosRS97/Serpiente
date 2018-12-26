
public class Graficador implements Runnable {
    private Table table;

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private boolean isPlaying = true;
    public Graficador(Table table) {
        this.table = table;
    }


    @Override
    public void run() {
        while(isPlaying){
            table.syncToString();
        }
    }
}
