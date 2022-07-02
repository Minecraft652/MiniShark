import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        List<String> i = new ArrayList<>();
        i.add("1");
        List<String> b = new ArrayList<>(i);
        i.clear();
        System.out.println(b.get(0));
    }
}
