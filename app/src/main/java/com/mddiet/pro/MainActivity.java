package com.mddiet.pro;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private SharedPreferences sp;
    private LinearLayout root, mealsBox, loggedFoodsBox, weeklyBox;
    private TextView targetTv, eatenTv, freeTv, remainingTv, latestWeightTv, resultTv, planTitleTv;
    private WeightChartView chart;
    private EditText nameEt, ageEt, heightEt, weightEt, targetEt, tBreakfastEt, tSnackEt, tLunchEt, tDinnerEt;
    private Spinner sexSp, activitySp, goalSp, foodSp, weeklyDaySp;
    private EditText customFoodEt, qtyEt, gramsEt, cal100Et;
    private Switch freeMealSwitch, waterSwitch;
    private int calories = 1850;
    private int today;

    private final int green = Color.rgb(31,122,90);
    private final int bg = Color.rgb(244,247,245);
    private final int border = Color.rgb(220,228,224);

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }

    private GradientDrawable rounded(int color, int radius, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if (stroke != 0) g.setStroke(dp(1), stroke);
        return g;
    }

    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(28,35,32));
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.RIGHT);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setPadding(0,dp(4),0,dp(4));
        return t;
    }

    private EditText input(String value, boolean number) {
        EditText e = new EditText(this);
        e.setText(value);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setGravity(Gravity.RIGHT);
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        e.setPadding(dp(12),dp(10),dp(12),dp(10));
        e.setBackground(rounded(Color.WHITE,10,border));
        if (number) e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return e;
    }

    private Spinner spinner(String[] items) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        s.setAdapter(ad);
        return s;
    }

    private View field(String label, View v) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        TextView l = text(label,13,false);
        l.setTextColor(Color.GRAY);
        box.addView(l);
        box.addView(v);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,0,0,dp(10));
        box.setLayoutParams(lp);
        return box;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16),dp(14),dp(16),dp(14));
        c.setBackground(rounded(Color.WHITE,18,border));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,0,0,dp(14));
        c.setLayoutParams(lp);
        return c;
    }

    private Button button(String s, boolean primary) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(primary ? Color.WHITE : Color.rgb(28,35,32));
        b.setBackground(rounded(primary ? green : Color.rgb(235,242,239),12,0));
        return b;
    }

    private String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("md_diet", MODE_PRIVATE);
        today = MealData.dayIndex(Calendar.getInstance());
        NotificationScheduler.createChannels(this);

        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(18),dp(14),dp(30));
        root.setBackgroundColor(bg);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(root);

        buildHeader();
        buildDashboard();
        buildTodayMeals();
        buildWeeklyPlan();
        buildFamilyMenu();
        buildShoppingPrep();
        buildManualFood();
        buildProfile();
        buildNotifications();
        buildWeight();
        buildSafety();

        setContentView(scroll);
        loadProfile();
        calculate(false);
        renderMeals();
        renderWeeklyPlan();
        renderFamilyMenu();
        renderLoggedFoods();
        renderWeight();
        requestNotificationPermission();
        NotificationScheduler.scheduleAll(this);
    }

    private void buildHeader() {
        LinearLayout c = card();
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.md_designer_logo);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(180)));
        c.addView(logo);
        TextView title = text("MD&SA Challenge Diet V3.3", 24, true);
        title.setTextColor(green);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        c.addView(title);
        TextView sub = text("خطة + متابعة + تسجيل أكلك الحقيقي", 14, false);
        sub.setGravity(Gravity.CENTER_HORIZONTAL);
        c.addView(sub);
        root.addView(c);
    }

    private void buildDashboard() {
        LinearLayout c = card();
        c.addView(text("ملخص اليوم", 19, true));
        targetTv = text("",16,true);
        eatenTv = text("",16,false);
        freeTv = text("",16,false);
        remainingTv = text("",18,true); remainingTv.setTextColor(green);
        latestWeightTv = text("آخر وزن: —",14,false);
        c.addView(targetTv);
        c.addView(eatenTv);
        c.addView(freeTv);
        c.addView(remainingTv);
        c.addView(latestWeightTv);
        root.addView(c);
    }

    private void buildTodayMeals() {
        LinearLayout c = card();
        planTitleTv = text("برنامج اليوم", 20, true);
        c.addView(planTitleTv);
        mealsBox = new LinearLayout(this);
        mealsBox.setOrientation(LinearLayout.VERTICAL);
        c.addView(mealsBox);
        root.addView(c);
    }


