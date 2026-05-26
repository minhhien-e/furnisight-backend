import java.util.*;
import java.text.*;

public class TestTZ {
    public static void main(String[] args) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        
        Calendar cld1 = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        System.out.println("Etc/GMT+7: " + formatter.format(cld1.getTime()));
        
        Calendar cld2 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println("Asia/Ho_Chi_Minh: " + formatter.format(cld2.getTime()));
    }
}
