import java.io.FileNotFoundException;
import java.util.Objects;

//import static jdk.javadoc.internal.doclets.toolkit.util.DocPath.parent;

public class BTree {
    private final int h;
    private final int size = 4;
    private String path = "src/main/java/btree.txt";
    private Page root;
    //todo add to buffer

    public BTree() throws FileNotFoundException {
        this.h = 0;
        Page root = new Page(size, true);
        root.savePage();
        this.root = root;
    }

    public int search(int key, int offset, boolean print) throws FileNotFoundException {
        System.out.println("Searching for key " + key);
        Page page = new Page(size, true);
        page.readPage(path, offset);
        int i = 0;

        while (i < page.getNumberOfKeys() && key > page.keys.get(i)) {
            i++;
        }

        if (i < page.getNumberOfKeys() && key == page.keys.get(i)) {
            if (print) {
                System.out.println("Key " + key + " found at index " + i + " in page at offset " + offset);
            }
            return offset;
        }
        if (page.isLeaf()) {
            if (print) {
                System.out.println("Key " + key + " not found");
            }
            return -1; // Indicating not found
        }
        int childOffset = page.childOffsets.get(i);
        if (print) {
            System.out.println("Descending to child at offset " + childOffset);
        }
        return search(key, childOffset, print);
    }

    public void splitChild(Page page, int i) throws FileNotFoundException {
        Page newPage = new Page(size, page.isLeaf());
        Page child = new Page(size, page.isLeaf());
        child.readPage(path, page.childOffsets.get(i));
        newPage.setLeaf(child.isLeaf());
        newPage.setNumberOfKeys(size - 1); //todo t - 1
        for (int j = 1; j < size; j++) { //todo t -1
            newPage.keys.set(j, child.keys.get(j + size)); //todo j + t
            newPage.recordIndexes.set(j, child.recordIndexes.get(j + size)); //todo j+t
        }
        if (!child.isLeaf()) {
            for (int j = 1; j <= size; j++) { //todo from j = 1 to t
                newPage.childOffsets.set(j, child.childOffsets.get(j + size)); //todo j + t
            }
        }
        child.setNumberOfKeys(size - 1); //todo t - 1
        for (int j = page.getNumberOfKeys() + 1; j >= i + 1; j--) {
            page.childOffsets.set(j + 1, page.childOffsets.get(j));
        }
        page.childOffsets.set(i + 1, newPage.getOffset()); //todo how to get offset
        for (int j = page.getNumberOfKeys(); j >= i; j--) {
            page.keys.set(j + 1, page.keys.get(j));
            page.recordIndexes.set(j + 1, page.recordIndexes.get(j));
        }
        page.keys.set(i, child.keys.get(size)); //todo t
        page.recordIndexes.set(i, child.recordIndexes.get(size)); //todo t
        page.setNumberOfKeys(page.getNumberOfKeys() + 1);
        child.savePage();
        newPage.savePage();
        page.savePage();
    }

    public void insertNonFull(Page page, int key, int index) throws FileNotFoundException {
        int i = page.getNumberOfKeys();
        if (page.isLeaf()) {
            while (i >= 1 && key < page.keys.get(i)) {
                page.keys.set(i + 1, page.keys.get(i));
                page.recordIndexes.set(i + 1, page.recordIndexes.get(i));
                i--;
            }
            page.keys.set(i + 1, key);
            page.recordIndexes.set(i + 1, index);
            page.setNumberOfKeys(page.getNumberOfKeys() + 1);
            page.savePage();
        } else {
            while (i >= 1 && key < page.keys.get(i)) {
                i--;
            }
            i++;
            Page child = new Page(size, true);
            child.readPage(path, page.childOffsets.get(i));
            if (child.getNumberOfKeys() == size) { //2t-1 todo change
                splitChild(page, i);
                if (key > page.keys.get(i)) {
                    i++;
                }
            }
            insertNonFull(child, key, index);
        }
    }

    public void insert(int key, int index) throws FileNotFoundException {
        Page root = new Page(size, true);
        root.readPage(path, 0);
        if(root.getNumberOfKeys() == size) { //todo 2t-1
            Page newRoot = new Page(size, false);
            newRoot.childOffsets.set(1, 0); //todo offsets hav to be right...
            this.root = newRoot;
            splitChild(newRoot, 1);
            insertNonFull(newRoot, key, index);
        }
        else {
            insertNonFull(root, key, index);
        }
    }


}
