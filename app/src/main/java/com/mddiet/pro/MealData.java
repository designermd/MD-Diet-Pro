package com.mddiet.pro;
import java.util.Calendar;
public class MealData {
  private static final String[][][] PLAN={
    {{"الفطور","بيض + خضرة + خبز عربي"},{"السناك","فاكهة + لبن قليل الدسم"},{"الغداء","دجاج مشوي + رز + سلطة"},{"العشاء","لبن وخيار أو بيضة"}},
    {{"الفطور","لبنة + خضرة + خبز + زيتون"},{"السناك","فاكهة + لوز"},{"الغداء","كفتة أو لحمة مشوية + بطاطا + سلطة"},{"العشاء","تونة + خس وخيار"}},
    {{"الفطور","بيض + خضرة + خبز"},{"السناك","موزة صغيرة أو زبادي"},{"الغداء","يخنة فاصوليا أو بازيلا + رز + سلطة"},{"العشاء","لبن وخيار أو جبنة خفيفة"}},
    {{"الفطور","بيض + جبنة بيضاء + خضرة + خبز"},{"السناك","تفاحة"},{"الغداء","سمك مشوي + بطاطا أو رز + سلطة"},{"العشاء","زبادي أو تونة"}},
    {{"الفطور","فول + خضرة + خبز"},{"السناك","فاكهة + مكسرات"},{"الغداء","شاورما دجاج منزلية + سلطة"},{"العشاء","لبن أو خضرة"}},
    {{"الفطور","بيض + لبنة + خضرة + خبز"},{"السناك","فاكهة"},{"الغداء","مجدرة + سلطة + لبن"},{"العشاء","زبادي أو بيضة"}},
    {{"الفطور","بيض + لبنة أو جبنة + خضرة + خبز"},{"السناك","حسب الحاجة"},{"الغداء","غداء عائلي: بروتين + سلطة + نوع نشويات واحد"},{"العشاء","خفيف أو بدون عشاء"}}
  };
  private static final String[][] SWAPS={
    {"لبنة + خضرة + خبز","فول + خضرة + خبز","جبنة بيضاء + خضرة + خبز"},
    {"تفاحة + 10 حبات لوز","زبادي قليل الدسم","موزة صغيرة"},
    {"سمك مشوي + بطاطا + سلطة","لحمة مشوية + رز + سلطة","دجاج مشوي + بطاطا + سلطة"},
    {"تونة + خضرة","لبن وخيار","بيضتان + خضرة"}
  };
  public static int dayIndex(Calendar c){int d=c.get(Calendar.DAY_OF_WEEK);switch(d){case Calendar.MONDAY:return 0;case Calendar.TUESDAY:return 1;case Calendar.WEDNESDAY:return 2;case Calendar.THURSDAY:return 3;case Calendar.FRIDAY:return 4;case Calendar.SATURDAY:return 5;default:return 6;}}
  public static String dayName(int d){String[] n={"الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت","الأحد"};return n[Math.max(0,Math.min(6,d))];}
  public static String mealName(int m){String[] n={"الفطور","السناك","الغداء","العشاء"};return n[Math.max(0,Math.min(3,m))];}
  public static String baseMeal(int d,int m){return PLAN[d][m][1];}
  public static String swappedMeal(int m,int i){return SWAPS[m][Math.floorMod(i,SWAPS[m].length)];}
  public static int mealCalories(int total,int m){double[] r={.25,.10,.45,.20};return (int)Math.round(total*r[m]);}
  public static String portionText(int d,int m,int cal,int swap){String b=swap<0?baseMeal(d,m):swappedMeal(m,swap);double s=Math.max(.75,Math.min(1.45,cal/1850.0));int protein=(int)(Math.round((180*s)/10.0)*10);int rice=Math.max(4,Math.min(9,(int)Math.round(6*s)));String bread=s<.9?"¼–½ رغيف":s<1.15?"½ رغيف":"½–1 رغيف";StringBuilder x=new StringBuilder(b);if(m==2&&(b.contains("دجاج")||b.contains("سمك")||b.contains("لحمة")||b.contains("كفتة")||b.contains("شاورما")))x.append("\nكمية البروتين تقريباً ").append(protein).append("غ");if(m==2&&b.contains("رز"))x.append("\nالرز تقريباً ").append(rice).append(" ملاعق مطبوخة");if(b.contains("خبز"))x.append("\nالخبز: ").append(bread);if(m==1&&(b.contains("لوز")||b.contains("مكسرات")))x.append("\nالمكسرات: 25–30غ");if(m==3)x.append("\nاختياري إذا ما في جوع حقيقي.");return x.toString();}
}
