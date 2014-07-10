package org.cyanogenmod.theme.chooserv2;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cyanogenmod.theme.chooser.R;

public class ComponentCardView extends LinearLayout {
    public static final int CARD_FADE_DURATION = 300;

    private static final float SEMI_OPAQUE_ALPHA = 0.2f;
    private static final int BACKGROUND_SEMI_OPAQUE_ALPHA = (int) (256.0f * SEMI_OPAQUE_ALPHA);

    private TextView mLabel;

    // Expanded Padding
    int mExpandPadLeft;
    int mExpandPadTop;
    int mExpandPadRight;
    int mExpandPadBottom;

    public ComponentCardView(Context context) {
        this(context, null);
    }

    public ComponentCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComponentCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        mLabel = (TextView) findViewById(R.id.label);

        Resources r = getContext().getResources();
        mExpandPadLeft =
                (int) r.getDimension(R.dimen.card_padding_left_right) + getPaddingLeft();
        mExpandPadTop =
                (int) r.getDimension(R.dimen.card_padding_top) + getPaddingTop();
        mExpandPadRight =
                (int) r.getDimension(R.dimen.card_padding_left_right) + getPaddingRight();
        mExpandPadBottom =
                (int) r.getDimension(R.dimen.card_padding_bottom) + getPaddingBottom();
    }

    public void expand() {
        TransitionDrawable bg = (TransitionDrawable) getBackground();
        Rect paddingRect = new Rect();
        bg.getPadding(paddingRect);

        setPadding(mExpandPadLeft, mExpandPadTop, mExpandPadRight, mExpandPadBottom);

        if (mLabel != null) {
            mLabel.setVisibility(View.VISIBLE);
        }
    }

    public void animateExpand() {
        if (getBackground() instanceof TransitionDrawable) {
            TransitionDrawable background = (TransitionDrawable) getBackground();
            if (mLabel != null) {
                mLabel.setVisibility(View.VISIBLE);
                mLabel.animate().alpha(1f).setDuration(CARD_FADE_DURATION);
            }
            background.startTransition(CARD_FADE_DURATION);
        }
    }

    public void collapse() {
        if (mLabel != null) {
            mLabel.setVisibility(View.GONE);
        }
        setPadding(0, 0, 0, 0);
    }

    public void animateFadeOut() {
        if (mLabel != null) {
            mLabel.animate().alpha(0f).setDuration(CARD_FADE_DURATION);
        }
        TransitionDrawable background = (TransitionDrawable) getBackground();
        background.reverseTransition(CARD_FADE_DURATION);
    }

    /**
     * Animates the card background and the title to 20% opacity.
     */
    public void animateCardFadeOut() {
        if (mLabel != null) {
            mLabel.animate().alpha(SEMI_OPAQUE_ALPHA).setDuration(CARD_FADE_DURATION);
        }
        final ValueAnimator bgAlphaAnimator = ValueAnimator.ofObject(new IntEvaluator(), 255,
                BACKGROUND_SEMI_OPAQUE_ALPHA);
        bgAlphaAnimator.setDuration(CARD_FADE_DURATION);
        bgAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getBackground().setAlpha((Integer) animation.getAnimatedValue());
            }
        });
        bgAlphaAnimator.start();
    }

    /**
     * Animates the card background and the title back to full opacity.
     */
    public void animateCardFadeIn() {
        if (getBackground().getAlpha() > 51) return;
        if (mLabel != null) {
            mLabel.animate().alpha(1f).setDuration(CARD_FADE_DURATION);
        }
        final ValueAnimator bgAlphaAnimator = ValueAnimator.ofObject(new IntEvaluator(),
                BACKGROUND_SEMI_OPAQUE_ALPHA, 255);
        bgAlphaAnimator.setDuration(CARD_FADE_DURATION);
        bgAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getBackground().setAlpha((Integer) animation.getAnimatedValue());
            }
        });
        bgAlphaAnimator.start();
    }
}