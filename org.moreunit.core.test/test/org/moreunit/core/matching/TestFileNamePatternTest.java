package org.moreunit.core.matching;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.moreunit.core.matching.TestFileNamePattern.isValid;

import java.util.Collection;

import org.junit.Test;

public class TestFileNamePatternTest
{
    private final NameTokenizer camelCaseTokenizer = new CamelCaseNameTokenizer();
    private final SeparatorNameTokenizer underscoreTokenizer = new SeparatorNameTokenizer("_");

    @Test
    public void should_evaluate_test_file_with_prefix() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("Pre${srcFile}", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("PreMyFile");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_test_file_with_suffix() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("${srcFile}_suffix", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("SomeFile_suffix");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("SomeFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_test_file_with_prefix_and_suffix() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("prefix_${srcFile}Suf", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("prefix_aFileSuf");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("aFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_test_file_with_variable_part_before_name() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("Pre*${srcFile}Suf", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("PreBarMySourceSuf");

        // then
        assertTrue(evaluation.isTestFile());

        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("BarMySource");

        Collection<String> names = evaluation.getOtherCorrespondingFilePatterns();
        assertEquals(2, names.size());
        assertTrue(names.contains("MySource"));
        assertTrue(names.contains("Source"));
    }

    @Test
    public void should_evaluate_test_file_with_variable_part_before_name__with_double_separator() throws Exception
    {
        for (String template : asList("pre*${srcFile}__suf", "pre__*${srcFile}__suf", "pre*__${srcFile}__suf", "pre__*__${srcFile}__suf"))
        {
            // given
            TestFileNamePattern pattern = new TestFileNamePattern(template, new SeparatorNameTokenizer("__"));

            // when
            FileNameEvaluation evaluation = pattern.evaluate("pre__bar__my__source__suf");

            // then
            assertTrue(evaluation.isTestFile());

            assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("bar__my__source");

            Collection<String> names = evaluation.getOtherCorrespondingFilePatterns();
            assertEquals(2, names.size());
            assertTrue(names.contains("my__source"));
            assertTrue(names.contains("source"));
        }
    }

    @Test
    public void should_evaluate_test_file_with_variable_part_after_name() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("Pre${srcFile}*Suf", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("PreMySourceBazSuf");

        // then
        assertTrue(evaluation.isTestFile());

        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MySourceBaz");

        Collection<String> names = evaluation.getOtherCorrespondingFilePatterns();
        assertEquals(2, names.size());
        assertTrue(names.contains("MySource"));
        assertTrue(names.contains("My"));
    }

    @Test
    public void should_evaluate_test_file_with_variable_part_after_name__with_separator() throws Exception
    {
        for (String template : asList("pre_${srcFile}*suf", "pre_${srcFile}_*suf", "pre_${srcFile}*_suf", "pre_${srcFile}_*_suf"))
        {
            // given
            TestFileNamePattern pattern = new TestFileNamePattern(template, underscoreTokenizer);

            // when
            FileNameEvaluation evaluation = pattern.evaluate("pre_my_source_baz_suf");

            // then
            assertTrue(evaluation.isTestFile());

            assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("my_source_baz");

            Collection<String> names = evaluation.getOtherCorrespondingFilePatterns();
            assertEquals(2, names.size());
            assertTrue(names.contains("my_source"));
            assertTrue(names.contains("my"));
        }
    }

    @Test
    public void should_evaluate_test_file_with_variable_part_before_prefix() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("*Pre${srcFile}Suf", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("FooPreMySourceSuf");

        // then
        assertTrue(evaluation.isTestFile());

        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MySource");

        Collection<String> names = evaluation.getOtherCorrespondingFilePatterns();
        assertTrue(names.isEmpty());
    }

    @Test
    public void should_evaluate_test_file_with_variable_part_after_suffix() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("Pre${srcFile}Suf*", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("PreMySourceSufQix");

        // then
        assertTrue(evaluation.isTestFile());

        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MySource");

        Collection<String> names = evaluation.getOtherCorrespondingFilePatterns();
        assertTrue(names.isEmpty());
    }

    @Test
    public void should_evaluate_test_file_with_variable_parts__extreme_case() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("*Pre*${srcFile}*Suf*", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("FooPreBarMySourceBazSufQix");

        // then
        assertTrue(evaluation.isTestFile());

        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("BarMySourceBaz");

        Collection<String> names = evaluation.getOtherCorrespondingFilePatterns();
        assertEquals(asList("BarMySource", "MySourceBaz", "SourceBaz", "BarMy", "Bar", "Baz"), names);
    }

    @Test
    public void should_evaluate_test_file_with_several_possible_prefixes() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("(Pre1|Pre2)${srcFile}", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("Pre1MyFile");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();

        evaluation = pattern.evaluate("Pre2MyFile");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_test_file_with_several_possible_suffixes() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("${srcFile}_(suf1|suf2)", underscoreTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("some_file_suf1");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("some_file");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();

        evaluation = pattern.evaluate("some_file_suf2");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("some_file");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_test_file_with_several_possible_prefixes_and_suffixes() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("(pre1|pre2)${srcFile}_(suf1|suf2)", underscoreTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("some_file_suf1");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("some_file");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();

        evaluation = pattern.evaluate("pre2_some_file");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("some_file");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();

        evaluation = pattern.evaluate("pre1_some_file_suf2");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("some_file");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();

        assertFalse(pattern.evaluate("some_file").isTestFile());
    }

    @Test
    public void should_evaluate_test_file_with_several_prefixes_and_suffixes_and_variable_parts() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("(Pre1|Pre2)*${srcFile}(Suf1|Suf2)*", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("Pre1MyFile");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).containsOnly("File");

        pattern.evaluate("MyFileSuf1");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).containsOnly("File");

        pattern.evaluate("Pre2MyFile");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).containsOnly("File");

        pattern.evaluate("Pre1MyFileSuf2");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("MyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).containsOnly("File");

        evaluation = pattern.evaluate("Pre2FooMyFileSuf2Bar");

        assertTrue(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("FooMyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).containsOnly("MyFile", "File");
    }

    @Test
    public void should_evaluate_src_file_with_prefix() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("Prefix${srcFile}", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("MyFile");

        assertFalse(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("PrefixMyFile");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_src_file_with_suffix() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("${srcFile}_suf", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("SomeFile");

        assertFalse(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("SomeFile_suf");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_src_file_with_prefix_and_suffix() throws Exception
    {
        TestFileNamePattern pattern = new TestFileNamePattern("pre_${srcFile}Suffix", camelCaseTokenizer);

        FileNameEvaluation evaluation = pattern.evaluate("aFile");

        assertFalse(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains("pre_aFileSuffix");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).isEmpty();
    }

    @Test
    public void should_evaluate_src_file_with_variable_parts() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("*Pre*${srcFile}*Suf*", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("Source");

        // then
        assertFalse(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(1).contains(".*Pre.*Source.*Suf.*");
        assertTrue(evaluation.getOtherCorrespondingFilePatterns().isEmpty());
    }

    @Test
    public void should_evaluate_src_file_with_several_possible_prefixes() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("(Pre1|Pre2)${srcFile}", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("Source");

        // then
        assertFalse(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(2).contains("Pre1Source", "Pre2Source");
        assertTrue(evaluation.getOtherCorrespondingFilePatterns().isEmpty());
    }

    @Test
    public void should_evaluate_src_file_with_several_possible_suffixes() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("${srcFile}(Suf1|Suf2)", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("Source");

        // then
        assertFalse(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(2).contains("SourceSuf1", "SourceSuf2");
        assertTrue(evaluation.getOtherCorrespondingFilePatterns().isEmpty());
    }

    @Test
    public void should_evaluate_src_file_with_several_prefixes_and_suffixes_and_variable_parts() throws Exception
    {
        // given
        TestFileNamePattern pattern = new TestFileNamePattern("*(Pre1|Pre2)*${srcFile}*(Suf1|Suf2)*", camelCaseTokenizer);

        // when
        FileNameEvaluation evaluation = pattern.evaluate("Source");

        // then
        assertFalse(evaluation.isTestFile());
        assertThat(evaluation.getPreferredCorrespondingFilePatterns()).hasSize(4) //
        .contains(".*Pre1.*Source.*Suf1.*", ".*Pre1.*Source.*Suf2.*", ".*Pre2.*Source.*Suf1.*", ".*Pre2.*Source.*Suf2.*");
        assertThat(evaluation.getOtherCorrespondingFilePatterns()).hasSize(4) //
        .contains(".*Pre1.*Source.*", ".*Pre1.*Source.*", ".*Source.*Suf1.*", ".*Source.*Suf2.*");
    }

    @Test
    public void should_validate_expresions() throws Exception
    {
        String[] withoutSeparator = { "${srcFile}", "*${srcFile}", "${srcFile}*", "*${srcFile}*" //
        , "Pre${srcFile}", "${srcFile}Suf", "Pre${srcFile}Suf" //
        , "*Pre${srcFile}", "${srcFile}Suf*", "*Pre${srcFile}Suf*" //
        , "*Pre*${srcFile}", "${srcFile}*Suf*", "*Pre*${srcFile}*Suf*" //
        , "(P1|P2)${srcFile}", "${srcFile}(S1|S2)", "*(P1|P2)*${srcFile}*(S1|S2|S3)*" };

        for (String template : withoutSeparator)
        {
            assertTrue(isValid(template, ""));
        }

        String[] withSeparator = { "${srcFile}", "*${srcFile}", "${srcFile}*", "*${srcFile}*" //
        , "pre_${srcFile}", "${srcFile}_suf", "pre-${srcFile}_suf" //
        , "*pre${srcFile}", "${srcFile}_suf*", "*pre_${srcFile}_suf*" //
        , "*_pre*${srcFile}", "${srcFile}*_suf_*", "*pre*_${srcFile}*_suf*" //
        , "(p1|p2)_${srcFile}", "${srcFile}_(s1|s2)", "*(p1|p2)_*${srcFile}*_(s1|s2|s3)*" };

        for (String template : withSeparator)
        {
            assertTrue(isValid(template, "_"));
        }
    }

    @Test
    public void should_invalidate_expresions() throws Exception
    {
        String[] withoutSeparator = { "*P*re*${srcFile}*Suf*", "*P1|P2*${srcFile}*(S1|S2)*", "*(P1|P2)*${srcFile}*S1|S2*" //
        , "(${srcFile})", "${something}" };

        for (String template : withoutSeparator)
        {
            assertFalse(isValid(template, ""));
        }

        String[] withSeparator = { "*pre*_${srcFile}*_s*uf*" };

        for (String template : withSeparator)
        {
            assertFalse(isValid(template, "_"));
        }
    }
}
