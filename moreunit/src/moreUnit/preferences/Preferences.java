package moreUnit.preferences;

import moreUnit.MoreUnitPlugin;

import org.eclipse.jface.preference.IPreferenceStore;

public class Preferences {
	
	private static Preferences	INSTANCE;
	
	public static synchronized Preferences instance() {
		if (INSTANCE == null) {
			INSTANCE = new Preferences();
		}
		return INSTANCE;
	}
	
	protected Preferences() {
	}

	public String[] getPrefixes() {
		return getValues(PreferenceConstants.PREFIXES);
	}

	public String[] getSuffixes() {
		return getValues(PreferenceConstants.SUFFIXES);
	}

	public boolean useClassCreationWizards() {
		if (store().contains(PreferenceConstants.USE_WIZARDS)) {
			return store().getBoolean(PreferenceConstants.USE_WIZARDS);
		}
		return store().getDefaultBoolean(PreferenceConstants.USE_WIZARDS);
	}

	public boolean switchToMatchingMethod() {
		if (store().contains(PreferenceConstants.SWITCH_TO_MATCHING_METHOD)) {
			return store().getBoolean(PreferenceConstants.SWITCH_TO_MATCHING_METHOD);
		}
		return store().getDefaultBoolean(PreferenceConstants.SWITCH_TO_MATCHING_METHOD);
	}

	public String getTestPackagePrefix() {
		if (store().contains(PreferenceConstants.TEST_PACKAGE_PREFIX)) {
			return store().getString(PreferenceConstants.TEST_PACKAGE_PREFIX);
		}
		return store().getDefaultString(PreferenceConstants.TEST_PACKAGE_PREFIX);
	}

	private String[] getValues(String listPreference) {
		if (store().contains(listPreference)) {
			return store().getString(listPreference).split(",");
		}
		return store().getDefaultString(listPreference).split(",");
	}

	private IPreferenceStore store() {
		return MoreUnitPlugin.getDefault().getPreferenceStore();
	}

}

// $Log$
// Revision 1.2  2006/05/12 22:34:36  channingwalton
// added class creation wizards if type to jump to does not exist
//
