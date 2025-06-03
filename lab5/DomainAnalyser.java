import java.util.Scanner;
import java.net.InetAddress;

public class DomainAnalyser {
    public static void main(String[] args) {
        String domain = null;
        String ip = null;
        Scanner sc = new Scanner(System.in);
        System.out.print("请输入域名: ");
        if (sc.hasNextLine()) {
            domain = sc.nextLine();
        }
        sc.close();

        try {
            InetAddress address = InetAddress.getByName(domain);
            ip = address.getHostAddress();
            System.out.println("域名: " + domain);
            System.out.println("IP地址: " + ip);
        } catch (java.net.UnknownHostException e) {
            System.out.println("无法解析域名: " + domain);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}