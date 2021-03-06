package org.moreunit.preferences;

import static java.util.Arrays.sort;
import static java.util.Collections.reverseOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.moreunit.MoreUnitPlugin;
import org.moreunit.core.util.StringLengthComparator;
import org.moreunit.elements.SourceFolderMapping;
import org.moreunit.util.PluginTools;

public class Preferences
{
    private static final Pattern MAVEN_TEST_FOLDER = Pattern.compile("src/test/.*");
    
    private static Map<IJavaProject, IPreferenceStore> preferenceMap = new HashMap<IJavaProject, IPreferenceStore>();

    private static final IPreferenceStore workbenchStore = MoreUnitPlugin.getDefault().getPreferenceStore();

    private static Preferences instance = new Preferences();

    static
    {
        workbenchStore.setDefault(PreferenceConstants.PREF_JUNIT_PATH, PreferenceConstants.PREF_JUNIT_PATH_DEFAULT);
        workbenchStore.setDefault(PreferenceConstants.TEST_TYPE, PreferenceConstants.DEFAULT_TEST_TYPE);
        workbenchStore.setDefault(PreferenceConstants.SHOW_REFACTORING_DIALOG, true);

        workbenchStore.setDefault(PreferenceConstants.PREFIXES, PreferenceConstants.DEFAULT_PRAEFIX);
        workbenchStore.setDefault(PreferenceConstants.SUFFIXES, PreferenceConstants.DEFAULT_SUFFIX);
        workbenchStore.setDefault(PreferenceConstants.USE_WIZARDS, PreferenceConstants.DEFAULT_USE_WIZARDS);
        workbenchStore.setDefault(PreferenceConstants.SWITCH_TO_MATCHING_METHOD, PreferenceConstants.DEFAULT_SWITCH_TO_MATCHING_METHOD);
        workbenchStore.setDefault(PreferenceConstants.TEST_PACKAGE_PREFIX, PreferenceConstants.DEFAULT_TEST_PACKAGE_PREFIX);
        workbenchStore.setDefault(PreferenceConstants.FLEXIBEL_TESTCASE_NAMING, PreferenceConstants.DEFAULT_FLEXIBLE_TESTCASE_NAMING);
        workbenchStore.setDefault(PreferenceConstants.TEST_SUPERCLASS, PreferenceConstants.DEFAULT_TEST_SUPERCLASS);
        workbenchStore.setDefault(PreferenceConstants.TEST_METHOD_TYPE, PreferenceConstants.TEST_METHOD_TYPE_JUNIT3);
        workbenchStore.setDefault(PreferenceConstants.EXTENDED_TEST_METHOD_SEARCH, PreferenceConstants.DEFAULT_EXTENDED_TEST_METHOD_SEARCH);
    }

    public static Preferences getInstance()
    {
        return instance;
    }

    /**
     * Necessary for easier testing
     */
    protected static void setInstance(Preferences preferences)
    {
        instance = preferences;
    }

    public boolean hasProjectSpecificSettings(IJavaProject javaProject)
    {
        if(javaProject == null)
            return false;

        return store(javaProject).getBoolean(PreferenceConstants.USE_PROJECT_SPECIFIC_SETTINGS);
    }

