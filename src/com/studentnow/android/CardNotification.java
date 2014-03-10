package com.studentnow.android;

import org.studentnow.ECard;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Helper class for showing and cancelling card notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class CardNotification {
	/**
	 * The unique identifier for this type of notification.
	 */
	private static final String NOTIFICATION_TAG = "Card";

	/**
	 * Shows the notification, or updates a previously shown notification of
	 * this type, with the given parameters.
	 * <p>
	 * TODO: Customise this method's arguments to present relevant content in
	 * the notification.
	 * <p>
	 * TODO: Customise the contents of this method to tweak the behaviour and
	 * presentation of card notifications. Make sure to follow the <a
	 * href="https://developer.android.com/design/patterns/notifications.html">
	 * Notification design guidelines</a> when doing so.
	 * 
	 * @see #cancel(Context)
	 */
	public static void notify(final Context context, final ECard eCard,
			final Intent intent, final int number) {
		final Resources res = context.getResources();

		// This image is used as the notification's large icon (thumbnail).
		// T-ODO: Remove this if your notification has no relevant thumbnail.
		// final Bitmap picture = BitmapFactory.decodeResource(res,
		// R.drawable.example_picture);

		final String app_name = res.getString(R.string.app_name);
		final String ticker = app_name + " - " + eCard.getTitle();
		final String title = eCard.getTitle();
		final String text = eCard.getDesc();

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)

				// Set appropriate defaults for the notification light, sound,
				// and vibration.
				.setDefaults(Notification.DEFAULT_ALL)

				// Set required fields, including the small icon, the
				// notification title, and text.
				.setSmallIcon(R.drawable.ic_stat_card).setContentTitle(title)
				.setContentText(text)

				// All fields below this line are optional.

				// Use a default priority (recognized on devices running Android
				// 4.1 or later)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)

				// Provide a large icon, shown with the notification in the
				// notification drawer on devices running Android 3.0 or later.
				// .setLargeIcon(picture)

				// Set ticker text (preview) information for this notification.
				.setTicker(ticker)

				// Show a number. This is useful when stacking notifications of
				// a single type.
				.setNumber(number);

		if (eCard.getSpotlightTime() > 0) {
			builder.setWhen(eCard.getSpotlightTime());
		} else if (eCard.hasDisplayTime()) {
			builder.setWhen(eCard.getDisplayTime());
		} else if (eCard.hasNotificationTime()) {
			builder.setWhen(eCard.getNotificationTime());
		}
		if (intent != null) {
			builder.setContentIntent(PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT));
		}

		// Show expanded text content on devices running Android 4.1 or
		// later.
		builder.setStyle(
				new NotificationCompat.BigTextStyle().bigText(text)
						.setBigContentTitle(title).setSummaryText(app_name))

		// Example additional actions for this notification. These will
		// only show on devices running Android 4.1 or later, so you
		// should ensure that the activity in this notification's
		// content intent provides access to the same actions in
		// another way.
		// .addAction(
		// R.drawable.ic_action_stat_share,
		// res.getString(R.string.action_share),
		// PendingIntent.getActivity(context, 0, Intent
		// .createChooser(
		// new Intent(Intent.ACTION_SEND).setType(
		// "text/plain")
		// .putExtra(Intent.EXTRA_TEXT,
		// "Dummy text"),
		// "Dummy title"),
		// PendingIntent.FLAG_UPDATE_CURRENT))
		// .addAction(R.drawable.ic_action_stat_reply,
		// res.getString(R.string.action_reply), null)

				// Automatically dismiss the notification when it is touched.
				.setAutoCancel(true);

		notify(context, builder.build(), number);
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static void notify(final Context context,
			final Notification notification, final int number) {
		final NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			nm.notify(NOTIFICATION_TAG, 0, notification);
		} else {
			nm.notify(NOTIFICATION_TAG.hashCode(), notification);
		}
	}

	/**
	 * Cancels any notifications of this type previously shown using
	 * {@link #notify(Context, String, int)}.
	 */
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static void cancel(final Context context) {
		final NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			nm.cancel(NOTIFICATION_TAG, 0);
		} else {
			nm.cancel(NOTIFICATION_TAG.hashCode());
		}
	}
}