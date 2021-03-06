package org.moreunit.mock.templates;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.moreunit.mock.model.Category;
import org.moreunit.mock.model.MockingTemplate;
import org.moreunit.mock.model.MockingTemplates;

import com.google.inject.Singleton;

@Singleton
public class MockingTemplateStore
{
    private final Map<String, Category> categories = new HashMap<String, Category>();
    private final Map<String, MockingTemplate> templates = new HashMap<String, MockingTemplate>();
    private final Map<String, Set<MockingTemplate>> templatesByCategory = new HashMap<String, Set<MockingTemplate>>();

    public void store(MockingTemplates mockingTemplates)
    {
        for (Category category : mockingTemplates.categories())
        {
            if(! categories.containsKey(category.id()))
            {
                categories.put(category.id(), category);
            }
        }

        for (MockingTemplate template : mockingTemplates)
        {
            store(template);
        }
    }

    private void store(MockingTemplate template)
    {
        if(templates.containsKey(template.id()))
        {
            throw new TemplateAlreadyDefinedException(template.id());
        }

        Set<MockingTemplate> categoryTemplates = templatesByCategory.get(template.categoryId());
        if(categoryTemplates == null)
        {
            categoryTemplates = new HashSet<MockingTemplate>();
            templatesByCategory.put(template.categoryId(), categoryTemplates);
        }
        categoryTemplates.add(template);
        templates.put(template.id(), template);
    }

    public MockingTemplate get(String templateId)
    {
        return templates.get(templateId);
    }

    public void clear()
    {
        categories.clear();
        templates.clear();
        templatesByCategory.clear();
    }

    public Collection<Category> getCategories()
    {
        return categories.values();
    }

    public Collection<MockingTemplate> getTemplates(Category category)
    {
        return templatesByCategory.get(category.id());
    }

    public Category getCategory(String categoryId)
    {
        return categories.get(categoryId);
    }

    public static class TemplateAlreadyDefinedException extends RuntimeException
    {
        private static final long serialVersionUID = - 6594805348873016229L;

        public TemplateAlreadyDefinedException(String templateId)
        {
            super(templateId);
        }
    }
}
