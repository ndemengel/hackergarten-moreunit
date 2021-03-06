package org.moreunit.mock.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.moreunit.core.util.Strings;
import org.moreunit.mock.log.Logger;
import org.moreunit.mock.model.Category;
import org.moreunit.mock.model.MockingTemplate;
import org.moreunit.mock.templates.MockingTemplateStore;

import com.google.inject.Inject;

public class TemplateStyleSelector implements SelectionListener
{
    private final Preferences preferences;
    private final MockingTemplateStore templateStore;
    private final Logger logger;
    private final List<Category> categories = new ArrayList<Category>();
    private final List<MockingTemplate> categoryTemplates = new ArrayList<MockingTemplate>();

    private IJavaProject project;
    private Combo categoryCombo;
    private Combo templateCombo;
    private boolean contentCreated;
    private boolean enabled;
    private Category selectedCategory;
    private MockingTemplate selectedTemplate;

    @Inject
    public TemplateStyleSelector(Preferences preferences, MockingTemplateStore templateStore, Logger logger)
    {
        this.preferences = preferences;
        this.templateStore = templateStore;
        this.logger = logger;

        categories.addAll(templateStore.getCategories());
        Collections.sort(categories);

        contentCreated = false;
        enabled = true;
    }

    public void createContents(Composite parent, IJavaProject project)
    {
        this.project = project;

        Composite labelAndFieldComposite = new Composite(parent, SWT.NONE);
        labelAndFieldComposite.setLayout(new GridLayout(3, false));

        GridData rowLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        rowLayoutData.heightHint = 30;
        labelAndFieldComposite.setLayoutData(rowLayoutData);

        Label label = new Label(labelAndFieldComposite, SWT.NONE);
        label.setText("Mock style:");

        categoryCombo = createCombo(labelAndFieldComposite);
        categoryCombo.setItems(categoryNames());

        templateCombo = createCombo(labelAndFieldComposite);
        templateCombo.setItems(new String[] { "Please select a category first..." });

        categoryCombo.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent event)
            {
                int categoryIdx = categoryCombo.getSelectionIndex();
                if(categoryIdx == - 1)
                {
                    return;
                }

                Category category = categories.get(categoryIdx);

                if(! category.equals(selectedCategory))
                {
                    categoryTemplates.clear();
                    categoryTemplates.addAll(templateStore.getTemplates(category));
                    Collections.sort(categoryTemplates);

                    templateCombo.setItems(templateNames());
                    templateCombo.select(0);
                    templateCombo.pack();

                    selectedCategory = category;
                }
            }
        });

        initValues();

        contentCreated = true;

        initStyle();

        parent.addDisposeListener(new DisposeListener()
        {
            public void widgetDisposed(DisposeEvent e)
            {
                // remembers selected template
                selectedTemplate = getSelectedTemplate();
            }
        });
    }

    private Combo createCombo(Composite parent)
    {
        Combo combo = new Combo(parent, SWT.SINGLE | SWT.BORDER);
        combo.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        return combo;
    }

    public void reloadTemplates()
    {
        categories.clear();

        categories.addAll(templateStore.getCategories());
        Collections.sort(categories);

        categoryCombo.setItems(categoryNames());
        templateCombo.setItems(new String[] { "Please select a category first..." });

        if(! categories.isEmpty())
        {
            initValues();
        }
    }

    private String[] categoryNames()
    {
        String[] categoryNames = new String[categories.size()];
        int i = 0;
        for (Category category : categories)
        {
            categoryNames[i++] = category.name();
        }
        return categoryNames;
    }

    private String[] templateNames()
    {
        String[] templateNames = new String[categoryTemplates.size()];
        int i = 0;
        for (MockingTemplate template : categoryTemplates)
        {
            templateNames[i++] = template.name();
        }
        return templateNames;
    }

    private void initValues()
    {
        String mockingTemplateId = preferences.getMockingTemplate(project);
        if(Strings.isBlank(mockingTemplateId) || templateStore.get(mockingTemplateId) == null)
        {
            categoryCombo.select(0);
            templateCombo.select(0);
        }
        else
        {
            selectTemplate(mockingTemplateId);
        }
    }

    public void selectTemplate(String mockingTemplateId)
    {
        MockingTemplate mockingTemplate = templateStore.get(mockingTemplateId);
        Category category = templateStore.getCategory(mockingTemplate.categoryId());
        Assert.isNotNull(category);

        categoryCombo.select(categories.indexOf(category));
        templateCombo.select(categoryTemplates.indexOf(mockingTemplate));
    }

    private void initStyle()
    {
        categoryCombo.setEnabled(enabled);
        templateCombo.setEnabled(enabled);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        if(contentCreated)
        {
            initStyle();
        }
    }

    public void savePreferences()
    {
        MockingTemplate template = templateCombo.isDisposed() ? selectedTemplate : getSelectedTemplate();
        if(template == null)
        {
            logger.warn("Could not retrieve selected template");
            return;
        }

        preferences.setMockingTemplate(project, template.id());

        if(logger.debugEnabled())
        {
            logger.debug(String.format("Defined template %s for %s", template.id(), project == null ? "workspace" : "project " + project.getElementName()));
        }
    }

    private MockingTemplate getSelectedTemplate()
    {
        int selectionIndex = templateCombo.getSelectionIndex();
        // it may happens that the first entry was not automatically selected...
        if(selectionIndex == - 1)
        {
            selectionIndex = 0;
        }
        if(categoryTemplates.isEmpty())
        {
            return null;
        }
        return categoryTemplates.get(selectionIndex);
    }

    public void widgetDefaultSelected(SelectionEvent event)
    {
        // nothing to do
    }

    public void widgetSelected(SelectionEvent event)
    {
        // nothing to do
    }
}
