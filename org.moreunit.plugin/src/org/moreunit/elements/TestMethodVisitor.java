package org.moreunit.elements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.moreunit.util.MoreUnitContants;
import org.moreunit.util.TestMethodDiviner;
import org.moreunit.util.TestMethodDivinerFactory;

/**
 * To get the testmethods the AST analyzes. This
 * 
 * @author vera 21.05.2006 21:45:30
 */
public class TestMethodVisitor extends ASTVisitor
{

    private List<MethodDeclaration> testmethods = new ArrayList<MethodDeclaration>();
    private TestMethodDivinerFactory testMethodDivinerFactory;
    //private TestMethodDiviner testMethodDiviner;

    public TestMethodVisitor(IType classType)
    {
        ASTParser testParser = ASTParser.newParser(AST.JLS3);
        testParser.setSource(classType.getCompilationUnit());
        testParser.createAST(null).accept(this);
        testMethodDivinerFactory = new TestMethodDivinerFactory(classType.getCompilationUnit());
        //testMethodDiviner = testMethodDivinerFactory.create();
        testMethodDivinerFactory.create();
    }

    // TODO move into testMethodDiviner
    @Override
    public boolean visit(MethodDeclaration node)
    {
        if(hasTestAnnotation(node))
            testmethods.add(node);
        else
        {
            if(node.getName().getFullyQualifiedName().startsWith(MoreUnitContants.TEST_JUNIT3_METHOD_PRAEFIX))
                testmethods.add(node);
        }
        return super.visit(node);
    }

    private boolean hasTestAnnotation(MethodDeclaration methodDeclaration)
    {
        List<?> modifiers = methodDeclaration.modifiers();
        for (Object modifier : modifiers)
        {
            if(modifier instanceof MarkerAnnotation)
            {
                MarkerAnnotation annotation = (MarkerAnnotation) modifier;
                if(MoreUnitContants.TEST_ANNOTATION_NAME.equals(annotation.getTypeName().getFullyQualifiedName()))
                    return true;
            }
        }

        return false;
    }

    public List<MethodDeclaration> getTestMethods()
    {
        return testmethods;
    }
}
