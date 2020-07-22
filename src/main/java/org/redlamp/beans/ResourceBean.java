package org.redlamp.beans;

import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.annotation.ManagedOperation;

@ManagedObject("jcgManagedObject")
public class ResourceBean {

	@ManagedAttribute
	public Integer getCount() {
		return CounterSingleton.getInstance().getCounter();
	}

	@ManagedOperation
	public void reset() {
		CounterSingleton.getInstance().reset();
	}
}
