package net.lamgc.oracle.sentry.oci.compute.ssh;

import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.common.SftpConstants;
import org.apache.sshd.sftp.common.SftpException;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import java.util.Set;

/**
 * Sftp 会话.
 * <p> 可通过会话访问远程服务器的文件.
 * @author LamGC
 */
@SuppressWarnings("unused")
public class SftpSession implements Closeable {

    /**
     * 排除的文件名.
     */
    private final static Set<String> EXCLUDED_FILE_NAMES;

    static {
        EXCLUDED_FILE_NAMES = new HashSet<>();
        EXCLUDED_FILE_NAMES.add(".");
        EXCLUDED_FILE_NAMES.add("..");
    }

    private final SftpClient sftpClient;

    /**
     * 创建 Sftp 会话.
     * @param sftpClient Sftp 客户端.
     */
    SftpSession(SftpClient sftpClient) {
        this.sftpClient = sftpClient;
    }

    /**
     * 获取指定文件夹内的所有文件.
     * @param path 文件夹路径.
     * @return 返回该目录下所有文件的文件名, 文件名不带路径.
     * @throws IOException 执行失败时抛出异常.
     */
    public Set<String> listFiles(String path) throws IOException {
        SftpClient.CloseableHandle handle = sftpClient.openDir(path);
        Set<String> paths = new HashSet<>();
        for (SftpClient.DirEntry entry : sftpClient.listDir(handle)) {
            if (EXCLUDED_FILE_NAMES.contains(entry.getFilename())) {
                continue;
            }
            paths.add(entry.getFilename());
        }
        sftpClient.close(handle);
        return paths;
    }

    /**
     * 读取指定路径的问题.
     * @param path 文件所在路径.
     * @return 返回文件输入流.
     * @throws FileNotFoundException 当文件不存在时抛出该异常.
     * @throws IOException 当操作执行失败时抛出异常.
     */
    public InputStream read(String path) throws IOException {
        if (!exists(path)) {
            throw new FileNotFoundException(path);
        }
        return sftpClient.read(path);
    }

    /**
     * 写入数据到指定目录.
     * @param path 待写入的路径.
     * @return 返回数据输出流.
     * @throws FileNotFoundException 当文件不存在时抛出该异常.
     * @throws IOException 如果操作失败则抛出异常.
     */
    public OutputStream write(String path) throws IOException {
        if (!exists(path)) {
            throw new FileNotFoundException(path);
        }
        return sftpClient.write(path, SftpClient.OpenMode.Write);
    }

    /**
     * 检查指定路径是否存在.
     * @param path 待检查的路径.
     * @return 如果存在, 返回 {@code true}, 如果文件不存在, 返回 {@code false}.
     * @throws IOException 执行失败时抛出.
     */
    public boolean exists(String path) throws IOException {
        try {
            return getAttributes(path) != null;
        } catch (IOException e) {
            if (e instanceof SftpException sftpException) {
                if (sftpException.getStatus() == SftpConstants.SSH_FX_NO_SUCH_FILE) {
                    return false;
                }
            }
            throw e;
        }
    }

    /**
     * 是否为一个目录.
     * @param path 待检查路径.
     * @return 如果是一个目录, 返回 {@code true}.
     * @throws FileNotFoundException 当路径不存在时抛出该异常.
     * @throws IOException 如果执行失败则抛出异常.
     */
    public boolean isDirectory(String path) throws IOException {
        if (!exists(path)) {
            throw new FileNotFoundException(path);
        }
        return getAttributes(path).isDirectory();
    }

    /**
     * 是否为一个文件.
     * @param path 待检查路径.
     * @return 如果是一个文件, 返回 {@code true}.
     * @throws FileNotFoundException 当路径不存在时抛出该异常.
     * @throws IOException 如果执行失败则抛出异常.
     */
    public boolean isFile(String path) throws IOException {
        if (!exists(path)) {
            throw new FileNotFoundException(path);
        }
        return getAttributes(path).isRegularFile();
    }

    /**
     * 获取文件大小.
     * @param path 待获取的路径.
     * @return 返回文件大小, 单位 b.
     * @throws NoSuchFileException 当指定路径不是一个文件(或符号链接)时抛出.
     * @throws FileNotFoundException 当指定路径不存在时抛出.
     * @throws IOException 当操作执行失败时抛出.
     */
    public long getFileSize(String path) throws IOException {
        if (!exists(path)) {
            throw new FileNotFoundException(path);
        }
        SftpClient.Attributes attributes = getAttributes(path);
        if (!attributes.isRegularFile()) {
            if (attributes.isSymbolicLink()) {
                return getFileSize(sftpClient.readLink(path));
            }
            throw new NoSuchFileException("Not a file: " + path);
        }

        return attributes.getSize();
    }

    /**
     * 获取指定路径的属性.
     * @param path 待获取属性的路径.
     * @return 返回路径所属属性.
     * @throws IOException 如果执行失败则抛出异常.
     */
    public SftpClient.Attributes getAttributes(String path) throws IOException {
        return sftpClient.stat(path);
    }

    /**
     * 创建文件夹.
     * @param path 待创建的目录.
     * @return 当文件夹已存在时返回 {@code false}, 不存在且创建成功则返回 {@code true}.
     * @throws IOException 如果操作执行失败则抛出异常.
     */
    public boolean mkdir(String path) throws IOException {
        if (exists(path)) {
            return false;
        }
        sftpClient.mkdir(path);
        return true;
    }

    /**
     * 创建新文件.
     * @param path 待创建的文件路径.
     * @return 当文件已存在时返回 {@code false}, 不存在且创建成功则返回 {@code true}.
     * @throws IOException 如果操作失败啧抛出异常.
     */
    public boolean createNewFile(String path) throws IOException {
        if (exists(path)) {
            return false;
        }
        SftpClient.CloseableHandle handle = sftpClient.open(path, SftpClient.OpenMode.Create);
        sftpClient.close(handle);
        return true;
    }

    /**
     * 删除指定路径.
     * <p> 该方法内部做了适配, 可兼容文件与文件夹两种类型.
     * @param path 待删除的路径.
     * @throws IOException 如果删除失败, 抛出异常.
     * @throws FileNotFoundException 当指定路径不存在时抛出该异常.
     * @throws DirectoryNotEmptyException 当路径为目录且目录不为空时抛出该异常.
     */
    public void delete(String path) throws IOException {
        if (!exists(path)) {
            throw new FileNotFoundException(path);
        }
        if (isDirectory(path)) {
            if (!listFiles(path).isEmpty()) {
                throw new DirectoryNotEmptyException(path);
            }
            sftpClient.rmdir(path);
        } else {
            sftpClient.remove(path);
        }
    }

    /**
     * 强制删除文件夹.
     * <p> 如果删除文件夹, 将会对文件夹执行遍历删除, 文件夹及子文件夹内的所有内容都会被删除.
     * @param path 待删除的路径.
     * @return 如果目标路径为文件夹且删除成功, 返回 {@code true}, 如果是文件, 则不会执行操作并返回 {@code false}.
     * 该行为是防止文件遭到误删.
     * @throws IOException 如果操作执行失败, 则抛出异常.
     */
    public boolean forceDeleteDir(String path) throws IOException {
        if (isDirectory(path)) {
            for (String filePath : listFiles(path)) {
                String fullFilePath = path + "/" + filePath;
                if (isDirectory(fullFilePath)) {
                    forceDeleteDir(fullFilePath);
                } else {
                    sftpClient.remove(fullFilePath);
                }
            }
            sftpClient.rmdir(path);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        sftpClient.close();
    }

}
