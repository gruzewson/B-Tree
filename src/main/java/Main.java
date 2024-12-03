import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        int d = 2; // Minimalna liczba kluczy w węźle (maksymalnie 2 * d)
        int recordNum = 100; // Liczba rekordów do wygenerowania

        BTree btree = new BTree(d, recordNum);
        btree.init(args, recordNum, 40);
        //System.out.println("record with key 1: " + btree.findRecord(1));

        btree.interactiveMode();

    }
}
