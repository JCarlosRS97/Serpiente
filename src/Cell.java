public class Cell {
    private int x, y;
    public Cell(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" +Character.forDigit(x, 10) + " " + Character.forDigit(y, 10) + ")";
    }
}
