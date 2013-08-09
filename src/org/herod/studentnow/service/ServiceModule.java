package org.herod.studentnow.service;

public interface ServiceModule {
	
	public abstract void load();
	
	public abstract void schedule();
	
	public abstract void cancel();
	
	public abstract void cycle();
	
	public abstract boolean save();

}
