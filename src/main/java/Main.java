import data.DataManager;
import data.Record;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        int d = 2; // Minimalna liczba kluczy w węźle (maksymalnie 2 * d)
        int recordNum = 10; // Liczba rekordów do wygenerowania

        BTree btree = new BTree(d, recordNum);
        btree.init(args, recordNum, 3);
        btree.insertRecord(new Record(List.of(1.0f, 2.0f, 3.0f) , 1));
        btree.printTree();

        System.out.println("record with key 1: " + btree.findRecord(1));

    }
}
