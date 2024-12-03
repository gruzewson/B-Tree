import data.DataManager;
import data.Record;
import data.RecordBuffer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
                insert(buffer.getRecord(j).getKey(), i * bufferSize + j + 1, false);
            }
            buffer.clearBuffer();
        }
        //printTree();
        System.out.println("\nRecord buffers read: " + recordBuffersRead);
        //todo record buffers saved, pages saved and read?
    }

    public void insertRecord(Record record) {
        //todo check if record already exists
        if(search(root, record.getKey(), false) != -1) {
            System.out.println("Record with key " + record.getKey() + " already exists");
            return;
        }

        RecordBuffer buffer = new RecordBuffer(1);
        buffer.addRecord(record);
        buffer.saveBuffer("src/main/java/data/data.txt");
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
        System.out.println(sb);
        for(int i = 0; i < page.getChildrenPageIds().size(); i++) {
            NodePage child = loadPage(page.getChildrenPageIds().get(i));
            printNode(child, prefix + "| ");
        }
    }

    public void printKeys(NodePage page) {
        for (int i = 0; i < page.getKeyCount(); i++) {
            // Visit the i-th child page first (if it exists)
            if (!page.isLeaf()) {
                NodePage child = loadPage(page.getChildrenPageIds().get(i));
                printKeys(child);
            }
            //print vertically
            System.out.print(page.getKeys().get(i) + " ");
            //print horizontally
            //System.out.print(page.getKeys().get(i) + "\n");
        }

        if (!page.isLeaf()) {
            NodePage lastChild = loadPage(page.getChildrenPageIds().get(page.getKeyCount()));
            printKeys(lastChild);
        }
    }


    public NodePage loadPage(int pageId) {
        try {
            return NodePage.loadFromFile("src/main/java/pages/Page" + pageId + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error loading Page: " + e.getMessage());
        }
    }

    public void interactiveMode() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------\n").append("Commands:\n").append("1 - insert record\n")
                .append("2 - find record\n").append("3 - print tree\n").append("4 - print keys\n")
                .append("q - exit program\n").append("-------------------------------");
        while(true){
            System.out.println(sb);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String command = reader.readLine();
            if(command.equals("q")){
                break;
            }
            if(command.equals("3")){
                printTree();
            }
            if(command.equals("1")){
                System.out.println("Enter record value [format: v1 v2 v3]: ");
                List<Float> values = new ArrayList<>();
                String[] inputs = reader.readLine().split(" ");
                for (String input : inputs) {
                    float value = Float.parseFloat(input);
                    values.add(value);
                }
                System.out.println("Enter record key: ");
                int key = Integer.parseInt(reader.readLine());
                insertRecord(new Record(values, key));
            }
            if(command.equals("2")){
                System.out.println("Enter key to find: ");
                int key = Integer.parseInt(reader.readLine().trim());
                Record record = findRecord(key);
                if (record != null) {
                    System.out.println(record);
                }

            }
            if(command.equals("4")){
                printKeys(root);
                System.out.println();
            }
        }
    }
}
