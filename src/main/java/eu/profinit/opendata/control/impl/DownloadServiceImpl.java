package eu.profinit.opendata.control.impl;

import eu.profinit.opendata.control.DownloadService;
import eu.profinit.opendata.model.DataInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

/**
 * Created by dm on 11/24/15.
 */
@Service
public class DownloadServiceImpl implements DownloadService {

    private Logger log = LogManager.getLogger(DownloadServiceImpl.class);

    @Override
    public InputStream downloadDataFile(DataInstance dataInstance) throws IOException {
        log.debug("Downloading from " + dataInstance.getUrl() + "...");
        return downloadDataFile(dataInstance.getUrl());
    }

    @Override
    public InputStream downloadDataFile(String urlString) throws IOException {
        if (isFileLocal(urlString)) {
            File localFile = new File(urlString);
            return new FileInputStream(localFile);
        }
        URL url  = new URL(urlString);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        log.debug("Download complete");
        return Channels.newInputStream(rbc);
    }

    @Override
    public InputStream downloadDataFileLocally(DataInstance dataInstance) throws IOException {
        Random random = new Random();

        URL url = new URL(dataInstance.getUrl());
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        String fileName = System.getProperty("java.io.tmpdir") + "/faktura-" + random.nextInt(100000);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        return downloadDataFile(fileName);
    }

    private boolean isFileLocal(String urlString) {
        return !urlString.startsWith("http");
    }
}
