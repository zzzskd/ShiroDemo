public class JavaBeanExample {
    private String name = "Hello World";

    public String getName() {
        System.out.println("getName...");
        return name;
    }

    public void setName(String name) {
        System.out.println("setName....");
        this.name = name;
    }
}
