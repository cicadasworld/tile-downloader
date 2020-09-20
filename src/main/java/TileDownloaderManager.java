import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TileDownloaderManager {

    private static final Logger logger = LoggerFactory.getLogger(TileDownloaderManager.class);

    /**
     * Single Thread Tile Download
     */
    public void downloadTilesSingleThread(String tileService, String destPath, int zStart, int zEnd) {
        Date startDate = new Date();
        TileDownloader tileDownloader = new TileDownloader.Builder()
                .tileService(tileService)
                .destPath(destPath)
                .zoomLevelStartIndex(zStart)
                .zoomLevelEndIndex(zEnd)
                .xStartIndex(0)
                .yStartIndex(0)
                .xEndIndex(0)
                .yEndIndex(0)
                .build();
        Thread tileDownloaderThread = new Thread(tileDownloader);
        tileDownloaderThread.start();

        try {
            tileDownloaderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Date endDate = new Date();
        long diffMill = endDate.getTime() - startDate.getTime();
        logger.info("==============ZOOM " + zStart + "-" + zEnd + "==============");
        logger.info("Time : " + diffMill);
        logger.info("Img Count : " + TileDownloader.downImageCount);
        logger.info("Avg : " + diffMill / TileDownloader.downImageCount);
        logger.info("=====================================");
    }

    /**
     * Multi Thread Tile Download
     * <p>
     * ZOOM : 0, 1, 2 	Thd : 1
     * ZOOM : 3 		Thd : 3 ( 2^3/2 -1 )
     * ZOOM : 4 		Thd : 7 ( 2^4/2 -1 ) ZOOM
     * : 5 		Thd : 15( 2^5/2 -1 ) ZOOM : X >= 6	Thd : 32
     */
    public void downloadTilesMultiThread(final String tileService, final String destPath, int zStart, int zEnd) {
        int numberOfThread;
        Date startDate = new Date();

        // Divide works to threads
        List<Thread> threads = new ArrayList<>();
        for (int z = zStart; z <= zEnd; z++) {
            logger.info("Zoom : " + z);
            //Calculate number of thread
            if (z < 3) {
                numberOfThread = 1;
            } else if (z < 6) {
                numberOfThread = (int) Math.pow(2, z - 1) - 1;
            } else {
                numberOfThread = 32;
            }
            int totalXDirectoryNum = (int) (Math.pow(2, z));
            for (int xStart = 0; xStart < totalXDirectoryNum; ) {
                int xEnd;
                //If directory num enough for thread num.
                if (totalXDirectoryNum / numberOfThread > 1) {
                    xEnd = xStart + totalXDirectoryNum / numberOfThread - 1;
                } else {
                    //If not then download all dirs.
                    xEnd = 0;
                }
                if (xEnd >= totalXDirectoryNum) {
                    xEnd = totalXDirectoryNum - 1;
                }

                TileDownloader tileDownloader = new TileDownloader.Builder()
                        .tileService(tileService)
                        .destPath(destPath)
                        .zoomLevelStartIndex(z)
                        .zoomLevelEndIndex(z)
                        .xStartIndex(xStart)
                        .yStartIndex(0)
                        .xEndIndex(xEnd)
                        .yEndIndex(0)
                        .build();
                logger.info("xStart:" + xStart + " xEnd:" + xEnd);
                Thread tileDownloaderThread = new Thread(tileDownloader);
                tileDownloaderThread.start();
                threads.add(tileDownloaderThread);
                if (totalXDirectoryNum / numberOfThread > 1) {
                    xStart += totalXDirectoryNum / numberOfThread;
                } else {
                    //Don't continue..
                    xStart = totalXDirectoryNum;
                }
            }

            //Wait threads
            logger.info("List size : " + threads.size());
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            logger.info("---Clear Thread List---");
            threads.clear();
        }

        Date endDate = new Date();

        long diffMill = endDate.getTime() - startDate.getTime();
        logger.info("==============ZOOM " + zStart + "-" + zEnd + "==============");
        logger.info("Time : " + diffMill);
        logger.info("Img Count : " + TileDownloader.downImageCount);
        logger.info("Avg Time : " + diffMill / TileDownloader.downImageCount);
        logger.info("====================================");
    }
}
