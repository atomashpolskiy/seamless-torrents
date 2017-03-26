package slt.jna;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.ObjCObject;
import org.rococoa.Rococoa;
import org.rococoa.RunOnMainThread;
import org.rococoa.cocoa.foundation.NSObject;

import java.util.Optional;

@RunOnMainThread
public abstract class NSUserNotificationCenter extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSUserNotificationCenter", _Class.class);

    private interface _Class extends ObjCClass {
        NSUserNotificationCenter defaultUserNotificationCenter();
    }

    public static NSUserNotificationCenter defaultCenter() {
        return CLASS.defaultUserNotificationCenter();
    }

    private volatile NSUserNotificationCenterDelegate delegate;
    private volatile ObjCObject delegateProxy;

    public abstract void deliverNotification(NSUserNotification notification);

    public abstract void removeAllDeliveredNotifications();

    public synchronized void setDelegate(NSUserNotificationCenterDelegate delegate) {
        if (delegateProxy == null) {
            this.delegate = delegate;
            delegateProxy = Rococoa.proxy(delegate);
            setDelegate(delegateProxy.id());
        }
    }

    abstract void setDelegate(ID delegate);

    public Optional<NSUserNotificationCenterDelegate> getDelegate() {
        return Optional.of(delegate);
    }
}
