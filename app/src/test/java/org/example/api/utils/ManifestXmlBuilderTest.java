package org.example.api.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ManifestXmlBuilder utility class.
 */
@DisplayName("ManifestXmlBuilder Tests")
class ManifestXmlBuilderTest {

    @Nested
    @DisplayName("XML Escaping Tests")
    class XmlEscapingTests {

        @Test
        @DisplayName("Should escape ampersand")
        void testEscapeXml_Ampersand() {
            assertEquals("AT&amp;T", ManifestXmlBuilder.escapeXml("AT&T"));
        }

        @Test
        @DisplayName("Should escape less than")
        void testEscapeXml_LessThan() {
            assertEquals("5 &lt; 10", ManifestXmlBuilder.escapeXml("5 < 10"));
        }

        @Test
        @DisplayName("Should escape greater than")
        void testEscapeXml_GreaterThan() {
            assertEquals("10 &gt; 5", ManifestXmlBuilder.escapeXml("10 > 5"));
        }

        @Test
        @DisplayName("Should escape double quote")
        void testEscapeXml_DoubleQuote() {
            assertEquals("Say &quot;Hello&quot;", ManifestXmlBuilder.escapeXml("Say \"Hello\""));
        }

        @Test
        @DisplayName("Should escape single quote")
        void testEscapeXml_SingleQuote() {
            assertEquals("It&apos;s working", ManifestXmlBuilder.escapeXml("It's working"));
        }

        @Test
        @DisplayName("Should escape multiple special characters")
        void testEscapeXml_Multiple() {
            assertEquals("&lt;tag attr=&quot;value&quot;&gt;Text &amp; more&lt;/tag&gt;",
                    ManifestXmlBuilder.escapeXml("<tag attr=\"value\">Text & more</tag>"));
        }

        @Test
        @DisplayName("Should handle null input")
        void testEscapeXml_Null() {
            assertEquals("", ManifestXmlBuilder.escapeXml(null));
        }

        @Test
        @DisplayName("Should handle empty string")
        void testEscapeXml_Empty() {
            assertEquals("", ManifestXmlBuilder.escapeXml(""));
        }

        @Test
        @DisplayName("Should not modify safe text")
        void testEscapeXml_SafeText() {
            assertEquals("Hello World 123", ManifestXmlBuilder.escapeXml("Hello World 123"));
        }
    }

    @Nested
    @DisplayName("Duration Formatting Tests")
    class DurationFormattingTests {

        @Test
        @DisplayName("Should format zero duration")
        void testFormatDuration_Zero() {
            assertEquals("PT0S", ManifestXmlBuilder.formatDuration(0));
        }

        @Test
        @DisplayName("Should format seconds only")
        void testFormatDuration_SecondsOnly() {
            assertEquals("PT45S", ManifestXmlBuilder.formatDuration(45));
        }

        @Test
        @DisplayName("Should format minutes and seconds")
        void testFormatDuration_MinutesAndSeconds() {
            assertEquals("PT2M5S", ManifestXmlBuilder.formatDuration(125));
        }

        @Test
        @DisplayName("Should format hours, minutes and seconds")
        void testFormatDuration_Full() {
            assertEquals("PT2H3M5S", ManifestXmlBuilder.formatDuration(7385));
        }

        @Test
        @DisplayName("Should format exact hour")
        void testFormatDuration_ExactHour() {
            assertEquals("PT1H", ManifestXmlBuilder.formatDuration(3600));
        }

        @Test
        @DisplayName("Should format exact minute")
        void testFormatDuration_ExactMinute() {
            assertEquals("PT5M", ManifestXmlBuilder.formatDuration(300));
        }

        @Test
        @DisplayName("Should format hour and minutes")
        void testFormatDuration_HourAndMinutes() {
            assertEquals("PT1H30M", ManifestXmlBuilder.formatDuration(5400));
        }

        @Test
        @DisplayName("Should format hour and seconds")
        void testFormatDuration_HourAndSeconds() {
            assertEquals("PT1H5S", ManifestXmlBuilder.formatDuration(3605));
        }

        @Test
        @DisplayName("Should handle negative as zero")
        void testFormatDuration_Negative() {
            assertEquals("PT0S", ManifestXmlBuilder.formatDuration(-100));
        }

        @Test
        @DisplayName("Should format typical video duration")
        void testFormatDuration_TypicalVideo() {
            // 1 minute 59.702 seconds (rounded to 119)
            assertEquals("PT1M59S", ManifestXmlBuilder.formatDuration(119));
        }
    }

    @Nested
    @DisplayName("Duration with Milliseconds Tests")
    class DurationWithMillisecondsTests {

        @Test
        @DisplayName("Should format duration with decimal seconds")
        void testFormatDurationWithMillis_DecimalSeconds() {
            assertEquals("PT1M59.702S", ManifestXmlBuilder.formatDurationWithMillis(119.702));
        }

        @Test
        @DisplayName("Should format whole seconds without decimal")
        void testFormatDurationWithMillis_WholeSeconds() {
            assertEquals("PT2M", ManifestXmlBuilder.formatDurationWithMillis(120.0));
        }

