import data.DataManager;
import data.Record;
import data.RecordBuffer;
import java.io.*;

public class BTree {
    private final int d;
    private int nextPageId;
    private NodePage root;
    private final Statistics stats;
    DataManager dataManager;

    public BTree(int d, int recordNum) {
        this.d = d;
        this.nextPageId = 0;
        this.root = new NodePage(nextPageId++, true, d);
        savePage(root);
        this.stats = new Statistics();
        this.dataManager = new DataManager(recordNum);
    }

    public NodePage getRoot() {
        return root;
    }

    public Statistics getStats() {
        return stats;
    }

    public void insertRecord(Record record) {
        if(search(root, record.getKey(), false) != -1) {
            System.out.println("Record with key " + record.getKey() + " already exists");
            return;
        }

        RecordBuffer buffer = new RecordBuffer(1); //todo: change buffer size, add read buffers
        buffer.addRecord(record);
        buffer.saveBuffer("src/main/java/data/data.txt");
        stats.incrementRecordBuffersSaved();
        insert(record.getKey(), dataManager.getRecordNum(), true);
    }

    public Record findRecord(int key) {
        int index = search(root, key, true);
        if (index == -1) {
            return null;
        }
        return dataManager.getRecord(index);
    }

    private int search(NodePage Page, int key, boolean print) {
        int i = 0;
        while (i < Page.getKeyCount() && key > Page.getKeys().get(i)) {
            i++;
        }

        if (i < Page.getKeyCount() && key == Page.getKeys().get(i)) {
            if (print)
                System.out.println("Key found at index: " + Page.getIndexes().get(i) + ", in tree page " + Page.getPageId());
            return Page.getIndexes().get(i);
        }
        else if (Page.isLeaf()) {
            if (print)
                System.out.println("Key not found");
            return -1;
        } else {
            NodePage child = loadPage(Page.getChildrenPageIds().get(i));
            stats.incrementPagesReadForSearch(1);
            return search(child, key, print);
        }
    }

    public void insert(int key, int index, boolean print) {
        if(search(root, key, false) != -1) {
            if(print)
                System.out.println("Record with key " + key + " already exists");
            return;
        }

        NodePage root = this.root;
        if (root.isFull()) {
            NodePage newRoot = new NodePage(nextPageId++, false, d);
            newRoot.addChildPageId(root.getPageId());
            splitChild(newRoot, 0, root);
            this.root = newRoot;
        }
        insertNonFull(this.root, key, index);
    }

    private void insertNonFull(NodePage page, int key, int index) {
        if (page.isLeaf()) {
            // Insert key in the leaf
            int i = page.getKeyCount() - 1;
            while (i >= 0 && key < page.getKeys().get(i)) {
                i--;
            }
            i++;
            page.getKeys().add(i, key);
            page.getIndexes().add(i, index);
            page.incrementKeyCount(1);
            savePage(page);
        } else {
            // Traverse to the proper child
            int i = page.getKeyCount() - 1;
            while (i >= 0 && key < page.getKeys().get(i)) {
                i--;
            }
            i++;
            NodePage child = loadPage(page.getChildrenPageIds().get(i));

            // Check if the child is full
            if (child.getKeyCount() == child.getMaxKeys()) {
                // Attempt to compensate with siblings before splitting
                if (!tryCompensate(page, i, child, key, index)) {
                    // Split the child
                    splitChild(page, i, child);
                    if (key > page.getKeys().get(i)) {
                        i++;
                    }
                }
                else {
                    return;
                }
            }

            // Continue insertion in the appropriate child
            insertNonFull(child, key, index);
        }
    }

    private boolean tryCompensate(NodePage parent, int childIndex, NodePage child, int key, int index) {
        boolean isRightSibling = childIndex < parent.getChildrenPageIds().size() - 1;
        boolean isLeftSibling = childIndex > 0;

        if (isRightSibling) {
            NodePage rightSibling = loadPage(parent.getChildrenPageIds().get(childIndex + 1));
            if (redistributeKeys(parent, child, rightSibling, childIndex, key, index, true)) {
                return true;
            }
        }

        if (isLeftSibling) {
            NodePage leftSibling = loadPage(parent.getChildrenPageIds().get(childIndex - 1));
            if (redistributeKeys(parent, child, leftSibling, childIndex - 1, key, index, false)) {
                return true;
            }
        }

        return false; // Redistribution not possible
    }

