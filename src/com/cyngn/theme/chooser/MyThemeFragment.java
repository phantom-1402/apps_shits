/*
 * Copyright (C) 2014 The Cyanogen, Inc
 */
package com.cyngn.theme.chooser;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.ThemeUtils;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ThemesContract;
import android.provider.ThemesContract.PreviewColumns;
import android.provider.ThemesContract.ThemesColumns;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.cyngn.theme.util.AudioUtils;
import com.cyngn.theme.util.ThemedTypefaceHelper;
import com.cyngn.theme.util.TypefaceHelperCache;
import com.cyngn.theme.util.Utils;

import java.io.IOException;

public class MyThemeFragment extends ThemeFragment {
    private static final String TAG = MyThemeFragment.class.getSimpleName();

    private String mBaseThemePkgName;
    private String mBaseThemeName;

    static MyThemeFragment newInstance(String baseThemePkgName, String baseThemeName) {
        MyThemeFragment f = new MyThemeFragment();
        Bundle args = new Bundle();
        args.putString("pkgName", CURRENTLY_APPLIED_THEME);
        args.putString("baseThemePkgName", baseThemePkgName);
        args.putString("baseThemeName", baseThemeName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getActivity();
        ThemedTypefaceHelper helper = sTypefaceHelperCache.getHelperForTheme(context,
                getAppliedFontPackageName());
        mTypefaceNormal = helper.getTypeface(Typeface.NORMAL);
        mBaseThemePkgName = getArguments().getString("baseThemePkgName");
        mBaseThemeName = getArguments().getString("baseThemeName");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mThemeTagLayout.setAppliedTagEnabled(true);
        if (mBaseThemePkgName.equals(ThemeUtils.getDefaultThemePackageName(getActivity()))) {
            mThemeTagLayout.setDefaultTagEnabled(true);
        }
        for (String pkgName : mCurrentTheme.values()) {
            if (!pkgName.equals(mBaseThemePkgName)) {
                mThemeTagLayout.setCustomizedTagEnabled(true);
                break;
            }
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getLoaderManager().getLoader(0) != null) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri;
        String[] projection;
        switch (id) {
            case LOADER_ID_ALL:
                projection = new String[]{
                        PreviewColumns.WALLPAPER_PREVIEW,
                        PreviewColumns.STATUSBAR_BACKGROUND,
                        PreviewColumns.STATUSBAR_WIFI_ICON,
                        PreviewColumns.STATUSBAR_WIFI_COMBO_MARGIN_END,
                        PreviewColumns.STATUSBAR_BLUETOOTH_ICON,
                        PreviewColumns.STATUSBAR_SIGNAL_ICON,
                        PreviewColumns.STATUSBAR_CLOCK_TEXT_COLOR,
                        PreviewColumns.STATUSBAR_BATTERY_CIRCLE,
                        PreviewColumns.STATUSBAR_BATTERY_LANDSCAPE,
                        PreviewColumns.STATUSBAR_BATTERY_PORTRAIT,
                        PreviewColumns.NAVBAR_BACK_BUTTON,
                        PreviewColumns.NAVBAR_HOME_BUTTON,
                        PreviewColumns.NAVBAR_RECENT_BUTTON,
                        PreviewColumns.ICON_PREVIEW_1,
                        PreviewColumns.ICON_PREVIEW_2,
                        PreviewColumns.ICON_PREVIEW_3,
                        PreviewColumns.LOCK_WALLPAPER_PREVIEW,
                        PreviewColumns.STYLE_PREVIEW,
                        PreviewColumns.NAVBAR_BACKGROUND
                };
                uri = PreviewColumns.APPLIED_URI;
                return new CursorLoader(getActivity(), uri, projection, null, null, null);
            default:
                // Only LOADER_ID_ALL differs for MyThemeFragment
                return super.onCreateLoader(id, args);
        }
    }

    @Override
    protected void populateSupportedComponents(Cursor c) {
    }

    @Override
    protected Boolean shouldShowComponentCard(String component) {
        return true;
    }

    @Override
    protected void loadTitle(Cursor c) {
        mTitle.setText(mBaseThemeName);
    }

    @Override
    protected void loadWallpaper(Cursor c, boolean animate) {
        int pkgNameIdx = c.getColumnIndex(ThemesContract.ThemesColumns.PKG_NAME);
        if (pkgNameIdx > -1) {
            super.loadWallpaper(c, animate);
            return;
        }
        Drawable overlay = null;
        if (animate) {
            overlay = getOverlayDrawable(mWallpaperCard, true);
        }

        int wpIdx = c.getColumnIndex(PreviewColumns.WALLPAPER_PREVIEW);
        final Resources res = getResources();
        final Context context = getActivity();
        Drawable wp = context == null ? null :
                WallpaperManager.getInstance(context).getDrawable();
        if (wp == null) {
            Bitmap bmp = Utils.loadBitmapBlob(c, wpIdx);
            if (bmp != null) wp = new BitmapDrawable(res, bmp);
        }
        if (wp != null) {
            mWallpaper.setImageDrawable(wp);
            mWallpaperCard.setWallpaper(wp);
            setCardTitle(mWallpaperCard, mCurrentTheme.get(ThemesColumns.MODIFIES_LAUNCHER),
                    getString(R.string.wallpaper_label));
        } else {
            mWallpaperCard.setEmptyViewEnabled(true);
            setAddComponentTitle(mWallpaperCard, getString(R.string.wallpaper_label));
        }

        if (animate) {
            animateContentChange(R.id.wallpaper_card, mWallpaperCard, overlay);
        }
    }

    @Override
    protected void loadLockScreen(Cursor c, boolean animate) {
        int pkgNameIdx = c.getColumnIndex(ThemesContract.ThemesColumns.PKG_NAME);
        if (pkgNameIdx > -1) {
            super.loadLockScreen(c, animate);
            return;
        }
        Drawable overlay = null;
        if (animate) {
            overlay = getOverlayDrawable(mLockScreenCard, true);
        }

        int wpIdx = c.getColumnIndex(PreviewColumns.LOCK_WALLPAPER_PREVIEW);
        final Resources res = getResources();
        final Context context = getActivity();
        Drawable wp = context == null ? null :
                WallpaperManager.getInstance(context).getFastKeyguardDrawable();
        if (wp == null) {
            Bitmap bmp = Utils.loadBitmapBlob(c, wpIdx);
            if (bmp != null) wp = new BitmapDrawable(res, bmp);
        }
        if (wp != null) {
            mLockScreenCard.setWallpaper(wp);
        } else {
            mLockScreenCard.setEmptyViewEnabled(true);
            setAddComponentTitle(mLockScreenCard, getString(R.string.lockscreen_label));
        }

        if (animate) {
            animateContentChange(R.id.lockscreen_card, mLockScreenCard, overlay);
        }
    }

    @Override
    protected void loadFont(Cursor c, boolean animate) {
        int pkgNameIdx = c.getColumnIndex(ThemesContract.ThemesColumns.PKG_NAME);
        if (pkgNameIdx > -1) {
            super.loadFont(c, animate);
            return;
        }
        Drawable overlay = null;
        if (animate) {
            overlay = getOverlayDrawable(mFontPreview, true);
        }
        setCardTitle(mFontCard, mCurrentTheme.get(ThemesColumns.MODIFIES_FONTS),
                getString(R.string.font_label));

        TypefaceHelperCache cache = TypefaceHelperCache.getInstance();
        ThemedTypefaceHelper helper = cache.getHelperForTheme(getActivity(),
                getAppliedFontPackageName());
        mTypefaceNormal = helper.getTypeface(Typeface.NORMAL);
        mFontPreview.setTypeface(mTypefaceNormal);
        if (animate) {
            animateContentChange(R.id.font_preview_container, mFontPreview, overlay);
        }
    }

    @Override
    protected void loadAudible(int type, Cursor c, boolean animate) {
        int pkgNameIdx = c.getColumnIndex(ThemesContract.ThemesColumns.PKG_NAME);
        if (pkgNameIdx > -1) {
            super.loadAudible(type, c, animate);
            return;
        }
        View audibleContainer = null;
        ImageView playPause = null;
        String modsComponent = "";
        switch (type) {
            case RingtoneManager.TYPE_RINGTONE:
                audibleContainer = mRingtoneContainer;
                playPause = mRingtonePlayPause;
                modsComponent = ThemesColumns.MODIFIES_RINGTONES;
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
                audibleContainer = mNotificationContainer;
                playPause = mNotificationPlayPause;
                modsComponent = ThemesColumns.MODIFIES_NOTIFICATIONS;
                break;
            case RingtoneManager.TYPE_ALARM:
                audibleContainer = mAlarmContainer;
                playPause = mAlarmPlayPause;
                modsComponent = ThemesColumns.MODIFIES_ALARMS;
                break;
        }
        if (audibleContainer == null) return;

        if (playPause == null) {
            playPause =
                    (ImageView) audibleContainer.findViewById(R.id.play_pause);
        }
        TextView title = (TextView) audibleContainer.findViewById(R.id.audible_name);
        MediaPlayer mp = mMediaPlayers.get(playPause);
        if (mp == null) {
            mp = new MediaPlayer();
        }
        final Context context = getActivity();
        Uri ringtoneUri;
        try {
            ringtoneUri = AudioUtils.loadDefaultAudible(context, type, mp);
        } catch (IOException e) {
            Log.w(TAG, "Unable to load default sound ", e);
            return;
        }
        if (ringtoneUri != null) {
            title.setText(RingtoneManager.getRingtone(context, ringtoneUri).getTitle(context));
        } else {
            title.setText(getString(R.string.audible_title_none));
            playPause.setVisibility(View.INVISIBLE);
        }
        setCardTitle((ComponentCardView) audibleContainer, mCurrentTheme.get(modsComponent),
                getAudibleLabel(type));

        playPause.setTag(mp);
        mMediaPlayers.put(playPause, mp);
        playPause.setOnClickListener(mPlayPauseClickListener);
        mp.setOnCompletionListener(mPlayCompletionListener);
    }

    @Override
    public String getThemePackageName() {
        return mBaseThemePkgName;
    }

}
