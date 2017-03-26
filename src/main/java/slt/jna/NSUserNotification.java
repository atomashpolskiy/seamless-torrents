package slt.jna;

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSImage;
import org.rococoa.cocoa.foundation.NSObject;

public abstract class NSUserNotification extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSUserNotification", _Class.class);

    private interface _Class extends ObjCClass {
        NSUserNotification alloc();
    }

    public static NSUserNotification create() {
        return CLASS.alloc().init();
    }

    abstract NSUserNotification init();

    public abstract void setTitle(String title);

    public abstract void setSubtitle(String subtitle);

    public abstract void setInformative​Text(String informative​Text);

    public abstract void setContent​Image(NSImage content​Image);

    public abstract void setIdentifier(String identifier);

    public abstract void setHas​Action​Button(boolean has​Action​Button);

    public abstract void setAction​Button​Title(String action​Button​Title);

    public abstract void setOther​Button​Title(String other​Button​Title);

    public abstract void setHas​Reply​Button(boolean has​Reply​Button);

}
