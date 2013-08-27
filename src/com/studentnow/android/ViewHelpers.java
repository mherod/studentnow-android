package com.studentnow.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.view.View;

public class ViewHelpers {

	private static int mShortAnimationDuration = 700;

	public static void crossfade(final View from, final View to) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			to.setAlpha(0f);
			to.setVisibility(View.VISIBLE);
			to.animate().alpha(1f).setDuration(mShortAnimationDuration)
					.setListener(null);
			from.animate().alpha(0f).setDuration(mShortAnimationDuration)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							from.setVisibility(View.GONE);
						}
					});
		} else {
			to.setVisibility(View.VISIBLE);
			from.setVisibility(View.GONE);
		}
	}

}
