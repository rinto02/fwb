/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os.vibrator.persistence;

import static android.os.VibrationEffect.Composition.PRIMITIVE_CLICK;
import static android.os.VibrationEffect.Composition.PRIMITIVE_LOW_TICK;
import static android.os.VibrationEffect.Composition.PRIMITIVE_SPIN;
import static android.os.VibrationEffect.Composition.PRIMITIVE_TICK;
import static android.os.vibrator.persistence.VibrationXmlParser.isSupportedMimeType;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertThrows;

import android.os.VibrationEffect;
import android.os.vibrator.PrebakedSegment;
import android.util.Xml;

import com.android.modules.utils.TypedXmlPullParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for {@link VibrationXmlParser} and {@link VibrationXmlSerializer}.
 *
 * <p>The {@link VibrationEffect} public APIs are covered by CTS to enforce the schema defined at
 * services/core/xsd/vibrator/vibration/vibration.xsd.
 */
@RunWith(JUnit4.class)
public class VibrationEffectXmlSerializationTest {

    @Test
    public void isSupportedMimeType_onlySupportsVibrationXmlMimeType() {
        // Single MIME type supported
        assertThat(isSupportedMimeType(
                VibrationXmlParser.APPLICATION_VIBRATION_XML_MIME_TYPE)).isTrue();
        assertThat(isSupportedMimeType("application/vnd.android.haptics.vibration+xml")).isTrue();
        // without xml suffix not supported
        assertThat(isSupportedMimeType("application/vnd.android.haptics.vibration")).isFalse();
        // different top-level not supported
        assertThat(isSupportedMimeType("haptics/vnd.android.haptics.vibration+xml")).isFalse();
        // different type not supported
        assertThat(isSupportedMimeType("application/vnd.android.vibration+xml")).isFalse();
    }

    @Test
    public void testParseElement_fromVibrationTag_succeedAndParserPointsToEndVibrationTag()
            throws Exception {
        VibrationEffect effect = VibrationEffect.startComposition()
                .addPrimitive(PRIMITIVE_CLICK)
                .addPrimitive(PRIMITIVE_TICK, 0.2497f)
                .compose();
        String xml = "<vibration-effect>"
                + "<primitive-effect name=\"click\"/>"
                + "<primitive-effect name=\"tick\" scale=\"0.2497\"/>"
                + "</vibration-effect>";
        VibrationEffect effect2 = VibrationEffect.startComposition()
                .addPrimitive(PRIMITIVE_LOW_TICK, 1f, 356)
                .addPrimitive(PRIMITIVE_SPIN, 0.6364f, 7)
                .compose();
        String xml2 = "<vibration-effect>"
                + "<primitive-effect name=\"low_tick\" delayMs=\"356\"/>"
                + "<primitive-effect name=\"spin\" scale=\"0.6364\" delayMs=\"7\"/>"
                + "</vibration-effect>";

        TypedXmlPullParser parser = createXmlPullParser(xml);
        assertParseElementSucceeds(parser, effect);
        parser.next();
        assertEndOfDocument(parser);

        // Test no-issues when an end-tag follows the vibration XML.
        // To test this, starting with the corresponding "start-tag" is necessary.
        parser = createXmlPullParser("<next-tag>" + xml + "</next-tag>");
        // Move the parser once to point to the "<vibration-effect> tag.
        parser.next();
        assertParseElementSucceeds(parser, effect);
        parser.next();
        assertEndTag(parser, "next-tag");

        parser = createXmlPullParser(xml + "<next-tag>");
        assertParseElementSucceeds(parser, effect);
        parser.next();
        assertStartTag(parser, "next-tag");

        parser = createXmlPullParser(xml + xml2);
        assertParseElementSucceeds(parser, effect);
        parser.next();
        assertParseElementSucceeds(parser, effect2);
        parser.next();
        assertEndOfDocument(parser);

        // Check when there is comment before the end tag.
        xml = "<vibration-effect><primitive-effect name=\"tick\"/><!-- hi --></vibration-effect>";
        parser = createXmlPullParser(xml);
        assertParseElementSucceeds(
                parser, VibrationEffect.startComposition().addPrimitive(PRIMITIVE_TICK).compose());
    }

