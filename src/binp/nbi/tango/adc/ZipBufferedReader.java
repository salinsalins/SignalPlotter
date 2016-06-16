package binp.nbi.tango.adc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipBufferedReader implements Closeable {

    private File file;
    private ZipInputStream zipInputStream;
    private ZipEntry zipEntry;
    private BufferedReader bufferedReader;

    /**
     * @return the zipInputStream
     */
    public ZipInputStream getZipInputStream() {
        return zipInputStream;
    }

    /**
     * @return the bufferedReader
     */
    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    /**
     * @return the zipEntry
     */
    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return file.getAbsolutePath();
    }

    public ZipBufferedReader(File f) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(f);
        zipInputStream = new ZipInputStream(new BufferedInputStream(fis));
        InputStreamReader isr = new InputStreamReader(zipInputStream);
        bufferedReader = new BufferedReader(isr);
        file = f;
        this.zipEntry = null;
    }

    public ZipBufferedReader(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    public String readLine() throws IOException {
        return bufferedReader.readLine();
    }

    public int read() throws IOException {
        return bufferedReader.read();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return bufferedReader.read(cbuf, off, len);
    }

    public ZipEntry getNextEntry() throws IOException {
        zipEntry = zipInputStream.getNextEntry();
        return zipEntry;
    }

    public String getNextEntryName() throws IOException {
        zipEntry = zipInputStream.getNextEntry();
        return zipEntry.getName();
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
        zipEntry = null;
        //zipInputStream = null;
        //bufferedReader = null;
    }

    public boolean findZipEntry(String entryName) {
        if (entryName == null || "".equals(entryName)) {
            return false;
        }
        try {
            //reset();
            while (getNextEntry() != null) {
                if (entryName.equals(zipEntry.getName())) {
                    return true;
                }
            }
            //reset();
        } catch (IOException e) {
        }
        return false;
    }

    public List<String> readZipEntryList() {
        LinkedList<String> list = new LinkedList<>();
        try {
            while (getNextEntry() != null) {
                list.add(zipEntry.getName());
            }
        } catch (IOException e) {
        }
        return list;
    }

}
