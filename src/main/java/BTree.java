import java.io.FileNotFoundException;

public class BTree {
    private final int h;
    private final int size = 4;
    private String path = "src/main/java/btree.txt";
    private Page page;

    public BTree() {
        this.h = 0;
        this.page = new Page(size, false);
    }

    public void search(int key, int offset) throws FileNotFoundException {
        System.out.println("Searching for key " + key);
        page.readPage(path ,offset);

        //can improve - maybe bisection??
        int i = 0;
        while (i < page.getNumberOfKeys() && page.keys.get(i) < key) {
            i++;
        }
        if (i < page.getNumberOfKeys() && page.keys.get(i) == key) {
            System.out.println("Key " + key + " found in the tree, with index" + page.recordIndexes.get(i));
        } else if(page.isLeaf() || page.childOffsets.get(i) == null) {
            System.out.println("Key " + key + " not found in the tree");
        }
        else {
            search(key, page.childOffsets.get(i));
        }
    }


}
