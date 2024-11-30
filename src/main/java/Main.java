import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Page page = new Page(4, false);
        page.addValue(1, 15, 15);
        page.addValue(2, 30, 30);

        Page page2 = new Page(4, true);
        page2.addValue(null, 5, 20);
        page2.addValue(null, 10, 22);
        page2.addValue(null, 12, 26);
        page2.addValue(null, 13, 34);

        Page page3 = new Page(4, true);
        page3.addValue(null, 16, 45);
        page3.addValue(null, 19, 50);
        page3.addValue(null, 21, 50);
        page3.addValue(null, 26, 50);

        System.out.println(page);
        System.out.println(page2);
        System.out.println(page3);
        page.savePage();
        page2.savePage();
        page3.savePage();

        BTree bTree = new BTree();
        bTree.insert(7, 23);
    }
}
