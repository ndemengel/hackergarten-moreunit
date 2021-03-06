package org.moreunit.core.languages;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.moreunit.core.extension.ExtensionPoints;
import org.moreunit.core.log.Logger;

// TODO Nicolas: handle extension addition/removal
public class LanguageExtensionManager
{
    private static final String FILE_EXTENSION_ATTR = "fileExtension";
    private static final String NAME_ATTR = "name";

    private final Logger logger;

    public LanguageExtensionManager(Logger logger)
    {
        this.logger = logger;
    }

    public boolean extensionExistsForLanguage(String langId)
    {
        return getLanguages().contains(new Language(langId));
    }

    private Collection<Language> getLanguages()
    {
        Collection<Language> languages = new HashSet<Language>();

        for (IConfigurationElement cfg : Platform.getExtensionRegistry().getConfigurationElementsFor(ExtensionPoints.LANGUAGES))
        {
            try
            {
                languages.add(new Language(cfg.getAttribute(FILE_EXTENSION_ATTR), cfg.getAttribute(NAME_ATTR)));
            }
            catch (Exception e)
            {
                logger.warn("Could not load extension from plug-in \"" + cfg.getContributor().getName() + "\" for point \"" + ExtensionPoints.LANGUAGES + "\": " + e.getMessage());
                continue;
            }
        }

        return languages;
    }
}
