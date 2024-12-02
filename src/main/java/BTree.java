import data.DataManager;
import data.Record;
import data.RecordBuffer;

import java.io.*;

public class BTree {
    private final int d;
    private int nextPageId;
    private NodePage root;
    private int recordBuffersRead = 0;
    DataManager dataManager;

    public BTree(int d, int recordNum) {
        this.d = d;
        this.nextPageId = 0;
        this.root = new NodePage(nextPageId++, true, d);
        root.savePage();
        this.dataManager = new DataManager(recordNum);
    }

    public void init(String[] args, int recordNum,int bufferSize) {
        dataManager.deleteAllPages();
        dataManager.getData(args, "data");
        RecordBuffer buffer = new RecordBuffer(bufferSize);
        for(int i = 0; i < Math.ceil((double) recordNum /bufferSize); i++) {
            buffer.readRecords("src/main/java/data/data.txt", i * bufferSize);
            recordBuffersRead++;
            for(int j = 0; j < buffer.getCurrentSize(); j++) {
                insert(buffer.getRecord(j).getKey(), i * bufferSize + j);
            }
            buffer.clearBuffer();
        }
        printTree();
        System.out.println("\nRecord buffers read: " + recordBuffersRead);
        //todo record buffers saved, pages saved and read?
    }

    public void insertRecord(Record record) {
        //todo check if record already exists, do i generate key or insert it?

        RecordBuffer buffer = new RecordBuffer(1);
        buffer.addRecord(record);
        buffer.saveBuffer("src/main/java/data/data.txt");
        insert(record.getKey(), dataManager.getRecordNum());
    }

    public Record findRecord(int key) {
        int index = search(root, key);
        if (index == -1) {
            return null;
        }
        return dataManager.getRecord(index);
    }

    private int search(NodePage Page, int key) {
        int i = 0;
        while (i < Page.getKeyCount() && key > Page.getKeys().get(i)) {
            i++;
        }

        if (i < Page.getKeyCount() && key == Page.getKeys().get(i)) {
            System.out.println("Key found at index: " + Page.getIndexes().get(i));
            return Page.getIndexes().get(i);
        }

        if (Page.isLeaf()) {
            System.out.println("Key not found");
            return -1;
        } else {
            NodePage child = loadPage(Page.getChildrenPageIds().get(i));
            return search(child, key);
        }
    }

    // todo block same keys, also index should be calculated
    public void insert(int key, int index) {
        NodePage root = this.root;
        if (root.isFull()) {
            NodePage newRoot = new NodePage(nextPageId++, false, d);
            newRoot.addChildPageId(root.getPageId());
            splitChild(newRoot, 0, root);
            this.root = newRoot;
            newRoot.savePage();
        }
        insertNonFull(this.root, key, index);
    }

    private void insertNonFull(NodePage Page, int key, int index) {
        if (Page.isLeaf()) {
            int i = Page.getKeyCount() - 1;
            while (i >= 0 && key < Page.getKeys().get(i)) {
                i--;
            }
            Page.getKeys().add(i + 1, key);
            Page.getIndexes().add(i + 1, index);
            Page.incrementKeyCount(1);
            Page.savePage();
        } else {
            int i = Page.getKeyCount() - 1;
            while (i >= 0 && key < Page.getKeys().get(i)) {
                i--;
            }
            i++;
            NodePage child = loadPage(Page.getChildrenPageIds().get(i));
            if (child.getKeyCount() == child.getMaxKeys()) {
                splitChild(Page, i, child);
                if (key > Page.getKeys().get(i)) {
                    i++;
                }
            }
            insertNonFull(loadPage(Page.getChildrenPageIds().get(i)), key, index);
        }
    }

    private void splitChild(NodePage parent, int index, NodePage fullChild) {
        NodePage newChild = new NodePage(nextPageId++, fullChild.isLeaf(), d);

        for (int j = 0; j < d-1; j++) {
            newChild.getKeys().add(fullChild.getKeys().remove(d+1));
            newChild.getIndexes().add(fullChild.getIndexes().remove(d+1));
        }
        newChild.setKeyCount(d-1);

        if (!fullChild.isLeaf()) {
            for (int j = 0; j < d + 1; j++) {
                newChild.getChildrenPageIds().add(fullChild.getChildrenPageIds().remove(d));
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
    }

    public void printTree() {
        printNode(root, "");
    }

    private void printNode(NodePage page, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("Page ").append(page.getPageId()).append(": ");
        for (int i = 0; i < page.getKeyCount(); i++) {
            if (i == page.getKeyCount() - 1) {
                sb.append(page.getKeys().get(i)).append(" ");
            } else{
                sb.append(page.getKeys().get(i)).append(", ");
            }
            //sb.append(page.getIndexes().get(i)).append("; ");
        }
        System.out.println(sb.toString());
        for(int i = 0; i < page.getChildrenPageIds().size(); i++) {
            NodePage child = loadPage(page.getChildrenPageIds().get(i));
            printNode(child, prefix + "| ");
        }
    }

    public NodePage loadPage(int pageId) {
        try {
            return NodePage.loadFromFile("src/main/java/pages/Page" + pageId + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error loading Page: " + e.getMessage());
        }
    }
}
