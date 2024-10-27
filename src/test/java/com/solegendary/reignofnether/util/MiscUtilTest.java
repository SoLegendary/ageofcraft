package com.solegendary.reignofnether.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiscUtilTest {

    @Test
    void shadeHexRGB() {
        int rgb = 0x646464; // == rgb(100,100,100)
        assertAll(
            () -> assertEquals(MiscUtil.shadeHexRGB(rgb, 2.0f), 0xC8C8C8),
            () -> assertEquals(MiscUtil.shadeHexRGB(rgb, 0.5f), 0x323232),
            () -> assertEquals(MiscUtil.shadeHexRGB(rgb, 0.0f), 0x000000)
        );
    }

    @Test
    void reverseHexRGB() {
        int rgb = 0xAABBCC; // == rgb(100,100,100)
        assertEquals(MiscUtil.reverseHexRGB(rgb), 0xCCBBAA);
    }

    @Test
    void getOscillatingFloat() {
        float output = MiscUtil.getOscillatingFloat(0.5d, 1.5d);
        assertTrue(output >= 0.5d && output <= 1.5d);
    }
}