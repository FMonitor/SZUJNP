import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class DocReader {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream("numbers.txt")));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.replace(" ", "");
                System.out.println(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}