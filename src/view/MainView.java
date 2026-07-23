package view;

import controller.MainController;
import controller.MenuController;
import model.menu.MenuType;
import util.CommandLine;

import java.util.List;

public class MainView {
    private final MainController controller;
    private final ConsoleView consoleView;
    private final MenuController menuController;

    public MainView(MainController controller, ConsoleView consoleView, MenuController menuController) {
        this.controller = controller;
        this.consoleView = consoleView;
        this.menuController = menuController;
    }

    public boolean checkCommand(List<String> t, CommandLine cmd) {
        // ۱. بررسی دستور خروج
        if (t.size() == 2 && t.get(0).equals("menu") && t.get(1).equals("logout")) {
            doLogout();
            return true;
        }

        // ۲. بررسی دستور ورود به زیرمنوها
        if (t.size() >= 3 && t.get(0).equals("menu") && t.get(1).equals("enter")) {
            String menuName = t.get(2);
            return doEnterMenu(menuName);
        }

        return false;
    }

    private void doLogout() {
        // اطلاع به کنترلر برای ذخیره‌سازی داده‌ها
        String result = controller.logout(menuController.getLoggedInUser());

        if (result.equals("SUCCESS")) {
            // الف: پاک کردن کاربر فعال از حافظه موقت برنامه (Session)
            menuController.setLoggedInUser(null);

            // ب: هدایت به منوی ثبت‌نام (دقیقاً طبق خط آخر تصویر داکیومنت)
            menuController.setCurrentMenu(MenuType.REGISTER);
            consoleView.printMessage("شما با موفقیت خارج شدید. وارد منوی ثبت‌نام (Register Menu) شدید.");
        } else {
            consoleView.printError("شما در حساب کاربری نیستید.");
        }
    }

    private boolean doEnterMenu(String menuName) {
        // طبق لیست داکیومنت، کاربر فقط می‌تواند به این ۴ منو برود
        switch (menuName.toLowerCase()) {
            case "play":
                menuController.setCurrentMenu(MenuType.GAME);
                consoleView.printMessage("وارد منوی بازی شدید.");
                return true;
            case "settings":
                menuController.setCurrentMenu(MenuType.SETTINGS);
                consoleView.printMessage("وارد منوی تنظیمات شدید.");
                return true;
            case "news":
                menuController.setCurrentMenu(MenuType.NEWS);
                consoleView.printMessage("وارد منوی اخبار شدید.");
                return true;
            case "profile":
                menuController.setCurrentMenu(MenuType.PROFILE);
                consoleView.printMessage("وارد منوی پروفایل شدید.");
                return true;
            default:
                consoleView.printError("نام منو نامعتبر است. منوهای مجاز: play, settings, news, profile");
                return true;
        }
    }
}