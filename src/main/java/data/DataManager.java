package data;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DataManager {
    private int recordNum;

    public DataManager(int recordNum) {
        this.recordNum = recordNum;
    }

    public void generateData(String fileName) {
        File file = new File("src/main/java/data/" + fileName + ".txt");
        float[] options = {2, 3, 3.5F, 4, 4.5F, 5};
        float v1, v2, v3;
        int key;
        Random random = new Random();

        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < recordNum; i++) {
                v1 = options[random.nextInt(options.length)];
                v2 = options[random.nextInt(options.length)];
                v3 = options[random.nextInt(options.length)];
                key = random.nextInt(Integer.MAX_VALUE);

                writer.write(String.format("%.1f %.1f %.1f %d%n", v1, v2, v3, key));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        File defaultDirectory = new File("src/main/java/data");
        fileChooser.setCurrentDirectory(defaultDirectory);
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            System.out.println("File selected: " + file.getAbsolutePath());
        } else {
            System.out.println("File selection canceled. Choosing default file...");
        }
    }


    public void readFromKeyboard(String fileName) {
        File file = new File("src/main/java/data/" + fileName + ".txt");
        float v1, v2, v3;
        int key;
        System.out.println("Enter the number of records: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int recNum;
        try {
            recNum = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.recordNum = recNum;

        System.out.println("Grades to choose from are: {2, 3, 3.5, 4, 4.5, 5}, key should be natural number \nEnter the grades and number: ");
        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < recordNum; i++) {
                String[] grades = reader.readLine().split("\\s+");
                v1 = Float.parseFloat(grades[0]);
                v2 = Float.parseFloat(grades[1]);
                v3 = Float.parseFloat(grades[2]);
                key = Integer.parseInt(grades[3]);

                writer.write(String.format("%.1f %.1f %.1f %d%n", v1, v2, v3, key));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllPages() {
        File dir = new File("src/main/java/pages");
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }

    //todo use buffers here for everything
    public void getData(String[] args, String fileName){
        String mode;
        if (args.length < 1) {
            mode = "generate";
        }
        else {
            mode = args[0];
        }
        switch (mode.toLowerCase()) {
            case "generate":
                //System.out.println("Generating data...");
                generateData(fileName);
                return;
            case "file":
                //System.out.println("Choose file...");
                chooseFile();
                return;
            case "keyboard":
                //System.out.println("Reading data from keyboard...");
                readFromKeyboard(fileName);
                return;
            default:
                System.out.println("Invalid mode");
        }
    }

    public Record getRecord(int index){
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/data/data.txt"))) {
            String line;
            for (int i = 0; i < index-1; i++) {
                reader.readLine();
            }
            line = reader.readLine();
            if (line == null) {
                return null;
            }
            String[] parts = line.split(" ");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid record format. Must contain at least one number and a key.");
            }
            float v1 = Float.parseFloat(parts[0]);
            float v2 = Float.parseFloat(parts[1]);
            float v3 = Float.parseFloat(parts[2]);
            int key = Integer.parseInt(parts[3]);
            List<Float> values = Arrays.asList(v1, v2, v3);
            return new Record(values, key);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }

    public int getRecordNum() {
        File file = new File("src/main/java/data/data.txt");
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }

        return count;
    }

    public void generateCommands() {
        File file = new File("src/main/java/data/commands.txt");
        float[] options = {2, 3, 3.5F, 4, 4.5F, 5};
        float v1, v2, v3;
        int key;
        Random random = new Random();

        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < recordNum; i++) {
                v1 = options[random.nextInt(options.length)];
                v2 = options[random.nextInt(options.length)];
                v3 = options[random.nextInt(options.length)];
                key = random.nextInt(Integer.MAX_VALUE);
                writer.write(String.format("insert %.1f %.1f %.1f %d%n", v1, v2, v3, key));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearData() {
        File file = new File("src/main/java/data/data.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}