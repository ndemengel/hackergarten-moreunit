package org.moreunit.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.moreunit.extensionpoints.INewTestCaseWizardContext;

public class NewTestCaseWizardContext implements INewTestCaseWizardContext
{
    private final IType classUnderTest;
    private final IPackageFragment testCasePackage;
    private final Map<String, Object> clientValues;
    private IType createdTestCase;

    public NewTestCaseWizardContext(IType classUnderTest, IPackageFragment testCasePackage)
    {
        this.classUnderTest = classUnderTest;
        this.testCasePackage = testCasePackage;
        this.clientValues = new HashMap<String, Object>();
    }

    public IType getClassUnderTest()
    {
        return classUnderTest;
    }

    public IType getCreatedTestCase()
    {
        return createdTestCase;
    }

    public IPackageFragment getTestCasePackage()
    {
        return testCasePackage;
    }

    void setCreatedTestCase(IType createdTestCase)
    {
        this.createdTestCase = createdTestCase;
    }

    public void put(String key, Object value)
    {
        clientValues.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key)
    {
        return (T) clientValues.get(key);
    }
}
