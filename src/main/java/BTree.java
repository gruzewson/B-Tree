import java.io.FileNotFoundException;
import java.util.Objects;

//import static jdk.javadoc.internal.doclets.toolkit.util.DocPath.parent;

public class BTree {
    private final int h;
    private final int size = 4;
    private String path = "src/main/java/btree.txt";
    private Page page; //todo change to buffer

    public BTree() {
        this.h = 0;
        this.page = new Page(size, false);
    }

    public SearchResult search(int key, int offset, boolean print) throws FileNotFoundException {
        System.out.println("Searching for key " + key);
        page.readPage(path, offset);

        int i = 0;
        while (i < page.getNumberOfKeys() && page.keys.get(i) < key) {
            i++;
        }
        if(i >= size) {
            System.out.println("Key " + key + " not found");
            return new SearchResult(offset, false);
        }

        if (i < page.getNumberOfKeys() && page.keys.get(i) == key) {
            if (print)
                System.out.println("Key " + key + " found in the tree, with index " + page.recordIndexes.get(i));
            return new SearchResult(offset, true);
        }

        if (page.isLeaf() || page.childOffsets.get(i) == null) {
            if (print)
                System.out.println("Key " + key + " not found");
            return new SearchResult(offset, false);
        }

        return search(key, page.childOffsets.get(i), print);
    }


    public void insert(int key, int index) throws FileNotFoundException {
        System.out.println("Inserting key " + key);

        SearchResult result = search(key, 0, false);

        if (result.keyFound) {
            System.out.println("Key " + key + " already exists in the tree");
            return;
        }
        page.readPage(path, result.pageOffset);
        System.out.println("Page: " + page);

        if (page.getNumberOfKeys() < size) {
            page.addValue(null, key, index);
            page.updatePage(result.pageOffset, path);
        } else if(page.getNumberOfKeys() == size) {
            //compensate
            System.out.println("Page is full, compensating");
            Page rightPage = new Page(size, page.isLeaf());
            rightPage.readPage(path, result.pageOffset+1); //+1 because we check a sibling TODO: check if this is correct
            if(rightPage.getNumberOfKeys() < size) {
                Page leftPage = page;
                Page parentPage = new Page(size, false);
                parentPage.readPage(path, result.pageOffset-1); //-1 because we check a parent TODO: check if this is correct
                //find the parent key to move
                int parentKeyIndex = 0;
                while(parentPage.keys.get(parentKeyIndex) < key) {
                    parentKeyIndex++;
                }
                //move the parent key to the right page
                rightPage.addValue(parentPage.childOffsets.get(parentKeyIndex), parentPage.keys.get(parentKeyIndex), parentPage.recordIndexes.get(parentKeyIndex));
                //move the left page's last key to the parent //TODO: check if this is necessary
                /*if(leftPage.keys.get(size-1) < key) {
                    parentPage.keys.set(parentKeyIndex, key);
                    parentPage.recordIndexes.set(parentKeyIndex, index);*/
                //} else {
                parentPage.keys.set(parentKeyIndex, leftPage.keys.get(size-1));
                parentPage.recordIndexes.set(parentKeyIndex, leftPage.recordIndexes.get(size-1));
                parentPage.childOffsets.set(parentKeyIndex, leftPage.childOffsets.get(size-1));
                //add key to the left side
                leftPage.setNumberOfKeys(size-1);
                leftPage.addValue(null, key, index);
                //update pages
                leftPage.updatePage(result.pageOffset, path);
                rightPage.updatePage(result.pageOffset+1, path);
                parentPage.updatePage(result.pageOffset-1, path);
            }
            else {
                System.out.println("Cant compensate, splitting");
                //split
                int midIndex = page.getNumberOfKeys() / 2;

                int midKey;
                if(key < page.keys.get(midIndex)) {
                    midKey = page.keys.get(midIndex-1);
                    midIndex--;
                } else {
                    midKey = key;
                }
                int midRecordIndex = page.recordIndexes.get(midIndex);

                // Create the new right page
                Page newRightPage = new Page(size, page.isLeaf());

                // Move the keys, recordIndexes, and childOffsets to the new right page
                for (int i = midIndex + 1; i < page.getNumberOfKeys(); i++) {
                    newRightPage.addValue(page.childOffsets.get(i), page.keys.get(i), page.recordIndexes.get(i));
                }

                // Create the parent page
                Page parentPage = new Page(size, false);

                // Add the mid key to the parent page
                parentPage.addValue(null, midKey, midRecordIndex);
                System.out.println("Parent page: " + midKey);

                // Adjust the current page (left side of the split) to contain only the keys up to the middle
                page.setNumberOfKeys(midIndex);

                // Update the left page and the right page
                page.addValue(null, key, index);
                page.updatePage(result.pageOffset, path);
                newRightPage.savePage();
                parentPage.updatePage(result.pageOffset - 1, path);


            }
        }
    }


}
