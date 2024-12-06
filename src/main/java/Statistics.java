public class Statistics {
    private int pagesReadForSearch;
    private int recordBuffersRead;
    private int recordBuffersSaved;
    private int pagesSaved;
    private int pagesRead;

    public Statistics() {
        this.recordBuffersRead = 0;
        this.recordBuffersSaved = 0;
        this.pagesSaved = 0;
        this.pagesRead = 0;
        this.pagesReadForSearch = 0;
    }

    public int getRecordBuffersRead() { return recordBuffersRead; }
    public void incrementRecordBuffersRead() { this.recordBuffersRead++; }

    public int getRecordBuffersSaved() { return recordBuffersSaved; }
    public void incrementRecordBuffersSaved() { this.recordBuffersSaved++; }

    public int getPagesSaved() { return pagesSaved; }
    public void incrementPagesSaved(int i) { this.pagesSaved+=i; }

    public int getPagesRead() { return pagesRead; }
    public void incrementPagesRead() { this.pagesRead++; }

    public int getPagesReadForSearch() { return pagesReadForSearch; }
    public void incrementPagesReadForSearch(int i) { this.pagesReadForSearch+=i; }

    public void reset() {
        this.recordBuffersRead = 0;
        this.recordBuffersSaved = 0;
        this.pagesSaved = 0;
        this.pagesRead = 0;
        this.pagesReadForSearch = 0;
    }

    public void printStatistics() {
        System.out.println("\nPages read " + pagesRead + " (for search " + pagesReadForSearch + ")");
        System.out.println("Pages saved " + pagesSaved);
        System.out.println("Record buffers read " + recordBuffersRead);
        System.out.println("Record buffers saved " + recordBuffersSaved);
    }
}
