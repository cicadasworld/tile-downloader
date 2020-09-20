import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * TMS tile downloader thread class. 
 */
public class TileDownloader implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(TileDownloader.class);

	// Variables
	private final String tileService;
	private final String destPath;

	private final int zoomLevelStartIndex;
	private final int zoomLevelEndIndex;

	private int xStartIndex;
	private int yStartIndex;

	private final int xEndIndex;
	private final int yEndIndex;

	// Global Statistic Parameters
	public static int downImageCount = 0;

	public TileDownloader(Builder builder) {
		this.tileService = builder.tileService;
		this.destPath = builder.destPath;
		this.zoomLevelStartIndex = builder.zoomLevelStartIndex;
		this.zoomLevelEndIndex = builder.zoomLevelEndIndex;
		this.xStartIndex = builder.xStartIndex;
		this.yStartIndex = builder.yStartIndex;
		this.xEndIndex = builder.xEndIndex;
		this.yEndIndex = builder.yEndIndex;
	}

	@Override
	public void run() {
		downloadTiles();
	}

	/**
	 * Download tiles.
	 */
	private void downloadTiles() {
		String destImgPath;
		String destImgName;
		String tileServiceImg;
		for (int z = zoomLevelStartIndex; z <= zoomLevelEndIndex; z++) {
			int maxXOfCurrentZoomLevel;
			int maxYOfCurrentZoomLevel;
			int xStart;
			int yStart;
			int xEnd;
			int yEnd;

			maxXOfCurrentZoomLevel = (int) (Math.pow(2, z));
			maxYOfCurrentZoomLevel = (int) (Math.pow(2, z));

			// X start
			if ((z == zoomLevelStartIndex) && (xStartIndex != 0)) {
				xStart = xStartIndex;
			} else {
				xStart = 0;
			}

			// X end
			if ((z == zoomLevelEndIndex) && (xEndIndex != 0)) {
				xEnd = xEndIndex;
			} else {
				xEnd = maxXOfCurrentZoomLevel - 1;
			}

			for (int x = xStart; x <= xEnd; x++) {
				// Y Start
				if ((z == zoomLevelStartIndex) && (x == xStart) && (yStartIndex != 0)) {
					yStart = yStartIndex;
				} else {
					yStart = 0;
				}

				// Y end
				if ((z == zoomLevelEndIndex) && (x == xEnd) && (yEndIndex != 0)) {
					yEnd = yEndIndex;
				} else {
					yEnd = maxYOfCurrentZoomLevel - 1;
				}

				for (int y = yStart; y <= yEnd; y++) {
					destImgPath = destPath + "/" + z + "/" + x;
					destImgName = y + ".png";
					tileServiceImg = getImgUrl(tileService, x, y, z);
					
					//Check if image exist.
					File checkFile = new File(destImgPath + "/" + destImgName);
					if(checkFile.exists()){
						logger.info("exist : " + destImgPath + "/" + destImgName);
						continue;
					}
					logger.info("download : " + destImgPath + "/" + destImgName);

					final byte[] imgBytes;
					if(tileService.toUpperCase().startsWith("HTTPS")){
						imgBytes = this.getImageByteArrayHTTPS(tileServiceImg);
					}else{
						imgBytes = this.getImageByteArrayHTTP(tileServiceImg);
					}
					
					if (imgBytes == null) {
						return;
					}

					final boolean writeResult = writeImageToDestination(destImgPath, destImgName, imgBytes);
					if (!writeResult) {
						return;
					}
					downImageCount++;
				}
				// Clean
				yStartIndex = 0;
			}
			// Clean
			xStartIndex = 0;
		}
		logger.info("completed!");
	}

	private String getImgUrl(String tileService, int x, int y, int z) {
		return tileService.replaceAll("\\{x\\}", String.valueOf(x))
				.replaceAll("\\{y\\}", String.valueOf(y))
				.replaceAll("\\{z\\}", String.valueOf(z));
	}

	/**
	 * Download image from http service.
	 */
	private byte[] getImageByteArrayHTTP(final String urlStr) {
		try {
			final URL url = new URL(urlStr);
			final InputStream in = new BufferedInputStream(url.openStream());
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buf = new byte[1024];
			int n;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			return out.toByteArray();
		} catch (final Exception e) {
			logger.info("getImageByteArrayHTTP:");
			logger.info(urlStr);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Download image from https service.
	 */
	private byte[] getImageByteArrayHTTPS(final String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			if (con == null){
				return null;
			}
			
			final InputStream in = con.getInputStream();
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buf = new byte[1024];
			int n;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			
			out.close();
			in.close();
			return out.toByteArray();
		} catch (final Exception e) {
			logger.info("getImageByteArrayHTTPS:");
			logger.info(urlStr);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Write image to destination.
	 */
	private boolean writeImageToDestination(final String destinationPath,
			final String imageName, final byte[] image) {
		try {
			final File file = new File(destinationPath);
			file.mkdirs();
			final FileOutputStream fos = new FileOutputStream(destinationPath + "/" + imageName);
			fos.write(image);
			fos.close();
			return true;
		} catch (final Exception e) {
			logger.info("writeImageToDestination:");
			logger.info(destinationPath);
			e.printStackTrace();
			return false;
		}
	}

	public static final class Builder {
		private String tileService;
		private String destPath;
		private int zoomLevelStartIndex = 0;
		private int zoomLevelEndIndex = 0;
		private int xStartIndex = 0;
		private int yStartIndex = 0;
		private int xEndIndex = 0;
		private int yEndIndex = 0;

		public Builder() {

		}
		public Builder tileService(String tileService) {
			this.tileService = tileService;
			return this;
		}

		public Builder destPath(String destPath) {
			this.destPath = destPath;
			return this;
		}

		public Builder zoomLevelStartIndex(int zoomLevelStartIndex) {
			this.zoomLevelStartIndex = zoomLevelStartIndex;
			return this;
		}

		public Builder zoomLevelEndIndex(int zoomLevelEndIndex) {
			this.zoomLevelEndIndex = zoomLevelEndIndex;
			return this;
		}

		public Builder xStartIndex(int xStartIndex) {
			this.xStartIndex = xStartIndex;
			return this;
		}

		public Builder yStartIndex(int yStartIndex) {
			this.yStartIndex = yStartIndex;
			return this;
		}

		public Builder xEndIndex(int xEndIndex) {
			this.xEndIndex = xEndIndex;
			return this;
		}

		public Builder yEndIndex(int yEndIndex) {
			this.yEndIndex = yEndIndex;
			return this;
		}

		public TileDownloader build() {
			return new TileDownloader(this);
		}
	}
}