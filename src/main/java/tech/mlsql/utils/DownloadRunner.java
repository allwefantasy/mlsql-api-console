package tech.mlsql.utils;


import net.csdn.common.logging.CSLogger;
import net.csdn.common.logging.Loggers;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allwefantasy on 11/7/2017.
 */
public class DownloadRunner {

    private static CSLogger logger = Loggers.getLogger(DownloadRunner.class);

    private static final String HEADER_KEY = "Content-Disposition";
    private static final String HEADER_VALUE = "attachment; filename=";


    public static void iteratorFiles(String path, List<File> files) {
        File p = new File(path);
        if (p.exists()) {
            if (p.isFile()) {
                files.add(p);
            } else if (p.isDirectory()) {
                File[] fileStatusArr = p.listFiles();
                if (fileStatusArr != null && fileStatusArr.length > 0) {
                    for (File file : fileStatusArr) {
                        iteratorFiles(file.getPath(), files);
                    }

                }
            }
        }
    }

    public static int getTarFileByTarFile(HttpServletResponse response, String pathStr) throws UnsupportedEncodingException {

        String[] fileChunk = pathStr.split("/");
        response.setContentType("application/octet-stream");
        //response.setHeader("Transfer-Encoding", "chunked");
        response.setHeader(HEADER_KEY, HEADER_VALUE + "\"" + URLEncoder.encode(fileChunk[fileChunk.length - 1], "utf-8") + "\"");

        try {
            org.apache.commons.io.IOUtils.copyLarge(new FileInputStream(new File(pathStr)), response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return 500;

        }
        return 200;
    }

    public static int getTarFileByPath(HttpServletResponse response, String pathStr) throws UnsupportedEncodingException {

        String[] fileChunk = pathStr.split("/");
        response.setContentType("application/octet-stream");
        //response.setHeader("Transfer-Encoding", "chunked");
        response.setHeader(HEADER_KEY, HEADER_VALUE + "\"" + URLEncoder.encode(fileChunk[fileChunk.length - 1] + ".tar", "utf-8") + "\"");


        try {
            OutputStream outputStream = response.getOutputStream();

            ArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(outputStream);

            List<File> files = new ArrayList<File>();

            iteratorFiles(pathStr, files);

            if (files.size() > 0) {
                InputStream inputStream = null;
                int len = files.size();
                int i = 1;
                for (File cur : files) {
                    logger.info("[" + i++ + "/" + len + "]" + ",读取文件" + cur.getPath() + " entryName:" + fileChunk[fileChunk.length - 1] + cur.getPath().substring(pathStr.length()));
                    inputStream = new FileInputStream(cur);
                    ArchiveEntry entry = tarOutputStream.createArchiveEntry(cur, fileChunk[fileChunk.length - 1] + cur.getPath().substring(pathStr.length()));
                    tarOutputStream.putArchiveEntry(entry);
                    org.apache.commons.io.IOUtils.copyLarge(inputStream, tarOutputStream);
                    tarOutputStream.closeArchiveEntry();
                }
                tarOutputStream.flush();
                tarOutputStream.close();
                return 200;
            } else return 400;

        } catch (Exception e) {
            e.printStackTrace();
            return 500;

        }
    }
}
