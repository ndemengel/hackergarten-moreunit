package org.moreunit.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.moreunit.SourceFolderContext;
import org.moreunit.elements.SourceFolderMapping;
import org.moreunit.log.LogHandler;
import org.moreunit.preferences.Preferences;
import org.moreunit.util.PluginTools;

/**
 * @author vera 24.11.2008 20:18:09
 */
public class RenamePackageParticipant extends RenameParticipant
{

    private IPackageFragment packageFragment;
    private IPackageFragmentRoot packageFragmentRoot;
    private List<IPackageFragmentRoot> correspondingPackages;

    @Override
    protected boolean initialize(Object element)
    {
        LogHandler.getInstance().handleInfoLog("RenamePackageParticipant.initialize");
        packageFragment = (IPackageFragment) element;

        IJavaElement fragment = packageFragment;
        while (! (fragment instanceof IPackageFragmentRoot))
        {
            fragment = fragment.getParent();
        }
        packageFragmentRoot = (IPackageFragmentRoot) fragment;
        correspondingPackages = getSourceFolderFromContext();

        return ! isTestSourceFolder();
    }

    private boolean isTestSourceFolder()
    {
        List<SourceFolderMapping> sourceMappingList = Preferences.getInstance().getSourceMappingList(packageFragment.getJavaProject());
        for (SourceFolderMapping mapping : sourceMappingList)
        {
            if(packageFragmentRoot.equals(mapping.getTestFolder()))
                return true;
        }

        return false;
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException
    {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        if(! getArguments().getUpdateReferences())
        {
            return null;
        }

        List<Change> changes = new ArrayList<Change>();

        for (IPackageFragmentRoot packageRoot : correspondingPackages)
        {
            String cutPackageName = packageFragment.getElementName();
            IPackageFragment packageToRename = packageRoot.getPackageFragment(PluginTools.getTestPackageName(cutPackageName, Preferences.getInstance(), packageFragment.getJavaProject()));
            if(packageToRename != null && packageToRename.exists())
            {
                RefactoringContribution refactoringContribution = RefactoringCore.getRefactoringContribution(IJavaRefactorings.RENAME_PACKAGE);
                RenameJavaElementDescriptor renameJavaElementDescriptor = (RenameJavaElementDescriptor) refactoringContribution.createDescriptor();
                renameJavaElementDescriptor.setJavaElement(packageToRename);
                renameJavaElementDescriptor.setNewName(PluginTools.getTestPackageName(getArguments().getNewName(), Preferences.getInstance(), packageFragment.getJavaProject()));

                RefactoringStatus refactoringStatus = new RefactoringStatus();
                Refactoring renameRefactoring = renameJavaElementDescriptor.createRefactoring(refactoringStatus);
                renameRefactoring.checkAllConditions(pm);

                changes.add(renameRefactoring.createChange(pm));
            }
        }

        return new CompositeChange(getName(), changes.toArray(new Change[changes.size()]));
    }

    private List<IPackageFragmentRoot> getSourceFolderFromContext()
    {
        SourceFolderContext context = SourceFolderContext.getInstance();
        return context.getSourceFolderToSearch(packageFragmentRoot);
    }

    @Override
    public String getName()
    {
        LogHandler.getInstance().handleInfoLog("RenamePackageParticipant.getName");
        return "MoreUnit Rename Package";
    }
}
