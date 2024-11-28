public class Main {
    public static void main(String[] args) {
        Page page = new Page(4, true);
        page.addValue(1, 1, null);
        page.addValue(3, 3, null);
        page.addValue(4, 4, null);
        page.addValue(2, 2, null);
        System.out.println(page);
        page.savePage();

    }
}
