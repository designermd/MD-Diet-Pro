package com.mddiet.pro;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root;
    private EditText name, age, height, weight, target;
    private Spinner sex, activity, goal;
    private TextView result, planTitle, planBody;
    private EditText tBreakfast, tSnack, tLunch, tDinner;
    private SharedPreferences sp;
    private int calories = 1850;

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }

    private TextView tv(String text, int size, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(Color.rgb(28,35,32));
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setPadding(0, dp(5), 0, dp(5));
        return v;
    }

    private EditText input(String hint, String value) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setBackgroundColor(Color.WHITE);
        return e;
    }

    private Spinner spinner(String[] items) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        s.setAdapter(ad);
        return s;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.rgb(220,228,224));
        c.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0,0,0,dp(14));
        c.setLayoutParams(lp);
        return c;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        return b;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("md_diet", MODE_PRIVATE);
        NotificationScheduler.createChannel(this);

        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(18), dp(14), dp(30));
        root.setBackgroundColor(Color.rgb(246,248,247));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(root);

        TextView title = tv("MD Diet Pro", 24, true);
        title.setTextColor(Color.rgb(31,122,90));
        root.addView(title);
        root.addView(tv("خطة غذائية مرنة + تذكير بالوجبات + حساب السعرات", 14, false));

        buildProfileCard();
        buildTimesCard();
        buildTodayCard();
        buildSafetyCard();

        setContentView(scroll);

        load();
        calculate();
        requestNotificationPermission();
        NotificationScheduler.scheduleAll(this);
    }

    private void buildProfileCard() {
        LinearLayout c = card();
        c.addView(tv("بياناتك", 19, true));

        name = input("الاسم", "محمد"); c.addView(labelWrap("الاسم", name));
        age = input("العمر", "47"); age.setInputType(2); c.addView(labelWrap("العمر (18+)", age));
        sex = spinner(new String[]{"رجل","امرأة"}); c.addView(labelWrap("الجنس", sex));
        height = input("الطول", "180"); height.setInputType(2); c.addView(labelWrap("الطول (سم)", height));
        weight = input("الوزن الحالي", "105"); weight.setInputType(8194); c.addView(labelWrap("الوزن الحالي (كغ)", weight));
        target = input("الوزن الهدف", "90"); target.setInputType(8194); c.addView(labelWrap("الوزن الهدف (كغ)", target));
        activity = spinner(new String[]{"قليل جداً / عمل مكتبي","خفيف","متوسط","عالٍ"}); c.addView(labelWrap("النشاط", activity));
        goal = spinner(new String[]{"تنزيل وزن","تثبيت وزن","زيادة وزن"}); c.addView(labelWrap("الهدف", goal));

        Button calc = button("احسب الخطة واحفظ");
        calc.setOnClickListener(v -> { calculate(); save(); NotificationScheduler.scheduleAll(this); });
        c.addView(calc);

        result = tv("", 15, false);
        result.setPadding(dp(4),dp(12),dp(4),dp(4));
        c.addView(result);
        root.addView(c);
    }

    private View labelWrap(String label, View field) {
        LinearLayout w = new LinearLayout(this);
        w.setOrientation(LinearLayout.VERTICAL);
        TextView l = tv(label,13,false);
        l.setTextColor(Color.GRAY);
        w.addView(l);
        w.addView(field);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,0,0,dp(8));
        w.setLayoutParams(lp);
        return w;
    }

    private void buildTimesCard() {
        LinearLayout c = card();
        c.addView(tv("أوقات الوجبات", 19, true));
        tBreakfast = input("08:00","08:00"); c.addView(labelWrap("الفطور",tBreakfast));
        tSnack = input("13:00","13:00"); c.addView(labelWrap("السناك",tSnack));
        tLunch = input("17:30","17:30"); c.addView(labelWrap("الغداء",tLunch));
        tDinner = input("20:30","20:30"); c.addView(labelWrap("العشاء",tDinner));

        Button b = button("حفظ الأوقات وتفعيل الإشعارات");
        b.setOnClickListener(v -> {
            saveTimes();
            NotificationScheduler.scheduleAll(this);
            Toast.makeText(this,"تم حفظ الأوقات وتفعيل التذكير",Toast.LENGTH_SHORT).show();
        });
        c.addView(b);
        root.addView(c);
    }

    private void buildTodayCard() {
        LinearLayout c = card();
        planTitle = tv("برنامج اليوم", 19, true);
        planBody = tv("",15,false);
        c.addView(planTitle);
        c.addView(planBody);
        root.addView(c);
    }

    private void buildSafetyCard() {
        LinearLayout c = card();
        c.addView(tv("ملاحظة صحية",18,true));
        TextView t = tv("التطبيق مخصص للبالغين 18+ ويعطي تقديرات عامة فقط. إذا عندك سكري، مرض كلوي/كبدي، مشاكل قلبية، أدوية تؤثر على الوزن، تاريخ اضطراب أكل، أو حمل/رضاعة، الأفضل مراجعة طبيب أو اختصاصي تغذية قبل اتباع عجز سعرات.",13,false);
        t.setTextColor(Color.rgb(110,75,25));
        c.addView(t);
        root.addView(c);
    }

    private void calculate() {
        try {
            int a = Integer.parseInt(age.getText().toString().trim());
            double h = Double.parseDouble(height.getText().toString().trim());
            double w = Double.parseDouble(weight.getText().toString().trim());
            double tg = Double.parseDouble(target.getText().toString().trim());
            if (a < 18 || a > 100 || h < 130 || h > 220 || w < 35 || w > 300) {
                result.setText("تأكد من البيانات. التطبيق مخصص لعمر 18+.");
                return;
            }

            boolean male = sex.getSelectedItemPosition()==0;
            double bmr = 10*w + 6.25*h - 5*a + (male ? 5 : -161);
            double[] acts = {1.2,1.375,1.55,1.725};
            double tdee = bmr * acts[activity.getSelectedItemPosition()];
            int g = goal.getSelectedItemPosition();
            double cal = tdee;
            if (g==0) cal = tdee*0.82;
            if (g==2) cal = tdee*1.10;

            int min = male ? 1500 : 1200;
            calories = (int)Math.round(Math.max(min,cal));
            double bmi = w/Math.pow(h/100.0,2);
            double ref = Math.min(w, Math.max(tg, h*0.42));
            int protein = (int)Math.round(Math.max(1.2*ref, Math.min(1.7*ref,180)));
            int fat = (int)Math.round((calories*0.28)/9.0);
            int carbs = Math.max(0,(int)Math.round((calories-protein*4-fat*9)/4.0));
            double water = Math.min(4.0,Math.max(1.8,w*0.03));

            result.setText(
                "BMR: " + Math.round(bmr) + " سعرة\n" +
                "احتياج المحافظة: " + Math.round(tdee) + " سعرة\n" +
                "هدفك اليومي: " + calories + " سعرة\n" +
                "BMI تقريبي: " + String.format(Locale.US,"%.1f",bmi) + "\n" +
                "بروتين: " + protein + "غ • كربوهيدرات: " + carbs + "غ • دهون: " + fat + "غ\n" +
                "ماء تقريبي: " + String.format(Locale.US,"%.1f",water) + " لتر"
            );

            sp.edit().putInt("calories", calories).apply();
            showToday();
        } catch (Exception e) {
            result.setText("تأكد من إدخال كل الأرقام بشكل صحيح.");
        }
    }

    private void showToday() {
        Calendar now = Calendar.getInstance();
        int d = MealData.dayIndex(now);
        planTitle.setText("برنامج اليوم — " + MealData.dayName(d));
        StringBuilder sb = new StringBuilder();
        for(int m=0;m<4;m++) {
            sb.append(MealData.mealName(m)).append(":\n")
              .append(MealData.mealText(d,m,calories)).append("\n\n");
        }
        sb.append("البزورات: 25–30غ فقط وبصحن صغير، مش من الكيس.");
        planBody.setText(sb.toString());
    }

    private void save() {
        sp.edit()
          .putString("name",name.getText().toString())
          .putString("age",age.getText().toString())
          .putInt("sex",sex.getSelectedItemPosition())
          .putString("height",height.getText().toString())
          .putString("weight",weight.getText().toString())
          .putString("target",target.getText().toString())
          .putInt("activity",activity.getSelectedItemPosition())
          .putInt("goal",goal.getSelectedItemPosition())
          .putInt("calories",calories)
          .apply();
    }

    private void saveTimes() {
        sp.edit()
          .putString("time_breakfast",tBreakfast.getText().toString().trim())
          .putString("time_snack",tSnack.getText().toString().trim())
          .putString("time_lunch",tLunch.getText().toString().trim())
          .putString("time_dinner",tDinner.getText().toString().trim())
          .apply();
    }

    private void load() {
        name.setText(sp.getString("name","محمد"));
        age.setText(sp.getString("age","47"));
        sex.setSelection(sp.getInt("sex",0));
        height.setText(sp.getString("height","180"));
        weight.setText(sp.getString("weight","105"));
        target.setText(sp.getString("target","90"));
        activity.setSelection(sp.getInt("activity",0));
        goal.setSelection(sp.getInt("goal",0));
        tBreakfast.setText(sp.getString("time_breakfast","08:00"));
        tSnack.setText(sp.getString("time_snack","13:00"));
        tLunch.setText(sp.getString("time_lunch","17:30"));
        tDinner.setText(sp.getString("time_dinner","20:30"));
        calories = sp.getInt("calories",1850);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 99);
        }
    }
}
