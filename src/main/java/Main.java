import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        int d = 2;
        int recordNum = 100;
        int bufferSize = 50;
        String commandsFile = "src/main/java/data/commands.txt";

        BTree btree = new BTree(d, recordNum, bufferSize);
        BTreeDataManager manager = new BTreeDataManager(btree);
        switch (args[1]) {
            case "interactive" -> manager.interactiveMode(true);

            case "commands" -> manager.commandsMode(commandsFile, true);

            case "mixed" -> {
                manager.commandsMode(commandsFile, true);
                manager.interactiveMode(false);
            }
            default -> manager.interactiveMode(true);
        }

    }
}
