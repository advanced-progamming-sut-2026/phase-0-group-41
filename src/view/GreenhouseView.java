package view;

import controller.GreenhouseController;
import model.greenhouse.Greenhouse;
import model.user.User;
import util.CommandLine;

import java.util.List;

public class GreenhouseView {
    private final GreenhouseController controller;
    private final ConsoleView consoleView;

    public GreenhouseView(GreenhouseController controller, ConsoleView consoleView) {
        this.controller = controller;
        this.consoleView = consoleView;
    }

    public boolean checkCommand(User user, List<String> t, CommandLine cmd) {
        if (t.isEmpty()) return false;
        String first = t.get(0).toLowerCase();

        if (first.equals("show")) {
            showGreenhouse(user);
            return true;
        }
        
        if (first.equals("unlock") && t.size() >= 2 && t.get(1).equals("pot")) {
            int[] loc = parseLocation(cmd);
            if (loc == null) {
                consoleView.printError("فرمت مختصات اشتباه است.");
                return true;
            }
            String result = controller.unlockPot(user, loc[1], loc[0]);
            handleResult(result, "گلدان جدید با موفقیت باز شد!");
            return true;
        }

        if (first.equals("plant")) {
            int[] loc = parseLocation(cmd);
            if (loc == null) {
                consoleView.printError("فرمت مختصات اشتباه است.");
                return true;
            }
            String result = controller.plant(user, loc[1], loc[0]);
            if (result.startsWith("SUCCESS_")) {
                consoleView.printMessage("گیاه " + result.split("_")[1] + " با موفقیت کاشته شد.");
            } else {
                handleResult(result, "");
            }
            return true;
        }

        if (first.equals("harvest")) {
            int[] loc = parseLocation(cmd);
            if (loc == null) return true;
            String result = controller.harvest(user, loc[1], loc[0]);
            if (result.startsWith("SUCCESS_")) {
                consoleView.printMessage("گیاه " + result.split("_")[1] + " برداشت شد! پاداش: ۱ بذر + ۱۰۰ سکه.");
            } else {
                handleResult(result, "");
            }
            return true;
        }

        if (first.equals("accelerate")) {
            int[] loc = parseLocation(cmd);
            if (loc == null) return true;
            String result = controller.accelerate(user, loc[1], loc[0]);
            handleResult(result, "رشد گیاه با پرداخت ۲ الماس سریعاً کامل شد!");
            return true;
        }

        return false; // دستور نامعتبر
    }

    private void showGreenhouse(User user) {
        Greenhouse gh = user.getGreenhouse();
        consoleView.printMessage("\n=== وضعیت گلخانه ===");
        consoleView.printMessage("موجودی گلدان در انبار: " + user.getPendingGreenhousePots());
        for (int r = 0; r < Greenhouse.ROWS; r++) {
            StringBuilder rowStr = new StringBuilder("ردیف " + r + ": ");
            for (int c = 0; c < Greenhouse.COLS; c++) {
                rowStr.append(String.format("%-15s", "[" + gh.getDisplayStatus(r, c) + "]"));
            }
            consoleView.printMessage(rowStr.toString());
        }
        consoleView.printMessage("====================\n");
    }

    private void handleResult(String result, String successMsg) {
        switch (result) {
            case "SUCCESS": consoleView.printMessage(successMsg); break;
            case "ERR_INVALID_COORD": consoleView.printError("مختصات خارج از محدوده گلخانه است."); break;
            case "ERR_ALREADY_UNLOCKED": consoleView.printError("این مکان از قبل باز است."); break;
            case "ERR_NO_POTS": consoleView.printError("شما هیچ گلدانی در انبار ندارید (از فروشگاه بخرید)."); break;
            case "ERR_LOCKED": consoleView.printError("این مکان قفل است. ابتدا باید آن را باز کنید."); break;
            case "ERR_NOT_EMPTY": consoleView.printError("این گلدان پر است."); break;
            case "ERR_EMPTY": consoleView.printError("گلدانی در این مختصات کاشته نشده است."); break;
            case "ERR_NOT_READY": consoleView.printError("گیاه هنوز به طور کامل رشد نکرده است."); break;
            case "ERR_ALREADY_READY": consoleView.printError("گیاه از قبل رشد کرده و آماده برداشت است."); break;
            case "ERR_NOT_ENOUGH_DIAMONDS": consoleView.printError("الماس کافی برای این کار ندارید."); break;
        }
    }

    private int[] parseLocation(CommandLine cmd) {
        List<String> values = cmd.getMulti("l");
        if (values.isEmpty()) return null;
        String joined = String.join(" ", values).replace("(", "").replace(")", "").replace(",", " ").trim();
        String[] parts = joined.split("\\s+");
        if (parts.length < 2) return null;
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}