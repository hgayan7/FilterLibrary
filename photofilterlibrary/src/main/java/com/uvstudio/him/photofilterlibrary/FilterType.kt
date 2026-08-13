package com.uvstudio.him.photofilterlibrary

/**
 * Enumeration of all available photo filters and visual presets.
 *
 * Each filter belongs to a [FilterCategory] and includes human-readable display names.
 */
enum class FilterType(
    val displayName: String,
    val category: FilterCategory,
    val description: String
) {
    // ==========================================
    // CLASSIC & MONOCHROME
    // ==========================================
    GRAYSCALE(
        "Grayscale",
        FilterCategory.CLASSIC,
        "Clean, balanced standard black and white."
    ),
    NOIR(
        "Noir",
        FilterCategory.CLASSIC,
        "High-contrast, dramatic deep shadows and crisp monochrome highlights."
    ),
    SEPIA(
        "Sepia",
        FilterCategory.CLASSIC,
        "Timeless warm antique sepia tones."
    ),
    MONO_WARM(
        "Mono Warm",
        FilterCategory.CLASSIC,
        "Subtle warm cream-tinted monochrome."
    ),
    MONO_COOL(
        "Mono Cool",
        FilterCategory.CLASSIC,
        "Slate and steel blue toned black and white."
    ),
    SILVER_TONE(
        "Silver Tone",
        FilterCategory.CLASSIC,
        "Luminous, metallic high-key silver monochrome."
    ),

    // ==========================================
    // VINTAGE & ANALOG FILM
    // ==========================================
    VINTAGE(
        "Vintage",
        FilterCategory.VINTAGE,
        "Nostalgic film look with warm golden highlights and lifted blacks."
    ),
    POLAROID_70S(
        "Polaroid '70s",
        FilterCategory.VINTAGE,
        "Authentic 1970s instant film with warm shadow shifts."
    ),
    KODAK_CHROME(
        "Kodak Chrome",
        FilterCategory.VINTAGE,
        "Punchy reds, rich saturation, and classic 1980s color reversal film look."
    ),
    KODAK_PORTRA(
        "Portra 400",
        FilterCategory.VINTAGE,
        "Soft pastel warmth, gentle highlight roll-off, and glowing skin tones."
    ),
    FUJI_VELVIA(
        "Fuji Velvia",
        FilterCategory.VINTAGE,
        "Vivid saturated colors, lush greens, and deep ocean blues."
    ),
    CROSS_PROCESS(
        "Cross Process",
        FilterCategory.VINTAGE,
        "Eclectic chemical cross-processing with quirky cyan shadows and warm highlights."
    ),
    LOMO(
        "Lomo",
        FilterCategory.VINTAGE,
        "Saturated center contrast with retro dark vignetted edges."
    ),
    RETRO_WARM(
        "Retro Warm",
        FilterCategory.VINTAGE,
        "Warm nostalgic tones inspired by 1960s photography."
    ),
    FILM_GRAIN(
        "Film Grain",
        FilterCategory.VINTAGE,
        "Organic analog film grain overlay."
    ),

    // ==========================================
    // CINEMATIC & MOODY
    // ==========================================
    TEAL_AND_ORANGE(
        "Teal & Orange",
        FilterCategory.CINEMATIC,
        "Modern blockbuster cinema grade with teal shadows and warm orange skin tones."
    ),
    CYBERPUNK(
        "Cyberpunk",
        FilterCategory.CINEMATIC,
        "Electric neon magenta highlights with moody deep cyan shadows."
    ),
    BLEACH_BYPASS(
        "Bleach Bypass",
        FilterCategory.CINEMATIC,
        "High-contrast, reduced saturation, gritty cinematic look."
    ),
    WARM_SUNSET(
        "Warm Sunset",
        FilterCategory.CINEMATIC,
        "Rich golden hour glow with warm amber tones."
    ),
    COOL_BLUE(
        "Cool Blue",
        FilterCategory.CINEMATIC,
        "Serene cool aquatic and marine blue grade."
    ),
    DRAMATIC(
        "Dramatic",
        FilterCategory.CINEMATIC,
        "Intense moody contrast with deep desaturated tones."
    ),
    ROSE_GOLD(
        "Rose Gold",
        FilterCategory.CINEMATIC,
        "Dreamy, elegant soft pink highlights and warm midtones."
    ),
    MOODY_DARK(
        "Moody Dark",
        FilterCategory.CINEMATIC,
        "Under-exposed, deep forest green shadows with moody atmosphere."
    ),
    HIGH_KEY(
        "High Key",
        FilterCategory.CINEMATIC,
        "Luminous, bright, ethereal exposure with soft contrast."
    ),
    CINEMATIC_POP(
        "Cinematic Pop",
        FilterCategory.CINEMATIC,
        "Punchy dynamic color separation and rich tone curve."
    ),

    // ==========================================
    // SOCIAL & LIFESTYLE
    // ==========================================
    CLARENDON(
        "Clarendon",
        FilterCategory.SOCIAL,
        "Brightens highlights, deepens shadows, and boosts overall vibrancy."
    ),
    JUNO(
        "Juno",
        FilterCategory.SOCIAL,
        "Accentuates warm tones and glowing, healthy skin."
    ),
    VALENCIA(
        "Valencia",
        FilterCategory.SOCIAL,
        "Warm pastel fade with soft antique lighting."
    ),
    ANIME_VIBE(
        "Anime Vibe",
        FilterCategory.SOCIAL,
        "Radiant sky blues, lush greens, and dreamy vibrant highlights."
    ),
    SLUMBER(
        "Slumber",
        FilterCategory.SOCIAL,
        "Desaturated matte finish with a dreamy warm haze."
    ),
    LARK(
        "Lark",
        FilterCategory.SOCIAL,
        "Brightens image, cools shadows, and enhances nature blues and greens."
    ),

    // ==========================================
    // ARTISTIC, CONVOLUTION & SPECIAL FX
    // ==========================================
    CYANOTYPE(
        "Cyanotype",
        FilterCategory.ARTISTIC,
        "Historic 19th-century Prussian blue blueprint monochrome."
    ),
    RGB_GLITCH_SHIFT(
        "RGB Glitch",
        FilterCategory.ARTISTIC,
        "Chromatic aberration and horizontal color channel displacement."
    ),
    THERMAL_VISION(
        "Thermal Vision",
        FilterCategory.ARTISTIC,
        "Infrared heat-map color distribution (Blue to Yellow to Red)."
    ),
    POSTERIZE(
        "Posterize",
        FilterCategory.ARTISTIC,
        "Quantized color levels for pop-art poster aesthetics."
    ),
    COMIC_DOTS(
        "Comic Dots",
        FilterCategory.ARTISTIC,
        "Pop-art vintage halftone print screen simulation."
    ),
    EMBOSS(
        "Emboss",
        FilterCategory.ARTISTIC,
        "3D textured relief engraving."
    ),
    SHARPEN(
        "Sharpen",
        FilterCategory.ARTISTIC,
        "Crisp, crystal-clear edge and micro-detail enhancement."
    ),
    SOFT_BLUR(
        "Soft Blur",
        FilterCategory.ARTISTIC,
        "Silky soft focus and optical diffusion."
    ),
    GAUSSIAN_BLUR(
        "Gaussian Blur",
        FilterCategory.ARTISTIC,
        "Smooth variable-radius Gaussian blur."
    ),
    EDGE_DETECT(
        "Edge Detect",
        FilterCategory.ARTISTIC,
        "High-contrast contour sketch and outline mapping."
    ),
    INVERT(
        "Invert",
        FilterCategory.ARTISTIC,
        "Color negative tone inversion."
    ),
    CYAN_BOOST(
        "Cyan Boost",
        FilterCategory.ARTISTIC,
        "Atmospheric cyan and emerald tint shift."
    ),
    BRIGHTNESS_BOOST(
        "Exposure Boost",
        FilterCategory.ARTISTIC,
        "High exposure luminance amplification."
    ),
    SOLARIZE(
        "Solarize",
        FilterCategory.ARTISTIC,
        "Sabatier partial tone reversal effect."
    ),
    VIGNETTE(
        "Vignette",
        FilterCategory.ARTISTIC,
        "Radial optical corner darkening."
    ),
    DUOTONE(
        "Duotone",
        FilterCategory.ARTISTIC,
        "Stylized dual-tone color mapping (Shadows to Highlights)."
    ),
    HALD_LUT(
        "3D LUT",
        FilterCategory.ARTISTIC,
        "Custom 3D Color Lookup Table (Adobe Lightroom / Photoshop PNG)."
    );

    companion object {
        /**
         * Returns all filters belonging to a specific category.
         */
        @JvmStatic
        fun getByCategory(category: FilterCategory): List<FilterType> {
            return if (category == FilterCategory.ALL) {
                entries
            } else {
                entries.filter { it.category == category }
            }
        }
    }
}
