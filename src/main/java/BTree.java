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
        root.savePage();
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
            newRoot.savePage();
            stats.incrementPagesSaved(1);
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
            page.savePage();
            System.out.println("Key " + key + " inserted at page " + page.getPageId());
            stats.incrementPagesSaved(1);
        } else {
            // Traverse to the child that will accommodate the key
            int i = page.getKeyCount() - 1;
            while (i >= 0 && key < page.getKeys().get(i)) {
                i--;
            }
            i++;
            NodePage child = loadPage(page.getChildrenPageIds().get(i));

            // Check if the child is full
            if (child.getKeyCount() == child.getMaxKeys()) {
                // Attempt to redistribute keys with siblings before splitting
                if (!tryCompensate(page, i, child, key, index)) {
                    // Split the child if redistribution is not possible
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
            insertNonFull(loadPage(page.getChildrenPageIds().get(i)), key, index);
        }
    }

    private boolean tryCompensate(NodePage parent, int childIndex, NodePage child, int key, int index) {
        // Check the right sibling
        if (childIndex < parent.getChildrenPageIds().size() - 1) {
            NodePage rightSibling = loadPage(parent.getChildrenPageIds().get(childIndex + 1));
            stats.incrementPagesRead();
            if (rightSibling.getKeyCount() < rightSibling.getMaxKeys()) {
                // Redistribute keys to right sibling
                rightSibling.getKeys().add(0, parent.getKeys().get(childIndex));
                rightSibling.getIndexes().add(0, parent.getIndexes().get(childIndex));
                rightSibling.incrementKeyCount(1);
                rightSibling.savePage();

                if(key > child.getKeys().get(child.getKeyCount() - 1)) {
                    parent.getKeys().set(childIndex,key);
                    parent.getIndexes().set(childIndex, index);
                    parent.incrementKeyCount(1);
                    System.out.println("Key " + key + " inserted at page " + parent.getPageId());
                } else {
                    parent.getKeys().set(childIndex, child.removeKey(child.getKeyCount() - 1));
                    parent.getIndexes().set(childIndex, child.removeIndex(child.getKeyCount() - 1));
                    child.incrementKeyCount(-1);
                    int i = child.getKeyCount() - 1;
                    while (i >= 0 && key < child.getKeys().get(i)) {
                        i--;
                    }
                    i++;
                    child.getKeys().add(i, key);
                    child.getIndexes().add(i, index);
                    child.incrementKeyCount(1);
                    System.out.println("Key " + key + " inserted at page " + child.getPageId());
                }
                child.savePage();
                parent.savePage();

                stats.incrementPagesSaved(3);
                return true;
            }
        }
        // Check the left sibling
        else if (childIndex > 0) {
            NodePage leftSibling = loadPage(parent.getChildrenPageIds().get(childIndex - 1));
            stats.incrementPagesRead();
            if (leftSibling.getKeyCount() < leftSibling.getMaxKeys()) {
                // Redistribute keys to left sibling
                leftSibling.getKeys().add(leftSibling.getKeyCount(), parent.getKeys().get(childIndex-1));
                leftSibling.getIndexes().add(leftSibling.getKeyCount(), parent.getIndexes().get(childIndex-1));
                leftSibling.incrementKeyCount(1);
                leftSibling.savePage();

                if(key < child.getKeys().get(0)) {
                    parent.getKeys().add(childIndex,key);
                    parent.getIndexes().set(childIndex, index);
                    parent.incrementKeyCount(1);
                    System.out.println("Key " + key + " inserted at page " + parent.getPageId());
                } else {
                    parent.getKeys().set(childIndex-1, child.removeKey(0));
                    parent.getIndexes().set(childIndex-1, child.removeIndex(0));
                    child.incrementKeyCount(-1);
                    int i = child.getKeyCount() - 1;
                    while (i >= 0 && key < child.getKeys().get(i)) {
                        i--;
                    }
                    i++;
                    child.getKeys().add(i, key);
                    child.getIndexes().add(i, index);
                    child.incrementKeyCount(1);
                    System.out.println("Key " + key + " inserted at page " + child.getPageId());
                }
                child.savePage();
                parent.savePage();

                stats.incrementPagesSaved(3);
                return true;
            }
        }

        return false; // Redistribution not possible
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

        parent.savePage();
        fullChild.savePage();
        newChild.savePage();
        stats.incrementPagesSaved(3);
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
            return NodePage.loadFromFile("src/main/java/pages/Page" + pageId + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error loading Page: " + e.getMessage());
        }
    }
}