    @Test
    public void
            testParseElement_fromVibrationSelectTag_succeedAndParserPointsToEndVibrationSelectTag()
                    throws Exception {
        VibrationEffect effect1 = VibrationEffect.startComposition()
                .addPrimitive(PRIMITIVE_CLICK)
                .addPrimitive(PRIMITIVE_TICK, 0.2497f)
                .compose();
        String vibrationXml1 = "<vibration-effect>"
                + "<primitive-effect name=\"click\"/>"
                + "<primitive-effect name=\"tick\" scale=\"0.2497\"/>"
                + "</vibration-effect>";
        VibrationEffect effect2 = VibrationEffect.startComposition()
                .addPrimitive(PRIMITIVE_LOW_TICK, 1f, 356)
                .addPrimitive(PRIMITIVE_SPIN, 0.6364f, 7)
                .compose();
        String vibrationXml2 = "<vibration-effect>"
                + "<primitive-effect name=\"low_tick\" delayMs=\"356\"/>"
                + "<primitive-effect name=\"spin\" scale=\"0.6364\" delayMs=\"7\"/>"
                + "</vibration-effect>";

        String xml = "<vibration-select>" + vibrationXml1 + vibrationXml2 + "</vibration-select>";
        TypedXmlPullParser parser = createXmlPullParser(xml);
        assertParseElementSucceeds(parser, effect1, effect2);
        parser.next();
        assertEndOfDocument(parser);

        // Test no-issues when an end-tag follows the vibration XML.
        // To test this, starting with the corresponding "start-tag" is necessary.
        parser = createXmlPullParser("<next-tag>" + xml + "</next-tag>");
        // Move the parser once to point to the "<vibration-effect> tag.
        parser.next();
        assertParseElementSucceeds(parser, effect1, effect2);
        parser.next();
        assertEndTag(parser, "next-tag");

        parser = createXmlPullParser(xml + "<next-tag>");
        assertParseElementSucceeds(parser, effect1, effect2);
        parser.next();
        assertStartTag(parser, "next-tag");

        xml = "<vibration-select>" + vibrationXml1 + vibrationXml2 + "</vibration-select>"
                + "<vibration-select>" + vibrationXml2 + vibrationXml1 + "</vibration-select>"
                + vibrationXml1;
        parser = createXmlPullParser(xml);
        assertParseElementSucceeds(parser, effect1, effect2);
        parser.next();
        assertParseElementSucceeds(parser, effect2, effect1);
        parser.next();
        assertParseElementSucceeds(parser, effect1);
        parser.next();
        assertEndOfDocument(parser);

        // Check when there is comment before the end tag.
        xml = "<vibration-select>" + vibrationXml1 + "<!-- comment --></vibration-select>";
        parser = createXmlPullParser(xml);
        parser.next();
        assertParseElementSucceeds(parser, effect1);
    }

    @Test
    public void testParseElement_withHiddenApis_onlySucceedsWithFlag() throws Exception {
        // Check when the root tag is "vibration".
        String xml =
                "<vibration-effect><predefined-effect name=\"texture_tick\"/></vibration-effect>";
        assertParseElementSucceeds(createXmlPullParser(xml),
                VibrationXmlSerializer.FLAG_ALLOW_HIDDEN_APIS,
                VibrationEffect.get(VibrationEffect.EFFECT_TEXTURE_TICK));
        assertParseElementFails(xml);

        // Check when the root tag is "vibration-select".
        xml = "<vibration-select>" + xml + "</vibration-select>";
        assertParseElementSucceeds(createXmlPullParser(xml),
                VibrationXmlSerializer.FLAG_ALLOW_HIDDEN_APIS,
                VibrationEffect.get(VibrationEffect.EFFECT_TEXTURE_TICK));
        assertParseElementFails(xml);
    }

    @Test
    public void testParseElement_badXml_throwsException() throws Exception {
        // No "vibration-select" tag.
        assertParseElementFails(
                "<vibration-effect>rand text<primitive-effect name=\"click\"/></vibration-effect>");
        assertParseElementFails("<bad-tag><primitive-effect name=\"click\"/></vibration-effect>");
        assertParseElementFails("<primitive-effect name=\"click\"/></vibration-effect>");
        assertParseElementFails("<vibration-effect><primitive-effect name=\"click\"/>");

        // Incomplete XML.
        assertParseElementFails("<vibration-select><primitive-effect name=\"click\"/>");
        assertParseElementFails("<vibration-select>"
                + "<vibration-effect>"
                + "<primitive-effect name=\"low_tick\" delayMs=\"356\"/>"
                + "</vibration-effect>");

        // Bad vibration XML.
        assertParseElementFails("<vibration-select>"
                + "<primitive-effect name=\"low_tick\" delayMs=\"356\"/>"
                + "</vibration-effect>"
                + "</vibration-select>");

        // "vibration-select" tag should have no attributes.
        assertParseElementFails("<vibration-select bad_attr=\"123\">"
                + "<vibration-effect>"
                + "<predefined-effect name=\"tick\"/>"
                + "</vibration-effect>"
                + "</vibration-select>");
    }