    public void setHasProjectSpecificSettings(IJavaProject javaProject, boolean hasProjectSpecificSettings)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.USE_PROJECT_SPECIFIC_SETTINGS, hasProjectSpecificSettings);
    }

    public void setMappingList(IJavaProject javaProject, List<SourceFolderMapping> mappingList)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.UNIT_SOURCE_FOLDER, PreferencesConverter.convertSourceMappingsToString(mappingList));
    }

    public List<SourceFolderMapping> getSourceMappingList(IJavaProject javaProject)
    {
        if(hasProjectSpecificSettings(javaProject))
        {
            return getProjectSpecificSourceMappingList(javaProject);
        }
        else
        {
            return getWorkspaceSpecificSourceMappingList(javaProject);
        }
    }

    private List<SourceFolderMapping> getProjectSpecificSourceMappingList(IJavaProject javaProject)
    {
        String mappingString = store(javaProject).getString(PreferenceConstants.UNIT_SOURCE_FOLDER);
        return PreferencesConverter.convertStringToSourceMappingList(mappingString);
    }

    private List<SourceFolderMapping> getWorkspaceSpecificSourceMappingList(IJavaProject javaProject)
    {
        List<SourceFolderMapping> mappings = new ArrayList<SourceFolderMapping>();

        List<IPackageFragmentRoot> javaSourceFolders = PluginTools.findJavaSourceFoldersFor(javaProject);

        IPackageFragmentRoot testSourceFolder = findDefaultTestSourceFolder(javaSourceFolders);

        List<IPackageFragmentRoot> possibleMainSrcFolders = findPossibleMainSourceFolders(javaSourceFolders);

        if(testSourceFolder != null)
        {
            for (IPackageFragmentRoot mainSourceFolder : possibleMainSrcFolders)
            {
                mappings.add(new SourceFolderMapping(javaProject, mainSourceFolder, testSourceFolder));
            }
        }

        return mappings;
    }

    private IPackageFragmentRoot findDefaultTestSourceFolder(List<IPackageFragmentRoot> sourceFolders)
    {
        String defaultTestSourceFolderPath = workbenchStore.getString(PreferenceConstants.PREF_JUNIT_PATH);

        for (IPackageFragmentRoot sourceFolder : sourceFolders)
        {
            String sourceFolderPath = PluginTools.getPathStringWithoutProjectName(sourceFolder);

            if(sourceFolderPath.equals(defaultTestSourceFolderPath))
            {
                return sourceFolder;
            }
        }

        return null;
    }

    private List<IPackageFragmentRoot> findPossibleMainSourceFolders(List<IPackageFragmentRoot> javaSourceFolders)
    {
        List<IPackageFragmentRoot> possibleMainSrcFolders = new ArrayList<IPackageFragmentRoot>();

        String defaultTestSourceFolderPath = workbenchStore.getString(PreferenceConstants.PREF_JUNIT_PATH);

        for (IPackageFragmentRoot sourceFolder : javaSourceFolders)
        {
            String sourceFolderPath = PluginTools.getPathStringWithoutProjectName(sourceFolder);

            if(! (sourceFolderPath.equals(defaultTestSourceFolderPath) || isMavenLikeTestFolder(sourceFolderPath)))
            {
                possibleMainSrcFolders.add(sourceFolder);
            }
        }

        return possibleMainSrcFolders;
    }

    private static boolean isMavenLikeTestFolder(String srcFolderPath)
    {
        return MAVEN_TEST_FOLDER.matcher(srcFolderPath).matches();
    }

    protected Preferences()
    {
        super();
    }

    public String getJunitDirectoryFromPreferences(IJavaProject javaProject)
    {
        return store(javaProject).getString(PreferenceConstants.PREF_JUNIT_PATH);
    }

    public void setJunitDirectory(String directory)
    {
        getProjectStore(null).setValue(PreferenceConstants.PREF_JUNIT_PATH, directory);
    }

    public String getTestMethodType(IJavaProject javaProject)
    {
        return store(javaProject).getString(PreferenceConstants.TEST_METHOD_TYPE);
    }

    public void setTestMethodTypeShouldUsePrefix(IJavaProject javaProject, boolean shouldUsePrefix)
    {
        String value;
        if(shouldUsePrefix)
            value = PreferenceConstants.TEST_METHOD_TYPE_JUNIT3;
        else
            value = PreferenceConstants.TEST_METHOD_TYPE_NO_PREFIX;

        getProjectStore(javaProject).setValue(PreferenceConstants.TEST_METHOD_TYPE, value);
    }

    public String getTestMethodDefaultContent(IJavaProject javaProject)
    {
        return store(javaProject).getString(PreferenceConstants.TEST_METHOD_DEFAULT_CONTENT);
    }

    public void setTestMethodDefaultContent(IJavaProject javaProject, String methodContent)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.TEST_METHOD_DEFAULT_CONTENT, methodContent);
    }

    public String[] getPrefixes(IJavaProject javaProject)
    {
        String preferenceValue = store(javaProject).getString(PreferenceConstants.PREFIXES);
        return PreferencesConverter.convertStringToArray(preferenceValue);
    }
    
    public String[] getPrefixesOrderedByDescLength(IJavaProject javaProject)
    {
        String[] prefixes = getPrefixes(javaProject);
        sort(prefixes, reverseOrder(new StringLengthComparator()));
        return prefixes;
    }

    public void setPrefixes(IJavaProject javaProject, String[] prefixes)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.PREFIXES, PreferencesConverter.convertArrayToString(prefixes));
    }

    public String[] getSuffixes(IJavaProject javaProject)
    {
        String preferenceValue = store(javaProject).getString(PreferenceConstants.SUFFIXES);
        return PreferencesConverter.convertStringToArray(preferenceValue);
    }

    public String[] getSuffixesOrderedByDescLength(IJavaProject javaProject)
    {
        String[] suffixes = getSuffixes(javaProject);
        sort(suffixes, reverseOrder(new StringLengthComparator()));
        return suffixes;
    }

    public void setSuffixes(IJavaProject javaProject, String[] suffixes)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.SUFFIXES, PreferencesConverter.convertArrayToString(suffixes));
    }

    public String getTestSuperClass(IJavaProject javaProject)
    {
        return getStringValue(PreferenceConstants.TEST_SUPERCLASS, javaProject);
    }

    public void setTestSuperClass(IJavaProject javaProject, String testSuperClass)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.TEST_SUPERCLASS, testSuperClass);
    }

    private String getStringValue(final String key, IJavaProject javaProject)
    {
        if(store(javaProject).contains(key))
        {
            return store(javaProject).getString(key);
        }
        return store(javaProject).getDefaultString(key);
    }

    public String getTestType(IJavaProject javaProject)
    {
        if(store(javaProject).contains(PreferenceConstants.TEST_TYPE))
        {
            return store(javaProject).getString(PreferenceConstants.TEST_TYPE);
        }
        return store(javaProject).getDefaultString(PreferenceConstants.DEFAULT_TEST_TYPE);
    }

    public void setTestType(IJavaProject javaProject, String testType)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.TEST_TYPE, testType);
    }

    public boolean shouldUseJunit4Type(IJavaProject javaProject)
    {
        if(store(javaProject).contains(PreferenceConstants.TEST_TYPE))
        {
            return PreferenceConstants.TEST_TYPE_VALUE_JUNIT_4.equals(store(javaProject).getString(PreferenceConstants.TEST_TYPE));
        }
        return PreferenceConstants.TEST_TYPE_VALUE_JUNIT_4.equals(PreferenceConstants.DEFAULT_TEST_TYPE);
    }

    public boolean shouldUseJunit3Type(IJavaProject javaProject)
    {
        if(store(javaProject).contains(PreferenceConstants.TEST_TYPE))
        {
            return PreferenceConstants.TEST_TYPE_VALUE_JUNIT_3.equals(store(javaProject).getString(PreferenceConstants.TEST_TYPE));
        }
        return PreferenceConstants.TEST_TYPE_VALUE_JUNIT_3.equals(PreferenceConstants.DEFAULT_TEST_TYPE);
    }

    public boolean shouldUseTestNgType(IJavaProject javaProject)
    {
        if(store(javaProject).contains(PreferenceConstants.TEST_TYPE))
        {
            return PreferenceConstants.TEST_TYPE_VALUE_TESTNG.equals(store(javaProject).getString(PreferenceConstants.TEST_TYPE));
        }
        return PreferenceConstants.TEST_TYPE_VALUE_TESTNG.equals(PreferenceConstants.DEFAULT_TEST_TYPE);
    }

    public boolean shouldUseFlexibleTestCaseNaming(IJavaProject javaProject)
    {
        if(store(javaProject).contains(PreferenceConstants.FLEXIBEL_TESTCASE_NAMING))
        {
            return store(javaProject).getBoolean(PreferenceConstants.FLEXIBEL_TESTCASE_NAMING);
        }
        return store(javaProject).getDefaultBoolean(PreferenceConstants.FLEXIBEL_TESTCASE_NAMING);
    }

    public void setShouldUseFlexibleTestCaseNaming(IJavaProject javaProject, boolean shouldUseFlexibleNaming)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.FLEXIBEL_TESTCASE_NAMING, shouldUseFlexibleNaming);
    }

    public String getTestPackagePrefix(IJavaProject javaProject)
    {
        return getStringValue(PreferenceConstants.TEST_PACKAGE_PREFIX, javaProject);
    }

    public void setTestPackagePrefix(IJavaProject javaProject, String packagePrefix)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.TEST_PACKAGE_PREFIX, packagePrefix);
    }

    public String getTestPackageSuffix(IJavaProject javaProject)
    {
        return getStringValue(PreferenceConstants.TEST_PACKAGE_SUFFIX, javaProject);
    }

    public void setTestPackageSuffix(IJavaProject javaProject, String packageSuffix)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.TEST_PACKAGE_SUFFIX, packageSuffix);
    }

    private IPreferenceStore store(IJavaProject javaProject)
    {
        IPreferenceStore resultStore = getProjectStore(javaProject);

        if(resultStore.getBoolean(PreferenceConstants.USE_PROJECT_SPECIFIC_SETTINGS))
            return resultStore;

        return workbenchStore;
    }

    public IPreferenceStore getProjectStore(IJavaProject javaProject)
    {
        if(javaProject == null)
            return workbenchStore;

        IPreferenceStore resultStore = null;

        if(preferenceMap.containsKey(javaProject))
        {
            resultStore = preferenceMap.get(javaProject);
        }
        else
        {
            ProjectScope projectScopeContext = new ProjectScope(javaProject.getProject());
            ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(projectScopeContext, MoreUnitPlugin.PLUGIN_ID);
            preferenceStore.setSearchContexts(new IScopeContext[] { projectScopeContext });
            preferenceMap.put(javaProject, preferenceStore);
            resultStore = preferenceStore;
        }

        return resultStore;
    }

    public static void clearProjectCach()
    {
        synchronized (preferenceMap)
        {
            preferenceMap.clear();
        }
    }

    public IPackageFragmentRoot getTestSourceFolder(IJavaProject project, IPackageFragmentRoot mainSrcFolder)
    {
        // check for project specific settings
        List<SourceFolderMapping> mappings = getSourceMappingList(project);

        for (SourceFolderMapping mapping : mappings)
        {
            if(mapping.getSourceFolder().equals(mainSrcFolder))
            {
                return mapping.getTestFolder();
            }
        }

        if(! mappings.isEmpty())
        {
            // falls back to first test folder defined
            return mappings.get(0).getTestFolder();
        }

        // no mapping exists: falls back to un-mapped source folders
        String junitFolder = getJunitDirectoryFromPreferences(project);

        for (IPackageFragmentRoot packageFragmentRoot : PluginTools.findJavaSourceFoldersFor(project))
        {
            if(PluginTools.getPathStringWithoutProjectName(packageFragmentRoot).equals(junitFolder))
            {
                return packageFragmentRoot;
            }
        }

        // falls back to given source folder
        return mainSrcFolder;
    }
    
    public IPackageFragmentRoot getMainSourceFolder(IJavaProject mainProject, IPackageFragmentRoot testSrcFolder)
    {
        List<SourceFolderMapping> mappings = getSourceMappingList(mainProject);

        for (SourceFolderMapping mapping : mappings)
        {
            if(mapping.getTestFolder().equals(testSrcFolder))
            {
                return mapping.getSourceFolder();
            }
        }

        if(! mappings.isEmpty())
        {
            // falls back to first main folder defined
            return mappings.get(0).getSourceFolder();
        }

        // no mapping exists: falls back to un-mapped source folders
        List<IPackageFragmentRoot> possibleMainSourceFolders = findPossibleMainSourceFolders(PluginTools.findJavaSourceFoldersFor(mainProject));
        if(! possibleMainSourceFolders.isEmpty())
        {
            return possibleMainSourceFolders.get(0);
        }

        // falls back to given source folder
        return testSrcFolder;
    }

    public IJavaProject getMainProject(IJavaProject testProject)
    {
        for (IPreferenceStore store : stores())
        {
            String mappingString = store.getString(PreferenceConstants.UNIT_SOURCE_FOLDER);
            List<SourceFolderMapping> mappings = PreferencesConverter.convertStringToSourceMappingList(mappingString);
            for (SourceFolderMapping mapping : mappings)
            {
                if(mapping.getTestFolder().getJavaProject().equals(testProject))
                {
                    return mapping.getJavaProject();
                }
            }
        }

        // falls back to given project
        return testProject;
    }

    private Collection<IPreferenceStore> stores()
    {
        return preferenceMap.values();
    }
    
    public boolean shouldUseTestMethodExtendedSearch(IJavaProject javaProject)
    {
        if(store(javaProject).contains(PreferenceConstants.EXTENDED_TEST_METHOD_SEARCH))
        {
            return store(javaProject).getBoolean(PreferenceConstants.EXTENDED_TEST_METHOD_SEARCH);
        }
        return store(javaProject).getDefaultBoolean(PreferenceConstants.EXTENDED_TEST_METHOD_SEARCH);
    }

    public void setShouldUseTestMethodExtendedSearch(IJavaProject javaProject, boolean shouldUseExtendedSearch)
    {
        getProjectStore(javaProject).setValue(PreferenceConstants.EXTENDED_TEST_METHOD_SEARCH, shouldUseExtendedSearch);
    }

    public static ProjectPreferences forProject(IJavaProject project)
    {
        return getInstance().getProjectView(project);
    }

    public ProjectPreferences getProjectView(IJavaProject project)
    {
        return new ProjectPreferences(this, project);
    }

    public static class ProjectPreferences
    {
        private final Preferences prefs;
        private final IJavaProject project;

        public ProjectPreferences(Preferences prefs, IJavaProject project)
        {
            this.prefs = prefs;
            this.project = project;
        }

        public String[] getClassPrefixes()
        {
            return prefs.getPrefixes(project);
        }

        public String[] getClassPrefixesOrderedByDescLength()
        {
            return prefs.getPrefixesOrderedByDescLength(project);
        }

        public String[] getClassSuffixes()
        {
            return prefs.getSuffixes(project);
        }

        public String[] getClassSuffixesOrderedByDescLength()
        {
            return prefs.getSuffixesOrderedByDescLength(project);
        }

        public IPackageFragmentRoot getMainSourceFolder(IPackageFragmentRoot testSrcFolder)
        {
            return prefs.getMainSourceFolder(project, testSrcFolder);
        }

        public String getPackagePrefix()
        {
            return prefs.getTestPackagePrefix(project);
        }

        public String getPackageSuffix()
        {
            return prefs.getTestPackageSuffix(project);
        }

        public List<SourceFolderMapping> getSourceFolderMappings()
        {
            return prefs.getSourceMappingList(project);
        }

        public IPackageFragmentRoot getTestSourceFolder(IPackageFragmentRoot mainSrcFolder)
        {
            return prefs.getTestSourceFolder(project, mainSrcFolder);
        }

        public String getTestMethodDefaultContent()
        {
            return prefs.getTestMethodDefaultContent(project);
        }

        public String getTestMethodType()
        {
            return prefs.getTestMethodType(project);
        }

        public String getTestSuperClass()
        {
            return prefs.getTestSuperClass(project);
        }

        public String getTestType()
        {
            return prefs.getTestType(project);
        }

        public boolean hasSpecificSettings()
        {
            return prefs.hasProjectSpecificSettings(project);
        }

        public boolean shouldUseFlexibleTestCaseNaming()
        {
            return prefs.shouldUseFlexibleTestCaseNaming(project);
        }

        public boolean shouldUseTestMethodExtendedSearch()
        {
            return prefs.shouldUseTestMethodExtendedSearch(project);
        }
    }
}
