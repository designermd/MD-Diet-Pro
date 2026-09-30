package com.mddiet.pro;

import java.util.Calendar;

public class MealData {
    private static final String[][][] PLAN = new String[][][]{
        {{"الفطور","بيض + خضرة + خبز عربي"},
         {"السناك","فاكهة + لبن قليل الدسم"},
         {"الغداء","دجاج مشوي + رز + سلطة"},
         {"العشاء","لبن وخيار أو بيضة"}},
        {{"الفطور","لبنة + خضرة + خبز + زيتون"},
         {"السناك","فاكهة + لوز"},
         {"الغداء","كفتة أو لحمة مشوية + بطاطا + سلطة"},
         {"العشاء","تونة + خس وخيار"}},
        {{"الفطور","بيض + خضرة + خبز"},
         {"السناك","موزة صغيرة أو زبادي"},
         {"الغداء","يخنة فاصوليا أو بازيلا + رز + سلطة"},
         {"العشاء","لبن وخيار أو جبنة خفيفة"}},
        {{"الفطور","بيض + جبنة بيضاء + خضرة + خبز"},
         {"السناك","تفاحة"},
         {"الغداء","سمك مشوي + بطاطا أو رز + سلطة"},
         {"العشاء","زبادي أو تونة"}},
        {{"الفطور","فول + خضرة + خبز"},
         {"السناك","فاكهة + مكسرات"},
         {"الغداء","شاورما دجاج منزلية + سلطة"},
         {"العشاء","لبن أو خضرة"}},
        {{"الفطور","بيض + لبنة + خضرة + خبز"},
         {"السناك","فاكهة"},
         {"الغداء","مجدرة + سلطة + لبن"},
         {"العشاء","زبادي أو بيضة"}},
        {{"الفطور","بيض + لبنة أو جبنة + خضرة + خبز"},
         {"السناك","حسب الحاجة"},
         {"الغداء","غداء عائلي: بروتين + سلطة + نوع نشويات واحد"},
         {"العشاء","خفيف أو بدون عشاء"}}
    };

    private static final String[][] SWAPS = new String[][]{
        {"لبنة + خضرة + خبز", "فول + خضرة + خبز", "جبنة + خضرة + خبز"},
        {"تفاحة + 10 حبات لوز", "زبادي قليل الدسم", "موزة صغيرة"},
        {"سمك مشوي + بطاطا + سلطة", "لحمة مشوية + رز + سلطة", "دجاج مشوي + بطاطا + سلطة"},
        {"تونة + خضرة", "لبن وخيار", "بيضتان + خضرة"}
    };

    public static int dayIndex(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        switch (dow) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            default: return 6;
        }
    }

    public static String dayName(int d) {
        String[] names = {"الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت","الأحد"};
        return names[Math.max(0, Math.min(6, d))];
    }

    public static String mealName(int m) {
        String[] names = {"الفطور","السناك","الغداء","العشاء"};
        return names[Math.max(0, Math.min(3, m))];
    }

    public static String baseMeal(int day, int meal) {
        return PLAN[day][meal][1];
    }

    public static String swappedMeal(int meal, int idx) {
        return SWAPS[meal][Math.floorMod(idx, SWAPS[meal].length)];
    }

    public static int mealCalories(int totalCalories, int meal) {
        double[] ratio = {0.25, 0.10, 0.45, 0.20};
        return (int)Math.round(totalCalories * ratio[meal]);
    }

    public static String portionText(int day, int meal, int calories, int swapIndex) {
        String b = swapIndex < 0 ? baseMeal(day, meal) : swappedMeal(meal, swapIndex);
        double scale = Math.max(0.75, Math.min(1.45, calories / 1850.0));
        int protein = (int)(Math.round((180 * scale)/10.0)*10);
        int rice = Math.max(4, Math.min(9, (int)Math.round(6*scale)));
        String bread = scale < .9 ? "¼–½ رغيف" : scale < 1.15 ? "½ رغيف" : "½–1 رغيف";

        StringBuilder s = new StringBuilder();
        s.append(b);
        if (meal == 2 && (b.contains("دجاج") || b.contains("سمك") || b.contains("لحمة") || b.contains("كفتة") || b.contains("شاورما"))) {
            s.append("\n").append("كمية البروتين تقريباً ").append(protein).append("غ");
        }
        if (meal == 2 && b.contains("رز")) {
            s.append("\n").append("الرز تقريباً ").append(rice).append(" ملاعق مطبوخة");
        }
        if (b.contains("خبز")) {
            s.append("\n").append("الخبز: ").append(bread);
        }
        if (meal == 1 && (b.contains("لوز") || b.contains("مكسرات"))) {
            s.append("\n").append("المكسرات: 25–30غ");
        }
        if (meal == 3) {
            s.append("\n").append("اختياري إذا ما في جوع حقيقي.");
        }
        return s.toString();
    }
}
