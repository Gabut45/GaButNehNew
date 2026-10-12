package santacoder.luckywheel.model;

import android.graphics.Bitmap;

public class LuckyItem {
    public int id;
    public String text;
    public int icon;
    public int bgColor;

    public LuckyItem(int id, String text, int icon, int bgColor) {
        this.id = id;
        this.text = text;
        this.icon = icon;
        this.bgColor = bgColor;
    }

}
