import java.util.List;

public class Record {
    private final List<Float> numbers;
    private final float average;

    public Record(List<Float> numbers) {
        this.numbers = numbers;
        this.average = calculateAverage(numbers);
    }

    private float calculateAverage(List<Float> numbers) {
        float sum = 0;
        for (float num : numbers) {
            sum += num;
        }
        return (float) (Math.round((sum / numbers.size()) * 100.0) / 100.0);
    }

    public float getAverage() {
        return average;
    }

    public float getNumber(int index) {
        return numbers.get(index);
    }

    @Override
    public String toString() {
        return numbers + " " + average;
    }
}