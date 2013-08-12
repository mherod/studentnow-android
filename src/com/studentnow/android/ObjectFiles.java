package com.studentnow.android;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectFiles {

	public static boolean saveObject(Object o, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(o);
		oos.close();
		fos.close();
		return true;
	}

	public static Object readObject(String file) throws IOException, ClassNotFoundException {
		Object o = null;
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		o = in.readObject();
		in.close();
		fileIn.close();
		return o;
	}

}
