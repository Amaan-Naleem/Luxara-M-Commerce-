package lk.jiat.luxara.util;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class AnimUtils {

    // Staggered slide-up + fade-in for list/grid items.
    public static void enter(View v, int position, boolean animate) {
        v.animate().cancel();
        if (!animate) {
            v.setAlpha(1f);
            v.setTranslationY(0f);
            return;
        }
        float dist = 60f * v.getResources().getDisplayMetrics().density / 2f;
        v.setAlpha(0f);
        v.setTranslationY(dist);
        v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(Math.min(position % 6, 5) * 70L)
                .setDuration(450)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
    }

    // Squash and pop, used on add-to-cart / heart buttons.
    public static void bounce(View v) {
        v.animate().cancel();
        v.animate().scaleX(0.75f).scaleY(0.75f).setDuration(90)
                .withEndAction(() -> v.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator(3f))
                        .start())
                .start();
    }

    // Subtle press-down feedback for cards / buttons.
    public static void pressEffect(View v) {
        v.setOnTouchListener((view, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(110).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().scaleX(1f).scaleY(1f).setDuration(180)
                            .setInterpolator(new OvershootInterpolator(2f)).start();
                    break;
            }
            return false;
        });
    }
}
