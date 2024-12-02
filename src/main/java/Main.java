import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        int d = 2; // Minimalna liczba kluczy w węźle (maksymalnie 2 * d)
        String basePath = "src/main/java/data";

        BTree btree = new BTree(d, basePath);
        btree.insert(10, 100);
        btree.insert(20, 200);
        btree.insert(30, 50);
        btree.insert(40, 150);
        btree.insert(50, 250);
        btree.insert(21, 250);
        btree.insert(22, 250);
        btree.insert(23, 250);
        /*btree.insert(60, 250);
        btree.insert(70, 250);
        btree.insert(11, 250);
        btree.insert(12, 250);
        btree.insert(13, 250);
        btree.insert(14, 250);*/
        /*btree.insert(31, 300);
        btree.insert(32, 400);
        btree.insert(29, 400);
        btree.insert(51, 400);*/
        //btree.insert(19, 400);
        //btree.insert(21, 400);
        //btree.insert(26, 400);

        Optional<Integer> result = btree.search(10);
        result.ifPresentOrElse(
                index -> System.out.println("Found key at index: " + index),
                () -> System.out.println("Key not found")
        );
    }
}
