/**
 * create by
 *
 * @author hujin 2020/9/20
 */
public class Main {

    private final static String url;
    private final static String destPath;
    private final static int zoomStart;
    private final static int zoomEnd;

    static {
        url = Configuration.getInstance().getString("download.url");
        destPath = Configuration.getInstance().getString("download.path");
        zoomStart = Configuration.getInstance().getInt("download.zoomStart");
        zoomEnd = Configuration.getInstance().getInt("download.zoomEnd");
    }

    public static void main(String[] args) {
        TileDownloaderManager tileDownloaderManager = new TileDownloaderManager();
        //tileDownloaderManager.downloadTilesSingleThread(url, destPath, zoomStart, zoomEnd);
        tileDownloaderManager.downloadTilesMultiThread(url, destPath, zoomStart, zoomEnd);
    }

}
