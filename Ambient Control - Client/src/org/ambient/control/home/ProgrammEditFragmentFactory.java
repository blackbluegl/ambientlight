package org.ambient.control.home;

import org.ambient.control.sceneryconfiguration.TronEditDialog;

import android.support.v4.app.Fragment;


public class ProgrammEditFragmentFactory {

	public static Fragment getByMapperName(String mapperName){
		return new TronEditDialog();
	}
}
