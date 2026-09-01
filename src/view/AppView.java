package view;

import controller.AppController;
import util.CommandLine;
import java.util.Scanner;

public class AppView {
    private final AppController appController;

    public AppView(AppController appController) {
        this.appController = appController;
    }

    public void start() {
        System.out.println("به بازی Plants vs Zombies 2 (نسخه‌ی درسی) خوش آمدید.");
        System.out.println("برای شروع، ثبت‌نام کنید: register -u <username> -p <pass> <pass_confirm> -n <nickname> -e <email> -g <gender>");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) {
                continue;
            }
            if (line.trim().equalsIgnoreCase("exit")) {
                appController.exitApp();
                break;
            }
            CommandLine cmd = new CommandLine(line);
            appController.dispatch(line, cmd);
        }
        scanner.close();
    }
}