private void buildWeeklyPlan() {
    LinearLayout c = card();
    c.addView(text("خطة الأسبوع — 7 أيام", 20, true));
    c.addView(text("اختَر اليوم وشوف الوجبة الأساسية والبدائل والكميات التقريبية حسب هدفك.", 13, false));
    weeklyDaySp = spinner(new String[]{"الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت","الأحد"});
    weeklyDaySp.setSelection(today);
    c.addView(field("اليوم", weeklyDaySp));
    weeklyBox = new LinearLayout(this);
    weeklyBox.setOrientation(LinearLayout.VERTICAL);
    c.addView(weeklyBox);
    weeklyDaySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { renderWeeklyPlan(); }
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    });
    root.addView(c);
}

private void renderWeeklyPlan() {
    if (weeklyBox == null || weeklyDaySp == null) return;
    int d = weeklyDaySp.getSelectedItemPosition();
    weeklyBox.removeAllViews();
    for (int m=0; m<4; m++) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12),dp(10),dp(12),dp(10));
        box.setBackground(rounded(Color.rgb(249,251,250),14,border));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,dp(5),0,dp(5));
        box.setLayoutParams(lp);

        TextView h = text(WeeklyPlanData.mealName(m), 17, true);
        h.setTextColor(green);
        box.addView(h);
        box.addView(text("الأساسي: " + WeeklyPlanData.meal(d,m), 15, false));
        box.addView(text(WeeklyPlanData.quantityHint(m, calories), 13, false));
        TextView alt = text("بدائل: " + WeeklyPlanData.alternatives(d,m), 13, false);
        alt.setTextColor(Color.DKGRAY);
        box.addView(alt);
        weeklyBox.addView(box);
    }
}

private void buildFamilyMenu() {
    LinearLayout c = card();
    c.addView(text("Family Menu — للعيلة", 20, true));
    c.addView(text("هيدا القسم منفصل عن حساب كالوري الدايت. أفكار غداء وعشاء للعيلة والأولاد.", 13, false));
    familyDaySp = spinner(new String[]{"الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت","الأحد"});
    familyDaySp.setSelection(today);
    c.addView(field("اليوم", familyDaySp));
    familyBox = new LinearLayout(this);
    familyBox.setOrientation(LinearLayout.VERTICAL);
    c.addView(familyBox);
    familyDaySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { renderFamilyMenu(); }
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    });
    root.addView(c);
}

private void renderFamilyMenu() {
    if (familyBox == null || familyDaySp == null) return;
    int d = familyDaySp.getSelectedItemPosition();
    familyBox.removeAllViews();
    LinearLayout box = new LinearLayout(this);
    box.setOrientation(LinearLayout.VERTICAL);
    box.setPadding(dp(12),dp(12),dp(12),dp(12));
    box.setBackground(rounded(Color.rgb(249,251,250),14,border));
    TextView lunch = text("غداء العيلة", 17, true); lunch.setTextColor(green); box.addView(lunch);
    box.addView(text(WeeklyPlanData.familyLunch(d), 15, false));
    TextView dinner = text("عشاء خفيف / فكرة ثانية", 17, true); dinner.setTextColor(green); dinner.setPadding(0,dp(10),0,dp(4)); box.addView(dinner);
    box.addView(text(WeeklyPlanData.familyDinner(d), 15, false));
    TextView note = text("للطفل الصغير: قدّم الأكل بقطع صغيرة مناسبة، وتجنّب المكسرات الكاملة والفشار بسبب خطر الاختناق.", 12, false);
    note.setTextColor(Color.DKGRAY); note.setPadding(0,dp(10),0,0); box.addView(note);
    familyBox.addView(box);
}

