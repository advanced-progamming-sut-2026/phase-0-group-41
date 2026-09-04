package gdx.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class AnimatedEntity {

    private final String pamPath;
    private String clipName;
    private float stateTime;
    private float x, y;
    private boolean flipped;
    private final Color tint = new Color(Color.WHITE);

    // اندازه‌ی هدفِ رسمِ این موجودیت (بر حسب واحد جهانِ بازی)، برای مقیاس‌دهیِ
    // خروجیِ PamPlayer که خودش پارامتر عرض/ارتفاع نمی‌گیرد و اسپرایتِ اصلی/
    // خام (اندازه‌ی واقعیِ اتلس، مثلاً صدها پیکسل) را رسم می‌کند — دقیقاً همان
    // چیزی که باعث می‌شد زامبی‌ها به‌نسبتِ کاشی/گیاه‌ها بیش‌ازحد بزرگ به‌نظر
    // برسند. وقتی targetWidth/targetHeight صفر باشد (پیش‌فرض)، رفتار قبلی
    // (بدون مقیاس‌دهی) حفظ می‌شود.
    private float targetWidth = 0f;
    private float targetHeight = 0f;
    // اندازه‌ی طبیعیِ اسپرایت در مقیاس ۱:۱ (برای محاسبه‌ی ضریب مقیاس)؛ اگر
    // صفر بماند، مقیاس‌دهی انجام نمی‌شود چون اندازه‌ی مرجعی برای مقایسه نیست.
    private float nativeWidth = 0f;
    private float nativeHeight = 0f;

    public AnimatedEntity(String pamPath, String clipName, float x, float y) {
        this.pamPath = pamPath;
        this.clipName = clipName;
        this.x = x;
        this.y = y;
    }

    public void update(float delta) { stateTime += delta; }

    /**
     * اندازه‌ی هدف (بر حسب واحد جهانِ بازی، مثلاً عرض/ارتفاع یک کاشی) و
     * اندازه‌ی طبیعیِ شناخته‌شده‌ی اسپرایت (بر حسب پیکسل خام اتلس) را تنظیم
     * می‌کند تا draw() بتواند خروجیِ PamPlayer را با نسبت ابعاد درست کوچک/
     * بزرگ کند. صدا زدنِ این متد اختیاری است؛ بدون آن، رفتار قبلی (بدون
     * مقیاس) حفظ می‌شود.
     */
    public void setTargetSize(float targetWidth, float targetHeight, float nativeWidth, float nativeHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.nativeWidth = nativeWidth;
        this.nativeHeight = nativeHeight;
    }

    public void draw(SpriteBatch batch) {
        batch.setColor(tint);
        boolean scaling = targetWidth > 0f && targetHeight > 0f && nativeWidth > 0f && nativeHeight > 0f;
        if (!scaling) {
            PamAssets.get().getPamPlayer().draw(batch, pamPath, clipName, stateTime, x, y, flipped);
            batch.setColor(Color.WHITE);
            return;
        }

        // نسبت ابعاد اصلی اسپرایت حفظ می‌شود (مثل drawFitted برای گیاهان)؛
        // مقیاس بر اساس کوچک‌ترین نسبتِ عرض/ارتفاع هدف به اندازه‌ی طبیعی
        // محاسبه می‌شود تا تصویر بدون کش‌آمدگی، داخل جعبه‌ی هدف جا شود.
        float scale = Math.min(targetWidth / nativeWidth, targetHeight / nativeHeight);

        Matrix4 originalTransform = batch.getTransformMatrix().cpy();
        Matrix4 scaledTransform = originalTransform.cpy()
                .translate(x, y, 0f)
                .scale(scale, scale, 1f)
                .translate(-x, -y, 0f);
        batch.setTransformMatrix(scaledTransform);
        PamAssets.get().getPamPlayer().draw(batch, pamPath, clipName, stateTime, x, y, flipped);
        batch.setTransformMatrix(originalTransform);

        batch.setColor(Color.WHITE);
    }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public float getX() { return x; }
    public float getY() { return y; }

    public String getClipName() { return clipName; }

    public void setClipName(String clipName) {
        if (!this.clipName.equals(clipName)) {
            this.clipName = clipName;
            this.stateTime = 0f;
        }
    }

    public void setFlipped(boolean flipped) { this.flipped = flipped; }
    public void setTint(Color color) { this.tint.set(color); }
    public float getStateTime() { return stateTime; }
}