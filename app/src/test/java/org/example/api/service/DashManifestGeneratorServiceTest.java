package org.example.api.service;

import org.example.api.dto.dash.AudioStreamMetadataDTO;
import org.example.api.dto.dash.DashManifestConfigDTO;
import org.example.api.dto.dash.SubtitleMetadataDTO;
import org.example.api.dto.dash.VideoStreamMetadataDTO;
import org.example.api.exception.ValidationException;
import org.example.api.utils.ManifestXmlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DashManifestGeneratorService.
 */
@DisplayName("DashManifestGeneratorService Tests")
class DashManifestGeneratorServiceTest {

    private DashManifestGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new DashManifestGeneratorService();
    }

    // ── XML helpers ──────────────────────────────────────────────────────

    /** Parses the manifest, failing the test with the manifest text if it isn't well-formed. */
    private Document parse(String manifest) {
        try {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(manifest)));
        } catch (Exception e) {
            throw new AssertionError("Generated manifest is not well-formed XML:\n" + manifest, e);
        }
    }

    private List<Element> elements(Document doc, String tag) {
        NodeList nodes = doc.getElementsByTagName(tag);
        return IntStream.range(0, nodes.getLength())
                .mapToObj(i -> (Element) nodes.item(i))
                .toList();
    }

    private List<Element> childElements(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return IntStream.range(0, nodes.getLength())
                .mapToObj(i -> (Element) nodes.item(i))
                .toList();
    }

    private Element only(List<Element> list) {
        assertThat(list).hasSize(1);
        return list.get(0);
    }

    private String baseUrl(Element representation) {
        return only(childElements(representation, "BaseURL")).getTextContent();
    }

    // ── Fixture factories (kept from the previous suite) ─────────────────

    private static DashManifestConfigDTO createBasicConfig() {
        DashManifestConfigDTO config = new DashManifestConfigDTO();
        config.setType("static");
        config.setMediaPresentationDuration("PT2M");
        config.setMinBufferTime("PT2S");
        config.setProfiles("urn:mpeg:dash:profile:isoff-on-demand:2011");
        config.setDurationSeconds(120);
        config.setVideoStreams(new ArrayList<>());
        config.setAudioStreams(new ArrayList<>());
        config.setSubtitleStreams(new ArrayList<>());
        return config;
    }

    private static VideoStreamMetadataDTO createVideoStream(String id, int width, int height, int bandwidth) {
        return VideoStreamMetadataDTO.builder()
                .id(id)
                .url("https://example.com/" + id + ".mp4")
                .codec("avc1.640028")
                .mimeType("video/mp4")
                .width(width)
                .height(height)
                .frameRate("24")
                .bandwidth(bandwidth)
                .build();
    }

    private static AudioStreamMetadataDTO createAudioStream(String id, String language, int bandwidth) {
        return AudioStreamMetadataDTO.builder()
                .id(id)
                .url("https://example.com/" + id + ".mp4")
                .codec("mp4a.40.2")
                .mimeType("audio/mp4")
                .bandwidth(bandwidth)
                .audioSamplingRate("44100")
                .audioChannels(2)
                .language(language)
                .languageName(ManifestXmlBuilder.getLanguageName(language))
                .build();
    }

    private static SubtitleMetadataDTO createSubtitleStream(String id, String language, String kind) {
        return SubtitleMetadataDTO.builder()
                .id(id)
                .url("https://example.com/" + id + ".vtt")
                .language(language)
                .languageName(ManifestXmlBuilder.getLanguageName(language))
                .mimeType("text/vtt")
                .kind(kind)
                .bandwidth(256)
                .build();
    }

    // ── Validation ───────────────────────────────────────────────────────

    static Stream<Arguments> invalidConfigs() {
        return Stream.of(
                Arguments.of("null config", (Supplier<DashManifestConfigDTO>) () -> null),
                Arguments.of("zero duration", supplierMutating(c -> c.setDurationSeconds(0))),
                Arguments.of("negative duration", supplierMutating(c -> c.setDurationSeconds(-10))),
                Arguments.of("null video streams", supplierMutating(c -> c.setVideoStreams(null))),
                Arguments.of("null audio streams", supplierMutating(c -> c.setAudioStreams(null))),
                Arguments.of("null subtitle streams", supplierMutating(c -> c.setSubtitleStreams(null))));
    }

    private static Supplier<DashManifestConfigDTO> supplierMutating(
            java.util.function.Consumer<DashManifestConfigDTO> mutation) {
        return () -> {
            DashManifestConfigDTO config = createBasicConfig();
            mutation.accept(config);
            return config;
        };
    }

    @ParameterizedTest(name = "Rejects {0}")
    @MethodSource("invalidConfigs")
    @DisplayName("Rejects invalid configurations with ValidationException")
    void rejectsInvalidConfigs(String name, Supplier<DashManifestConfigDTO> configSupplier) {
        assertThatThrownBy(() -> service.generateManifestXml(configSupplier.get()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Accepts empty stream lists, producing a manifest with no AdaptationSets")
    void acceptsEmptyStreamLists() {
        String manifest = service.generateManifestXml(createBasicConfig());

        Document doc = parse(manifest);
        assertThat(elements(doc, "Period")).hasSize(1);
        assertThat(elements(doc, "AdaptationSet")).isEmpty();
    }

    // ── Document structure ───────────────────────────────────────────────

    @Test
    @DisplayName("MPD root and Period carry the configuration attributes")
    void mpdHeaderAndPeriod_carryConfigAttributes() {
        String manifest = service.generateManifestXml(createBasicConfig());

        assertThat(manifest).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        Document doc = parse(manifest);
        Element mpd = doc.getDocumentElement();
        assertThat(mpd.getTagName()).isEqualTo("MPD");
        assertThat(mpd.getAttribute("xmlns")).isEqualTo("urn:mpeg:dash:schema:mpd:2011");
        assertThat(mpd.getAttribute("type")).isEqualTo("static");
        assertThat(mpd.getAttribute("mediaPresentationDuration")).isEqualTo("PT2M");
        assertThat(mpd.getAttribute("minBufferTime")).isEqualTo("PT2S");
        assertThat(mpd.getAttribute("profiles")).isEqualTo("urn:mpeg:dash:profile:isoff-on-demand:2011");

        Element period = only(elements(doc, "Period"));
        assertThat(period.getAttribute("duration")).isEqualTo("PT2M");
    }

    // ── Video ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Video AdaptationSet")
    class VideoAdaptationSetTests {

        @Test
        @DisplayName("Sorts representations by height descending and carries full attributes")
        void sortsByHeightWithFullAttributes() {
            DashManifestConfigDTO config = createBasicConfig();
            // Deliberately out of order
            config.setVideoStreams(List.of(
                    createVideoStream("video-360", 640, 360, 467558),
                    createVideoStream("video-1080", 1920, 1080, 3423702),
                    createVideoStream("video-720", 1280, 720, 1215044)));

            Document doc = parse(service.generateManifestXml(config));

            Element set = only(elements(doc, "AdaptationSet"));
            assertThat(set.getAttribute("id")).isEqualTo("0");
            assertThat(set.getAttribute("contentType")).isEqualTo("video");
            assertThat(set.getAttribute("mimeType")).isEqualTo("video/mp4");
            assertThat(set.getAttribute("subsegmentAlignment")).isEqualTo("true");
            assertThat(set.getAttribute("startWithSAP")).isEqualTo("1");

            List<Element> reps = childElements(set, "Representation");
            assertThat(reps).extracting(r -> r.getAttribute("height"))
                    .containsExactly("1080", "720", "360");

            // Full attribute set of one representation, asserted together
            Element top = reps.get(0);
            assertThat(top.getAttribute("id")).isEqualTo("video-1080");
            assertThat(top.getAttribute("bandwidth")).isEqualTo("3423702");
            assertThat(top.getAttribute("codecs")).isEqualTo("avc1.640028");
            assertThat(top.getAttribute("width")).isEqualTo("1920");
            assertThat(top.getAttribute("frameRate")).isEqualTo("24");
            assertThat(baseUrl(top)).isEqualTo("https://example.com/video-1080.mp4");
        }

        @Test
        @DisplayName("Includes SegmentBase only when BOTH ranges are present")
        void includesSegmentBaseOnlyWhenBothRangesPresent() {
            VideoStreamMetadataDTO withRanges = createVideoStream("video-full", 1920, 1080, 3000000);
            withRanges.setInitRange("0-740");
            withRanges.setIndexRange("741-1048");

            VideoStreamMetadataDTO withoutRanges = createVideoStream("video-none", 1280, 720, 1200000);

            VideoStreamMetadataDTO initOnly = createVideoStream("video-init-only", 640, 360, 400000);
            initOnly.setInitRange("0-740");

            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(List.of(withRanges, withoutRanges, initOnly));

            Document doc = parse(service.generateManifestXml(config));

            Element segmentBase = only(elements(doc, "SegmentBase"));
            assertThat(segmentBase.getAttribute("indexRange")).isEqualTo("741-1048");
            assertThat(only(childElements(segmentBase, "Initialization")).getAttribute("range"))
                    .isEqualTo("0-740");
            // ...and it belongs to the representation that has both ranges
            assertThat(((Element) segmentBase.getParentNode()).getAttribute("id"))
                    .isEqualTo("video-full");
        }

        @Test
        @DisplayName("Escapes user-provided content so values round-trip through a real XML parser")
        void escapesUserProvidedContent() {
            VideoStreamMetadataDTO video = createVideoStream("video-<test>", 1920, 1080, 3000000);
            video.setUrl("https://example.com/video?a=1&b=2");
            video.setCodec("avc1.640028 & more");

            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(List.of(video));

            // parse() failing IS the escaping test; round-tripping the
            // original values proves escaping is correct, not just present.
            Document doc = parse(service.generateManifestXml(config));

            Element rep = only(elements(doc, "Representation"));
            assertThat(rep.getAttribute("id")).isEqualTo("video-<test>");
            assertThat(rep.getAttribute("codecs")).isEqualTo("avc1.640028 & more");
            assertThat(baseUrl(rep)).isEqualTo("https://example.com/video?a=1&b=2");
        }
    }

    // ── Audio ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Audio AdaptationSets")
    class AudioAdaptationSetTests {

        @Test
        @DisplayName("Groups by language, ordered und → en → alphabetical, with sequential ids and labels")
        void groupsByLanguageInPriorityOrder() {
            DashManifestConfigDTO config = createBasicConfig();
            // Scrambled languages; two "en" streams to prove grouping
            List<AudioStreamMetadataDTO> streams = new ArrayList<>(List.of(
                    createAudioStream("audio-de", "de", 128000),
                    createAudioStream("audio-en-hi", "en", 256000),
                    createAudioStream("audio-und", "und", 128000),
                    createAudioStream("audio-es", "es", 128000),
                    createAudioStream("audio-en-lo", "en", 64000)));
            // Null languageName exercises the ManifestXmlBuilder fallback branch
            streams.get(0).setLanguageName(null);
            config.setAudioStreams(streams);

            Document doc = parse(service.generateManifestXml(config));

            List<Element> sets = elements(doc, "AdaptationSet");
            assertThat(sets).extracting(s -> s.getAttribute("lang"))
                    .containsExactly("und", "en", "de", "es");
            assertThat(sets).extracting(s -> s.getAttribute("id"))
                    .containsExactly("1", "2", "3", "4");
            assertThat(sets).allSatisfy(s ->
                    assertThat(s.getAttribute("contentType")).isEqualTo("audio"));

            // Grouping: both "en" streams live in the single "en" set
            assertThat(childElements(sets.get(1), "Representation")).hasSize(2);

            // Labels: provided languageName used directly; null falls back to lookup
            assertThat(sets.get(1).getAttribute("label"))
                    .isEqualTo(ManifestXmlBuilder.getLanguageName("en"));
            assertThat(sets.get(2).getAttribute("label"))
                    .isEqualTo(ManifestXmlBuilder.getLanguageName("de"));
        }

        @Test
        @DisplayName("Sorts representations by bandwidth descending with full attributes, channel config, and SegmentBase")
        void sortsByBandwidthWithFullAttributes() {
            AudioStreamMetadataDTO top = createAudioStream("audio-256", "en", 256000);
            top.setInitRange("0-640");
            top.setIndexRange("641-900");

            DashManifestConfigDTO config = createBasicConfig();
            config.setAudioStreams(List.of(
                    createAudioStream("audio-64", "en", 64000),
                    top,
                    createAudioStream("audio-128", "en", 128000)));

            Document doc = parse(service.generateManifestXml(config));

            Element set = only(elements(doc, "AdaptationSet"));
            List<Element> reps = childElements(set, "Representation");
            assertThat(reps).extracting(r -> r.getAttribute("bandwidth"))
                    .containsExactly("256000", "128000", "64000");

            Element rep = reps.get(0);
            assertThat(rep.getAttribute("id")).isEqualTo("audio-256");
            assertThat(rep.getAttribute("codecs")).isEqualTo("mp4a.40.2");
            assertThat(rep.getAttribute("audioSamplingRate")).isEqualTo("44100");
            assertThat(baseUrl(rep)).isEqualTo("https://example.com/audio-256.mp4");

            Element channelConfig = only(childElements(rep, "AudioChannelConfiguration"));
            assertThat(channelConfig.getAttribute("schemeIdUri"))
                    .isEqualTo("urn:mpeg:dash:23003:3:audio_channel_configuration:2011");
            assertThat(channelConfig.getAttribute("value")).isEqualTo("2");

            Element segmentBase = only(childElements(rep, "SegmentBase"));
            assertThat(segmentBase.getAttribute("indexRange")).isEqualTo("641-900");
        }
    }

    // ── Subtitles ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Subtitle AdaptationSets")
    class SubtitleAdaptationSetTests {

        @Test
        @DisplayName("Creates one AdaptationSet per subtitle, language-sorted, with ids from 100")
        void onePerSubtitleWithIdsFrom100() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setSubtitleStreams(List.of(
                    createSubtitleStream("subtitle-es", "es", "subtitles"),
                    createSubtitleStream("subtitle-en", "en", "subtitles")));

            Document doc = parse(service.generateManifestXml(config));

            List<Element> sets = elements(doc, "AdaptationSet");
            assertThat(sets).extracting(s -> s.getAttribute("lang"))
                    .containsExactly("en", "es"); // alphabetical
            assertThat(sets).extracting(s -> s.getAttribute("id"))
                    .containsExactly("100", "101");

            Element set = sets.get(0);
            assertThat(set.getAttribute("contentType")).isEqualTo("text");
            assertThat(set.getAttribute("mimeType")).isEqualTo("text/vtt");

            Element role = only(childElements(set, "Role"));
            assertThat(role.getAttribute("schemeIdUri")).isEqualTo("urn:mpeg:dash:role:2011");
            assertThat(role.getAttribute("value")).isEqualTo("subtitles");

            Element rep = only(childElements(set, "Representation"));
            assertThat(rep.getAttribute("id")).isEqualTo("subtitle-en");
            assertThat(rep.getAttribute("bandwidth")).isEqualTo("256");
            assertThat(baseUrl(rep)).isEqualTo("https://example.com/subtitle-en.vtt");
        }

        @Test
        @DisplayName("Maps auto-generated ('asr') kind to the 'subtitles' role, passing other kinds through")
        void mapsAsrKindToSubtitlesRole() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setSubtitleStreams(List.of(
                    createSubtitleStream("subtitle-auto", "en", "asr"),
                    createSubtitleStream("subtitle-cc", "es", "captions")));

            Document doc = parse(service.generateManifestXml(config));

            List<Element> roles = elements(doc, "Role");
            assertThat(roles).extracting(r -> r.getAttribute("value"))
                    .containsExactly("subtitles", "captions");
        }
    }

    // ── Whole manifest ───────────────────────────────────────────────────

    @Test
    @DisplayName("Generates a well-formed manifest with all stream types present")
    void generatesCompleteManifestWithAllStreamTypes() {
        DashManifestConfigDTO config = createBasicConfig();
        config.setVideoStreams(List.of(
                createVideoStream("video-1", 1920, 1080, 3423702),
                createVideoStream("video-2", 1280, 720, 1215044)));
        config.setAudioStreams(List.of(
                createAudioStream("audio-1", "en", 130482),
                createAudioStream("audio-2", "es", 128000)));
        config.setSubtitleStreams(List.of(
                createSubtitleStream("subtitle-1", "en", "subtitles"),
                createSubtitleStream("subtitle-2", "es", "subtitles")));

        Document doc = parse(service.generateManifestXml(config));

        List<Element> sets = elements(doc, "AdaptationSet");
        assertThat(sets).hasSize(5); // 1 video + 2 audio langs + 2 subtitles
        assertThat(sets.stream().filter(s -> "video".equals(s.getAttribute("contentType")))).hasSize(1);
        assertThat(sets.stream().filter(s -> "audio".equals(s.getAttribute("contentType")))).hasSize(2);
        assertThat(sets.stream().filter(s -> "text".equals(s.getAttribute("contentType")))).hasSize(2);
    }
}