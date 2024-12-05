package data;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecordBuffer {
    private final int maxSize;
    private Record[] buffer;
    private int currentSize = 0;

    public RecordBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.buffer = new Record[maxSize];
    }

    public void readRecords(String filePath, int startLine) {
        File file = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentLine = 0;

            while (currentLine < startLine && reader.readLine() != null) {
                currentLine++;
            }

            for (int i = 0; i < maxSize; i++) {
                line = reader.readLine();
                if (line == null) { //end of file
                    for (int j = i; j < maxSize; j++) {
                        buffer[j] = null;
                    }
                    break;
                }

                String[] parts = line.split(" ");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Invalid record format. Must contain at least one number and a key.");
                }

                List<Float> numbers = Arrays.stream(parts, 0, parts.length - 1)
                        .map(Float::parseFloat)
                        .toList();
                int key = Integer.parseInt(parts[parts.length - 1]);
                Record record = new Record(numbers, key);
                buffer[i] = record;
                currentSize++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }


    @Override
    public String toString() {
        return Arrays.stream(buffer)
                .map(record -> record == null ? "null" : record.toString())
                .collect(Collectors.joining("\n"));
    }

    public void clearBuffer() {
        buffer = new Record[maxSize];
        currentSize = 0;
    }

    public Record[] getRecords() {
        return buffer;
    }

    public boolean isNull() {
        for (Record record : buffer) {
            if (record == null) {
                return true;
            }
        }
        return false;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public Record getRecord(int index) {
        return buffer[index];
    }

    public void addRecord(Record record) {
        if(currentSize == maxSize) {
            System.out.println("Buffer is full");
        }
        else {
            buffer[currentSize] = record;
            currentSize++;
        }
    }

    public void saveBuffer(String filePath) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            for (Record record : buffer) {
                if (record == null) {
                    break;
                }
                writer.write(String.format("%.1f %.1f %.1f %d%n", record.getNumber(0),
                        record.getNumber(1), record.getNumber(2), record.getKey()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}