package com.nuttyknot.whenwhere.where;

import com.nuttyknot.whenwhere.where.event.Event;

public interface ActionListener {
	public abstract void handleEvent(Event event);
}
