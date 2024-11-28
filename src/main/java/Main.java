import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Page page = new Page(4, false);
        page.addValue(null, 1, 1);
        page.addValue(1, 3, 3);
        page.addValue(null, 4, 4);
        page.addValue(null, 5, 2);

        Page page2 = new Page(4, true);
        page2.addValue(null, 2, 5);
        System.out.println(page);
        System.out.println(page2);
        page.savePage();
        page2.savePage();

        BTree bTree = new BTree();
        bTree.search(2, 0);


    }
}
