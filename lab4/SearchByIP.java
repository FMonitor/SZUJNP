import java.net.InetAddress;

public class SearchByIP {
    public static void main(String[] args) {
        try {
            InetAddress szuAddress = InetAddress.getByName("dns.google.com");
            System.out.println("主机名对应的IP地址: " + szuAddress.getHostAddress());

            InetAddress szuIPAddress = InetAddress.getByName("8.8.8.8");
            System.out.println("IP地址对应的主机名: " + szuIPAddress.getCanonicalHostName());

             
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
