import controller.AppController;


public final class Main {
    
    private Main() {// کلاس Main فقط نقطه شروع برنامه است و نیازی به ساخت شیء از آن نداریم.
    }
    
    public static void main(String[] args) {
        AppController appController = new AppController();
        appController.run();
    }
}
