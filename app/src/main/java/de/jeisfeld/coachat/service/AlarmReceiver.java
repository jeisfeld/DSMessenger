package de.jeisfeld.coachat.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.entity.Contact;
import de.jeisfeld.coachat.entity.Contact.AiPolicy;
import de.jeisfeld.coachat.entity.ConversationFlags;
import de.jeisfeld.coachat.entity.ReplyPolicy;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.account.ContactRegistry;
import de.jeisfeld.coachat.message.MessageDetails.MessagePriority;
import de.jeisfeld.coachat.message.MessageDetails.MessageType;

public class AlarmReceiver extends BroadcastReceiver {
	/**
	 * The Alarm action
	 */
	public static final String ALARM_ACTION = "de.jeisfeld.coachat.ALARM_RECEIVER";

	/**
	 * The extra key for relationId.
	 */
	public static final String EXTRA_RELATION_ID = "de.jeisfeld.coachat.EXTRA_RELATION_ID";

	/**
	 * The text send for auto-generated message to AI.
	 */
	private static final String AUTO_MESSAGE_TEXT = "[@]";

	/**
	 * Set the alarm for a contact.
	 *
	 * @param context The context.
	 * @param contact The contact.
	 */
	public static void setAlarm(final Context context, final Contact contact) {
		if (contact == null || contact.isSlave()
				|| (contact.getAiPolicy() != AiPolicy.AUTOMATIC && contact.getAiPolicy() != AiPolicy.AUTOMATIC_NOMESSAGE)) {
			cancelAlarm(context, contact);
			return;
		}
		Long alarmTime = contact.getAlarmTime();
		if (alarmTime == null) {
			cancelAlarm(context, contact);
			return;
		}
		if (alarmTime < System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)) {
			cancelAlarm(context, contact);
			executeAlarm(context, contact);
			return;
		}

		PendingIntent alarmIntent = createAlarmIntent(context, contact);
		setAlarm(context, alarmTime, alarmIntent);
	}

	/**
	 * Create the PendingIntent for an alarm.
	 *
	 * @param context The context.
	 * @param contact The contact.
	 * @return The PendingIntent.
	 */
	private static PendingIntent createAlarmIntent(final Context context, final Contact contact) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(ALARM_ACTION);
		intent.putExtra(EXTRA_RELATION_ID, contact.getRelationId());
		int uniqueId = contact.getRelationId();
		return PendingIntent.getBroadcast(context, uniqueId, intent,
				PendingIntent.FLAG_CANCEL_CURRENT | (VERSION.SDK_INT >= VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0));
	}

	/**
	 * Cancel the alarm for a contact.
	 *
	 * @param context The context.
	 * @param contact The contact.
	 */
	public static void cancelAlarm(final Context context, final Contact contact) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(createAlarmIntent(context, contact));
	}

	/**
	 * Execute the alarm for a contact.
	 *
	 * @param context The context.
	 * @param contact The contact.
	 */
	public static void executeAlarm(final Context context, final Contact contact) {
		if (contact.isSlave() || !contact.getAiMessageSuffix().contains("\"@\":")
				|| (contact.getAiPolicy() != AiPolicy.AUTOMATIC && contact.getAiPolicy() != AiPolicy.AUTOMATIC_NOMESSAGE)) {
			return;
		}

		ReplyPolicy replyPolicy = contact.getMyPermissions().getDefaultReplyPolicy();
		String conversationFlagString = new ConversationFlags(replyPolicy, replyPolicy.isExpectsAcknowledgement(),
				replyPolicy.isExpectsResponse() && !replyPolicy.isExpectsAcknowledgement()).toString();

		new HttpSender(context).sendMessage("db/conversation/sendmessage.php", contact, UUID.randomUUID(), null,
				"messageType", MessageType.TEXT.name(), "messageText", AUTO_MESSAGE_TEXT, "priority", MessagePriority.NORMAL.name(),
				"conversationId", UUID.randomUUID().toString(), "timestamp", Long.toString(System.currentTimeMillis()),
				"conversationFlags", conversationFlagString, "subject", context.getString(R.string.text_automatic_message));
	}

	/**
	 * Recreate all alarms.
	 *
	 * @param context The context.
	 */
	public static void recreateAllAlarms(final Context context) {
		for (Contact contact : ContactRegistry.getInstance().getContacts(false)) {
			setAlarm(context, contact);
		}
	}

	/**
	 * Set an alarm.
	 *
	 * @param context     The context
	 * @param alarmTime   The alarm time
	 * @param alarmIntent The alarm intent
	 */
	protected static void setAlarm(final Context context, final long alarmTime, final PendingIntent alarmIntent) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);

		reEnableAlarmsOnBoot(context);
	}

	/**
	 * Enable SdMountReceiver to automatically restart the alarm when the device is rebooted.
	 *
	 * @param context The context.
	 */
	protected static void reEnableAlarmsOnBoot(final Context context) {
		ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			recreateAllAlarms(context);
		}
		else if (ALARM_ACTION.equals(action)) {
			int relationId = intent.getIntExtra(EXTRA_RELATION_ID, -1);
			if (relationId >= 0) {
				Contact contact = ContactRegistry.getInstance().getContact(relationId);
				executeAlarm(context, contact);
			}
		}
	}
}
