package com.mddiet.pro;

public class FoodCatalog {
    public static String[] items() {
        return new String[]{
                "مخصّص / Custom",
                "رز مطبوخ",
                "دجاج مشوي",
                "لحم مشوي",
                "بطاطا مشوية",
                "خبز عربي",
                "لبنة",
                "زبادي",
                "تفاحة",
                "موزة",
                "مكسرات",
                "شاورما دجاج",
                "فول",
                "مجدرة"
        };
    }

    public static int caloriesPer100(String name) {
        switch (name) {
            case "رز مطبوخ": return 130;
            case "دجاج مشوي": return 165;
            case "لحم مشوي": return 250;
            case "بطاطا مشوية": return 93;
            case "خبز عربي": return 275;
            case "لبنة": return 215;
            case "زبادي": return 60;
            case "تفاحة": return 52;
            case "موزة": return 89;
            case "مكسرات": return 600;
            case "شاورما دجاج": return 220;
            case "فول": return 110;
            case "مجدرة": return 160;
            default: return 100;
        }
    }
}