    private boolean redistributeKeys(NodePage parent, NodePage child, NodePage sibling,
                                     int parentKeyIndex, int key, int index, boolean isRightSibling) {
        if (sibling.getKeyCount() >= sibling.getMaxKeys()) {
            return false;
        }

        if (isRightSibling) {
            // Move parent key to right sibling
            sibling.getKeys().add(0, parent.getKeys().get(parentKeyIndex));
            sibling.getIndexes().add(0, parent.getIndexes().get(parentKeyIndex));
        } else {
            // Move parent key to left sibling
            sibling.getKeys().add(sibling.getKeyCount(), parent.getKeys().get(parentKeyIndex));
            sibling.getIndexes().add(sibling.getKeyCount(), parent.getIndexes().get(parentKeyIndex));
        }
        sibling.incrementKeyCount(1);

        // Update parent key and redistribute in the child if needed
        if (isRightSibling && key > child.getKeys().get(child.getKeyCount() - 1) ||
                !isRightSibling && key < child.getKeys().get(0)) {
            parent.getKeys().set(parentKeyIndex, key);
            parent.getIndexes().set(parentKeyIndex, index);
        } else {
            int removedKeyIndex = isRightSibling ? child.getKeyCount() - 1 : 0;
            parent.getKeys().set(parentKeyIndex, child.removeKey(removedKeyIndex));
            parent.getIndexes().set(parentKeyIndex, child.removeIndex(removedKeyIndex));
            child.incrementKeyCount(-1);

            int i = child.getKeyCount() - 1;
            while (i >= 0 && key < child.getKeys().get(i)) {
                i--;
            }
            child.getKeys().add(i + 1, key);
            child.getIndexes().add(i + 1, index);
            child.incrementKeyCount(1);
        }
        savePage(child);
        savePage(sibling);
        savePage(parent);
        return true;
    }


    private void splitChild(NodePage parent, int index, NodePage fullChild) {
        NodePage newChild = new NodePage(nextPageId++, fullChild.isLeaf(), d);

        for (int j = 0; j < d-1; j++) {
            newChild.getKeys().add(fullChild.getKeys().remove(d+1));
            newChild.getIndexes().add(fullChild.getIndexes().remove(d+1));
        }
        newChild.setKeyCount(d-1);

        if (!fullChild.isLeaf()) {
            for (int j = 0; j < d; j++) {
                newChild.getChildrenPageIds().add(fullChild.getChildrenPageIds().remove(d+1));
            }
        }

        // move middle key to parent
        parent.getKeys().add(index, fullChild.getKeys().remove(d));
        parent.getIndexes().add(index, fullChild.getIndexes().remove(d));
        parent.getChildrenPageIds().add(index + 1, newChild.getPageId());
        parent.incrementKeyCount(1);

        fullChild.setKeyCount(d);

        savePage(parent);
        savePage(fullChild);
        savePage(newChild);
    }

    public void printTree() {
        printNode(root, "");
    }

    private void printNode(NodePage page, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("Page ").append(page.getPageId()).append(": ").append(page.isLeaf() ? "leaf" : "internal")
                .append(", ").append("keys[").append(page.getKeyCount()).append("]: ");
        for (int i = 0; i < page.getKeyCount(); i++) {
            sb.append(page.getKeys().get(i)).append(" > ").append(page.getIndexes().get(i)).append(", ");
        }
        System.out.println(sb);
        for(int i = 0; i < page.getChildrenPageIds().size(); i++) {
            NodePage child = loadPage(page.getChildrenPageIds().get(i));
            printNode(child, prefix + "| ");
        }
    }

    public void printKeys(NodePage page) {
        for (int i = 0; i < page.getKeyCount(); i++) {
            if (!page.isLeaf()) {
                NodePage child = loadPage(page.getChildrenPageIds().get(i));
                printKeys(child);
            }
            // print vertically
            System.out.print(page.getKeys().get(i) + " ");
            // print horizontally
            //System.out.print(page.getKeys().get(i) + "\n");
        }

        if (!page.isLeaf()) {
            NodePage lastChild = loadPage(page.getChildrenPageIds().get(page.getKeyCount()));
            printKeys(lastChild);
        }
    }

    public NodePage loadPage(int pageId) {
        try {
            stats.incrementPagesRead();
            //System.out.println("loading page: " + pageId);
            return NodePage.loadFromFile("src/main/java/pages/Page" + pageId + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error loading Page: " + e.getMessage());
        }
    }

    public void savePage(NodePage page) {
        try {
            String pagesPath = "src/main/java/pages";
            stats.incrementPagesSaved(1);
            page.saveToFile(pagesPath + "/Page" + page.getPageId() + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error saving Page: " + e.getMessage());
        }
    }
}
