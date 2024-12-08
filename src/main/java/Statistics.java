public class Statistics {
    private int pagesReadForSearch;
    private int pagesSaved;
    private int pagesRead;

    public Statistics() {
        this.pagesSaved = 0;
        this.pagesRead = 0;
        this.pagesReadForSearch = 0;
    }

    public void incrementPagesSaved(int i) { this.pagesSaved+=i; }
    public void incrementPagesRead() { this.pagesRead++; }
    public void incrementPagesReadForSearch(int i) { this.pagesReadForSearch+=i; }

    public void reset() {
        this.pagesSaved = 0;
        this.pagesRead = 0;
        this.pagesReadForSearch = 0;
    }

    public void printStatistics() {
        System.out.println("\nPages read " + pagesRead);
        System.out.println("Pages saved " + pagesSaved);
    }
}
