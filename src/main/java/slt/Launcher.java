package slt;

import com.sun.jna.NativeLibrary;
import slt.jna.NSUserNotification;
import slt.jna.NSUserNotificationCenter;
import slt.jna.NSUserNotificationCenterDelegate;

import java.util.Objects;

public class Launcher {

    public static void main(String[] args) {
        NativeLibrary.addSearchPath("rococoa",
                Objects.requireNonNull(System.getenv("ROCOCOA_HOME"), "Rococoa library home is not specified"));

        NSUserNotification notification = NSUserNotification.create();
        notification.setTitle("title");
        notification.setSubtitle("subtitle");
        notification.setInformative​Text("informative text");

        NSUserNotificationCenterDelegate delegate = new NSUserNotificationCenterDelegate() {
            public boolean userNotificationCenter_should​Present​Notification(NSUserNotificationCenter center, NSUserNotification notification) {
                // always show notifications
                return true;
            }
        };

        NSUserNotificationCenter notificationCenter = NSUserNotificationCenter.defaultCenter();
        notificationCenter.setDelegate(delegate);
        notificationCenter.deliverNotification(notification);
    }
}
