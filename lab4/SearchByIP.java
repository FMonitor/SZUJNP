import java.net.InetAddress;
import java.util.Scanner;

public class SearchByIP {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("请输入要查询的IP地址或主机名: ");
            String ipAddress = scanner.nextLine();
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            boolean isIP = inetAddress.getHostAddress().equals(ipAddress);
            if (isIP) {
                System.out.println("IP地址对应的主机名: " + inetAddress.getHostName());
            } else {
                System.out.println("主机名对应的IP地址: " + inetAddress.getHostAddress());
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