    @Test
    public void testPrimitives_allSucceed() throws IOException {
        VibrationEffect effect = VibrationEffect.startComposition()
                .addPrimitive(PRIMITIVE_CLICK)
                .addPrimitive(PRIMITIVE_TICK, 0.2497f)
                .addPrimitive(PRIMITIVE_LOW_TICK, 1f, 356)
                .addPrimitive(PRIMITIVE_SPIN, 0.6364f, 7)
                .compose();
        String xml = "<vibration-effect>"
                + "<primitive-effect name=\"click\"/>"
                + "<primitive-effect name=\"tick\" scale=\"0.2497\"/>"
                + "<primitive-effect name=\"low_tick\" delayMs=\"356\"/>"
                + "<primitive-effect name=\"spin\" scale=\"0.6364\" delayMs=\"7\"/>"
                + "</vibration-effect>";

        assertPublicApisParserSucceeds(xml, effect);
        assertPublicApisSerializerSucceeds(effect, "click", "tick", "low_tick", "spin");
        assertPublicApisRoundTrip(effect);

        assertHiddenApisParseVibrationEffectSucceeds(xml, effect);
        assertHiddenApisSerializerSucceeds(effect, "click", "tick", "low_tick", "spin");
        assertHiddenApisRoundTrip(effect);
    }

    @Test
    public void testParseDocument_withVibrationSelectTag_withHiddenApis_onlySucceedsWithFlag()
            throws Exception {
        // Check when the root tag is "vibration-effect".
        String xml =
                "<vibration-effect><predefined-effect name=\"texture_tick\"/></vibration-effect>";
        assertParseDocumentSucceeds(xml,
                VibrationXmlSerializer.FLAG_ALLOW_HIDDEN_APIS,
                VibrationEffect.get(VibrationEffect.EFFECT_TEXTURE_TICK));
        assertThat(parseDocument(xml, /* flags= */ 0)).isNull();

        // Check when the root tag is "vibration-select".
        xml = "<vibration-select>" + xml + "</vibration-select>";
        assertParseDocumentSucceeds(xml,
                VibrationXmlSerializer.FLAG_ALLOW_HIDDEN_APIS,
                VibrationEffect.get(VibrationEffect.EFFECT_TEXTURE_TICK));
        assertThat(parseDocument(xml, /* flags= */ 0)).isNull();
    }

    @Test
    public void testWaveforms_allSucceed() throws IOException {
        VibrationEffect effect = VibrationEffect.createWaveform(new long[]{123, 456, 789, 0},
                new int[]{254, 1, 255, 0}, /* repeat= */ 0);
        String xml = "<vibration-effect>"
                + "<waveform-effect><repeating>"
                + "<waveform-entry durationMs=\"123\" amplitude=\"254\"/>"
                + "<waveform-entry durationMs=\"456\" amplitude=\"1\"/>"
                + "<waveform-entry durationMs=\"789\" amplitude=\"255\"/>"
                + "<waveform-entry durationMs=\"0\" amplitude=\"0\"/>"
                + "</repeating></waveform-effect>"
                + "</vibration-effect>";

        assertPublicApisParserSucceeds(xml, effect);
        assertPublicApisSerializerSucceeds(effect, "123", "456", "789", "254", "1", "255", "0");
        assertPublicApisRoundTrip(effect);

        assertHiddenApisParseVibrationEffectSucceeds(xml, effect);
        assertHiddenApisSerializerSucceeds(effect, "123", "456", "789", "254", "1", "255", "0");
        assertHiddenApisRoundTrip(effect);
    }

