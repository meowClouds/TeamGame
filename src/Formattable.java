import java.util.Map;

public interface Formattable {
    String toCSVFormat();
    String toDisplayFormat();
    Map<String, Object> toMap();
}