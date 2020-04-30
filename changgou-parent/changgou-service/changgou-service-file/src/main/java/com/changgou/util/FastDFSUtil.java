package com.changgou.util;


import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 实现FastDFS文件管理
 *      文件上传
 *      文件删除
 *      文件下载
 *      文件信息获取
 *      Storage信息获取
 *      Tracker信息获取
 */
public class FastDFSUtil {

    /**
     * 加载Tracker信息
     */
    static {
        try {
            //查找文件classpath下的文件路径
            String filePath = new ClassPathResource("fdfs_client.conf").getPath();
            //加载tracker链接信息
            ClientGlobal.init(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装获取TrackerService
     * @return
     * @throws Exception
     */
    public static TrackerServer getTrackerServer() throws Exception {
        // 创建一个TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient获取TrackerServer链接对象，并获取Storage信息
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    public static StorageClient getStorageClient(TrackerServer trackerServer){
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }

    /**
     * 文件上传
     * @Parm fastDFSFile:上传问文件信息封装
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{

        //附加参数
        NameValuePair[]meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", fastDFSFile.getAuthor());

        //获取TrackerService
        TrackerServer trackerServer = getTrackerServer();

        //通过TrackerService的链接信息可以获取Storage的链接信息，
        // 创建StorageClient对象的储存Storage的链接信息
        StorageClient storageClient = getStorageClient(trackerServer);

        //通过storageClient访问Storage，实现文件上传，并且获取文件上传后的储存信息
        //uploads[]
        //第一个是文件上传所存储的组名字  group1
        //第二个是上传到storage的文件名字  M00/02/44/hdfg.jpg
        String[] uploads = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        return uploads;
    }

    /**
     * 获取文件信息
     * @param groupName 文件组名
     * @param remoteFileName 文件路径名字
     * @return
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception{
        //获取TrackerService
        TrackerServer trackerServer = getTrackerServer();
        //通过trackerService获取Storage信息创建StorageClient对象储存信息
        StorageClient storageClient = getStorageClient(trackerServer);
        //获取文件信息
        return storageClient.get_file_info(groupName, remoteFileName);
    }

    /**
     *  文件下载
     */
    public static InputStream downloadFile(String groupName, String remoteFileName) throws Exception {
        //获取TrackerService
        TrackerServer trackerServer = getTrackerServer();
        //通过trackerService获取Storage信息创建StorageClient对象储存信息
        StorageClient storageClient = getStorageClient(trackerServer);

        byte[] buffer = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(buffer);
    }

    /**
     * 文件删除
     * @param groupName
     * @param remoteFileName
     * @throws Exception
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception {
        //获取TrackerService
        TrackerServer trackerServer = getTrackerServer();
        //通过trackerService获取Storage信息创建StorageClient对象储存信息
        StorageClient storageClient = getStorageClient(trackerServer);

        //删除文件
        storageClient.delete_file(groupName, remoteFileName);
    }

    /**
     * 获取storage信息
     * @return
     */
    public static StorageServer getStorages() throws Exception {
        // 创建一个TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient获取TrackerServer链接对象，并获取Storage信息
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取Storage信息
        return trackerClient.getStoreStorage(trackerServer);
    }

    /**
     * 获取storage的ip和端口信息
     * @return
     */
    public static ServerInfo[] getServiceInfo(String groupName, String remoteFileName) throws Exception {
        // 创建一个TrackerClient对象，通过TrackerClient对象访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient获取TrackerServer链接对象，并获取Storage信息
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取storage的ip和端口信息
        return trackerClient.getFetchStorages(trackerServer,groupName, remoteFileName);
    }

    /**
     *获取Tracker信息
     * @return
     */
    public static String getTrackerInfo() throws Exception {
        //获取TrackerService
        TrackerServer trackerServer = getTrackerServer();
        //Tracker的ip，http的端口
        String ip = trackerServer.getInetSocketAddress().getHostString();
        int tracker_http_port = ClientGlobal.getG_tracker_http_port();
        String url = "http://" + ip + ":" + tracker_http_port;
        return url;
    }

    public static void main(String[] args) throws Exception{
//        FileInfo file = getFile("group1", "M00/00/00/wKixO14cBhiASi5vAAIMWSLaid0061.png");
//        System.out.println(file.getSourceIpAddr());
//        System.out.println(file.getFileSize());

//        //文件下载
//        InputStream is = downloadFile("group1", "M00/00/00/wKixO14cBhiASi5vAAIMWSLaid0061.png");
//        //将文件写入到磁盘
//        FileOutputStream os = new FileOutputStream("D:/2.png");
//        //顶意一个缓存区
//        byte[] buffer = new byte[1024];
//        while (is.read(buffer)!=-1){
//            os.write(buffer);
//        }
//        os.flush();
//        os.close();
//        is.close();

        //文件删除
        deleteFile("group1", "M00/00/00/wKixO14cGTSAFFTrAAIMWSLaid0090.png");
    }

}
