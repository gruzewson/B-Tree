package data;

import java.util.List;

public class Record {
    private final List<Float> numbers;
    private final int key;

    public Record(List<Float> numbers, int key) {
        this.numbers = numbers;
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public float getNumber(int index) {
        return numbers.get(index);
    }

    @Override
    public String toString() {
        return numbers + " " + key;
    }
}