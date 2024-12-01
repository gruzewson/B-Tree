import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class Page {
    private Integer offset; // Offset (line number) in the file
    private final int size; // Maximum number of keys in the page
    private boolean isLeaf; // Whether the page is a leaf
    private int numberOfKeys; // Current number of keys

    final ArrayList<Integer> keys;           // List of keys stored in the page
    final ArrayList<Integer> recordIndexes;  // List of record indexes (for each key)
    final ArrayList<Integer> childOffsets;   // Pointers to child pages (offsets in file)

    public Page(int size, boolean isLeaf) {
        this.size = size;
        this.isLeaf = isLeaf;
        this.numberOfKeys = 0;

        keys = new ArrayList<>();
        recordIndexes = new ArrayList<>();
        childOffsets = new ArrayList<>();

        // Fill arrays with placeholders (null)
        for (int i = 0; i < size; i++) {
            keys.add(null);
            recordIndexes.add(null);
        }
        for (int i = 0; i < size + 1; i++) {
            childOffsets.add(null);
        }
    }

    // Getter for size
    public int getSize() {
        return size;
    }

    // Getter for number of keys
    public int getNumberOfKeys() {
        return numberOfKeys;
    }

    // Setter for number of keys
    public void setNumberOfKeys(int number) {
        this.numberOfKeys = number;
    }

    // Getter for leaf status
    public boolean isLeaf() {
        return isLeaf;
    }

    // Setter for leaf status
    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    /**
     * Getter for the page's offset in the file.
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Setter for the page's offset.
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    /**
     * Add a key, index, and child offset to the page.
     */
    public void addValue(Integer offset, int key, int index) {
        for (int i = 0; i < numberOfKeys; i++) {
            if (keys.get(i) == key) {
                System.out.println("Key " + key + " is already in the page");
                return;
            }
        }

        int i = 0;
        while (i < numberOfKeys && keys.get(i) < key) {
            i++;
        }

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

    public void savePage(String path) {
        if (offset == null) {
            // If the offset is not set, append the page as a new line
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
                String pageData = serializePage();
                writer.write(pageData + "\n");
            } catch (IOException e) {
                throw new RuntimeException("Error saving page to file", e);
            }
            // Calculate the offset (line number) of the newly appended page
            this.offset = getFileLineCount(path) - 1;
        } else {
            // If offset is set, update the existing line in the file
            updatePage(path);
        }
    }


    public void readPage(String path, int offset) throws FileNotFoundException {
        File file = new File(path);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentLine = 0;

            while ((line = reader.readLine()) != null) {
                if (currentLine == offset) {
                    String[] parts = line.trim().split("\\s+");
                    System.out.println(Arrays.toString(parts));

                    this.offset = Integer.parseInt(parts[0]); // Read offset
                    isLeaf = parts[1].equals("1"); // Read isLeaf
                    numberOfKeys = Integer.parseInt(parts[2]); // Read numberOfKeys

                    int partIndex = 3; // Start reading child offsets, keys, and record indexes
                    for (int i = 0; i < numberOfKeys; i++) {
                        childOffsets.set(i, Integer.parseInt(parts[partIndex++])); // Child offset
                        keys.set(i, Integer.parseInt(parts[partIndex++])); // Key
                        recordIndexes.set(i, Integer.parseInt(parts[partIndex++])); // Record index
                    }

                    // Read the last child offset
                    childOffsets.set(numberOfKeys, Integer.parseInt(parts[partIndex]));



                    break;
                }
                currentLine++;
            }

            if (line == null) {
                throw new IllegalArgumentException("Offset " + offset + " is out of range");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading page from file", e);
        }
    }

    private int getFileLineCount(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            int count = 0;
            while (reader.readLine() != null) {
                count++;
            }
            return count;
        } catch (IOException e) {
            throw new RuntimeException("Error counting lines in file", e);
        }
    }

    public void updatePage(String path) {
        if (offset == null) {
            throw new IllegalStateException("Offset is not set. Cannot update the page.");
        }

        String updatedPage = serializePage(); // Serialize the page data into a string

        try {
            File tempFile = new File("temp_btree.txt");

            // Use a temporary file to update the required line
            try (BufferedReader reader = new BufferedReader(new FileReader(path));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                int currentLine = 0;

                while ((line = reader.readLine()) != null) {
                    if (currentLine == offset) {
                        writer.write(updatedPage); // Write the updated page data
                    } else {
                        writer.write(line); // Write the unchanged line
                    }
                    writer.newLine();
                    currentLine++;
                }
            }

            // Replace the original file with the updated one
            Files.move(tempFile.toPath(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Error updating page in file", e);
        }
    }

    private String serializePage() {
        StringBuilder sb = new StringBuilder();

        // Offset will not be written to the file directly but is calculated implicitly
        sb.append(isLeaf ? "1" : "0").append(" ");
        sb.append(numberOfKeys).append(" ");

        for (int i = 0; i < numberOfKeys; i++) {
            sb.append(childOffsets.get(i) == null ? "0" : childOffsets.get(i)).append(" ");
            sb.append(keys.get(i)).append(" ");
            sb.append(recordIndexes.get(i)).append(" ");
        }

        sb.append(childOffsets.get(numberOfKeys) == null ? "0" : childOffsets.get(numberOfKeys)).append(" ");

        return sb.toString().trim();
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Leaf: ").append(isLeaf).append("\n");
        sb.append("Keys: ");
        for (int i = 0; i < numberOfKeys; i++) {
            sb.append(keys.get(i)).append(" ");
        }
        sb.append("\nOffset: ").append(offset);
        return sb.toString();
    }
}
