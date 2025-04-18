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
    int bufferSize;
    int height = 0;
    int duplicates = 0;

    public BTree(int d, int recordNum, int bufferSize) {
        this.d = d;
        this.nextPageId = 0;
        this.root = new NodePage(nextPageId++, true, d);
        this.height +=1;
        savePage(root);
        this.stats = new Statistics();
        this.dataManager = new DataManager(recordNum);
        this.bufferSize = bufferSize;
    }

    public NodePage getRoot() {
        return root;
    }

    public Statistics getStats() {
        return stats;
    }

    public void insertRecord(Record record) {
        /*if(search(root, record.getKey(), false) != -1) {
            System.out.println("Record with key " + record.getKey() + " already exists");
            return;
        }*/

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
        return dataManager.getRecord(index, bufferSize);
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
            //System.out.println("loading page: " + child.getPageId());
            stats.incrementPagesReadForSearch(1);
            return search(child, key, print);
        }
    }

    public void insert(int key, int index, boolean print) {

        NodePage root = this.root;
        if (root.isFull()) {
            NodePage newRoot = new NodePage(nextPageId++, false, d);
            newRoot.addChildPageId(root.getPageId());
            splitChild(newRoot, 0, root);
            this.root = newRoot;
            height +=1;
        }
        insertNonFull(this.root, key, index);
    }

    private void insertNonFull(NodePage Page, int key, int index) {
        if (Page.isLeaf()) {
            int i = 0;
            while (i < Page.getKeyCount() && key > Page.getKeys().get(i)) {
                i++;
            }

            if (i < Page.getKeyCount() && key == Page.getKeys().get(i)) {
                System.out.println("Record with key " + key + " already exists");
                duplicates++;
                return;
            }
            Page.getKeys().add(i, key);
            Page.getIndexes().add(i, index);
            Page.incrementKeyCount(1);
            savePage(Page);
        } else {
            int i = Page.getKeyCount() - 1;
            while (i >= 0 && key < Page.getKeys().get(i)) {
                i--;
            }
            i++;
            NodePage child = loadPage(Page.getChildrenPageIds().get(i));
            boolean compensate = false;
            if (child.getKeyCount() == child.getMaxKeys()) {
                if(child.isLeaf()){
                    compensate = compensate(Page, i, child, key);
                }
                if (!compensate){
                    splitChild(Page, i, child);
                    if (key > Page.getKeys().get(i)) {
                        i++;
                    }
                }
            }
            //System.out.println("loading page: " + Page.getChildrenPageIds().get(i));
            if(child.getPageId() == Page.getChildrenPageIds().get(i)) {
                insertNonFull(child, key, index);
            }
            else {
                insertNonFull(loadPage(Page.getChildrenPageIds().get(i)), key, index);
            }
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
                newChild.getChildrenPageIds().add(fullChild.getChildrenPageIds().remove(d));
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

    private boolean compensate(NodePage parent, int childIndex, NodePage child, int key) {
        //check right sibling
        if (childIndex < parent.getChildrenPageIds().size() - 1) {
            //check if key goes into parent
            if(key > child.getKeys().get(child.getKeyCount() - 1)){
                return false;
            }
            NodePage rightSibling = loadPage(parent.getChildrenPageIds().get(childIndex + 1)); //childIndex + 1 is the right sibling
            if (rightSibling.getKeyCount() < rightSibling.getMaxKeys()) {
                // Redistribute keys to right sibling
                rightSibling.getKeys().add(0, parent.getKeys().remove(childIndex));
                rightSibling.getIndexes().add(0, parent.getIndexes().remove(childIndex));
                rightSibling.incrementKeyCount(1);
                parent.incrementKeyCount(-1);
                savePage(rightSibling);
                //update parent and child
                parent.getKeys().add(childIndex, child.removeKey(child.getKeyCount() - 1));
                parent.getIndexes().add(childIndex, child.removeIndex(child.getKeyCount() - 1));
                child.incrementKeyCount(-1);
                parent.incrementKeyCount(1);
                savePage(child);
                savePage(parent);
                return true;
            }
            return false;
        }
        else if (childIndex > 0) {
            if(key < child.getKeys().get(0)){
                return false;
            }
            NodePage leftSibling = loadPage(parent.getChildrenPageIds().get(childIndex - 1));
            if (leftSibling.getKeyCount() < leftSibling.getMaxKeys()) {
                // Redistribute keys to left sibling
                leftSibling.getKeys().add(leftSibling.getKeyCount(), parent.getKeys().remove(childIndex - 1));
                leftSibling.getIndexes().add(leftSibling.getKeyCount(), parent.getIndexes().remove(childIndex - 1));
                leftSibling.incrementKeyCount(1);
                parent.incrementKeyCount(-1);
                savePage(leftSibling);
                //update parent and child
                parent.getKeys().add(childIndex - 1, child.removeKey(0));
                parent.getIndexes().add(childIndex - 1, child.removeIndex(0));
                child.incrementKeyCount(-1);
                parent.incrementKeyCount(1);
                savePage(child);
                savePage(parent);
                return true;
            }
            return false;
        }
        return false;
    }

    public void printTree() {
        printNode(root, "");
    }

    private void printNode(NodePage page, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("Page ").append(page.getPageId()).append(": ").append(page.isLeaf() ? "leaf" : "internal")
                .append(", ").append("keys[").append(page.getKeyCount()).append("]: {");
        for (int i = 0; i < page.getKeyCount(); i++) {
            //sb.append(page.getKeys().get(i)).append(" > ").append(page.getIndexes().get(i)).append(", ");
            if(i == page.getKeyCount() - 1) {
                sb.append(page.getKeys().get(i)).append("}");
            }
            else {
                sb.append(page.getKeys().get(i)).append(", ");
            }
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
            if (stats != null) {
                stats.incrementPagesSaved(1);
            }
            page.saveToFile(pagesPath + "/Page" + page.getPageId() + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error saving Page: " + e.getMessage());
        }
    }

    public int getHeight() {
        return height;
    }

}