        @Test
        @DisplayName("Should format with millisecond precision")
        void testFormatDurationWithMillis_Precision() {
            assertEquals("PT45.123S", ManifestXmlBuilder.formatDurationWithMillis(45.123));
        }

        @Test
        @DisplayName("Should handle very small decimals")
        void testFormatDurationWithMillis_SmallDecimal() {
            assertEquals("PT1.001S", ManifestXmlBuilder.formatDurationWithMillis(1.001));
        }
    }

    @Nested
    @DisplayName("Language Code Normalization Tests")
    class LanguageCodeTests {

        @Test
        @DisplayName("Should convert underscore to hyphen")
        void testNormalizeLanguageCode_Underscore() {
            assertEquals("en-US", ManifestXmlBuilder.normalizeLanguageCode("en_US"));
        }

        @Test
        @DisplayName("Should leave hyphen unchanged")
        void testNormalizeLanguageCode_Hyphen() {
            assertEquals("en-US", ManifestXmlBuilder.normalizeLanguageCode("en-US"));
        }

        @Test
        @DisplayName("Should handle simple code")
        void testNormalizeLanguageCode_Simple() {
            assertEquals("en", ManifestXmlBuilder.normalizeLanguageCode("en"));
        }

        @Test
        @DisplayName("Should handle null")
        void testNormalizeLanguageCode_Null() {
            assertEquals("und", ManifestXmlBuilder.normalizeLanguageCode(null));
        }

        @Test
        @DisplayName("Should handle empty string")
        void testNormalizeLanguageCode_Empty() {
            assertEquals("und", ManifestXmlBuilder.normalizeLanguageCode(""));
        }

        @Test
        @DisplayName("Should handle multiple underscores")
        void testNormalizeLanguageCode_MultipleUnderscores() {
            assertEquals("zh-Hans-CN", ManifestXmlBuilder.normalizeLanguageCode("zh_Hans_CN"));
        }
    }

    @Nested
    @DisplayName("Language Name Tests")
    class LanguageNameTests {

        @Test
        @DisplayName("Should return name for English")
        void testGetLanguageName_English() {
            assertEquals("English", ManifestXmlBuilder.getLanguageName("en"));
        }

        @Test
        @DisplayName("Should return name for Spanish")
        void testGetLanguageName_Spanish() {
            assertEquals("Spanish", ManifestXmlBuilder.getLanguageName("es"));
        }

        @Test
        @DisplayName("Should return name for French")
        void testGetLanguageName_French() {
            assertEquals("French", ManifestXmlBuilder.getLanguageName("fr"));
        }

        @Test
        @DisplayName("Should return name for German")
        void testGetLanguageName_German() {
            assertEquals("German", ManifestXmlBuilder.getLanguageName("de"));
        }

        @Test
        @DisplayName("Should return name for Japanese")
        void testGetLanguageName_Japanese() {
            assertEquals("Japanese", ManifestXmlBuilder.getLanguageName("ja"));
        }

        @Test
        @DisplayName("Should return name for Chinese")
        void testGetLanguageName_Chinese() {
            assertEquals("Chinese", ManifestXmlBuilder.getLanguageName("zh"));
        }

        @Test
        @DisplayName("Should handle language with region")
        void testGetLanguageName_WithRegion() {
            assertEquals("English", ManifestXmlBuilder.getLanguageName("en-US"));
            assertEquals("Spanish", ManifestXmlBuilder.getLanguageName("es-MX"));
        }

        @Test
        @DisplayName("Should return Unknown for undefined")
        void testGetLanguageName_Undefined() {
            assertEquals("Unknown", ManifestXmlBuilder.getLanguageName("und"));
        }

        @Test
        @DisplayName("Should return Unknown for null")
        void testGetLanguageName_Null() {
            assertEquals("Unknown", ManifestXmlBuilder.getLanguageName(null));
        }

        @Test
        @DisplayName("Should return uppercase code for unknown language")
        void testGetLanguageName_Unknown() {
            assertEquals("XY", ManifestXmlBuilder.getLanguageName("xy"));
        }

        @Test
        @DisplayName("Should handle case insensitive")
        void testGetLanguageName_CaseInsensitive() {
            assertEquals("English", ManifestXmlBuilder.getLanguageName("EN"));
            assertEquals("Spanish", ManifestXmlBuilder.getLanguageName("ES"));
        }
    }

    @Nested
    @DisplayName("Indentation Tests")
    class IndentationTests {

        @Test
        @DisplayName("Should return empty string for level 0")
        void testIndent_LevelZero() {
            assertEquals("", ManifestXmlBuilder.indent(0));
        }

        @Test
        @DisplayName("Should return correct indent for level 1")
        void testIndent_LevelOne() {
            assertEquals("  ", ManifestXmlBuilder.indent(1));
        }

        @Test
        @DisplayName("Should return correct indent for level 2")
        void testIndent_LevelTwo() {
            assertEquals("    ", ManifestXmlBuilder.indent(2));
        }

        @Test
        @DisplayName("Should return correct indent for level 5")
        void testIndent_LevelFive() {
            assertEquals("          ", ManifestXmlBuilder.indent(5));
        }

        @Test
        @DisplayName("Should handle negative level as zero")
        void testIndent_Negative() {
            assertEquals("", ManifestXmlBuilder.indent(-1));
        }
    }
}