private void buildShoppingPrep() {
    LinearLayout c = card();
    c.addView(text("Friday Shopping List — Diet + Family", 20, true));
    TextView shopping = text(WeeklyPlanData.unifiedShoppingList(), 14, false);
    c.addView(shopping);
    TextView prepTitle = text("Friday Meal Prep", 19, true);
    prepTitle.setPadding(0,dp(16),0,dp(4));
    c.addView(prepTitle);
    TextView prep = text(WeeklyPlanData.fridayPrep(), 14, false);
    c.addView(prep);
    root.addView(c);
}

    private void buildManualFood() {
        LinearLayout c = card();
        c.addView(text("شو أكلت اليوم؟", 19, true));
        foodSp = spinner(FoodCatalog.items());
        c.addView(field("اختَر أكلة أو Custom", foodSp));

        customFoodEt = input("", false);
        customFoodEt.setHint("اسم الأكلة إذا كانت Custom");
        c.addView(field("اسم الأكلة المخصصة", customFoodEt));

        qtyEt = input("1", false);
        qtyEt.setHint("مثال: 1 صحن أو 2 قطعة");
        c.addView(field("الكمية / الوصف", qtyEt));

        gramsEt = input("100", true);
        gramsEt.setHint("الوزن بالغرام");
        c.addView(field("الوزن (غرام)", gramsEt));

        cal100Et = input("100", true);
        cal100Et.setHint("سعرات كل 100غ");
        c.addView(field("سعرات 100غ", cal100Et));

        freeMealSwitch = new Switch(this);
        freeMealSwitch.setText("اعتبر هيدا Free Meal / ما تحسبها على الهدف");
        freeMealSwitch.setTextDirection(View.TEXT_DIRECTION_RTL);
        c.addView(freeMealSwitch);

        foodSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) foodSp.getSelectedItem();
                boolean custom = position == 0;
                customFoodEt.setEnabled(custom);
                if (!custom) {
                    customFoodEt.setText(item);
                    cal100Et.setText(String.valueOf(FoodCatalog.caloriesPer100(item)));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button addBtn = button("أضف الأكلة", true);
        Button clearBtn = button("مسح سجل اليوم", false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(2);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1);
        lp.setMargins(dp(4),dp(4),dp(4),dp(4));
        addBtn.setLayoutParams(lp); clearBtn.setLayoutParams(lp);
        row.addView(addBtn); row.addView(clearBtn); c.addView(row);

        loggedFoodsBox = new LinearLayout(this);
        loggedFoodsBox.setOrientation(LinearLayout.VERTICAL);
        c.addView(loggedFoodsBox);

        addBtn.setOnClickListener(v -> addFoodEntry());
        clearBtn.setOnClickListener(v -> {
            sp.edit().remove("foods_" + todayKey()).apply();
            renderLoggedFoods();
        });

        root.addView(c);
    }

    private void buildProfile() {
        LinearLayout c = card();
        c.addView(text("بياناتك وحساب الهدف", 19, true));
        nameEt = input("محمد", false); c.addView(field("الاسم", nameEt));
        ageEt = input("47", true); c.addView(field("العمر (18+)", ageEt));
        sexSp = spinner(new String[]{"رجل","امرأة"}); c.addView(field("الجنس", sexSp));
        heightEt = input("180", true); c.addView(field("الطول (سم)", heightEt));
        weightEt = input("105", true); c.addView(field("الوزن الحالي (كغ)", weightEt));
        targetEt = input("90", true); c.addView(field("الوزن الهدف (كغ)", targetEt));
        activitySp = spinner(new String[]{"قليل جداً / عمل مكتبي","خفيف","متوسط","عالٍ"}); c.addView(field("النشاط", activitySp));
        goalSp = spinner(new String[]{"تنزيل وزن","تثبيت وزن","زيادة وزن"}); c.addView(field("الهدف", goalSp));
        Button calcBtn = button("احسب واحفظ", true);
        calcBtn.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(48)));
        c.addView(calcBtn);
        resultTv = text("", 14, false);
        resultTv.setPadding(0,dp(10),0,0);
        c.addView(resultTv);
        calcBtn.setOnClickListener(v -> { calculate(true); renderMeals(); renderWeeklyPlan(); renderFamilyMenu(); renderLoggedFoods(); });
        root.addView(c);
    }

    private void buildNotifications() {
        LinearLayout c = card();
        c.addView(text("أوقات التنبيهات", 19, true));
        tBreakfastEt = input("08:00", false); c.addView(field("الفطور", tBreakfastEt));
        tSnackEt = input("13:00", false); c.addView(field("السناك", tSnackEt));
        tLunchEt = input("17:30", false); c.addView(field("الغداء", tLunchEt));
        tDinnerEt = input("20:30", false); c.addView(field("العشاء", tDinnerEt));
        waterSwitch = new Switch(this);
        waterSwitch.setText("تذكير ماء كل ساعتين تقريباً");
        waterSwitch.setTextDirection(View.TEXT_DIRECTION_RTL);
        c.addView(waterSwitch);
        Button saveBtn = button("حفظ الأوقات وتفعيل الإشعارات", true);
        saveBtn.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(48)));
        c.addView(saveBtn);
        saveBtn.setOnClickListener(v -> {
            saveTimes();
            sp.edit().putBoolean("water_enabled", waterSwitch.isChecked()).apply();
            NotificationScheduler.scheduleAll(this);
            Toast.makeText(this, "تم حفظ الأوقات والتذكيرات", Toast.LENGTH_SHORT).show();
        });
        root.addView(c);
    }

    private void buildWeight() {
        LinearLayout c = card();
        c.addView(text("متابعة الوزن", 19, true));
        EditText addWeightEt = input("", true);
        addWeightEt.setHint("مثلاً 104.5");
        c.addView(field("أدخل وزن اليوم (كغ)", addWeightEt));
        Button addBtn = button("سجّل الوزن", true);
        addBtn.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(48)));
        c.addView(addBtn);
        chart = new WeightChartView(this);
        c.addView(chart, new LinearLayout.LayoutParams(-1, dp(190)));
        addBtn.setOnClickListener(v -> {
            try {
                float val = Float.parseFloat(addWeightEt.getText().toString().trim());
                if (val < 35 || val > 300) throw new Exception();
                addWeight(val);
                addWeightEt.setText("");
                renderWeight();
            } catch (Exception e) {
                Toast.makeText(this, "أدخل وزن صحيح", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(c);
    }

    private void buildSafety() {
        LinearLayout c = card();
        c.addView(text("ملاحظة صحية", 18, true));
        TextView t = text("التطبيق مخصص للبالغين 18+ ويعطي تقديرات عامة فقط. إذا عندك سكري، مرض كلوي أو كبدي، مشاكل قلبية، أدوية تؤثر على الوزن، اضطراب أكل، حمل أو رضاعة، الأفضل مراجعة طبيب أو اختصاصي تغذية.", 13, false);
        t.setTextColor(Color.rgb(105,78,25));
        c.addView(t);
        root.addView(c);
    }

    private void calculate(boolean save) {
        try {
            int a = (int) Double.parseDouble(ageEt.getText().toString().trim());
            double h = Double.parseDouble(heightEt.getText().toString().trim());
            double w = Double.parseDouble(weightEt.getText().toString().trim());
            double tg = Double.parseDouble(targetEt.getText().toString().trim());
            if (a < 18 || a > 100 || h < 130 || h > 220 || w < 35 || w > 300) {
                resultTv.setText("تأكد من البيانات. التطبيق للبالغين 18+ فقط.");
                return;
            }
            boolean male = sexSp.getSelectedItemPosition() == 0;
            double bmr = 10*w + 6.25*h - 5*a + (male ? 5 : -161);
            double[] acts = {1.2, 1.375, 1.55, 1.725};
            double tdee = bmr * acts[activitySp.getSelectedItemPosition()];
            int g = goalSp.getSelectedItemPosition();
            double cal = tdee;
            if (g == 0) cal = tdee * 0.82;
            if (g == 2) cal = tdee * 1.10;
            int min = male ? 1500 : 1200;
            calories = (int)Math.round(Math.max(min, cal));
            double bmi = w / Math.pow(h/100.0, 2);
            double ref = Math.min(w, Math.max(tg, h*0.42));
            int protein = (int)Math.round(Math.max(1.2*ref, Math.min(1.7*ref,180)));
            int fat = (int)Math.round((calories*0.28)/9.0);
            int carbs = Math.max(0, (int)Math.round((calories - protein*4 - fat*9)/4.0));
            double water = Math.min(4.0, Math.max(1.8, w*0.03));
            resultTv.setText(
                    "هدف السعرات: " + calories + " سعرة\n" +
                    "احتياج المحافظة: " + Math.round(tdee) + " سعرة\n" +
                    "BMI تقريبي: " + String.format(Locale.US, "%.1f", bmi) + "\n" +
                    "بروتين: " + protein + "غ   كارب: " + carbs + "غ   دهون: " + fat + "غ\n" +
                    "ماء تقريبي: " + String.format(Locale.US, "%.1f", water) + " لتر"
            );
            sp.edit().putInt("calories", calories).apply();
            if (save) {
                saveProfile();
                Toast.makeText(this, "تم حفظ الخطة", Toast.LENGTH_SHORT).show();
            }
            updateSummary();
        } catch (Exception e) {
            resultTv.setText("تأكد من إدخال كل الأرقام بشكل صحيح.");
        }
    }

    private void renderMeals() {
        today = MealData.dayIndex(Calendar.getInstance());
        planTitleTv.setText("برنامج اليوم — " + MealData.dayName(today));
        mealsBox.removeAllViews();
        for (int meal = 0; meal < 4; meal++) {
            final int m = meal;
            int swap = sp.getInt("swap_" + today + "_" + m, -1);
            boolean done = sp.getBoolean(doneKey(today, m), false);

            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(dp(12),dp(12),dp(12),dp(12));
            box.setBackground(rounded(Color.rgb(249,251,250),14,border));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1,-2);
            blp.setMargins(0,dp(6),0,dp(6));
            box.setLayoutParams(blp);

            TextView title = text(MealData.mealName(m) + " — " + MealData.mealCalories(calories, m) + " سعرة تقريباً", 17, true);
            title.setTextColor(green);
            box.addView(title);
            box.addView(text(MealData.portionText(today, m, calories, swap), 15, false));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(2);
            Button eaten = button(done ? "✓ أكلت" : "أكلت الوجبة", done);
            Button swapBtn = button("بدّل الوجبة", false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
            lp.setMargins(dp(4),dp(4),dp(4),dp(4));
            eaten.setLayoutParams(lp); swapBtn.setLayoutParams(lp);
            row.addView(eaten); row.addView(swapBtn); box.addView(row);

            eaten.setOnClickListener(v -> {
                boolean current = sp.getBoolean(doneKey(today, m), false);
                sp.edit().putBoolean(doneKey(today, m), !current).apply();
                renderMeals();
            });
            swapBtn.setOnClickListener(v -> {
                int cur = sp.getInt("swap_" + today + "_" + m, -1);
                sp.edit().putInt("swap_" + today + "_" + m, cur + 1).apply();
                renderMeals();
            });
            mealsBox.addView(box);
        }
        updateSummary();
    }

    private void addFoodEntry() {
        try {
            String selected = (String) foodSp.getSelectedItem();
            String name = foodSp.getSelectedItemPosition() == 0 ? customFoodEt.getText().toString().trim() : selected;
            if (name.isEmpty()) name = "أكلة بدون اسم";
            String qty = qtyEt.getText().toString().trim();
            if (qty.isEmpty()) qty = "1";
            double grams = Double.parseDouble(gramsEt.getText().toString().trim());
            double cal100 = Double.parseDouble(cal100Et.getText().toString().trim());
            boolean free = freeMealSwitch.isChecked();
            int totalCal = (int)Math.round((grams * cal100) / 100.0);
            String row = clean(name) + "|" + clean(qty) + "|" + grams + "|" + cal100 + "|" + totalCal + "|" + (free ? 1 : 0);
            String key = "foods_" + todayKey();
            String old = sp.getString(key, "");
            String combined = old.isEmpty() ? row : old + ";;" + row;
            sp.edit().putString(key, combined).apply();

            qtyEt.setText("1");
            gramsEt.setText("100");
            freeMealSwitch.setChecked(false);
            renderLoggedFoods();
            Toast.makeText(this, "تمت إضافة الأكلة", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "تأكد من الاسم والوزن وسعرات 100غ", Toast.LENGTH_SHORT).show();
        }
    }

    private String clean(String s) {
        return s.replace("|", " ").replace(";;", " ");
    }

    private void renderLoggedFoods() {
        loggedFoodsBox.removeAllViews();
        String key = "foods_" + todayKey();
        String raw = sp.getString(key, "");
        if (raw.isEmpty()) {
            loggedFoodsBox.addView(text("ما في أكل مسجل اليوم بعد.", 14, false));
            updateSummary();
            return;
        }
        String[] rows = raw.split(";;");
        for (String row : rows) {
            String[] p = row.split("\\|");
            if (p.length < 6) continue;
            String name = p[0];
            String qty = p[1];
            String grams = p[2];
            String cal100 = p[3];
            String total = p[4];
            boolean free = "1".equals(p[5]);
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(dp(10),dp(10),dp(10),dp(10));
            item.setBackground(rounded(Color.rgb(250,250,250),12,border));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
            lp.setMargins(0,dp(5),0,dp(5));
            item.setLayoutParams(lp);
            TextView t = text(name + (free ? "  (Free Meal)" : ""), 16, true);
            item.addView(t);
            item.addView(text("الكمية: " + qty + "   |   الوزن: " + grams + "غ   |   سعرات 100غ: " + cal100, 14, false));
            item.addView(text("السعرات المحسوبة: " + total + " سعرة", 14, false));
            loggedFoodsBox.addView(item);
        }
        updateSummary();
    }

    private void updateSummary() {
        int eaten = 0;
        int free = 0;
        String raw = sp.getString("foods_" + todayKey(), "");
        if (!raw.isEmpty()) {
            String[] rows = raw.split(";;");
            for (String row : rows) {
                String[] p = row.split("\\|");
                if (p.length < 6) continue;
                int total;
                try { total = (int)Math.round(Double.parseDouble(p[4])); } catch (Exception e) { total = 0; }
                boolean isFree = "1".equals(p[5]);
                if (isFree) free += total; else eaten += total;
            }
        }
        targetTv.setText("هدف اليوم: " + calories + " سعرة");
        eatenTv.setText("المأكول المحسوب: " + eaten + " سعرة");
        freeTv.setText("Free Meal Calories: " + free + " سعرة");
        remainingTv.setText("المتبقي من الهدف: " + Math.max(0, calories - eaten) + " سعرة");
    }

    private String doneKey(int d, int m) {
        return "done_" + todayKey() + "_" + d + "_" + m;
    }

    private void saveProfile() {
        sp.edit()
                .putString("name", nameEt.getText().toString())
                .putString("age", ageEt.getText().toString())
                .putInt("sex", sexSp.getSelectedItemPosition())
                .putString("height", heightEt.getText().toString())
                .putString("weight", weightEt.getText().toString())
                .putString("target", targetEt.getText().toString())
                .putInt("activity", activitySp.getSelectedItemPosition())
                .putInt("goal", goalSp.getSelectedItemPosition())
                .putInt("calories", calories)
                .apply();
    }

    private void saveTimes() {
        sp.edit()
                .putString("time_breakfast", tBreakfastEt.getText().toString().trim())
                .putString("time_snack", tSnackEt.getText().toString().trim())
                .putString("time_lunch", tLunchEt.getText().toString().trim())
                .putString("time_dinner", tDinnerEt.getText().toString().trim())
                .apply();
    }

    private void loadProfile() {
        nameEt.setText(sp.getString("name", "محمد"));
        ageEt.setText(sp.getString("age", "47"));
        sexSp.setSelection(sp.getInt("sex", 0));
        heightEt.setText(sp.getString("height", "180"));
        weightEt.setText(sp.getString("weight", "105"));
        targetEt.setText(sp.getString("target", "90"));
        activitySp.setSelection(sp.getInt("activity", 0));
        goalSp.setSelection(sp.getInt("goal", 0));
        tBreakfastEt.setText(sp.getString("time_breakfast", "08:00"));
        tSnackEt.setText(sp.getString("time_snack", "13:00"));
        tLunchEt.setText(sp.getString("time_lunch", "17:30"));
        tDinnerEt.setText(sp.getString("time_dinner", "20:30"));
        waterSwitch.setChecked(sp.getBoolean("water_enabled", false));
        calories = sp.getInt("calories", 1850);
        cal100Et.setText("100");
    }

    private void addWeight(float value) {
        String date = todayKey();
        String raw = sp.getString("weights", "");
        LinkedHashMap<String, Float> map = new LinkedHashMap<>();
        if (!raw.isEmpty()) {
            String[] rows = raw.split(";");
            for (String r : rows) {
                String[] p = r.split(",");
                if (p.length == 2) {
                    try { map.put(p[0], Float.parseFloat(p[1])); } catch (Exception ignored) {}
                }
            }
        }
        map.put(date, value);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Float> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(e.getKey()).append(",").append(e.getValue());
        }
        sp.edit().putString("weights", sb.toString()).apply();
    }

    private void renderWeight() {
        String raw = sp.getString("weights", "");
        List<Float> vals = new ArrayList<>();
        String lastDate = "", lastVal = "";
        if (!raw.isEmpty()) {
            for (String r : raw.split(";")) {
                String[] p = r.split(",");
                if (p.length == 2) {
                    try {
                        vals.add(Float.parseFloat(p[1]));
                        lastDate = p[0];
                        lastVal = p[1];
                    } catch (Exception ignored) {}
                }
            }
        }
        chart.setValues(vals);
        latestWeightTv.setText(lastVal.isEmpty() ? "آخر وزن: —" : "آخر وزن: " + lastVal + " كغ   (" + lastDate + ")");
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 99);
        }
    }
}
