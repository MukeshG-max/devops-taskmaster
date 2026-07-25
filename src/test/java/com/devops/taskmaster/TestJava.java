public class TestJava {
    public static void main(String[] args) {
        System.out.println("✅ Java is working perfectly!");
        System.out.println("JDK Version: " + System.getProperty("java.version"));
        
        for (int i = 1; i <= 2; i++) {
            System.out.println("Task " + i + " - Ready for Kubernetes deployment!");
        }
    }
}