public class SearchResult {
    public Integer pageOffset;
    public boolean keyFound;

    public SearchResult(Integer pageOffset, boolean keyFound) {
        this.pageOffset = pageOffset;
        this.keyFound = keyFound;
    }
}