    @Test
    public void testPredefinedEffects_publicEffectsWithDefaultFallback_allSucceed()
            throws IOException {
        for (Map.Entry<String, Integer> entry : createPublicPredefinedEffectsMap().entrySet()) {
            VibrationEffect effect = VibrationEffect.get(entry.getValue());
            String xml = String.format(
                    "<vibration-effect><predefined-effect name=\"%s\"/></vibration-effect>",
                    entry.getKey());

            assertPublicApisParserSucceeds(xml, effect);
            assertPublicApisSerializerSucceeds(effect, entry.getKey());
            assertPublicApisRoundTrip(effect);

            assertHiddenApisParseVibrationEffectSucceeds(xml, effect);
            assertHiddenApisSerializerSucceeds(effect, entry.getKey());
            assertHiddenApisRoundTrip(effect);
        }
    }

    @Test
    public void testPredefinedEffects_hiddenEffects_onlySucceedsWithFlag() throws IOException {
        for (Map.Entry<String, Integer> entry : createHiddenPredefinedEffectsMap().entrySet()) {
            VibrationEffect effect = VibrationEffect.get(entry.getValue());
            String xml = String.format(
                    "<vibration-effect><predefined-effect name=\"%s\"/></vibration-effect>",
                    entry.getKey());

            assertPublicApisParserFails(xml);
            assertPublicApisSerializerFails(effect);

            assertHiddenApisParseVibrationEffectSucceeds(xml, effect);
            assertHiddenApisSerializerSucceeds(effect, entry.getKey());
            assertHiddenApisRoundTrip(effect);
        }
    }

    @Test
    public void testPredefinedEffects_allEffectsWithNonDefaultFallback_onlySucceedsWithFlag()
            throws IOException {
        for (Map.Entry<String, Integer> entry : createAllPredefinedEffectsMap().entrySet()) {
            boolean nonDefaultFallback = !PrebakedSegment.DEFAULT_SHOULD_FALLBACK;
            VibrationEffect effect = VibrationEffect.get(entry.getValue(), nonDefaultFallback);
            String xml = String.format(
                    "<vibration-effect><predefined-effect name=\"%s\" fallback=\"%s\"/>"
                            + "</vibration-effect>",
                    entry.getKey(), nonDefaultFallback);

            assertPublicApisParserFails(xml);
            assertPublicApisSerializerFails(effect);

            assertHiddenApisParseVibrationEffectSucceeds(xml, effect);
            assertHiddenApisSerializerSucceeds(effect, entry.getKey());
            assertHiddenApisRoundTrip(effect);
        }
    }

    private void assertPublicApisParserFails(String xml) throws IOException {
        assertThat(parseVibrationEffect(xml, /* flags= */ 0)).isNull();
    }

    private void assertPublicApisParserSucceeds(String xml, VibrationEffect effect)
            throws IOException {
        assertThat(parseVibrationEffect(xml, /* flags= */ 0)).isEqualTo(effect);
    }

    private TypedXmlPullParser createXmlPullParser(String xml) throws Exception {
        TypedXmlPullParser parser = Xml.newFastPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(new StringReader(xml));
        parser.next(); // read START_DOCUMENT
        return parser;
    }

    private void assertParseDocumentSucceeds(String xml, int flags, VibrationEffect... effects)
            throws Exception {
        assertThat(parseDocument(xml, flags).getVibrationEffects()).containsExactly(effects);
    }

    /**
     * Asserts parsing vibration from an open TypedXmlPullParser succeeds, and that the parser
     * points to the end "vibration" or "vibration-select" tag.
     */
    private void assertParseElementSucceeds(
            TypedXmlPullParser parser, VibrationEffect... effects) throws Exception {
        assertParseElementSucceeds(parser, VibrationXmlParser.FLAG_ALLOW_HIDDEN_APIS, effects);
    }

    private void assertParseElementSucceeds(
            TypedXmlPullParser parser, int flags, VibrationEffect... effects) throws Exception {
        String tagName = parser.getName();
        assertThat(Set.of("vibration-effect", "vibration-select")).contains(tagName);

        assertThat(parseElement(parser, flags).getVibrationEffects()).containsExactly(effects);
        assertThat(parser.getEventType()).isEqualTo(XmlPullParser.END_TAG);
        assertThat(parser.getName()).isEqualTo(tagName);
    }

    private void assertEndTag(TypedXmlPullParser parser, String expectedTagName) throws Exception {
        assertThat(parser.getName()).isEqualTo(expectedTagName);
        assertThat(parser.getEventType()).isEqualTo(parser.END_TAG);
    }

    private void assertStartTag(TypedXmlPullParser parser, String expectedTagName)
            throws Exception {
        assertThat(parser.getName()).isEqualTo(expectedTagName);
        assertThat(parser.getEventType()).isEqualTo(parser.START_TAG);
    }

