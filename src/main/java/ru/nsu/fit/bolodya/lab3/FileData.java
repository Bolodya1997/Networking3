package ru.nsu.fit.bolodya.lab3;

public class FileData {

    private String name;

    private long length;
    private long remain;

    private boolean finished = false;

    public FileData(String name, long length, long remain) {
        this.name = name;
        this.length = length;
        this.remain = remain;
    }

    public String getName() {
        return name;
    }

    public long getLength() {
        return length;
    }

    public long getRemain() {
        return remain;
    }

    public void setRemain(long remain) {
        this.remain = (remain > 0) ? remain : 0;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
