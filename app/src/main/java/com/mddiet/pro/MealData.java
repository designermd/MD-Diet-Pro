package com.mddiet.pro;

import java.util.Calendar;

public class MealData {
    private static final String[][][] PLAN = new String[][][]{
        {{"فطور","2 بيض + خضرة + خبز عربي"},
         {"سناك","حبة فاكهة + لبن قليل الدسم"},
         {"غداء","دجاج مشوي + رز + سلطة كبيرة"},
         {"عشاء","لبن وخيار أو بيضة - اختياري حسب الجوع"}},
        {{"فطور","لبنة + خضرة + خبز + 5 زيتونات"},
         {"سناك","فاكهة + لوز"},
         {"غداء","كفتة أو لحمة مشوية + بطاطا + سلطة"},
         {"عشاء","تونة مصفاة + خس وخيار"}},
        {{"فطور","بيض + خضرة + خبز"},
         {"سناك","موزة صغيرة أو زبادي"},
         {"غداء","يخنة فاصوليا أو بازيلا + رز + سلطة"},
         {"عشاء","لبن وخيار أو جبنة خفيفة"}},
        {{"فطور","بيض + جبنة بيضاء + خضرة + خبز"},
         {"سناك","تفاحة + قهوة بدون سكر"},
         {"غداء","سمك مشوي + بطاطا أو رز + سلطة"},
         {"عشاء","زبادي أو تونة - اختياري"}},
        {{"فطور","فول + خضرة + خبز بكمية معتدلة"},
         {"سناك","فاكهة + مكسرات غير مملحة"},
         {"غداء","شاورما دجاج منزلية + سلطة"},
         {"عشاء","لبن أو خضرة فقط إذا في جوع"}},
        {{"فطور","بيض + لبنة + خضرة + خبز"},
         {"سناك","حبة فاكهة"},
         {"غداء","مجدرة + سلطة كبيرة + لبن، بدون خبز"},
         {"عشاء","زبادي أو بيضة - اختياري"}},
        {{"فطور","بيض + لبنة أو جبنة + خضرة + خبز"},
         {"سناك","حسب الحاجة ويمكن إلغاؤه"},
         {"غداء","غداء عائلي مضبوط: بروتين + سلطة + نوع نشويات واحد"},
         {"عشاء","خفيف أو بدون عشاء إذا ما في جوع"}}
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
        return PLAN[0][m][0];
    }

    public static String baseMeal(int day, int meal) {
        return PLAN[day][meal][1];
    }

    public static String portionNote(int day, int meal, int calories) {
        double scale = Math.max(0.75, Math.min(1.45, calories / 1850.0));
        int protein = (int)(Math.round((180 * scale)/10.0)*10);
        int rice = Math.max(4, Math.min(9, (int)Math.round(6*scale)));
        String bread = scale < .9 ? "¼–½ رغيف" : scale < 1.15 ? "½ رغيف" : "½–1 رغيف";

        String b = baseMeal(day, meal);
        String note = "";
        if (meal == 2 && (b.contains("دجاج") || b.contains("سمك") || b.contains("لحمة") || b.contains("كفتة") || b.contains("شاورما"))) {
            note += " • بروتين تقريباً " + protein + "غ";
        }
        if (meal == 2 && b.contains("رز")) note += " • رز تقريباً " + rice + " ملاعق مطبوخة";
        if (b.contains("خبز")) note += " • خبز " + bread;
        if (meal == 1 && b.contains("مكسرات")) note += " • مكسرات 25–30غ";
        return note;
    }

    public static String mealText(int day, int meal, int calories) {
        return baseMeal(day, meal) + portionNote(day, meal, calories);
    }
}
