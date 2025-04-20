import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetAddr {
    public static void main(String[] args) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("本地主机名: " + localHost.getHostName());
            System.out.println("本地IP地址: " + localHost.getHostAddress());
            
            InetAddress remoteHost = InetAddress.getByName("www.szu.edu.cn");
            System.out.println("\n远程主机名: " + remoteHost.getHostName());
            System.out.println("远程IP地址: " + remoteHost.getHostAddress());
            
            System.out.println("远程主机是否可达: " + remoteHost.isReachable(5000));
            
        } catch (UnknownHostException e){
            System.out.println("无法解析主机名: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}