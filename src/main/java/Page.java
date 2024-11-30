import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class Page {
    private final int size; //how many keys can be stored in the page
    private boolean isLeaf;
    private int numberOfKeys;

    final ArrayList<Integer> keys;
    final ArrayList<Integer> recordIndexes;
    final ArrayList<Integer> childOffsets;


    public Page(int size, boolean isLeaf) {
        this.size = size;
        this.isLeaf = isLeaf;
        this.numberOfKeys = 0;

        keys = new ArrayList<>();
        recordIndexes = new ArrayList<>();
        childOffsets = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            keys.add(null);
            recordIndexes.add(null);
        }
        for(int i = 0; i < size + 1; i++)
            childOffsets.add(null);
    }

    public int getSize() {
        return size;
    }

    public int getNumberOfKeys() {
        return numberOfKeys;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void addValue(Integer offset, int key, int index) {
        /*if (numberOfKeys == size) {
            System.out.println("Page is full, key " + key + " cannot be added" + " offset: " + offset);
            return;
        }*/
        //check if key is already in the page
        for (int i = 0; i < numberOfKeys; i++) {
            if (keys.get(i) == key) {
                System.out.println("Key " + key + " is already in the page");
                return;
            }
        }
        //check where to insert the key
        int i = 0;
        while (i < numberOfKeys && keys.get(i) < key) {
            i++;
        }
        System.out.println("Inserting key " + key + " at index " + i);
        //shift keys, recordIndexes and childOffsets to the right
        for (int j = numberOfKeys - 1; j >= i; j--) {
            keys.set(j + 1, keys.get(j));
            recordIndexes.set(j + 1, recordIndexes.get(j));
            childOffsets.set(j + 1, childOffsets.get(j));
        }
        keys.set(i, key);
        recordIndexes.set(i, index);
        childOffsets.set(i, offset);
        numberOfKeys++;
    }

    public void savePage() {
        String path = "src/main/java/btree.txt";

        try (FileWriter writer = new FileWriter(path, true)) {
            StringBuilder sb = new StringBuilder();
            //append boolean as 0 or 1
            sb.append(isLeaf ? "1" : "0").append(" ");
            sb.append(numberOfKeys).append(" ");
            for (int i = 0; i < numberOfKeys; i++) {
                if(childOffsets.get(i) == null) {
                    sb.append("0").append(" ");
                }
                else {
                    sb.append(childOffsets.get(i)).append(" ");
                }
                sb.append(keys.get(i)).append(" ");
                sb.append(recordIndexes.get(i)).append(" ");
            }
            if(childOffsets.get(numberOfKeys) == null) {
                sb.append("0").append(" ");
            }
            else {
                sb.append(childOffsets.get(numberOfKeys)).append(" ");
            }
            sb.append("\n");
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readPage(String path, int offset) throws FileNotFoundException {
        File file = new File(path);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentLine = 0;

            // Navigate to the line at the specified offset
            while ((line = reader.readLine()) != null) {
                if (currentLine == offset) {
                    // Parse the line and populate the Page object
                    String[] parts = line.trim().split("\\s+");

                    // The first value is whether the page is a leaf (1 = true, 0 = false)
                    isLeaf = parts[0].equals("1");


                    // The second value is the number of keys
                    int numKeys = Integer.parseInt(parts[1]);
                    this.numberOfKeys = numKeys;

                    // Read the keys, record indexes, and child offsets
                    int partIndex = 2;
                    for (int i = 0; i < numKeys; i++) {
                        // Read child offset
                        int childOffset = Integer.parseInt(parts[partIndex++]);
                        this.childOffsets.set(i, childOffset == 0 ? null : childOffset);

                        // Read key
                        this.keys.set(i, Integer.parseInt(parts[partIndex++]));

                        // Read record index
                        this.recordIndexes.set(i, Integer.parseInt(parts[partIndex++]));
                    }
                    int childOffset = Integer.parseInt(parts[partIndex++]);
                    this.childOffsets.set(numKeys, childOffset == 0 ? null : childOffset);
                    break;
                }
                currentLine++;
            }

            // If we reach the end of the file without finding the offset, throw an exception
            if (line == null) {
                throw new IllegalArgumentException("Offset " + offset + " is out of range");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePage(int offset, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(isLeaf ? "1" : "0").append(" ");
        sb.append(getNumberOfKeys()).append(" ");

        // Append the page data
        for (int i = 0; i < getNumberOfKeys(); i++) {
            sb.append(childOffsets.get(i) == null ? "0" : childOffsets.get(i)).append(" ");
            sb.append(keys.get(i)).append(" ");
            sb.append(recordIndexes.get(i)).append(" ");
        }
        sb.append(childOffsets.get(getNumberOfKeys()) == null ? "0" : childOffsets.get(getNumberOfKeys())).append(" ");

        String updatedPage = sb.toString();
        try {
            // Open the file and overwrite the specific page
            File tempFile = new File("src/main/java/btree_temp.txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(path));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                int currentLine = 0;

                // Read the file line by line
                while ((line = reader.readLine()) != null) {
                    if (currentLine == offset) {
                        // Update the line at the given offset
                        writer.write(updatedPage);
                    } else {
                        // Write other lines as is
                        writer.write(line);
                    }
                    writer.newLine();
                    currentLine++;
                }
            }

            // Replace the original file with the temporary file
            Files.move(tempFile.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while updating the page", e);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Leaf: ").append(isLeaf).append("\n");
        for (int i = 0; i < numberOfKeys; i++) {
            sb.append(keys.get(i)).append(" ");
        }
        return sb.toString();
    }

    public void setNumberOfKeys(int i) {
        this.numberOfKeys = i;
    }
}