    private void assertEndOfDocument(TypedXmlPullParser parser) throws Exception {
        assertThat(parser.getEventType()).isEqualTo(parser.END_DOCUMENT);
    }

    private void assertHiddenApisParseVibrationEffectSucceeds(String xml, VibrationEffect effect)
            throws IOException {
        assertThat(parseVibrationEffect(xml, VibrationXmlParser.FLAG_ALLOW_HIDDEN_APIS))
                .isEqualTo(effect);
    }

    private void assertPublicApisSerializerFails(VibrationEffect effect) {
        assertThrows("Expected serialization to fail for " + effect,
                VibrationXmlSerializer.SerializationFailedException.class,
                () -> serialize(effect, /* flags= */ 0));
    }

    private void assertParseElementFails(String xml) {
        assertThrows("Expected parsing to fail for " + xml,
                VibrationXmlParser.VibrationXmlParserException.class,
                () -> parseElement(createXmlPullParser(xml), /* flags= */ 0));
    }

    private void assertPublicApisSerializerSucceeds(VibrationEffect effect,
            String... expectedSegments) throws IOException {
        assertSerializationContainsSegments(serialize(effect, /* flags= */ 0), expectedSegments);
    }

    private void assertHiddenApisSerializerSucceeds(VibrationEffect effect,
            String... expectedSegments) throws IOException {
        assertSerializationContainsSegments(
                serialize(effect, VibrationXmlSerializer.FLAG_ALLOW_HIDDEN_APIS), expectedSegments);
    }

    private void assertSerializationContainsSegments(String xml, String[] expectedSegments) {
        for (String expectedSegment : expectedSegments) {
            assertThat(xml).contains(expectedSegment);
        }
    }

    private void assertPublicApisRoundTrip(VibrationEffect effect) throws IOException {
        assertThat(parseVibrationEffect(serialize(effect, /* flags= */ 0), /* flags= */ 0))
                .isEqualTo(effect);
    }

    private void assertHiddenApisRoundTrip(VibrationEffect effect) throws IOException {
        String xml = serialize(effect, VibrationXmlSerializer.FLAG_ALLOW_HIDDEN_APIS);
        assertThat(parseVibrationEffect(xml, VibrationXmlParser.FLAG_ALLOW_HIDDEN_APIS))
                .isEqualTo(effect);
    }

    private static VibrationEffect parseVibrationEffect(
            String xml, @VibrationXmlParser.Flags int flags) throws IOException {
        return VibrationXmlParser.parseVibrationEffect(new StringReader(xml), flags);
    }

    private static ParsedVibration parseDocument(String xml, int flags)
            throws IOException {
        return VibrationXmlParser.parseDocument(new StringReader(xml), flags);
    }

    private static ParsedVibration parseElement(TypedXmlPullParser parser, int flags)
            throws IOException, VibrationXmlParser.VibrationXmlParserException {
        return VibrationXmlParser.parseElement(parser, flags);
    }

    private static String serialize(VibrationEffect effect, @VibrationXmlSerializer.Flags int flags)
            throws IOException {
        StringWriter writer = new StringWriter();
        VibrationXmlSerializer.serialize(effect, writer, flags);
        return writer.toString();
    }

    private static Map<String, Integer> createAllPredefinedEffectsMap() {
        Map<String, Integer> map = createHiddenPredefinedEffectsMap();
        map.putAll(createPublicPredefinedEffectsMap());
        return map;
    }

    private static Map<String, Integer> createPublicPredefinedEffectsMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("tick", VibrationEffect.EFFECT_TICK);
        map.put("click", VibrationEffect.EFFECT_CLICK);
        map.put("heavy_click", VibrationEffect.EFFECT_HEAVY_CLICK);
        map.put("double_click", VibrationEffect.EFFECT_DOUBLE_CLICK);
        return map;
    }

    private static Map<String, Integer> createHiddenPredefinedEffectsMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("texture_tick", VibrationEffect.EFFECT_TEXTURE_TICK);
        map.put("pop", VibrationEffect.EFFECT_POP);
        map.put("thud", VibrationEffect.EFFECT_THUD);
        for (int i = 0; i < VibrationEffect.RINGTONES.length; i++) {
            map.put(String.format("ringtone_%d", i + 1), VibrationEffect.RINGTONES[i]);
        }
        return map;
    }
}