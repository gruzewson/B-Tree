import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Page {
    private int pageId; // Unikalny identyfikator węzła
    private boolean isLeaf; // Czy węzeł jest liściem
    private int keyCount; // Liczba kluczy w węźle
    private List<Integer> keys; // Lista kluczy
    private List<Integer> indexes; // Lista indeksów związanych z kluczami
    private List<Integer> childrenPageIds; // Lista identyfikatorów dzieci (tylko dla węzłów wewnętrznych)
    private int maxKeys; // Maksymalna liczba kluczy (2d)

    public Page(int pageId, boolean isLeaf, int d) {
        this.pageId = pageId;
        this.isLeaf = isLeaf;
        this.keyCount = 0;
        this.keys = new ArrayList<>();
        this.indexes = new ArrayList<>();
        this.childrenPageIds = new ArrayList<>();
        this.maxKeys = 2 * d; // Maksymalnie 2d kluczy
    }

    // Gettery i settery
    public int getPageId() {
        return pageId;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public int getKeyCount() {
        return keyCount;
    }

    public List<Integer> getKeys() {
        return keys;
    }

    public List<Integer> getIndexes() {
        return indexes;
    }

    public List<Integer> getChildrenPageIds() {
        return childrenPageIds;
    }

    public int getMaxKeys() {
        return maxKeys;
    }

    public boolean isFull() {
        return keyCount >= maxKeys;
    }

    public void addKey(int key, int index) {
        if (isFull()) {
            throw new IllegalStateException("Page is full. Cannot add more keys.");
        }
        keys.add(key);
        indexes.add(index);
        keyCount++;
    }

    public void addChildPageId(int pageId) {
        childrenPageIds.add(pageId);
    }

    public void setKeyCount(int count) {
        this.keyCount = count;
    }

    public void incrementKeyCount(int count) {
        this.keyCount += count;
    }


    // Zapis węzła do pliku
    public void saveToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(pageId + "," + isLeaf + "," + keyCount + "," + maxKeys + "\n");
            for (int i = 0; i < keys.size(); i++) {
                writer.write(keys.get(i) + ":" + indexes.get(i) + "\n");
            }
            for (int childPageId : childrenPageIds) {
                writer.write(childPageId + "\n");
            }
        }
    }

    public static Page loadFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String[] header = reader.readLine().split(",");
            int pageId = Integer.parseInt(header[0]);
            boolean isLeaf = Boolean.parseBoolean(header[1]);
            int keyCount = Integer.parseInt(header[2]);
            int maxKeys = Integer.parseInt(header[3]);

            Page Page = new Page(pageId, isLeaf, maxKeys / 2);
            Page.setKeyCount(keyCount);
            System.out.println("keyCount: " + keyCount);

            for (int i = 0; i < Page.getKeyCount(); i++) {
                String[] keyValue = reader.readLine().split(":");
                Page.getKeys().add(Integer.parseInt(keyValue[0]));
                Page.getIndexes().add(Integer.parseInt(keyValue[1]));
            }

            String childLine;
            while ((childLine = reader.readLine()) != null) {
                Page.getChildrenPageIds().add(Integer.parseInt(childLine));
            }

            return Page;
        }
    }

}
