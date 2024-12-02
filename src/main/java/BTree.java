import java.io.*;
import java.util.Optional;

public class BTree {
    private int d; // Minimalna liczba kluczy w węźle (maksymalnie 2d)
    private String basePath; // Ścieżka bazowa dla plików węzłów
    private int nextPageId; // Identyfikator dla kolejnych węzłów
    private Page root; // Korzeń drzewa

    public BTree(int d, String basePath) {
        this.d = d;
        this.basePath = basePath;
        this.nextPageId = 0;

        // Tworzymy początkowy korzeń jako liść
        this.root = new Page(nextPageId++, true, d);
        savePage(root);
    }

    // Operacja search()
    public Optional<Integer> search(int key) {
        return search(root, key);
    }

    private Optional<Integer> search(Page Page, int key) {
        int i = 0;
        while (i < Page.getKeyCount() && key > Page.getKeys().get(i)) {
            i++;
        }

        if (i < Page.getKeyCount() && key == Page.getKeys().get(i)) {
            return Optional.of(Page.getIndexes().get(i));
        }

        if (Page.isLeaf()) {
            return Optional.empty();
        } else {
            Page child = loadPage(Page.getChildrenPageIds().get(i));
            return search(child, key);
        }
    }

    // Operacja insert()
    public void insert(int key, int index) {
        Page root = this.root;
        if (root.isFull()) {
            // Korzeń jest pełny, potrzebny split
            Page newRoot = new Page(nextPageId++, false, d);
            newRoot.addChildPageId(root.getPageId());
            splitChild(newRoot, 0, root);
            this.root = newRoot;
            savePage(newRoot);
        }
        insertNonFull(this.root, key, index);
    }

    private void insertNonFull(Page Page, int key, int index) {
        if (Page.isLeaf()) {
            int i = Page.getKeyCount() - 1;
            while (i >= 0 && key < Page.getKeys().get(i)) {
                i--;
            }
            Page.getKeys().add(i + 1, key);
            Page.getIndexes().add(i + 1, index);
            Page.incrementKeyCount(1);
            savePage(Page);
        } else {
            int i = Page.getKeyCount() - 1;
            while (i >= 0 && key < Page.getKeys().get(i)) {
                i--;
            }
            i++;
            System.out.println("i: " + i);
            System.out.println("Page: " + Page.getKeys());
            System.out.println("Page.getChildrenPageIds(): " + Page.getChildrenPageIds());
            Page child = loadPage(Page.getChildrenPageIds().get(i));
            if (child.getKeyCount() == child.getMaxKeys()) { // Sprawdzamy, czy osiągnięto limit 2d+1
                splitChild(Page, i, child);
                if (key > Page.getKeys().get(i)) {
                    i++; // Przesuń wskaźnik, jeśli klucz idzie do nowego dziecka
                }
            }
            insertNonFull(loadPage(Page.getChildrenPageIds().get(i)), key, index);
        }
    }

    private void splitChild(Page parent, int index, Page fullChild) {
        Page newChild = new Page(nextPageId++, fullChild.isLeaf(), d);

        // Przenieś klucze i indeksy z pełnego węzła do nowego
        for (int j = 0; j < d-1; j++) {
            newChild.getKeys().add(fullChild.getKeys().remove(d+1));
            newChild.getIndexes().add(fullChild.getIndexes().remove(d+1));
        }
        newChild.setKeyCount(d-1); // Nowy węzeł ma dokładnie `d` kluczy
        System.out.println("newChild: " + newChild.getKeys());

        // Jeśli węzeł nie jest liściem, przenieś również dzieci
        if (!fullChild.isLeaf()) {
            for (int j = 0; j < d + 1; j++) {
                newChild.getChildrenPageIds().add(fullChild.getChildrenPageIds().remove(d + 1));
            }
        }

        // Przenieś środkowy klucz z pełnego węzła do rodzica
        parent.getKeys().add(index, fullChild.getKeys().remove(d));
        parent.getIndexes().add(index, fullChild.getIndexes().remove(d));
        parent.getChildrenPageIds().add(index + 1, newChild.getPageId());
        parent.incrementKeyCount(1);
        System.out.println("parent: " + parent.getKeys());

        // Zaktualizuj liczbę kluczy w oryginalnym węźle
        fullChild.setKeyCount(d);

        // Zapisz zmiany w plikach
        savePage(parent);
        savePage(fullChild);
        savePage(newChild);
    }

    // Zarządzanie plikami węzłów
    private void savePage(Page Page) {
        try {
            Page.saveToFile(basePath + "/Page_" + Page.getPageId() + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error saving Page: " + e.getMessage());
        }
    }

    private Page loadPage(int pageId) {
        try {
            return Page.loadFromFile(basePath + "/Page_" + pageId + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error loading Page: " + e.getMessage());
        }
    }
}
