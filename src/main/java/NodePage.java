import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NodePage {
    private final int pageId;
    private final boolean isLeaf;
    private int keyCount;
    private final List<Integer> keys;
    private final List<Integer> indexes;
    private final List<Integer> childrenPageIds;
    private final int maxKeys;

    public NodePage(int pageId, boolean isLeaf, int d) {
        this.pageId = pageId;
        this.isLeaf = isLeaf;
        this.keyCount = 0;
        this.keys = new ArrayList<>();
        this.indexes = new ArrayList<>();
        this.childrenPageIds = new ArrayList<>();
        this.maxKeys = 2 * d;
    }

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

    public int removeKey(int index) {
        return keys.remove(index);
    }

    public int removeIndex(int index) {
        return indexes.remove(index);
    }

    public int getMaxKeys() {
        return maxKeys;
    }

    public boolean isFull() {
        return keyCount >= maxKeys;
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

    public static NodePage loadFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String[] header = reader.readLine().split(",");
            if (header.length != 4) {
                throw new IllegalArgumentException("Invalid header format: " + String.join(",", header));
            }

            int pageId = Integer.parseInt(header[0].trim());
            boolean isLeaf = Boolean.parseBoolean(header[1].trim());
            int keyCount = Integer.parseInt(header[2].trim());
            int maxKeys = Integer.parseInt(header[3].trim());

            NodePage page = new NodePage(pageId, isLeaf, maxKeys / 2);
            page.setKeyCount(keyCount);

            // Parse keys and indexes
            for (int i = 0; i < keyCount; i++) {
                String line = reader.readLine();
                String[] keyValue = line.split(":");
                page.getKeys().add(Integer.parseInt(keyValue[0].trim()));
                page.getIndexes().add(Integer.parseInt(keyValue[1].trim()));
            }

            // Parse child page IDs if not a leaf
            if (!isLeaf) {
                String childLine;
                while ((childLine = reader.readLine()) != null) {
                    page.getChildrenPageIds().add(Integer.parseInt(childLine.trim()));
                }
            }
            return page;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error parsing number in file: " + filePath, e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error in file format: " + filePath, e);
        }
    }
}
