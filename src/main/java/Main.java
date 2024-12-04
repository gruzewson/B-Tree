import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        int d = 2;
        int recordNum = 15;
        int bufferSize = 10;
        String commandsFile = "src/main/java/data/commands.txt";

        BTree btree = new BTree(d, recordNum);
        switch (args[1]) {
            case "interactive" -> {
                btree.init(args, recordNum, bufferSize);
                btree.interactiveMode();
            }
            case "commands" -> {
                btree.commandsMode(commandsFile, recordNum);
            }
            case "mixed" -> {
                btree.commandsMode(commandsFile, recordNum);
                btree.interactiveMode();
            }
            default -> System.out.println("Invalid mode");
        }

    }
}
