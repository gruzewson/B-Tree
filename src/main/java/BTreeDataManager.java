import data.Record;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BTreeDataManager {
    private final BTree bTree;

    public BTreeDataManager(BTree bTree) {
        this.bTree = bTree;
    }

    public void interactiveMode(boolean deletePages) throws IOException {
        if(deletePages) {
            bTree.dataManager.deleteAllPages();
            bTree.dataManager.clearData();
        }
        StringBuilder sb = new StringBuilder();
        bTree.getStats().reset();
        sb.append("-------------------------------\n")
                .append("Commands:\n")
                .append("1 - insert record\n")
                .append("2 - find record\n")
                .append("3 - print tree\n")
                .append("4 - print keys\n")
                .append("q - exit program\n")
                .append("-------------------------------");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println(sb);
            String command = reader.readLine();
            int key;

            switch (command) {
                case "q":
                    return;

                case "1":
                    System.out.println("Enter record value [format: v1 v2 v3]: ");
                    List<Float> values = new ArrayList<>();
                    String[] inputs = reader.readLine().split(" ");
                    for (String input : inputs) {
                        values.add(Float.parseFloat(input));
                    }
                    System.out.println("Enter record key: ");
                    key = Integer.parseInt(reader.readLine());
                    bTree.insertRecord(new Record(values, key));
                    break;

                case "2":
                    System.out.println("Enter key to find: ");
                    key = Integer.parseInt(reader.readLine().trim());
                    Record record = bTree.findRecord(key);
                    if (record != null) {
                        System.out.println(record);
                    }
                    break;

                case "3":
                    bTree.printTree();
                    break;

                case "4":
                    bTree.printKeys(bTree.getRoot());
                    System.out.println();
                    break;

                default:
                    System.out.println("Invalid command.");
                    break;
            }

            bTree.getStats().printStatistics();
            bTree.getStats().reset();
        }
    }

    public void commandsMode(String filename, boolean generateCommands) {
        bTree.getStats().reset();
        bTree.dataManager.deleteAllPages();
        bTree.dataManager.clearData();
        if(generateCommands)
            bTree.dataManager.generateCommands();

        File file = new File(filename);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts[0].equals("insert")) {
                    float v1 = Float.parseFloat(parts[1]);
                    float v2 = Float.parseFloat(parts[2]);
                    float v3 = Float.parseFloat(parts[3]);
                    int key = Integer.parseInt(parts[4]);
                    bTree.insertRecord(new Record(List.of(v1, v2, v3), key));
                    bTree.printTree();
                } else {
                    System.out.println("Invalid command: " + line);
                }

                bTree.getStats().printStatistics();
                bTree.getStats().reset();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }
}
