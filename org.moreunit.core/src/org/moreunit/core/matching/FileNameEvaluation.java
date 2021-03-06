package org.moreunit.core.matching;

import java.util.Collection;
import java.util.Iterator;

public class FileNameEvaluation
{
    private final boolean testFile;
    private final Collection<String> otherCorrespondingFilePatterns;
    private final Collection<String> preferredCorrespondingFilePatterns;

    public FileNameEvaluation(boolean testFile, Collection<String> preferredCorrespondingFilePatterns, Collection<String> otherCorrespondingFilePatterns)
    {
        this.testFile = testFile;
        this.preferredCorrespondingFilePatterns = preferredCorrespondingFilePatterns;
        this.otherCorrespondingFilePatterns = otherCorrespondingFilePatterns;
    }

    public boolean isTestFile()
    {
        return testFile;
    }

    public Collection<String> getPreferredCorrespondingFilePatterns()
    {
        return preferredCorrespondingFilePatterns;
    }

    public Collection<String> getOtherCorrespondingFilePatterns()
    {
        return otherCorrespondingFilePatterns;
    }

    public String getPreferredCorrespondingFileName()
    {
        Iterator<String> it = preferredCorrespondingFilePatterns.iterator();
        if(! it.hasNext())
        {
            return null;
        }
        return it.next().replaceAll("\\.\\*", "");
    }
}
