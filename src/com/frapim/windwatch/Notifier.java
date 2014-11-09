/**
* WindWatch - Wind tracking application compatible with Android Wear.
* Copyright (C) 2014  Francisco Pimenta

* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.frapim.windwatch;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.preview.support.wearable.notifications.*;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;

public class Notifier {
	private static final String ACTION_DISABLE = 
			"com.android.wear.hackaton.windtide.intent.action.DISABLE";
	private static final int NOTIFICATION_ID = 001;
	private NotificationManagerCompat mNotificationManager;
	private Context mContext;
	private int mBigIconSize;
	private int mArrowWidth;
	private int mArrowHeadHeight;
	private int mArrowHeight;
	
	public Notifier(Context context) {
		mContext = context;
		mNotificationManager = NotificationManagerCompat.from(mContext);
		Resources res = context.getResources();
		mBigIconSize = res.getDimensionPixelSize(R.dimen.big_icon_size);
		mArrowWidth = res.getDimensionPixelSize(R.dimen.wind_direction_arrow_width);
		mArrowHeight = res.getDimensionPixelSize(R.dimen.wind_direction_arrow_height);
		mArrowHeadHeight = res.getDimensionPixelSize(R.dimen.wind_direction_arrow_head_height);
	}
	
	public void cancelNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
	
	public static void cancelNotification(Context context) {
		NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);		
	}
	
	public void notify(Wind wind) {		
		NotificationCompat.Builder notificationBuilder = getNotificationBuilder(mContext);
		notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
        	.addLine(wind.getDescriptionText(mContext))
        	.addLine(wind.location));
	    notificationBuilder.setContentTitle(wind.scale.getText(mContext));
	    //notificationBuilder.setContentText(wind.getDescriptionText(mContext));
	    notificationBuilder.setLargeIcon(createIcon(mContext, wind));
		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
		        NotificationManagerCompat.from(mContext);
		
		Notification notification =
		        new WearableNotifications.Builder(notificationBuilder)
		        .setHintHideIcon(true)
		        .build();
		
		// Build the notification and issues it with notification manager.
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	private NotificationCompat.Builder getNotificationBuilder(Context context) {
		Intent viewIntent = new Intent(context, SettingsActivity.class);
		PendingIntent viewPendingIntent =
		        PendingIntent.getActivity(context, 0, viewIntent, 0);
		
		Intent disableIntent = new Intent(ACTION_DISABLE);
		PendingIntent disablePendingIntent =
		        PendingIntent.getBroadcast(mContext, 0, disableIntent, 0);
		
		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentIntent(viewPendingIntent)
		        .addAction(R.drawable.btn_cancel, 
		        		mContext.getString(R.string.action_disable), disablePendingIntent);
		return notificationBuilder;
	}
	
	public void notifyGeneralError(String description) {
		String title = mContext.getString(R.string.error_general);		
		notifyError(title, description);		
	}
	
	public void notifyLocationError() {
		String title = mContext.getString(R.string.error_location);
		String description = mContext.getString(R.string.error_location_description);		
		notifyError(title, description);
	}
	
	public void notifyNetworkError() {
		String title = mContext.getString(R.string.error_network);
		String description = mContext.getString(R.string.error_network_description);
		notifyError(title, description);
	}
	
	private void notifyError(String title, String description) {
		NotificationCompat.Builder notificationBuilder = getNotificationBuilder(mContext);
	    notificationBuilder.setContentTitle(title);
	    notificationBuilder.setContentText(description);
		
		// Build the notification and issues it with notification manager.
		mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	
	public Bitmap createIcon(Context context, Wind wind) {
		Paint paint = new Paint();
		int color = wind.scale.getColor(context);
		paint.setColor(color);
		paint.setStyle(Style.FILL_AND_STROKE);
		Bitmap bitmap = Bitmap.createBitmap(mBigIconSize, mBigIconSize, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);		
		Bitmap backgroundBmp = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.turbine);
		Bitmap backgroundScaled = Bitmap.createScaledBitmap(backgroundBmp, mBigIconSize, mBigIconSize, false);
		canvas.drawBitmap(backgroundScaled, new Matrix(), new Paint());
		canvas.drawPath(getArrowPath(wind.degrees), paint);
		return bitmap;
	}
	
	private Path getArrowPath(float degrees) {
		Path path = new Path();
		int leftX = (mBigIconSize/2) - (mArrowWidth/2);
		int leftHeadX = leftX - mArrowWidth;
		int rightX = (mBigIconSize/2) + (mArrowWidth/2);
		int rightHeadX = rightX + mArrowWidth;
		path.moveTo(leftX, mArrowHeight); // bottom left
		path.lineTo(leftX, mArrowHeadHeight); // left, arrow head start
		path.lineTo(leftHeadX, mArrowHeadHeight); //  left, arrow head end 
		path.lineTo(mBigIconSize/2, 0); 				   // top
		path.lineTo(rightHeadX, mArrowHeadHeight); // right, arrow head end
		path.lineTo(rightX, mArrowHeadHeight); // right, arrow head start
		path.lineTo(rightX, mArrowHeight); // bottom right
		path.lineTo(leftX, mArrowHeight); // bottom left
		path.close();
		Matrix translateMatrix = new Matrix();
		translateMatrix.postTranslate(0, 30);
		path.transform(translateMatrix);
		RectF bounds = new RectF();
		path.computeBounds(bounds, true);
		Matrix rotateMatrix = new Matrix();
		rotateMatrix.postRotate(degrees, 
		                   (bounds.right + bounds.left)/2, 
		                   (bounds.bottom + bounds.top)/2);
		path.transform(rotateMatrix);
		return path;
	}
}
