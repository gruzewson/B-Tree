import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Page {
    private final int size; //how many keys can be stored in the page
    private final boolean isLeaf;
    private int numberOfKeys;

    private final ArrayList<Integer> keys;
    private final ArrayList<Integer> recordIndexes;
    private final ArrayList<Integer> childOffsets;


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
            childOffsets.add(null);
        }
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

    public void addValue(int key, int index, Integer offset) {
        if (numberOfKeys == size) {
            System.out.println("Page is full, key " + key + " cannot be added");
            return;
        }
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
            sb.append("\n");
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
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

}
